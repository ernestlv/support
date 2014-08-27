package com.scrippsnetworks.wcm.mobile.detection;

import com.day.cq.commons.jcr.JcrConstants;
import com.scrippsnetworks.wcm.config.TemplateConfigService;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.util.Constant;
import com.scrippsnetworks.wcm.util.ContentRootPaths;
import com.scrippsnetworks.wcm.util.PagePropertyNames;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * SNI WCM Mobile Detection Service
 */
@Component(label = "SNI WCM Mobile Detection Service",
        description = "Service detect if page has mobile version",
        enabled = true,
        immediate = true,
        metatype = true)
@Service(value = MobilePageDetection.class)

public class MobilePageDetectionImpl implements MobilePageDetection {
    /**
     * Constants
     */
    private static Logger logger = LoggerFactory.getLogger(MobilePageDetection.class);
    private Set<String> acceptedTypes = new HashSet<String>();

    @Property(label = "Sling:resourceType", description = "Accepted Sling Resource types that support mobile version",
            value = {"sni-food/components/pagetypes/homepage",
                    "sni-food/components/pagetypes/search-results",
                    "sni-food/components/pagetypes/photo-gallery",
                    "sni-food/components/pagetypes/video",
                    "sni-food/components/pagetypes/recipe",
                    "sni-food/components/pagetypes/video-channel",
                    "sni-food/components/pagetypes/show",
                    "sni-food/components/pagetypes/universal-landing",
                    "sni-food/components/pagetypes/program-guide-daily",
                    "sni-food/components/pagetypes/topic",
                    "sni-food/components/pagetypes/article-simple",
                    "sni-food/components/pagetypes/talent"})
    private static final String MOBILE_SUPPORTED_TYPES = "mobile.supported.types";

    @Reference
    private TemplateConfigService config;

    /**
     * Activates this OSGi component, setting its properties from the ComponentContext and
     * initializing its state.
     */
    @Activate
    protected void activate(ComponentContext ctx) {
        Dictionary props = ctx.getProperties();
        logger.debug("Activate: Mobile Modal Url Rewrite Service");
        updateCollection(props);
        logger.debug("Collection of types filled");
    }

    /**
     * Deactivates this OSGi component, cleaning up any state.
     */
    @Deactivate
    protected void deactivate(ComponentContext ctx) {
        logger.debug("Deactivate: Mobile Modal Url Rewritter Service");
    }

    /**
     * Updates the state of this OSGi component when the ComponentContext has changed.
     */
    @Modified
    protected void modified(ComponentContext ctx) {
        Dictionary props = ctx.getProperties();
        logger.debug("Modified: Mobile Modal Url Rewrite Service");
        updateCollection(props);
        logger.debug("Collection of types updated");
    }

    
    private void updateCollection(Dictionary props) {
        String[] types = OsgiUtil.toStringArray(props.get(MOBILE_SUPPORTED_TYPES));
        for (String type : types) {
            if (!acceptedTypes.contains(type)) {
                acceptedTypes.add(type);
            }
        }
    }

    @Override
    public boolean isSupportMobileVersion(SniPage sniPage) {
       if(sniPage != null){
    	   if(sniPage.hasContent()){
    		   String resourceType = sniPage.getContentResource().getResourceType();
    		   if(acceptedTypes.contains(resourceType)){
    			   return true;
    		   }
    	   }
       }
       return false;
    }
}
