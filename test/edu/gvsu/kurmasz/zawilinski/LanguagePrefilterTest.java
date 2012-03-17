package edu.gvsu.kurmasz.zawilinski;

import org.junit.Ignore;
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

   @Ignore
   @Test
   public void Something() throws Throwable {
      fail("Make sure Language begins with non '=' so ===Polish=== is not accepted.");
   }

   @Ignore
   @Test
   public void V1() throws Throwable {
      fail("implement test (V1)");
   }

   @Ignore
   @Test
   public void V2() throws Throwable {
      fail("implement test (V2)");
   }

   @Ignore
   @Test
   public void reChecksearchLanguageEndParams() throws Throwable {
      fail("reverify that changing parms to searchForLanguageEnd produces at least one fail.");
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
      sendCharacters(pf, "03===Polish===p", 243);
      sendCharacters(pf, new String(LONG_ARRAY), 1);
      sendCharacters(pf, "03=Polish==p", 243);
      sendCharacters(pf, new String(LONG_ARRAY), 112);
      sendCharacters(pf, new String(LONG_ARRAY, LONG_ARRAY.length / 2, LONG_ARRAY.length / 4));
      sendCharacters(pf, new String(LONG_ARRAY, 100, 100));
      endText(pf);
      verify(pf, never()).sendCharacters(Matchers.<char[]>any(), anyInt(), anyInt());
   }

   // At the moment, HeaderSearch throws an exception if an = appears inside a header.
   // a Single = is not a header marker, so it should be allowed.  We just haven't had time
   // to add that feature to HeaderSearch
   @Test(expected = IllegalArgumentException.class)
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
      sendCharacters(pf, "==Polish==6eep Line 1\n7eep Line 2", 5);
      sendCharacters(pf, "8eep Line 3", 17);
      sendCharacters(pf, "which is longer than the rest", 3);
      endText(pf);
      assertEquals("6eep Line 1\n7eep Line 28eep Line 3which is longer than the rest", pf.sent());
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

   //
   // Detects second language header
   //

   @Test
   public void detectsSecondHeaderOnSameLine() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "Words More words ==Header 1== H1 data ==Polish== Polish data", 22);
      endText(pf);
      assertEquals(" Polish data", pf.sent());
   }

   @Test
   public void detectsSecondHeaderOnDifferentLine() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "Words More words", 3);
      sendCharacters(pf, "==Header 1==", 15);
      sendCharacters(pf, "H1 data ", 6);
      sendCharacters(pf, "==Polish==", 0);
      sendCharacters(pf, "Polish data", 22);
      endText(pf);
      assertEquals("Polish data", pf.sent());
   }

   @Test
   public void detectsSecondHeaderSplitOverDifferentLine() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "Words More words", 3);
      sendCharacters(pf, "==Head", 3);
      sendCharacters(pf, "er 1== H1 data ==Po", 6);
      sendCharacters(pf, "lish==Polish data", 22);
      endText(pf);
      assertEquals("Polish data", pf.sent());
   }


   @Test
   public void failedPartialLongMatchRestartsCorrectly() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abcdefg", 5);
      sendCharacters(pf, "hijklmnop", 6);
      sendCharacters(pf, "urumq==Pol", 7);
      sendCharacters(pf, "is", 8);
      sendCharacters(pf, "h Line Jeden\nKeep Line 2==Polish==", 9);
      sendCharacters(pf, "Keep Line 3", 10);
      sendCharacters(pf, "which is longer than the rest", 11);
      endText(pf);
      assertEquals("Keep Line 3which is longer than the rest", pf.sent());
   }

   //
   // Detects end
   //

   @Test
   public void detectsEndInSameLine() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abcdefg", 5);
      sendCharacters(pf, "hijklmnop", 6);
      sendCharacters(pf, "urumq==Pol", 7);
      sendCharacters(pf, "is", 8);
      sendCharacters(pf, "h==\nLine Jeden\nKeep Line 2==Spanish==We shouldn't see this line.", 9);
      endText(pf);
      assertEquals("\nLine Jeden\nKeep Line 2==Spanish==", pf.sent());
   }

   @Test
   public void detectsEndAtEndOfSameLine() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abcdefg", 5);
      sendCharacters(pf, "hijklmnop", 6);
      sendCharacters(pf, "urumq==Pol", 7);
      sendCharacters(pf, "is", 8);
      sendCharacters(pf, "h==\nLine Jeden\nKeep Line 2==Spanish==", 9);
      sendCharacters(pf, "We shouldn't see this line.", 3);
      endText(pf);
      assertEquals("\nLine Jeden\nKeep Line 2==Spanish==", pf.sent());
   }


   @Test
   public void detectsEndInDiffernetLines() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abcdefg", 5);
      sendCharacters(pf, "hijklmnop", 6);
      sendCharacters(pf, "urumq==Pol", 7);
      sendCharacters(pf, "is", 8);
      sendCharacters(pf, "h==\nLine Jeden\nKeep Line 2\n", 9);
      sendCharacters(pf, "Keep Line 3", 10);
      sendCharacters(pf, "which is longer than the rest", 11);
      sendCharacters(pf, "==New Mexico==", 11);
      sendCharacters(pf, "Other data", 11);

      endText(pf);
      assertEquals("\nLine Jeden\nKeep Line 2\nKeep Line 3which is longer than the rest==New Mexico==", pf.sent());
   }

   @Test
   public void passesThirdLevelHeaders() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abcdefg", 5);
      sendCharacters(pf, "hijklmnop", 6);
      sendCharacters(pf, "urumq==Pol", 7);
      sendCharacters(pf, "is", 8);
      sendCharacters(pf, "h==\nLine Jeden\nKeep Line 2\n", 9);
      sendCharacters(pf, "Keep Line 3", 10);
      sendCharacters(pf, "which is longer than the rest", 11);
      sendCharacters(pf, "===Warszawa=== the capital", 22);
      sendCharacters(pf, "==New Mexico==", 11);
      sendCharacters(pf, "Other data", 11);

      endText(pf);
      assertEquals("\nLine Jeden\nKeep Line 2\nKeep Line 3which is longer than the rest===Warszawa=== the " +
            "capital==New Mexico==", pf.sent());
   }

   @Test
   public void ignoresUnbalancedHeadersHeaders_3_2() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abcdefg", 5);
      sendCharacters(pf, "hijklmnop", 6);
      sendCharacters(pf, "urumq==Pol", 7);
      sendCharacters(pf, "is", 8);
      sendCharacters(pf, "h==\nLine Jeden\nKeep Line 2\n", 9);
      sendCharacters(pf, "Keep Line 3", 10);
      sendCharacters(pf, "which is longer than the rest", 11);
      sendCharacters(pf, "===Warszawa== the capital", 22);
      sendCharacters(pf, "==New Mexico==", 11);
      sendCharacters(pf, "Other data", 11);

      endText(pf);
      assertEquals("\nLine Jeden\nKeep Line 2\nKeep Line 3which is longer than the rest===Warszawa== the " +
            "capital==New Mexico==", pf.sent());
   }

   @Test
   public void ignoresUnbalancedHeadersHeaders_2_3() throws Throwable {
      TestableLanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abcdefg", 5);
      sendCharacters(pf, "hijklmnop", 6);
      sendCharacters(pf, "urumq==Pol", 7);
      sendCharacters(pf, "is", 8);
      sendCharacters(pf, "h==\nLine Jeden\nKeep Line 2\n", 9);
      sendCharacters(pf, "Keep Line 3", 10);
      sendCharacters(pf, "which is longer than the rest", 11);
      sendCharacters(pf, "==Warszawa=== the capital", 22);
      sendCharacters(pf, "==New Mexico==", 11);
      sendCharacters(pf, "Other data", 11);

      endText(pf);
      assertEquals("\nLine Jeden\nKeep Line 2\nKeep Line 3which is longer than the rest==Warszawa=== the " +
            "capital==New Mexico==", pf.sent());
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
