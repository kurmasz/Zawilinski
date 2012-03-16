package edu.gvsu.kurmasz.zawilinski;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


// Created  3/15/12 at 1:14 PM
// (C) Zachary Kurmas 2012

public class HeaderSearchTest {

   //////////////////////////////////////////////////////////////////////////////
   //
   // Tests that explain subtlties of the algorithm
   //
   //////////////////////////////////////////////////////////////////////////////


   // I think it would be nice to allow headers to contain an = sign, but that
   // code will take some work to write correctly.  So, for now, we punt.
   @Test(expected = IllegalArgumentException.class)
   public void recoversFromUnbalanced2_1() throws Throwable {
      verify_process(hs, "Words ==123=ghi jkl==mno== p", 6, "mno", 26 + 6);
   }

   // Headers are expected to have an open and a close.  Thus, an unclosed header
   // can cause problems, as shown here.
   // However, newlines terminate a header, so this problem shouldn't appear often in practice.
   @Test
   public void unclosed3hidesLevel2HeaderIfNoNewline() throws Throwable {
      verify_process(hs, "Words ===123 words for unclosed header==mno== content", 4);
   }

   // The newline closes the otherwise unclosed level 3 header
   @Test
   public void recoversFromUnclosed3_withNewline() throws Throwable {
      verify_process(hs, "Words ===123 words for unclosed header\nThen newline ==mno== content", 6, "mno", 59 + 6);
   }


   @Test
   public void recoversFromUnbalanced3() throws Throwable {
      verify_process(hs, "Words ===Tier 3== This is the content ==This is the header==.", 4, "This is the header",
            60 + 4);
   }


   private HeaderSearch hs;

   @Before
   public void init() {
      hs = new HeaderSearch();
   }

   private static void verify_process(HeaderSearch headerSearch, String s, int start, String expectedHeader,
                                      int expectedPlace) {
      char[] myChars = new char[(s.length() + start) * 2];

      for (int i = 0; i < start; i++) {
         myChars[i] = '*';
      }
      System.arraycopy(s.toCharArray(), 0, myChars, start, s.length());
      for (int i = start + s.length(); i < myChars.length; i++) {
         myChars[i] = '-';
      }
      HeaderSearch.Result result = headerSearch.process(myChars, start, s.length());
      if (expectedHeader == null) {
         assertNull("expecting search to return null", result);
      } else {
         assertNotNull("process should have returned a non-null result", result);
         assertEquals(expectedHeader, result.header);
         assertEquals(expectedPlace, result.next);
      }
   }


   private static void verify_process(HeaderSearch headerSearch, String s, int start) {
      verify_process(headerSearch, s, start, null, 0);
   }

   /////////////////////////////////////////////////////////////////////////////////////
   //
   // First line tests
   //
   /////////////////////////////////////////////////////////////////////////////////////

   //
   // Front of first line
   //

   @Test
   public void findsHeaderAtFrontOfFirstLine_ShortOffset() throws Throwable {
      verify_process(hs, "==Polish==Then some words", 3, "Polish", 10 + 3);
   }

   @Test
   public void findsHeaderAtFrontOfFirstLineZeroOffset() throws Throwable {
      verify_process(hs, "==Polish==Then some words", 0, "Polish", 10 + 0);
   }

   @Test
   public void findsHeaderAtFrontOfFirstLine_LOngOffset() throws Throwable {
      verify_process(hs, "==Polish==Then some words", 133, "Polish", 10 + 133);
   }


   @Test
   public void findsHeaderAtFrontOfFirstLineWithFollowingNewline() throws Throwable {
      verify_process(hs, "==Polish==\nThen some words", 5, "Polish", 10 + 5);
   }

   @Test
   public void findsHeaderWithSpaceAtFrontOfFirstLine() throws Throwable {
      verify_process(hs, "==Lady is a Tramp==Then some words", 53, "Lady is a Tramp", 19 + 53);
   }

   //
   // Middle of first line
   //

   @Test
   public void findsHeaderAtMiddleOfFirstLine_ShortOffset() throws Throwable {
      verify_process(hs, "Over the River and Through the Woods==Polish==Then some words", 1, "Polish", 46 + 1);
   }

   @Test
   public void findsHeaderAtMiddleOfFirstLineZeroOffset() throws Throwable {
      verify_process(hs, "Some words==Polish==Then some words", 0, "Polish", 20 + 0);
   }

   @Test
   public void findsHeaderAtMiddleOfFirstLine_LongOffset() throws Throwable {
      verify_process(hs, "Goodie Wowzer!==Polish==Then some words", 133, "Polish", 24 + 133);
   }

   @Test
   public void findsHeaderAtMiddleOfFirstLineWithFollowingNewline() throws Throwable {
      verify_process(hs, "Georgia and Arizona==Polish==\nThen some words", 93, "Polish", 29 + 93);
   }

   @Test
   public void findsHeaderWithSpaceAtMiddleOfFirstLine() throws Throwable {
      verify_process(hs, "xxy==Lady is a Tramp==Then some words", 11, "Lady is a Tramp", 22 + 11);
   }

   @Test
   public void findsHeaderAtMiddleOfNewLineWithLeadingNewlines1() throws Throwable {
      verify_process(hs, "\n==Lady is a Tramp==Then some words", 4, "Lady is a Tramp", 20 + 4);
   }

   @Test
   public void findsHeaderAtMiddleOfNewLineWithLeadingNewlines3() throws Throwable {
      verify_process(hs, "\nA cool song==Lady is a Tramp==Then some words", 11, "Lady is a Tramp", 31 + 11);
   }

   @Test
   public void findsHeaderAtMiddleOfNewLineWithLeadingNewlines2() throws Throwable {
      verify_process(hs, "\nA\n\ncool  \nsong==Lady is a Tramp==Then some words", 154, "Lady is a Tramp", 34 + 154);
   }

   @Test
   public void findsHeaderAtMiddleOfNewLineWithLeadingNewlines4() throws Throwable {
      verify_process(hs, "\nBa ba black sheep\n==Lady is a Tramp==Then some words", 11, "Lady is a Tramp", 38 + 11);
   }

   //
   // Header near end of first line
   //
   @Test
   public void findsHeaderNearEndOfFirstLine() throws Throwable {
      verify_process(hs, "Sunshine on my shoulders==H at E==x", 14, "H at E", 34 + 14);
   }

   @Test
   public void findsHeaderNearEndOfFirstLineWithNewline() throws Throwable {
      verify_process(hs, "Sunshine on my shoulders==H at E==\n", 1, "H at E", 34 + 1);
   }

   @Test
   public void findsHeaderAlmostOnLineByItself() throws Throwable {
      verify_process(hs, "==abcdefg==x", 1, "abcdefg", 11 + 1);
   }

   @Test
   public void findsHeaderAlmostOnLineByItself2() throws Throwable {
      verify_process(hs, "==abcd efg==\n", 15, "abcd efg", 12 + 15);
   }


   //
   // Lack of header on first line recognized
   //

   @Test
   public void emptyLineNotAProblem() throws Throwable {
      verify_process(hs, "", 14);
   }

   @Test
   public void newlineOnlyNotAProblem() throws Throwable {
      verify_process(hs, "\n", 14);
   }


   @Test
   public void noHeaderOnFirstLine() throws Throwable {
      verify_process(hs, "Fourscore and seven years ago, our forefathers brought forth on this continent a new " +
            "nation", 33);
   }

   @Test
   public void wrongLevelHeaderOnFirstline_l1() throws Throwable {
      verify_process(hs, "Fourscore and seven =years ago= our fathers...", 33);
   }

   @Test
   public void wrongLevelHeaderOnFirstline_l3() throws Throwable {
      verify_process(hs, "Fourscore and seven ===years ago=== our fathers...", 33);
   }

   @Test
   public void newlineInterruptionRecognized() throws Throwable {
      verify_process(hs, "abc ==def\nghi== jlkmn", 1);
   }

   @Test
   public void unbalancedRecognized1() throws Throwable {
      verify_process(hs, "abc ==def\nghi=== jlkmn", 1);
   }

   @Test
   public void unbalancedRecognized2() throws Throwable {
      verify_process(hs, "abc ==def\nghi= jlkmn", 1);
   }

   @Test
   public void unbalancedRecognized3() throws Throwable {
      verify_process(hs, "abc ===def\nghi== jlkmn", 1);
   }

   @Test
   public void unbalancedRecognized4() throws Throwable {
      verify_process(hs, "abc =def\nghi== jlkmn", 1);
   }

   @Test
   public void unclosedLevel2() throws Throwable {
      verify_process(hs, "abc ==def and now we add content even though we forgot to close the header", 1);
   }


   //
   // Header outside visible range not seen
   //
   @Test
   public void headerBeforeStartNotSeen() throws Throwable {
      String s = "word==Polish== More words ==Spanish== Spanish stuff";
      HeaderSearch.Result result = hs.process(s.toCharArray(), 14, s.length() - 14);
      assertNotNull("result should not be null", result);
      assertEquals("Spanish", result.header);
      assertEquals(37, result.next);
   }

   @Test
   public void headerThatSpltsStartSeenProperly() throws Throwable {
      String s = "word==Polish== More words ==Spanish== Spanish stuff";
      HeaderSearch.Result result = hs.process(s.toCharArray(), 9, s.length() - 9);
      assertNotNull("result should not be null", result);
      assertEquals(" More words ", result.header);
      assertEquals(28, result.next);
   }

   @Test
   public void headerBeyondLengthNotSeen() throws Throwable {
      String s = "Words.  Words.  More Words. ==Spanish== Spanish stuff";
      HeaderSearch.Result result = hs.process(s.toCharArray(), 0, 28);
      assertNull("result should be null", result);
   }

   //
   // Handles aborted searches
   //

   @Test
   public void looksPastSingleEquals() throws Throwable {
      verify_process(hs, "Words = other words ==abc def==ghi ", 14, "abc def", 31 + 14);
   }

   @Test
   public void newlineTerminatesSearch() throws Throwable {
      verify_process(hs, "Words==abc\ndef==ghi jkl==mno", 4, "ghi jkl", 25 + 4);
   }

   @Test
   public void recoversFromUnbalanced_2_3() throws Throwable {
      verify_process(hs, "Words ==123===ghi jkl==mno== p", 144, "mno", 28 + 144);
   }


   @Test
   public void recoversFromUnclosed3_withNewline2() throws Throwable {
      verify_process(hs, "Words ===123 words for unclosed header\n==mno== content", 6, "mno", 46 + 6);
   }

   /////////////////////////////////////////////////////////////////////////////////////
   //
   // Second line tests
   //
   /////////////////////////////////////////////////////////////////////////////////////

   //
   // Header contained in second line
   //
   @Test
   public void headerFoundAtStartOfSecondLine() throws Throwable {
      verify_process(hs, "This is the first line with no header", 33);
      verify_process(hs, "==Found it== Now we can continue", 19, "Found it", 12 + 19);
   }

   @Test
   public void headerFoundInMiddleOfSecondLine() throws Throwable {
      verify_process(hs, "This is the first line with no header", 33);
      verify_process(hs, "and a leading part 2 ==Found it== Now we can continue", 0, "Found it", 33 + 0);
   }

   @Test
   public void headerFoundInMiddleOfSecondLineWithNewlines() throws Throwable {
      verify_process(hs, "This is the first line with no header", 33);
      verify_process(hs, "and a\n leading part\n 2\n==Found it== Now we can continue", 0, "Found it", 35 + 0);
   }

   @Test
   public void headerFoundNearEndOfSecondLine() throws Throwable {
      verify_process(hs, "This is the first line with no header", 33);
      verify_process(hs, "and a leading part 2 ==Found it== ", 5, "Found it", 33 + 5);
   }

   //
   //   Header crosses from first to second.
   //

   @Test
   public void headerAtVeryEndOfFirst() throws Throwable {
      verify_process(hs, "Fatalnie!==Polish==", 113);
      verify_process(hs, "Aargh!", 12, "Polish", 0 + 12);
   }

   @Test
   public void headerAtVeryEndOfFirst2() throws Throwable {
      verify_process(hs, "Fatalnie!==Polish==", 113);
      verify_process(hs, "\nAargh!", 12, "Polish", 0 + 12);
   }

   @Test
   public void headerAtVeryEndOfFirst3() throws Throwable {
      verify_process(hs, "Fatalnie!==Polish==", 113);
      verify_process(hs, " Aargh!", 3, "Polish", 0 + 3);
   }

   @Test
   public void headerIsEntireFirstLine() throws Throwable {
      verify_process(hs, "==Polish==", 113);
      verify_process(hs, " Aargh!", 3, "Polish", 0 + 3);
   }

   @Test
   public void headerIsEntireFirstLineWithSpace() throws Throwable {
      verify_process(hs, "==Pol ish==", 113);
      verify_process(hs, " Aargh!", 3, "Pol ish", 0 + 3);
   }

   @Test
   public void oneEqualOnFirstLine() throws Throwable {
      verify_process(hs, "Words\n=", 12);
      verify_process(hs, "=Polish== Stuff", 12, "Polish", 9 + 12);
   }

   @Test
   public void twoEqualOnFirstLine() throws Throwable {
      verify_process(hs, "Words\n==", 12);
      verify_process(hs, "Polish== Stuff", 12, "Polish", 8 + 12);
   }

   @Test
   public void oneLetterOnFirstLine() throws Throwable {
      verify_process(hs, "Words\n==P", 12);
      verify_process(hs, "olish== Stuff", 12, "Polish", 7 + 12);
   }

   @Test
   public void halfLettersOnFirstLine() throws Throwable {
      verify_process(hs, "Words\n==Poli", 12);
      verify_process(hs, "sh== Stuff", 12, "Polish", 4 + 12);
   }

   @Test
   public void mostLettersOnFirstLine() throws Throwable {
      verify_process(hs, "Words\n==Polis", 12);
      verify_process(hs, "h== Stuff", 12, "Polish", 3 + 12);
   }

   @Test
   public void allLettersOnFirstLine() throws Throwable {
      verify_process(hs, "Words\n==Polish", 12);
      verify_process(hs, "== Stuff", 12, "Polish", 2 + 12);
   }

   @Test
   public void oneCloseOnFirstLine() throws Throwable {
      verify_process(hs, "Words\n==Polish=", 12);
      verify_process(hs, "= Stuff", 123, "Polish", 1 + 123);
   }

   //
   // Aborted headers on first line recover on second
   //
   @Test
   public void unclosedLevelTwoBrokenByNewline() throws Throwable {
      verify_process(hs, "Words\n==Oops Forgot to close\n", 12);
      verify_process(hs, "==Header==\n", 12, "Header", 10 + 12);
   }

   @Test
   public void unclosedLevelThree() throws Throwable {
      verify_process(hs, "Words===Oops Forgot to close\n", 12);
      verify_process(hs, "==Header==\n", 12, "Header", 10 + 12);
   }

   @Test
   public void unclosedLevelThreeBrokenByNewline() throws Throwable {
      verify_process(hs, "Words\n===Oops Forgot to close\n", 12);
      verify_process(hs, "==Header==\n", 12, "Header", 10 + 12);
   }

   @Test
   public void unbalancedOnLine1() throws Throwable {
      verify_process(hs, "Words\n==Oops Closed Incorrectly=== more stuff", 12);
      verify_process(hs, "==Header==\n", 12, "Header", 10 + 12);

   }


   //
   // Characters at end of first line ignored when flowing to second
   //
   @Test
   public void misdirectedEndFlow() throws Throwable {
      String s = "Abcd efg hijk ==New Mexico";
      // Only consider up to "New"
      HeaderSearch.Result result = hs.process(s.toCharArray(), 0, 19);
      assertNull("should be null", result);

      verify_process(hs, "York== and keep going", 15, "NewYork", 15 + 6);
   }


   /////////////////////////////////////////////////////////////////////////////////////
   //
   // Multiple line tests
   //
   /////////////////////////////////////////////////////////////////////////////////////

   @Test
   public void headerInThirdLine() throws Throwable {
      verify_process(hs, "Line 1", 3);
      verify_process(hs, "Line 2", 14);
      verify_process(hs, "Line 3 contains ==The Header== and come content", 0, "The Header", 30);
   }

   @Test
   public void headerSplit() throws Throwable {
      verify_process(hs, "Nothing to see here", 34);
      verify_process(hs, "===Nor here===", 24);
      verify_process(hs, "Nope", 24);
      verify_process(hs, "But... ==T", 24);
      verify_process(hs, "his is", 24);
      verify_process(hs, " a ", 24);
      verify_process(hs, "Header==", 24);
      verify_process(hs, "With data", 1, "This is a Header", 1);
   }

   @Test
   public void oneLetterPerLine() throws Throwable {
      String h = "==Alaska==";
      for (int i = 0; i < h.length(); i++) {
         verify_process(hs, h.substring(i, i + 1), i);
      }
      verify_process(hs, ".", 3, "Alaska", 3);
   }

   @Test
   public void newlineInterruptedMultipleLine() throws Throwable {
      verify_process(hs, "Nothing to see here", 34);
      verify_process(hs, "===Nor here===", 24);
      verify_process(hs, "Nope", 24);
      verify_process(hs, "But... ==T", 24);
      verify_process(hs, "his is\nnot", 24);
      verify_process(hs, " a ", 24);
      verify_process(hs, "Header==", 24);
      verify_process(hs, "This is=", 1);
      verify_process(hs, "=.", 10, "This is", 11);
   }
}



