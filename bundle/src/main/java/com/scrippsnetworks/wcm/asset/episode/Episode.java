package com.scrippsnetworks.wcm.asset.episode;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.asset.DataUtil;
import com.scrippsnetworks.wcm.taglib.Functions;
import com.scrippsnetworks.wcm.fnr.util.AssetSlingResourceTypes;
import com.scrippsnetworks.wcm.util.MonthNames;
import com.scrippsnetworks.wcm.util.NodeNames;
import org.apache.commons.lang.Validate;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.el.PropertyNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract object representation of an Episode asset
 * @author Jason Clark
 * Date: 7/26/12
 */
@Deprecated
public class Episode {
    
    private static final String COMMA = ",";
    private static final String SPACE = " ";

    /* MEMBER FIELDS */
    private String assetPath;
    private String contentPath;
    private Map<String, Object> properties;

    private static final String[] ALLOWED_OVERRIDES = {"jcr:title"};
    private static final Logger LOG = LoggerFactory.getLogger(Episode.class);
    
    /**
     * empty constructor
     */
    public Episode() {}

    /**
     * Construct an Episode object using the path to the JCR asset node for that episode
     * @param resource Sling Resource in hand (can be any resource)
     * @param pathToEpisodeAsset String path to JCR asset node for episode
     */
    public Episode(final Resource resource, final String pathToEpisodeAsset) {

        Validate.notNull(resource);
        Validate.notNull(pathToEpisodeAsset);
        String pathToRetrieve = Functions.getBasePath(pathToEpisodeAsset)
                + "/" + NodeNames.JCR_CONTENT.nodeName();
        Resource episodeResource = resource.getResourceResolver().getResource(pathToRetrieve);
        PageManager pageManager = resource.getResourceResolver().adaptTo(PageManager.class);
        
        try {
            ValueMap episodeResourceProperties = ResourceUtil.getValueMap(episodeResource);
            Validate.isTrue(DataUtil.valueMapIsType(episodeResourceProperties,
                    AssetSlingResourceTypes.EPISODE.resourceType()));
            assetPath = pathToEpisodeAsset;
            properties = new HashMap<String, Object>();
            //for (String key : episodeResourceProperties.keySet()) {
            for (Map.Entry<String,Object> set : episodeResourceProperties.entrySet()) {
                properties.put(set.getKey(), set.getValue());
            }

            String[] pageLinks = episodeResourceProperties.get("sni:pageLinks", String[].class);
            if(pageLinks != null) {
            	contentPath = pageLinks[0];
            }
            //Override sni-asset properties with certain ones from content properties
            if(contentPath != null && !contentPath.isEmpty()) {
            	Page contentPage = pageManager.getPage(contentPath);
            	ValueMap contentResourceProperties = contentPage.getProperties();
            	for(String overrideKey : ALLOWED_OVERRIDES) {
            		String overrideValue = contentResourceProperties.get(overrideKey, String.class);
            		if(overrideValue != null) {
            			properties.put(overrideKey, overrideValue);
            		}
            	}
            	
            }
        // TODO do we really want to catch NPE?
        } catch (NullPointerException npe) {
        	LOG.error("A null pointer exception occured while trying to populate the Episode object: " + npe.getMessage());
            //setting assetPath for bug fixoring if necessary
            assetPath = pathToEpisodeAsset;
            properties = null;
        }
    }

    /**
     * This timestamp format is specific to Episodes, used in the Tune-In time module
     * @param dateMap Map from the dateMapFromSchedulePath method in DataUtil
     * @return String of timestamp formatted for Tune-In times portion of episode page
     */
    public static String tuneInTimeStamp(final Map<String, String> dateMap) {
        if (dateMap == null) {
            return null;
        }
        try {
            Map<String, String> standardTime = DataUtil.convertMilTimeToStandard(dateMap.get("time"));
            StringBuilder output = new StringBuilder();
            output.append(MonthNames.values()[(Integer.valueOf(dateMap.get("month")) - 1)].monthName());
            output.append(SPACE);
            output.append(dateMap.get("day"));
            output.append(COMMA);
            output.append(SPACE);
            output.append(dateMap.get("year"));
            output.append(SPACE);
            output.append(standardTime.get("time"));
            output.append(SPACE);
            output.append(standardTime.get("period"));
            return output.toString();
        } catch (PropertyNotFoundException e) {
            return null;
        }
    }

    public String getAssetPath() {
        return assetPath;
    }
    public void setAssetPath(String assetPath) {
        this.assetPath = assetPath;
    }
    public Map<String, Object> getProperties() {
        return properties;
    }
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
