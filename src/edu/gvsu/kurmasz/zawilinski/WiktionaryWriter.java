package edu.gvsu.kurmasz.zawilinski;

import edu.gvsu.kurmasz.warszawa.io.OutputHelper;
import edu.gvsu.kurmasz.zawilinski.MediaWikiLoader.XMLConfigurationException;

import javax.xml.bind.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Writes an XML object tree  out to XML
 *
 * @author Zachary Kurmas
 */
// (C) 2010 Zachary Kurmas
// Created February 13, 2010
public class WiktionaryWriter {

   protected Marshaller marshaller;

   /**
    * Constructor
    *
    * @throws XMLConfigurationException if there are any problems with configuring the XML parsing
    *                                   classes
    */
   public WiktionaryWriter() {
      //
      // Setup JAXB objects
      //
      try {
         JAXBContext jaxbContext = JAXBContext.newInstance();
         marshaller = jaxbContext.createMarshaller();

         // make pretty
         marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
               Boolean.TRUE);

      } catch (JAXBException e) {
         throw new XMLConfigurationException(
               "Problem setting up JAXB objects.", e);
      }
   } // end constructor


   /**
    * Set whether the resulting xml file should be formatted nicely (with
    * whitespace and tabs).
    *
    * @param value {@code true} for nice formatting {@code false} otherwise.
    * @throws XMLConfigurationException if the underlying call to {@code setProperty} throws an
    *                                   exception.
    */
   public void setFormattedOutput(boolean value) {
      try {
         marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
               Boolean.valueOf(value));
      } catch (PropertyException e) {
         throw new XMLConfigurationException(
               "Error setting formatting on JAXB context: ", e);
      }
   }

   /**
    * Call {@code setProperty} on the underlying {@code Marshaller} object.
    *
    * @param name  the name of the property to be set.
    * @param value the value of the property to be set.
    * @throws PropertyException when there is an error processing the given property.
    */
   public void setProperty(String name, Object value) throws PropertyException {
      marshaller.setProperty(name, value);
   }

   /**
    * Write the data rooted at {@code root} to {@code output}
    *
    * @param root   root of the data
    * @param output where to write output
    * @throws JAXBException if anything strange happens.
    */
   public void write(JAXBElement<?> root, OutputStream output)
         throws JAXBException {
      marshaller.marshal(root, output);
   }

   /**
    * Write the data rooted at {@code root} to the specified file
    *
    * @param root root of the data
    * @param file file to write to (or "-" for STDIN)
    * @throws JAXBException         if anything strange happens.
    * @throws FileNotFoundException if {@code file} can't be written
    */
   public void write(JAXBElement<?> root, String file)
         throws FileNotFoundException, JAXBException {
      // TODO: Double check that we want an output Stream and not a Writer
      write(root, OutputHelper.getOutputStream(file, OutputHelper.DEFAULT_OUTPUT_STREAM_MAP));
   } // end write
} // end WiktionaryWriter
