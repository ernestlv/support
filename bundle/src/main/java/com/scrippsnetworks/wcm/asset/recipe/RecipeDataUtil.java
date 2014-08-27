package com.scrippsnetworks.wcm.asset.recipe;

import com.scrippsnetworks.wcm.asset.DataUtil;
import com.scrippsnetworks.wcm.util.ResourceRankOrderComparator;
import com.scrippsnetworks.wcm.util.AssetPropertyNames;

import java.util.*;

import org.apache.commons.lang.WordUtils;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;

/**
 * Recipe data access logic
 * @author Jason Clark
 * Date: 6/3/12
 * updated by various other folks
 */
@Deprecated
public final class RecipeDataUtil {

    private static final String XLINKED = "crosslinked";
    private static final String SPACE = " ";
    private static final String OPEN_H4 = "<h4>";
    private static final String CLOSE_H4 = "</h4>";
    private static final String NULL = "null";
    private static final String EMPTY_STRING = "";

    // Constants which are going to use in the below code
    public static final String RECIPE_DATA_NODE_NUTRITION = "nutrition";
    public static final String RECIPE_DATA_NODE_BLOCKS = "blocks";
    public static final String JCR_TITLE_INGREDIENTS = "ingredients";
    public static final String JCR_TITLE_DIRECTIONS = "directions";
    public static final String JCR_TITLE_INSTRUCTIONS = "instructions";

    public static final String JCR_TITLE_CROSSLINKED_INGREDIENTS = XLINKED + SPACE + JCR_TITLE_INGREDIENTS;
    public static final String JCR_TITLE_CROSSLINKED_DIRECTIONS = XLINKED + SPACE + JCR_TITLE_DIRECTIONS;
    public static final String JCR_TITLE_CROSSLINKED_INSTRUCTIONS = XLINKED + SPACE + JCR_TITLE_INSTRUCTIONS;

    private RecipeDataUtil() {}

    /**
     * This retrieves properties from the recipe's jcr:content node, merging them with any properties
     * that should be overridden from the page node that called for the data.
     * @param recipeResource Sling Resource for recipe data node
     * @param pageResource Sling Resource for page calling for data
     * @return Map of properties from recipe node, merged with any overrides from page node
     */
    public static Map<String, Object> getRecipeProperties(final Resource recipeResource,
                                                          final Resource pageResource) {
        if (recipeResource != null && pageResource != null) {
            return DataUtil.getAssetData(recipeResource, pageResource);
        } else {
            return null;
        }
    }

    /**
     * This retrieves properties from the recipe's nutrition node, merging them with any properties
     * that should be overridden from the page node that called for the data.
     * @param recipeResource Sling Resource of recipe data node
     * @param pageResource Sling Resource of page requesting data
     * @return Map containing the properties from the nutrition data node
     */
    public static Map<String, Object> getNutritionProperties(final Resource recipeResource,
                                                             final Resource pageResource) {
        if (recipeResource != null && pageResource != null) {
            return DataUtil.getAssetData(recipeResource.getChild(RECIPE_DATA_NODE_NUTRITION),
                    pageResource);
        } else {
            return null;
        }
    }

    /**
     * Generic function for retrieving recipe data nodes
     * @param recipeResource Sling Resource of recipe data node
     * @param jcrTitles String[] of node jcr:titles to retrieve data from
     * @return List of Resources containing all the data from matching nodes
     */
    private static List<Resource> getRecipeBlocks(final Resource recipeResource, final String[] jcrTitles) {
        if (recipeResource == null
                || jcrTitles == null
                || jcrTitles.length == 0) {
            return null;
        }
        Iterator<Resource> recipeDataNodesItr = recipeResource.getChild(RECIPE_DATA_NODE_BLOCKS).listChildren();
        List<Resource> blockResources = new ArrayList<Resource>();
        while (recipeDataNodesItr.hasNext()) {
            Resource resource = recipeDataNodesItr.next();
            ValueMap properties = ResourceUtil.getValueMap(resource);
            if (properties != null
                    && properties.containsKey(AssetPropertyNames.JCR_TITLE.propertyName())
                    && isListed(properties.get(AssetPropertyNames.JCR_TITLE.propertyName()).toString(), jcrTitles)
                    && properties.containsKey(AssetPropertyNames.SNI_RECIPE_BODY.propertyName())) {
                blockResources.add(resource);
            }
        }
        Collections.sort(blockResources, new ResourceRankOrderComparator());
        return blockResources;
    }

    /**
     * Retrieves List of ingredient blocks as Strings from recipe data node
     * @param recipeResource Sling Resource for the recipe data node
     * @return List of Strings representing ingredient entries, in the order they were stored
     */
    public static List<String> getIngredientBlocks(final Resource recipeResource) {
        if (recipeResource == null) {
            return null;
        }
        String[] ingredientTitles = {JCR_TITLE_CROSSLINKED_INGREDIENTS, JCR_TITLE_INGREDIENTS};
        List<Resource> foundBlocks = getRecipeBlocks(recipeResource, ingredientTitles);
        List<Resource> ingredientBlocks = deDupeCrosslinkedBlocks(foundBlocks);

        List<String> output = new ArrayList<String>();
        for (Resource resource : ingredientBlocks) {
            output.add(ResourceUtil.getValueMap(resource)
                    .get(AssetPropertyNames.SNI_RECIPE_BODY.propertyName()).toString());
        }
        return output;
    }

    /**
     * Retrieves List of instruction blocks as Strings from recipe data node
     * @param recipeResource Sling Resource for the recipe data node
     * @return List of Strings representing the recipe instructions entries, in order they were stored
     */
    public static List<String> getInstructionBlocks(final Resource recipeResource) {
        if (recipeResource == null) {
            return null;
        }
        String[] instructionTitles = { JCR_TITLE_CROSSLINKED_DIRECTIONS, JCR_TITLE_CROSSLINKED_INSTRUCTIONS,
                                       JCR_TITLE_DIRECTIONS, JCR_TITLE_INSTRUCTIONS };
        List<Resource> foundBlocks = getRecipeBlocks(recipeResource, instructionTitles);
        List<Resource> instructionBlocks = deDupeCrosslinkedBlocks(foundBlocks);

        List<String> output = new ArrayList<String>();
        for (Resource resource : instructionBlocks) {
            output.add(ResourceUtil.getValueMap(resource)
                    .get(AssetPropertyNames.SNI_RECIPE_BODY.propertyName()).toString());
        }
        return output;
    }

    /**
     * Retrieves any recipe blocks that aren't Directions, Instructions or Ingredients
     * Takes into consideration that there can be crosslinked versions of these blocks
     * Tries to sort first by sni:rankOrder, otherwise FIFO
     * @param recipeResource Sling Resource for the recipe data node
     * @return List of Strings representing the recipe blocks
     */
    public static List<String> getAdditionalBlocks(final Resource recipeResource) {
        if (recipeResource == null) {
            return null;
        }
        String[] knownJcrTitles = { JCR_TITLE_CROSSLINKED_DIRECTIONS,
                                    JCR_TITLE_DIRECTIONS,
                                    JCR_TITLE_CROSSLINKED_INSTRUCTIONS,
                                    JCR_TITLE_INSTRUCTIONS,
                                    JCR_TITLE_CROSSLINKED_INGREDIENTS,
                                    JCR_TITLE_INGREDIENTS };
        List<Resource> foundBlocks = getRecipeBlocksWithUnknownTitles(recipeResource, knownJcrTitles);
        if (foundBlocks == null || foundBlocks.size() == 0) {
            return null;
        } else {
            List<Resource> cleanedBlocks = deDupeCrosslinkedBlocks(foundBlocks);
            Collections.sort(cleanedBlocks, new ResourceRankOrderComparator());
            List<String> sortedBodies = new ArrayList<String>();
            for (Resource resource : cleanedBlocks) {
                ValueMap props = ResourceUtil.getValueMap(resource);
                if (props.containsKey(AssetPropertyNames.SNI_RECIPE_BODY.propertyName())) {
                    String blockTitle = props.containsKey(AssetPropertyNames.JCR_TITLE.propertyName()) ? props.get(AssetPropertyNames.JCR_TITLE.propertyName(), String.class) : EMPTY_STRING;
                    String formattedTitle = WordUtils.capitalize(blockTitle.toLowerCase().replaceFirst(XLINKED + SPACE, EMPTY_STRING));
                    String printTitle = blockTitle.toLowerCase().contains(NULL) ? EMPTY_STRING : OPEN_H4 + formattedTitle + CLOSE_H4;
                    String body = printTitle + props.get(AssetPropertyNames.SNI_RECIPE_BODY.propertyName(), String.class);
                    sortedBodies.add(body);
                }
            }
            return sortedBodies;
        }
    }

    /**
     * Checks List of Resources to see if there are crosslinked versions of the same recipe block,
     * dupes are omitted from the return results
     * Assumes that these blocks are uniquely identified by their sni:rankOrder
     * @param resources List of Resources to check (recipe blocks)
     * @return List of Resources minus duplicate blocks
     */
    private static List<Resource> deDupeCrosslinkedBlocks(final List<Resource> resources) {
        Map<String, Resource> resourceMap = new HashMap<String, Resource>();
        for (Resource resource : resources) {
            ValueMap properties = ResourceUtil.getValueMap(resource);
            if (properties.containsKey(AssetPropertyNames.JCR_TITLE.propertyName())
                    && properties.containsKey(AssetPropertyNames.SNI_RANK_ORDER.propertyName())) {
                String rankOrder = properties.get(AssetPropertyNames.SNI_RANK_ORDER.propertyName()).toString();
                String jcrTitle  = properties.get(AssetPropertyNames.JCR_TITLE.propertyName()).toString();
                if (resourceMap.containsKey(rankOrder)) {
                    if (jcrTitle.toLowerCase().matches("^" + XLINKED + ".*")) {
                        //crosslinked blocks take precedence
                        resourceMap.put(rankOrder, resource);
                    }
                } else {
                    resourceMap.put(rankOrder, resource);
                }
            }
        }
        return new ArrayList<Resource>(resourceMap.values());
    }

    /**
     * This is to match a String against a String[] of possible matches, ignoring case
     * @param name String value to check
     * @param titles String[] of properties to match against
     * @return boolean
     */
    private static boolean isListed(final String name, final String[] titles) {
        if (name == null
                || name.length() == 0
                || titles == null
                || titles.length == 0) {
            return false;
        }
        for (String title : titles) {
            if (name.equalsIgnoreCase(title)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Generic function for retrieving nodes of the recipe data nodes as a List of Strings
     * @param recipeResource Sling Resource of recipe data node
     * @param jcrTitles String[] of node jcr:titles to retrieve data from
     * @return List of Strings containing all the data from matching nodes
     */
    private static List<String> getRecipeData(final Resource recipeResource, final String[] jcrTitles) {
        if (recipeResource == null
                || jcrTitles == null
                || jcrTitles.length == 0) {
            return null;
        }
        List<Resource> foundResources = getRecipeBlocks(recipeResource, jcrTitles);
        List<String> output = new ArrayList<String>();
        for (Resource resource : foundResources) {
            output.add(ResourceUtil.getValueMap(resource)
                    .get(AssetPropertyNames.SNI_RECIPE_BODY.propertyName()).toString());
        }
        return output;
    }

    /**
     * For when you want recipe blocks that have crazy titles...
     * @param recipeResource Sling Resource of recipe data node
     * @param knownJcrTitles String[] of jcr:titles that are known and will be ignored
     * @return List of Strings representing recipe bodies for these oddly-titled blocks
     */
    private static List<Resource> getRecipeBlocksWithUnknownTitles(final Resource recipeResource,
                                                                   final String[] knownJcrTitles) {
        if (recipeResource == null
                || knownJcrTitles == null
                || knownJcrTitles.length == 0) {
            return null;
        }
        Iterator<Resource> recipeDataNodesItr = recipeResource.getChild(RECIPE_DATA_NODE_BLOCKS).listChildren();
        List<Resource> output = new ArrayList<Resource>();
        while (recipeDataNodesItr.hasNext()) {
            Resource resource = recipeDataNodesItr.next();
            ValueMap properties = ResourceUtil.getValueMap(resource);
            if (properties != null
                    && properties.containsKey(AssetPropertyNames.JCR_TITLE.propertyName())
                    && !isListed(properties.get(AssetPropertyNames.JCR_TITLE.propertyName()).toString(), knownJcrTitles)
                    && properties.containsKey(AssetPropertyNames.SNI_RECIPE_BODY.propertyName())) {
                output.add(resource);
            }
        }
        return output;
    }
    
    /**
     * Retrieves nutrition data nodes (refactored jpc 9.18.12)
     * @param recipeResource Sling Resource for the recipe data node
     * @return List contains all the child data nodes
     */
    public static List<Resource> getAllNutritionDataNodes(final Resource recipeResource) {
        if (recipeResource != null) {
            Resource nutritionResource = recipeResource.getChild(RECIPE_DATA_NODE_NUTRITION);
            if(nutritionResource!=null) {
               Iterator<Resource> resourceItr = recipeResource.getChild(RECIPE_DATA_NODE_NUTRITION).listChildren();
               List<Resource> output = new ArrayList<Resource>();
               while (resourceItr.hasNext()) {
                   Resource resource = resourceItr.next();
                   ValueMap properties = ResourceUtil.getValueMap(resource);
                   if (properties.containsKey(AssetPropertyNames.SNI_VALUE.propertyName())) {
                       output.add(resource);
                   }
               }
               Collections.sort(output, new ResourceRankOrderComparator());
               return output;
            }
        }
        return null;
    }

}
