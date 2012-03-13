package edu.gvsu.kurmasz.zawilinski;

import org.junit.Test;
import org.mockito.Matchers;
import org.xml.sax.SAXException;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * @author Zachary Kurmas
 */
// Created  3/4/12 at 10:36 AM
// (C) Zachary Kurmas 2012

public class LanguagePrefilterTest extends TextPrefilterTest {


   // When testing  LanguagePrefilter we want to verify that the correct set of
   // characters gets passed to sendCharacters.  However, the characters can get
   // passed over several calls, and we don't specify how they should or shouldn't be broken
   // up.  This class keeps track of all the characters passed so we can verify
   // the final result.
   private static class TestableLanguagePrefilter extends LanguagePrefilter {

      StringBuffer passed = new StringBuffer();

      /**
       * Constructor.
       *
       * @param language language to search for
       */
      public TestableLanguagePrefilter(String language) {
         super(language);
      }

      /**
       * Sends characters through the filter (i.e., calls {@code XMLFilterImpl.characters()}). Note that
       * calling {@code super.characters()} from a subclass won't pass characters through the filter.  It will
       * make a recursive call to {@link edu.gvsu.kurmasz.zawilinski.TextPrefilter#characters(char[], int, int)} characters}.
       *
       * @param ch     An array of characters.
       * @param start  The starting position in the array.
       * @param length The number of characters to use from the array.
       * @throws org.xml.sax.SAXException The client may throw an exception during processing.
       */
      @Override
      protected void sendCharacters(char[] ch, int start, int length) throws SAXException {
         passed.append(ch, start, length);
         super.sendCharacters(ch, start, length);    //To change body of overridden methods use File | Settings | File Templates.
      }

      public String sent() {
         return passed.toString();
      }

   }


   private static final String LANGUAGE = "Polish";


   private static final char[] CHARS = "\n .,abcdefghijklmnopqrstuvwxyz-?<>".toCharArray();

   private static char[] makeRandomString(int length) {
      // The use of the seed 0 and a new Random object is needed for repeatability
      Random rand = new Random(0);

      char[] answer = new char[length];

      for (int i = 0; i < length; i++) {
         answer[i] = CHARS[rand.nextInt(CHARS.length)];
      }
      return answer;
   }

   private static char[] LONG_ARRAY = makeRandomString(8192);

   public TestableLanguagePrefilter make() {
      return new TestableLanguagePrefilter(LANGUAGE);
   }

   public TestableLanguagePrefilter makeSpy() {
      return spy(make());
   }


   @Test
   public void Something() throws Throwable {
      fail("Make sure Language begins with non '=' so ===Polish=== is not accepted.");
   }

   @Test
   public void V1() throws Throwable {
      fail("implement test (V1)");
   }

   @Test
   public void V2() throws Throwable {
      fail("implement test (V2)");
   }

   @Test
   public void reChecksearchLanguageEndParams() throws Throwable {
      fail("reverify that changing parms to searchForLanguageEnd produces at least one fail.");
   }


   @Test
   public void makeLanguageSearchString() throws Throwable {
      assertEquals("==Soupy==", LanguagePrefilter.makeLanguageSearchString("Soupy"));
   }


   //
   // findTargetLangaugeHeader
   //

   @Test
   public void findsAtBeginning() throws Throwable {
      String s = "==Polish==Words and such";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 0, s.length(),
            "==Polish==".toCharArray());
      assertEquals(0, observed);
   }

   @Test
   public void findsAtBeginning_offset() throws Throwable {
      // Notice that a start of 10 prevents us from finding the first ==Polish==
      String s = "==Polish==0123456789==Polish==Words and such";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 20, s.length() - 20,
            "==Polish==".toCharArray());
      assertEquals(20, observed);
   }

   @Test
   public void findsInMiddle() throws Throwable {
      String s = "abcdefghik==Polish==Words and such";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 0, s.length(),
            "==Polish==".toCharArray());
      assertEquals(10, observed);
   }

   @Test
   public void findsInMiddle_offset() throws Throwable {
      String s = "==Polish==abcdefghik==Polish==Words and such";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 10, s.length() - 10,
            "==Polish==".toCharArray());
      assertEquals(20, observed);
   }

   @Test
   public void findsInMiddle_disregardPartial() throws Throwable {
      String s = "abc==Poli==defghij==Polish==Words and such";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 0, s.length(),
            "==Polish==".toCharArray());
      assertEquals(18, observed);
   }

   @Test
   public void findsInMiddle_disregardOverlap() throws Throwable {
      String s = "abc==Pol==Polish==Words and such";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 0, s.length(),
            "==Polish==".toCharArray());
      assertEquals(8, observed);
   }

   @Test
   public void findsFirstOccurrence() throws Throwable {
      String s = "01==Polis==234==Polish==foo==Polish==bar==Polish==";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 0, s.length(),
            "==Polish==".toCharArray());
      assertEquals(14, observed);
   }

   @Test
   public void findsAtEnd() throws Throwable {
      String s = "0123456789abcdefghik==Polish==";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 0, s.length(),
            "==Polish==".toCharArray());
      assertEquals(20, observed);
   }

   @Test
   public void findsAtEnd_partial() throws Throwable {
      String s = "0123456789==Pa==Pol==bcdish==efghik==Polish==";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 0, s.length(),
            "==Polish==".toCharArray());
      assertEquals(35, observed);
   }

   @Test
   public void findsAtEnd_overlap() throws Throwable {
      String s = "0123456789==Po==Polish==";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 0, s.length(),
            "==Polish==".toCharArray());
      assertEquals(14, observed);
   }

   @Test
   public void findsAtEnd_offset() throws Throwable {
      String s = "0123456789abcdefghik==Polish==";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 10, s.length() - 10,
            "==Polish==".toCharArray());
      assertEquals(20, observed);
   }

   @Test
   public void findsPartial9() throws Throwable {
      String s = "01234==Polish=";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 0, s.length(),
            "==Polish==".toCharArray());
      assertEquals(5, observed);
   }

   @Test
   public void findsPartial8() throws Throwable {
      String s = "01234==Polish";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 0, s.length(),
            "==Polish==".toCharArray());
      assertEquals(5, observed);
   }


   @Test
   public void findsPartial7() throws Throwable {
      String s = "01234==Polis";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 0, s.length(),
            "==Polish==".toCharArray());
      assertEquals(5, observed);
   }

   @Test
   public void findsPartial3() throws Throwable {
      String s = "01234==P";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 0, s.length(),
            "==Polish==".toCharArray());
      assertEquals(5, observed);
   }

   @Test
   public void findsPartial2() throws Throwable {
      String s = "01234==";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 0, s.length(),
            "==Polish==".toCharArray());
      assertEquals(5, observed);
   }

   @Test
   public void findsPartial1() throws Throwable {
      String s = "01234=";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 0, s.length(),
            "==Polish==".toCharArray());
      assertEquals(5, observed);
   }

   @Test
   public void findPartail_overlap() throws Throwable {
      String s = "=Polish==";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 0, s.length(),
            "==Polish==".toCharArray());
      assertEquals(7, observed);
   }

   @Test
   public void findPartail_overlap_offset() throws Throwable {
      // First character is before start
      String s = "==Polish==";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 1, s.length() - 1,
            "==Polish==".toCharArray());
      assertEquals(8, observed);
   }

   @Test
   public void findPartial_stopsAtLength9() throws Throwable {
      // Only consider 9 characters, so this should detect a partial
      String s = "==Polish=!";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 0, 9,
            "==Polish==".toCharArray());
      assertEquals(0, observed);
   }

   @Test
   public void findPartial_stopsAtLength8() throws Throwable {
      // Only consider 9 characters, so this should detect a partial
      String s = "==PolishDude";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 0, 8,
            "==Polish==".toCharArray());
      assertEquals(0, observed);
   }

   @Test
   public void findPartial_stopsAtLength1() throws Throwable {
      // Only consider 9 characters, so this should detect a partial
      String s = "ThisWillComp=letelyFakeItOut";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 4, 9,
            "==Polish==".toCharArray());
      assertEquals(12, observed);
   }

   @Test
   public void recognizesNotFound_short() throws Throwable {
      String s = "abc";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 0, s.length(),
            "==Polish==".toCharArray());
      assertEquals(-1, observed);
   }

   @Test
   public void recognizesNotFound_offset_short() throws Throwable {
      String s = "0123456789abc";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 10, s.length() - 10,
            "==Polish==".toCharArray());
      assertEquals(-1, observed);
   }

   @Test
   public void recognizesNotFound_partial() throws Throwable {
      String s = "=Polish==S";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 0, s.length(),
            "==Polish==".toCharArray());
      assertEquals(-1, observed);
   }

   @Test
   public void recognizesNotFound_partial_offset() throws Throwable {
      // Shouldn't be found because first '=' is out of scope
      String s = "==Polish==S";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 1, s.length() - 1,
            "==Polish==".toCharArray());
      assertEquals(-1, observed);
   }

   @Test
   public void recognizesNotFound_partial_offset2() throws Throwable {
      String s = "Words==Polish==FollowedbyMoreWords";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 6, s.length() - 6,
            "==Polish==".toCharArray());
      assertEquals(-1, observed);
   }

   @Test
   public void recognizesNotFound_beforeStart() throws Throwable {
      String s = "Words==Polish==Data starts here.";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 16, s.length() - 16,
            "==Polish==".toCharArray());
      assertEquals(-1, observed);
   }

   @Test
   public void recognizesNotFound_beforeAfterLength() throws Throwable {
      String s = "Words==Polish==Data starts here.==Polish==Stuff";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 15, 17,
            "==Polish==".toCharArray());
      assertEquals(-1, observed);
   }

   @Test
   public void recognizesNotFound_partialAndAfterLength() throws Throwable {
      String s = "Words==Polish==Data starts here.==Polish==Stuff";
      int observed = LanguagePrefilter.findTargetLanguageHeader(s.toCharArray(), 6, 26,
            "==Polish==".toCharArray());
      assertEquals(-1, observed);
   }


   //
   //   handle TextElementCharacters
   //

   @Test
   public void dropsShortDataIfLanguageNotPresent() throws Throwable {
      LanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abc");
      endText(pf);
      verify(pf, never()).sendCharacters(Matchers.<char[]>any(), anyInt(), anyInt());
   }

   @Test
   public void dropsShortDataIfLanguageNotPresent_pad() throws Throwable {
      LanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abc", 5);
      endText(pf);
      verify(pf, never()).sendCharacters(Matchers.<char[]>any(), anyInt(), anyInt());
   }


   @Test
   public void dropsMediumDataIfLanguageNotPresent() throws Throwable {
      LanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abcdefghijk012343498397");
      endText(pf);
      verify(pf, never()).sendCharacters(Matchers.<char[]>any(), anyInt(), anyInt());
   }

   @Test
   public void dropsMediumDataIfLanguageNotPresent_pad() throws Throwable {
      LanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abcdefghijk012343498397", 7);
      endText(pf);
      verify(pf, never()).sendCharacters(Matchers.<char[]>any(), anyInt(), anyInt());
   }

   @Test
   public void dropsLongDataIfLanguageNotPresent() throws Throwable {
      LanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, new String(LONG_ARRAY));
      endText(pf);
      verify(pf, never()).sendCharacters(Matchers.<char[]>any(), anyInt(), anyInt());
   }

   @Test
   public void dropsMixedDataIfLanguageNotPresent() throws Throwable {
      LanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "h");
      sendCharacters(pf, "ijk0093", 12);
      sendCharacters(pf, "l", 3);
      sendCharacters(pf, "l", 15);
      sendCharacters(pf, new String(LONG_ARRAY));
      sendCharacters(pf, "==NotPolish==", 8);
      sendCharacters(pf, new String(LONG_ARRAY), 1);
      sendCharacters(pf, "03==Polish=", 243);
      sendCharacters(pf, new String(LONG_ARRAY), 112);
      sendCharacters(pf, new String(LONG_ARRAY, LONG_ARRAY.length / 2, LONG_ARRAY.length / 4));
      sendCharacters(pf, new String(LONG_ARRAY, 100, 100));
      endText(pf);
      verify(pf, never()).sendCharacters(Matchers.<char[]>any(), anyInt(), anyInt());
   }

   @Test
   public void dropsDataIfEndOfLanguageHeaderMising() throws Throwable {
      LanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "==Polish=", 29);
      sendCharacters(pf, "This should not be sent", 2);
      endText(pf);
      verify(pf, never()).sendCharacters(Matchers.<char[]>any(), anyInt(), anyInt());
   }

   @Test
   public void dropsDataIfBeginningOfLanguageHeaderMising() throws Throwable {
      LanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "=Polish==", 2);
      sendCharacters(pf, new String(LONG_ARRAY), 112);
      endText(pf);
      verify(pf, never()).sendCharacters(Matchers.<char[]>any(), anyInt(), anyInt());
   }


   @Test
   public void passesDataForLanguage_StartOfText_noNewLine() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "==Polish==Keep Line 1\nKeep Line 2");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      endText(pf);
      assertEquals("Keep Line 1\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }

   @Test
   public void passesDataForLanguage_StartOfText_noNewLine_pad() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "==Polish==Keep Line 1\nKeep Line 2", 5);
      sendCharacters(pf, "Keep Line 3", 17);
      sendCharacters(pf, "which is longer than the rest", 3);
      endText(pf);
      assertEquals("Keep Line 1\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }

   @Test
   public void passesDataForLanguage_StartOfText_newLine() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "==Polish==\nKeep Line 1\nKeep Line 2", 12);
      sendCharacters(pf, "Keep Line 3", 19);
      sendCharacters(pf, "which is longer than the rest", 103);
      endText(pf);
      assertEquals("\nKeep Line 1\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }

   @Test
   public void passesDataForLanguage_StartOfText_alone() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "==Polish==");
      sendCharacters(pf, "First line to keep\nKeep Line 2");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      endText(pf);
      assertEquals("First line to keep\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }

   @Test
   public void passesDataForLanguage_StartOfText_alone_pad1() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "==Polish==", 1);
      sendCharacters(pf, "First line to keep\nKeep Line 2", 3);
      sendCharacters(pf, "Keep Line 3", 223);
      sendCharacters(pf, "which is longer than the rest", 17);
      endText(pf);
      assertEquals("First line to keep\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }

   @Test
   public void passesDataForLanguage_StartOfText_alone_pad2() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "==Polish==", 16);
      sendCharacters(pf, "First line to keep\nKeep Line 2", 3);
      sendCharacters(pf, "Keep Line 3", 223);
      sendCharacters(pf, "which is longer than the rest", 17);
      endText(pf);
      assertEquals("First line to keep\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }


   @Test
   public void passesDataForLanguage_StartOfText_shortSplitCombined() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "==Pol");
      sendCharacters(pf, "is");
      sendCharacters(pf, "h==1st line to keep\nKeep Line 2");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      endText(pf);
      assertEquals("1st line to keep\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }

   @Test
   public void passesDataForLanguage_StartOfText_shortSplitCombined_pad() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "==Pol", 87);
      sendCharacters(pf, "is", 6);
      sendCharacters(pf, "h==1st line to keep\nKeep Line 2", 19);
      sendCharacters(pf, "Keep Line 3", 22);
      sendCharacters(pf, "which is longer than the rest", 41);
      endText(pf);
      assertEquals("1st line to keep\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }


   @Test
   public void passesDataForLanguage_StartOfText_shortSplitAlone() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "==Pol", 5);
      sendCharacters(pf, "is", 17);
      sendCharacters(pf, "h==", 19);
      sendCharacters(pf, "Keep Line 1\nKeep Line 2", 26);
      sendCharacters(pf, "Keep Line 3", 41);
      sendCharacters(pf, "which is longer than the rest", 227);
      endText(pf);
      assertEquals("Keep Line 1\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }

   @Test
   public void passesDataForLanguage_StartOfText_longSplitCombined() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "==Poli", 8);
      sendCharacters(pf, "sh==Line 1 for keeping\nKeep Line 2", 17);
      sendCharacters(pf, "Keep Line 3", 32);
      sendCharacters(pf, "which is longer than the rest", 13);
      endText(pf);
      assertEquals("Line 1 for keeping\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }

   @Test
   public void passesDataForLanguage_StartOfLine_noNewline() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop", 8);
      sendCharacters(pf, "==Polish==Keep Line 1\nKeep Line 2", 12);
      sendCharacters(pf, "Keep Line 3", 13);
      sendCharacters(pf, "which is longer than the rest", 19);
      endText(pf);
      assertEquals("Keep Line 1\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }

   @Test
   public void passesDataForLanguage_StartOfLine_newline() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abcdefg", 9);
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, "==Polish==\nKeep Line 1\nKeep Line 2");
      sendCharacters(pf, "Keep Line 3", 13);
      sendCharacters(pf, "which is longer than the rest");
      endText(pf);
      assertEquals("\nKeep Line 1\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }

   @Test
   public void passesDataForLanguage_StartOfLine_alone() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abcdefg", 31);
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, "==Polish==", 89);
      sendCharacters(pf, "My Line 1\nKeep Line 2");
      sendCharacters(pf, "Keep Line 3", 22);
      sendCharacters(pf, "which is longer than the rest");
      endText(pf);
      assertEquals("My Line 1\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }

   @Test
   public void passesDataForLanguage_StartOfLine_shortSplitCombined() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abcdefg", 5);
      sendCharacters(pf, "hijklmnop", 5);
      sendCharacters(pf, "==Pol", 5);
      sendCharacters(pf, "is", 1);
      sendCharacters(pf, "h==Lovely Line 1\nKeep Line 2", 8);
      sendCharacters(pf, "Keep Line 3", 212);
      sendCharacters(pf, "which is longer than the rest", 4);
      endText(pf);
      assertEquals("Lovely Line 1\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }


   @Test
   public void passesDataForLanguage_StartOfLine_longSplitCombined() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, "==Polis", 223);
      sendCharacters(pf, "h==Da Line 1\nKeep Line 2", 332);
      sendCharacters(pf, "Keep Line 3", 7);
      sendCharacters(pf, "which is longer than the rest");
      endText(pf);
      assertEquals("Da Line 1\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }


   @Test
   public void passesDataForLanguage_StartOfLine_splitAlone() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, "==Pol");
      sendCharacters(pf, "is", 119);
      sendCharacters(pf, "h==");
      sendCharacters(pf, "Dreamy Line 1\nKeep Line 2");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      endText(pf);
      assertEquals("Dreamy Line 1\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }


   @Test
   public void passesDataForLanguage_MiddleOfLine_noNewline() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abcdefg", 4);
      sendCharacters(pf, "hijklmnop==Polish==Keep Line 1\nKeep Line 2", 4);
      sendCharacters(pf, "Keep Line 3", 4);
      sendCharacters(pf, "which is longer than the rest", 4);
      endText(pf);
      assertEquals("Keep Line 1\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }

   @Test
   public void passesDataForLanguage_MiddleOfLine_newline() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abcdefg", 5);
      sendCharacters(pf, "hijklmnop\n==Polish==\nKeep Line 1\nKeep Line 2", 34);
      sendCharacters(pf, "Keep Line 3", 31);
      sendCharacters(pf, "which is longer than the rest", 4);
      endText(pf);
      assertEquals("\nKeep Line 1\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }

   @Test
   public void passesDataForLanguage_nearEndOfLine() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "qrstuvw==Polish==x", 8);
      sendCharacters(pf, "Line uno\nKeep Line 2", 5);
      sendCharacters(pf, "Keep Line 3", 3);
      sendCharacters(pf, "which is longer than the rest", 21);
      endText(pf);
      assertEquals("xLine uno\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }

   @Test
   public void passesDataForLanguage_nearEndOfLine_split() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "qrstuvw==Poli");
      sendCharacters(pf, "sh==x", 8);
      sendCharacters(pf, "Line uno\nKeep Line 2", 5);
      sendCharacters(pf, "Keep Line 3", 3);
      sendCharacters(pf, "which is longer than the rest", 21);
      endText(pf);
      assertEquals("xLine uno\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }


   @Test
   public void passesDataForLanguage_EndOfLine() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abcdefg", 1);
      sendCharacters(pf, "hijklmnop", 7);
      sendCharacters(pf, "qrstuvw==Polish==", 8);
      sendCharacters(pf, "Line uno\nKeep Line 2", 5);
      sendCharacters(pf, "Keep Line 3", 3);
      sendCharacters(pf, "which is longer than the rest", 21);
      endText(pf);
      assertEquals("Line uno\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }


   @Test
   public void passesDataForLanguage_EndOfLine_shortSplitCombined() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abcdefg", 19);
      sendCharacters(pf, "hijklmnop", 19);
      sendCharacters(pf, "urmqu==Pol", 19);
      sendCharacters(pf, "is", 18);
      sendCharacters(pf, "h==", 17);
      sendCharacters(pf, "Line 1\nKeep Line 2", 17);
      sendCharacters(pf, "Keep Line 3", 16);
      sendCharacters(pf, "which is longer than the rest");
      endText(pf);
      assertEquals("Line 1\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }

   @Test
   public void passesDataForLanguage_EndOfLine_longSplit() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abcdefg", 13);
      sendCharacters(pf, "hijklmnop", 12);
      sendCharacters(pf, "urmqu==Poli", 8);
      sendCharacters(pf, "sh==", 9);
      sendCharacters(pf, "The first Line\nKeep Line 2", 10);
      sendCharacters(pf, "Keep Line 3", 11);
      sendCharacters(pf, "which is longer than the rest", 12);
      endText(pf);
      assertEquals("The first Line\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }

   @Test
   public void passesDataForLanguage_EndOfLine_splitAlone() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, "urumq==Pol");
      sendCharacters(pf, "is");
      sendCharacters(pf, "h==");
      sendCharacters(pf, "Line Jeden\nKeep Line 2");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      endText(pf);
      assertEquals("Line Jeden\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }

   @Test
   public void failedPartialLongMatchRestartsCorrectly() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abcdefg", 5);
      sendCharacters(pf, "hijklmnop", 6);
      sendCharacters(pf, "urumq==Pol", 7);
      sendCharacters(pf, "is", 8);
      sendCharacters(pf, "h=Line Jeden\nKeep Line 2==Polish==", 9);
      sendCharacters(pf, "Keep Line 3", 10);
      sendCharacters(pf, "which is longer than the rest", 11);
      endText(pf);
      assertEquals("Keep Line 3which is longer than the rest", pf.sent());
   }

   @Test
   public void failedPartialShortMatchRestartsCorrectly() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abcdefg", 5);
      sendCharacters(pf, "hijklmnop", 6);
      sendCharacters(pf, "urumq==Pol", 7);
      sendCharacters(pf, "x=", 8);
      sendCharacters(pf, "=Polish==", 9);
      sendCharacters(pf, "Keep Line 3", 10);
      sendCharacters(pf, "which is longer than the rest", 11);
      endText(pf);
      assertEquals("Keep Line 3which is longer than the rest", pf.sent());
   }

   @Test
   public void handlesOverlap() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abcdefg", 5);
      sendCharacters(pf, "hijklmnop", 6);
      sendCharacters(pf, "==Pol==Polish==x==Po", 3);
      sendCharacters(pf, "Keep Line 3", 10);
      sendCharacters(pf, "which is longer than the rest", 11);
      endText(pf);
      assertEquals("x==PoKeep Line 3which is longer than the rest", pf.sent());
   }


   // Does't pass different preceeding language

   // Doesn't pass succeeding different language

   // Isn't confused by false == in language text

   // Recognizes when language start broken across invocations (one for each character at wich its broken)

   // Recognizes when end broken across invocations

   // First article contains polish.  Starting second article begins filtering.

   // First article contains Polish, Second article can also contain polish.

   // Make sure partial data from previous article doesn't affect subsequent articles.

   // Doesn't start on ===Polish=== or ====Polish====

   // Doesn't stop on ===Sthg=== or ====sthg====

}
