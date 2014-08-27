package com.scrippsnetworks.wcm.taglib;

import java.util.Map;
import java.util.HashMap;
import java.lang.RuntimeException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.tagext.DynamicAttributes;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.designer.Design;
import com.day.cq.wcm.api.designer.Designer;
import com.day.cq.wcm.api.designer.Style;
import org.apache.sling.api.scripting.SlingScriptHelper;
import com.scrippsnetworks.wcm.AbstractComponent;
import com.scrippsnetworks.wcm.AbstractSearchComponent;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.search.SearchService;

/**
 * Tag for creating a search component bean.
 *
 * This tag provides for creating an AbstractSearchComponent bean and injecting it
 * with the expected properties.
 * 
 * @author Scott Everett Johnson
 * 
 */
public class SearchComponentBeanTag extends ComponentBeanTag implements DynamicAttributes {

	private final Logger log = LoggerFactory.getLogger(getClass());

    private String serviceName = null;
    private Map<String, Object> dynamicAttributes = new HashMap<String, Object>();

	/**
	 * Inject bean properties.
	 * 
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
    @Override
	protected AbstractComponent createComponent(PageContext pageContext)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		AbstractSearchComponent component = (AbstractSearchComponent) super.createComponent(pageContext);

        SniPage currentSniPage = (SniPage)component.getRequest().getAttribute("currentSniPage");
        if (currentSniPage == null) {
            throw new RuntimeException("could not retrieve current sni page");
        }
        component.setCurrentSniPage(currentSniPage);

        // the SlingScriptHelper was set on the component by the supertype
        SearchService searchService = getSearchService(component.getSlingScriptHelper(), currentSniPage.getBrand());
        if (searchService == null) {
            throw new RuntimeException("could not retrieve search service");
        }
        component.setSearchService(searchService);
        component.setServiceName(serviceName);
        component.setDynamicAttributes(dynamicAttributes);

		return component;
	}

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setDynamicAttribute(String uri, String localName, Object value) {
        dynamicAttributes.put(localName, value);
    }

    private SearchService getSearchService(SlingScriptHelper sling, String brand) {
        SearchService searchService = null;

        if (brand == null) {
            throw new RuntimeException("brand must be nonnull");
        }
        String siteFilter = "(siteName=" + brand + ")";

        if (sling != null) {
            SearchService[] ssArray = sling.getServices(SearchService.class, siteFilter);
            if (ssArray != null && ssArray.length > 0) {
                searchService = ssArray[0];
            } else {
                throw new RuntimeException("could not retrieve search service");
            }
        } else {
            throw new RuntimeException("SlingScriptHelper not available");
        }

        return searchService;
    }
}

