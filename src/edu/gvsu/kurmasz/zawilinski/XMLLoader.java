// Note:  I'm trying to abstract the loader to work with any XML schema, passed into load as a Class object
// The problem is that if the file implements a different schema, we don't get an error,
// we just get an empty XML object.  We want a fail-fast setup:  An incorrect XML schema
// should immediately throw an exception.
//
// This appears to be a difference in how newInstance(String) and newInstance(Class<?>...) work

package edu.gvsu.kurmasz.zawilinski;

import edu.gvsu.kurmasz.warszawa.log.Log;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;

/**
 * @author Zachary Kurmas
 */
// Created  4/12/12 at 1:36 PM
// (C) Zachary Kurmas 2012

public class XMLLoader {

   /**
    * The debug level at which the beginning and end of parsing is recorded in the log.
    */
   public static final int PARSE_BEGIN_END = 100;

   /**
    * Exception describing a problem with the XML software setup.
    * Typically these exceptions indicate a bug in the code or a runtime configuration problem.
    */
   public static class XMLConfigurationException extends RuntimeException {
      public XMLConfigurationException(String problem, Throwable cause) {
         super(problem, cause);
      }
   }

   private XMLLoader() {
   }


   /**
    * Create a JAXB {@code Unmarshaller} for the specified context path.
    *
    * @param contextPath the context path
    * @return a JAXB {@code Unmarshaller} for the specified context path.
    * @throws XMLConfigurationException if there is a problem creating the JAXB context
    */
   private static Unmarshaller createUnmarshaller(Class<?> contextPath) {
      Unmarshaller unmarshaller;
      try {
         JAXBContext jaxbContext = JAXBContext.newInstance(contextPath);
         unmarshaller = jaxbContext.createUnmarshaller();
      } catch (JAXBException e) {
         throw new XMLConfigurationException(
               "Problem setting up JAXB objects.", e);
      }
      return unmarshaller;
   } // end constructor

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
   public static <T, U> JAXBElement<U> load(Source source, Log log, Class<T> context,
                                            Class<U> root,
                                         Unmarshaller.Listener listener) throws
         JAXBException {

      if (source == null) {
         throw new IllegalArgumentException("Must specify input source.");
      }
      if (log == null) {
         throw new IllegalArgumentException("Log may not be null");
      }
      if (context == null) {
         throw new IllegalArgumentException("Unmarshaller may not be null.");
      }

      Unmarshaller unmarshaller = createUnmarshaller(context);
      unmarshaller.setListener(listener);
      log.println(XMLLoader.PARSE_BEGIN_END, "Begin unmarshal.");
      JAXBElement<U> answer = unmarshaller.unmarshal(source, root);
      //Object answer = unmarshaller.unmarshal(source);

      log.println(XMLLoader.PARSE_BEGIN_END, "Complete unmarshal.");

      return answer;
         } // end load

   public static <T, U> JAXBElement<U> load(Source source, Log log, Class<T> context, Class<U> root) throws
         JAXBException {
      return load(source, log, context, root, null);
   }
}
