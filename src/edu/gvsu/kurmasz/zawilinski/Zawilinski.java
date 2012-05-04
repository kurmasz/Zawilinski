package edu.gvsu.kurmasz.zawilinski;

import edu.gvsu.kurmasz.warszawa.util.BuildInfo;

/**
 * Constants shared by entire Zawilinski library
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

   public static void main(String[] args) {
      BuildInfo bi = BuildInfo.make(Zawilinski.class);
      System.out.println("Zawilinski version " + bi.getVersion() + " built "
            + bi.getBuildDate() + ".");
   }
}
