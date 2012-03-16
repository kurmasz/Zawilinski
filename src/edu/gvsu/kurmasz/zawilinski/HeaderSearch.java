package edu.gvsu.kurmasz.zawilinski;

/**
 * Helper class to search through multiple groups of characters for a MediaWiki header (e.g., ==Polish==).
 *
 * This class has to handle two main challenges:
 * <ol>
 * <li>The header may be broken across several groups of characters</li>
 * <li>The data is "dirty".  We can't assume that the header markers (e.g., "===") will be closed and balanced.</li>
 * </ol>
 *
 * @author Zachary Kurmas
 */

// Created  3/15/12 at 12:26 PM
// (C) Zachary Kurmas 2012

public class HeaderSearch {

   public static class Result {
      public String header;
      public int next;

      public Result(String header, int next) {
         this.header = header;
         this.next = next;
      }
   }

   enum Stage {PRE, OPEN, IN, CLOSE, POST}

   private Stage stage = Stage.PRE;
   private int foundInStage = 0;
   private StringBuffer buffer = new StringBuffer();

   private int openSize;


   // For now, we don't see these values changing.  At some point, we may want to make these regular instance variables.
   // If you do make these regular instance variables, then make sure you add the appropriate tests, because all the tests
   // assume these values are constant.
   private static final int MIN_HEADER_LEVEL = 2;
   private static final int DESIRED_HEADER_LEVEL = 2;
   private static final char HEADER_CHAR = '=';

   protected boolean isOpen(char c) {
      return c == HEADER_CHAR;
   }

   protected boolean isClose(char c) {
      return c == HEADER_CHAR;
   }

   private boolean isNewline(char c) {
      return c == '\n';
   }

   protected boolean isHeader(char c) {
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
         // Make sure we start each header section with a clean buffer
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
         throw new IllegalArgumentException("This case hasn't been handled yet");
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
