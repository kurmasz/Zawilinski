package edu.gvsu.kurmasz.zawilinski;

import edu.gvsu.kurmasz.zawilinski.mw.current.*;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Zachary Kurmas
 */
// Created  1/24/12 at 1:03 PM
// (C) Zachary Kurmas 2012

public class SampleContentCheck {

  public static void verifyMWSampleContent(MediaWikiType doc) {
    String[][] expected = {
        {"Content for rev 1", "Content for rev 2 (pickles)"},
        {"Content for rev 1 of page 2 (blueberries)", "Content for rev 2 of page 2",
            "Content for rev 3 of page 2 (carrots)"}
    };
    verifyMWSampleContentText(doc, expected);
    verifyMWSampleContentOther(doc);
  }

  public static void verifyMWSampleContent_8fore(MediaWikiType doc) {
    String[][] expected = {
        {"Cont8nt for r8v 1", "Cont8nt for r8v 2 (pickl8s)"},
        {"Cont8nt for r8v 1 of pag8 2 (blu8b8rri8s)", "Cont8nt for r8v 2 of pag8 2",
            "Cont8nt for r8v 3 of pag8 2 (carrots)"}
    };
    verifyMWSampleContentText(doc, expected);
    verifyMWSampleContentOther(doc);
  }

  public static void verifyMWSampleContent_8foreoa(MediaWikiType doc) {
    String[][] expected = {
        {"C8nt8nt f8r r8v 1", "C8nt8nt f8r r8v 2 (pickl8s)"},
        {"C8nt8nt f8r r8v 1 8f p8g8 2 (blu8b8rri8s)", "C8nt8nt f8r r8v 2 8f p8g8 2",
            "C8nt8nt f8r r8v 3 8f p8g8 2 (c8rr8ts)"}
    };
    verifyMWSampleContentText(doc, expected);
    verifyMWSampleContentOther(doc);
  }


  public static void verifyMWSampleContentText(MediaWikiType doc, String[][] expected) {
    int pageNum = 0;
    for (PageType page : doc.getPage()) {
      int revNum = 0;
      for (Object rawRev : page.getRevisionOrUpload()) {
        RevisionType rev = (RevisionType) (rawRev);
        String observed = rev.getText().getValue();
        assertEquals("Checking page " + pageNum + " revision " + revNum,
            expected[pageNum][revNum], observed);
        revNum++;
      }
      pageNum++;
    }
  }

  public static void verifyMWSampleContentTextSqueeze(MediaWikiType doc, String[][] expected) {
    int pageNum = 0;
    assertEquals("NumPages", expected.length, doc.getPage().size());
    for (PageType page : doc.getPage()) {
      int revNum = 0;
      for (Object rawRev : page.getRevisionOrUpload()) {
        RevisionType rev = (RevisionType) (rawRev);
        //System.out.println("=>" + rev.getText().getValue() + "<==");
        String observed = rev.getText().getValue().replaceAll("\n\\s*", "\n");
        assertEquals("Checking page " + pageNum + " revision " + revNum,
            expected[pageNum][revNum], observed);
        revNum++;
      }
      pageNum++;
    }
  }


  public static void verifyMWSampleContentOther(MediaWikiType doc) {

    SiteInfoType siteInfo = doc.getSiteinfo();
    assertEquals("Wiktionary", siteInfo.getSitename());
    assertEquals("MediaWiki 1.29.0-wmf.9", siteInfo.getGenerator());
    assertEquals("case-sensitive", siteInfo.getCase().value());

    List<NamespaceType> namespaces = siteInfo.getNamespaces().getNamespace();
    assertEquals(8, namespaces.size());
    assertEquals("Media", namespaces.get(0).getValue());
    assertEquals(new BigInteger("-2"), namespaces.get(0).getKey());
    assertEquals("Special", namespaces.get(1).getValue());
    assertEquals(new BigInteger("-1"), namespaces.get(1).getKey());

    int last = 7;
    assertEquals("Topic", namespaces.get(last).getValue());
    assertEquals(new BigInteger("2600"), namespaces.get(last).getKey());

    List<PageType> pages = doc.getPage();
    assertEquals(2, pages.size());

    //
    // page 1
    //
    PageType page1 = pages.get(0);
    assertEquals("Sample page 1", page1.getTitle());
    assertEquals(6, page1.getId().intValue());
    List<Object> p1revs = page1.getRevisionOrUpload();
    assertEquals(2, p1revs.size());

    // page 1, revision 1
    RevisionType p1r1 = (RevisionType) p1revs.get(0);
    assertEquals(3, p1r1.getId().intValue());
    XMLGregorianCalendar timestamp = p1r1.getTimestamp();
    assertEquals(2002, timestamp.getYear());
    assertEquals(12, timestamp.getMonth());
    assertEquals(12, timestamp.getDay());
    assertEquals(8, timestamp.getHour());
    assertEquals(06, timestamp.getMinute());
    assertEquals(51, timestamp.getSecond());

    ContributorType contributor = p1r1.getContributor();
    assertEquals("Sjc~enwiktionary", contributor.getUsername());
    assertEquals(2, contributor.getId().intValue());

    assertEquals("/* For more information */ remove non-existing page", p1r1.getComment().getValue());


    // page1 revision 2
    RevisionType p1r2 = (RevisionType) p1revs.get(1);
    assertEquals(4, p1r2.getId().intValue());
    timestamp = p1r2.getTimestamp();
    assertEquals(2002, timestamp.getYear());
    assertEquals(12, timestamp.getMonth());
    assertEquals(12, timestamp.getDay());
    assertEquals(8, timestamp.getHour());
    assertEquals(11, timestamp.getMinute());
    assertEquals(20, timestamp.getSecond());

    contributor = p1r2.getContributor();
    assertEquals("Zachary Kurmas", contributor.getUsername());
    assertEquals(42856, contributor.getId().intValue());

    assertEquals("/* For more information */ remove non-existing page", p1r2.getComment().getValue());


    //
    // page 2
    //
    PageType page2 = pages.get(1);
    assertEquals("Sample page 2", page2.getTitle());
    assertEquals(61, page2.getId().intValue());
    List<Object> p2revs = page2.getRevisionOrUpload();
    assertEquals(3, p2revs.size());

    // page 2, revision 1
    RevisionType p2r1 = (RevisionType) p2revs.get(0);
    assertEquals(21, p2r1.getId().intValue());
    timestamp = p2r1.getTimestamp();
    assertEquals(2009, timestamp.getYear());
    assertEquals(8, timestamp.getMonth());
    assertEquals(10, timestamp.getDay());
    assertEquals(5, timestamp.getHour());
    assertEquals(21, timestamp.getMinute());
    assertEquals(17, timestamp.getSecond());

    contributor = p2r1.getContributor();
    assertEquals("Ivan Štambuk", contributor.getUsername());
    assertEquals(42855, contributor.getId().intValue());

    assertEquals("Another comment", p2r1.getComment().getValue());


    // page 2, revision 3
    RevisionType p2r3 = (RevisionType) p2revs.get(2);
    assertEquals(36, p2r3.getId().intValue());

    contributor = p2r3.getContributor();
    assertEquals("Bugs Bunny", contributor.getUsername());
    assertEquals(42857, contributor.getId().intValue());

    assertEquals("What's up, Doc?", p2r3.getComment().getValue());
  }
}
