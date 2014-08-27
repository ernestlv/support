package com.scrippsnetworks.wcm.asset.photogallery;

import com.scrippsnetworks.wcm.asset.DataUtil;
import org.apache.commons.lang.Validate;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;

/**
 * Abstract representation of a Photo Gallery page's title-description component
 * @author Jason Clark
 * Date: 7/25/12
 */
@Deprecated
public class TitleDescriptionComponent {

    private static final String EMPTY_STRING = "";

    public static final String PROPERTY_NAME_TITLE = "title";
    public static final String PROPERTY_NAME_DESCRIPTION = "description";
    public static final String TITLE_DESCRIPTION_RESOURCE_NAME = "title-description";
    public static final String TITLE_DESCRIPTION_RESOURCE_TYPE =
            "sni-wcm/components/pagetypes/photo-gallery/components/"
            + TITLE_DESCRIPTION_RESOURCE_NAME;

    /* MEMBER FIELDS */
    private String title;
    private String description;

    /**
     * empty constructor
     */
    public TitleDescriptionComponent() {}

    /**
     * Instantiate a PhotoGalleryTitleDescriptionComponent using a Resource from
     * a title-description component
     * @param resource Sling Resource of the title-description component
     */
    public TitleDescriptionComponent(final Resource resource) {
        Validate.notNull(resource);
        Validate.isTrue(resource.isResourceType(TITLE_DESCRIPTION_RESOURCE_TYPE));

        ValueMap resourceProperties = ResourceUtil.getValueMap(resource);
        if (resourceProperties.containsKey(PROPERTY_NAME_TITLE)) {
            title = resourceProperties.get(PROPERTY_NAME_TITLE).toString();
        } else {
            title = EMPTY_STRING;
        }

        if (resourceProperties.containsKey(PROPERTY_NAME_DESCRIPTION)) {
            description = resourceProperties.get(PROPERTY_NAME_DESCRIPTION).toString();
        } else {
            description = EMPTY_STRING;
        }
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
