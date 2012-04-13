package edu.gvsu.kurmasz.zawilinski;

import edu.gvsu.kurmasz.warszawa.deprecated.joswa.JoswaOption;
import edu.gvsu.kurmasz.warszawa.deprecated.joswa.JoswaOptionParser;
import edu.gvsu.kurmasz.warszawa.io.InputHelper;
import edu.gvsu.kurmasz.warszawa.log.Log;
import edu.gvsu.kurmasz.zawilinski.mw.current.MediaWikiType;
import edu.gvsu.kurmasz.zawilinski.mw.current.PageType;
import edu.gvsu.kurmasz.zawilinski.mw.current.RevisionType;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Program to filter the data for a given language from Wiktionary. In
 * particular, this class filters out revisions and/or pages that don't contain
 * any data for the specified language.
 *
 * @author Zachary Kurmas
 */
// (C) 2010 Zachary Kurmas
// Created February 12, 2010
public class FilterWiktionaryByLanguage {

   private static final java.io.PrintStream error_out = System.err;
   private static final java.io.PrintStream usage_out = System.err;

   /**
    * Container for command-line options.
    *
    * @author Zachary Kurmas
    */
   // (C) 2010 Zachary Kurmas
   // Created February 27, 2010
   public static class MyOptions {
      public String inputFile = "-";
      public String language = null;

      /* Keeping all revisions lets you analyze things like (1) when the data for the language under study was entered relative
         to when the initial entry was created, or (2) whether the data for the language in quesion was entered and
         removed.
      */
      //@JoswaOption
      //public Boolean keepAllRevisions = false;

      /*
         Keeping all pages allows you to see which pages *don't* have data for the language under study.
       */
      //@JoswaOption()
      //public Boolean keepAllPages = false;


      @JoswaOption(shortName = 'h', usage = "display this help message")
      public boolean help = false;

      @JoswaOption(usage = "name of log file (or \"-\" for standard output)", argName = "file")
      public String logFile = null;

      @JoswaOption(argName = "level", usage = "minimum log level printed.")
      public Integer logLevel = MediaWikiLoader.PARSE_BEGIN_END;

      @JoswaOption(shortName = 'o', usage = "output file (or \"-\" for standard output)", argName = "file")
      public String outputFile = "-";

      @JoswaOption(argName = "file", usage = "file containing information on truncated entries")
      public String textSizeLog = null;

      @JoswaOption(argName = "limit", usage = "maximum number of characters passed to unmarshaller (per revision)")
      public Integer textSizeLimit = TextSizePrefilter.UNLIMITED;
   }

   /**
    * Keep only those pages that contain some data for the specified language.
    * IMPORTANT! This filter is written specifically to run after the {@code
    * LanguageFilter} SAX filter is applied. This SAX filter assures that all
    * text segments <em>begin</em> with the expected language string (e.g.,
    * "==Polish=="). If you don't apply the SAX filter, you need a post-filter
    * that checks to see if the revision text <em>contains</em> the
    * languageString.
    *
    * @author Zachary Kurmas
    */
   // (C) 2010 Zachary Kurmas
   // Created February 12, 2010
   private static class PostFilterByLanguage implements PostFilter {
      // The string that indicates the start of a language section in
      // Wiktionary.
      // This string typically looks like this ==Polish== (with the "==")
      private String languageString;

      /**
       * Constructor
       *
       * @param language the language to filter for.
       */
      public PostFilterByLanguage(String language) {
         this.languageString = "==" + language + "==";
      }

      // override
      public boolean keepPage(PageType page) {
         // If the total size of all revisions is 0 (or there are no
         // revisions), then this page contains no data for the requested
         // language.
         return Util.getTextSize(page) > 0;
      }

      public boolean keepRevision(RevisionType revision) {
         // IMPORTANT! Notice the "startsWith" This filter works properly
         // only if the LanguagePrefilter SAX filter has been applied (which
         // assures that each <text>segment begins with the requested
         // language headerContent.
         //return Util.getText(revision).startsWith(languageString);
         return Util.getTextSize(revision) > 0;
      }
   }

   /**
    * Main
    *
    * @param args
    * @throws JAXBException if there is a problem generating the DOM
    */
   public static void main(String[] args) throws JAXBException {

      // parse the command line.  Command line options are
      // public members of the options object.  The remaining
      // command line arguments (those that are not parameters) are placed into
      // leftovers.
      MyOptions options = new MyOptions();
      JoswaOptionParser option_parser = new JoswaOptionParser(options);
      List<String> leftovers = option_parser.parse(args);

      // Make sure there is at least one argument left: the language
      if (leftovers.size() < 1 || options.help) {
         usage_out.println("Usage:  "
               + FilterWiktionaryByLanguage.class.getSimpleName()
               + " <language> [file] [options]");
         option_parser.printHelp(usage_out);
         return;
      }
      // The language is the first argument
      options.language = leftovers.get(0);

      // if there is a second argument, it is the file name of the input.
      // default is stdin.  (The default value is defined in the MyOptions class.)
      if (leftovers.size() > 1) {
         options.inputFile = leftovers.get(1);
      }

      Log postFilterLog;
      if (options.logFile != null) {
         postFilterLog = Log.makeLogOrQuit(options.logFile, options.logLevel);
      } else {
         postFilterLog = new Log();
      }

      Log textSizeLog;
      if (options.textSizeLog != null) {
         textSizeLog = Log.makeLogOrQuit(options.textSizeLog, TextSizePrefilter.TRUNCATIONS);
      } else {
         textSizeLog = new Log();
      }


      // The pre-filter and post-filter work together.  The pre-filter removes all the text
      // from an entry that is outside the subsection for the particular language.  If an entry
      // does have a section for the specified language, then the data passed to the unmarshaller
      // only has text for the desired language.  If an entry does not have a section for the
      // specified language, then the text for that entry is empty.  The post-filter detects the
      // empty text section and deletes the DOM elements for that entry.  Notice that it is the
      // pre-filter that actually modifies the data.  The post filter simply decides which elements
      // to keep in the DOM and which to discard.

      LanguagePrefilter lpf = new LanguagePrefilter(options.language);
      PostFilterByLanguage postFilter = new PostFilterByLanguage(options.language);

      // pfwl.addSAXFilter(new BugChecker("Top"));
      // pfwl.addSAXFilter(new PerformanceFilter(new PrintStream(new

      // Some Wiktionary entries have been vandalized by adding several gigabytes of random
      // text.  This filter prevents these entries from unnecessarily slowing down (or
      // even crashing) the filter.
      TextSizePrefilter lts = new TextSizePrefilter(options.textSizeLimit, textSizeLog);

      WiktionaryWriter writer = new WiktionaryWriter();

      try {
         JAXBElement<MediaWikiType> root = PostFilteredMediaWikiLoader.loadFilteredPages(InputHelper
               .openFilteredInputStream(options.inputFile), postFilterLog, postFilter, lpf, lts);
         try {
            writer.write(root, options.outputFile);
         } catch (FileNotFoundException fnf) {
            error_out.println("Cannot open \"" + options.outputFile
                  + "\" for writing.");
         }
      } catch (FileNotFoundException fnfe) {
         error_out.println("Could not open \"" + options.inputFile
               + "\".");
      } catch (IOException e) {
         error_out.println("Could not open \"" + options.inputFile
               + "\" as a compressed file.");
      }
   } // end main
} // end FilterWiktionaryByLanguage
