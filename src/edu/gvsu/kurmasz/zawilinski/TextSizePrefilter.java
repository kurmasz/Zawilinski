package edu.gvsu.kurmasz.zawilinski;

import edu.gvsu.kurmasz.warszawa.log.SimpleLog;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * A {@link TextPrefilter} that limits the number of characters allowed in a given MediaWiki
 * &lt;text&gt; element.
 *
 * @author Zachary Kurmas
 */
// (C) 2010 Zachary Kurmas
// Created February 7, 2010
public class TextSizePrefilter extends TextPrefilter {

   // Indicate that the text size is unlimited. (When writing an application
   // that may or may not apply this filter, it is easier to specify an
   // "infinite" filter size than to conditionally remove this filter from the filter chain.)
   public static final int UNLIMITED = -1;

   // size of the <text> element currently being processed.
   private int currentTextSize;

   // maximum allowable size for a text segment.
   private int textSizeLimit;

   private SimpleLog log;



   /**
    * Constructor
    *
    * @param maximum_text_size maximum allowable size for a text segment.
    * @param log               {@link SimpleLog} to which to write the list of truncated
    *                          items.
    */
   public TextSizePrefilter(int maximum_text_size, SimpleLog log) {

      this.log = log;

      if (maximum_text_size < 0 && maximum_text_size != UNLIMITED) {
         throw new IllegalArgumentException("maximum_text_size must be "
               + UNLIMITED + " (for unlimited size) or >= 0");
      }

      if (maximum_text_size == UNLIMITED) {
         this.textSizeLimit = Integer.MAX_VALUE;
      } else {
         this.textSizeLimit = maximum_text_size;
      }
   }

   /**
    * Constructor
    *
    * @param maximum_text_size maximum allowable size for a text segment.
    */
   public TextSizePrefilter(int maximum_text_size) {
      this(maximum_text_size, null);
   }

   @Override
   protected void handleStartTextElement(String uri, String localName,
                                         String qName, Attributes attrs) throws SAXException {
      // Remember, a call to handleTextElementCharacters does not necessarily
      // contain all the content of the current <text> element.  Thus, when we
      // see an opening <text> tag, we need to reset a counter.

      // Notice that this method doesn't explicitly pass along the tag.
      // The superclass handles that.

      currentTextSize = 0;
   }

   @Override
   protected void handleTextElementCharacters(char[] ch, int start, int length)
         throws SAXException {
      int old_text_size = currentTextSize;
      currentTextSize += length;

      // If we're already over the limit, then return from this method
      // without passing any data along
      if (old_text_size >= textSizeLimit) {
         // do nothing
      }

      // If this call puts us over the limit, then pass up only enough
      // data to reach the limit.
      else if (currentTextSize > textSizeLimit) {
         int using = textSizeLimit - old_text_size;
         // System.out.printf("Truncating.  c %d -- l %d -- u %d\n",
         // old_text_size, length, using);

         assert using <= length : "ERROR calculating using!"
               + String.format("%d  %d %s", using, length,
               getCurrentTitle());

         sendCharacters(ch, start, using);
      } else {

         // We haven't reached the threshold yet,
         // so pass everything along.
         sendCharacters(ch, start, length);
      }
   }

   @Override
   protected void handleEndTextElement(String uri, String localName,
                                       String qName) throws SAXException {

      // Print truncation information to the log
      // TODO: Have the superclass also save the revision id so that we know *which* revision was
      // truncated.
      if (currentTextSize > this.textSizeLimit && log != null) {
         log.println(Zawilinski.TRUNCATIONS, String.format("%d %d %s", currentTextSize, textSizeLimit, getCurrentTitle()));
      }
   }

   // package-protected for testing purposes
   int getTextSizeLimit() {
      return textSizeLimit;
   }

   // package-protected for testing purposes
   int getCurrentTextSize() {
      return currentTextSize;
   }

   // package-protected for testing purposes
   SimpleLog getLog() {
      return log;
   }
} // end TextSizePrefilter
