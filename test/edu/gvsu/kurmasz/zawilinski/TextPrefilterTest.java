package edu.gvsu.kurmasz.zawilinski;

import org.junit.Test;
import org.mockito.Matchers;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Zachary Kurmas
 */
// Created  2/3/12 at 10:19 AM
// (C) Zachary Kurmas 2012

public class TextPrefilterTest {

   public static class MyTextPrefilter extends TextPrefilter {

      MyTextPrefilter() {
         super();
      }

      MyTextPrefilter(XMLReader r) {
         super(r);
      }

      @Override
      protected void handleStartTextElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
         return;
      }

      @Override
      protected void handleTextElementCharacters(char[] ch, int start, int length) throws SAXException {
         return;
      }

      @Override
      protected void handleEndTextElement(String uri, String localName, String qName) throws SAXException {
         return;
      }
   }

   private static TextPrefilter makeTextPrefilterSpy() {
      return spy(new MyTextPrefilter());
   }

   public TextPrefilter makeSpy() {
      return makeTextPrefilterSpy();
   }


   static void startTitle(TextPrefilter tp) throws SAXException {
      String uri = "URI";
      String localName = TextPrefilter.TITLE_ELEMENT_NAME;
      String qName = "Qname";
      Attributes attrs = mock(Attributes.class);

      tp.startElement(uri, localName, qName, attrs);
   }

   static void endTitle(TextPrefilter tp) throws SAXException {
      String uri = "URI";
      String localName = TextPrefilter.TITLE_ELEMENT_NAME;
      String qName = "Qname";
      Attributes attrs = mock(Attributes.class);

      tp.endElement(uri, localName, qName);
   }

   static void startText(TextPrefilter tp) throws SAXException {
      String uri = "URI";
      String localName = TextPrefilter.TEXT_ELEMENT_NAME;
      String qName = "Qname";
      Attributes attrs = mock(Attributes.class);

      tp.startElement(uri, localName, qName, attrs);
   }

   static void endText(TextPrefilter tp) throws SAXException {
      String uri = "URI";
      String localName = TextPrefilter.TEXT_ELEMENT_NAME;
      String qName = "Qname";
      Attributes attrs = mock(Attributes.class);

      tp.endElement(uri, localName, qName);
   }

   static void startPage(TextPrefilter tp) throws SAXException {
      String uri = "URI";
      String localName = TextPrefilter.PAGE_ELEMENT_NAME;
      String qName = "Qname";
      Attributes attrs = mock(Attributes.class);

      tp.startElement(uri, localName, qName, attrs);
   }

   static void endPage(TextPrefilter tp) throws SAXException {
      String uri = "URI";
      String localName = TextPrefilter.PAGE_ELEMENT_NAME;
      String qName = "Qname";
      Attributes attrs = mock(Attributes.class);

      tp.endElement(uri, localName, qName);
   }

   static char[] sendCharacters(TextPrefilter pf, String s) throws SAXException {
      char[] myChars = s.toCharArray();
      pf.characters(myChars, 0, myChars.length);
      return myChars;
   }

   static char[] sendCharacters(TextPrefilter pf, String s, int start) throws SAXException {
      char[] myChars = new char[(s.length() + start) * 2];

      for (int i = 0; i < start; i++) {
         myChars[i] = '*';
      }
      System.arraycopy(s.toCharArray(), 0, myChars, start, s.length());
      for (int i = start + s.length(); i < myChars.length; i++) {
         myChars[i] = '-';
      }
      pf.characters(myChars, start, s.length());
      return myChars;
   }

   //
   // constructor
   //
   @Test
   public void inTextInitiallyFalse() throws Throwable {
      assertFalse("inText should initially be false", makeSpy().inText());
   }

   @Test
   public void inTitleInitiallyFalse() throws Throwable {
      assertFalse("inText should initially be false", makeSpy().inTitle());
   }

   @Test
   public void firstConstructorSetsNullParent() throws Throwable {
      assertNull("parent should be null", new MyTextPrefilter().getParent());
   }

   @Test
   public void secondConstructorPassesParent() throws Throwable {
      XMLReader reader = mock(XMLReader.class);
      TextPrefilter tp = new MyTextPrefilter(reader);
      assertEquals(reader, tp.getParent());
   }


   //
   // startElement
   //

   @Test
   public void startElementCallsHandleStartTextElementIfStartOfText() throws Throwable {
      TextPrefilter tp = makeSpy();
      String uri = "URI";
      String localName = TextPrefilter.TEXT_ELEMENT_NAME;
      String qName = "Qname";
      Attributes attrs = mock(Attributes.class);

      tp.startElement(uri, localName, qName, attrs);
      verify(tp).handleStartTextElement(uri, localName, qName, attrs);
   }

   @Test
   public void startElementDoesNotCallHandleStartTextElementIfOther() throws Throwable {
      TextPrefilter tp = makeSpy();
      String uri = "URI";
      String localName = TextPrefilter.TITLE_ELEMENT_NAME;
      String qName = "Qname";
      Attributes attrs = mock(Attributes.class);

      tp.startElement(uri, localName, qName, attrs);
      verify(tp, never()).handleStartTextElement(Matchers.<String>any(), Matchers.<String>any(),
            Matchers.<String>any(), Matchers.<Attributes>any());
   }

   @Test
   public void startElementCallsSuperStartElementIfStartOfText() throws Throwable {
      TextPrefilter tp = makeSpy();
      ContentHandler handler = mock(ContentHandler.class);
      tp.setContentHandler(handler);
      String uri = "URI";
      String localName = TextPrefilter.TITLE_ELEMENT_NAME;
      String qName = "Qname";
      Attributes attrs = mock(Attributes.class);

      tp.startElement(uri, localName, qName, attrs);
      verify(handler).startElement(uri, localName, qName, attrs);
   }

   @Test
   public void startElementCallsSuperStartElementIfNotStartOfText() throws Throwable {
      TextPrefilter tp = makeSpy();
      ContentHandler handler = mock(ContentHandler.class);
      tp.setContentHandler(handler);
      String uri = "URI";
      String localName = "a;ldjkpejk";
      String qName = "Qname";
      Attributes attrs = mock(Attributes.class);

      tp.startElement(uri, localName, qName, attrs);
      verify(handler).startElement(uri, localName, qName, attrs);
   }


   @Test
   public void startElementSetsInTextIfStartOfText() throws Throwable {
      TextPrefilter tp = makeSpy();
      String uri = "URI";
      String localName = TextPrefilter.TEXT_ELEMENT_NAME;
      String qName = "Qname";
      Attributes attrs = mock(Attributes.class);

      tp.startElement(uri, localName, qName, attrs);
      assertTrue(tp.inText());
   }

   @Test
   public void startElementDoesNotSetInTextIfNotStartOfText() throws Throwable {
      TextPrefilter tp = makeSpy();
      String uri = "URI";
      String localName = TextPrefilter.TITLE_ELEMENT_NAME;
      String qName = "Qname";
      Attributes attrs = mock(Attributes.class);

      tp.startElement(uri, localName, qName, attrs);
      assertFalse(tp.inText());
   }

   @Test
   public void startElementDoesNotSetInTextIfNotStartOfText2() throws Throwable {
      TextPrefilter tp = makeSpy();
      String uri = "URI";
      String localName = "lajdfp;j";
      String qName = "Qname";
      Attributes attrs = mock(Attributes.class);

      tp.startElement(uri, localName, qName, attrs);
      assertFalse(tp.inText());
   }

   @Test
   public void startElementSetsInTitileifStartOfTitle() throws Throwable {
      TextPrefilter tp = makeSpy();
      String uri = "URI";
      String localName = TextPrefilter.TITLE_ELEMENT_NAME;
      String qName = "Qname";
      Attributes attrs = mock(Attributes.class);

      tp.startElement(uri, localName, qName, attrs);
      assertTrue(tp.inTitle());
   }

   @Test
   public void startElementDoesNotSetInTitleIfNotStartOfTitle() throws Throwable {
      TextPrefilter tp = makeSpy();
      String uri = "URI";
      String localName = TextPrefilter.TEXT_ELEMENT_NAME;
      String qName = "Qname";
      Attributes attrs = mock(Attributes.class);

      tp.startElement(uri, localName, qName, attrs);
      assertFalse(tp.inTitle());
   }

   @Test
   public void startElementDoesNotSetInTextIfNotStartOfTitle() throws Throwable {
      TextPrefilter tp = makeSpy();
      String uri = "URI";
      String localName = "lajdfp;j";
      String qName = "Qname";
      Attributes attrs = mock(Attributes.class);

      tp.startElement(uri, localName, qName, attrs);
      assertFalse(tp.inTitle());
   }

   @Test
   public void startElementSetsCurrentTitleToEmptyStringIfStartOfTitle() throws Throwable {
      TextPrefilter tp = makeSpy();
      String uri = "URI";
      String localName = TextPrefilter.TITLE_ELEMENT_NAME;
      String qName = "Qname";
      Attributes attrs = mock(Attributes.class);

      String theTitle = "The really cool test title";
      // Set the title
      tp.startElement(uri, localName, qName, attrs);
      tp.characters(theTitle.toCharArray(), 0, theTitle.length());
      tp.endElement(uri, localName, qName);
      assertEquals(theTitle, tp.getCurrentTitle());


      // Verify title gets re-set
      tp.startElement(uri, localName, qName, attrs);
      assertEquals("", tp.getCurrentTitle());
   }

   @Test
   public void startElementDoesNotSetCurrentTitleToEmptyStringIfStartOfTitle() throws Throwable {
      TextPrefilter tp = makeSpy();
      String uri = "URI";
      String localName = TextPrefilter.TEXT_ELEMENT_NAME;
      String qName = "Qname";
      Attributes attrs = mock(Attributes.class);

      String theTitle = "The really cool test title";
      // Set the title
      tp.startElement(uri, TextPrefilter.TITLE_ELEMENT_NAME, qName, attrs);
      tp.characters(theTitle.toCharArray(), 0, theTitle.length());
      tp.endElement(uri, TextPrefilter.TITLE_ELEMENT_NAME, qName);

      // Verify title does not get re-set
      tp.startElement(uri, localName, qName, attrs);
      assertEquals(theTitle, tp.getCurrentTitle());

   }

   @Test
   public void startElementDoesNotSetCurrentTitleToEmptyStringIfStartOfTitle2() throws Throwable {
      TextPrefilter tp = makeSpy();
      String uri = "URI";
      String localName = "Jabba the hutt";
      String qName = "Qname";
      Attributes attrs = mock(Attributes.class);

      String theTitle = "The really cool test title";
      // Set the title
      tp.startElement(uri, TextPrefilter.TITLE_ELEMENT_NAME, qName, attrs);
      tp.characters(theTitle.toCharArray(), 0, theTitle.length());
      tp.endElement(uri, TextPrefilter.TITLE_ELEMENT_NAME, qName);

      // Verify title does not get re-set
      tp.startElement(uri, localName, qName, attrs);
      assertEquals(theTitle, tp.getCurrentTitle());
   }


   //
   // characters
   //

   @Test
   public void charactersAppendsToCurrentTitleIfInTitle() throws Throwable {
      TextPrefilter tp = makeSpy();
      String uri = "URI";
      String localName = TextPrefilter.TITLE_ELEMENT_NAME;
      String qName = "Qname";
      Attributes attrs = mock(Attributes.class);

      tp.startElement(uri, localName, qName, attrs);

      String partialTitle1 = "part of th";
      tp.characters(partialTitle1.toCharArray(), 0, partialTitle1.length());
      assertEquals(partialTitle1, tp.getCurrentTitle());

      String partialTitle2 = "e title";
      tp.characters(partialTitle2.toCharArray(), 0, partialTitle2.length());
      assertEquals(partialTitle1 + partialTitle2, tp.getCurrentTitle());
   }

   @Test
   public void charactersDoesNotAppendToCurrentTitleIfNotInTitle() throws Throwable {
      TextPrefilter tp = makeSpy();
      assertFalse(tp.inTitle());

      startTitle(tp);
      endTitle(tp);

      String partialTitle1 = "part of th";
      tp.characters(partialTitle1.toCharArray(), 0, partialTitle1.length());
      assertEquals("", tp.getCurrentTitle());
   }

   @Test
   public void charactersDoesNotAppendToCurrentTitleIfNotInTitle2() throws Throwable {
      TextPrefilter tp = makeSpy();
      assertFalse(tp.inTitle());

      startTitle(tp);
      endTitle(tp);

      startText(tp);

      String partialTitle1 = "part of th";
      tp.characters(partialTitle1.toCharArray(), 0, partialTitle1.length());
      assertEquals("", tp.getCurrentTitle());
   }


   @Test
   public void charactersCallsHandleTextElementIfInText() throws Throwable {
      TextPrefilter tp = makeSpy();

      startText(tp);
      String something = "Something to say";
      char[] theChars = something.toCharArray();
      tp.characters(theChars, 0, something.length());

      verify(tp).handleTextElementCharacters(theChars, 0, something.length());
   }

   @Test
   public void charactersDoesNotCallsHandleTextElementIfNotInText() throws Throwable {
      TextPrefilter tp = makeSpy();

      String something = "Something to say";
      char[] theChars = something.toCharArray();
      tp.characters(theChars, 0, something.length());

      verify(tp, never()).handleTextElementCharacters(Matchers.<char[]>any(), anyInt(), anyInt());
   }

   @Test
   public void charactersDoesNotCallsHandleTextElementIfNotInText2() throws Throwable {
      TextPrefilter tp = makeSpy();

      startText(tp);
      endText(tp);

      String something = "Something to say";
      char[] theChars = something.toCharArray();
      tp.characters(theChars, 0, something.length());

      verify(tp, never()).handleTextElementCharacters(Matchers.<char[]>any(), anyInt(), anyInt());
   }

   @Test
   public void charactersCallsSuperIfNotInText() throws Throwable {
      TextPrefilter tp = makeSpy();
      ContentHandler handler = mock(ContentHandler.class);
      tp.setContentHandler(handler);

      String something = "Something to say";
      char[] theChars = something.toCharArray();
      tp.characters(theChars, 0, something.length());

      verify(handler).characters(theChars, 0, something.length());
   }

   @Test
   public void charactersCallsSuperIfNotInText2() throws Throwable {
      TextPrefilter tp = makeSpy();
      ContentHandler handler = mock(ContentHandler.class);
      tp.setContentHandler(handler);

      startText(tp);
      endText(tp);

      String something = "Something to say";
      char[] theChars = something.toCharArray();
      tp.characters(theChars, 0, something.length());

      verify(handler).characters(theChars, 0, something.length());
   }

   @Test
   public void charactersDoesNotCallSuperIfInText() throws Throwable {
      // This test passes only when testing MyTextPrefilter
      // Thus, we want to make sure subclass tests don't attempt to run this
      // test using instances of the subclass.
      TextPrefilter tp = makeTextPrefilterSpy();
      ContentHandler handler = mock(ContentHandler.class);
      tp.setContentHandler(handler);

      startText(tp);

      String something = "Something to say";
      char[] theChars = something.toCharArray();
      tp.characters(theChars, 0, something.length());

      verify(handler, never()).characters(Matchers.<char[]>any(), anyInt(), anyInt());
   }

   //
   // sendCharacters
   //
   @Test
   public void sendCharactersCallsSuperCharacters() throws Throwable {
      TextPrefilter tp = makeSpy();
      ContentHandler handler = mock(ContentHandler.class);
      tp.setContentHandler(handler);

      String something = "Something to say";
      char[] theChars = something.toCharArray();
      tp.sendCharacters(theChars, 0, something.length());

      verify(handler).characters(theChars, 0, something.length());
   }

   //
   // endElement
   //
   @Test
   public void endElementSetsInTextToFalseWhenEndingText() throws Throwable {
      TextPrefilter tp = makeSpy();

      startText(tp);
      endText(tp);

      assertFalse(tp.inText());
   }

   @Test
   public void endElementCallsHandleEndTextElementWhenEndingText() throws Throwable {
      TextPrefilter tp = makeSpy();

      startText(tp);

      String uri = "URI";
      String localName = TextPrefilter.TEXT_ELEMENT_NAME;
      String qName = "Qname";

      tp.endElement(uri, localName, qName);
      verify(tp).handleEndTextElement(uri, localName, qName);
   }

   @Test
   public void endElementDoesNotCallHandleEndTextElementWhenNotEndingText() throws Throwable {
      TextPrefilter tp = makeSpy();

      startTitle(tp);
      endTitle(tp);
      verify(tp, never()).handleEndTextElement(Matchers.<String>any(), Matchers.<String>any(), Matchers.<String>any());
   }

   @Test
   public void endElementSetsInTitleToFalseWhenEndingTitle() throws Throwable {
      TextPrefilter tp = makeSpy();

      startTitle(tp);
      endTitle(tp);

      assertFalse(tp.inTitle());
   }

   @Test
   public void endElementSetsCurrentTitleToNullWhenEndingPage() throws Throwable {
      TextPrefilter tp = makeSpy();
      startPage(tp);
      startTitle(tp);
      endTitle(tp);
      endPage(tp);

      assertNull(tp.getCurrentTitle());
   }

   @Test
   public void endElementCallsSuperIfText() throws Throwable {
      TextPrefilter tp = makeSpy();
      ContentHandler handler = mock(ContentHandler.class);
      tp.setContentHandler(handler);

      startText(tp);

      String uri = "URI";
      String localName = TextPrefilter.TEXT_ELEMENT_NAME;
      String qName = "Qname";


      tp.endElement(uri, localName, qName);
      verify(handler).endElement(uri, localName, qName);
   }

   @Test
   public void endElementCallsSuperIfTitle() throws Throwable {
      TextPrefilter tp = makeSpy();
      ContentHandler handler = mock(ContentHandler.class);
      tp.setContentHandler(handler);

      startTitle(tp);

      String uri = "URI";
      String localName = TextPrefilter.TITLE_ELEMENT_NAME;
      String qName = "Qname";


      tp.endElement(uri, localName, qName);
      verify(handler).endElement(uri, localName, qName);
   }


   @Test
   public void endElementCallsSuperIfPage() throws Throwable {
      TextPrefilter tp = makeSpy();
      ContentHandler handler = mock(ContentHandler.class);
      tp.setContentHandler(handler);

      startPage(tp);

      String uri = "URI";
      String localName = TextPrefilter.PAGE_ELEMENT_NAME;
      String qName = "Qname";

      tp.endElement(uri, localName, qName);
      verify(handler).endElement(uri, localName, qName);
   }

   //
   // getCurrentTitle
   //

   @Test
   public void currentTitleInitiallyNull() throws Throwable {
      TextPrefilter tp = makeSpy();
      assertNull("Should be initially null", tp.getCurrentTitle());
   }

   @Test
   public void currentTitleBlankAfterStartTitle() throws Throwable {
      TextPrefilter tp = makeSpy();
      startPage(tp);
      startTitle(tp);
      assertEquals("", tp.getCurrentTitle());
   }

   // The characters and endElement methods test other currentTitle behavior

}
