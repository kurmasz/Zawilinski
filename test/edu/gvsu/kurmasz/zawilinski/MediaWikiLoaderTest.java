package edu.gvsu.kurmasz.zawilinski;

import edu.gvsu.kurmasz.warszawa.log.Log;
import edu.gvsu.kurmasz.zawilinski.mw.current.MediaWikiType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.powermock.api.mockito.PowerMockito.spy;

/**
 * @author Zachary Kurmas
 */
// Created  11/21/11 at 3:50 PM
// (C) Zachary Kurmas 2011


@RunWith(PowerMockRunner.class)
@PrepareForTest({MediaWikiLoader.class, JAXBContext.class})
public class MediaWikiLoaderTest {

   //
   // createUnmarshaller
   //

   // verify that the test resources are configured correctly.
   @Test
   public void openResource() throws Throwable {
      InputStream is = this.getClass().getResourceAsStream("/testLanguageFilter.xml");
      Assert.assertNotNull("Oops", is);
   }

   @Test
   public void createUnmarshallerDoesCreateAndReturnUnmarshaller() throws Throwable {
      String contextPath = "edu.gvsu.kurmasz.zawilinski.mw.current";
      JAXBContext myContext = mock(JAXBContext.class);
      Unmarshaller myUnmarshaller = mock(Unmarshaller.class);
      mockStatic(JAXBContext.class);

      PowerMockito.when(JAXBContext.newInstance(contextPath)).thenReturn(myContext);
      Mockito.when(myContext.createUnmarshaller()).thenReturn(myUnmarshaller);

      Unmarshaller observed = MediaWikiLoader.createUnmarshaller();
      assertEquals(myUnmarshaller, observed);

      verifyStatic();
      JAXBContext.newInstance(contextPath);

      verify(myContext).createUnmarshaller();
   }

   @Test
   public void createUnmarshallerThrowsXMLConfigurationExceptionIfErrorMakingContext() throws Throwable {
      String contextPath = "edu.gvsu.kurmasz.zawilinski.mw.current";
      JAXBException exception = mock(JAXBException.class);
      mockStatic(JAXBContext.class);
      PowerMockito.when(JAXBContext.newInstance(contextPath)).thenThrow(exception);

      try {
         MediaWikiLoader.createUnmarshaller();
         fail("Should have thrown Exception");
      } catch (MediaWikiLoader.XMLConfigurationException xmlce) {
         assertEquals(exception, xmlce.getCause());
      }
   }

   @Test
   public void createUnmarshallerThrowsXMLConfigExceptionIfErrorCreatingUnmarshaller() throws Throwable {
      String contextPath = "edu.gvsu.kurmasz.zawilinski.mw.current";
      JAXBContext myContext = mock(JAXBContext.class);
      Unmarshaller myUnmarshaller = mock(Unmarshaller.class);
      JAXBException exception = mock(JAXBException.class);
      mockStatic(JAXBContext.class);

      PowerMockito.when(JAXBContext.newInstance(contextPath)).thenReturn(myContext);
      Mockito.when(myContext.createUnmarshaller()).thenThrow(exception);

      try {
         MediaWikiLoader.createUnmarshaller();
         fail("Should have thrown Exception");
      } catch (MediaWikiLoader.XMLConfigurationException xmlce) {
         assertEquals(exception, xmlce.getCause());
      }
   }

   //
   // load(Source, Log, Unmarshaller)
   //

   @Test(expected = IllegalArgumentException.class)
   public void loadThrowsExceptionIfSourceNull() throws Throwable {
      Log log = mock(Log.class);
      Unmarshaller unmarshaller = mock(Unmarshaller.class);
      MediaWikiLoader.load((Source) null, log, unmarshaller);
   }

   @Test(expected = IllegalArgumentException.class)
   public void loadThrowsExceptionIfLogNull() throws Throwable {
      Source source = mock(Source.class);
      Unmarshaller unmarshaller = mock(Unmarshaller.class);
      MediaWikiLoader.load(source, null, unmarshaller);
   }

   @Test(expected = IllegalArgumentException.class)
   public void loadThrowsExceptionIfUnmarshallerNull() throws Throwable {
      Source source = mock(Source.class);
      Log log = mock(Log.class);
      MediaWikiLoader.load(source, log, (Unmarshaller) null);
   }

   @Test
   public void loadPrintsBeginMessage() throws Throwable {
      Source source = mock(Source.class);
      Log log = mock(Log.class);
      Unmarshaller unmarshaler = mock(Unmarshaller.class);
      MediaWikiLoader.load(source, log, unmarshaler);

      verify(log).println(MediaWikiLoader.PARSE_BEGIN_END, "Begin unmarshal.");
   }

   @Test
   public void loadPrintsEndMessage() throws Throwable {
      Source source = mock(Source.class);
      Log log = mock(Log.class);
      Unmarshaller unmarshaler = mock(Unmarshaller.class);
      MediaWikiLoader.load(source, log, unmarshaler);

      verify(log).println(MediaWikiLoader.PARSE_BEGIN_END, "Complete unmarshal.");
   }

   @Test
   public void loadUnmarshallsData() throws Throwable {
      Source source = mock(Source.class);
      Log log = mock(Log.class);
      Unmarshaller unmarshaler = mock(Unmarshaller.class);
      MediaWikiLoader.load(source, log, unmarshaler);

      verify(unmarshaler).unmarshal(source);
   }

   @Test
   public void loadReturnsUnmarshalledDataAsJAXBElement() throws Throwable {
      Source source = mock(Source.class);
      Log log = mock(Log.class);
      Unmarshaller unmarshaler = mock(Unmarshaller.class);
      @SuppressWarnings("unchecked")
      JAXBElement<MediaWikiType> expected = mock(JAXBElement.class);
      when(unmarshaler.unmarshal(source)).thenReturn(expected);

      JAXBElement<MediaWikiType> observed = MediaWikiLoader.load(source, log, unmarshaler);

      assertEquals(expected, observed);
   }


   //
   // load(Source, Log)
   //
   @Test
   public void load_Source_Log() throws Throwable {

      Source source = mock(Source.class);
      Log log = mock(Log.class);

      Unmarshaller unmarshaller = mock(Unmarshaller.class);
      spy(MediaWikiLoader.class);
      PowerMockito.doReturn(unmarshaller).when(MediaWikiLoader.class);
      MediaWikiLoader.createUnmarshaller();

      @SuppressWarnings("unchecked")
      JAXBElement<MediaWikiType> expected = mock(JAXBElement.class);
      PowerMockito.doReturn(expected).when(MediaWikiLoader.class);
      MediaWikiLoader.load(source, log, unmarshaller);

      JAXBElement<MediaWikiType> observed = MediaWikiLoader.load(source, log);
      assertEquals(expected, observed);

      verifyStatic();
      MediaWikiLoader.load(source, log, unmarshaller);
   }

   @Test(expected = JAXBException.class)
   public void load_String_passesException() throws Throwable {
      Source source = mock(Source.class);
      Log log = mock(Log.class);

      Unmarshaller unmarshaller = mock(Unmarshaller.class);
      spy(MediaWikiLoader.class);
      PowerMockito.doReturn(unmarshaller).when(MediaWikiLoader.class);
      MediaWikiLoader.createUnmarshaller();

      PowerMockito.doThrow(new JAXBException("Oops!")).when(MediaWikiLoader.class);
      MediaWikiLoader.load(source, log, unmarshaller);


      MediaWikiLoader.load(source, log);
   }

   //
   // load(InputStream, Log, String)
   //
   @Test
   public void load_InputStream_Log() throws Throwable {

      InputStream inputStream = mock(InputStream.class);
      Log log = mock(Log.class);

      InputSource inputSource = mock(InputSource.class);
      SAXSource saxSource = mock(SAXSource.class);

      spy(MediaWikiLoader.class);

      whenNew(InputSource.class).withArguments(inputStream).thenReturn(inputSource);
      whenNew(SAXSource.class).withArguments(inputSource).thenReturn(saxSource);

      @SuppressWarnings("unchecked")
      JAXBElement<MediaWikiType> expected = mock(JAXBElement.class);
      PowerMockito.doReturn(expected).when(MediaWikiLoader.class);
      MediaWikiLoader.load(saxSource, log);

      JAXBElement<MediaWikiType> observed = MediaWikiLoader.load(inputStream, log);
      assertEquals(expected, observed);

      verifyStatic();
      MediaWikiLoader.load(saxSource, log);
   }

   @Test(expected = JAXBException.class)
   public void load_InputStream_Log_PassesException() throws Throwable {

      InputStream inputStream = mock(InputStream.class);
      Log log = mock(Log.class);

      InputSource inputSource = mock(InputSource.class);
      SAXSource saxSource = mock(SAXSource.class);

      spy(MediaWikiLoader.class);

      whenNew(InputSource.class).withArguments(inputStream).thenReturn(inputSource);
      whenNew(SAXSource.class).withArguments(inputSource).thenReturn(saxSource);

      PowerMockito.doThrow(new JAXBException("Oops")).when(MediaWikiLoader.class);
      MediaWikiLoader.load(saxSource, log);

      MediaWikiLoader.load(inputStream, log);
   }
}
