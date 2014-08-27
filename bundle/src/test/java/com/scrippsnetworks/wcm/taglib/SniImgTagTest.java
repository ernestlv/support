package com.scrippsnetworks.wcm.taglib;


import java.util.*;
import java.lang.*;
import java.io.IOException;
import java.lang.annotation.Annotation;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.scripting.jsp.util.TagUtil;
import com.day.cq.wcm.api.WCMMode;

import com.scrippsnetworks.wcm.image.ImageUrlService;
import com.scrippsnetworks.wcm.image.ImageAspect;
import com.scrippsnetworks.wcm.image.RenditionInfo;
import com.scrippsnetworks.wcm.image.ImageDimensions;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;

import org.junit.*;
import static org.junit.Assert.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;



public class SniImgTagTest {

    @Mock PageContext pageContext;
    @Mock SlingBindings slingBindings;
    @Mock SlingScriptHelper sling;
    @Mock SlingHttpServletRequest slingRequest;
    @Mock ResourceResolver resourceResolver;
    @Mock JspWriter out;
    @Mock ImageUrlService imageUrlService;

    public static final String DAM_PATH = "/content/dam/images/an-image";

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        when(pageContext.getRequest()).thenReturn(slingRequest);
        when(slingRequest.getAttribute(SlingBindings.class.getName())).thenReturn(slingBindings);
        when(slingBindings.getSling()).thenReturn(sling);
        when(slingRequest.getAttribute(WCMMode.class.getName())).thenReturn(WCMMode.DISABLED);
        when(pageContext.getAttribute("resourceResolver")).thenReturn(resourceResolver);
        when(pageContext.getOut()).thenReturn(out);
        when(sling.getService(ImageUrlService.class)).thenReturn(imageUrlService);
    }

    @Test
    public void basicTest() throws JspException, IOException {
        SniImgTag tag = new SniImgTag();
        tag.setPageContext(pageContext);
        tag.setDamPath(DAM_PATH);
        tag.setRendition(RenditionInfo.sni8col.name());
        tag.setAspect(ImageAspect.landscape.name());

        int result = tag.doEndTag();
        assertEquals("doEndTag returns EVAL_PAGE", Tag.EVAL_PAGE, result);

        ArgumentCaptor<String> outputCaptor = ArgumentCaptor.forClass(String.class);
        verify(out).print(outputCaptor.capture());

        // make sure the image url service is called correctly...the value returned is not part of our unit test
        verify(imageUrlService).getImageUrl(DAM_PATH, RenditionInfo.sni8col, ImageAspect.landscape);

        String tagOutput = outputCaptor.getValue();

        assertNotNull(tagOutput);

        Whitelist whitelist = Whitelist.none().addTags("img").addAttributes("img", "height", "width", "src", "alt", "title");
        assertTrue("markup is valid according to whitelist", Jsoup.isValid(tagOutput, whitelist));

        Document doc = Jsoup.parseBodyFragment(tagOutput);

        Element imgTag = doc.select("img").first();
        assertNotNull(imgTag);

        assertNotNull("src attribute is nonnull", imgTag.attr("src"));

        ImageDimensions dim = RenditionInfo.sni8col.getImageDimensions(ImageAspect.landscape);
        assertNotNull(imgTag.attr("width"));
        assertNotNull(imgTag.attr("height"));
        assertEquals("width is correct", imgTag.attr("width"), String.valueOf(dim.getWidth()));
        assertEquals("height is correct", imgTag.attr("height"), String.valueOf(dim.getHeight()));
    }

    @Test
    public void basicLazyTest() throws JspException, IOException {
        SniImgTag tag = new SniImgTag();
        tag.setPageContext(pageContext);
        tag.setDamPath(DAM_PATH);
        tag.setRendition(RenditionInfo.sni8col.name());
        tag.setAspect(ImageAspect.landscape.name());
        tag.setLazy(true);
        tag.setLazyMode("snirsdiv");

        int result = tag.doEndTag();
        assertEquals("doEndTag returns EVAL_PAGE", Tag.EVAL_PAGE, result);

        ArgumentCaptor<String> outputCaptor = ArgumentCaptor.forClass(String.class);
        verify(out).print(outputCaptor.capture());

        // make sure the image url service is called correctly...the value returned is not part of our unit test
        verify(imageUrlService).getImageUrl(DAM_PATH, RenditionInfo.sni8col, ImageAspect.landscape);

        String tagOutput = outputCaptor.getValue();

        assertNotNull(tagOutput);

        Whitelist whitelist = Whitelist.none().addTags("div").addAttributes("div", "class", "height", "width", "alt", "title");
        assertTrue("markup of " + tagOutput + " is valid according to whitelist", Jsoup.isValid(tagOutput, whitelist));

        Document doc = Jsoup.parseBodyFragment(tagOutput);

        Element imgTag = doc.select("div").first();
        assertNotNull(imgTag);

        assertNotNull("content is nonnull", imgTag.text());

        ImageDimensions dim = RenditionInfo.sni8col.getImageDimensions(ImageAspect.landscape);
        assertNotNull(imgTag.attr("width"));
        assertNotNull(imgTag.attr("height"));
        assertEquals("width is correct", imgTag.attr("width"), String.valueOf(dim.getWidth()));
        assertEquals("height is correct", imgTag.attr("height"), String.valueOf(dim.getHeight()));
    }

    @Test
    public void noResourceResolverTest() throws JspException, IOException {

        // with no resource resolver
        when(pageContext.getAttribute("resourceResolver")).thenReturn(null);

        SniImgTag tag = new SniImgTag();
        tag.setPageContext(pageContext);
        tag.setDamPath(DAM_PATH);
        tag.setRendition(RenditionInfo.sni8col.name());
        // tag.setAspect(ImageAspect.landscape.name());
        tag.setLazy(true);
        tag.setLazyMode("snirsdiv");

        int result = tag.doEndTag();
        assertEquals("doEndTag returns EVAL_PAGE", Tag.EVAL_PAGE, result);

        ArgumentCaptor<String> outputCaptor = ArgumentCaptor.forClass(String.class);
        verify(out).print(outputCaptor.capture());
        System.out.println("verify out");

        // make sure the image url service is called correctly...the value returned is not part of our unit test
        verify(imageUrlService).getImageUrl(DAM_PATH, RenditionInfo.sni8col, null);
        System.out.println("verify url");

        String tagOutput = outputCaptor.getValue();

        assertNotNull(tagOutput);
        System.out.println("tag output nonnull");
        System.out.println(tagOutput);

        Whitelist whitelist = Whitelist.none().addTags("div").addAttributes("div", "class", "height", "width", "alt", "title");
        assertTrue("markup of " + tagOutput + " is valid according to whitelist", Jsoup.isValid(tagOutput, whitelist));

        Document doc = Jsoup.parseBodyFragment(tagOutput);

        Element imgTag = doc.select("div").first();
        assertNotNull(imgTag);

        // If no resource resolver could be acquired, there should be no failures, but no height/width.
        assertNotNull("content is nonnull", imgTag.text());
        assertTrue("width empty", "".equals(imgTag.attr("width")));
        assertTrue("height empty", "".equals(imgTag.attr("height")));
    }

}

