package com.scrippsnetworks.wcm.taglib;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.designer.Design;
import com.day.cq.wcm.api.designer.Designer;
import com.day.cq.wcm.api.designer.Style;
import com.scrippsnetworks.wcm.AbstractComponent;

/**
 * 
 * @author mei-yichang, Scott Johnson
 * 
 */
public class ComponentBeanTag extends SimpleTagSupport {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private String beanClass;
	private String id;
    private String errorId;
    private boolean throwErrors = false;

	/**
	 * Load all objects from /libs/wcm/global.jsp
	 * 
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	protected AbstractComponent createComponent(PageContext pageContext)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {

		AbstractComponent component = (AbstractComponent) Class.forName(
				beanClass).newInstance();

		component.setPageContext(pageContext);

		component.setComponent((Component) pageContext
				.getAttribute("component"));
		component.setProperties((ValueMap) pageContext
				.getAttribute("properties"));
		component.setPageManager((PageManager) pageContext
				.getAttribute("pageManager"));
		component
				.setCurrentPage((Page) pageContext.getAttribute("currentPage"));
		component.setPageProperties((ValueMap) pageContext
				.getAttribute("pageProperties"));
		component.setComponent((Component) pageContext
				.getAttribute("component"));
		component.setDesigner((Designer) pageContext.getAttribute("designer"));
		component.setCurrentDesign((Design) pageContext
				.getAttribute("currentDesign"));
		component.setCurrentStyle((Style) pageContext
				.getAttribute("currentStyle"));

		component.setRequest((HttpServletRequest) pageContext.getRequest());
		component.setResponse((HttpServletResponse) pageContext.getResponse());

		component.setSlingRequest((SlingHttpServletRequest) component
				.getRequest());
		component.setSlingResponse((SlingHttpServletResponse) component
				.getResponse());

		component.setSlingScriptHelper((SlingScriptHelper) pageContext
				.getAttribute("sling"));

		return component;

	}

	@Override
	public void doTag() throws JspException {
        JspException error = null;
        PageContext pageContext = null;
        String errorMessage = null;
		try {
			pageContext = (PageContext) getJspContext();
			AbstractComponent component = createComponent(pageContext);
			component.doAction();
			pageContext.setAttribute(id, component);
		} catch (InstantiationException e) {
            errorMessage = "Could not initialize bean";
			log.error(errorMessage, e);
            error = new JspException(errorMessage, e);
		} catch (IllegalAccessException e) {
            errorMessage = "Could not initialize bean";
			log.error(errorMessage, e);
            error = new JspException(errorMessage, e);
		} catch (ClassNotFoundException e) {
			errorMessage = "Class not found: '" + beanClass + "'";
			log.error(errorMessage, e);
            error = new JspException(errorMessage, e);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
            error = new JspException(e.getMessage(), e);
		} finally {
            if (error != null) {
                if (errorId != null) {
                    pageContext.setAttribute(errorId, error);
                }
                if (throwErrors) {
                    throw error;
                }
            }
        }
	}

	public void setBean(String beanClass) {
		this.beanClass = beanClass;
	}

	public void setId(String id) {
		this.id = id;
	}

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    public void setThrowErrors(boolean throwErrors) {
        this.throwErrors = throwErrors;
    }
}
