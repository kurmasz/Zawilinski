package edu.gvsu.kurmasz.zawilinski;

import edu.gvsu.kurmasz.zawilinski.mw.current.PageType;
import edu.gvsu.kurmasz.zawilinski.mw.current.RevisionType;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author Zachary Kurmas
 */
// Created  4/6/12 at 4:56 PM
// (C) Zachary Kurmas 2012

public class PostFilterTest {

   @Test
   public void keepAllPageReturnsTrue() throws Throwable {
      assertTrue(PostFilter.KEEP_ALL.keepPage(mock(PageType.class)));
   }

   @Test
   public void keepAllRevisionReturnsTrue() throws Throwable {
      assertTrue(PostFilter.KEEP_ALL.keepRevision(mock(RevisionType.class), mock(PageType.class)));
   }

   @Test
   public void keepNoPageReturnsFalse() throws Throwable {
      assertFalse(PostFilter.KEEP_NONE.keepPage(mock(PageType.class)));
   }

   @Test
   public void keepNoRevisionReturnsFalse() throws Throwable {
      assertFalse(PostFilter.KEEP_NONE.keepRevision(mock(RevisionType.class), mock(PageType.class)));
   }
}

