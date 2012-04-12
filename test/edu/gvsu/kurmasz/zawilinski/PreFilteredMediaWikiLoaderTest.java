package edu.gvsu.kurmasz.zawilinski;

import edu.gvsu.kurmasz.warszawa.log.Log;
import edu.gvsu.kurmasz.zawilinski.mw.current.MediaWikiType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.*;

// Created  1/24/12 at 11:12 AM
// (C) Zachary Kurmas 2012


@RunWith(PowerMockRunner.class)
@PrepareForTest({PreFilteredMediaWikiLoader.class, SAXParserFactory.class, MediaWikiLoader.class, InputSource.class})
public class PreFilteredMediaWikiLoaderTest {


   //
   // createSaxReader
   //
   @Test
   public void createSaxReaderCreatesParserFactory() throws Throwable {
      SAXParserFactory factory = mock(SAXParserFactory.class);

      mockStatic(SAXParserFactory.class);
      when(SAXParserFactory.newInstance()).thenReturn(factory);

      SAXParser parser = mock(SAXParser.class);
      when(factory.newSAXParser()).thenReturn(parser);


      PreFilteredMediaWikiLoader.createSAXReader();

      verifyStatic();
      SAXParserFactory.newInstance();
   }

   @Test
   public void createSaxReaderMakesFactoryNamespaceaware() throws Throwable {
      SAXParserFactory factory = mock(SAXParserFactory.class);

      mockStatic(SAXParserFactory.class);
      when(SAXParserFactory.newInstance()).thenReturn(factory);

      SAXParser parser = mock(SAXParser.class);
      when(factory.newSAXParser()).thenReturn(parser);

      PreFilteredMediaWikiLoader.createSAXReader();

      Mockito.verify(factory).setNamespaceAware(true);
   }

   @Test
   public void createSAXReaderCreatesParser() throws Throwable {
      SAXParserFactory factory = mock(SAXParserFactory.class);

      mockStatic(SAXParserFactory.class);
      when(SAXParserFactory.newInstance()).thenReturn(factory);

      SAXParser parser = mock(SAXParser.class);
      when(factory.newSAXParser()).thenReturn(parser);


      PreFilteredMediaWikiLoader.createSAXReader();

      Mockito.verify(factory).newSAXParser();
   }

   @Test
   public void createSAXReaderThrowsXMLConfigExceptionIfParserConfigProblem() throws Throwable {
      SAXParserFactory factory = mock(SAXParserFactory.class);

      mockStatic(SAXParserFactory.class);
      when(SAXParserFactory.newInstance()).thenReturn(factory);

      ParserConfigurationException cause = mock(ParserConfigurationException.class);

      when(factory.newSAXParser()).thenThrow(cause);

      try {
         PreFilteredMediaWikiLoader.createSAXReader();
      } catch (XMLLoader.XMLConfigurationException e) {
         assertEquals(cause, e.getCause());
      }
   }


   @Test
   public void createSAXReaderThrowsXMLConfigExceptionIfSAXException() throws Throwable {
      SAXParserFactory factory = mock(SAXParserFactory.class);

      mockStatic(SAXParserFactory.class);
      when(SAXParserFactory.newInstance()).thenReturn(factory);

      SAXException cause = mock(SAXException.class);

      when(factory.newSAXParser()).thenThrow(cause);

      try {
         PreFilteredMediaWikiLoader.createSAXReader();
      } catch (XMLLoader.XMLConfigurationException e) {
         assertEquals(cause, e.getCause());
      }
   }

   @Test
   public void createSAXReaderGetsAndReturnsXMLReader() throws Throwable {
      SAXParserFactory factory = mock(SAXParserFactory.class);

      mockStatic(SAXParserFactory.class);
      when(SAXParserFactory.newInstance()).thenReturn(factory);

      SAXParser parser = mock(SAXParser.class);
      when(factory.newSAXParser()).thenReturn(parser);

      XMLReader expected = mock(XMLReader.class);
      when(parser.getXMLReader()).thenReturn(expected);

      XMLReader observed = PreFilteredMediaWikiLoader.createSAXReader();

      assertEquals(expected, observed);

      Mockito.verify(parser).getXMLReader();
   }

   @Test
   public void createSAXReaderThrowsXMLConfigurationExceptionIfGetXMLReaderFails() throws Throwable {
      SAXParserFactory factory = mock(SAXParserFactory.class);

      mockStatic(SAXParserFactory.class);
      when(SAXParserFactory.newInstance()).thenReturn(factory);

      SAXParser parser = mock(SAXParser.class);
      when(factory.newSAXParser()).thenReturn(parser);

      SAXException cause = mock(SAXException.class);
      when(parser.getXMLReader()).thenThrow(cause);

      try {
         PreFilteredMediaWikiLoader.createSAXReader();
      } catch (XMLLoader.XMLConfigurationException e) {
         assertEquals(cause, e.getCause());
      }
   }

   //
   // load(InputSource, Log, Unmarshaller, filters)
   //

//   @Test(expected = IllegalArgumentException.class)
//   public void throwsIllegalArgumentExceptionIfSourceNull() throws Throwable {
//
//      InputSource source = null;
//      Log log = mock(Log.class);
//      Unmarshaller unmarshaller = mock(Unmarshaller.class);
//      XMLFilter filter = mock(XMLFilter.class);
//      spy(PreFilteredMediaWikiLoader.class);
//
//      PreFilteredMediaWikiLoader.load(source, log, unmarshaller, filter);
//   }
//
//   @Test(expected = IllegalArgumentException.class)
//   public void throwsIllegalArgumentExceptionIfLogNull() throws Throwable {
//
//      InputSource source = mock(InputSource.class);
//      Log log = null;
//      Unmarshaller unmarshaller = mock(Unmarshaller.class);
//      XMLFilter filter = mock(XMLFilter.class);
//      spy(PreFilteredMediaWikiLoader.class);
//
//      PreFilteredMediaWikiLoader.load(source, log, unmarshaller, filter);
//   }
//
//   @Test(expected = IllegalArgumentException.class)
//   public void throwsIllegalArgumentExceptionIfUnmarshallerNull() throws Throwable {
//
//      InputSource source = mock(InputSource.class);
//      Log log = mock(Log.class);
//      Unmarshaller unmarshaller = null;
//      XMLFilter filter = mock(XMLFilter.class);
//      spy(PreFilteredMediaWikiLoader.class);
//
//      PreFilteredMediaWikiLoader.load(source, log, unmarshaller, filter);
//   }
//
//   @Test(expected = IllegalArgumentException.class)
//   public void throwsIllegalArgumentExceptionIfSomeFilterNull() throws Throwable {
//
//      InputSource source = mock(InputSource.class);
//      Log log = mock(Log.class);
//      Unmarshaller unmarshaller = mock(Unmarshaller.class);
//      XMLFilter filter1 = mock(XMLFilter.class);
//      XMLFilter filter2 = mock(XMLFilter.class);
//      XMLFilter filter3 = mock(XMLFilter.class);
//      XMLFilter filter4 = null;
//      XMLFilter filter5 = mock(XMLFilter.class);
//      spy(PreFilteredMediaWikiLoader.class);
//
//      PreFilteredMediaWikiLoader.load(source, log, unmarshaller,
//            filter1, filter2, filter3, filter4, filter5);
//   }


//   @Test
//   public void load_CreatesReader() throws Throwable {
//
//      InputSource source = mock(InputSource.class);
//      Log log = mock(Log.class);
//      Unmarshaller unmarshaller = mock(Unmarshaller.class);
//      XMLFilter filter = mock(XMLFilter.class);
//      spy(PreFilteredMediaWikiLoader.class);
//
//      PreFilteredMediaWikiLoader.load(source, log, unmarshaller, filter);
//
//      verifyStatic();
//      PreFilteredMediaWikiLoader.createSAXReader();
//   }

//   @Test
//   public void loadChainsFilters() throws Throwable {
//
//      InputSource source = mock(InputSource.class);
//      Log log = mock(Log.class);
//      Unmarshaller unmarshaller = mock(Unmarshaller.class);
//      XMLFilter filter1 = mock(XMLFilter.class);
//      XMLFilter filter2 = mock(XMLFilter.class);
//      XMLFilter filter3 = mock(XMLFilter.class);
//      XMLFilter filter4 = mock(XMLFilter.class);
//      XMLReader reader = mock(XMLReader.class);
//
//      spy(PreFilteredMediaWikiLoader.class);
//      when(PreFilteredMediaWikiLoader.createSAXReader()).thenReturn(reader);
//
//      PreFilteredMediaWikiLoader.load(source, log, unmarshaller,
//            filter1, filter2, filter3, filter4);
//
//      Mockito.verify(filter1).setParent(reader);
//      Mockito.verify(filter2).setParent(filter1);
//      Mockito.verify(filter3).setParent(filter2);
//      Mockito.verify(filter4).setParent(filter3);
//   }

//   @Test
//   public void loadCreatesSaxSource() throws Throwable {
//      spy(SAXSource.class);
//      InputSource source = mock(InputSource.class);
//      Log log = mock(Log.class);
//      Unmarshaller unmarshaller = mock(Unmarshaller.class);
//      XMLFilter filter1 = mock(XMLFilter.class);
//      XMLFilter filter2 = mock(XMLFilter.class);
//      XMLFilter filter3 = mock(XMLFilter.class);
//      XMLFilter filter4 = mock(XMLFilter.class);
//
//      SAXSource saxSource = mock(SAXSource.class);
//      whenNew(SAXSource.class).withArguments(filter4, source).thenReturn(saxSource);
//
//      PreFilteredMediaWikiLoader.load(source, log, unmarshaller,
//            filter1, filter2, filter3, filter4);
//
//
//      verifyNew(SAXSource.class).withArguments(filter4, source);
//   }

//   @Test
//   public void loadCallsAndReturnsMediaWikiLoaderLoad() throws Throwable {
//
//
//      InputSource source = mock(InputSource.class);
//      Log log = mock(Log.class);
//      Unmarshaller unmarshaller = mock(Unmarshaller.class);
//      XMLFilter filter1 = mock(XMLFilter.class);
//      XMLFilter filter2 = mock(XMLFilter.class);
//      XMLFilter filter3 = mock(XMLFilter.class);
//      XMLFilter filter4 = mock(XMLFilter.class);
//      @SuppressWarnings("unchecked")
//      JAXBElement<MediaWikiType> expected = mock(JAXBElement.class);
//
//      SAXSource saxSource = mock(SAXSource.class);
//      whenNew(SAXSource.class).withArguments(filter4, source).thenReturn(saxSource);
//
//      spy(MediaWikiLoader.class);
//      PowerMockito.doReturn(expected).when(MediaWikiLoader.class);
//      MediaWikiLoader.load(saxSource, log, unmarshaller);
//
//      JAXBElement<MediaWikiType> observed =
//            PreFilteredMediaWikiLoader.load(source, log, unmarshaller,
//                  filter1, filter2, filter3, filter4);
//
//      assertEquals(expected, observed);
//
//      verifyStatic();
//      MediaWikiLoader.load(saxSource, log, unmarshaller);
//   }

   //
   // load(InputStream, Log, XMLFilter...)
   //
//   @Test
//   public void load_InputStream_CreatesInputSource() throws Throwable {
//
//      spy(MediaWikiLoader.class);
//      InputStream inputStream = mock(InputStream.class);
//      InputSource inputSource = mock(InputSource.class);
//
//      when(MediaWikiLoader.createUnmarshaller()).thenReturn(mock(Unmarshaller.class));
//
//      whenNew(InputSource.class).withArguments(inputStream).thenReturn(inputSource);
//      PreFilteredMediaWikiLoader.load(inputStream, mock(Log.class));
//
//      PowerMockito.verifyNew(InputSource.class).withArguments(inputStream);
//   }

//   @Test
//   public void load_InputStream_CreatesUnmarshaller() throws Throwable {
//
//      mockStatic(MediaWikiLoader.class);
//      InputStream inputStream = mock(InputStream.class);
//      InputSource inputSource = mock(InputSource.class);
//      Unmarshaller unmarshaller = mock(Unmarshaller.class);
//      PowerMockito.when(MediaWikiLoader.createUnmarshaller()).thenReturn(unmarshaller);
//
//      whenNew(InputSource.class).withArguments(inputStream).thenReturn(inputSource);
//      PreFilteredMediaWikiLoader.load(inputStream, mock(Log.class));
//
//      verifyStatic();
//      MediaWikiLoader.createUnmarshaller();
//   }

//   @Test
//   public void load_InputStream_CallsAndReturns_load() throws Throwable {
//      mockStatic(MediaWikiLoader.class);
//      InputStream inputStream = mock(InputStream.class);
//      InputSource inputSource = mock(InputSource.class);
//      Log log = mock(Log.class);
//      Unmarshaller unmarshaller = mock(Unmarshaller.class);
//      XMLFilter filter1 = mock(XMLFilter.class);
//      XMLFilter filter2 = mock(XMLFilter.class);
//      PowerMockito.when(MediaWikiLoader.createUnmarshaller()).thenReturn(unmarshaller);
//
//      whenNew(InputSource.class).withArguments(inputStream).thenReturn(inputSource);
//      @SuppressWarnings("unchecked")
//      JAXBElement<MediaWikiType> expected = mock(JAXBElement.class);
//
//      spy(PreFilteredMediaWikiLoader.class);
//      doReturn(expected).when(PreFilteredMediaWikiLoader.class);
//      PreFilteredMediaWikiLoader.load(inputSource, log, unmarshaller, filter1, filter2);
//
//      JAXBElement<MediaWikiType> observed = PreFilteredMediaWikiLoader.load(inputStream, log,
//            filter1, filter2);
//      assertEquals(expected, observed);
//
//      verifyStatic();
//      PreFilteredMediaWikiLoader.load(inputSource, log, unmarshaller, filter1, filter2);
//   }
}
