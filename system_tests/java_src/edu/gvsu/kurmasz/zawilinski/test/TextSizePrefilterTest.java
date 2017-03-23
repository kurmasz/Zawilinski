package edu.gvsu.kurmasz.zawilinski.test;

import edu.gvsu.kurmasz.warszawa.io.InputHelper;
import edu.gvsu.kurmasz.warszawa.io.OutputHelper;
import edu.gvsu.kurmasz.warszawa.log.Log;
import edu.gvsu.kurmasz.warszawa.log.SimpleLog;
import edu.gvsu.kurmasz.zawilinski.*;
import edu.gvsu.kurmasz.zawilinski.mw.current.MediaWikiType;
import edu.gvsu.kurmasz.zawilinski.mw.current.PageType;
import edu.gvsu.kurmasz.zawilinski.mw.current.RevisionType;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;

/**
 * Verify that the text size pre-filter limits the size of each
 * revision's text. In particular, this program verifies that, when
 * the text size pre-filter is applied, the post-filter doesn't receive
 * any revisions with more text than allowed by the filter.  If the post
 * filter detects too much text, it writes an error message and exits
 * with a non-zero value.
 * <p>
 * Also, for each page, the program writes out (1) the page number, (2) the
 * number of revisions, and (3) the page title.  This output can be compared
 * with the output of the countRevisions ruby script to verify that the
 * filter does not "loose" any pages or revisions.
 *
 */
public class TextSizePrefilterTest {

    private static final int ERROR_RETURN = 5;

    private static class SizeVerifyPostFilter implements PostFilter {

        private int size;
        private int returnValue = 0;
        private PrintWriter output;
        private int pageCount = 0;
        private int revCount = 0;

        private SizeVerifyPostFilter(int size, PrintWriter output) {
            this.size = size;
            this.output = output;
        }

        public boolean keepPage(PageType page) {
            output.printf("%10d %4d %s\n", pageCount, revCount, page.getTitle());
            pageCount++;
            revCount = 0;
            return false;
        }

        public boolean keepRevision(RevisionType revision, PageType page) {
            if (Util.getTextSize(revision) > size) {
                output.printf("ERROR! Revision %d of page %d (%s) has size %d\n",
                        revCount, pageCount, page.getTitle(), Util.getTextSize(revision));
                returnValue = ERROR_RETURN;
            }
            revCount++;
            return false;
        }

        public int getReturnValue() {
            return returnValue;
        }
    }

    public static void main(String[] args) throws JAXBException, FileNotFoundException {

        if (args.length < 2) {
            System.err.println("Usage:  zawilinskiTest.zawilinskiTest.TextSizePrefilterTest filename size [logfile [loglevel]]");
            return;
        }
        final int size = Integer.parseInt(args[1]);
        int returnValue = 0;

        SimpleLog log;
        if (args.length >= 3) {
            int level = 0;
            if (args.length >= 4) {
                level = Integer.parseInt(args[3]);
            }
            log = new Log(args[2], level);
        } else {
            log = new SimpleLog();
        }
        InputStream input = InputHelper.openMappedAndFilteredInputStreamOrQuit(args[0]);
        TextSizePrefilter tspf = new TextSizePrefilter(size, log);
        PrintWriter output = OutputHelper.openMappedWriterOrQuit("-", "utf-8", true);
        SizeVerifyPostFilter svpf = new SizeVerifyPostFilter(size, output);

        MediaWikiType root = PostFilteredMediaWikiLoader.load(input, log, svpf, tspf).getValue();

        if (svpf.getReturnValue() == ERROR_RETURN ) {
            output.println("There were errors");
        }
        output.close();
        System.exit(svpf.getReturnValue());
    }
}
