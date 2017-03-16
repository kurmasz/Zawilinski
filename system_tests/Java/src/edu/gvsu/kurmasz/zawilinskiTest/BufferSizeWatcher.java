package edu.gvsu.kurmasz.zawilinskiTest;

import edu.gvsu.kurmasz.warszawa.io.InputHelper;
import edu.gvsu.kurmasz.warszawa.log.SimpleLog;
import edu.gvsu.kurmasz.zawilinski.PostFilter;
import edu.gvsu.kurmasz.zawilinski.PostFilteredMediaWikiLoader;
import edu.gvsu.kurmasz.zawilinski.TextPrefilter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.TreeMap;

/**
 * Create a histogrm of SAX parser buffer lengths
 */
public class BufferSizeWatcher {

    /**
     * Create a histogram of buffer lengths
     */
    public static class BufferSizePrefilter extends TextPrefilter {

        private Map<Integer, Integer> sizeMap;
        private PrintStream output;
        private int max = -1;

        public BufferSizePrefilter(Map<Integer, Integer> sizeMap, PrintStream output) {
            super();
            this.sizeMap = sizeMap;
            this.output = output;
        }

        @Override
        protected void handleStartTextElement(String s, String s1, String s2, Attributes attributes) throws SAXException {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        protected void handleTextElementCharacters(char[] chars, int start, int length) throws SAXException {
            if (length > max) {
                max = length;
            }
            if (output != null) {
                //output.printf("%5d -- %s\n", length, new String(chars, start, length));
                output.printf("%7d -- %d\n", length, max);
            }
            if (sizeMap.containsKey(length)) {
                sizeMap.put(length, sizeMap.get(length) + 1);
            } else {
                sizeMap.put(length, 1);
            }
            super.sendCharacters(chars, start, length);
        }

        @Override
        protected void handleEndTextElement(String s, String s1, String s2) throws SAXException {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    public static void main(String[] args) throws JAXBException {

        if (args.length == 0) {
            System.err.println("Usage:  BufferSizeWatcher file");
            System.exit(1);
        }
        TreeMap<Integer, Integer> histogram = new TreeMap<Integer, Integer>();
        BufferSizePrefilter bsp = new BufferSizePrefilter(histogram, null);
        InputStream input = InputHelper.openMappedAndFilteredInputStreamOrQuit(args[0]);
        PostFilteredMediaWikiLoader.load(input, new SimpleLog(), PostFilter.KEEP_NONE, bsp);

        for (Map.Entry<Integer,Integer> entry : histogram.entrySet()) {
            System.out.printf("%8d %8d\n", entry.getKey(), entry.getValue());
        }
    }


}
