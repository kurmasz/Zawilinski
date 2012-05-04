package edu.gvsu.kurmasz.zawilinski;

import javax.xml.bind.JAXBException;

/**
 * Logging levels and {@link #main(String[])}
 */
public class Zawilinski {

    //////////////////////////////////////////////////
    //
    // Logging levels
    //
    /////////////////////////////////////////////////

    /**
     * Debug level for the beginning and end of parsing.
     */
    public static final int PARSE_BEGIN_END = 500;

    /**
     * Debug level used to list entries truncated by text size
     */
    public static final int TRUNCATIONS = PARSE_BEGIN_END - 10;

    /**
     * Debug level used to list pages dumped by post-filter
     */
    public static final int PAGE_DUMPED = TRUNCATIONS - 10;

    /**
     * Debug level used to list revisions dumped by post-filter
     */
    public static final int REVISION_DUMPED = PAGE_DUMPED - 1;
    /**
     * Debug level used to list pages kept by post-filter
     */
    public static final int PAGE_KEPT = REVISION_DUMPED - 10;

    /**
     * Debug level used to list revisions kept by post-filter
     */
    public static final int REVISION_KEPT = PAGE_KEPT - 1;


    public static void main(String[] args) throws JAXBException {

        if (args.length < 1) {
            System.err.println("Usage:  java -jar zawilinski.jar programName [param1] [param2] ...");
            return;
        }

        String program = args[0];
        String[] otherArgs = new String[args.length -1];
        System.arraycopy(args, 1, otherArgs, 0, args.length -1);

        if (program.equals("Langauge") || program.equals(FilterWiktionaryByLanguage.class.getSimpleName())) {
            FilterWiktionaryByLanguage.main(otherArgs);
        } else if (program.equals("Title") || program.equals(FilterWiktionaryByTitle.class.getSimpleName())) {
            FilterWiktionaryByTitle.main(otherArgs);
        } else {
            System.err.printf("Program \"%s\" not recognized\n", program);
        }
    }
}
