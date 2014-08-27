package com.scrippsnetworks.wcm.image;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.http.HttpStatus;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.commons.mime.MimeTypeService;
import org.apache.sling.commons.testing.jcr.MockNode;
import org.apache.sling.commons.testing.sling.MockSlingHttpServletRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.day.cq.dam.api.Asset;
import com.scrippsnetworks.http.MockStatusExposingSlingServletResponse;



public class SniImageServletTest {
	
	private static final SniImageServlet servlet = new SniImageServlet();
	private MockStatusExposingSlingServletResponse response;
	
	@Before
	public void setup() {
		response = new MockStatusExposingSlingServletResponse(mock(SlingHttpServletResponse.class));
	}

	@Test
	public void emptySelectorsShouldReturn500() throws IOException {
		MockSlingHttpServletRequest req = new MockSlingHttpServletRequest("", "", "", "", "");
		servlet.doGet(req, response);
		Assert.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getHttpStatus());
		Assert.assertEquals(SniImageServlet.NO_IMAGE_SPECIFIED, response.getErrorMessage());
	}
	
	@Test 
	public void emptyResourcePathShouldReturn500() throws IOException {
		MockSlingHttpServletRequest req = new MockSlingHttpServletRequest("", "1.2", "", "", "");
		servlet.doGet(req, response);
		Assert.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.getHttpStatus());
		Assert.assertEquals(SniImageServlet.NO_RESOURCE_SPECIFIED, response.getErrorMessage());
	}

	@Test 
	public void nullResourceShouldReturn404() throws IOException {
		MockSlingHttpServletRequest req = new MockSlingHttpServletRequest("dummyPath", "rend.invalidRenditionType", "", "", "");
		req.setResource(null);
		servlet.doGet(req, response);
		Assert.assertEquals(HttpStatus.SC_NOT_FOUND, response.getHttpStatus());
		Assert.assertEquals(SniImageServlet.IMAGE_NOT_FOUND, response.getErrorMessage());
	}

	@Test
	public void nonDamAssetShouldReturn404() throws IOException, RepositoryException {
		MockSlingHttpServletRequest req = new MockSlingHttpServletRequest("dummyPath", "rend.invalidRenditionType", "", "", "");
		Resource mockResource = mock(Resource.class);
		req.setResource(mockResource);
		Node mock = new MockNode("dummyPath");
		mock.setProperty("jcr:primaryType", "NON-DAM-IMAGE");
		given(mockResource.adaptTo(Node.class)).willReturn(mock);
		servlet.doGet(req, response);
		Assert.assertEquals(HttpStatus.SC_NOT_FOUND, response.getHttpStatus());
		Assert.assertEquals(SniImageServlet.BASE_IMAGE_NOT_DEFINED, response.getErrorMessage());
	}
	
	@Test 
	public void nonAssetResourceShouldReturn404() throws IOException, PathNotFoundException, RepositoryException {
		MockSlingHttpServletRequest req = new MockSlingHttpServletRequest("dummyPath", "rend.invalidRenditionType", "", "", "");
		SniImageServlet servlet = new SniImageServlet();
		Resource mockResource = mock(Resource.class);
		req.setResource(mockResource);
		Node mock = new MockNode("dummyPath");
		mock.setProperty("jcr:primaryType", "dam:Asset");
		given(mockResource.adaptTo(Node.class)).willReturn(mock);
		servlet.doGet(req, response);
		Assert.assertEquals(HttpStatus.SC_NOT_FOUND, response.getHttpStatus());
		Assert.assertEquals(SniImageServlet.RESOURCE_IS_NOT_ASSET, response.getErrorMessage());
	}
	
	@Test
	public void invalidRenditionInfoShouldReturn404() throws IOException, PathNotFoundException, RepositoryException {
		MockSlingHttpServletRequest req = new MockSlingHttpServletRequest("dummyPath", "rend.invalidRenditionType", "", "", "");
		Resource mockResource = mock(Resource.class);
		req.setResource(mockResource);
		Node mock = new MockNode("dummyPath");
		mock.setProperty("jcr:primaryType", "dam:Asset");
		given(mockResource.adaptTo(Node.class)).willReturn(mock);
		given(mockResource.adaptTo(Asset.class)).willReturn(mock(Asset.class));
		servlet.doGet(req, response);
		Assert.assertEquals(HttpStatus.SC_NOT_FOUND, response.getHttpStatus());
		Assert.assertEquals(SniImageServlet.RENDITION_NOT_SUPPORTED, response.getErrorMessage());
	}
	
	@Test
	public void invalidAspectShouldReturn404() throws IOException, PathNotFoundException, RepositoryException {
		MockSlingHttpServletRequest req = new MockSlingHttpServletRequest("dummyPath", "rend.sni8col.INVALID_ASPECT_RATIO", "", "", "");
		Resource mockResource = mock(Resource.class);
		req.setResource(mockResource);
		Node mock = new MockNode("dummyPath");
		mock.setProperty("jcr:primaryType", "dam:Asset");
		given(mockResource.adaptTo(Node.class)).willReturn(mock);
		given(mockResource.adaptTo(Asset.class)).willReturn(mock(Asset.class));
		servlet.doGet(req, response);
		Assert.assertEquals(HttpStatus.SC_NOT_FOUND, response.getHttpStatus());
		Assert.assertEquals(SniImageServlet.ASPECT_NOT_SUPPORTED, response.getErrorMessage());
	}
	
	@Test
	public void invalidAspectSizeShouldReturn404() throws IOException, PathNotFoundException, RepositoryException {
		// TODO: Utilize testng and create a parameterized version of this test, to test multiple combinations of non-working combos
		MockSlingHttpServletRequest req = new MockSlingHttpServletRequest("dummyPath", "rend.sni2col.wide", "", "", "");
		Resource mockResource = mock(Resource.class);
		req.setResource(mockResource);
		Node mock = new MockNode("dummyPath");
		mock.setProperty("jcr:primaryType", "dam:Asset");
		given(mockResource.adaptTo(Node.class)).willReturn(mock);
		given(mockResource.adaptTo(Asset.class)).willReturn(mock(Asset.class));
		servlet.doGet(req, response);
		Assert.assertEquals(HttpStatus.SC_NOT_FOUND, response.getHttpStatus());
		Assert.assertEquals(SniImageServlet.ASPECT_NOT_SUPPORTED_AT_REQUESTED_SIZE, response.getErrorMessage());
	}
	
	@Test
	public void nonMatchingExtensionsShouldReturn404() throws IOException, PathNotFoundException, RepositoryException {
		MockSlingHttpServletRequest req = new MockSlingHttpServletRequest("dummyPath", "rend.sni8col.landscape", "png", "", "");
		MimeTypeService mockMimeService = mock(MimeTypeService.class);
		given(mockMimeService.getExtension("image/jpg")).willReturn("jpg");
		servlet.mimeTypeService = mockMimeService;
		Resource mockResource = mock(Resource.class);
		req.setResource(mockResource);
		Node mock = new MockNode("dummyPath");
		mock.setProperty("jcr:primaryType", "dam:Asset");
		given(mockResource.adaptTo(Node.class)).willReturn(mock);
		Asset mockAsset = mock(Asset.class);
		given(mockAsset.getMimeType()).willReturn("image/jpg");
		given(mockResource.adaptTo(Asset.class)).willReturn(mockAsset);
		servlet.doGet(req, response);
		Assert.assertEquals(HttpStatus.SC_NOT_FOUND, response.getHttpStatus());
		Assert.assertEquals(SniImageServlet.EXTENSION_MUST_MATCH, response.getErrorMessage());
	}
	
	//TODO: Make the Generating Rendtion Picker more testable, currently we will only receive nulls on getRendition calls in the servlet.
}
