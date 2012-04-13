package edu.gvsu.kurmasz.zawilinski;

import edu.gvsu.kurmasz.zawilinski.mw.current.PageType;
import edu.gvsu.kurmasz.zawilinski.mw.current.RevisionType;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities to simplify the reading and writing of XML documents as well as
 * navigating {@code MediaWikiType} objects.
 *
 * @author Zachary Kurmas
 */
// (C) 2010 Zachary Kurmas
// Created Mar 1, 2010
public class Util {

   /**
    * {@code PrintStream} to which to send error messages (currently the
    * standard error).
    */
   //public static final PrintStream error_out = System.err;

   /**
    * Constructor to prevent users from instantiating this class.
    */
   private Util() {
   }

   /**
    * Return a {@code List} of the given page's {@code RevisionType} objects.
    *
    * @param page the page
    * @return a {@code List} of the given page's {@code RevisionType} objects
    */
   public static List<RevisionType> getRevisions(PageType page) {
      ArrayList<RevisionType> answer = new ArrayList<RevisionType>();

      for (Object o : page.getRevisionOrUploadOrLogitem()) {
         if (o instanceof RevisionType) {
            answer.add((RevisionType) o);
         }
      }
      return answer;
   }

   /**
    * Return the length of the revision's text element.
    *
    * @param revision the revision
    * @return the length of the revision's text element.
    */
   public static int getTextSize(RevisionType revision) {
      return revision.getText().getValue().length();
   }

   /**
    * Returns the sum of the text sizes of all of this page's revisions.
    *
    * @param page the page
    * @return the sum of the text sizes of all of this page's revisions
    */
   public static long getTextSize(PageType page) {
      long size = 0;
      for (RevisionType rev : getRevisions(page)) {
         size += getTextSize(rev);
      }
      return size;
   }

   /**
    * Returns this revision's text.
    *
    * @param revision the revision
    * @return this revision's text.
    */
   public static String getText(RevisionType revision) {
      return revision.getText().getValue();
   }

   /**
    * Returns the text for the specified revision number. For this method,
    * revisions are numbered sequentially. They are <em>not</em> identified by
    * their {@code revisionID}.
    *
    * @param page    the page
    * @param rev_num The order of the revision in the revision list. (Note: This is
    *                <em>not</em> the MediaWiki {@code revisionID}.
    * @return the text for the specified revision number
    * @throws ArrayIndexOutOfBoundsException if {@code rev_num} is out of bounds.
    */
   public static String getText(PageType page, int rev_num) {
      return getText(getRevisions(page).get(rev_num));
   }

//   static void print(MediaWikiType root) {
//      for (PageType page : root.getPage()) {
//         System.out.println("Title: " + page.getTitle());
//         for (RevisionType revision : getRevisions(page)) {
//            System.out.println("\t" + getText(revision));
//         }
//      }
//   }
} // end Util.
