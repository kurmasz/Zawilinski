package edu.gvsu.kurmasz.zawilinski;

import edu.gvsu.kurmasz.warszawa.log.Log;
import edu.gvsu.kurmasz.zawilinski.mw.current.MediaWikiType;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;

/**
 * @author Zachary Kurmas
 */
// Created  1/24/12 at 12:49 PM
// (C) Zachary Kurmas 2012

public class PreFilteredMediaWikiLoader_noMock_Test {

   @Test
   public void loadWithNoFiltersLoadsDataAsExpected() throws Throwable {
      InputStream input = this.getClass().getResourceAsStream("/mw_sample_current.xml");
      Assert.assertNotNull("input", input);
      InputSource inputSource = new InputSource(input);
      Unmarshaller unmarshaller = MediaWikiLoader.createUnmarshaller();
      Log log = new Log();

      JAXBElement<MediaWikiType> observed = PreFilteredMediaWikiLoader.load(inputSource, log, unmarshaller);
      MediaWikiType root = observed.getValue();
      SampleContentCheck.verifyMWSampleContent(root);
   }

   @Test
   public void loadWithOneFilter() throws Throwable {

      InputStream input = this.getClass().getResourceAsStream("/mw_sample_current.xml");
      Assert.assertNotNull("input", input);
      InputSource inputSource = new InputSource(input);
      Unmarshaller unmarshaller = MediaWikiLoader.createUnmarshaller();
      Log log = new Log();

      XMLFilter filter = new CharSubPrefilter('e', '8');

      JAXBElement<MediaWikiType> observed = PreFilteredMediaWikiLoader.load(inputSource, log, unmarshaller, filter);
      MediaWikiType root = observed.getValue();
      SampleContentCheck.verifyMWSampleContent_8fore(root);
   }

   // This test also verifies that the substitutions are performed in the correct order.
   // If they are performed in a different order we get a different answer.
   // It also demonstrates that filters can compose
   @Test
   public void loadWithThreeFilters() throws Throwable {

      InputStream input = this.getClass().getResourceAsStream("/mw_sample_current.xml");
      Assert.assertNotNull("input", input);
      InputSource inputSource = new InputSource(input);
      Unmarshaller unmarshaller = MediaWikiLoader.createUnmarshaller();
      Log log = new Log();

      XMLFilter filter1 = new CharSubPrefilter('e', 'a');
      XMLFilter filter2 = new CharSubPrefilter('a', 'o');
      XMLFilter filter3 = new CharSubPrefilter('o', '8');

      JAXBElement<MediaWikiType> observed = PreFilteredMediaWikiLoader.load(inputSource, log, unmarshaller,
            filter1, filter2, filter3);
      MediaWikiType root = observed.getValue();
      SampleContentCheck.verifyMWSampleContent_8foreoa(root);
   }

   @Test
   public void load_InputStream_WithThreeFilters() throws Throwable {

      InputStream input = this.getClass().getResourceAsStream("/mw_sample_current.xml");
      Assert.assertNotNull("input", input);
      Log log = new Log();

      XMLFilter filter1 = new CharSubPrefilter('e', 'a');
      XMLFilter filter2 = new CharSubPrefilter('a', 'o');
      XMLFilter filter3 = new CharSubPrefilter('o', '8');

      JAXBElement<MediaWikiType> observed = PreFilteredMediaWikiLoader.load(input, log,
            filter1, filter2, filter3);
      MediaWikiType root = observed.getValue();
      SampleContentCheck.verifyMWSampleContent_8foreoa(root);
   }


   public static class CharSubPrefilter extends TextPrefilter {
      private char replace;
      private char with;

      public CharSubPrefilter(char replace, char with) {
         this.replace = replace;
         this.with = with;
      }

      @Override
      protected void handleStartTextElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
         return;
      }

      @Override
      protected void handleTextElementCharacters(char[] ch, int start, int length) throws SAXException {
         for (int i = start; i < start + length; i++) {
            if (ch[i] == replace) {
               ch[i] = with;
            }
         }
         sendCharacters(ch, start, length);
      }

      @Override
      protected void handleEndTextElement(String uri, String localName, String qName) throws SAXException {
         return;
      }
   }
}
