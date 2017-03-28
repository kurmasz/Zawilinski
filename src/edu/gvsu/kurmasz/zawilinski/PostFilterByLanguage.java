package edu.gvsu.kurmasz.zawilinski;

import edu.gvsu.kurmasz.zawilinski.mw.current.PageType;
import edu.gvsu.kurmasz.zawilinski.mw.current.RevisionType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Keep only those pages that contain some data for the specified language and
 * remove the trailing level 2 language header left by the prefilter.
 * <p/>
 * <p>IMPORTANT! This filter is written specifically to run after the {@link
 * LanguagePrefilter} SAX filter is applied. This SAX filter assures that all
 * text segments <em>begin</em> with the expected language string (e.g.,
 * "==Polish=="). If you don't apply the SAX filter, you need a different
 * post-filter
 * that checks to see if the revision text <em>contains</em> the
 * languageString.</p>
 *
 * @author Zachary Kurmas
 */
// (C) 2010 Zachary Kurmas
// Created February 12, 2010
public class PostFilterByLanguage implements PostFilter {

    private Pattern headerPattern = Pattern.compile("([^=])==([^=\\n])+(=[^=\\n]+)*==$");

    // override
    public boolean keepPage(PageType page) {
        // If the total size of all revisions is 0 (or there are no
        // revisions), then this page contains no data for the requested
        // language.
        return Util.getTextSize(page) > 0;
    }

    public boolean keepRevision(RevisionType revision, PageType page) {
        String text = Util.getText(revision);
        System.out.printf("Text in: =>%s<=\n", text);
        // If the text contains the next header (e.g., ==Spanish==), then
        // remove it.
        Matcher m = headerPattern.matcher(text);
        if (m.find()) {
            String g1 = m.group(1);
           // System.out.println("Found =>" + g1 + "<=");
            text = m.replaceFirst(g1);
            Util.setText(revision, text);
        }

        // IMPORTANT! This filter works properly
        // only if the LanguagePrefilter SAX filter has been applied (which
        // assures that each <text>segment begins with the requested
        // language headerContent.

        // Trim removes leading and trailing whitespace.  This will remove
        // entries with a ==Polish== tag that contains no data.  (This can happen either
        // because the Wiktionary user forgets to add data, or because the user adds a
        // level 2 heading next instead of a level 3 heading.)

        //System.out.printf("Text: =>%s<=\n", text);
        //System.out.printf("Trimmed: =>%s<=\n", text.trim());

        return text.trim().length() > 0;
    }
}

