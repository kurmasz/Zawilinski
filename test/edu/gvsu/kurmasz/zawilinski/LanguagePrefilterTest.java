package edu.gvsu.kurmasz.zawilinski;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.xml.sax.SAXException;

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


   private static final String LANGUAGE = "My Favorite Language=really";
   private static final String LANGUAGE_HEADER = "==" + LANGUAGE + "==";
   private static final String LH_P1 = LANGUAGE_HEADER.substring(0, 5);
   private static final String LH_P2 = LANGUAGE_HEADER.substring(5, 8);
   private static final String LH_P3 = LANGUAGE_HEADER.substring(8);

   /*
   private static final char[] CHARS = "\n .,abcdefghijklmnopqrstuvwxyz-?<>".toCharArray();

   private static char[] makeRandomString(int length) {
      // The use of the seed 0 and a new Random object is needed for repeatability
      java.util.Random rand = new java.util.Random(0);

      char[] answer = new char[length];

      for (int i = 0; i < length; i++) {
         answer[i] = CHARS[rand.nextInt(CHARS.length)];
      }
      return answer;
   }

   private static char[] LONG_ARRAY = makeRandomString(8192);
   */
   public TestableLanguagePrefilter make() {
      return new TestableLanguagePrefilter(LANGUAGE);
   }

   public TestableLanguagePrefilter makeSpy() {
      return spy(make());
   }

   @Ignore
   @Test
   public void reChecksearchLanguageEndParams() throws Throwable {
      fail("reverify that changing parms to searchForLanguageEnd produces at least one fail.");
   }


   //
   //  Drops all data when no header visible
   //

   @Test
   public void dropsEverythingIfLanguageNotPresent() throws Throwable {
      LanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "abc");
      endText(pf);
      verify(pf, never()).sendCharacters(Matchers.<char[]>any(), anyInt(), anyInt());
   }


   @Test
   public void dropsEverythingIfLanguageNotPresent_multipleLines() throws Throwable {
      LanguagePrefilter pf = makeSpy();

      startText(pf);
      sendCharacters(pf, "Four score and seven years ago");
      sendCharacters(pf, "Our fathers brought forth on this continent a new nation,");
      sendCharacters(pf, "conceived in liberty, and dedicated to the proposition that all men are created equal.\n");
      sendCharacters(pf, "Now we are engaged in a great civil war, testing whether that nation,\nor any nation...");
      sendCharacters(pf, "==NOT" + LANGUAGE + "== so conceived and so dedicated, can long endure.");
      sendCharacters(pf, "We are met on a great battle-field of that war. ==Another irrelevant headerContent== We have");
      sendCharacters(pf, "Come to dedicate a portion of that field, as a final resting place for those who ");
      sendCharacters(pf, "Here gave their lives ===" + LANGUAGE + "=== (notice wrong level) that that nation might live.");
      sendCharacters(pf, "It is altogether fitting and proper =Polish= that we should do this.\n");
      sendCharacters(pf, "==" + LANGUAGE + "===");
      sendCharacters(pf, "(Unbalanced) But, in a larger sense");
      sendCharacters(pf, "===" + LANGUAGE + "==\n");
      sendCharacters(pf, "(Unbalanced again) we can not dedicate, we can not consecrate, " +
            "we can not hallow this ground...");
      endText(pf);
      verify(pf, never()).sendCharacters(Matchers.<char[]>any(), anyInt(), anyInt());
   }

   @Test
   public void ignoresHeaderBeforeStart() throws Throwable {
      LanguagePrefilter pf = makeSpy();
      startText(pf);
      sendCharacters(pf, "Four score and seven years ago");
      String s = LANGUAGE_HEADER + "Our fathers brought forth on this continent a new nation,";
      sendCharacters(pf, s, LANGUAGE_HEADER.length(), s.length() - LANGUAGE_HEADER.length());
      endText(pf);
      verify(pf, never()).sendCharacters(Matchers.<char[]>any(), anyInt(), anyInt());
   }

   @Test
   public void ignoresHeaderAfterLength() throws Throwable {
      LanguagePrefilter pf = makeSpy();
      startText(pf);
      sendCharacters(pf, "Four score and seven years ago");
      String s = "Our fathers brought forth on this continent a new nation," + LANGUAGE_HEADER;
      sendCharacters(pf, s, 0, s.length() - LANGUAGE_HEADER.length());
      sendCharacters(pf, "conceived in liberty, and dedicated to the proposition that all men are created equal.\n");
      endText(pf);
      verify(pf, never()).sendCharacters(Matchers.<char[]>any(), anyInt(), anyInt());
   }


   @Test
   public void dropsPartialHeader() throws Throwable {
      LanguagePrefilter pf = makeSpy();
      startText(pf);
      sendCharacters(pf, "Four score and seven years ago");
      String s = "Our fathers brought forth on this continent a new nation," + LH_P1;
      endText(pf);
      verify(pf, never()).sendCharacters(Matchers.<char[]>any(), anyInt(), anyInt());
   }

   //
   //  Passes data after header
   //


   @Test
   public void passesDataForLanguage_StartOfText_noNewLine() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, LANGUAGE_HEADER + "Keep Line 1\nKeep Line 2");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "Keep Line 1\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }


   @Test
   public void passesDataForLanguage_StartOfText_newLine() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, LANGUAGE_HEADER + "\nKeep Line 1\nKeep Line 2");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "\nKeep Line 1\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }

   @Test
   public void passesDataForLanguage_StartOfText_alone() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, LANGUAGE_HEADER);
      sendCharacters(pf, "First line to keep\nKeep Line 2");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "First line to keep\nKeep Line 2Keep Line 3which is longer than the rest",
            pf.sent());
   }

   @Test
   public void passesDataForLanguage_StartOfText_shortSplitCombined() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, LH_P1);
      sendCharacters(pf, LH_P2);
      sendCharacters(pf, LH_P3 + "1st line to keep\nKeep Line 2");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "1st line to keep\nKeep Line 2Keep Line 3which is longer than the rest",
            pf.sent());
   }

   @Test
   public void passesDataForLanguage_StartOfText_shortSplitAlone() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, LH_P1);
      sendCharacters(pf, LH_P2);
      sendCharacters(pf, LH_P3);
      sendCharacters(pf, "1st line to keep\nKeep Line 2");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "1st line to keep\nKeep Line 2Keep Line 3which is longer than the rest",
            pf.sent());
   }


   @Test
   public void passesDataForLanguage_StartOfSegment_noNewline() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, LANGUAGE_HEADER + "Keep Line 1\nKeep Line 2");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "Keep Line 1\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }

   @Test
   public void passesDataForLanguage_StartOfSegment_newline() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, LANGUAGE_HEADER + "\nKeep Line 1\nKeep Line 2");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "\nKeep Line 1\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }

   @Test
   public void passesDataForLanguage_StartOfSegment_alone() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, LANGUAGE_HEADER);
      sendCharacters(pf, "My Line 1\nKeep Line 2");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "My Line 1\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }

   @Test
   public void passesDataForLanguage_StartOfSegment_shortSplitCombined() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, LH_P1);
      sendCharacters(pf, LH_P2);
      sendCharacters(pf, LH_P3 + "Lovely Line 1\nKeep Line 2");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "Lovely Line 1\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }


   @Test
   public void passesDataForLanguage_StartOfSegment_splitAlone() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, LH_P1);
      sendCharacters(pf, LH_P2);
      sendCharacters(pf, LH_P3);
      sendCharacters(pf, "Dreamy Line 1\nKeep Line 2");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "Dreamy Line 1\nKeep Line 2Keep Line 3which is longer than the rest",
            pf.sent());
   }


   @Test
   public void passesDataForLanguage_MiddleOfLine_noNewline() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop" + LANGUAGE_HEADER + "Keep Line 1\nKeep Line 2");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "Keep Line 1\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }

   @Test
   public void passesDataForLanguage_MiddleOfLine_newline() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop\n" + LANGUAGE_HEADER + "\nKeep Line 1\nKeep Line 2");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "\nKeep Line 1\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }

   @Test
   public void passesDataForLanguage_EndOfLine() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, "qrstuvw" + LANGUAGE_HEADER);
      sendCharacters(pf, "Line uno\nKeep Line 2");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "Line uno\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }


   @Test
   public void passesDataForLanguage_EndOfLine_shortSplitCombined() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, "urmqu" + LH_P1);
      sendCharacters(pf, LH_P2);
      sendCharacters(pf, LH_P3);
      sendCharacters(pf, "Line 1\nKeep Line 2");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "Line 1\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }


   @Test
   public void passesDataForLanguage_EndOfLine_splitAlone() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, "urumq" + LH_P1);
      sendCharacters(pf, LH_P2);
      sendCharacters(pf, LH_P3);
      sendCharacters(pf, "Line Jeden\nKeep Line 2");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "Line Jeden\nKeep Line 2Keep Line 3which is longer than the rest", pf.sent());
   }

   @Test
   public void ignoresDataBeforeStart() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "Outside line 1");
      sendCharacters(pf, "Outside line 2");
      sendCharacters(pf, LANGUAGE_HEADER);
      sendCharacters(pf, "-->,--Content line 1\n((((", 6, 15);
      sendCharacters(pf, "****Content line 2====", 4, 14);
      sendCharacters(pf, "which is longer than the others");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "Content line 1\nContent line 2which is longer than the others", pf.sent());
   }

   @Test
   public void ignoresPreviousHeaders_SameLine() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "My Country\n");
      sendCharacters(pf, "==Not the header==Tis ==still no==of thee\n" + LANGUAGE_HEADER + "Sweet land of liberty");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "Sweet land of liberty", pf.sent());
   }

   @Test
   public void ignoresPreviousHeaders_DifferentLine() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "My Country\n");
      sendCharacters(pf, "==Not the header==");
      sendCharacters(pf, "Tis of thee\n");
      sendCharacters(pf, LANGUAGE_HEADER);
      sendCharacters(pf, "Sweet land of liberty");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "Sweet land of liberty", pf.sent());
   }

   @Test
   public void ignoresWrongLevelHeaders_SameLine() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "My Country\n");
      sendCharacters(pf, "=" + LANGUAGE_HEADER + "=Tis of thee\n" + LANGUAGE_HEADER);
      sendCharacters(pf, "Sweet land of liberty");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "Sweet land of liberty", pf.sent());
   }

   @Test
   public void ignoresWrongLevelHeaders_DifferentLine() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "My Country\n");
      sendCharacters(pf, "=" + LANGUAGE_HEADER + "=");
      sendCharacters(pf, "Tis of the\n");
      sendCharacters(pf, LANGUAGE_HEADER);
      sendCharacters(pf, "Sweet land of liberty");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "Sweet land of liberty", pf.sent());
   }

   @Test
   public void ignoresUnbalancedLeft_SameLine() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "My Country\n");
      sendCharacters(pf, "=" + LANGUAGE_HEADER + "Tis of the\n" + LANGUAGE_HEADER);
      sendCharacters(pf, "Sweet land of liberty");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "Sweet land of liberty", pf.sent());
   }

   @Test
   public void ignoresUnbalancedLeft_DifferentLine() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "My Country\n");
      sendCharacters(pf, "=" + LANGUAGE_HEADER);
      sendCharacters(pf, "Tis of the\n");
      sendCharacters(pf, LANGUAGE_HEADER);
      sendCharacters(pf, "Sweet land of liberty");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "Sweet land of liberty", pf.sent());
   }

   @Test
   public void ignoresUnbalancedRight_SameLine() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "My Country\n");
      sendCharacters(pf, LANGUAGE_HEADER + "=Tis of thee\n" + LANGUAGE_HEADER);
      sendCharacters(pf, "Sweet land of liberty");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "Sweet land of liberty", pf.sent());
   }

   @Test
   public void ignoresUnbalancedRight_DifferentLine() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "My Country\n");
      sendCharacters(pf, LANGUAGE_HEADER + "=");
      sendCharacters(pf, "Tis of the\n");
      sendCharacters(pf, LANGUAGE_HEADER);
      sendCharacters(pf, "Sweet land of liberty");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "Sweet land of liberty", pf.sent());
   }

   @Test
   public void detectsSecondHeaderSplitOverDifferentLine() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "Words More words");
      sendCharacters(pf, "==Head");
      sendCharacters(pf, "er 1== H1 data " + LH_P1);
      sendCharacters(pf, LH_P2 + LH_P3 + "Polish data");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "Polish data", pf.sent());
   }


   @Test
   public void failedPartialLongMatchRestartsCorrectly() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, "urumq" + LH_P1);
      sendCharacters(pf, LH_P2);
      sendCharacters(pf, "h Line Jeden\nKeep Line 2" + LANGUAGE_HEADER);
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "Keep Line 3which is longer than the rest", pf.sent());
   }

   @Test
   public void passesPartialMatches() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, "urumq" + LH_P1);
      sendCharacters(pf, LH_P2);
      sendCharacters(pf, "h Line Jeden\nKeep Line 2" + LANGUAGE_HEADER);
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest==Partial Ending");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "Keep Line 3which is longer than the rest==Partial Ending", pf.sent());
   }

   @Test
   public void passesEndingMatch() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, "urumq" + LH_P1);
      sendCharacters(pf, LH_P2);
      sendCharacters(pf, "h Line Jeden\nKeep Line 2" + LANGUAGE_HEADER);
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest==Ending==");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "Keep Line 3which is longer than the rest==Ending==", pf.sent());
   }


   //
   //  Stops passing data after next header
   //
   @Test
   public void detectsEndInSameLine() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, "urumq" + LH_P1);
      sendCharacters(pf, LH_P2);
      sendCharacters(pf, LH_P3 + "\nLine Jeden\nKeep Line 2==Spanish==We shouldn't see this line.");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "\nLine Jeden\nKeep Line 2==Spanish==", pf.sent());
   }

   @Test
   public void detectsEndAtEndOfSameLine() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, "urumq" + LH_P1);
      sendCharacters(pf, LH_P2);
      sendCharacters(pf, LH_P3 + "\nLine Jeden\nKeep Line 2==Spanish==");
      sendCharacters(pf, "We shouldn't see this line.");
      endText(pf);
      assertEquals(LANGUAGE_HEADER + "\nLine Jeden\nKeep Line 2==Spanish==", pf.sent());
   }


   @Test
   public void detectsEndInDiffernetLines() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, "urumq" + LH_P1);
      sendCharacters(pf, LH_P2);
      sendCharacters(pf, LH_P3 + "\nLine Jeden\nKeep Line 2\n");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      sendCharacters(pf, "==New Mexico==");
      sendCharacters(pf, "Other data");

      endText(pf);
      assertEquals(LANGUAGE_HEADER + "\nLine Jeden\nKeep Line 2\nKeep Line 3which is longer than the rest==New " +
            "Mexico==",
            pf.sent());
   }

   @Test
   public void doesNotEndForLevel3Headers() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, "urumq" + LH_P1);
      sendCharacters(pf, LH_P2);
      sendCharacters(pf, LH_P3 + "\nLine Jeden\nKeep Line 2\n");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      sendCharacters(pf, "===Warszawa=== the capital");
      sendCharacters(pf, " and largest city");
      sendCharacters(pf, "==New Mexico==");
      sendCharacters(pf, "Other data");

      endText(pf);
      assertEquals(LANGUAGE_HEADER + "\nLine Jeden\nKeep Line 2\nKeep Line 3which is longer than the " +
            "rest===Warszawa=== the capital and largest city==New Mexico==", pf.sent());
   }


   @Test
   public void doesntCloseForUnbalancedHeadersHeaders_3_2() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, "urumq" + LH_P1);
      sendCharacters(pf, LH_P2);
      sendCharacters(pf, LH_P3 + "\nLine Jeden\nKeep Line 2\n");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      sendCharacters(pf, "===Warszawa== the capital");
      sendCharacters(pf, "==New Mexico==");
      sendCharacters(pf, "Other data");

      endText(pf);
      assertEquals(LANGUAGE_HEADER + "\nLine Jeden\nKeep Line 2\nKeep Line 3which is longer than the " +
            "rest===Warszawa== the capital==New Mexico==", pf.sent());
   }

   @Test
   public void doesntCloseForUnbalancedHeadersHeaders_2_3() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, "urumq" + LH_P1);
      sendCharacters(pf, LH_P2);
      sendCharacters(pf, LH_P3 + "\nLine Jeden\nKeep Line 2\n");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      sendCharacters(pf, "==Warszawa=== the capital");
      sendCharacters(pf, "==New Mexico==");
      sendCharacters(pf, "Other data");

      endText(pf);
      assertEquals(LANGUAGE_HEADER + "\nLine Jeden\nKeep Line 2\nKeep Line 3which is longer than the " +
            "rest==Warszawa=== the capital==New Mexico==", pf.sent());
   }

   @Test
   public void doesntCloseForUnclosedHeader() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, "urumq" + LH_P1);
      sendCharacters(pf, LH_P2);
      sendCharacters(pf, LH_P3 + "\nLine Jeden\nKeep Line 2\n");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      sendCharacters(pf, "==Warszawa the capital\n");
      sendCharacters(pf, "and largest city");
      sendCharacters(pf, "==New Mexico==");
      sendCharacters(pf, "Other data");

      endText(pf);
      assertEquals(LANGUAGE_HEADER + "\nLine Jeden\nKeep Line 2\nKeep Line 3which is longer than the " +
            "rest==Warszawa the capital\nand largest city==New Mexico==", pf.sent());
   }

   @Test
   public void unclosedHeaderCanCauseProblems() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, "urumq" + LH_P1);
      sendCharacters(pf, LH_P2);
      sendCharacters(pf, LH_P3 + "\nLine Jeden\nKeep Line 2\n");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      sendCharacters(pf, "==Warszawa the capital ");
      sendCharacters(pf, "and largest city");
      sendCharacters(pf, "==New Mexico==");
      sendCharacters(pf, "Other data");

      endText(pf);
      assertEquals(LANGUAGE_HEADER + "\nLine Jeden\nKeep Line 2\nKeep Line 3which is longer than the " +
            "rest==Warszawa the capital and largest city==", pf.sent());
   }


   @Test
   public void doesntCloseForTagBeforeStart() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, LANGUAGE_HEADER);
      sendCharacters(pf, "\nLine Jeden\nKeep Line 2\n");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      sendCharacters(pf, "==Warszawa== the capital", 12, 12);
      sendCharacters(pf, "==New Mexico==");
      sendCharacters(pf, "Other data");

      endText(pf);
      assertEquals(LANGUAGE_HEADER + "\nLine Jeden\nKeep Line 2\nKeep Line 3which is longer than the " +
            "rest the capital==New Mexico==", pf.sent());
   }

   @Test
   public void doesntCloseForTagAfterEnd() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, LANGUAGE_HEADER);
      sendCharacters(pf, "\nLine Jeden\nKeep Line 2\n");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest==warszawa==", 0, 29);
      sendCharacters(pf, " the capital");
      sendCharacters(pf, "==New Mexico==");
      sendCharacters(pf, "Other data");

      endText(pf);
      assertEquals(LANGUAGE_HEADER + "\nLine Jeden\nKeep Line 2\nKeep Line 3which is longer than the " +
            "rest the capital==New Mexico==", pf.sent());
   }

   @Test
   public void handlesShortHeaders() throws Throwable {
      TestableLanguagePrefilter pf = new TestableLanguagePrefilter("b");

      startText(pf);
      sendCharacters(pf, "One Two Three");
      sendCharacters(pf, "==a==");
      sendCharacters(pf, "Four, Five Six");
      sendCharacters(pf, "==b==");
      sendCharacters(pf, "Seven Eight");
      sendCharacters(pf, "==c==");
      sendCharacters(pf, "Other data");

      endText(pf);
      assertEquals("==b==Seven Eight==c==", pf.sent());
   }

   //
   //  header open only once
   //

   @Test
   public void targetHeaderWIllClose() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, LANGUAGE_HEADER);
      sendCharacters(pf, "\nLine Jeden\nKeep Line 2\n");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      sendCharacters(pf, LANGUAGE_HEADER);
      sendCharacters(pf, " the capital");
      sendCharacters(pf, "==New Mexico==");
      sendCharacters(pf, "Other data");

      endText(pf);
      assertEquals(LANGUAGE_HEADER + "\nLine Jeden\nKeep Line 2\nKeep Line 3which is longer than the " +
            "rest" + LANGUAGE_HEADER, pf.sent());
   }

   @Test
   public void targetHeaderWontReopenClose() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, LANGUAGE_HEADER);
      sendCharacters(pf, "\nLine Jeden\nKeep Line 2\n");
      sendCharacters(pf, "Keep Line 3");
      sendCharacters(pf, "which is longer than the rest");
      sendCharacters(pf, "==Stop==");
      sendCharacters(pf, "data, data, and more data.");
      sendCharacters(pf, LANGUAGE_HEADER);
      sendCharacters(pf, " the capital");
      sendCharacters(pf, "==New Mexico==");
      sendCharacters(pf, "Other data");

      endText(pf);
      assertEquals(LANGUAGE_HEADER + "\nLine Jeden\nKeep Line 2\nKeep Line 3which is longer than the " +
            "rest==Stop==", pf.sent());
   }

   //
   //  resets between articles
   //
   @Test
   public void resetsBetweenArticles_noEndInone() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, LANGUAGE_HEADER);
      sendCharacters(pf, "One, Two, buckle my shoe.");
      endText(pf);

      startText(pf);
      sendCharacters(pf, "Article two, line 1");
      sendCharacters(pf, LANGUAGE_HEADER);
      sendCharacters(pf, "Three, Four, shut the door.");

      assertEquals(LANGUAGE_HEADER + "One, Two, buckle my shoe." +
            LANGUAGE_HEADER + "Three, Four, shut the door.", pf.sent());
   }

   @Test
   public void resetsBetweenArticles_endInone() throws Throwable {
      TestableLanguagePrefilter pf = make();

      startText(pf);
      sendCharacters(pf, "abcdefg");
      sendCharacters(pf, "hijklmnop");
      sendCharacters(pf, LANGUAGE_HEADER);
      sendCharacters(pf, "One, Two, buckle my shoe.");
      sendCharacters(pf, "==Stop Here== don't print this");
      endText(pf);

      startText(pf);
      sendCharacters(pf, "Article two, line 1");
      sendCharacters(pf, LANGUAGE_HEADER);
      sendCharacters(pf, "Three, Four, shut the door.");

      assertEquals(LANGUAGE_HEADER + "One, Two, buckle my shoe.==Stop Here==" +
            LANGUAGE_HEADER + "Three, Four, shut the door.", pf.sent());
   }
}
