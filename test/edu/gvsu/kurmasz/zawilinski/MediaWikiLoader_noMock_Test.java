package edu.gvsu.kurmasz.zawilinski;

import edu.gvsu.kurmasz.warszawa.log.Log;
import edu.gvsu.kurmasz.zawilinski.mw.current.MediaWikiType;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;
import java.io.InputStream;

// Created  1/20/12 at 9:07 PM
// (C) Zachary Kurmas 2012

public class MediaWikiLoader_noMock_Test {


   //
   // Test load(Source, Log, Unmarshaller)
   //
   @Test
   public void loadReturnsExpectedData() throws Throwable {
      InputStream input = this.getClass().getResourceAsStream("/mw_sample_current.xml");
      Assert.assertNotNull("input", input);
      SAXSource saxSource = new SAXSource(new InputSource(input));
      Unmarshaller unmarshaller = MediaWikiLoader.createUnmarshaller();
      Log log = new Log();

      JAXBElement<MediaWikiType> observed = MediaWikiLoader.load(saxSource, log, unmarshaller);
      MediaWikiType root = observed.getValue();
      SampleContentCheck.verifyMWSampleContent(root);
   }

   @Test(expected = JAXBException.class)
   public void loadThrowsExceptionIfWrongSchema() throws Throwable {
      InputStream input = this.getClass().getResourceAsStream("/mw_sample_0.4.xml");
      Assert.assertNotNull("input", input);
      SAXSource saxSource = new SAXSource(new InputSource(input));
      Unmarshaller unmarshaller = MediaWikiLoader.createUnmarshaller();
      Log log = new Log();

      MediaWikiLoader.load(saxSource, log, unmarshaller);
   }

   @Test(expected = JAXBException.class)
   public void loadThrowsExceptionIfUnparsableFile() throws Throwable {
      InputStream input = this.getClass().getResourceAsStream("/mw_sample_current_broken.xml");
      Assert.assertNotNull("input", input);
      SAXSource saxSource = new SAXSource(new InputSource(input));
      Unmarshaller unmarshaller = MediaWikiLoader.createUnmarshaller();
      Log log = new Log();

      MediaWikiLoader.load(saxSource, log, unmarshaller);
   }

   //
   // Test load(Source, Log)
   //

   @Test
   public void load_Source_Log_ReturnsExpectedData() throws Throwable {
      InputStream input = this.getClass().getResourceAsStream("/mw_sample_current.xml");
      Assert.assertNotNull("input", input);
      SAXSource saxSource = new SAXSource(new InputSource(input));
      Log log = new Log();

      JAXBElement<MediaWikiType> observed = MediaWikiLoader.load(saxSource, log);
      MediaWikiType root = observed.getValue();
      SampleContentCheck.verifyMWSampleContent(root);
   }

   @Test(expected = JAXBException.class)
   public void load_Source_Log_ThrowsExceptionIfWrongSchema() throws Throwable {
      InputStream input = this.getClass().getResourceAsStream("/mw_sample_0.4.xml");
      Assert.assertNotNull("input", input);
      SAXSource saxSource = new SAXSource(new InputSource(input));
      Log log = new Log();

      MediaWikiLoader.load(saxSource, log);
   }

   @Test(expected = JAXBException.class)
   public void load_Source_Log_ThrowsExceptionIfUnparsableFile() throws Throwable {
      InputStream input = this.getClass().getResourceAsStream("/mw_sample_current_broken.xml");
      Assert.assertNotNull("input", input);
      SAXSource saxSource = new SAXSource(new InputSource(input));
      Log log = new Log();

      MediaWikiLoader.load(saxSource, log);
   }

   //
   // Test load(InputStream, Log)
   //

   @Test
   public void load_InputStream_Log_ReturnsExpectedData() throws Throwable {
      InputStream input = this.getClass().getResourceAsStream("/mw_sample_current.xml");
      Assert.assertNotNull("input", input);
      Log log = new Log();

      JAXBElement<MediaWikiType> observed = MediaWikiLoader.load(input, log);
      MediaWikiType root = observed.getValue();
      SampleContentCheck.verifyMWSampleContent(root);
   }

   @Test(expected = JAXBException.class)
   public void load_InputStream_Log_ThrowsExceptionIfWrongSchema() throws Throwable {
      InputStream input = this.getClass().getResourceAsStream("/mw_sample_0.4.xml");
      Assert.assertNotNull("input", input);
      Log log = new Log();

      MediaWikiLoader.load(input, log);
   }

   @Test(expected = JAXBException.class)
   public void load_InputStream_Log_ThrowsExceptionIfUnparsableFile() throws Throwable {
      InputStream input = this.getClass().getResourceAsStream("/mw_sample_current_broken.xml");
      Assert.assertNotNull("input", input);
      Log log = new Log();

      MediaWikiLoader.load(input, log);
   }
}
