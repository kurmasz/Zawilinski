package edu.gvsu.kurmasz.zawilinski;

import edu.gvsu.kurmasz.zawilinski.mw.current.MediaWikiType;
import edu.gvsu.kurmasz.zawilinski.mw.current.PageType;
import edu.gvsu.kurmasz.zawilinski.mw.current.RevisionType;
import org.apache.tools.bzip2.CBZip2InputStream;

import java.io.*;
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
    * String identifying the standard input (Allows users to specify the standard input as a "file"). *
    */
   public static final String STDIN = "-";

   /**
    * String identifying the standard output. (Allows users to specify the standard output as a "file"). *
    */
   public static final String STDOUT = "-";

   /**
    * {@code PrintStream} to which to send error messages (currently the
    * standard error).
    */
   public static final PrintStream error_out = System.err;

   // Return value when creation of print writer for standard out fails.
   private static final int STDOUT_CREATE_FAILED = 1;
   private static final int PRINT_WRITER_CREATE_FAILED = 2;

   /**
    * A {@code PrintWriter} object attached to the standard output. {@code
    * PrintWriter} objects handle non-ASCII characters better than {@code
    * PrintStream}s.
    */
   public static final PrintWriter stdout = makeStdoutOrQuit();

   // package protected.
   static final String BZ2_SUFFIX = ".bz2";

   /**
    * ***********************************************************************
    * Constructor to prevent users from instantiating this class.
    * ************************************************************************
    */
   private Util() {
      return;
   }

   /**
    * ***********************************************************************
    * Create a {@code PrintWriter} object attached to the standard output, or
    * print an error message and exit.
    *
    * @return a {@code PrintWriter} object attached to the standard output.
    *         ************************************************************************
    */
   public static PrintWriter makeStdoutOrQuit() {
      try {
         return new PrintWriter(new OutputStreamWriter(System.out, "utf8"),
               true);
      } catch (UnsupportedEncodingException e) {
         error_out.println("UTF8 character encoding not supported.");
         error_out.println(e.getMessage());
         System.exit(STDOUT_CREATE_FAILED);
         return null;
      }
   } // end makeStdoutOrQuit

   /**
    * ***********************************************************************
    * Open a {@code PrintWriter} to the requested file, or print an error
    * message and quit.
    *
    * @param filename the name of the file to be written (or "-" for the standard
    *                 output).
    * @return a {@code PrintWriter} attached to the specified file.
    *         ************************************************************************
    */
   public static PrintWriter openWriterOrQuit(String filename) {
      if (filename == null) {
         return null;
      } else if (filename.equals(STDOUT)) {
         return makeStdoutOrQuit();
      } else {
         try {
            return new PrintWriter(filename, "UTF-8");
         } catch (IOException e) {
            error_out.printf("Cannot open \"%s\" for writing because %s\n",
                  filename, e.getMessage());
            System.exit(PRINT_WRITER_CREATE_FAILED);
            return null;
         }
      }
   } // end getWriterOrQuit

   /**
    * ***********************************************************************
    * Return a {@code List} of the given page's {@code RevisionType} objects.
    *
    * @param page the page
    * @return a {@code List} of the given page's {@code RevisionType} objects
    *         ************************************************************************
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
    * ***********************************************************************
    * Return the length of the revision's text element.
    *
    * @param revision the revision
    * @return the length of the revision's text element.
    *         ************************************************************************
    */
   public static int getTextSize(RevisionType revision) {
      return revision.getText().getValue().length();
   }

   /**
    * ***********************************************************************
    * Returns the sum of the text sizes of all of this page's revisions.
    *
    * @param page the page
    * @return the sum of the text sizes of all of this page's revisions
    *         ************************************************************************
    */
   public static long getTextSize(PageType page) {
      long size = 0;
      for (RevisionType rev : getRevisions(page)) {
         size += getTextSize(rev);
      }
      return size;
   }

   /**
    * ***********************************************************************
    * Returns this revision's text.
    *
    * @param revision the revision
    * @return this revision's text.
    *         ************************************************************************
    */
   public static String getText(RevisionType revision) {
      return revision.getText().getValue();
   }

   /**
    * ***********************************************************************
    * Returns the text for the specified revision number. For this method,
    * revisions are numbered sequentially. They are <em>not</em> identified by
    * their {@code revisionID}.
    *
    * @param page    the page
    * @param rev_num The order of the revision in the revision list. (Note: This is
    *                <em>not</em> the MediaWiki {@code revisionID}.
    * @return the text for the specified revision number
    * @throws ArrayIndexOutOfBoundsException if {@code rev_num} is out of bounds.
    *                                        ************************************************************************
    */
   public static String getText(PageType page, int rev_num) {
      return getText(getRevisions(page).get(rev_num));
   }

   /**
    * ***********************************************************************
    * Open the named {@code .bz2} file and create an {@code InputStream} to
    * read the uncompressed data.
    *
    * @param filename {@code .bz2} file to open.
    * @return an {@code InputStream} from which the uncompressed data can be
    *         read.
    * @throws FileNotFoundException if {@code filename} isn't found.
    * @throws IOException           if {@code filename} isn't in bzip2 format.
    *                               ************************************************************************
    */
   public static CBZip2InputStream openBZStream(String filename)
         throws IOException {

      // Open a regular Input Stream
      InputStream is1 = new FileInputStream(new File(filename));

      // Now, read the first two characters. Should be 'B' and 'Z'
      is1.read();
      is1.read();

      // Finally, create the Zip2 reader
      return new CBZip2InputStream(is1);
   }

   public static void print(MediaWikiType root) {
      for (PageType page : root.getPage()) {
         System.out.println("Title: " + page.getTitle());
         for (RevisionType revision : getRevisions(page)) {
            System.out.println("\t" + getText(revision));
         }
      }
   }
} // end Util.
