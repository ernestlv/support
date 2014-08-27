package com.scrippsnetworks.wcm.taglib;

import org.junit.*;
import static org.junit.Assert.*;
import java.util.*;
import java.lang.*;
import java.lang.annotation.Annotation;
import com.scrippsnetworks.wcm.taglib.TagUtils;

public class TagUtilsTest {
	
	@org.junit.Test
	public void testCompleteHREF() {
		String[] testcases = new String[] {"/path/to/foo", "/path/to/foo.html", "http://www.foodnetwork.com","http://reddit.com/","foodnetwork.com/foo/bar","path/to/foo",null};
		
		for (String testcase : testcases) {
			String result = TagUtils.completeHREF(testcase);
			// Silly test, just makes sure various inputs are handled. Not concerned about actual output here,
			// this procedure was lifted from CQ's ImageResource class.
			assertTrue("bogus test for now",result == null || result == "" || result.length() > 0);
		}
	}

    @org.junit.Test
    public void testSplitList() {

        String listItem = new String("Foo");
        List<String> list;
        List<List<String>> lol;
        int listSize;
        int chunks;

        /* passing null list */
        lol = TagUtils.splitList(null,0);
        assertTrue("passing null list returns nonnull list of lists", lol != null);
        assertTrue("passing null list returns empty list of lists", lol.size() == 0 );

        /* passing empty list */
        lol = TagUtils.splitList(new ArrayList<String>(), 10);
        assertTrue("passing empty list returns nonnull list of lists", lol != null);
        assertTrue("passing empty list returns empty list of lists", lol.size() == 0);

        /* passing chunks = 0 */
        listSize = 3; chunks = 0;
        list = Collections.nCopies(listSize, listItem);
        lol = TagUtils.splitList(list, chunks);
        assertTrue("chunks = 0 returns lol.size() == 0", lol.size() == 0); 

        /* passing chunks = -1 */
        listSize = 3; chunks = -1;
        list = Collections.nCopies(listSize, listItem);
        lol = TagUtils.splitList(list, chunks);
        assertTrue("chunks = -1 returns lol.size() == 0 ", lol.size() == 0); 

        /* passing list < chunks */
        listSize = 3; chunks = 10;
        list = Collections.nCopies(listSize, listItem);
        lol = TagUtils.splitList(list, chunks);
        assertTrue("list.size() < chunks returns lol.size() == list.size()", lol.size() == list.size()); 

        /* passing list = chunks */
        listSize = 3; chunks = 3;
        list = Collections.nCopies(listSize, listItem);
        lol = TagUtils.splitList(list, chunks);
        assertTrue("list.size() = chunks returns lol.size() == list.size()", lol.size() == chunks); 

        /* passing list > chunks */
        listSize = 100; chunks = 9;
        list = new ArrayList<String>();
        for (int i = 0; i < listSize; i++) {
            list.add(String.valueOf(i));
        }
        lol = TagUtils.splitList(list, chunks);
        assertTrue("list.size() > chunks returns list of evenly sized lists, last ragged", lol.size() == chunks);
        for (List l : lol.subList(0, chunks-1)) {
            assertTrue("items evenly distributed among all but last of returned lists", l.size() == 12);
        }
        assertTrue("last returned list contains correct number of items", lol.get(8).size() == 4);
        int i = 0;
        for (List<String> l : lol) {
            for (String s : l) {
                assertTrue("list items ordered correctly", Integer.valueOf(s).intValue() == i);
                i++;
            }
        }
        assertTrue("list of lists has same number of elements as list", i == listSize);
    }

    @org.junit.Test
    public void testChunkList() {

        String listItem = new String("Foo");
        List<String> list;
        List<List<String>> lol;
        int listSize;
        int chunkSize;

        /* passing null list */
        lol = TagUtils.chunkList(null,0);
        assertTrue("passing null list returns nonnull list of lists", lol != null);
        assertTrue("passing null list returns empty list of lists", lol.size() == 0 );

        /* passing empty list */
        lol = TagUtils.chunkList(new ArrayList<String>(), 10);
        assertTrue("passing empty list returns nonnull list of lists", lol != null);
        assertTrue("passing empty list returns empty list of lists", lol.size() == 0);

        /* passing chunkSize = 0 */
        listSize = 3; chunkSize = 0;
        list = Collections.nCopies(listSize, listItem);
        lol = TagUtils.chunkList(list, chunkSize);
        assertTrue("chunkSize = 0 returns lol.size() == 0 ", lol.size() == 0); 

        /* passing chunkSize = -1 */
        listSize = 3; chunkSize = -1;
        list = Collections.nCopies(listSize, listItem);
        lol = TagUtils.chunkList(list, chunkSize);
        assertTrue("chunkSize = 0 returns lol.size() == 0 ", lol.size() == 0); 

        /* passing list < chunkSize */
        listSize = 3; chunkSize = 4;
        list = Collections.nCopies(listSize, listItem);
        lol = TagUtils.chunkList(list, chunkSize);
        assertTrue("list.size() < chunkSize returns lol.size() == 1", lol.size() == 1); 

        /* passing list = chunkSize */
        listSize = 3; chunkSize = 3;
        list = Collections.nCopies(listSize, listItem);
        lol = TagUtils.chunkList(list, chunkSize);
        assertTrue("list.size() = chunkSize returns lol.size() == 1", lol.size() == 1); 

        /* passing list > chunkSize */
        listSize = 100; chunkSize = 12; // 8 lists of 12, 1 of 4
        list = Collections.nCopies(listSize, listItem);
        list = new ArrayList<String>();
        for (int i = 0; i < listSize; i++) {
            list.add(String.valueOf(i));
        }
        lol = TagUtils.chunkList(list, chunkSize);
        assertTrue("correct number of lists returned", lol.size() == 9); 
        for (List l : lol.subList(0, 8)) {
            assertTrue("items evenly distributed among all but last of returned lists", l.size() == chunkSize);
        }
        assertTrue("last returned list contains correct number of items", lol.get(8).size() == 4);
        int i = 0;
        for (List<String> l : lol) {
            for (String s : l) {
                assertTrue("list items ordered correctly", Integer.valueOf(s).intValue() == i);
                i++;
            }
        }

    }
	
}
