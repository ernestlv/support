package com.scrippsnetworks.wcm.recipe;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.recipe.categories.Category;
import com.scrippsnetworks.wcm.recipe.ingredients.Ingredient;
import com.scrippsnetworks.wcm.recipe.instructions.Instruction;
import com.scrippsnetworks.wcm.recipe.notes.Note;
import com.scrippsnetworks.wcm.recipe.nutrition.NutritionBlock;
import com.scrippsnetworks.wcm.recipe.promotion.Promotion;
import com.scrippsnetworks.wcm.recipe.related.Related;
import com.scrippsnetworks.wcm.snitag.SniTag;
import com.scrippsnetworks.wcm.video.Video;
import com.scrippsnetworks.wcm.video.channel.Channel;
import org.apache.sling.api.resource.ValueMap;

import java.util.List;

/**
 * @author Jason Clark
 *         Date: 5/11/13
 */
public interface Recipe {

    /** Merged recipe content page + asset properties */
    public ValueMap getProperties();

    /** If you need the content page the recipe came from. */
    public SniPage getRecipePage();

    /** SniTags from the Recipe page. */
    public List<SniTag> getSniTags();
    
    /** Get the Recipe title */
    public String getTitle();
    
    /** Get the Recipe description */
    public String getDescription();

    /** Return path to recipe asset. */
    public String getAssetPath();

    /** If source attribution clobbers the sponsorship (FN Magazine, Entwine etc). */
    public boolean hasOverrideAttribution();

    /** List of Ingredients. */
    public List<Ingredient> getIngredients();

    /** Instructions text. */
    public List<Instruction> getInstructions();

    /** Categories from this Recipe. */
    public List<Category> getCategories();

    /** Tags/Categories from this Recipe. */
    public List<SniTag> getAllCategories();

    /** Content Carousel for this Recipe. */
    public List<Related> getRelatedContentPages();

    /** NutritionBlocks for this Recipe. */
    public List<NutritionBlock> getNutritionBlocks();

    /** Warnings for this Recipe. */
    public List<SniTag> getWarnings();

    /** The talent associated with this recipe via asset relationships. */
    public SniPage getRelatedTalentPage();

    /** The show associated with this recipe via asset relationships. */
    public SniPage getRelatedShowPage();

    /** The episdoe related to this recipe via asset relationships. */
    public SniPage getRelatedEpisodePage();

    /** Path to the fullset image related to this Recipe. */
    public String getImagePath();

    /** Retrieve a video to display on the Recipe page, from hub. */
    public List<Video> getVideos();

    /** Retrieve a Channel from the Recipe hub. */
    public List<Channel> getChannels();

    /** Grab package anchor page or next recipe */
    public Promotion getPromotion();

    /** Copyright text from this Recipe. */
    public String getCopyright();

    /** Get the formatted date published, used for schema.org markup */
    public String getDatePublished();
    
    /** Get the formatted total cook, prep and inactive prep times. */
    public String getTotalTime();

    /** Get the formatted cook time. */
    public String getCookTime();

    /** Get the formatted prep time. */
    public String getPrepTime();

    /** Get the formatted inactive prep time. */
    public String getInactivePrepTime();
	
	/** Retrieve list of text terms that are crosslinked */
    public List<String> getCrosslinkedTerms();
    
    /** Get the difficulty level of the recipe. */
    public String getDifficulty();

    /** Get overlay type */
    public String getHubOverlayType();

    /** Get the editor/cook's notes */
    public List<Note> getNotes();
    
    /** Get Healthy Label **/
    public boolean getHealthy();
    
    /** Get Healthy tag description **/
    public String getHealthyDescription();

    /** Check if the recipe is restricted. */
    public boolean getRestricted();
}
