package edu.gvsu.kurmasz.zawilinski;

import edu.gvsu.kurmasz.warszawa.log.SimpleLog;
import edu.gvsu.kurmasz.zawilinski.mw.current.MediaWikiType;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import java.io.InputStream;

/**
 * Generate the DOM for a MediaWiki XML document from a filtered XML stream.
 * In particular, this class applies a SAX "pre-filter" to an XML data stream, then
 * uses JAXB to unmarshal the filtered steam into Java objects. (See {@link MediaWikiLoader} for more details.)
 *
 * @author Zachary Kurmas
 */
// (C) 2010 Zachary Kurmas
// Created February 13, 2010
public class PreFilteredMediaWikiLoader {

   // package scope to allow for testing
   static XMLReader createSAXReader() {
      SAXParserFactory spf = SAXParserFactory.newInstance();

      spf.setNamespaceAware(true);
      SAXParser saxParser;
      try {
         saxParser = spf.newSAXParser();
      } catch (ParserConfigurationException e) {
         throw new MediaWikiLoader.XMLConfigurationException(
               "Problem generating SAX parser.", e);
      } catch (SAXException e) {
         throw new MediaWikiLoader.XMLConfigurationException(
               "Problem configuring SAX parser.", e);
      }

      try {
         return saxParser.getXMLReader();
      } catch (SAXException e) {
         throw new MediaWikiLoader.XMLConfigurationException("Problem creating XMLReader.", e);
      }
   }

   /**
    * Loads and filters and XML stream, then returns a
    * {@code JAXBElement<MediaWikiType>} object representing the root.
    *
    * @param source       the source of the XML data
    * @param log          a {@code Log} to which to report progress
    * @param unmarshaller the JAXB unmarshaller to use
    * @param filterList   a list of SAX filters to apply.
    * @return a {@code MediaWikiType} object representing the root of the filtered XML stream.
    * @throws JAXBException if there is a problem generating the DOM
    * @throws MediaWikiLoader.XMLConfigurationException
    *                       if there is a problem configuring the XML filters or parser
    */
   public static JAXBElement<MediaWikiType> load(InputSource source, SimpleLog log, Unmarshaller unmarshaller,
                                                 XMLFilter... filterList) throws JAXBException {

      if (source == null) {
         throw new IllegalArgumentException("Must specify input source.");
      }
      if (log == null) {
         throw new IllegalArgumentException("Log may not be null");
      }
      if (unmarshaller == null) {
         throw new IllegalArgumentException("Unmarshaller may not be null.");
      }

      XMLReader tail = createSAXReader();

      // add filters
      for (XMLFilter filter : filterList) {
         if (filter == null) {
            throw new IllegalArgumentException("No filter may not be null.");
         }
         filter.setParent(tail);
         tail = filter;
      }

      Source saxSource = new SAXSource(tail, source);
      return MediaWikiLoader.load(saxSource, log, unmarshaller);
   }

   /**
    * Loads and filters and XML stream, then returns a
    * {@code JAXBElement<MediaWikiType>} object representing the root.
    *
    * @param source     the source of the XML data
    * @param log        a {@code Log} to which to report progress
    * @param filterList a list of SAX filters to apply
    * @return a {@code MediaWikiType} object representing the root of the filtered XML stream.
    * @throws JAXBException if there is a problem generating the DOM
    * @throws MediaWikiLoader.XMLConfigurationException
    *                       if there is a problem configuring the XML filters or parser
    */
   public static JAXBElement<MediaWikiType> load(InputStream source, SimpleLog log,
                                                 XMLFilter... filterList) throws JAXBException {
      return load(new InputSource(source), log, MediaWikiLoader.createUnmarshaller(), filterList);
   }


}
