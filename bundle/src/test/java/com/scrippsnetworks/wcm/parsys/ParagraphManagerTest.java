package com.scrippsnetworks.wcm.parsys.behavior;

import java.util.LinkedList;
import java.util.List;
import java.util.Arrays;
import org.apache.sling.api.resource.Resource;
import com.day.cq.wcm.foundation.Paragraph;
import com.day.cq.wcm.foundation.ParagraphSystem;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.parsys.ParagraphManager;
import com.scrippsnetworks.wcm.parsys.ParagraphSystemContext;
import com.scrippsnetworks.wcm.parsys.ParagraphManagerBehavior;
import com.scrippsnetworks.wcm.parsys.impl.BaseParagraphManager;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ParagraphManagerTest {

    @Mock SniPage sniPage;
    @Mock Resource parsysResource;
    @Mock ParagraphSystem parsys;
    @Mock ParagraphManagerBehavior behavior;
    @Mock Paragraph aParagraph;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        when(parsys.paragraphs()).thenReturn(Arrays.<Paragraph>asList(aParagraph));
    }

    @Test
    public void testNoBehaviors() {
        ParagraphManager paragraphManager = new BaseParagraphManager(sniPage, parsysResource, parsys, null); 
        List<Paragraph> output = paragraphManager.getParagraphs();
        assertNotNull("list nonnull", output);
        assertEquals("correct number of paragraphs", parsys.paragraphs().size(), output.size());
    }

    @Test
    public void testBehaviorsCalled() {
        when(behavior.getParagraphs(isA(ParagraphSystemContext.class), isA(LinkedList.class))).thenReturn(Arrays.<Paragraph>asList(aParagraph));
        ParagraphManager paragraphManager = new BaseParagraphManager(sniPage, parsysResource, parsys, behavior); 
        List<Paragraph> output = paragraphManager.getParagraphs();
        verify(behavior).getParagraphs(isA(ParagraphSystemContext.class), isA(LinkedList.class));
        assertNotNull("list nonnull", output);
        assertEquals("correct number of paragraphs", parsys.paragraphs().size(), output.size());
    }
}
