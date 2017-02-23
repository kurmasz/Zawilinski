package edu.gvsu.kurmasz.zawilinski;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.regex.Pattern;

/**
 * A pre-filter that only allows text through that is in the section for the specified language.   Note:  In addition
 * to the expected headers of the form {@code ==Language==}, this filter also allows (1) spaces around the language
 * (e.g., ({@code == Language ==} and (2) the language to be a link (e.g., {@code "==[[Language]]=="}.
 *
 * @author Zachary Kurmas
 */
// (C) 2010 Zachary Kurmas
// Created February 7, 2010
public class LanguagePrefilter extends TextPrefilter {

   private enum SectionStage {PRE, IN, POST}

   private SectionStage sectionStage;
   private HeaderSearch hs;
   private Pattern headerPattern;

   /**
    * Constructor specifying a specific header.
    *
    * @param language language to search for
    */
   public LanguagePrefilter(String language) {
      // See class javadoc comment for use of regexp.
      // The call to Pattern.quote below makes sure the parameter language is not treated as a regular expression.
      // For example, if the user passes "a.b", we want to make sure we accept only the literal header "a.b", not
      // "aab", or some other header that matches the regular expression "a.b"
      headerPattern = Pattern.compile("\\s*(\\[\\[)?" + Pattern.quote(language) + "(\\]\\])?\\s*");
   }

   /**
    * Constructor specifying a regular expression that the header must match.  Thus,
    * passing {@code Pattern.compile("P[ei]g")} will match "==Peg==" and "==Pig==". Note, however:
    *
    * <ul>
    * <li> This filter passes at most one section through the filter.  Thus,
    * if a revision contained both ==Peg== and ==Pig==, only the first one encountered would be passed.</li>
    * <li> The regular expression is
    * applied only to the text identified as the header content.  It is not used to identify the header itself.  In
    * other words, you can't use this feature to specify a different depth or style of header.</li>
    * </ul>
    * <em>Important:  Use at your own risk. This feature has not yet been thoroughly tested.</em>
    *
    * @param pattern regular expression that the header must match.
    */
   public LanguagePrefilter(Pattern pattern) {
      headerPattern = Pattern.compile("\\s*(\\[\\[)?" + pattern.pattern() + "(\\]\\])?\\s*");
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
      // Nothing to do
   }

   private void handlePreStage(char[] ch, int start, int length) throws SAXException {
      HeaderSearch.Result result = hs.process(ch, start, length);
      // if result is null, then there is no complete headerContent, and therefore,
      // nothing to pass through.
      //
      if (result == null) {
         return;
      }
      // If result is not null, but the headerContent is not equal to language
      // then we found the section headerContent for a different language.

      // if the headerContent is the language we are filtering for, then
      // switch stages

      if (headerPattern.matcher(result.headerContent).matches()) {
         sectionStage = SectionStage.IN;
         String fullHeader = result.fullHeader();
         sendCharacters(fullHeader.toCharArray(), 0, fullHeader.length());
         // Now we need a new HeaderSearch to find the end of the language.
         hs = new HeaderSearch();
         handleInStage(ch, result.next, length - (result.next - start));
      } else {
         // if we have found a different header, then re-set the search
         hs = new HeaderSearch();
         handlePreStage(ch, result.next, length - (result.next - start));
      }
   }

   private void handleInStage(char[] ch, int start, int length) throws SAXException {
      HeaderSearch.Result result = hs.process(ch, start, length);

      // We have found the start of another section
      if (result != null) {
         sectionStage = SectionStage.POST;
         hs = null; // to free space
         length = (result.next - start);
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
