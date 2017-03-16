package edu.gvsu.kurmasz.zawilinski;

import edu.gvsu.kurmasz.warszawa.log.Log;
import edu.gvsu.kurmasz.warszawa.log.SimpleLog;
import edu.gvsu.kurmasz.zawilinski.mw.current.MediaWikiType;
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
 * <p/>
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

  /**
   * Name of package containing schema-derived classes (i.e., the classes that correspond to each element in the
   * MediaWiki schema).
   */
  public static final String CONTEXT_PATH = "edu.gvsu.kurmasz.zawilinski.mw.current";

  /**
   * Exception describing a problem with the XML software setup.
   * Typically these exceptions indicate a bug in the code or a runtime configuration problem.
   */
  public static class XMLConfigurationException extends RuntimeException {
    public XMLConfigurationException(String problem, Throwable cause) {
      super(problem, cause);
    }
  }

  private MediaWikiLoader() {
  }

  /**
   * Create a JAXB {@code Unmarshaller} for the specified context path.
   *
   * @param contextPath the context path
   * @return a JAXB {@code Unmarshaller} for the specified context path.
   * @throws XMLConfigurationException if there is a problem creating the JAXB context
   */
  private static Unmarshaller createUnmarshaller(String contextPath) {
    Unmarshaller unmarshaller;
    try {
	// The XML library has built-in limits to prevent DoS attacks from maliciously large .xml files.
	// MediaWiki xml files are very large, but not maliciously so.  This turns off the limits so these
	// large files can be processed.
      System.setProperty("totalEntitySizeLimit","0");
      System.setProperty("jdk.xml.totalEntitySizeLimit","0");
      JAXBContext jaxbContext = JAXBContext.newInstance(contextPath);
      unmarshaller = jaxbContext.createUnmarshaller();
    } catch (JAXBException e) {
      throw new XMLConfigurationException(
          "Problem setting up JAXB objects.", e);
    }
    return unmarshaller;
  } // end constructor

  /**
   * Create a JAXB {@code Unmarshaller} for documents using the current MediaWiki schema.
   *
   * @return a JAXB {@code Unmarshaller} for documents using the  current MediaWiki schema.
   * @throws XMLConfigurationException if there is a problem creating the JAXB context
   */
  public static Unmarshaller createUnmarshaller() {
    return createUnmarshaller(CONTEXT_PATH);
  }


  /**
   * Loads the entire XML document and returns a
   * {@code JAXBElement<MediaWikiType>} object representing the root.
   *
   * @param source       the source of the XML data
   * @param log          a {@code Log} to which to report progress
   * @param unmarshaller the JAXB Unmarshaller
   * @return a {@code JAXBElement<MediaWikiType>} object representing the root of the loaded XML document.
   * @throws JAXBException            if there is a problem generating the DOM
   * @throws IllegalArgumentException if any parameters are {@code null}
   */
  public static JAXBElement<MediaWikiType> load(Source source, SimpleLog log, Unmarshaller unmarshaller) throws
      JAXBException {

    if (source == null) {
      throw new IllegalArgumentException("Must specify input source.");
    }
    if (log == null) {
      throw new IllegalArgumentException("Log may not be null");
    }
    if (unmarshaller == null) {
      throw new IllegalArgumentException("Unmarshaller may not be null.");
    }

    log.println(Zawilinski.PARSE_BEGIN_END, "Begin unmarshal.");
    Object answer = unmarshaller.unmarshal(source);
    log.println(Zawilinski.PARSE_BEGIN_END, "Complete unmarshal.");

    assert answer instanceof JAXBElement : "Unexpected return type from unmarshal!  "
        + answer.getClass().getName();

    // the variable elem exists solely so I can add the
    // annotation to suppress the unchecked warning.
    @SuppressWarnings("unchecked")
    JAXBElement<MediaWikiType> elem = (JAXBElement<MediaWikiType>) (answer);

    return elem;
  } // end load


  /**
   * Loads the entire XML document and returns a
   * {@code JAXBElement<MediaWikiType>} object representing the root.
   *
   * @param source the source of the XML data
   * @param log    a {@code Log} to which to report progress
   * @return a {@code JAXBElement<MediaWikiType>} object representing the root of the loaded XML document.
   * @throws JAXBException             if there is a problem generating the DOM
   * @throws XMLConfigurationException if there is a problem creating the JAXB context
   * @throws IllegalArgumentException  if any parameters are {@code null}
   */
  public static JAXBElement<MediaWikiType> load(Source source, Log log) throws JAXBException {
    return load(source, log, createUnmarshaller());
  }

  /**
   * Loads the entire XML document and returns a
   * {@code JAXBElement<MediaWikiType>} object representing the root.
   *
   * @param input the source of the XML data
   * @param log   a {@code Log} to which to report progress
   * @return a {@code JAXBElement<MediaWikiType>} object representing the root of the loaded XML document.
   * @throws JAXBException             if there is a problem generating the DOM
   * @throws XMLConfigurationException if there is a problem creating the JAXB context
   * @throws IllegalArgumentException  if any parameters are {@code null}
   */
  public static JAXBElement<MediaWikiType> load(InputStream input, Log log) throws JAXBException {
    return load(new SAXSource(new InputSource(input)), log);
  }
}
