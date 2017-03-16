package edu.gvsu.kurmasz.zawilinskiTest;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Verify that the LanguagePrefilter correctly handles an entire
 * Wiktionary XML dump
 */
public class LanguagePrefilterTest {

    private static final int ERROR_RETURN = 6;
    // This regexp describes a general level 2 header.
    private static final Pattern level2headerPattern = Pattern.compile("[^=]==([^=\\n])==[^=]");

    private static class LanguageVerifyPostFilter implements PostFilter {

        // This regexp describes what the header for the specific language should look like.
        private Pattern specificLanguageHeader;
        private int returnValue = 0;
        private PrintWriter output;
        private int pageCount = 0;
        private int revCount = 0;
        private int langCount = 0;

        private LanguageVerifyPostFilter(String language, PrintWriter output) {
            this.specificLanguageHeader = Pattern.compile("^==\\s*(\\[\\[)?" + Pattern.quote(language) + "(\\]\\])?\\s*==([^=]|\n|$)");
            this.output = output;
        }

        public boolean keepPage(PageType page) {
            output.printf("%10d %4d %4d %s\n", pageCount, langCount, revCount, page.getTitle());
            pageCount++;
            revCount = 0;
            langCount = 0;
            return false;
        }

        public boolean keepRevision(RevisionType revision, PageType page) {
            String text = Util.getText(revision);
            if (text.length() > 0) {
                Matcher m1 = specificLanguageHeader.matcher(text);
                if (!m1.find()) {
                    output.printf("ERROR! Revision %d of page %d (%s) does not begin with %s\nBeginning is \"%s\" instead",
                            revCount, pageCount, page.getTitle(), specificLanguageHeader, text);
                    returnValue = ERROR_RETURN;

                } else {
                    langCount++;
                    Matcher m2 = level2headerPattern.matcher(text);
                    if (text.length() > m1.end() && m2.find(m1.end())) {
                        output.printf("ERROR! Revision %d of page %d (%s) contains wrong level 2 header\n\t%s",
                                revCount, pageCount, page.getTitle(), m2.group());
                        returnValue = ERROR_RETURN;
                    }
                }
            }
            revCount++;
            return false;
        }

        public int getReturnValue() {
            return returnValue;
        }
    }


    public static void main(String[] args) throws FileNotFoundException, JAXBException {

        if (args.length < 2) {
            System.err.println("Usage:  zawilinskiTest.zawilinskiTest.LanguageVerifyPrefilterTest filename language [logfile [loglevel]]");
            return;
        }
        String language = args[1];

        int returnValue = 0;

        SimpleLog log;
        if (args.length >= 3) {
            int level = 0;
            if (args.length >= 3) {
                level = Integer.parseInt(args[3]);
            }
            log = new Log(args[2], level);
        } else {
            log = new SimpleLog();
        }
        InputStream input = InputHelper.openMappedAndFilteredInputStreamOrQuit(args[0]);
        LanguagePrefilter lpf = new LanguagePrefilter(language);
        //TextSizePrefilter tspf = new TextSizePrefilter(10*1024*1024, log);
        PrintWriter output = OutputHelper.openMappedWriterOrQuit("-", "utf-8", true);
        LanguageVerifyPostFilter svpf = new LanguageVerifyPostFilter(language, output);

        MediaWikiType root = PostFilteredMediaWikiLoader.load(input, log, svpf, lpf).getValue();

        if (svpf.getReturnValue() == ERROR_RETURN) {
            output.println("There were errors");
        }
        output.close();
        System.exit(svpf.getReturnValue());
    }
}
