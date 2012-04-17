package edu.gvsu.kurmasz.zawilinski;

import edu.gvsu.kurmasz.warszawa.log.Log;
import edu.gvsu.kurmasz.zawilinski.mw.current.MediaWikiType;
import edu.gvsu.kurmasz.zawilinski.mw.current.PageType;
import edu.gvsu.kurmasz.zawilinski.mw.current.RevisionType;

import javax.xml.bind.Unmarshaller;

/**
 * Handles events from unmarshaller. In particular, when a Wiktionary page
 * has finished loading, this code checks to see if the page is wanted. If
 * not, it removes that page, thereby freeing memory.
 *
 * @author Zachary Kurmas
 */
// (C) 2010 Zachary Kurmas
// Created Feb 7, 2010
public class PageFilterListener extends Unmarshaller.Listener {
   // The root of the xml tree
   private MediaWikiType mediawiki;

   // The page we want to delete
   private PageType pageToDelete = null;
   // total pages read.
   private int pageCount = 0;
   // total pages kept.
   private int keptPageCount = 0;

   private RevisionType revisionToDelete = null;
   // total revisions read.
   private int revisionCount = 0;
   // total revisions kept.
   private int keptRevisionCount = 0;
   private PageType currentPage = null;

   // The code that determines whether we want to keep a particular page.
   private PostFilter filter;
   private Log log;

   /**
    * Constructor
    *
    * @param filter filter to apply
    */
   public PageFilterListener(PostFilter filter, Log log) {
      this.filter = filter;
      this.log = log;
   }

   /**
    * Called before a particular element is unmarshalled.
    *
    * @param target the object about to be populated with xml data.
    * @param parent the parent of {@code target}
    */
   public void beforeUnmarshal(Object target, Object parent) {
      // If we are about to begin filling the root element, save a handle
      // to that element.
      if (target instanceof MediaWikiType) {
         mediawiki = (MediaWikiType) target;
      }

      // If we're about to create a new page, see if we want to delete the
      // previous page. We delete it by removing it from the root
      // element's list of pages. Similarly, if we're about to create a
      // new revision, see if we want to remove the previous revision.
      else if (target instanceof PageType) {
         removePage();
         currentPage = (PageType) target;
      } else if (target instanceof RevisionType) {
         removeRevision();
      }
   } // end beforeUnmarshal

   // Remove the specified page from the Mediawiki object's list of pages.
   private void removePage() {
      if (pageToDelete != null) {
         boolean answer = mediawiki.getPage().remove(pageToDelete);
         assert answer : "Couldn't delete desired page.";
         pageToDelete = null;
      }

   }

   // Remove the specified revision from the current page's list of revisions.
   private void removeRevision() {
      if (revisionToDelete != null) {
         boolean answer = currentPage.getRevisionOrUploadOrLogitem()
               .remove(revisionToDelete);
         assert answer : "Couldn't delete desired revision.";
         revisionToDelete = null;
      }
   }

   /**
    * Called after a particular element is unmarshalled.
    *
    * @param target the object that has just been populated with xml data.
    * @param parent the parent of {@code target}
    */
   public void afterUnmarshal(Object target, Object parent) {

      // We run this at the end of unmarshalling a MediaWikiType in
      // case we don't want the very last entry.
      if (target instanceof MediaWikiType) {
         removePage();
      }

      // If we have just finished populating a page,
      // check and see if we want to keep the page.
      if (target instanceof PageType) {

         // remove the last revision if it didn't pass the filter.
         removeRevision();

         pageCount++;
         PageType page = (PageType) target;

         // See if we want to keep the page.
         if (filter.keepPage(page)) {
            keptPageCount++;

            if (log.willLog(PostFilter.PAGE_FILTER_PROGRESS)) {
               String message = String.format("%-11s %5d %7d %30s %d",
                     "kept page", keptPageCount, pageCount, page
                     .getTitle(), Util.getTextSize(page));
               log.println(PostFilter.PAGE_FILTER_PROGRESS, message);
            }
         } else {
            if (log.willLog(PostFilter.PAGE_FILTER_PROGRESS)) {
               String message = String.format("%-11s %5d %7d %30s %d",
                     "dumped page", keptPageCount, pageCount, page
                     .getTitle(), Util.getTextSize(page));
               log.println(PostFilter.PAGE_FILTER_PROGRESS, message);
            }
            pageToDelete = page;
         } // end else
      } // end if PageType

      else if (target instanceof RevisionType) {
         revisionCount++;
         RevisionType revision = (RevisionType) target;
         // See if we want to keep the page.
         if (filter.keepRevision(revision, currentPage)) {
            keptRevisionCount++;

            if (log.willLog(PostFilter.REVISION_FILTER_PROGRESS)) {
               String message = String.format(
                     "    %-11s %5d %7d %12s %d", "kept rev.",
                     keptRevisionCount, revisionCount, revision
                     .getId(), Util.getTextSize(revision));
               log.println(PostFilter.PAGE_FILTER_PROGRESS, message);
            }
         } else {
            if (log.willLog(PostFilter.REVISION_FILTER_PROGRESS)) {
               String message = String.format(
                     "    %-11s %5d %7d %12s %d", "dumped rev.",
                     keptRevisionCount, revisionCount, revision
                     .getId(), Util.getTextSize(revision));
               log.println(PostFilter.PAGE_FILTER_PROGRESS, message);
            }
            revisionToDelete = revision;

         } // end else
      } // end if RevisionType
   } // end afterUnmarshall
}
