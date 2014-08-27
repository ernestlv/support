package com.scrippsnetworks.wcm.mobile.modal;

import com.scrippsnetworks.wcm.AbstractComponent;
import com.scrippsnetworks.wcm.config.SiteConfigService;
import com.scrippsnetworks.wcm.photogallery.PhotoGallery;
import com.scrippsnetworks.wcm.photogallery.PhotoGalleryFactory;
import com.scrippsnetworks.wcm.photogallery.PhotoGallerySlide;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.util.List;

public class PhotoGalleryBean extends AbstractComponent {
    /**
     * Constants
     */
    public static final String OUTBRAIN_WIDGET_ID_PROP = "outbrain-widget-id";
    public static final String OUTBRAIN_WIDGET_ID_DEFAULT = "AR_6";
    public static final String THUMBNAILS_PER_PAGE = "thumbnailsPerPage";
    public static final String START_SLIDE = "startSlide";
    public static final String GALLERY = "gallery";
    public static final String PAGE_NUMBER = "pageNumber";
    public static final String OUTBRAIN_WIDGET_ID = "outbrainWidgetId";



    private void getPhotoGallery(Resource pageResource) {
        ValueMap properties=pageResource.adaptTo(ValueMap.class);
        String outbrainWidgetId = properties.get(OUTBRAIN_WIDGET_ID_PROP, String.class);
        if(StringUtils.isEmpty(outbrainWidgetId)){
            outbrainWidgetId= OUTBRAIN_WIDGET_ID_DEFAULT;
        }

        Resource parentResource = pageResource.getParent();
        if(parentResource!=null){
            PhotoGallery gallery = new PhotoGalleryFactory().withParsysResource(parentResource).build();
            List<PhotoGallerySlide> allSlides = gallery.getAllSlides();
            Integer deepLinkPageNumber = gallery.getPage().getDeepLinkPageNumber();
            int gallerySize=gallery.getSlideCount();
            if(deepLinkPageNumber!=null){
                int startSlide=deepLinkPageNumber>gallerySize+1?1:deepLinkPageNumber-1;
                getPageContext().setAttribute(START_SLIDE,startSlide);
            }

            BundleContext bundleContext = FrameworkUtil.getBundle(SiteConfigService.class).getBundleContext();
            ServiceReference siteConfigRef = bundleContext.getServiceReference(SiteConfigService.class.getName());
            final SiteConfigService configService = (SiteConfigService)bundleContext.getService(siteConfigRef);

            getPageContext().setAttribute(THUMBNAILS_PER_PAGE, configService.getPhotoGalleryThumbnailPageSize());
            getPageContext().setAttribute(GALLERY, allSlides);
            getPageContext().setAttribute(PAGE_NUMBER, deepLinkPageNumber);
            getPageContext().setAttribute(OUTBRAIN_WIDGET_ID, outbrainWidgetId);
        }
    }

    @Override
    public void doAction() throws Exception {

        Resource pageResource = (Resource)getRequest().getAttribute(MediaBean.MEDIA_RESOURCE);
        if(pageResource!=null){
             getPhotoGallery(pageResource);
         }
    }
}
