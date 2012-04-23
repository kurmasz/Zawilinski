package edu.gvsu.kurmasz.zawilinski;

import edu.gvsu.kurmasz.zawilinski.mw.current.PageType;
import edu.gvsu.kurmasz.zawilinski.mw.current.RevisionType;


/**
 * Keep only those pages that contain some data for the specified language.
 * IMPORTANT! This filter is written specifically to run after the {@link
 * LanguagePrefilter} SAX filter is applied. This SAX filter assures that all
 * text segments <em>begin</em> with the expected language string (e.g.,
 * "==Polish=="). If you don't apply the SAX filter, you need a post-filter
 * that checks to see if the revision text <em>contains</em> the
 * languageString.
 *
 * @author Zachary Kurmas
 */
// (C) 2010 Zachary Kurmas
// Created February 12, 2010
public class PostFilterByLanguage implements PostFilter {
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

    public boolean keepRevision(RevisionType revision, PageType page) {
        // IMPORTANT! Notice the "startsWith" This filter works properly
        // only if the LanguagePrefilter SAX filter has been applied (which
        // assures that each <text>segment begins with the requested
        // language headerContent.
        //return Util.getText(revision).startsWith(languageString);
        return Util.getTextSize(revision) > 0;
    }
}

