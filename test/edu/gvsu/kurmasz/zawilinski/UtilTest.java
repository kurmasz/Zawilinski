package edu.gvsu.kurmasz.zawilinski;

import edu.gvsu.kurmasz.warszawa.log.Log;
import edu.gvsu.kurmasz.zawilinski.mw.current.*;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class UtilTest {

    @Test
    public void getRevisionTypeReturnsAllRevisionsAndOnlyRevisions() throws Throwable {

        PageType page = mock(PageType.class);

        RevisionType r1 = mock(RevisionType.class);
        RevisionType r2 = mock(RevisionType.class);
        RevisionType r3 = mock(RevisionType.class);
        RevisionType r4 = mock(RevisionType.class);

        ArrayList<Object> bigList = new ArrayList<Object>();
        bigList.add(r1);
        bigList.add(r2);
        bigList.add(mock(UploadType.class));
        bigList.add(r3);
        bigList.add(mock(LogItemType.class));
        bigList.add(r4);

        when(page.getRevisionOrUploadOrLogitem()).thenReturn(bigList);

        List<RevisionType> observed = Util.getRevisions(page);

        List<RevisionType> expected = new ArrayList<RevisionType>();
        expected.add(r1);
        expected.add(r2);
        expected.add(r3);
        expected.add(r4);

        assertEquals(expected, observed);
    }

    @Test
    public void getText_revision() throws Throwable {
        RevisionType revision = mock(RevisionType.class);
        TextType textType = mock(TextType.class);
        String sampleText = "Hello, World!";

        when(textType.getValue()).thenReturn(sampleText);
        when(revision.getText()).thenReturn(textType);

        assertEquals(sampleText, Util.getText(revision));
        verify(textType).getValue();
        verify(revision).getText();
    }

    private MediaWikiType getRoot() throws JAXBException {
        InputStream input = this.getClass().getResourceAsStream("/mw_sample_0.5.xml");
        Assert.assertNotNull("input", input);
        Log log = new Log();

        JAXBElement<MediaWikiType> observed = MediaWikiLoader.load(input, log);
        return observed.getValue();
    }

    @Test
    public void getText_revision_system() throws Throwable {
        MediaWikiType root = getRoot();
        RevisionType rev = (RevisionType) root.getPage().get(1).getRevisionOrUploadOrLogitem().get(1);
        assertEquals("Content for rev 2 of page 2", Util.getText(rev));
    }

    @Test
    public void getTextSize_revision() throws Throwable {
        RevisionType revision = mock(RevisionType.class);
        TextType textType = mock(TextType.class);
        String sampleText = "Hello, World!";

        when(textType.getValue()).thenReturn(sampleText);
        when(revision.getText()).thenReturn(textType);

        assertEquals(sampleText.length(), Util.getTextSize(revision));
        verify(textType).getValue();
        verify(revision).getText();
    }

    @Test
    public void getTextSize_revision_system() throws Throwable {
        MediaWikiType root = getRoot();
        RevisionType rev = (RevisionType) root.getPage().get(1).getRevisionOrUploadOrLogitem().get(1);
        assertEquals("Content for rev 2 of page 2".length(), Util.getTextSize(rev));
    }


    @Test
    public void getTextSize_Page() throws Throwable {
        PageType page = mock(PageType.class);

        RevisionType r1 = mock(RevisionType.class);
        TextType t1 = mock(TextType.class);
        when(t1.getValue()).thenReturn("text 1");
        when(r1.getText()).thenReturn(t1);

        RevisionType r2 = mock(RevisionType.class);
        TextType t2 = mock(TextType.class);
        when(t2.getValue()).thenReturn("text 2 foo bar");
        when(r2.getText()).thenReturn(t2);

        RevisionType r3 = mock(RevisionType.class);
        TextType t3 = mock(TextType.class);
        when(t3.getValue()).thenReturn("t3 32");
        when(r3.getText()).thenReturn(t3);

        List<Object> revs = new ArrayList<Object>();
        revs.add(r1);
        revs.add(r2);
        revs.add(r3);
        when(page.getRevisionOrUploadOrLogitem()).thenReturn(revs);

        assertEquals("size", 25, Util.getTextSize(page));
    }

    @Test
    public void getTextSize_page_system() throws Throwable {
        MediaWikiType root = getRoot();
        PageType page1 = root.getPage().get(0);
        assertEquals(44, Util.getTextSize(page1));

        PageType page2 = root.getPage().get(1);
        assertEquals(105, Util.getTextSize(page2));
    }


    @Test
    public void getText_page() throws Throwable {
        PageType page = mock(PageType.class);

        RevisionType r1 = mock(RevisionType.class);
        TextType t1 = mock(TextType.class);
        when(t1.getValue()).thenReturn("text 1");
        when(r1.getText()).thenReturn(t1);

        RevisionType r2 = mock(RevisionType.class);
        TextType t2 = mock(TextType.class);
        when(t2.getValue()).thenReturn("text 2 foo bar");
        when(r2.getText()).thenReturn(t2);

        RevisionType r3 = mock(RevisionType.class);
        TextType t3 = mock(TextType.class);
        when(t3.getValue()).thenReturn("t3 32");
        when(r3.getText()).thenReturn(t3);

        List<Object> revs = new ArrayList<Object>();
        revs.add(r1);
        revs.add(r2);
        revs.add(r3);
        when(page.getRevisionOrUploadOrLogitem()).thenReturn(revs);

        assertEquals("R1", "text 1", Util.getText(page, 0));
        assertEquals("R2", "text 2 foo bar", Util.getText(page, 1));
        assertEquals("R3", "t3 32", Util.getText(page, 2));
    }

    @Test
    public void getText_page_system() throws Throwable {
        MediaWikiType root = getRoot();
        PageType page1 = root.getPage().get(0);
        assertEquals("Content for rev 1", Util.getText(page1, 0));

        PageType page2 = root.getPage().get(1);
        assertEquals("Content for rev 3 of page 2 (carrots)", Util.getText(page2, 2));
    }

    @Test
    public void setText() throws Throwable {
        RevisionType r2 = mock(RevisionType.class);
        TextType t2 = mock(TextType.class);
        when(r2.getText()).thenReturn(t2);

        String newText = "the new text";

        Util.setText(r2, newText);
        verify(r2).getText();
        verify(t2).setValue(newText);
    }

    @Test
    public void setText_system() throws Throwable {
        String newText = "I love radishes!";
        MediaWikiType root = getRoot();
        RevisionType rev = (RevisionType) root.getPage().get(1).getRevisionOrUploadOrLogitem().get(0);
        Util.setText(rev, newText);

        assertEquals(newText, rev.getText().getValue());
        assertEquals(newText, Util.getText(root.getPage().get(1),0));
    }


}
