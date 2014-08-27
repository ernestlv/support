package com.scrippsnetworks.wcm.asset.recipe;

import java.util.*;

import javax.jcr.Node;
import javax.jcr.query.Query;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.commons.lang.Validate;

import com.scrippsnetworks.wcm.util.ContentRootPaths;
import com.scrippsnetworks.wcm.taglib.Functions;

/**
 * This is a representation of recipe data for consumption by web display formatters
 * @author Jason Clark Date: 6/2/12
 */
@Deprecated
public class Recipe {

    private static final String JCR_CONTENT = "/jcr:content";

	private String recipePath;
	private Resource recipeResource;
    private Resource assetResource;
	private List<String> ingredientBlocks;
	private List<String> instructionBlocks;
    private List<String> additionalBlocks;
	private Map<String, Object> recipeProperties;
	private Map<String, Object> nutritionProperties;
    private List<Resource> nutritionDataNodes;
	private ResourceResolver resolver;

	/**
	 * Construct an empty Recipe object
	 */
	public Recipe() {
	}

	/**
	 * Construct a Recipe object given a Sling Resource of the calling page and
	 * the String representing a path to the data node containing the recipe.
	 * 
	 * @param resource Sling Resource of page calling for data.
	 * @param path String representing a path to the JCR node containing recipe data.
	 */
	public Recipe(final Resource resource, final String path) {
		Validate.notNull(resource);
		Validate.notNull(path);
		recipePath = Functions.getBasePath(path) + JCR_CONTENT;
		resolver = resource.getResourceResolver();
		recipeResource = resolver.getResource(recipePath);
        assetResource = resolver.getResource(Functions.getResourceProperty(resource, "sni:assetLink") + JCR_CONTENT);
        ingredientBlocks = RecipeDataUtil.getIngredientBlocks(recipeResource);
        instructionBlocks = new ArrayList<String>();
        List<String> instructionBlockStringList = RecipeDataUtil.getInstructionBlocks(recipeResource);
        if (instructionBlockStringList != null) {
            for (String block : instructionBlockStringList) {
                instructionBlocks.add(FormatFunctions.formatRecipeInstructions(block));
            }
        }
        additionalBlocks = RecipeDataUtil.getAdditionalBlocks(recipeResource);
        if (additionalBlocks != null && additionalBlocks.size() > 0) {
            instructionBlocks.addAll(additionalBlocks);
        }
		recipeProperties = RecipeDataUtil.getRecipeProperties(recipeResource, resource);
		nutritionProperties = RecipeDataUtil.getNutritionProperties(recipeResource, resource);
        nutritionDataNodes = RecipeDataUtil.getAllNutritionDataNodes(recipeResource);		
	}

	void setRecipePath(String path) {
		recipePath = path;
	}

	void setRecipeResource(Resource resource) {
		recipeResource = resource;
	}

	void setRecipeProperties(Map<String, Object> map) {
		recipeProperties = map;
	}

	void setIngredientBlocks(List<String> blocks) {
		ingredientBlocks = blocks;
	}

	void setInstructionBlocks(List<String> blocks) {
		instructionBlocks = blocks;
	}

	void setNutritionProperties(Map<String, Object> properties) {
		nutritionProperties = properties;
	}

    public void setNutritionDataNodes(List<Resource> nutritionNodes) {
		nutritionDataNodes = nutritionNodes;
	}

	public String getRecipePath() {
		return recipePath;
	}

	public Resource getRecipeResource() {
		return recipeResource;
	}

    public Resource getAssetResource() {
        return assetResource;
    }

	public Map<String, Object> getRecipeProperties() {
		return recipeProperties;
	}

	public List<String> getIngredientBlocks() {
		return ingredientBlocks;
	}

	public List<String> getInstructionBlocks() {
		return instructionBlocks;
	}

	public Map<String, Object> getNutritionProperties() {
		return nutritionProperties;
	}

    public List<Resource> getNutritionDataNodes() {
		return nutritionDataNodes;
	}

    public List<String> getAdditionalBlocks() {
        return additionalBlocks;
    }

    public void setAdditionalBlocks(List<String> additionalBlocks) {
        this.additionalBlocks = additionalBlocks;
    }

    /**
	 * Search for the Recipe content path by asset path
	 * 
	 * @return
	 */
	public String getRecipeContentPath() {
		StringBuilder builder = new StringBuilder();
		builder.append("/jcr:root").append(ContentRootPaths.RECIPES.path())
				.append("//element(*,cq:Page)[jcr:content/@sni:assetLink='")
				.append(recipePath.replace("/jcr:content","")).append("']");

		Iterator<Resource> iter = resolver.findResources(builder.toString(),
                Query.XPATH);
		if (iter.hasNext()) {
			return iter.next().getPath();
		} else {
			return null;
		}
	}
}
