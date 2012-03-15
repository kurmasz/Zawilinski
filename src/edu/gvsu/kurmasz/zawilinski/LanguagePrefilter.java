package edu.gvsu.kurmasz.zawilinski;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import sun.swing.SwingUtilities2;

import java.util.Arrays;

/**
 * A pre-filter that only allows text through related to the specified
 * language.
 *
 *
 * <p>If a language contains an '=' (I don't know why it would), then the filter may fail to detect some headers.
 * For example, given an "language" abc=def, the filter may fail to detect the start of the language section if the
 * article data looks like this "...words words ==abc==abc=def==Words words words..."
 * </p>
 *
 * @author Zachary Kurmas
 */
// (C) 2010 Zachary Kurmas
// Created February 7, 2010
public class LanguagePrefilter extends TextPrefilter {

   private enum SectionStage {PRE, IN, POST}

   private SectionStage sectionStage;
   private HeaderSearch hs;
   private String language;


   /**
    * Constructor.
    *
    * @param language language to search for
    */
   public LanguagePrefilter(String language) {
      this.language = language;
   }

   @Override
   protected void handleStartTextElement(String uri, String localName,
                                         String name, Attributes attrs) throws SAXException {

      sectionStage = SectionStage.PRE;
      hs = new HeaderSearch();

   }

   @Override
   protected void handleEndTextElement(String uri, String localName,
                                       String name) throws SAXException {
     // TODO:  Flush the last of the text if there is no following language section
   }

   private void handlePreStage(char[] ch, int start, int length) throws SAXException {
      HeaderSearch.Result result = hs.process(ch, start, length);
      // if result is null, then there is no complete header, and therefore,
      // nothing to pass through.  If result is not null, but the header is not equal to language
      // then we found the section header for a different language.
      if (result != null && result.header.equals(language)) {
         sectionStage = SectionStage.IN;
         // Now we need a new HeaderSearch to find the end of the language.
         hs = new HeaderSearch();
         handleInStage(ch, result.next, length - start + result.next);
      }
   }

   private void handleInStage(char[] ch, int start, int length) throws SAXException {
      HeaderSearch.Result result = hs.process(ch, start, length);

      // We have found the start of another section
      if (result != null) {
         sectionStage = SectionStage.POST;
         hs = null; // to free space
         length -= (result.next - start);
         start = result.next;
      }
      sendCharacters(ch, start, length);
   }

   @Override
   protected void handleTextElementCharacters(char[] ch, int start, int length)
         throws SAXException {

      switch (sectionStage) {
         case POST:
            return;
         case PRE:
            handlePreStage(ch, start, length);
            return;
         case IN:
            handleInStage(ch, start, length);
            return;
      }
   }
} // end LanguagePrefilter
