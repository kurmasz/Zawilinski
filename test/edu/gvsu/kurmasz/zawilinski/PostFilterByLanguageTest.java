package edu.gvsu.kurmasz.zawilinski;

import edu.gvsu.kurmasz.warszawa.log.Log;
import edu.gvsu.kurmasz.zawilinski.mw.current.MediaWikiType;
import edu.gvsu.kurmasz.zawilinski.mw.current.PageType;
import edu.gvsu.kurmasz.zawilinski.mw.current.RevisionType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;

import org.powermock.modules.junit4.PowerMockRunner;

import javax.xml.bind.JAXBElement;

import java.io.InputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Util.class})

public class PostFilterByLanguageTest {

    private static final String SAMPLE_TEXT = "==Target Language== text text text \n text text text.";
    private static final String SAMPLE_TEXT_WITH_TRAILER = SAMPLE_TEXT + "==Trailer==";

    @Test
    public void keepPageReturnsTrueIfSizeIsNonzero() throws Throwable {
        mockStatic(Util.class);

        PageType page = mock(PageType.class);
        PostFilterByLanguage pfl = new PostFilterByLanguage("someLanguage");

        when(Util.getTextSize(page)).thenReturn(new Long("1"));

        assertTrue(pfl.keepPage(page));
    }

    @Test
    public void keepPageReturnsFalseIfSizeIsZero() throws Throwable {
        mockStatic(Util.class);

        PageType page = mock(PageType.class);
        PostFilterByLanguage pfl = new PostFilterByLanguage("someLanguage");

        when(Util.getTextSize(page)).thenReturn(new Long("0"));

        assertFalse(pfl.keepPage(page));
    }

    @Test
    public void keepRevisionReturnsTrueIfTextLengthIsNonzeroAndNoTrailer() throws Throwable {
        mockStatic(Util.class);

        RevisionType revision = mock(RevisionType.class);
        PostFilterByLanguage pfl = new PostFilterByLanguage("someLanguage");

        when(Util.getText(revision)).thenReturn(SAMPLE_TEXT);

        assertTrue(pfl.keepRevision(revision, mock(PageType.class)));
    }

    @Test
    public void keepRevisionReturnsTrueIfTextLengthIsNonzeroAndTrailer() throws Throwable {
        mockStatic(Util.class);

        RevisionType revision = mock(RevisionType.class);
        PostFilterByLanguage pfl = new PostFilterByLanguage("someLanguage");

        when(Util.getText(revision)).thenReturn(SAMPLE_TEXT_WITH_TRAILER);

        assertTrue(pfl.keepRevision(revision, mock(PageType.class)));
    }


    @Test
    public void keepRevisionReturnsFalseIfTextLengthIsZero() throws Throwable {
        mockStatic(Util.class);

        RevisionType revision = mock(RevisionType.class);
        PostFilterByLanguage pfl = new PostFilterByLanguage("someLanguage");

        when(Util.getText(revision)).thenReturn("");

        assertFalse(pfl.keepRevision(revision, mock(PageType.class)));
    }


    @Test
    public void keepRevisionRemovesTrailingHeaderIfPresent() throws Throwable {
        mockStatic(Util.class);

        RevisionType revision = mock(RevisionType.class);
        PostFilterByLanguage pfl = new PostFilterByLanguage("someLanguage");

        when(Util.getText(revision)).thenReturn(SAMPLE_TEXT_WITH_TRAILER);
        pfl.keepRevision(revision, mock(PageType.class));

        verifyStatic();
        Util.setText(revision, SAMPLE_TEXT);
    }

    @Test
    public void keepRevisionsDoesNotModifyTextIfNoTrailerPresent() throws Throwable {
        mockStatic(Util.class);

        RevisionType revision = mock(RevisionType.class);
        PostFilterByLanguage pfl = new PostFilterByLanguage("someLanguage");

        when(Util.getText(revision)).thenReturn(SAMPLE_TEXT);

        pfl.keepRevision(revision, mock(PageType.class));

        verifyStatic(never());
        Util.setText(revision, SAMPLE_TEXT);
    }
}
