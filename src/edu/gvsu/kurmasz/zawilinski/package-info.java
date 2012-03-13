/**
 *
 Zawilinksi is a Java library that simplifies the process of loading
 and filtering MediaWiki XML dumps so that the loaded data is a
 manageable size (i.e., small enough so that generating and accessing a
 DOM doesn't cause a typical desktop computer to "thrash").

 <p>The Zawilinksi library combines the efficiency of a stream parser with
 the simplicity of a DOM parser.  Specifically, it provides two sets of
 filters that allow the programmer to remove unnecessary data as soon
 as practical (by "practical", we mean "easy to code"), thereby reducing both the
 workload of the DOM parser and the memory footprint of the resulting
 DOM.  (Of course, this is only helpful when the desired analysis
 requires a sufficiently small subset of the XML data.)</p>

 <p>Zawilinski processes documents first with a stream parser, then a DOM parser.</p>

 <p>The first set of filters (called "pre-filters") are placed on the
 stream parser.  Pre-filters look for and remove "low-hanging-fruit":
 article text that can be identified as irrelevant without considering
 any data outside the article text.  (Pre-filters do not have access to
 the article's title, author, timestamp, or other meta-data.)  The
 second set of filters (called "post-filters") examine each article's
 DOM object as it is created and decide wither to keep that article in
 the DOM or immediately discard it. Thus, articles that are not
 relevant to the analysis don't continue to consume memory.</p>

 <p>Thus, the goal of the pre-filters is to reduce the load on the DOM parser as
 much possible without requiring complex code; and the goal of the
 post-filter is to provide an easy way for programmers to identify and
 remove entire irrelevant articles from the DOM so as to free up memory
 for the relevant articles. The result is a DOM that contains only
 relevant articles and is small enough to fit comfortably in the RAM of
 a typical desktop PC.</p>
 *
 *
 */
package edu.gvsu.kurmasz.zawilinski;
