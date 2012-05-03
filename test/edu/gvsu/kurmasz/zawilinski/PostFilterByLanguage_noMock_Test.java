package edu.gvsu.kurmasz.zawilinski;

import edu.gvsu.kurmasz.warszawa.log.Log;
import edu.gvsu.kurmasz.zawilinski.mw.current.MediaWikiType;
import org.junit.Test;

import javax.xml.bind.JAXBElement;
import java.io.InputStream;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class PostFilterByLanguage_noMock_Test {
    @Test
    public void testAll() throws Throwable {
        final String language = "Polish";
        InputStream input = this.getClass().getResourceAsStream("/testLanguageFilter.xml");
        assertNotNull(input);
        LanguagePrefilter lpf = new LanguagePrefilter(language);
        PostFilterByLanguage postFilter = new PostFilterByLanguage();
        JAXBElement<MediaWikiType> root = PostFilteredMediaWikiLoader.load(input, mock(Log.class), postFilter, lpf);

        String[][] expected = {

                // Word 1
                {
                        "==Polish==\nThis is the Polish content\nA second line of Polish content\n",
                        "==Polish==This is the r2 Polish content\nA second line of Polish content (from rev 2)" +
                                "\n",
                },

                // Word 2
                {
                        "==Polish==\nWord 2 polish content\n",
                        "==Polish==\nWord 2 polish content (mod)",
                        "==Polish==\nWord 2 polish content (mod 2)",
                        ""
                }
        };
        SampleContentCheck.verifyMWSampleContentTextSqueeze(root.getValue(), expected);
    }
}
