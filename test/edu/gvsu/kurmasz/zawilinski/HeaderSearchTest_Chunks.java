package edu.gvsu.kurmasz.zawilinski;

/**
 * This test re-runs the tests in HeaderSearchTest, but uses many smaller
 * calls to process
 *
 * @author Zachary Kurmas
 */
// Created  4/9/12 at 3:42 PM
// (C) Zachary Kurmas 2012

public class HeaderSearchTest_Chunks {

   public static class Chunks1Test extends HeaderSearchTest {
      protected void verify_process(HeaderSearch headerSearch, String s, String expectedHeader,
                                    int expectedPlace) {
         verify_process_chunks(headerSearch, s, expectedHeader, expectedPlace, new int[]{1});
      }
   }

   public static class Chunk2Test extends HeaderSearchTest {
      protected void verify_process(HeaderSearch headerSearch, String s, String expectedHeader,
                                    int expectedPlace) {
         verify_process_chunks(headerSearch, s, expectedHeader, expectedPlace, new int[]{2});
      }
   }

   public static class Chunks3Test extends HeaderSearchTest {
      protected void verify_process(HeaderSearch headerSearch, String s, String expectedHeader,
                                    int expectedPlace) {
         verify_process_chunks(headerSearch, s, expectedHeader, expectedPlace, new int[]{3});
      }
   }

   public static class Chunks7Test extends HeaderSearchTest {
      protected void verify_process(HeaderSearch headerSearch, String s, String expectedHeader,
                                    int expectedPlace) {
         verify_process_chunks(headerSearch, s, expectedHeader, expectedPlace, new int[]{7});
      }
   }

   public static class Chunks315Test extends HeaderSearchTest {
      protected void verify_process(HeaderSearch headerSearch, String s, String expectedHeader,
                                    int expectedPlace) {
         verify_process_chunks(headerSearch, s, expectedHeader, expectedPlace, new int[]{3, 1, 5});
      }
   }
}
