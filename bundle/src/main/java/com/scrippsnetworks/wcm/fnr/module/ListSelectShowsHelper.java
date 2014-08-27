package com.scrippsnetworks.wcm.fnr.module;

import com.day.cq.replication.ReplicationStatus;
import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is meant to be used in CQ module JSP
 * {@link #convertShowPathsToGroupCpmnData(String[])} is the main method exposed, see javadoc on that method for more info
 *
 * This is exposed as an OSGi Service, currently accessed by the JSP using {@link com.scrippsnetworks.wcm.fnr.util.OsgiHelper}
 *
 * @author Ken Shih (156223)
 * @created 4/17/13 9:35 AM
 */
@Component(label="FNR ListSelectedShows Module Helper",
        description="Provides encapsulated business logic for construction of Module data",
        immediate=true,metatype=true)
@Service(value=ListSelectShowsHelper.class)
public class ListSelectShowsHelper {

    public static final String ATTR_TITLE = "title";
    public static final String ATTR_TUNE_IN_TIME = "tuneInTime";
    public static final String ATTR_IMAGE_PATH = "imagePath";
    public static final String ATTR_SHOW_PATH = "showPath";
    public static final String PROPERTY_SNI_FEATUREBANNER = "sni:featureBannerImage";
    //TODO this is placeholder. actual name should be this, but may not be final
    public static final String PROPERTY_SNI_TUNE_IN_TIME = "sni:tuneInTime";
    public static final String PROPERTY_JCR_TITLE = "jcr:title";
    private static final Logger log = LoggerFactory.getLogger(ListSelectShowsHelper.class);

    @Reference
    protected ResourceResolverFactory resourceResolverFactory;

    /**
     * reasons this is needed:
     * 1) module needs replication status BEFORE rendering
     *
     * each show is considered PUBLISHED and has the following information in the map:
     * "title"     - title from Show
     * "tuneInTime"- editor entered text from Show
     * "imagePath" - path to image banner resource
     * "showPath"  - path to show content page (one of those passed in as "showPaths")
     *
     * @param showPaths as strings of show pages that are associated with an instance of the module
     * @return a list of maps containing only the data needed to render the module
     */
    public List<Map<String,Object>> convertShowPathsToGroupCpmnData(String[] showPaths){
        List<Map<String,Object>> shows = new ArrayList<Map<String,Object>>();
        if(showPaths==null){
            return shows;
        }
        ResourceResolver resourceResolver = null;
        try {
            resourceResolver=resourceResolverFactory.getAdministrativeResourceResolver(null);
        } catch (LoginException e) {
            log.error("error logging into jcr",e);
            //swallowing error, letting code below fail
            // TODO consider throwing RuntimeException instead
        }
        try {
            for(String path:showPaths){
                if(path==null||path.isEmpty()) continue;

                Resource rc = resourceResolver.getResource(path);
                if(rc==null) continue;

                Page showPage = rc.adaptTo(Page.class);
                Resource contentResource = showPage.getContentResource();
                if(contentResource==null) continue;

                //get Page Attributes
                SniPage sniPageShow = newSniPage(showPage);
                ValueMap showPageProperties = sniPageShow.getProperties();

                Map<String,Object> pageAttrs = new HashMap<String,Object>();
                //get Title Attribute
                String title = showPageProperties.get(PROPERTY_JCR_TITLE,"");
                pageAttrs.put(ATTR_TITLE,title);

                //get Tune in Time attribute
                String tuneInTime = showPageProperties.get(PROPERTY_SNI_TUNE_IN_TIME,"");
                pageAttrs.put(ATTR_TUNE_IN_TIME,tuneInTime);

                //get Banner Image
                String bannerPath = showPageProperties.get(PROPERTY_SNI_FEATUREBANNER,"");
                pageAttrs.put(ATTR_IMAGE_PATH,bannerPath);

                //set showPath
                pageAttrs.put(ATTR_SHOW_PATH, path);

                //add map as a row in list
                shows.add(pageAttrs);
            }//end for loop
            return shows;
        } finally {
            if(resourceResolver!=null) {
                resourceResolver.close();
            }
        }
    }

    //use of static method is encapsulated so that this is testable/mockable
    //without test being dependent on a class that should have its own test
    protected SniPage newSniPage(Page page){
        return PageFactory.getSniPage(page);
    }
}
