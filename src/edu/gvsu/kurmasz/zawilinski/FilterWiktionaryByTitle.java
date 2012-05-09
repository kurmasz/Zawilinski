package edu.gvsu.kurmasz.zawilinski;

import edu.gvsu.kurmasz.warszawa.io.InputHelper;
import edu.gvsu.kurmasz.warszawa.log.Log;
import edu.gvsu.kurmasz.zawilinski.mw.current.MediaWikiType;
import edu.gvsu.kurmasz.zawilinski.mw.current.PageType;
import edu.gvsu.kurmasz.zawilinski.mw.current.RevisionType;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.regex.Pattern;

/**
 * Filter a MediaWiki dump for a list of specific articles.
 */
public class FilterWiktionaryByTitle {

    private static final PrintStream usage_out = System.err;
    private static final PrintStream error_out = System.err;

    private static class TitlePostFilter implements PostFilter {

        private Pattern[] patterns;
        private PageType currentPage;
        private boolean keepCurrentPage;

        private TitlePostFilter(Pattern[] patterns) {
            this.patterns = patterns;
        }

        public boolean keepPage(PageType page) {

            if (page == currentPage) {
                return keepCurrentPage;
            }

            currentPage = page;
            String title = page.getTitle();
            for (Pattern p : patterns) {
                if (p.matcher(title).find()) {
                    keepCurrentPage = true;
                    return true;
                }
            }
            keepCurrentPage = false;
            return false;
        }

        public boolean keepRevision(RevisionType revision, PageType page) {
            return keepPage(page);
        }
    }


    public static void main(String[] args) throws JAXBException {

        if (args.length < 2) {
            usage_out.println("Usage:  "
                    + FilterWiktionaryByTitle.class.getSimpleName()
                    + " file regexp1 [regexp2] [regexp3] ...");
            System.exit(1);
        }

        String inputFile = args[0];
        String logfile = "titlePrefilter.log";
        Log log;

        log = new Log();
//        try {
//            log = new Log(logfile, Zawilinski.PAGE_DUMPED);
//        } catch (FileNotFoundException e) {
//            usage_out.println("Could not open logfile \"" + logfile + "\" for writing.");
//            System.exit(2);
//            return;
//        }


        Pattern[] patterns = new Pattern[args.length - 1];
        for (int i = 0; i < args.length - 1; i++) {
            patterns[i] = Pattern.compile(args[i + 1]);
        }

        TitlePostFilter postFilter = new TitlePostFilter(patterns);

        // Some Wiktionary entries have been vandalized by adding several gigabytes of random
        // text.  This filter prevents these entries from unnecessarily slowing down (or
        // even crashing) the filter.
        TextSizePrefilter lts = new TextSizePrefilter(1000000, log);

        WiktionaryWriter writer = new WiktionaryWriter();

        try {
            JAXBElement<MediaWikiType> root = PostFilteredMediaWikiLoader.load(InputHelper
                    .openMappedAndFilteredInputStream(inputFile), log, postFilter, lts);
            writer.write(root, System.out);
        } catch (FileNotFoundException fnfe) {
            error_out.println("Could not open \"" + inputFile
                    + "\".");
        } catch (IOException e) {
            error_out.println("Could not open \"" + inputFile
                    + "\" as a compressed file.");
        }
    } // end main

}
