package edu.gvsu.kurmasz.zawilinski;

import edu.gvsu.kurmasz.zawilinski.mw.current.PageType;
import edu.gvsu.kurmasz.zawilinski.mw.current.RevisionType;

/**
 * Classes that implement this interface determine which {@code Page}s and
 * {@code Revisions} are removed by the post-filter.
 *
 * @author Zachary Kurmas
 */
// (C) 2010 Zachary Kurmas
// Created Mar 16, 2010
public interface PostFilter {



    /**
     * Determines whether {@code page} should be retained or discarded.
     *
     * @param page the {@code PageType} object under consideration
     * @return {@code true} if {@code page} should be kept, {@code false}
     *         otherwise.
     */
    public boolean keepPage(PageType page);

    /**
     * Determines whether {@code revision} should be retained or discarded.
     *
     * @param revision the {@code RevisionType} object under consideration
     * @return {@code true} if {@code revision} should be kept, {@code
     *         false} otherwise.
     */
    public boolean keepRevision(RevisionType revision, PageType page);


    /**
     * {@code PostFilter} that retains all pages and all revisions.
     */
    public static final PostFilter KEEP_ALL = new PostFilter() {
        public boolean keepPage(PageType page) {
            return true;
        }

        public boolean keepRevision(RevisionType revision, PageType page) {
            return true;
        }
    };

    /**
     * {@code PostFilter} that discards all pages and all revisions.
     */
    public static final PostFilter KEEP_NONE = new PostFilter() {
        public boolean keepPage(PageType page) {
            return false;
        }

        public boolean keepRevision(RevisionType revision, PageType page) {
            return false;
        }
    };
}
