package com.scrippsnetworks.wcm;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.EditContext;
import com.day.cq.wcm.api.designer.Design;
import com.day.cq.wcm.api.designer.Designer;
import com.day.cq.wcm.api.designer.Style;

/**
 * Base Class for the Action Components to be used in JSTL
 * 
 * @author mei-yichang
 * 
 */
public abstract class AbstractComponent {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private EditContext editContext;
	private ValueMap properties;
	private PageManager pageManager;
	private Page currentPage;
	private ValueMap pageProperties;
	private Node currentNode;

	private Component component;
	private Designer designer;
	private Design currentDesign;
	private Style currentStyle;
	private SlingHttpServletRequest slingRequest;
	private SlingHttpServletResponse slingResponse;
	private SlingScriptHelper slingScriptHelper;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private PageContext _pageContext;

	public abstract void doAction() throws Exception;

	public Component getComponent() {
		return component;
	}

	public Design getCurrentDesign() {
		return currentDesign;
	}

	protected Node getCurrentNode() {
		return currentNode;
	}

	public Page getCurrentPage() {
		return currentPage;
	}

	public Style getCurrentStyle() {
		return currentStyle;
	}

	public Designer getDesigner() {
		return designer;
	}

	public EditContext getEditContext() {
		return editContext;
	}

	public PageContext getPageContext() {
		return _pageContext;
	}

	public PageManager getPageManager() {
		return pageManager;
	}

	public ValueMap getPageProperties() {
		return pageProperties;
	}

	public ValueMap getProperties() {
		return properties;
	}

	public String getProperty(String key) {
		return (String) getProperties().get(key);
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	/**
	 * Gets the current JCR session object.
	 * 
	 * @return Returns the current JCR session object or null if none could be
	 *         obtained.
	 */
	protected final Session getSession() throws RepositoryException {
		Session session = null;
		Page page = getCurrentPage();
		if (page != null) {
			Node node = page.adaptTo(Node.class);
			if (node != null) {
				session = node.getSession();
			}
		}
		return session;

	}

	public SlingHttpServletRequest getSlingRequest() {
		return slingRequest;
	}

	public SlingHttpServletResponse getSlingResponse() {
		return slingResponse;
	}

	public SlingScriptHelper getSlingScriptHelper() {
		return slingScriptHelper;
	}

	public void setComponent(Component component) {
		this.component = component;
	}

	public void setCurrentDesign(Design currentDesign) {
		this.currentDesign = currentDesign;
	}

	public void setCurrentPage(Page currentPage) {
		this.currentPage = currentPage;
	}

	public void setCurrentStyle(Style currentStyle) {
		this.currentStyle = currentStyle;
	}

	public void setDesigner(Designer designer) {
		this.designer = designer;
	}

	public void setEditContext(EditContext editContext) {
		this.editContext = editContext;
	}

	public void setPageContext(PageContext pageContext) {
		_pageContext = pageContext;
	}

	public void setPageManager(PageManager pageManager) {
		this.pageManager = pageManager;
	}

	public void setPageProperties(ValueMap pageProperties) {
		this.pageProperties = pageProperties;
	}

	public void setProperties(ValueMap properties) {
		this.properties = properties;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	public void setSlingRequest(SlingHttpServletRequest slingRequest) {
		this.slingRequest = slingRequest;
	}

	public void setSlingResponse(SlingHttpServletResponse slingRes) {
		this.slingResponse = slingRes;
	}

    public void setSlingScriptHelper(SlingScriptHelper slingScriptHelper) {
        this.slingScriptHelper = slingScriptHelper;
    }

	public void setCurrentNode(Node currentNode) {
		this.currentNode = currentNode;
	}
}
