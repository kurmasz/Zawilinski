package edu.gvsu.kurmasz.zawilinski;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Arrays;

/**
 * A pre-filter that only allows text through related to the specified
 * language.
 *
 * @author Zachary Kurmas
 */
// (C) 2010 Zachary Kurmas
// Created February 7, 2010
public class LanguagePrefilter extends TextPrefilter {
   // Notes:
   // (1) I found an interesting bug where "==" from the previous text got left
   // in the buffer. I'm not sure how this happened; however, clearing the
   // buffer when starting a new page will certainly fix the problem. Be aware
   // of this potential problem when fixing other bugs or adding functionality

   // package level to support testing.
   static final int BUFFER_SIZE = 8192;

   private static final char EQUALS = '=';
   private static final String NEWLINE = "\n";
   private static final int NO_PARTIAL = -1;
   private static final int NOT_FOUND = -1;
   private static final int SINGLE_EQUAL = -2;

   private StringBuffer buffer;

   private boolean foundLanguage = false;
   private boolean done = false;

   private String language;

   private char[] languageChars;

   private int partialLoc = NO_PARTIAL;

   static String makeLanguageSearchString(String language) {
      return EQUALS + (EQUALS + language + EQUALS + EQUALS);
   }

   /**
    * Constructor.
    *
    * @param language language to search for
    */
   public LanguagePrefilter(String language) {

      this.language = language;

      // Build the string that indicates the start of the content for the language in question
      String language_search_string = makeLanguageSearchString(language);

      // put that string in a char[]
      languageChars = new char[language_search_string.length()];
      language_search_string.getChars(0, language_search_string.length(),
            languageChars, 0);

      // make a buffer
      buffer = new StringBuffer(BUFFER_SIZE);
   }

   @Override
   protected void handleStartTextElement(String uri, String localName,
                                         String name, Attributes attrs) throws SAXException {
      foundLanguage = false;
      done = false;

      // make sure the buffer is empty. See note above.
      //buffer.delete(0, buffer.length());
   }

   @Override
   protected void handleEndTextElement(String uri, String localName,
                                       String name) throws SAXException {
      // foundLanguage = false;
      //done = false;
   }

   @Override
   protected void handleTextElementCharacters(char[] ch, int start, int length)
         throws SAXException {

      /*
         * if (getCurrentTitle().equals("mineta")) { System.out.printf(
         * "%d %d ->" + new String(ch, start, length) + "<-", start, length); }
         */

      // If we've already completely processed the language, just return
      if (done) {
         return;
      } else if (!foundLanguage) {
         searchForLanguageStart(ch, start, length);
      } else {
         // (V1) Verify failures if start or length change
         searchForLanguageEnd(ch, start, length);
      }
   }

   private void searchForLanguageStart_withPartial(char[] ch, int start, int length) throws SAXException {
      // headerLoc will keep track of which character in the language header we expect to see next.
      int headerLoc = partialLoc;

      // Walk through the data and attempt to add to the partial loc.
      for (int dataLoc = start; dataLoc < start + length; dataLoc++) {


         if (ch[dataLoc] == languageChars[headerLoc]) {
            partialLoc++;
         } else {
            // This indicates that the data does not match the header.
            // Since we have no match, we clear the partial and continue searching as normal.
            partialLoc = NO_PARTIAL;
            searchForLanguageStart_noPartial(ch, dataLoc, length - (dataLoc - start));
            return;
         }
         headerLoc++;

         // if headerLoc reaches the end of the header string, then we have found a match.
         if (headerLoc == languageChars.length) {
            partialLoc = NO_PARTIAL;
            foundLanguage = true;
            searchForLanguageEnd(ch, dataLoc + 1, length - (dataLoc - start) - 1);
            return;
         }
      }
   }

   private void searchForLanguageStart_noPartial(char[] ch, int start, int length) throws SAXException {

      int end = start + length - 1;
      int header_start = findTargetLanguageHeader(ch, start, length, languageChars);

      // If the language header is not found, then we return without sending any characters along
      if (header_start == NOT_FOUND) {
         System.out.println("No header for " + Arrays.toString(ch));
         return;
      }

      // If the header_start is close to the end of the buffer, then we have found
      // a partial match.

      // Note:  "+ 1" is correct.  However, +0 will also work.  The match will be
      // incorrectly marked as a partial; but, the next call to searchForLanguageStart
      // will then recognize the match and send the line.
      else if (header_start > end - languageChars.length + 1) {
         System.out.println("Partial for " + Arrays.toString(ch));
         partialLoc = end - header_start + 1;
         System.out.println("PL = " + partialLoc);
      }

      // In this case we have found a complete match.
      else {
         foundLanguage = true;
         System.out.println("Match for " + Arrays.toString(ch));
         searchForLanguageEnd(ch, header_start + languageChars.length, end - header_start - languageChars.length +
               1);
      }

   }

   private void searchForLanguageStart(char[] ch, int start, int length) throws SAXException {

      // If there is an existing partial, then check and see if we can add to it
      if (partialLoc != NO_PARTIAL) {
         searchForLanguageStart_withPartial(ch, start, length);
      } else {
         searchForLanguageStart_noPartial(ch, start, length);
      }
   }

   private void searchForLanguageEnd(char[] ch, int start, int length) throws SAXException {
      System.out.println("Sending " + new String(ch, start, length));
      sendCharacters(ch, start, length);
   }


   // Look through the buffer for text that can be shipped on.
   // returns true if everything has been processed that can be processed.
   private boolean process2() throws SAXException {

      // Look for headings.
      int headding_start = findDoubleEquals2(0);

      // If no headings, then ship everything but any trailing equals signs.
      if (headding_start == -1) {
         shipLine();
         return true;
      }

      // If there is a heading, check for an end:
      int headding_end = findDoubleEquals2(headding_start);

      // If there's no end,
      if (headding_end == -1) {

         // First, see if this is a false heading (i.e., a newline before the
         // next ==)
         if (buffer.indexOf(NEWLINE, headding_start) > -1) {
            // it is, so we "ship" the rest of the line
            shipLine();
            return true;
         }

         // if it is an unterminated header, then keep it in the buffer
         return true;
      } // if no heading end

      String heading = buffer.substring(headding_start, headding_end - 2);

      if (heading.contains(NEWLINE)) {
         // If there is a newline in the heading, it isn't a true heading.
         // Ship everything up to the next ==
         shipLine(headding_end - 2);

         // return false, because there may be text after the trailing ==
         // that can be processed right now.
         return false;
      }

      if (heading.equals(language)) {
         foundLanguage = true;
         // System.out.println("Found Polish: " + getCurrentTitle());
         // ship the language string
         sendCharacters(languageChars, 0, languageChars.length);

         // Flush the buffer
         buffer.delete(0, headding_end);
         // may be able to process more data after the end of the header.
         return false;
      }

      // If this is some other heading *AND* We're currently processing the
      // desired language, then we're done with this text field.
      else if (foundLanguage) {
         // System.out.println("Done " + heading);
         foundLanguage = false;
         buffer.delete(0, buffer.length());
         done = true;
         // Nothing left to process
         return true;
      }

      // This is a tag *before* we get to the desired language. Print it out
      // as "regular" text.
      else {
         shipLine(headding_end);
         buffer.delete(0, headding_end);
         return false;

      }

   }

   private void shipLine(int length) throws SAXException {
      if (foundLanguage) {
         char[] my_chars = new char[length];
         buffer.getChars(0, length, my_chars, 0);
         sendCharacters(my_chars, 0, length);
      }
      buffer.delete(0, length);
   }

   private void shipLine() throws SAXException {
      int last_equals = getLastEquals();

      // if last_equals < 0, then the buffer contains equals only. We need to
      // keep it around until we get more data.
      if (last_equals >= 0) {
         shipLine(last_equals + 1);
      }
   }

   private int getLastEquals() {
      int i = buffer.length() - 1;
      while (i >= 0 && buffer.charAt(i) == EQUALS) {
         i--;
      }
      return i;
   }


   /*
    * Return the index of the beginning of the string {@code header} within {@code ch}.
    * Will also return an index if there is a partial match at the end of the subset of ch between {@code start} and
    * {@code start + length -1}
   */
   // package private for testability
   static int findTargetLanguageHeader(char[] ch, int start, int length, char[] header) {
      //System.out.println("Searching for " + Arrays.toString(header));

      // Search for a complete match
      int end = start + length - 1;
      //System.out.println("Last:  " + (end - header.length));
      for (int i = start; i <= end; i++) {
         boolean found = true;
         //System.out.println();
         for (int j = 0; j < header.length && i + j <= end; j++) {
            // System.out.print(ch[i + j]);
            if (ch[i + j] != header[j]) {
               found = false;
               break;
            }
         } // end for j

         if (found) {
            return i;
         }
      }

      return NOT_FOUND;
   }


   /**
    * Find the starting index of a "==" sequence.
    *
    * @param ch    the chararacter array to search
    * @param start the starting index
    * @param end   the last index
    * @return the starting index of the == sequence, NOT_FOUND if there are no == sequences,
    *         or SINGLE_EQUAL if no other == sequences found and the last character is =.
    */
   int findDoubleEquals(char[] ch, int start, int end) {
      for (int i = start; i < end; i++) {
         if (ch[i] == EQUALS && ch[i + 1] == EQUALS) {
            return i;
         }
      }

      if (ch[end] == EQUALS) {
         return SINGLE_EQUAL;
      }
      return NOT_FOUND;
   }

   // Returns the first place *after* two equals signs.
   private int findDoubleEquals2(int start) {
      int place = buffer.indexOf("==", start);
      while (place != -1) {
         // Check for "only";
         if ((place == 0 || buffer.charAt(place - 1) != EQUALS)
               && (place < buffer.length() - 2)
               && buffer.charAt(place + 2) != EQUALS) {
            return place + 2;
         }
         place = buffer.indexOf("==", place + 2);
      }
      return -1;
   }

} // end LanguagePrefilter
