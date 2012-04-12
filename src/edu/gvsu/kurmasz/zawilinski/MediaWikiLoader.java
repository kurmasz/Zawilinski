package edu.gvsu.kurmasz.zawilinski;

import com.sun.jmx.remote.internal.Unmarshal;
import edu.gvsu.kurmasz.warszawa.log.Log;
import edu.gvsu.kurmasz.zawilinski.mw.current.MediaWikiType;
import edu.gvsu.kurmasz.zawilinski.mw.current.ObjectFactory;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import java.io.InputStream;

/**
 * <p>Generate the DOM for a MediaWiki XML document.  In particular, this class uses JAXB to unmarshall an XML stream
 * into
 * Java objects. This class is written specifically to support MediaWiki XML documents only because it expects the root
 * of the DOM to be a {@link MediaWikiType} object.</p>
 *
 * <p>Debug levels used:</p>
 * <ul>
 * <li>{@code PARSE_BEGIN_END}: Announce beginning and end of parsing</li>
 * </ul>
 * <p/>
 *
 * @author Zachary Kurmas
 */
// (C) 2010 Zachary Kurmas
// Created February 12, 2010
public class MediaWikiLoader {


   public static final Class<ObjectFactory> ROOT_CLASS = ObjectFactory.class;
   public static final Class<MediaWikiType> ROOT_CLASS2 = MediaWikiType.class;

   private MediaWikiLoader() {
   }

   /**
    * Loads the entire XML document and returns a
    * {@code JAXBElement<MediaWikiType>} object representing the root.
    *
    * @param source the source of the XML data
    * @param log    a {@code Log} to which to report progress
    * @return a {@code JAXBElement<MediaWikiType>} object representing the root of the loaded XML document.
    * @throws JAXBException            if there is a problem generating the DOM
    * @throws edu.gvsu.kurmasz.zawilinski.XMLLoader.XMLConfigurationException
    *                                  if there is a problem creating the JAXB context
    * @throws IllegalArgumentException if any parameters are {@code null}
    */
   public static JAXBElement<MediaWikiType> load(Source source, Log log, Unmarshaller.Listener listener) throws
         JAXBException {
      return XMLLoader.load(source, log, ROOT_CLASS, ROOT_CLASS2, listener);
   }

   public static JAXBElement<MediaWikiType> load(Source source, Log log) throws
         JAXBException {
      return XMLLoader.load(source, log, ROOT_CLASS, ROOT_CLASS2);
   }

   /**
    * Loads the entire XML document and returns a
    * {@code JAXBElement<MediaWikiType>} object representing the root.
    *
    * @param input the source of the XML data
    * @param log   a {@code Log} to which to report progress
    * @return a {@code JAXBElement<MediaWikiType>} object representing the root of the loaded XML document.
    * @throws JAXBException            if there is a problem generating the DOM
    * @throws edu.gvsu.kurmasz.zawilinski.XMLLoader.XMLConfigurationException
    *                                  if there is a problem creating the JAXB context
    * @throws IllegalArgumentException if any parameters are {@code null}
    */
   public static JAXBElement<MediaWikiType> load(InputStream input, Log log) throws JAXBException {
      return load(new SAXSource(new InputSource(input)), log);
   }
}