package edu.gvsu.kurmasz.zawilinski;

import edu.gvsu.kurmasz.warszawa.io.InputHelper;
import edu.gvsu.kurmasz.warszawa.log.Log;
import edu.gvsu.kurmasz.warszawa.log.SimpleLog;
import edu.gvsu.kurmasz.zawilinski.mw.current.MediaWikiType;
import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.InputStream;

/**
 * Generates a DOM for a Wiktionary XML document that contains only elements for selected entries.
 * Specifically, this class class (1) applies a SAX filter to an XML data stream,
 * (2) uses JAXB to unmarshal the filtered steam into Java objects, then (3) applies a second
 * filter to the newly created DOM elements for each Wiktionary entry.  The final DOM contains
 * elements for only those Wiktionary entries that passed through the filter.
 * (See {@link PreFilteredMediaWikiLoader}
 * and {@link MediaWikiLoader} for more details.)
 *
 * <p> Currently this class can apply only one post filter.
 *
 * <p>
 * Debug levels: (activates all levels above):
 * <ul>
 * <li>PARSE_BEGIN_END: Announce beginning and end of parsing
 * <li>PAGE_DUMPED: Announces which Wiktionary entries kept and which dumped.
 * <li>REVISION_DUMPED: Announces which Wiktionary revisions are kept and which are dumped.
 * </ul>
 *
 * @author Zachary Kurmas
 */
// (C) 2010 Zachary Kurmas
// Created Feb 12, 2010
public class PostFilteredMediaWikiLoader {

    public static JAXBElement<MediaWikiType> loadFilteredPages(InputStream source, SimpleLog log,
                                                              PostFilter postFilter,
                                                              XMLFilter... filterList) throws JAXBException {
      Unmarshaller unmarshaller = MediaWikiLoader.createUnmarshaller();
      unmarshaller.setListener(new PageFilterListener(postFilter, log));
      return PreFilteredMediaWikiLoader.load(new InputSource(source), log, unmarshaller, filterList);
   }

   public static void main(String[] args) throws JAXBException {
      String filename = "/Users/kurmasz/Documents/LocalResearch/LanguageWiki/SampleInput/mw_sample_0.4.xml";
      InputStream source = InputHelper.openFilteredInputStreamOrQuit(new File(filename));
      SimpleLog log = new Log(System.err, 0);
      JAXBElement<MediaWikiType> elem = loadFilteredPages(source, log, PostFilter.KEEP_ALL);
      MediaWikiType root = elem.getValue();
      //Util.print(root);
   }
}
