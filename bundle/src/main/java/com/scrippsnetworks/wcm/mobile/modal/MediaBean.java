package com.scrippsnetworks.wcm.mobile.modal;


import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.AbstractComponent;
import com.scrippsnetworks.wcm.fnr.util.OsgiHelper;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.url.UrlMapper;
import com.scrippsnetworks.wcm.url.impl.PathHelper;
import com.scrippsnetworks.wcm.url.impl.UrlMapperImpl;
import com.scrippsnetworks.wcm.util.Constant;
import com.scrippsnetworks.wcm.util.ContentRootPaths;
import com.scrippsnetworks.wcm.util.PagePropertyNames;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.MarkerFactory;
import org.slf4j.helpers.BasicMarker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.scrippsnetworks.wcm.fnr.util.PageSlingResourceTypes.*;


public class MediaBean extends AbstractComponent {
    /**
     * Constants
     */
    public static final String TYPE = "type";
    public static final String MEDIA_RESOURCE = "mediaResource";
    public static final String MEDIA_SNI_PAGE = "mediaSniPage";

    private UrlMapper urlMapper = new OsgiHelper().getOsgiService(UrlMapper.class.getName());

    public MediaBean() {
    }

    @Override
    public void doAction() throws Exception {
        RequestPathInfo pathInfo = getSlingRequest().getRequestPathInfo();
        Resource resource = getCurrentPage().adaptTo(Resource.class);
        ResourceResolver resourceResolver = resource.getResourceResolver();
        String suffix = pathInfo.getSuffix();
        if (suffix != null) {
            String path = suffix;
            if (!StringUtils.contains(path, ContentRootPaths.CONTENT_FOOD.path())) {
                path = ContentRootPaths.CONTENT_FOOD.path() + path;
            }
            path=StringUtils.replace(path,Constant.HTML,"");
            path=path.replaceAll("[.].*","");
            log.info("*MEDIABEAN* before mapped path: "+path);
            Resource pageResource = null;
            String mappedPath = urlMapper.resolvePath(getSlingRequest(), path);
            path = mappedPath != null ? mappedPath : path;
            log.info("*MEDIABEAN* post mapped path: "+path);
            pageResource = resourceResolver.resolve(getSlingRequest(), path);
            ValueMap valueMap = null;
            if (pageResource != null && pageResource.isResourceType(Constant.CQ_PAGE)) {
                Page mediaPage = pageResource.adaptTo(Page.class);
                SniPage mediaSniPage = PageFactory.getSniPage(mediaPage);
                getRequest().setAttribute(MEDIA_SNI_PAGE, mediaSniPage);

                pageResource = pageResource.getChild(JcrConstants.JCR_CONTENT);
            }

            if (pageResource != null) {
                valueMap = pageResource.adaptTo(ValueMap.class);
                getRequest().setAttribute(MEDIA_RESOURCE,pageResource);
            }
            int from=0;
            int to=0;
            String currentType = null;
            if (valueMap != null) {
                currentType = valueMap.get(PagePropertyNames.SLING_RESOURCE_TYPE.propertyName(), String.class);
                from= StringUtils.lastIndexOf(currentType, Constant.SLASH) + 1;
                to=currentType.length();
            }
            log.info("*MEDIABEAN* currentType: "+currentType);
            if (VIDEO.resourceType().equals(currentType)) {
                getPageContext().setAttribute(TYPE, VIDEO.resourceType().substring(from, to));
            } else if (VIDEO_PLAYER.resourceType().equals(currentType) || VIDEO_PLAYER_MOBILE.resourceType().equals(currentType)) {
                getPageContext().setAttribute(TYPE, VIDEO_PLAYER.resourceType().substring(from, to));
            } else if (VIDEO_CHANNEL.resourceType().equals(currentType) || VIDEO_CHANNEL_MOBILE.resourceType().equals(currentType)) {
                getPageContext().setAttribute(TYPE, VIDEO_CHANNEL.resourceType().substring(from, to));
            } else if(PHOTO_GALLERY.resourceType().equals(currentType)|| PHOTO_GALLERY_LISTING.resourceType().equals(currentType)|| PHOTO_GALLERY_MOBILE.resourceType().equals(currentType)){
                getPageContext().setAttribute(TYPE, PHOTO_GALLERY.resourceType().substring(from, to));
            }
        }
    }

}
