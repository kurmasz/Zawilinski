package edu.gvsu.kurmasz.zawilinski;

import edu.gvsu.kurmasz.warszawa.deprecated.joswa.JoswaOption;
import edu.gvsu.kurmasz.warszawa.deprecated.joswa.JoswaOptionParser;
import edu.gvsu.kurmasz.warszawa.io.InputHelper;
import edu.gvsu.kurmasz.warszawa.log.Log;
import edu.gvsu.kurmasz.warszawa.log.SimpleLog;
import edu.gvsu.kurmasz.zawilinski.mw.current.*;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Load the raw inflection template strings from a filtered MediaWiki dump.  This code handles only one langauge at a
 * time, and assumes that the input has already been filtered to contain data for that language only.
 * <p/>
 * Created by kurmasz on 3/24/17.
 */
public class InflectionTemplateLoader {

  private static final java.io.PrintStream usage_out = System.err;
  private static final java.io.PrintStream error_out = System.err;

  // Log levels
  private static final int PAGE_PROGRESS = 25;
  private static final int REVISION_PROGRESS = PAGE_PROGRESS - 2;
  private static final int RESULT = REVISION_PROGRESS - 2;
  private static final int PROBLEM = RESULT - 2;
  private static final int SHOW_TEMPLATE = RESULT - 2;


  /****************************************************************
   * Command-line parameters for this class
   *
   * @author Zachary Kurmas
   ****************************************************************/
  // (C) 2010 Zachary Kurmas
  // Created Feb 27, 2010
  public static class MyOptions {

    @JoswaOption(shortName = 'h', usage = "print this help message")
    public Boolean help = false;

    @JoswaOption(usage = "file to which progress messages are printed.", argName = "file")
    public String progressLog = null;

    @JoswaOption(usage = "level above which progress messages are printed.", argName = "level")
    public Integer progressLevel = RESULT;

    @JoswaOption(usage = "file to which templates are written", argName = "file")
    public String templateFile = null;

    @JoswaOption(usage = "file to which the words and their inflection data are written", argName = "file")
    public String wordFile = null;

    @JoswaOption(usage = "file to which the names of unrecognized templates are written.", argName = "file")
    public String unrecognizedTemplateFile = null;

    public String inputFile = "-";
  }

  /****************************************************************
   * Parts of speech
   *
   * @author Zachary Kurmas
   ****************************************************************/
  // (C) 2010 Zachary Kurmas
  // Created Feb 13, 2010
  private enum PartOfSpeech {
    VERB("Conjugation"), NOUN("Declension"), ADJECTIVE("Declension");

    public String inflectionType;

    private PartOfSpeech(String it) {
      inflectionType = it;
    }
  }


 public interface TemplateCallback {
    void callback(String word, RevisionType revision, String template);
  }

  // Private so it can't be instantiated.
  private InflectionTemplateLoader() {
    return;
  }

  /**************************************************************************
   * Examine a Wiktionary dump and extract the inflection template for those
   * {@code &lt;revisions&gt; } containing one
   *
   * @param mw       The result of unmarshalling a Wiktionary xml dump.
   * @param callback the function to be called when a tempalte is found
   **************************************************************************/
  public static void loadTemplates(MediaWikiType mw, TemplateCallback callback, SimpleLog progressLog) {
    for (PageType page : mw.getPage()) {
      progressLog.println(PAGE_PROGRESS, page.getTitle());
      String currentTitle = page.getTitle();

      int count = 0;
      for (Object object : page.getRevisionOrUpload()) {
        if (object instanceof UploadType) {
          continue;
        }

        assert object instanceof RevisionType : "Unexpected type in list";

        count++;
        RevisionType revision = (RevisionType) object;
        progressLog.println(REVISION_PROGRESS, String.format(
            "%10s Revision %4d (id %s): ", currentTitle, count,
            revision.getId().toString()));
        String template = getTemplate(revision, progressLog);
        callback.callback(currentTitle, revision, template);
      } // end foreach revision
    } // end foreach page
  } // end load Templates.

  public static void loadTemplates(MediaWikiType mw, TemplateCallback callback) {
    loadTemplates(mw, callback, new SimpleLog());
  }

  /*
   * Search through a wiktionary <revision> for a template containing
	 * inflection data.
	 */
  private static String getTemplate(RevisionType revision, SimpleLog progressLog) {
    String text = revision.getText().getValue();

    assert text.startsWith("==") : "Doesn't start with expected string!";

    // First, we need to find the part of speech.
    // Look through each line of text until you find the first level 4
    PartOfSpeech partOfSpeech = null;

    int place = 0;

    while (partOfSpeech == null) {
      // The '\n' at the beginning shouldn't be a problem since the entire
      // text should begin with ==Polish==
      place = text.indexOf("\n===", place);
      if (place == -1) {
        progressLog.println(PROBLEM,
            "No 3rd-level heading in revision.");
        return null;
      }
      // This is a level five heading (or deeper). ignore.
      // The 5 accounts for the five level headings *and* the \n we
      // matched with the last indexOf
      if (text.charAt(place + 5) == '=') {
        place = place + 6;
        continue;
      }

      int end = text.indexOf("===", place + 3);
      if (end == -1) {
        progressLog.println(PROBLEM, "Tag not balanced!");
        return null;
      }

      // The +4 passes both the newline *and* the ===
      String tag = text.substring(place + 4, end);
      // System.out.println("tag: " + tag);
      if (tag.charAt(0) != '=') {

        // start looking past the ending === tag
        place = end + 3;
        continue;
      }

      // remove the leading '=' sign
      String tag2 = tag.substring(1);
      // We have a level 4 tag
      // System.out.println(tag2);
      for (PartOfSpeech pos : PartOfSpeech.values()) {
        if (tag2.equals(pos.inflectionType)) {
          // Possible match, but, remember, both noun and adjectives
          // have declensions.
          partOfSpeech = pos;
          break;
        }
      }
      place = end;

    } // end while

    if (partOfSpeech == null) {
      progressLog.println(PROBLEM,
          "No inflection data (no level-4 heading)");
      return null;
    }

    // Now, get the template.
    int templateStart = text.indexOf("{{", place);
    if (templateStart == -1) {
      progressLog.println(PROBLEM, "No template");
      return null;
    }

    int templateEnd = text.indexOf("}}", templateStart + 2);

    if (templateEnd == -1) {
      progressLog.println(PROBLEM,
          "Template not parsable.  No ending tag.");
      return null;
    }

    String template = text.substring(templateStart + 2, templateEnd).trim()
        .replace('\n', ' ');
    progressLog.println(RESULT, "Found template.");
    progressLog.println(SHOW_TEMPLATE, ("\t->" + template + "<-"));

    return template;

  } // end make word


  public static void main(String[] args) throws JAXBException {

    //
    // parse the command line options
    //
    MyOptions options = new MyOptions();
    JoswaOptionParser parser = new JoswaOptionParser(options);
    List<String> leftovers = parser.parse(args);

    // If the user doesn't specify a file name, print usage information
    if (leftovers.size() < 1 || options.help) {
      usage_out.printf("Usage:  %s [options] <input>\n",
          InflectionTemplateLoader.class.getSimpleName());
      usage_out
          .println("<input> can be a file name, or \"-\" for standard input.");
      parser.printHelp(usage_out);
      return;
    }

    options.inputFile = leftovers.get(0);


    try {
      JAXBElement<MediaWikiType> root = MediaWikiLoader.load(InputHelper
          .openMappedAndFilteredInputStream(options.inputFile), new Log());

      InflectionTemplateLoader.loadTemplates(root.getValue(), new TemplateCallback() {
        @Override
        public void callback(String word, RevisionType revision, String template) {
          if (template != null) {
            System.out.println("-----");
            System.out.println(word);
            System.out.println(revision.getId());
            System.out.println(template);
          }
        }
      });
    } catch (FileNotFoundException fnfe) {
      error_out.println("Could not open \"" + options.inputFile
          + "\".");
    }

  }
}
