package com.scrippsnetworks.wcm.taglib;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.*;
import java.lang.*;
import java.lang.annotation.Annotation;

import com.scrippsnetworks.wcm.taglib.Functions;
import com.scrippsnetworks.wcm.taglib.TagUtils;

import org.apache.sling.commons.testing.sling.*;

public class FunctionsTest {
	
	@org.junit.Test
	public void testGetResourceChild() {
		MockResourceResolver mrr = new MockResourceResolver();
		mrr.setSearchPath("/");
		MockResource mr1 = new MockResource(mrr, "/foo","sni-wcm/components/module/foo");
		MockResource mr2 = new MockResource(mrr, "/foo/bar", "sni-wcm/components/module/bar");
		MockResource mr3 = new MockResource(mrr, "/foo/baz", "sni-wcm/components/module/baz");

		mrr.addResource(mr1);
		mrr.addResource(mr2);
		mrr.addResource(mr3);

		// Test we got the child resource back with valid resource and child.
		Resource child = Functions.getResourceChild(mr1, "bar");
		assertTrue("existing resource returned","/foo/bar".equals(child.getPath()));
		
		// should return child baz
		child = Functions.getResourceChild(mr1, "baz");
		assertTrue("existing resource returned","/foo/baz".equals(child.getPath()));
		
		// nonnull resource, nonnull but nonexisting resource: return NonExistingResource
		child = Functions.getResourceChild(mr1, "foobar");
		assertTrue("returns null resource for valid parent", child == null);

		// null child name: return null
		child = Functions.getResourceChild(mr1, null);
		assertTrue("null child name returns null",child == null);
		
		// null resource, nonnull child path: null response (can't create NonExistingResource without full path)
		child = Functions.getResourceChild(null,"foobar");
		assertTrue("null parent resource returns null",child == null);

		// both arguments null: null response
		child = Functions.getResourceChild(null,null);
		assertTrue("both null parent and null child returns null",child == null);		
	}
	
	@org.junit.Test
	public void testGetResourceChildIterator() {
		MockResourceResolver mrr = new MockResourceResolver();
		mrr.setSearchPath("/");
		MockResource mr1 = new MockResource(mrr, "/foo","sni-wcm/components/module/foo");
		MockResource mr2 = new MockResource(mrr, "/foo/bar", "sni-wcm/components/module/bar");
		MockResource mr3 = new MockResource(mrr, "/foo/baz", "sni-wcm/components/module/baz");

		mrr.addResource(mr1);
		mrr.addResource(mr2);
		mrr.addResource(mr3);
		Resource child;
		
		// Test the right number of children is returned
		Iterator<Resource> resIt = Functions.getResourceChildIterator(mr1);
		assertTrue("returns nonnull child iterator", resIt != null);
		ArrayList<Resource> arr = new ArrayList<Resource>();
		while (resIt.hasNext()) {
			arr.add(resIt.next());
			
		}
		assertTrue("iterator returns children", arr.size() == 2);
		
		// test using null, want empty list of children
		resIt = Functions.getResourceChildIterator(null);
		assertTrue("null returns nonnull child iterator", resIt != null);
		arr.clear();	
		while (resIt.hasNext()) {
			arr.add(resIt.next());
		}
		assertTrue("null returns empty list of children", arr.size() == 0);		
	}

	@org.junit.Test
    public void testSplitStringArrayProperty() {
        MockResource res = new MockResource(new MockResourceResolver(),"/foo", "bar");
        ValueMap properties = res.adaptTo(ValueMap.class);
        String[] teststrings = {"foo", "bar", "baz"};

        List<List<String>> retlist;
        retlist = Functions.splitStringArrayProperty(null, "foobar", 2);
        assertTrue("null properties argument returns nonnull list", retlist != null);
        assertTrue("null properties argument returns empty list", retlist.size() == 0);

        retlist = Functions.splitStringArrayProperty(properties, "foobar", 2);
        assertTrue("empty properties argument returns nonnull list", retlist != null);
        assertTrue("empty properties argument returns empty list", retlist.size() == 0);

        retlist = Functions.splitStringArrayProperty(properties, null, 2);
        assertTrue("null name argument returns nonnull list", retlist != null);
        assertTrue("null name argument returns empty list", retlist.size() == 0);

        retlist = Functions.splitStringArrayProperty(properties, "", 2);
        assertTrue("empty name argument returns nonnull list", retlist != null);
        assertTrue("empty name argument returns empty list", retlist.size() == 0);

        properties.put("foobar", teststrings);

        retlist = Functions.splitStringArrayProperty(properties, "foobar", -1);
        assertTrue("negative chunks argument returns nonnull list", retlist != null);
        assertTrue("negative chunks argument returns empty list", retlist.size() == 0);

        retlist = Functions.splitStringArrayProperty(properties, "foobar", 0);
        assertTrue("zero chunks argument returns nonnull list", retlist != null);
        assertTrue("zero chunks argument returns empty list", retlist.size() == 0);

        retlist = Functions.splitStringArrayProperty(properties, "whatever", 0);
        assertTrue("non-existent property returns nonnull list", retlist != null);
        assertTrue("non-existent property eturns empty list", retlist.size() == 0);

        properties.put("foobar", teststrings);
        retlist = Functions.splitStringArrayProperty(properties, "foobar", 2);
        assertTrue("nonempty list returned", retlist.size() > 0);
        String[] retarr = new String[teststrings.length];
        int i = 0;
        for (List<String> col : retlist) {
            for (String s : col) {
                retarr[i++] = s;
            }
        }
        assertTrue("reconstructed array is the same", Arrays.equals(teststrings, retarr));


    }

}
