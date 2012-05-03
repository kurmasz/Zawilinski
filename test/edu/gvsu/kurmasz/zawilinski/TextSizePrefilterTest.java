package edu.gvsu.kurmasz.zawilinski;

// Created  2/8/12 at 1:14 PM
// (C) Zachary Kurmas 2012

import edu.gvsu.kurmasz.warszawa.log.Log;
import edu.gvsu.kurmasz.warszawa.log.SimpleLog;
import edu.gvsu.kurmasz.zawilinski.mw.current.MediaWikiType;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;

import javax.xml.bind.JAXBElement;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class TextSizePrefilterTest extends TextPrefilterTest {


   private static final int TEST_LIMIT = 100;

   public TextSizePrefilter makeSpy() {
      return spy(new TextSizePrefilter(TEST_LIMIT));
   }

   //
   // Constructor 1
   //
   @Test
   public void firstConstructorSetsMaximumTextSize() throws Throwable {
      TextSizePrefilter pf = new TextSizePrefilter(TEST_LIMIT, mock(SimpleLog.class));
      assertEquals(TEST_LIMIT, pf.getTextSizeLimit());
   }

   @Test
   public void firstConstructorSetsTheLog() throws Throwable {
      SimpleLog log = mock(SimpleLog.class);
      TextSizePrefilter pf = new TextSizePrefilter(TEST_LIMIT, log);
      assertEquals(log, pf.getLog());
   }

   @Test(expected = IllegalArgumentException.class)
   public void firstConstructorThrowsExceptionIfMaxTextSizeTooSmall() throws Throwable {
      new TextSizePrefilter(-2, mock(SimpleLog.class));
   }

   @Test
   public void firstConstructorSetsUNLIMITEDtoMAX_INT() throws Throwable {
      TextSizePrefilter pf = new TextSizePrefilter(TextSizePrefilter.UNLIMITED, mock(SimpleLog.class));
      assertEquals(Integer.MAX_VALUE, pf.getTextSizeLimit());
   }

   //
   // Constructor 2
   //
   @Test
   public void secondConstructorPassesParent() throws Throwable {
      TextSizePrefilter pf = new TextSizePrefilter(TEST_LIMIT);
      assertEquals(TEST_LIMIT, pf.getTextSizeLimit());
   }

   @Test
   public void secondConstructorSetsTheLogToNull() throws Throwable {
      TextSizePrefilter pf = new TextSizePrefilter(TEST_LIMIT);
      assertNull("log should be null", pf.getLog());
   }

   @Test(expected = IllegalArgumentException.class)
   public void secondConstructorThrowsExceptionIfMaxTextSizeTooSmall() throws Throwable {
      new TextSizePrefilter(-2);
   }

   @Test
   public void secondConstructorSetsUNLIMITEDtoMAX_INT() throws Throwable {
      TextSizePrefilter pf = new TextSizePrefilter(TextSizePrefilter.UNLIMITED);
      assertEquals(Integer.MAX_VALUE, pf.getTextSizeLimit());
   }

   //
   // handleStartTextElement
   //
   @Test
   public void handleStartTextElementSetsCurrentSizeTo0() throws Throwable {
      TextSizePrefilter pf = new TextSizePrefilter(TextSizePrefilter.UNLIMITED);

      startText(pf);
      assertEquals(0, pf.getCurrentTextSize());

      String myText = "abcdefg";
      char[] myChars = myText.toCharArray();
      pf.characters(myChars, 0, myText.length());
      // make sure the test is set up correctly
      assertEquals(myText.length(), pf.getCurrentTextSize());

      // the effect of endText on current text size is unspecified; however,
      // we want ot make sure that there is some useful work for the start tag to handle
      endText(pf);
      assertEquals(myText.length(), pf.getCurrentTextSize());

      // Now that we have a non-zero currentText value, make sure it gets re-set
      startText(pf);
      assertEquals(0, pf.getCurrentTextSize());
   }

   //
   // handleTextElementCharacters
   //
   @Test
   public void handleCharactersSendsCharactersIfUnderLimit() throws Throwable {
      TextSizePrefilter pf = spy(new TextSizePrefilter(7));
      startPage(pf);
      startText(pf);

      String myText = "abcdefg";
      char[] myChars = myText.toCharArray();
      pf.characters(myChars, 0, myText.length());
      verify(pf).sendCharacters(myChars, 0, myText.length());
   }

   @Test
   public void handleCharactersRecognizesWhenStillUnder() throws Throwable {
      TextSizePrefilter pf = spy(new TextSizePrefilter(10));
      startPage(pf);
      startText(pf);

      String myText = "123";
      char[] myChars = myText.toCharArray();
      pf.characters(myChars, 0, myText.length());
      verify(pf).sendCharacters(myChars, 0, myText.length());

      String myText2 = "4567";
      char[] myChars2 = myText2.toCharArray();
      pf.characters(myChars2, 0, myText2.length());
      verify(pf).sendCharacters(myChars2, 0, myText2.length());

      String myText3 = "890";
      char[] myChars3 = myText3.toCharArray();
      pf.characters(myChars3, 0, myText3.length());
      verify(pf).sendCharacters(myChars3, 0, myText3.length());
   }

   @Test
   public void handleCharactersSendsPartialWhenGoingOver() throws Throwable {
      TextSizePrefilter pf = spy(new TextSizePrefilter(6));
      startPage(pf);
      startText(pf);

      String myText = "1234567";
      char[] myChars = myText.toCharArray();
      pf.characters(myChars, 0, myText.length());
      verify(pf).sendCharacters(myChars, 0, myText.length() - 1);
   }

   @Test
   public void handleCharacterDetectsOverAfterSeveralCalls() throws Throwable {
      TextSizePrefilter pf = spy(new TextSizePrefilter(9));
      startPage(pf);
      startText(pf);

      String myText = "123";
      char[] myChars = myText.toCharArray();
      pf.characters(myChars, 0, myText.length());
      verify(pf).sendCharacters(myChars, 0, myText.length());

      String myText2 = "4567";
      char[] myChars2 = myText2.toCharArray();
      pf.characters(myChars2, 0, myText2.length());
      verify(pf).sendCharacters(myChars2, 0, myText2.length());

      String myText3 = "890";
      char[] myChars3 = myText3.toCharArray();
      pf.characters(myChars3, 0, myText3.length());
      verify(pf).sendCharacters(myChars3, 0, myText3.length() - 1);
   }

   @Test
   public void handleCharacterAccountsForStartAndLength() throws Throwable {
      TextSizePrefilter pf = spy(new TextSizePrefilter(9));
      startPage(pf);
      startText(pf);

      String myText = "xx123xx";
      char[] myChars = myText.toCharArray();
      pf.characters(myChars, 2, 3);
      verify(pf).sendCharacters(myChars, 2, 3);

      String myText2 = "xxx4567x";
      char[] myChars2 = myText2.toCharArray();
      pf.characters(myChars2, 3, 4);
      verify(pf).sendCharacters(myChars2, 3, 4);

      String myText3 = "x890xxxxxxxx";
      char[] myChars3 = myText3.toCharArray();
      pf.characters(myChars3, 1, 3);
      verify(pf).sendCharacters(myChars3, 1, 2);
   }

   @Test
   public void handleCharacterDoesNotSendWhenOverLimit() throws Throwable {
      TextSizePrefilter pf = spy(new TextSizePrefilter(10));
      startPage(pf);
      startText(pf);

      String myText = "123";
      char[] myChars = myText.toCharArray();
      pf.characters(myChars, 0, myText.length());
      verify(pf).sendCharacters(myChars, 0, myText.length());

      String myText2 = "4567";
      char[] myChars2 = myText2.toCharArray();
      pf.characters(myChars2, 0, myText2.length());
      verify(pf).sendCharacters(myChars2, 0, myText2.length());

      String myText3 = "890";
      char[] myChars3 = myText3.toCharArray();
      pf.characters(myChars3, 0, myText3.length());
      verify(pf).sendCharacters(myChars3, 0, myText3.length());

      String myText4 = "xxy";
      char[] myChars4 = myText4.toCharArray();
      pf.characters(myChars4, 0, myText4.length());
      verify(pf, times(3)).sendCharacters(Matchers.<char[]>any(), anyInt(), anyInt());

   }

   // verify truncations written to log.

   //
   // endText
   //
   @Test
   public void logEntryGeneratedWhenOverflow() throws Throwable {

      SimpleLog log = mock(SimpleLog.class);
      TextSizePrefilter pf = new TextSizePrefilter(9, log);
      startPage(pf);
      startTitle(pf);
      pf.characters("Mark Twain".toCharArray(), 0, 10);
      endTitle(pf);
      startText(pf);

      String myText = "123";
      char[] myChars = myText.toCharArray();
      pf.characters(myChars, 0, myText.length());

      String myText2 = "4567";
      char[] myChars2 = myText2.toCharArray();
      pf.characters(myChars2, 0, myText2.length());

      String myText3 = "89ABCDEF";
      char[] myChars3 = myText3.toCharArray();
      pf.characters(myChars3, 0, myText3.length());
      endText(pf);

      verify(log).println(Zawilinski.TRUNCATIONS, "15 9 Mark Twain");
   }

   @Test
   public void noLogEntryGeneratedWhenNoOverflow() throws Throwable {

      SimpleLog log = mock(SimpleLog.class);
      TextSizePrefilter pf = new TextSizePrefilter(16, log);
      startPage(pf);
      startTitle(pf);
      pf.characters("Mark Twain".toCharArray(), 0, 10);
      endTitle(pf);
      startText(pf);

      String myText = "123";
      char[] myChars = myText.toCharArray();
      pf.characters(myChars, 0, myText.length());

      String myText2 = "4567";
      char[] myChars2 = myText2.toCharArray();
      pf.characters(myChars2, 0, myText2.length());

      String myText3 = "89ABCDEF";
      char[] myChars3 = myText3.toCharArray();
      pf.characters(myChars3, 0, myText3.length());
      endText(pf);

      verify(log, never()).println(anyInt(), anyString());
   }

   //
   //end-to-end
   //
   @Test
   public void filterDoesNothingifNoDataTooLarge() throws Throwable {
      InputStream input = this.getClass().getResourceAsStream("/mw_sample_0.5.xml");
      Assert.assertNotNull("input", input);
      Log log = new Log();

      // 41 is the length of the longest article text string currently in the sample data
      TextSizePrefilter filter = new TextSizePrefilter(41, log);
      JAXBElement<MediaWikiType> observed = PreFilteredMediaWikiLoader.load(input, log, filter);
      MediaWikiType root = observed.getValue();
      SampleContentCheck.verifyMWSampleContent(root);
   }

   @Test
   public void filterActuallyFilters() throws Throwable {
      InputStream input = this.getClass().getResourceAsStream("/mw_sample_0.5.xml");
      Assert.assertNotNull("input", input);
      Log log = new Log();

      // 41 is the length of the longest article text string currently in the sample data
      TextSizePrefilter filter = new TextSizePrefilter(19, log);
      JAXBElement<MediaWikiType> observed = PreFilteredMediaWikiLoader.load(input, log, filter);
      MediaWikiType root = observed.getValue();

      String[][] expected = {
            {"Content for rev 1", "Content for rev 2 ("},
            {"Content for rev 1 o", "Content for rev 2 o",
                  "Content for rev 3 o"}
      };
      SampleContentCheck.verifyMWSampleContentText(root, expected);
      SampleContentCheck.verifyMWSampleContentOther(root);
   }

   @Test
   public void filterWritesToLog() throws Throwable {
      InputStream input = this.getClass().getResourceAsStream("/mw_sample_0.5.xml");
      Assert.assertNotNull("input", input);
      Log log = new Log();
      SimpleLog mlog = mock(SimpleLog.class);
      // 41 is the length of the longest article text string currently in the sample data
      TextSizePrefilter filter = new TextSizePrefilter(19, mlog);
      PreFilteredMediaWikiLoader.load(input, log, filter);

      verify(mlog).println(Zawilinski.TRUNCATIONS, "27 19 Sample page 1");
      verify(mlog).println(Zawilinski.TRUNCATIONS, "41 19 Sample page 2");
      verify(mlog).println(Zawilinski.TRUNCATIONS, "27 19 Sample page 2");
      verify(mlog).println(Zawilinski.TRUNCATIONS, "37 19 Sample page 2");
      verifyNoMoreInteractions(mlog);
   }

   @Test
   public void filterAllowsNullLog() throws Throwable {
      InputStream input = this.getClass().getResourceAsStream("/mw_sample_0.5.xml");
      Assert.assertNotNull("input", input);
      // 41 is the length of the longest article text string currently in the sample data
      TextSizePrefilter filter = new TextSizePrefilter(19, null);
      PreFilteredMediaWikiLoader.load(input, mock(Log.class), filter);
   }
}
