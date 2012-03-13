package edu.gvsu.kurmasz.zawilinski;

import edu.gvsu.kurmasz.warszawa.deprecated.joswa.JoswaOption;
import edu.gvsu.kurmasz.zawilinski.MediaWikiLoader;
import edu.gvsu.kurmasz.zawilinski.PostFilteredMediaWikiLoader;
import edu.gvsu.kurmasz.zawilinski.TextSizePrefilter;
import edu.gvsu.kurmasz.zawilinski.Util;
import edu.gvsu.kurmasz.zawilinski.mw.current.PageType;
import edu.gvsu.kurmasz.zawilinski.mw.current.RevisionType;

import javax.xml.bind.JAXBException;

/****************************************************************
 * Program to filter the data for a given language from Wiktionary. In
 * particular, this class filters out revisions and/or pages that don't contain
 * any data for the specified language.
 * 
 * @author Zachary Kurmas
 * 
 ****************************************************************/
// (C) 2010 Zachary Kurmas
// Created February 12, 2010
public class FilterWiktionaryByLanguage
{
	private static final java.io.PrintStream usage_out = System.err;

	/****************************************************************
	 * Container for command-line options.
	 * 
	 * @author Zachary Kurmas
	 * 
	 ****************************************************************/
	// (C) 2010 Zachary Kurmas
	// Created February 27, 2010
	public static class MyOptions
	{
		public String inputFile = Util.STDIN;
		public String language = null;

		/*@JoswaOption
		public Boolean keepAllRevisions = false;

		@JoswaOption()
		public Boolean keepAllPages = false;
    */

		@JoswaOption(shortName = 'h', usage = "display this help message")
		public boolean help = false;

		@JoswaOption(usage = "name of log file (or \"-\" for standard output)", argName = "file")
		public String logFile = null;

		@JoswaOption(argName = "level", usage = "minimum log level printed.")
		public Integer logLevel = MediaWikiLoader.PARSE_BEGIN_END;

		@JoswaOption(shortName = 'o', usage = "output file (or \"-\" for standard output)", argName = "file")
		public String outputFile = Util.STDOUT;

		@JoswaOption(argName = "file", usage = "file containing information on truncated entries")
		public String textSizeLog = null;

		@JoswaOption(argName = "limit", usage = "maximum number of characters passed to unmarshaller (per revision)")
		public Integer textSizeLimit = TextSizePrefilter.UNLIMITED;
	}

	/****************************************************************
	 * 
	 * Keep only those pages that contain some data for the specified language.
	 * IMPORTANT! This filter is written specifically to run after the {@code
	 * LanguageFilter} SAX filter is applied. This SAX filter assures that all
	 * text segments <em>begin</em> with the expected language string (e.g.,
	 * "==Polish=="). If you don't apply the SAX filter, you need a post-filter
	 * that checks to see if the revision text <em>contains</em> the
	 * languageString.
	 * 
	 * @author Zachary Kurmas
	 * 
	 ****************************************************************/
	// (C) 2010 Zachary Kurmas
	// Created February 12, 2010
	private static class PostFilterByLanguage implements
			PostFilteredMediaWikiLoader.PostFilter
	{
		// The string that indicates the start of a language section in
		// Wiktionary.
		// This string typically looks like this ==Polish== (with the "==")
		private String languageString;

		/**************************************************************************
		 * Constructor
		 * 
		 * @param language
		 *            the language to filter for.
		 **************************************************************************/
		public PostFilterByLanguage(String language) {
			this.languageString = "==" + language + "==";
		}

		// override
		public boolean keepPage(PageType page)
		{
			// If the total size of all revisions is 0 (or there are no
			// revisions), then this page contains no data for the requested
			// language.
			return Util.getTextSize(page) > 0;
		}

		public boolean keepRevision(RevisionType revision)
		{
			// IMPORTANT! Notice the "startsWith" This filter works properly
			// only if the LanguagePrefilter SAX filter has been applied (which
			// assures that each <text>segment begins with the requested
			// language header.
			return Util.getText(revision).startsWith(languageString);
		}
	}

	/**************************************************************************
	 * Main
	 * 
	 * @param args

	 * @throws JAXBException if there is a problem generating the DOM
	 **************************************************************************/
//	public static void main(String[] args) throws JAXBException
//	{
//
//		// parse the command line.  Command line options are
//    // public members of the options object.  The remaining
//    // command line arguments (those that are not parameters) are placed into
//    // leftovers.
//		MyOptions options = new MyOptions();
//		JoswaOptionParser option_parser = new JoswaOptionParser(options);
//		List<String> leftovers = option_parser.parse(args);
//
//		// Make sure there is at least one argument left: the language
//		if (leftovers.size() < 1 || options.help) {
//			usage_out.println("Usage:  "
//					+ FilterWiktionaryByLanguage.class.getSimpleName()
//					+ " <language> [file] [options]");
//			option_parser.printHelp(usage_out);
//			return;
//		}
//    // The language is the first argument
//		options.language = leftovers.get(0);
//
//		// if there is a second argument, it is the file name of the input.
//		// default is stdin.  (The default value is defined in the MyOptions class.)
//		if (leftovers.size() > 1) {
//			options.inputFile = leftovers.get(1);
//		}
//
//    // The pre-filter and post-filter work together.  The pre-filter removes all the text
//    // from an entry that is outside the subsection for the particular language.  If an entry
//    // does have a section for the specified language, then the data passed to the unmarshaller
//    // only has text for the desired language.  If an entry does not have a section for the
//    // specified language, then the text for that entry is empty.  The post-filter detects the
//    // empty text section and deletes the DOM elements for that entry.  Notice that it is the
//    // pre-filter that actually modifies the data.  The post filter simply decides which elements
//    // to keep in the DOM and which to discard.
//
//
//
//		PostFilterByLanguage postFilter = new PostFilterByLanguage(options.language);
//		PostFilteredMediaWikiLoader pfwl = new PostFilteredMediaWikiLoader(
//        postFilter);
//
//		// pfwl.addSAXFilter(new BugChecker("Top"));
//		// pfwl.addSAXFilter(new PerformanceFilter(new PrintStream(new
//		// FileOutputStream("fwblPerformance.log"))));
//
//		pfwl.addSAXFilter(new LanguagePrefilter(options.language));
//
//    // Some Wiktionary entries have been vandalized by adding several gigabytes of random
//    // text.  This filter prevents these entries from unnecessarily slowing down (or
//    // even crashing) the filter.
//		TextSizePrefilter lts = new TextSizePrefilter(options.textSizeLimit);
//
//		// Set up the text postFilter log, if desired.
//		if (options.textSizeLimit != TextSizePrefilter.UNLIMITED) {
//			PrintWriter log = Util.openWriterOrQuit(options.textSizeLog);
//			//lts.getLog().setLog(log);
//		}
//		pfwl.addSAXFilter(lts);
//
//		WiktionaryWriter writer = new WiktionaryWriter();
//
//		//pfwl.getLog().setLogOrQuit(options.logFile);
//		//pfwl.getLog().setThreshold(options.logLevel);
//		try {
//			pfwl.setPreprocessedSource(options.inputFile);
//			pfwl.load();
//
//			try {
//				writer.write(pfwl.getRoot(), options.outputFile);
//			} catch (FileNotFoundException fnf) {
//				Util.error_out.println("Cannot open \"" + options.outputFile
//						+ "\" for writing.");
//			}
//
//		} catch (FileNotFoundException fnfe) {
//			Util.error_out.println("Could not open \"" + options.inputFile
//					+ "\".");
//		} catch (IOException e) {
//			Util.error_out.println("Could not open \"" + options.inputFile
//					+ "\" as a compressed file.");
//		}
//	} // end main
} // end FilterWiktionaryByLanguage
