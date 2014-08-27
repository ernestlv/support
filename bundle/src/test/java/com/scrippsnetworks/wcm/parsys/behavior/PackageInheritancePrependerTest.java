package com.scrippsnetworks.wcm.parsys.behavior;

import java.util.LinkedList;
import java.util.Arrays;
import org.apache.sling.api.resource.Resource;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.foundation.Paragraph;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.snipackage.SniPackage;
import com.scrippsnetworks.wcm.parsys.ParagraphSystemContext;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PackageInheritancePrependerTest {

    public static final String REGION_NAME = "superlead";
    public static final String PARSYS_PATH = "/content/food/how-to/how-to-test-packages/"
        + JcrConstants.JCR_CONTENT + "/" + REGION_NAME;
    public static final String ANCHOR_PATH = "/content/food/shows/a/a-show";
    public static final String SHARE_PARENT_PARSYS_PATH = ANCHOR_PATH + "/" + JcrConstants.JCR_CONTENT + "/" + REGION_NAME;
    public static final String SHARED1_PATH = SHARE_PARENT_PARSYS_PATH + "/superlead-awesome";
    public static final String SHARED2_PATH = SHARE_PARENT_PARSYS_PATH + "/superlead-superduper";

    @Mock ParagraphSystemContext paragraphManager;
    @Mock Resource parsys;
    @Mock Resource shareParentParsys;
    @Mock Resource shared1, shared2;
    @Mock SniPage sniPage;
    @Mock SniPackage sniPackage;

    LinkedList paragraphs = new LinkedList<Paragraph>();
    
    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);

        // Set up the paragraph manager with the current page and the current parsys resource
        when(paragraphManager.getCurrentPage()).thenReturn(sniPage);
        when(paragraphManager.getCurrentResource()).thenReturn(parsys);

        // Set up minimal mocking for page and parsys resource
        when(parsys.getName()).thenReturn(REGION_NAME);
        when(parsys.getPath()).thenReturn(PARSYS_PATH);
        when(sniPage.getSniPackage()).thenReturn(sniPackage);

        // The package shares a couple modules
        when(sniPackage.getModules()).thenReturn(Arrays.<Resource>asList(shared1, shared2));;

        when(shareParentParsys.getPath()).thenReturn(SHARE_PARENT_PARSYS_PATH);
        when(shared1.getParent()).thenReturn(shareParentParsys);
        when(shared2.getParent()).thenReturn(shareParentParsys);
    }

    @Test
    public void testHappyPath() {
        PackageInheritancePrepender behavior = new PackageInheritancePrepender();
        LinkedList<Paragraph> output = behavior.execute(paragraphManager, paragraphs);
        assertNotNull("returned list is nonnull", output);
        assertEquals("returned list has the correct number of paragraphs", sniPackage.getModules().size(), output.size());
    }

    @Test
    public void testRegionMismatch() {
        when(shareParentParsys.getPath()).thenReturn(ANCHOR_PATH + "/" + JcrConstants.JCR_CONTENT + "/foodebar");

        PackageInheritancePrepender behavior = new PackageInheritancePrepender();
        LinkedList<Paragraph> output = behavior.execute(paragraphManager, paragraphs);
        assertNotNull("returned list is nonnull", output);
        assertEquals("returned list has no paragraphs", paragraphs.size(), output.size());
    }

    @Test
    public void testNullPage() {
        when(paragraphManager.getCurrentPage()).thenReturn(null);

        PackageInheritancePrepender behavior = new PackageInheritancePrepender();
        LinkedList<Paragraph> output = behavior.execute(paragraphManager, paragraphs);
        assertNotNull("returned list is nonnull", output);
        assertEquals("returned list has the correct number of paragraphs", paragraphs.size(), output.size());
    }

    @Test
    public void testNullParsysResource() {
        when(paragraphManager.getCurrentResource()).thenReturn(null);

        PackageInheritancePrepender behavior = new PackageInheritancePrepender();
        LinkedList<Paragraph> output = behavior.execute(paragraphManager, paragraphs);
        assertNotNull("returned list is nonnull", output);
        assertEquals("returned list has the correct number of paragraphs", paragraphs.size(), output.size());
    }

    @Test
    public void testNullContext() {
        PackageInheritancePrepender behavior = new PackageInheritancePrepender();
        LinkedList<Paragraph> output = behavior.execute(null, paragraphs);
        assertNotNull("returned list is nonnull", output);
        assertEquals("returned list has the correct number of paragraphs", paragraphs.size(), output.size());
    }

    @Test
    public void testNullParagraphs() {
        // should create an empty list of paragraphs if input paragraphs null
        PackageInheritancePrepender behavior = new PackageInheritancePrepender();
        LinkedList<Paragraph> output = behavior.execute(paragraphManager, null);
        assertNotNull("returned list is nonnull", output);
        assertEquals("returned list has the correct number of paragraphs", sniPackage.getModules().size(), output.size());
    }
}
