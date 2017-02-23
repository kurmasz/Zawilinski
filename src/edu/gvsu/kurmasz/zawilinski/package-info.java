/**
 *
 *
 * <code>Zawilinski</code> is a Java library designed specifically to simplify the extraction of
 * data from Wiktionary entries;  more generally, it simplifies the process of loading
 * and filtering MediaWiki XML dumps so that the resulting document object tree is of
 * manageable size (i.e., small enough so that generating and accessing the object tree
 * doesn't cause a typical desktop computer  to "thrash").
 *
 * <h2>Why do I need Zawilinski?</h2>
 *
 * <p>Wiktionary and Wikipedia make their data available for study by publishing very
 * large (tens of gigabytes) XML files.  There are two basic approaches for loading
 * XML files: stream parsers
 * and tree-based parsers.  Stream parsers (e.g., Simple API for XML -- SAX)
 * are efficient; however, writing this type of filter is time consuming and difficult because
 * the programmer has to explicitly keep track of the current position in the XML parse tree
 * and often must write code to buffer previous XML events.</p>
 *
 * Tree-based parsers (e.g., Document Object Model -- DOM) load the
 * entire XML document into an object tree.  Writing code to access data in
 * the object tree is much easier than handling XML events generated by a stream parser; however,
 * a document's object tree is typically 2 to 10 times the
 * size of the document itself.  Thus, it is impractical, if not
 * impossible, to load an entire Wikipedia or Wiktionary XML file into
 * an object tree.  In fact, as a result of vandalism, there are a few Wiktionary articles
 * that are themselves too large to be loaded into an object tree.
 *
 * <h2>How does it work?</h2>
 *
 * <p>The Zawilinksi library combines the efficiency of a stream-based (i.e., SAX) parser with
 * the simplicity of a tree-based (i.e., DOM parser).  It processes documents first with a SAX
 * parser, then a DOM parser.  Users can place filters on both parsers.  The two sets of filters
 * then work together to remove unnecessary data as soon
 * as practical (by which we mean "easy to code"), thereby reducing both the
 * workload of the DOM parser and the memory footprint of the resulting
 * object tree.  (Of course, this is only helpful when the desired analysis
 * requires a sufficiently small subset of the XML data.)</p>
 *
 * <p>Zawilinski is based on three key observations:</p>
 *
 * <ol>
 *
 *  <li>It is not exceptionally difficult to write a SAX filter to filter an article's text.
 *  (More generally, it is not difficult to write a SAX filter for the content of a  "leaf" node
 *  in an XML parse tree.)
 *
 * <li> It is very difficult to write a SAX filter that removes entire articles.  (Removing an
 *  entire article at the stream parser level requires buffering the opening {@code <article>} element as well as all
 *  events that occur between the start of the article and the point at which the filter determines whether to keep
 *  the article.)</li>
 *
 * <li>It is not difficult to write a DOM filter that removes entire articles.  DOM filters have access to the entire
 * article, including meta-data. In addition, removing the entire article entails removing only the articles root
 * node from the document object tree.
 *
 * <li>The vast majority of the data in a Wikitionary or Wikipedia XML dump is the article text (i.e.,
 * the content of the {@code <text>} element).</li>
 *
 * </ol>
 *
 * <p>Zawilinski first applies filters (called "pre-filters") to the SAX parser to remove most of the article content
 * unrelated to the research question.  It then applies filters (called "post-filters") to the DOM parser  to remove
 * the entire article from the object tree.  The pre-filters remove the majority of the unnecessary data,
 * thereby significantly reducing the DOM parser's workload and memory requirements. The post-filters then
 * remove the rest of the article.  Consequently, the DOM parser avoids building large object sub-trees that will
 * soon be discarded, and the programmer avoids having to explicitly buffer XML events to remove articles at the
 * SAX-processing stage.</p>
 *
 * <h3>Example</h3>
 *
 * <p>Here is how we use Zawilinski to support our study of Polish inflection data in Wiktionary:</p>
 *
 * <p>The <a href="http://en.wiktionary.org">English Wiktionary</a> contains English-language descriptions of words
 * in many different languages.  Each article may contain sections for several different languages.  For example,
 * the article for "pies" contains an English section discussing the plural of the word "pie" and a Polish section
 * discussing the Polish word "pies", which means "dog".  For our study of Polish inflection data in Wiktionary,
 * we are interested in only the Polish section of articles.  We are not interested in any articles that do not have a
 * Polish section.</p>
 *
 * <p>To filter a Wiktionary dump for the data relevant to our study, we defined two pre-filters and one
 * post-filter:</p>
 *
 * <dl>
 *    <dt>Language Pre-filter</dt>
 *    <dd>
 *       The language pre-filter retains only the Polish section of an article.  Specifically,
 *       it dumps all article text until it encounters the substring "{@code ==Polish==}".  It then retains all article
 *       data until it encounters the headerContent for the next language section (e.g., "{@code ==Spanish==}").
 *    </dd>
 *
 *    <dt>Size Pre-filter</dt>
 *    <dd>The size pre-filter retains only the first few megabytes of article text. (Several vandalized
 *    articles in Wiktionary's history contain a few hundred million expletives.  The DOM parser can't handle
 *    that much text.) </dd>
 *
 *    <dt>Language Post-filter</dt>
 *    <dd>The language pre-filter removes all text outside the Polish section.  Thus,
 *    articles without a Polish section contain
 *    no text when they reach the post-filter.  The language post-filter simply removes all articles with no article
 *    text from the object tree.  (Of course, this filter assumes that the language filter has been applied.)</dd>
 *    </dl>
 *
 * Notice that the DOM parser still builds object sub-trees for articles that don't contain Polish data.  However,
 * those sub-trees are very small compared to the object sub-tree for an unfiltered article.  Similarly,
 * the size pre-filter prevents vandalized articles from causing "Out of Memory" errors.
 *
 * <h2>Using Zawilinski</h2>
 *
 * {@code Zawilinski} comes with several stand-alone filter programs; or,
 * users can use it as a library to support their custom filters.
 *
 * <h3>Included programs</h3>
 *
 * <ul>
 * <li><code>FilterWiktionaryByLanguage</code>: Filter a Wiktionary XML dump to retain only
 * those pages or revisions that contain information for a specified language.
 * Run {@code java -jar Zawilinski.jar --help} for details.</li>
 * </ul>
 *
 * <h3>Customizing filters</h3>
 *
 * We designed Zawilinski primarily to support the extraction and analysis of grammar data from Wiktionary entries.
 * To use Zawilinski for a different study, users need only:
 *
 * <ol>
 *  <li> Write pre-filter (not always necessary)
 *  <li> Write post-filter
 *  <li> Write code to examine the remaining entries
 * </ol>
 *
 *  <dl>
 *   <dt><b>Write pre-filter</b>:</dt>
 *   <dd>  The pre-filter can be any implementation of
 *        <a href="http://docs.oracle.com/javase/6/docs/api/org/xml/sax/XMLFilter.html"></code>XMLFilter</code></a>
 *        and can modify the XML stream any way it likes (it is not limited to filtering text); however,
 *        as explained earlier, writing a filter for a SAX parser can be very difficult.

 *       <p>
 *       We provide the abstract class {@link edu.gvsu.kurmasz.zawilinski.TextPrefilter} to simplify the process
 *       of filtering article text.  A subclass of {@code TextPrefilter} must define
 *       {@link edu.gvsu.kurmasz.zawilinski.TextPrefilter#handleTextElementCharacters(char[], int, int) }.
 *       This method examines the characters in the array passed to it, then calls
 *       {@link edu.gvsu.kurmasz.zawilinski.TextPrefilter#sendCharacters(char[], int, int)} with
 *       those characters that are to pass through the filter. (See {@link edu.gvsu.kurmasz.zawilinski.TextPrefilter}
 *       for details.)  SAX may pass the article text to the filter over the course of several events.  Thus,
 *       each call to {@code handleTextElementCharacters} may contain only part of the article text.</p>
 *
 *       <p>It is not always necessary to write a pre-filter.  If the articles of interest are small enough and few
 *       enough, then using the {@link edu.gvsu.kurmasz.zawilinski.TextSizePrefilter} as the only pre-filter will be
 *       sufficient.</p>
 * </dd>
 *
 *   <dt><b>Write post-filter</b>:</dt>
 *   <dd>Post-filters decide whether to retain or discard the document object sub-tree for a particular article or
 *   revision.
 *   (Each article may have many revisions.)
 *       A post-filter is an implementation of the {@link edu.gvsu.kurmasz.zawilinski.PostFilter} interface.
 *       This interface defines two methods: {@code keepPage} and {@code keepRevision}.  Writing these methods requires
 *       the programmer to know the <a href="doc-files/mediaWikiImportSchema.png">MediaWiki XML Schema</a> and the
 *       Java classes that correspond to each element in the schema.
 *      (See the {@link edu.gvsu.kurmasz.zawilinski.mw.current} package for details.} <p></p>
 *    </dd>
 *
 *    <dt><b>Analyze remaining entries</b>:</dt>
 *    <dd>
 *       Zawilinski produces a object tree that can then be examined as desired.
 *    </dd>
 * </dl>
 *
 *
 * <h2> Other Tips </h2>
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
 */
package edu.gvsu.kurmasz.zawilinski;
