package edu.gvsu.kurmasz.zawilinski.test;

import edu.gvsu.kurmasz.warszawa.io.InputHelper;
import edu.gvsu.kurmasz.warszawa.log.Log;
import edu.gvsu.kurmasz.warszawa.log.SimpleLog;
import edu.gvsu.kurmasz.zawilinski.*;
import edu.gvsu.kurmasz.zawilinski.mw.current.MediaWikiType;
import edu.gvsu.kurmasz.zawilinski.mw.current.PageType;
import edu.gvsu.kurmasz.zawilinski.mw.current.RevisionType;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Verify that the articles kept by the post filter (1) all contain the desired language and
 * (2) do not contain a trailing level 2 header.  Also print out the titles and number of revisions of the kept pages
 */
public class PostFilterByLanguageTest {

    public static final int ERROR_VAL = 19;

    public static void main(String[] args) throws FileNotFoundException, JAXBException {

        if (args.length < 3) {
            System.err.println("Usage:  zawilinskiTest.zawilinskiTest.PostFilterByLanguageTest filename language outputFile [logfile [loglevel]]");
            return;
        }
        String language = args[1];
        Pattern languageHeaderPattern = Pattern.compile("^==\\s*(\\[\\[" + language + "\\]\\]|" + language +")\\s*==([^=]|$)");
        String outputFile = args[2];
        int returnValue = 0;

        SimpleLog log;
        if (args.length >= 4) {
            int level = 0;
            if (args.length >= 5) {
                level = Integer.parseInt(args[4]);
            }
            log = new Log(args[3], level);
        } else {
            log = new SimpleLog();
        }
        InputStream input = InputHelper.openMappedAndFilteredInputStreamOrQuit(args[0]);


        LanguagePrefilter lpf = new LanguagePrefilter(language);
        PostFilterByLanguage postFilter = new PostFilterByLanguage();
        JAXBElement<MediaWikiType> root = PostFilteredMediaWikiLoader.load(input, log, postFilter, lpf);

        Pattern endPattern = Pattern.compile("[^=]==$");

        int returnVal = 0;
        int pageCount = 0;
        // verify that all articles begin with the language header and do not end
        // with anything that looks remotely like a level 2 header
        for (PageType page : root.getValue().getPage()) {
            List<RevisionType> revs = Util.getRevisions(page);
            if (revs.size() == 0) {
                System.out.println("ERROR:  Page with no revisions:  " + page.getTitle());
                returnVal = ERROR_VAL;
            }
            for (RevisionType rev : revs) {
                String text = Util.getText(rev);
                Matcher m1 = languageHeaderPattern.matcher(text);
                if (!m1.find()) {
                    System.out.println("ERROR: Bad rev (" + rev.getId() + ") in " + page.getTitle() +
                            "does not start with language header");
                    returnVal = ERROR_VAL;
                }
                Matcher m2 = endPattern.matcher(text);
                if (m2.find()) {
                    System.out.println("ERROR: Bad rev (" + rev.getId() + ") in " + page.getTitle() +
                            "might end with L2 headder");
                    returnVal = ERROR_VAL;
                }
            }
            System.out.printf("%8d %4d %s\n", pageCount, revs.size(), page.getTitle());
            pageCount++;
        }

        WiktionaryWriter writer = new WiktionaryWriter();

        try {
            writer.write(root, outputFile);
        } catch (FileNotFoundException fnf) {
            System.err.println("Cannot open \"" + outputFile
                    + "\" for writing.");
        }
    }
}
