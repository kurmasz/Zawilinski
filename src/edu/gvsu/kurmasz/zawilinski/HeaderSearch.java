package edu.gvsu.kurmasz.zawilinski;

/**
 * Helper class to search through multiple groups of characters for a MediaWiki headerContent (e.g.,
 * ==Polish==). Note that this class cannot handle an intermediate '=' in the headerContent.  For example,
 * it won't recognize "==this=that==" as a headerContent.
 *
 * This class has to handle two main challenges:
 * <ol>
 * <li>The headerContent may be broken across several groups of characters</li>
 * <li>The data is "dirty".  We can't assume that the headerContent markers (e.g., "===") will be closed and balanced.</li>
 * </ol>
 *
 * @author Zachary Kurmas
 */

// Created  3/15/12 at 12:26 PM
// (C) Zachary Kurmas 2012

class HeaderSearch {

   /**
    * The result of the current processing step
    */
   public static class Result {

      /**
       * The <em>content</em> of the header (or {@code null} if no header found yet). The content is the "Polish"
       * part of "==Polish=="
       */
      public String headerContent;

      /**
       * The index of the first character is the search string after the full header.
       */
      public int next;

      /**
       * Constructor
       *
       * @param headerContent the text of the headerContent
       * @param next          the index of the location immediately after the full header
       */
      public Result(String headerContent, int next) {
         this.headerContent = headerContent;
         this.next = next;
      }

      /**
       * Build the full header by placing the opening and closing markup around it.
       *
       * @return the full headerContent
       */
      public String fullHeader() {
         return HEADER + this.headerContent + HEADER;
      }
   }

   enum Stage {PRE, OPEN, IN, CLOSE, POST}

   private Stage stage = Stage.PRE;
   private int foundInStage = 0;
   private StringBuffer buffer = new StringBuffer();

   private int openSize;


   // For now, we don't anticipate these values changing.  At some point,
   // we may want to make these regular instance variables. If you do make
   // these regular instance variables, then make sure you add the appropriate
   // tests, because all the tests assume these values are constant.

   // This is the minimum header level.  Thus ==Foo== is considered a header, but =Foo= is not.
   // (Single = is just another character.  Two == is the opening of a header and the algorithm
   // then expects to see a closing ==)
   private static final int MIN_HEADER_LEVEL = 2;

   // These are set to make it difficult to introduce bugs when changing the header character or depth.
   private static final String HEADER = "==";
   private static final int DESIRED_HEADER_LEVEL = HEADER.length();
   private static final char HEADER_CHAR = HEADER.charAt(0);

   private boolean isOpen(char c) {
      return c == HEADER_CHAR;
   }

   private boolean isClose(char c) {
      return c == HEADER_CHAR;
   }

   private boolean isNewline(char c) {
      return c == '\n';
   }

   private boolean isHeader(char c) {
      return !isOpen(c) && !isClose(c) && !isNewline(c);
   }

   private void setStage(Stage newStage) {
      stage = newStage;
      foundInStage = 0;
   }

   private void processPre(char ch) {
      if (isOpen(ch)) {
         setStage(Stage.OPEN);
         processOpen(ch);
      }
   }

   private void processOpen(char ch) {
      if (isOpen(ch)) {
         foundInStage++;
      } else if (foundInStage >= MIN_HEADER_LEVEL) {
         // Make sure we start each headerContent section with a clean buffer
         if (buffer.length() > 0) {
            buffer = new StringBuffer();
         }
         openSize = foundInStage;
         setStage(Stage.IN);
         processIn(ch);
      } else {
         setStage(Stage.PRE);
         processPre(ch);
      }
   }

   private void processIn(char ch) {
      if (isHeader(ch)) {
         foundInStage++;
         buffer.append(ch);
      } else if (isClose(ch)) {
         setStage(Stage.CLOSE);
         processClose(ch);
      } else if (isNewline(ch)) {
         setStage(Stage.PRE);
         processPre(ch);
      }
   }

   private void processClose(char ch) {
      if (isClose(ch)) {
         foundInStage++;
      } else if (foundInStage < MIN_HEADER_LEVEL) {
         for (int i = 0; i < foundInStage; i++) {
            buffer.append(HEADER_CHAR);
         }
         setStage(Stage.IN);
         processIn(ch);
      } else if (foundInStage == DESIRED_HEADER_LEVEL && openSize == DESIRED_HEADER_LEVEL) {
         setStage(Stage.POST);
      } else { // foundInStage != DESIRED_HEADER_LEVEL or openSize != DESIRED_HEADER_LEVEL
         setStage(Stage.PRE);
         processPre(ch);
      }
   }

   private void processChar(char ch) {
      //System.out.printf("char %c -- found in Stage %d -- stage %s\n", ch, foundInStage, stage);
      switch (stage) {
         case PRE:
            processPre(ch);
            return;
         case OPEN:
            processOpen(ch);
            return;
         case IN:
            processIn(ch);
            return;
         case CLOSE:
            processClose(ch);
            return;
      }
   }

   public Result process(char[] ch, int start, int length) {
      for (int i = start; i < start + length; i++) {
         processChar(ch[i]);
         if (stage == Stage.POST) {
            return new Result(buffer.toString(), i);
         }
      }
      return null;
   }
}
