package edu.gvsu.kurmasz.zawilinski;

import edu.gvsu.kurmasz.warszawa.io.InputHelper;
import edu.gvsu.kurmasz.warszawa.log.Log;
import edu.gvsu.kurmasz.zawilinski.mw.current.MediaWikiType;
import edu.gvsu.kurmasz.zawilinski.mw.current.PageType;
import edu.gvsu.kurmasz.zawilinski.mw.current.RevisionType;
import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.InputStream;

/**
 * Generates a DOM for a Wiktionary XML document that contains only elements for selected entries.
 * Specifically, this class class (1) applies a SAX filter to an XML data stream,
 * (2) uses JAXB to unmarshal the filtered steam into Java objects, then (3) applies a second
 * filter to the newly created DOM elements for each Wiktionary entry.  The final DOM contains
 * elements for only those Wiktionary entries that passed through the filter.
 * (See {@link PreFilteredMediaWikiLoader}
 * and {@link MediaWikiLoader} for more details.)
 *
 * <p> Currently this class can apply only one post filter. That post-filter must
 * be passed to the constructor. (At a quick glance, it doesn't appear to write a post-filter
 * that could apply several other post-filters.)</p>
 *
 * <p>
 * Debug levels: (activates all levels above):
 * <ul>
 * <li>PARSE_BEGIN_END: Announce beginning and end of parsing
 * <li>PAGE_FILTER_PROGRESS: Announces which Wiktionary entries kept and which dumped.
 * <li>REVISION_FILTER_PROGRESS: Announces which Wiktionary revisions are kept and which are dumpted.
 * </ul>
 *
 * @author Zachary Kurmas
 */
// (C) 2010 Zachary Kurmas
// Created Feb 12, 2010
public class PostFilteredMediaWikiLoader {

   public static final int PAGE_FILTER_PROGRESS = MediaWikiLoader.PARSE_BEGIN_END - 1;
   public static final int REVISION_FILTER_PROGRESS = PAGE_FILTER_PROGRESS - 1;

   private static boolean filterActive = true;

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
      public boolean keepRevision(RevisionType revision);
   } // end PostFilter interface

   /**
    * {@code PostFilter} that retains all pages and all revisions.
    */
   public static final PostFilter KEEP_ALL = new PostFilter() {
      public boolean keepPage(PageType page) {
         return true;
      }

      public boolean keepRevision(RevisionType revision) {
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

      public boolean keepRevision(RevisionType revision) {
         return false;
      }
   };

   /**
    * Handles events from unmarshaller. In particular, when a Wiktionary page
    * has finished loading, this code checks to see if the page is wanted. If
    * not, it removes that page, thereby freeing memory.
    *
    * @author Zachary Kurmas
    */
   // (C) 2010 Zachary Kurmas
   // Created Feb 7, 2010
   private static class PageFilterListener extends Unmarshaller.Listener {
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
         // Skip this step if the filter is not active.
         // (e.g., when loadAll()) called
         if (!filterActive) {
            return;
         }

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

      // Remove the specified page from the mediawiki object's list of pages.
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
         // Skip this step if the filter is not active.
         // (e.g., when loadAll()) called
         if (!filterActive) {
            return;
         }


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
               pageToDelete = null;

               keptPageCount++;

               if (log.willLog(PAGE_FILTER_PROGRESS)) {
                  String message = String.format("%-11s %5d %7d %30s %d",
                        "kept page", keptPageCount, pageCount, page
                        .getTitle(), Util.getTextSize(page));
                  log.println(PAGE_FILTER_PROGRESS, message);
               }
            } else {
               if (log.willLog(PAGE_FILTER_PROGRESS)) {
                  String message = String.format("%-11s %5d %7d %30s %d",
                        "dumped page", keptPageCount, pageCount, page
                        .getTitle(), Util.getTextSize(page));
                  log.println(PAGE_FILTER_PROGRESS, message);
               }
               pageToDelete = page;

            } // end else
         } // end if PageType

         else if (target instanceof RevisionType) {
            revisionCount++;
            RevisionType revision = (RevisionType) target;

            // See if we want to keep the page.
            if (filter.keepRevision(revision)) {
               revisionToDelete = null;

               keptRevisionCount++;

               if (log.willLog(REVISION_FILTER_PROGRESS)) {
                  String message = String.format(
                        "    %-11s %5d %7d %12s %d", "kept rev.",
                        keptRevisionCount, revisionCount, revision
                        .getId(), Util.getTextSize(revision));
                  log.println(PAGE_FILTER_PROGRESS, message);
               }
            } else {
               if (log.willLog(REVISION_FILTER_PROGRESS)) {
                  String message = String.format(
                        "    %-11s %5d %7d %12s %d", "dumped rev.",
                        keptRevisionCount, revisionCount, revision
                        .getId(), Util.getTextSize(revision));
                  log.println(PAGE_FILTER_PROGRESS, message);
               }
               revisionToDelete = revision;

            } // end else
         } // end if RevisionType
      } // end afterUnmarshall

   }

   public static JAXBElement<MediaWikiType> loadFilteredPages(InputStream source, Log log,
                                                              PostFilter postFilter,
                                                              XMLFilter... filterList) throws JAXBException {
      Unmarshaller unmarshaller = MediaWikiLoader.createUnmarshaller();
      unmarshaller.setListener(new PageFilterListener(postFilter, log));
      return PreFilteredMediaWikiLoader.load(new InputSource(source), log, unmarshaller, filterList);
   }



   public static void main(String[] args) throws JAXBException {
      String filename = "/Users/kurmasz/Documents/LocalResearch/LanguageWiki/SampleInput/mw_sample_0.4.xml";
      InputStream source = InputHelper.openFilteredInputStreamOrQuit(new File(filename));
      Log log =  new Log(System.err, 0);
      JAXBElement<MediaWikiType> elem = loadFilteredPages(source, log, KEEP_ALL);
      MediaWikiType root = elem.getValue();
      Util.print(root);
   }


}
