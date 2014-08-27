package com.scrippsnetworks.wcm.util.modalwindow;

import com.day.cq.commons.jcr.JcrConstants;
import com.scrippsnetworks.wcm.config.TemplateConfigService;
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
 * SNI WCM Mobile Modal Path Constructor Service
 */
@Component(label = "SNI WCM Mobile Modal Path Constructor Service",
        description = "Component construct modal window path for mobile site",
        enabled = true,
        immediate = true,
        metatype = true)
@Service(value = MobileModalPath.class)
public class MobileModalUrlRewritter implements MobileModalPath {
    /**
     * Constants
     */
    private static Logger logger = LoggerFactory.getLogger(MobileModalUrlRewritter.class);
    private Set<String> acceptedTypes = new HashSet<String>();

    @Property(label = "Sling:resourceType", description = "Accepted Sling Resource types for overriding path on mobile modal window",
            value = {"sni-food/components/pagetypes/photo-gallery",
                    "sni-food/components/pagetypes/mobile/photo-gallery",
                    "sni-food/components/pagetypes/photo-gallery-listing",
                    "sni-food/components/pagetypes/video",
                    "sni-food/components/pagetypes/video-channel",
                    "sni-food/components/pagetypes/video-player"})
    private static final String ACTIVATED_TYPES = "activated.types";

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
        String[] types = OsgiUtil.toStringArray(props.get(ACTIVATED_TYPES));
        for (String type : types) {
            if (!acceptedTypes.contains(type)) {
                acceptedTypes.add(type);
            }
        }
    }

    @Override
    public String getModalWindowPath(ResourceResolver resourceResolver, String currentPath, int pageNumber) {
        if (StringUtils.isEmpty(currentPath)) {
            logger.warn("Path is empty or null");
            return currentPath;
        }
        String resourceSuperType = null;
        Resource resource = resourceResolver.getResource(currentPath);

        if (null == resource) {
            logger.debug("Can not get resource by this path");
            return currentPath;
        } else {
            if (resource.isResourceType(Constant.CQ_PAGE)) {
                resource = resource.getChild(JcrConstants.JCR_CONTENT);
                if (resource != null) {
                    ValueMap valueMap = resource.adaptTo(ValueMap.class);
                    resourceSuperType = valueMap.get(PagePropertyNames.SLING_RESOURCE_TYPE.propertyName(), String.class);
                }
            }

            if (resourceSuperType != null && acceptedTypes.contains(resourceSuperType)) {
                return overridePath(currentPath, pageNumber);
            }
        }
        return currentPath;
    }

    @Override
    public String getModalWindowPath(ResourceResolver resourceResolver, String currentPath) {
        if (StringUtils.isEmpty(currentPath)) {
            logger.warn("Path is empty or null");
            return currentPath;
        }

        Pattern pageNumberPattern = Pattern.compile(".+[.]page-(\\d+)[.].*");
        Matcher pageNumberMatcher = pageNumberPattern.matcher(currentPath);

        int pageNumber = 0;
        if (pageNumberMatcher.find()){
            pageNumber = Integer.parseInt(pageNumberMatcher.group(1));
        }

        currentPath = currentPath.replaceAll("[.].*[.]", ".");
        currentPath = currentPath.replaceAll(".html$", "");

        String resourceSuperType = null;
        Resource resource = resourceResolver.getResource(currentPath);

        if (null == resource) {
            logger.debug("Can not get resource by this path");
            return currentPath;
        } else {
            if (resource.isResourceType(Constant.CQ_PAGE)) {
                resource = resource.getChild(JcrConstants.JCR_CONTENT);
                if (resource != null) {
                    ValueMap valueMap = resource.adaptTo(ValueMap.class);
                    resourceSuperType = valueMap.get(PagePropertyNames.SLING_RESOURCE_TYPE.propertyName(), String.class);
                }
            }

            if (resourceSuperType != null && acceptedTypes.contains(resourceSuperType)) {
                return overridePath(currentPath, pageNumber);
            }
        }
        return currentPath;
    }

    private String overridePath(String currentPath, int pageNumber) {
        if (config != null && StringUtils.isNotEmpty(currentPath)) {
            String modalWindowPath = config.getModalWindowPath();
            if (StringUtils.isNotEmpty(modalWindowPath) && !StringUtils.contains(modalWindowPath,Constant.HTML)) {
                logger.debug("Add .html to current path");
                modalWindowPath += ".html";
            }

            if (pageNumber > 1){
                modalWindowPath = modalWindowPath.replace(".html",".page-" + pageNumber + ".html");
            }

            return modalWindowPath + currentPath.replace(ContentRootPaths.CONTENT_FOOD.path(),"");
        }
        logger.warn("Configuration service is not started or empty path");

        return currentPath;
    }
}
