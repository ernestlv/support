package com.scrippsnetworks.wcm.export.page.content;

import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.asset.hub.Hub;
import com.scrippsnetworks.wcm.asset.recipe.Recipe;
import com.scrippsnetworks.wcm.util.PagePropertyNames;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.resource.Resource;
import java.util.Map;

/**
 * Properties for a recipe page (including asset props)
 * @author Jason Clark
 *         Date: 12/3/12
 */
public class RecipeData extends MetaData {
    public RecipeData(final Page page) {
        super(page);

        //TODO commenting out until this does something; as per Sonar
        /* Resource pageContentResource = page.getContentResource();
        ValueMap pageProperties = ResourceUtil.getValueMap(pageContentResource);

        if (pageProperties.containsKey(PagePropertyNames.SNI_ASSET_LINK.propertyName())) {
            String assetPath = pageProperties.get(PagePropertyNames.SNI_ASSET_LINK.propertyName()).toString();
            if (assetPath != null) {
                Recipe recipe = new Recipe(pageContentResource, assetPath);
            }
        }
        */
    }
}
