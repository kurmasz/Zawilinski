/**
 * <code>Zawilinski</code> is a Java library designed to simplify the extraction of data from
 * Wiktionary entries.
 *
 * We wrote {@code Zawilinski} specifically to extract and analyze the  inflections of all the Polish words in the
 * <a href="http://en.wiktionary.org">English Wiktionary</a>. However, we designed the library so that others could
 * easily extend it to support the extraction of translations, pronunciation, or any other data in Wiktionary.
 * (We haven't tried this ourselves, but we don't expect it would be difficult to use {@code Zawilinski} with any MediaWiki XML dump.)
 *
 * <h2>Why {@code Zawilinski} is useful</h2>
 *
 * <p>{@code Zawilinski}'s key feature is a series of "hooks" that allow users to filter out irrelevant data
 * as early as possible, thereby reducing both processing time and RAM needed to complete the analysis.</p>
 *
 * <p>Wiktionary, Wikipedia, and other Wikimedia projects make their content available to
 * researchers in XML files.
 * (See <a href="http://meta.wikimedia.org/wiki/Data_dumps"><code>http://meta.wikimedia.org/wiki/Data_dumps</code></a>.)
 * The simplest and most straightforward way to analyze the contents of these files is
 * to build a <a href="http://en.wikipedia.org/wiki/Document_Object_Model">Document Object Model</a> (DOM),
 * then traverse the DOM and examine the contents of the desired elements.  Unfortunately, the complete DOM of
 * of a typical Wiktionary XML dump is much too large to be handled by a typical workstation. (Attempting to create one
 * generates {@code OutOfMemory} errors, or causes the memory system to thrash.) </p>
 *
 * <p>{@code Zawilinski} provides two "hooks" (i.e., Java interfaces) that allow users to discard irrelevant data from a
 * Wiktionary XML dump as soon as possible. One hook, which we call the {@code pre-filter}, allows users to filter the
 * text of a Wiktionary entry before {@code Zawilinksi} creates the DOM elements for that entry.
 * The other hook, which we call the {@code post-filter}, allows users to examine the DOM for each Wiktionary entry
 * as it's created, then immediately delete the DOM if it is not needed.
 *
 * <dl>
 * <dt><b>Pre-filter:</b></dt><dd> A <em>pre-filter</em> is user-supplied code that examines and filters the text of a
 * Wiktionary entry.  (The "text" is the Mediawiki markup that defines the content that users see when visiting the page.)
 * Reducing the size of an entry's text reduces the amount of work and RAM needed to build the DOM elements for that entry.
 * (An entry also contains metadata such as the contributor's name or IP and the last update time; but, that data tends to be
 * small compared to the text.)  For example, when extracting data about Polish inflections, we filter out all entry text
 * that is not part of the "Polish" section.  Specifically, we remove all text until we see {@code ==Polish==}, then remove all text
 * after the next second-level heading (e.g., {@code ==Spanish==}).  As a result, the DOM elements created do not contain
 * entry text that is obviously irrelevant to our study.  This both reduces the time needed to generate (or <em>unmarshal</em>) the DOM elements, and reduces
 * the memory needed to store these elements.  We also use the pre-filter to limit the text size of each entry to 64MB.
 * Entries larger than this have been vandalized by the insertion of long strings of random characters.  Attempting to generate
 * DOM elements from these vandalized entries generates {@code OutOfMemory} errors. </dd>
 * </dl>
 *
 * <dt><b>Post-filter:</b></dt><dd> A <em>post-filter</em> is user-supplied code that examines the DOM elements for a
 * Wiktionary entry immediate after they have been created, then decides whether to keep or discard those DOM elements.
 * A typical computer does not have enough RAM to hold the DOM for each entry in a Wiktionary dump.
 * Discarding unnecessary DOM elements frees RAM for other elements.  We wrote a post-filter that discards the DOM elements for
 * Wiktionary entries that do not represent Polish words (i.e., do not have a "Polish" section).  Post-filters do not
 * modify an entry, they simply tell the unmarshaller which DOM elements to keep and which to discard.</dd>
 * </dl>
 *
 * <p>Users can combine pre-filters and post-filters to define a DOM that can fit in RAM and be efficiently
 * examined for the desired data.  This smaller DOM can also be written to disk so that users can repeat / debug their analyses
 * without repeating the filtering process each time.  (In fact, {@code Zawilinski} can be simply used as stand-alone XML filter.)</p>
 *
 * <p>At this point, you may be wondering why we need both pre- and post- filters. Pre-filters are necessary to assure that
 * each Wiktionary entry is small enough to be unmarshalled into a set of DOM elements.  (Some entries that have been vandalized
 * to contain gigabytes of random text.)  However, for simplicity, pre-filters can examine and filter only
 * the text of an entry.  They cannot examine or filter the metadata.  Thus, the post-filters are needed to remove entire unwanted entries.</p> 
 *
 *
 * <h2>Using {@code Zawilinski} </h2>
 *
 * {@code Zawilinski} comes with several stand-alone filter programs; or,
 * users can use it as a library to support their custom filters.
 *
 * <h3>Included programs</h3>
 *
 * <code>Zawilinski</code> contains several programs that can be used without writing any
 * code:
 *
 * <ul>
 * <li><code>FilterWiktionaryByLanguage</code>: Filter a Wiktionary XML dump to retain only
 * those pages or revisions that contain information for a specified language.
 * Run {@code java -jar Zawilinski.jar --help} for details.</li>
 * </ul>
 *
 *
 * <h3>Typical library use</h3>
 *
 * We designed {@code Zawilinski} primary to support the extraction and analysis of data from Wiktionary entries.
 * To develop a new study, users need only do the following:
 *
 * <ol>
 *  <li> Write pre-filter
 *  <li> Write post-filter
 *  <li> Write code to examine the remaining entries
 * </ol>
 *
 * <dl>
 *   <dt><b>Write pre-filter</b>:</dt>
 *   <dd> Pre-filters modify the XML stream before it reaches the JAXB marshaler.
 *        The pre-filter can be any implementation of
 *        <a href="http://download.oracle.com/javase/1.4.2/docs/api/org/xml/sax/XMLFilter.html"></code>XMLFilter</code></a>
 *        and can modify the XML stream any way it likes (it is not limited to filtering text); however,
 *        writing an XML filter can be challenging because the programmer must explicitly keep track of what
 *        part of the document is currently being processed (e.g., the depth of the current element).
 *
 *       <p>
 *       We provide the abstract class {@link edu.gvsu.cis.kurmasz.zawilinski.TextPrefilter} to simplify the process of filtering the content
 *       of Wiktionary entries.  A subclass of {@code TextPrefilter} must define {@link edu.gvsu.cis.kurmasz.zawilinski.TextPrefilter#handleTextElementCharacters(char[], int, int) }.
 *       This method examines the characters in the array passed to it, then calls {@link edu.gvsu.cis.kurmasz.zawilinski.TextPrefilter#charHelper(char[], int, int)} with
 *       those characters that are to pass through the filter. (See {@link edu.gvsu.cis.kurmasz.zawilinski.TextPrefilter} for details.)
 * </dd>
 *
 *   <dt><b>Write post-filter</b>:</dt>
 *   <dd>Post-filters decide whether to retain or discard the DOM elements for a particular Wiktionary entry or revision. (Each entry may have many revisions.)
 *       A post-filter is an implementation of the {@link edu.gvsu.kurmasz.zawilinski.PostFilteredMediaWikiLoader.PostFilter} interface.
 *       This interface defines two methods: {@code keepPage} and {@code keepRevision}.  Writing these methods requires
 *       the programmer to know the <a href="doc-files/mediaWikiImportSchema.png">XML schema for Wiktionary</a> and the Java classes that correspond to each element in the schema.
 *      (See {@link the edu.gvsu.cis.kurmasz.zawilinski.mw} package for details.}
 *
 *    </dd>
 *
 *    <dt><b>Analyze remaining entries</b>:</dt>
 *    <dd>
 *    </dd>
 *
 * </dl>
 *
 * <ul>
 *
 * <li>Filter the XML document to remove {@code <page>}s, {@code <revision>}s,
 * and content that are not relevant to the intended study.
 *
 * <li>Load an XML document into a hierarchy of Java objects. Technically
 * speaking, <code>Zawilinski</code> uses <a target="_top"
 * href="http://java.sun.com/developer/technicalArticles/WebServices/jaxb/"
 * >JAXB</a> to <em>unmarshal</em> the XML document.</li>
 *
 *
 * <li>Examine the retained objects and content for the data under study.
 *
 * </ul>
 *
 * <h3>Filtering and Loading</h3>
 *
 * <p>
 * {@link edu.gvsu.kurmasz.zawilinski.MediaWikiLoader} loads an XML
 * document into a hierarchy of Java objects. (See
 * {@link edu.gvsu.cis.kurmasz.zawilinski.mw} for the Javadoc for these classes;
 * and see <a href=
 * "http://upload.wikimedia.org/wikipedia/mediawiki/4/41/MediaWikiImportSchema.png"
 * >this image</a> for a picture of the MediaWiki XML schema represented by
 * these classes.) The challenge is that MediaWiki XML documents tend to be too
 * large to fit in memory. Thus, we must remove unnecessary pages, revisions, or
 * content to save memory.
 * </p>
 *
 * <p>
 * Zawilinksi provides two types of filters:
 *
 * <ul>
 * <li><em>Pre-filters</em> remove XML content before it is loaded into Java
 * objects. {@link edu.gvsu.kurmasz.zawilinski.PreFilteredMediaWikiLoader}
 * extends {@code WiktionaryLoader} to provide pre-filtering capability.</li>
 * <li> <em>Post-filters</em> remove unwanted
 * {@link edu.gvsu.cis.kurmasz.zawilinski.mw.PageType} and
 * {@link edu.gvsu.cis.kurmasz.zawilinski.mw.RevisionType} objects (e.g.,
 * objects that don't contain any data of interest).
 * {@link edu.gvsu.kurmasz.zawilinski.PostFilteredMediaWikiLoader} extends
 * {@code SaxFilteredWiktionaryLoader} to provide post-filtering capability.</li>
 *
 * </ul>
 *
 *
 *
 *
 * <h4>Post-filter</h4>
 *
 * <p>
 * Most Wiktionary/Wikipedia XML dumps are too large to fit in memory. Thus,
 * <code>Zawilinski</code> provides the post-filter mechanism to remove any {@code PageType}
 * or {@code RevisionType} object that does not contain data relevant to the
 * researcher. Specifically, Zawilisnki uses the JAXB
 *
 * <a target="_top" href="https://jaxb.dev.java.net/nonav/2.2-ea/docs/api/javax/xml/bind/Unmarshaller.html#unmarshalEventCallback"
 * >Event Callback</a>
 *
 * mechanism to immediately remove unwanted {@code PageType} and {@code
 * RevisionType} objects. Users pass an object that implements
 * {@link edu.gvsu.kurmasz.zawilinski.PostFilteredMediaWikiLoader.PostFilter}
 * to a {@link edu.gvsu.kurmasz.zawilinski.PostFilteredMediaWikiLoader}
 * object. The callback method calls <!--
 * {@link edu.gvsu.kurmasz.zawilinski.PostFilteredMediaWikiLoader.PostFilter#keepPage(edu.gvsu.cis.kurmasz.zawilinski.mw.PageType)}
 * or
 * {@link edu.gvsu.kurmasz.zawilinski.PostFilteredMediaWikiLoader.PostFilter#keepRevision(edu.gvsu.cis.kurmasz.zawilinski.mw.RevisionType)}
 * --> {@code keepPage} or {@code keepRevision} as appropriate. If a {@code
 * PageType} object isn't wanted, it is removed from the list of {@code
 * PageType} objects in the parent {@code MediawikiType} object. Similarly,
 * unwanted {@code RevisionType} objects are removed from the list of {@code
 * RevisionType} objects in the parent {@code PageObject}.
 * </p>
 *
 *
 * <h4>Pre-filter</h4>
 *
 * <p>
 * Pre-filters remove XML content before it is loaded into Java objects.
 * Specifically, pre-filters</em> are <a target="_top"
 * href="http://www.saxproject.org/filters.html">SAX filters</a> applied to the
 * XML stream before it reaches the JAXB unmarshaller. (Also see
 *
 * <a target="_top"
 * href="http://www.ibm.com/developerworks/xml/library/x-tipsaxfilter/">this
 * tutorial</a>.)
 *
 * Pre-filters are essential in cases where some XML {@code <page>} or {@code
 * <revision>} elements contain so much data that they attempting to load them
 * into a single Java object produces an OutOfMemory error. Pre-filters can also
 * be used to reduce the unmarshaler's workload by removing XML content that
 * will obviously be discarded by the post-filter.
 * </p>
 *
 * <p>
 *
 *
 *     <h2> Other Tips </h2>
 * <h3> Obtaining dumps </h3>
 * <p>The English Wiktionary dumps can be found at <a href="http://dumps.wikimedia.org/enwiktionary/"> http://dumps.wikimedia.org/enwiktionary/</a>.
 * Clicking on a date takes you to a web page that describes the different files available.  Clicking on
 * <a href="http://dumps.wikimedia.org/enwiktionary/latest/">latest</a> takes you to a list of files.
 * We have tended to use one of two files:</p>
 * <ul>
 *   <li><code>pages-meta-history.xml.(7z|bz2)</code>: All pages with complete edit history
 *    <li><code>pages-articles.xml.bz2</code>: Current versions of article content.
 * </ul>
 *
 * <p>You can download these files by simply clicking on the link on the web page; but, we have found it
 * more convenient to use <code>curl</code>:</p>
 *
 * <p><code>curl http://dumps.wikimedia.org/enwiktionary/<em>DATE</em>/enwiktionary-<em>DATE</em>-pages-meta-history.xml.bz2 > <em>LOCAL_NAME</em>.xml.bz2</code></p>
 *
 * <p>Where <em>DATE</em> is the date of the dump in <code>YYYYMMDD</code> format, and <em>LOCAL_NAME</em> is the name
 * you want the file to have locally.</p>
 *
 *
 *
 *
 *
 */
package edu.gvsu.cis.kurmasz.zawilinski;

