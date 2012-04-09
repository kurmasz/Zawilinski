package edu.gvsu.kurmasz.zawilinski;

import edu.gvsu.kurmasz.warszawa.log.Log;
import edu.gvsu.kurmasz.zawilinski.mw.current.MediaWikiType;
import edu.gvsu.kurmasz.zawilinski.mw.current.PageType;
import edu.gvsu.kurmasz.zawilinski.mw.current.RevisionType;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

/**
 * @author Zachary Kurmas
 */
// Created  4/6/12 at 5:01 PM
// (C) Zachary Kurmas 2012

public class PageFilterListenerTest {

   private static class CustomPostFilter implements PostFilter {

      private Set<Object> itemsToKeep;

      public CustomPostFilter(Object... items) {
         itemsToKeep = new HashSet<Object>(Arrays.asList(items));
      }

      public boolean keepPage(PageType page) {
         boolean answer= itemsToKeep.contains(page);
         System.out.println("Keeping?  " + answer);
         return answer;
      }

      public boolean keepRevision(RevisionType revision) {
         return itemsToKeep.contains(revision);
      }
   }


   @Test
   public void beforeUnmarshallSetsMediaWiki() throws Throwable {
      MediaWikiType mw = mock(MediaWikiType.class);

      PageFilterListener pfl = new PageFilterListener(PostFilter.KEEP_NONE, mock(Log.class));

      // should just save mw without any interactions
      pfl.beforeUnmarshal(mw, null);
      // Again, shouldn't interact with mw
      pfl.afterUnmarshal(mock(PageType.class), null);
      Mockito.verifyZeroInteractions(mw);

      // should try to remove page
      pfl.beforeUnmarshal(mock(PageType.class), null);
      verify(mw).getPage();
   }

   @Test
   public void beforeUnmarshallAttempsToRemovesPreviousPageIfToBeDeleted() throws Throwable {
      MediaWikiType mw = mock(MediaWikiType.class);
      PageType firstPage = mock(PageType.class);

      PageFilterListener pfl = new PageFilterListener(PostFilter.KEEP_NONE, mock(Log.class));

      // should just save mw without any interactions
      pfl.beforeUnmarshal(mw, null);
      // Again, shouldn't interact with mw
      pfl.afterUnmarshal(firstPage, null);
      Mockito.verifyZeroInteractions(mw);
      Mockito.verifyZeroInteractions(firstPage);

      // should try to remove page
      @SuppressWarnings("unchecked")
      List<PageType> pages = mock(List.class);
      stub(mw.getPage()).toReturn(pages);
      pfl.beforeUnmarshal(mock(PageType.class), null);
      verify(pages).remove(firstPage);
   }

   @Test
   public void beforeUnmarshallDoesNotAttemptToRemovesPreviousPageIfNotToBeDeleted() throws Throwable {
      MediaWikiType mw = mock(MediaWikiType.class);
      PageType firstPage = mock(PageType.class);

      PageFilterListener pfl = new PageFilterListener(PostFilter.KEEP_ALL, mock(Log.class));

      // should just save mw without any interactions
      pfl.beforeUnmarshal(mw, null);
      // Again, shouldn't interact with mw
      pfl.afterUnmarshal(firstPage, null);

      // should try to remove page
      pfl.beforeUnmarshal(mock(PageType.class), null);
      Mockito.verifyZeroInteractions(mw);
      Mockito.verifyZeroInteractions(firstPage);
   }

   @Test
   public void beforeUnmarshallAttempsToRemovePreviousRevisionInPageIfToBeDeleted() throws Throwable {
      MediaWikiType mw = mock(MediaWikiType.class);
      PageType firstPage = mock(PageType.class);
      RevisionType firstRev = mock(RevisionType.class);

      PageFilterListener pfl = new PageFilterListener(PostFilter.KEEP_NONE, mock(Log.class));

      // should just save mw without any interactions
      pfl.beforeUnmarshal(mw, null);
      pfl.beforeUnmarshal(firstPage, null);
      pfl.beforeUnmarshal(firstRev, null);
      pfl.afterUnmarshal(firstRev, null);
      Mockito.verifyZeroInteractions(mw);
      Mockito.verifyZeroInteractions(firstPage);

      // should try to remove page
      @SuppressWarnings("unchecked")
      List<Object> revs = mock(List.class);
      stub(firstPage.getRevisionOrUploadOrLogitem()).toReturn(revs);
      pfl.beforeUnmarshal(mock(RevisionType.class), null);
      verify(revs).remove(firstRev);
   }

   @Test
   public void beforeUnmarshallDoesNotAttempsToRemovesPreviousRevisionIfNotToBeDeleted() throws Throwable {
      MediaWikiType mw = mock(MediaWikiType.class);
      PageType firstPage = mock(PageType.class);
      RevisionType firstRev = mock(RevisionType.class);

      PageFilterListener pfl = new PageFilterListener(PostFilter.KEEP_ALL, mock(Log.class));

      // should just save mw without any interactions
      pfl.beforeUnmarshal(mw, null);
      pfl.beforeUnmarshal(firstPage, null);
      pfl.afterUnmarshal(firstRev, null);
      Mockito.verifyZeroInteractions(mw);


      pfl.beforeUnmarshal(mock(RevisionType.class), null);
      verifyZeroInteractions(firstPage);
      verifyZeroInteractions(mw);
   }


   @Test
   public void afterUnmarshallAttemptsToRemoveLastPageIfDesired() throws Throwable {
      MediaWikiType mw = mock(MediaWikiType.class);
      PageType firstPage = mock(PageType.class);

      PageFilterListener pfl = new PageFilterListener(PostFilter.KEEP_NONE, mock(Log.class));

      // should just save mw without any interactions
      pfl.beforeUnmarshal(mw, null);
      // Again, shouldn't interact with mw
      pfl.afterUnmarshal(firstPage, null);
      Mockito.verifyZeroInteractions(mw);

      // should try to remove page
      @SuppressWarnings("unchecked")
      List<PageType> pages = mock(List.class);
      stub(mw.getPage()).toReturn(pages);
      pfl.afterUnmarshal(mw, null);
      verify(pages).remove(firstPage);
   }

   @Test
   public void afterUnmarshallAttempsToRemoveLastRevisionInPageIfToBeDeleted() throws Throwable {
      MediaWikiType mw = mock(MediaWikiType.class);
      PageType firstPage = mock(PageType.class);
      RevisionType firstRev = mock(RevisionType.class);

      PageFilterListener pfl = new PageFilterListener(PostFilter.KEEP_NONE, mock(Log.class));

      // should just save mw without any interactions
      pfl.beforeUnmarshal(mw, null);
      pfl.beforeUnmarshal(firstPage, null);
      pfl.beforeUnmarshal(firstRev, null);
      pfl.afterUnmarshal(firstRev, null);
      Mockito.verifyZeroInteractions(mw);
      Mockito.verifyZeroInteractions(firstPage);
      Mockito.verifyZeroInteractions(firstRev);

      // should try to remove page
      @SuppressWarnings("unchecked")
      List<Object> revs = mock(List.class);
      stub(firstPage.getRevisionOrUploadOrLogitem()).toReturn(revs);
      pfl.afterUnmarshal(firstPage, null);
      verify(revs).remove(firstRev);
   }

   @Test
   public void afterUnmarshallResetsPageToDelete() throws Throwable {
      MediaWikiType mw = mock(MediaWikiType.class);
      PageType firstPage = mock(PageType.class);
      PageType secondPage = mock(PageType.class);
      PageType thirdPage = mock(PageType.class);

      CustomPostFilter cps = new CustomPostFilter(secondPage);
      PageFilterListener pfl = new PageFilterListener(cps, mock(Log.class));
      pfl.beforeUnmarshal(mw, null);
      pfl.beforeUnmarshal(firstPage, null);
      pfl.afterUnmarshal(firstPage, null);
      pfl.beforeUnmarshal(secondPage, null);
      pfl.afterUnmarshal(secondPage, null);
      pfl.beforeUnmarshal(thirdPage, null);
      verify(mw, times(1)).getPage();
   }
}
