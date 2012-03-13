package edu.gvsu.kurmasz.zawilinski;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * This abstract class simplifies the writing SAX filters that
 * examine and modify only the contents of MediaWiki {@code <text>} tags. Specifically,
 * this class is an implementation of {@code XMLFilter} that provides callbacks
 * that are only active for {@code <text>} tags:
 *
 * <ul>
 * <li> {@link #handleStartTextElement(String, String, String, Attributes)} </li>
 * <li> {@link #handleTextElementCharacters(char[], int, int)}</li>
 * <li> {@link #handleEndTextElement(String, String, String)}
 * </ul>
 *
 * <p>All other events are passed along unchanged.</p>
 *
 * <p>This class's
 * {@link TextPrefilter#startElement(String, String, String, Attributes)}
 * {@link #endElement(String, String, String)} methods pass all start and end
 * events along unchanged (including the start and end of a {@code <text>} tag).
 * The {@link #characters(char[], int, int)} method passes all
 * character events along <em>except</em> those events related to {@code <text>}
 * elements. </p>
 *
 * <p>{@link #characters(char[], int, int)} does not pass along <em>any</em>
 * of a {@code text} element's content.  Instead, all {@code <text>} element content
 * is passed to the {@link #handleTextElementCharacters(char[], int, int)} callback.
 * This method is responsible for calling {@link #sendCharacters(char[],
 * int, int)} with the desired filtered / transformed content.
 * (Note that the subclass must call {@code
 * charHelper} and <em>not</em> {@code super.characters}. Calling {@code
 * super.characters} would, in effect, call {@code TextPrefilter.characters()},
 * thus causing an infinite recursion.)</p>
 *
 * <p>Take care when examining {@code <text>} content.  The character array parameter to {@link #handleTextElementCharacters(char[], int, int)}
 * does not necessarily contain the entire content of the text element.  Instead, it contains only
 * one buffer full of characters (typically a few kilobytes).  The XML parser makes no guarantees as to how
 * and element's content is broken into buffers; thus, the character array may even contain partial lines, words,
 * or sentences.</p>
 *
 * <p>See {@link TextSizePrefilter} for a relatively simple implementation.
 * {@link LanguagePrefilter} is a considerably more complex example because
 * the filter searches for a string that may get broken across calls to {@link TextPrefilter#handleTextElementCharacters(char[], int, int)}.</p>
 *
 * @author Zachary Kurmas
 */
// (C) 2010 Zachary Kurmas
// Created February 6, 2010
public abstract class TextPrefilter extends XMLFilterImpl {

   // package scope for testing
   static final String TEXT_ELEMENT_NAME = "text";
   static final String TITLE_ELEMENT_NAME = "title";
   static final String PAGE_ELEMENT_NAME = "page";

   // true if current parser position is inside a <text> element
   private boolean inText = false;

   // true if current parser position is inside a <title> element
   private boolean inTitle = false;

   // the tile of the current <page> element
   private String currentTitle;

   /**
    * Constructor
    */
   public TextPrefilter() {
      super();
   }

   /**
    * Constructor
    *
    * @param parent the parent XML reader
    */
   public TextPrefilter(XMLReader parent) {
      super(parent);
   }


   /**
    * Return the title of the current page (if available)
    *
    * @return the title of the current page, or {@code null} if not available
    *         (e.g., because parser is not currently inside a {@code page}
    *         element).
    */
   public String getCurrentTitle() {
      return currentTitle;
   }

   /**
    * Called at the beginning of an XML {@code <text>} element. See {@code
    * XMLFilterImpl}.
    *
    * @param uri       The element's Namespace URI, or the empty string.
    * @param localName The element's local name, or the empty string.
    * @param qName     The element's qualified (prefixed) name, or the empty string.
    * @param attrs     The element's attributes.
    * @throws SAXException The client may throw an exception during processing.
    */
   abstract protected void handleStartTextElement(String uri,
                                                  String localName, String qName, Attributes attrs)
         throws SAXException;


   @Override
   public void startElement(String uri, String localName, String qName,
                            Attributes attrs) throws SAXException {
      // No elements should be nested withing <text>. This if inText is true,
      // there's a problem.
      assert !inText : "No elements should be nested within text!";

      // If this is a <text> tag, remember that we're in a <text> element,
      // then call the abstract method that handles starting <text> tags.
      if (localName.equals(TEXT_ELEMENT_NAME)) {
         inText = true;
         handleStartTextElement(uri, localName, qName, attrs);

         // if this is a <title> tag, remember that we're inside a <title>
         // tag and reset the currentTitle (because we're about to get new
         // title data).
      } else if (localName.equals(TITLE_ELEMENT_NAME)) {
         inTitle = true;
         currentTitle = "";
      }

      // Pass the event along.
      super.startElement(uri, localName, qName, attrs);
   }

   /**
    * Called after the underlying SAX parser reads a set of {@code <text>}
    * data. See {@code XMLFilterImpl}.
    *
    * @param ch     An array of characters.
    * @param start  The starting position in the array.
    * @param length The number of characters to use from the array.
    * @throws SAXException The client may throw an exception during processing.
    */
   abstract protected void handleTextElementCharacters(char[] ch, int start,
                                                       int length) throws SAXException;

   // Override. This class passes all characters through the filter, except
   // those of <text> elements. Those characters are handed to the subclass for
   // further processing.
   public void characters(char[] ch, int start, int length)
         throws SAXException {
      // Remember the current title
      if (inTitle) {
         currentTitle += new String(ch, start, length);
      }

      // This filter handles text elements only. Pass all other method calls
      // through unchanged.
      if (inText) {
         handleTextElementCharacters(ch, start, length);
      } else {
         super.characters(ch, start, length);
      }
   }

   /**
    * Sends characters through the filter (i.e., calls {@code XMLFilterImpl.characters()}). Note that
    * calling {@code super.characters()} from a subclass won't pass characters through the filter.  It will
    * make a recursive call to {@link TextPrefilter#characters(char[], int, int)} characters}.
    *
    * @param ch     An array of characters.
    * @param start  The starting position in the array.
    * @param length The number of characters to use from the array.
    * @throws SAXException The client may throw an exception during processing.
    */
   protected void sendCharacters(char[] ch, int start, int length)
         throws SAXException {
      super.characters(ch, start, length);
   }

   /**
    * Called when an ending {@code </text>} is reached.
    *
    * @param uri       The element's Namespace URI, or the empty string.
    * @param localName The element's local name, or the empty string.
    * @param qName     The element's qualified (prefixed) name, or the empty string.
    * @throws SAXException The client may throw an exception during processing.
    */
   abstract protected void handleEndTextElement(String uri, String localName,
                                                String qName) throws SAXException;


   @Override
   public void endElement(String uri, String localName, String qName)
         throws SAXException {

      // if we're finishing a </text> element, (1) note that we're no longer
      // in a </text> element, then (2) call the abstract method that handles
      // </text> events.
      if (localName.equals(TEXT_ELEMENT_NAME)) {
         assert inText : "Exiting text but not in text!";
         inText = false;

         handleEndTextElement(uri, localName, qName);

      } else if (localName.equals(TITLE_ELEMENT_NAME)) {
         assert inTitle : "Exiting title but not in <title>";
         inTitle = false;
      } else if (localName.equals(PAGE_ELEMENT_NAME)) {
         currentTitle = null;
      }

      // pass event along.
      super.endElement(uri, localName, qName);

   } // end endElement


   // package protected for testing purposes.
   boolean inText() {
      return inText;
   }

   // package protected for testing purposes.
   boolean inTitle() {
      return inTitle;
   }

} // end TextPrefilter
