package edu.gvsu.kurmasz.zawilinski;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;


// Created  3/15/12 at 1:14 PM
// (C) Zachary Kurmas 2012

public class HeaderSearchTest {

    //////////////////////////////////////////////////////////////////////////////
    //
    // Tests that explain subtleties of the algorithm
    //
    //////////////////////////////////////////////////////////////////////////////

    // Newlines close a header, so ==correct" is correctly seen as the beginning of a header.
    @Test
    public void newlineClosesHeader() throws Throwable {
        verify_process(hs, "==Unclosed header\n content ==correct header== content2", "correct header", 45);
    }

    // Headers are assumed to have an open and a close.  Thus, an unclosed header
    // without an intervening newline can generate counter-intuitive results.
    // Fortunately, most headers are on lines to themselves, so this situation should be rare.
    @Test
    public void unclosedHeaderCanHideHeaderOnSameLine() throws Throwable {
        verify_process(hs, "Words ===Intended L3 header Intended content ==Intended L2 header== Intended L2 content\n " +
                "Content line 2\n==Next L2 Header== Content", "Next L2 Header", 122);
    }

    @Test
    public void unclosedHeaderCanHideHeaderOnSameLine2() throws Throwable {
        verify_process(hs, "Words ===Intended L3 header Intended content ==Intended L2 header== Intended L2 content " +
                "Content line 2==Next L2 Header== Content", " Intended L2 content Content line 2", 104);
    }

    // Headers, even headers of different levels are treated as a unit --- even if unbalanced.
    // Thus, the algorithm doesn't begin looking for a new header until after "===ub header==" is closed.
    // This is why the correct header to be found is "true header"
    @Test
    public void unbalancedHeadersTreatedAsUnit() throws Throwable {
        verify_process(hs, "Words ===ub header== content of ub header ==true header==Content 2", "true header", 57);
    }


    // Because unbalanced headers are treated as a unit, a string of several '=' inside a header can cause
    // counter-intuitive results.
    @Test
    public void headersTreatedAsAUnit2() throws Throwable {
        verify_process(hs, "==Illegal<===>header== Intended content ==Intended l2 header== Intended content",
                " Intended content ", 42);
    }

    // A header won't be processed until *after* it sees a non-header character.
    // In This example, the header isn't recognized as closed at the end of the first
    // batch of characters, because the algorithm is waiting to make sure the
    // first character of the next batch isn't a "=".
    @Test
    public void requireNonHeaderChar() throws Throwable {
        verify_process(hs, "==Not Recognized Yet==");
        verify_process(hs, "Now it will be", "Not Recognized Yet", 0);
    }

    //////////////////////////////////////////////////////////////////////////////
    //
    // main test code
    //
    //////////////////////////////////////////////////////////////////////////////

    @Test
    public void resultContainsCorrectFullHeader() throws Throwable {
        HeaderSearch.Result result = new HeaderSearch.Result("TheString", 15);
        assertEquals("==TheString==", result.fullHeader());
    }

    private static final String CHALLENGE_HEADER_CONTENT = "This header has it all:  Punctuation, spaces, letters, " +
            "and numbers: [1 2] 3 = 4 | 4 = 5, 7. <8(9)10= 11= {12} Wow!:;\"?\" '/' -__.Whew!";
    private static final String CHALLENGE_HEADER = "==" + CHALLENGE_HEADER_CONTENT + "==";

    private HeaderSearch hs;

    @Before
    public void init() {
        hs = new HeaderSearch();
    }

    // This "_impl" version of verify_process allows to easily call this method from any child classes
    protected void verify_process_impl(HeaderSearch headerSearch, String s, String expectedHeader,
                                       int expectedPlace) {
        HeaderSearch.Result result = headerSearch.process(s.toCharArray(), 0, s.length());
        if (expectedHeader == null) {
            assertNull("expecting search to return null for \"" + s + "\".", result);
        } else {
            assertNotNull("process should have returned a non-null result for \"" + s + "\".", result);
            assertEquals(expectedHeader, result.headerContent);
            assertEquals(expectedPlace, result.next);
        }
    }

    protected void verify_process(HeaderSearch headerSearch, String s, String expectedHeader,
                                  int expectedPlace) {
        verify_process_impl(headerSearch, s, expectedHeader, expectedPlace);
    }

    protected void verify_process(HeaderSearch headerSearch, String s) {
        verify_process_impl(headerSearch, s, null, 0);
    }

    protected void verify_process_chunks(HeaderSearch headerSearch, String s, String expectedHeader,
                                         int expectedPlace, int[] chunks) {

        int chunkSum = 0;
        int index = 0;
        while (chunkSum < s.length()) {
            int chunkSize = chunks[index];
            index++;
            index %= chunks.length;
            if (chunkSum + chunkSize > s.length()) {
                chunkSize = s.length() - chunkSum;
            }

            String piece = s.substring(chunkSum, chunkSum + chunkSize);
            // System.out.println("Piece: " + piece);
            if (expectedHeader != null && chunkSum <= expectedPlace && expectedPlace < chunkSum + chunkSize) {
                verify_process_impl(headerSearch, piece, expectedHeader, expectedPlace - chunkSum);
                // Once we find a result, the headerSearch must be re-set
                break;
            } else {
                verify_process_impl(headerSearch, piece, null, 0);
            }
            chunkSum += chunkSize;
        }
    }


    // Key features
    // * Header can be any characters except for '=' new line
    // * Header can contain *single* '='
    // * Newlines terminate a header
    // * header must begin with exactly two ==
    // * Header may be split among several lines
    // * Headers are assumed to be closed
    // * Headers can be "unbalanced"  (i.e., == can close ===)

    // Cases to verify:
    // * Only return matches for level 2 headers
    // * single '=' don't mess things up
    // * unclosed headers don't cause problems
    // * unbalanced headers don't cause problems.
    // * line breaks don't cause problems

    /////////////////////////////////////////////////////////////////////////////////////
    //
    // First line tests
    //
    /////////////////////////////////////////////////////////////////////////////////////

    //
    // Header found at front of first line
    //

    @Test
    public void findsHeaderAtFrontOfFirstLine() throws Throwable {
        verify_process(hs, "==Polish==Then some words", "Polish", 10);
    }

    @Test
    public void findsHeaderAtFrontOfFirstLineWithFollowingNewline() throws Throwable {
        verify_process(hs, "==Polish==\nThen some words", "Polish", 10);
    }

    @Test
    public void findsHeaderAtFrontOfFirstLine2() throws Throwable {
        verify_process(hs, CHALLENGE_HEADER + "Then some words", CHALLENGE_HEADER_CONTENT, CHALLENGE_HEADER.length());
    }

    //
    // Middle of first line
    //


    @Test
    public void findsHeaderAtMiddleOfFirstLine() throws Throwable {
        verify_process(hs, "Over the River and Through the Woods==Polish==Then some words", "Polish", 46);
    }

    @Test
    public void findsHeaderAtMiddleOfFirstLineWithFollowingNewline() throws Throwable {
        verify_process(hs, "Georgia and Arizona==Polish==\nThen some words", "Polish", 29);
    }

    @Test
    public void findsHeaderAtMiddleOfFirstLine2() throws Throwable {
        verify_process(hs, "start of line " + CHALLENGE_HEADER + "After header.",
                CHALLENGE_HEADER_CONTENT, 14 + CHALLENGE_HEADER.length());
    }

    @Test
    public void findsHeaderAtMiddleOfNewLineWithLeadingNewlines1() throws Throwable {
        verify_process(hs, "\n==Lady is a Tramp==Then some words", "Lady is a Tramp", 20);
    }

    @Test
    public void findsHeaderAtMiddleOfNewLineWithLeadingNewlines3() throws Throwable {
        verify_process(hs, "\nA cool song==Lady is a Tramp==Then some words", "Lady is a Tramp", 31);
    }

    @Test
    public void findsHeaderAtMiddleOfNewLineWithLeadingNewlines2() throws Throwable {
        verify_process(hs, "\nA\n\ncool  \nsong==Lady is a Tramp==Then some words", "Lady is a Tramp", 34);
    }

    @Test
    public void findsHeaderAtMiddleOfNewLineWithLeadingNewlines4() throws Throwable {
        verify_process(hs, "\nBa ba black sheep\n==Lady is a Tramp==Then some words", "Lady is a Tramp", 38);
    }

    //
    // Header near end of first line
    //
    @Test
    public void findsHeaderNearEndOfFirstLine() throws Throwable {
        verify_process(hs, "Sunshine on my shoulders==H at E==x", "H at E", 34);
    }

    @Test
    public void findsHeaderNearEndOfFirstLineWithNewline() throws Throwable {
        verify_process(hs, "Sunshine on my shoulders==H at E==\n", "H at E", 34);
    }

    @Test
    public void findsHeaderAlmostOnLineByItself() throws Throwable {
        verify_process(hs, "==abcdefg==x", "abcdefg", 11);
    }

    @Test
    public void findsHeaderAlmostOnLineByItself2() throws Throwable {
        verify_process(hs, "==abcd efg==\n", "abcd efg", 12);
    }

    @Test
    public void findsHeaderAlmostOnLineByItself3() throws Throwable {
        verify_process(hs, CHALLENGE_HEADER + "\n", CHALLENGE_HEADER_CONTENT, CHALLENGE_HEADER.length());
    }

    @Test
    public void findsFirstHeader() throws Throwable {
        verify_process(hs, "abc def ==ghi== jlk mno ==pqr== stu ==vwyxz== 12345", "ghi", 15);
    }

    //
    // Lack of header on first line recognized
    //

    @Test
    public void emptyLineNotAProblem() throws Throwable {
        verify_process(hs, "");
    }

    @Test
    public void newlineOnlyNotAProblem() throws Throwable {
        verify_process(hs, "\n");
    }

    @Test
    public void noHeaderOnFirstLine() throws Throwable {
        verify_process(hs, "Fourscore and seven years ago, our forefathers brought forth on this continent a new " +
                "nation");
    }

    @Test
    public void wrongLevelHeaderOnFirstline_l1() throws Throwable {
        verify_process(hs, "Fourscore and seven =years ago= our fathers...");
    }

    @Test
    public void wrongLevelHeaderOnFirstline_l3() throws Throwable {
        verify_process(hs, "Fourscore and seven ===years ago=== our fathers...");
    }

    @Test
    public void unclosedLevel2() throws Throwable {
        verify_process(hs, "abc ==def and now we add content even though we forgot to close the header");
    }

    @Test
    public void unclosedLevel3() throws Throwable {
        verify_process(hs, "abc ===def and now we add content even though we forgot to close the header");
    }

    @Test
    public void newlineInterruptionRecognized_notFound() throws Throwable {
        verify_process(hs, "abc ==def\nghi== jlkmn");
    }

    @Test
    public void unbalancedRecognized_2_3_notFound() throws Throwable {
        verify_process(hs, "abc ==defnghi=== jlkmn");
    }

    @Test
    public void unbalancedRecognized_3_2_notFound() throws Throwable {
        verify_process(hs, "abc ===defnghi== jlkmn");
    }


    //
    // Header found beyond non-headers *i.e., things that look like headers, but aren't).
    //

    @Test
    public void recoverFromNewlineInterruptionL2() throws Throwable {
        verify_process(hs, "abc ==def\nghi==jlkmn==op", "jlkmn", 22);
    }

    @Test
    public void recoverFromNewlineInterruptionL3() throws Throwable {
        verify_process(hs, "Words ===123 words for unclosed header\n==mno== content", "mno", 46);
    }

    @Test
    public void recoverFromUnbalanced_2_3() throws Throwable {
        verify_process(hs, "abc ==defnghi=== jlkmn ==then found== more stuff", "then found", 37);
    }

    @Test
    public void recoverFromUnbalanced_3_2() throws Throwable {
        // Note:  Lower level headings must be closed before considering the next heading.
        // The ===defnghi== is treaded as a completed heading.    This is why the correct
        // answer is ==then found== not "== jlkmn =="
        verify_process(hs, "abc ===defnghi== jlkmn ==then found== more stuff", "then found", 37);
    }

    @Test
    public void recoverFromLevel1() throws Throwable {
        verify_process(hs, "a =bcd= efg ==hijk== lmnop", "hijk", 20);
    }

    @Test
    public void recoverFromLevel3() throws Throwable {
        verify_process(hs, "a ===bcd=== efg ==hijk== lmnop", "hijk", 24);
    }

    @Test
    public void level1DoesntCountForOpen() throws Throwable {
        verify_process(hs, "a b = cde ==fghi== jkl", "fghi", 18);
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
        verify_process(hs, "This is the first line with no header");
        verify_process(hs, "==Found it== Now we can continue", "Found it", 12);
    }

    @Test
    public void headerFoundInMiddleOfSecondLine() throws Throwable {
        verify_process(hs, "This is the first line with no header");
        verify_process(hs, "and a leading part 2 ==Found it== Now we can continue", "Found it", 33);
    }

    @Test
    public void headerFoundInMiddleOfSecondLineWithNewlines() throws Throwable {
        verify_process(hs, "This is the first line with no header");
        verify_process(hs, "and a\n leading part\n 2\n==Found it== Now we can continue", "Found it", 35);
    }

    @Test
    public void challengeHeaderFoundInMiddleOfSecondLineWithNewlines() throws Throwable {
        verify_process(hs, "This is the first line with no header");
        verify_process(hs, "and a\n leading part\n 2\n" + CHALLENGE_HEADER + "Now we can continue", CHALLENGE_HEADER_CONTENT,
                23 + CHALLENGE_HEADER.length());
    }

    @Test
    public void headerFoundNearEndOfSecondLine() throws Throwable {
        verify_process(hs, "This is the first line with no header");
        verify_process(hs, "and a leading part 2 ==Found it== ", "Found it", 33);
    }

    //
    //   Header crosses from first to second.
    //

    @Test
    public void headerAtVeryEndOfFirst() throws Throwable {
        verify_process(hs, "Fatalnie!==Polish==");
        verify_process(hs, "Aargh!", "Polish", 0);
    }

    @Test
    public void headerAtVeryEndOfFirst2() throws Throwable {
        verify_process(hs, "Fatalnie!==Polish==");
        verify_process(hs, "\nAargh!", "Polish", 0);
    }

    @Test
    public void headerAtVeryEndOfFirst3() throws Throwable {
        verify_process(hs, "Fatalnie!==Polish==");
        verify_process(hs, " Aargh!", "Polish", 0);
    }

    @Test
    public void headerIsEntireFirstLine() throws Throwable {
        verify_process(hs, "==Polish==");
        verify_process(hs, " Aargh!", "Polish", 0);
    }

    @Test
    public void headerIsEntireFirstLineWithSpace() throws Throwable {
        verify_process(hs, CHALLENGE_HEADER);
        verify_process(hs, " Aargh!", CHALLENGE_HEADER_CONTENT, 0);
    }

    @Test
    public void oneEqualOnFirstLine() throws Throwable {
        verify_process(hs, "Words\n=");
        verify_process(hs, "=Polish== Stuff", "Polish", 9);
    }

    @Test
    public void twoEqualOnFirstLine() throws Throwable {
        verify_process(hs, "Words\n==");
        verify_process(hs, "Polish== Stuff", "Polish", 8);
    }

    @Test
    public void oneLetterOnFirstLine() throws Throwable {
        verify_process(hs, "Words\n==P");
        verify_process(hs, "olish== Stuff", "Polish", 7);
    }

    @Test
    public void halfLettersOnFirstLine() throws Throwable {
        verify_process(hs, "Words\n==Poli");
        verify_process(hs, "sh== Stuff", "Polish", 4);
    }

    @Test
    public void mostLettersOnFirstLine() throws Throwable {
        verify_process(hs, "Words\n==Polis");
        verify_process(hs, "h== Stuff", "Polish", 3);
    }

    @Test
    public void allLettersOnFirstLine() throws Throwable {
        verify_process(hs, "Words\n==Polish");
        verify_process(hs, "== Stuff", "Polish", 2);
    }

    @Test
    public void oneCloseOnFirstLine() throws Throwable {
        verify_process(hs, "Words\n==Polish=");
        verify_process(hs, "= Stuff", "Polish", 1);
    }

    //
    // Aborted headers on first line recover on second
    //

    @Test
    public void unclosedLevelOne() throws Throwable {
        verify_process(hs, "Words word = more words");
        verify_process(hs, "==Header==\n", "Header", 10);
    }

    @Test
    public void unclosedLevelTwoBrokenByNewline() throws Throwable {
        verify_process(hs, "Words\n==Oops Forgot to close\n");
        verify_process(hs, "==Header==\n", "Header", 10);
    }

    @Test
    public void unclosedLevelTwoBrokenByNewline2() throws Throwable {
        verify_process(hs, "Words\n==Oops Forgot to close");
        verify_process(hs, "\n==Header==\n", "Header", 11);
    }

    @Test
    public void unclosedLevelThreeBrokenByNewline() throws Throwable {
        verify_process(hs, "Words===Oops Forgot to close\n");
        verify_process(hs, "==Header==\n", "Header", 10);
    }

    @Test
    public void unclosedLevelThreeBrokenByNewline2() throws Throwable {
        verify_process(hs, "Words===Oops Forgot to close");
        verify_process(hs, "\n==Header==\n", "Header", 11);
    }


    @Test
    public void unbalancedOnLine1() throws Throwable {
        verify_process(hs, "Words\n==Oops Closed Incorrectly=== more stuff");
        verify_process(hs, "==Header==\n", "Header", 10);
    }

    @Test
    public void unbalancedOnLine1and2() throws Throwable {
        verify_process(hs, "Words\n==Oops Closed ");
        verify_process(hs, "Incorrectly=== more stuff==Header==\n", "Header", 35);
    }


    /////////////////////////////////////////////////////////////////////////////////////
    //
    // Multiple line tests
    //
    /////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void headerInThirdLine() throws Throwable {
        verify_process(hs, "Line 1");
        verify_process(hs, "Line 2");
        verify_process(hs, "Line 3 contains ==The Header== and some content", "The Header", 30);
    }

    @Test
    public void headerSplit() throws Throwable {
        verify_process(hs, "Nothing to see here");
        verify_process(hs, "===Nor here===");
        verify_process(hs, "Nope");
        verify_process(hs, "But... ==T");
        verify_process(hs, "his is");
        verify_process(hs, " a ");
        verify_process(hs, "Header==");
        verify_process(hs, "With data", "This is a Header", 0);
    }

    @Test
    public void oneLetterPerLine() throws Throwable {
        String h = "==Alaska==";
        for (int i = 0; i < h.length(); i++) {
            verify_process(hs, h.substring(i, i + 1));
        }
        verify_process(hs, ".", "Alaska", 0);
    }

    @Test
    public void newlineInterruptedMultipleLine() throws Throwable {
        verify_process(hs, "Nothing to see here");
        verify_process(hs, "===Nor here===");
        verify_process(hs, "Nope");
        verify_process(hs, "But... ==T");
        verify_process(hs, "his is\nnot");
        verify_process(hs, " a ");
        verify_process(hs, "Header==");
        verify_process(hs, "This is=");
        verify_process(hs, "=.", "This is", 1);
    }

    /////////////////////////////////////////////////////////////////////////////////////
    //
    // data outside start, length not considered
    //
    /////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void headerBeforeStartNotSeen() throws Throwable {
        String s = "word==Polish== More words ==Spanish== Spanish stuff";
        HeaderSearch.Result result = hs.process(s.toCharArray(), 14, s.length() - 14);
        assertNotNull("result should not be null", result);
        assertEquals("Spanish", result.headerContent);
        assertEquals(37, result.next);
    }

    @Test
    public void headerBeforeStartNotSeen2() throws Throwable {
        // Notice that the value of start is right before the end of the ==Polish== tag
        String s = "word==Polish== More words ==Spanish== Spanish stuff";
        HeaderSearch.Result result = hs.process(s.toCharArray(), 12, s.length() - 12);
        assertNotNull("result should not be null", result);
        assertEquals(" More words ", result.headerContent);
        assertEquals(28, result.next);
    }


    @Test
    public void headerThatSpltsStartSeenProperly() throws Throwable {
        String s = "word==Polish== More words ==Spanish== Spanish stuff";
        HeaderSearch.Result result = hs.process(s.toCharArray(), 9, s.length() - 9);
        assertNotNull("result should not be null", result);
        assertEquals(" More words ", result.headerContent);
        assertEquals(28, result.next);
    }

    @Test
    public void headerBeyondLengthNotSeen() throws Throwable {
        String s = "Words.  Words.  More Words. ==Spanish== Spanish stuff";
        HeaderSearch.Result result = hs.process(s.toCharArray(), 0, 28);
        assertNull("result should be null", result);
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

        verify_process(hs, "York== and keep going", "NewYork", 6);
    }


    //
    // getCurrentHeader
    //
    @Test
    public void getCurrentHeader_returnsNullIfStagePRE() throws Throwable {
        String s = "No headers here";
        hs.process(s.toCharArray(), 0, s.length());
        HeaderSearch.Result result = hs.getCurrentContent();
        assertNull("should be null", result);
    }

    @Test
    public void getCurrentHeader_returnsNullIfStageOpen() throws Throwable {
        String s = "Beginning of header =";
        hs.process(s.toCharArray(), 0, s.length());
        HeaderSearch.Result result = hs.getCurrentContent();
        assertNull("should be null", result);
    }

    @Test
    public void getCurrentHeader_returnsNullIfStageOpen2() throws Throwable {
        String s = "Beginning of header ==";
        hs.process(s.toCharArray(), 0, s.length());
        HeaderSearch.Result result = hs.getCurrentContent();
        assertNull("should be null", result);
    }

    @Test
    public void getCurrentHeader_returnsNullIfStageIN() throws Throwable {
        String s = "Beginning of header ==Ta Da";
        hs.process(s.toCharArray(), 0, s.length());
        HeaderSearch.Result result = hs.getCurrentContent();
        assertNull("should be null", result);
    }

    @Test
    public void getCurrentHeader_returnsNullIfStageIN2() throws Throwable {
        String s = "Beginning of header ==Ta Da=de da";
        hs.process(s.toCharArray(), 0, s.length());
        HeaderSearch.Result result = hs.getCurrentContent();
        assertNull("should be null", result);
    }

    @Test
    public void getCurrentHeader_returnsNullIfStageCloseWithLength1() throws Throwable {
        String s = "Beginning of header ==Ta Da=";
        hs.process(s.toCharArray(), 0, s.length());
        HeaderSearch.Result result = hs.getCurrentContent();
        assertNull("should be null", result);
    }

    @Test
    public void getCurrentHeader_returnsNullIfStageCloseWithLength3() throws Throwable {
        String s = "Beginning of header ==Ta Da===";
        hs.process(s.toCharArray(), 0, s.length());
        HeaderSearch.Result result = hs.getCurrentContent();
        assertNull("should be null", result);
    }

    @Test
    public void getCurrentHeader_returnsNullIfStageCloseWithLength3b() throws Throwable {
        String s = "Beginning of header ===Ta Da===";
        hs.process(s.toCharArray(), 0, s.length());
        HeaderSearch.Result result = hs.getCurrentContent();
        assertNull("should be null", result);
    }

    @Test
    public void getCurrentHeader_returnsNullIfStagePost() throws Throwable {
        String s = "Beginning of header ==Ta Da== and done";
        hs.process(s.toCharArray(), 0, s.length());
        HeaderSearch.Result result = hs.getCurrentContent();
        assertNull("should be null", result);
    }

    @Test
    public void getCurrentHeader_ReturnsHeaderIfVeryEndOfValidHeader() throws Throwable {
        String s = "Beginning of header ==A good=header==";
        hs.process(s.toCharArray(), 0, s.length());
        HeaderSearch.Result result = hs.getCurrentContent();
        assertNotNull("Should not be null", result);
        assertEquals("header content", "A good=header", result.headerContent);
        assertEquals("full header", "==A good=header==", result.fullHeader());
        assertEquals("Next", -1, result.next);
    }

}



