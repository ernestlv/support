package com.scrippsnetworks.wcm.export.snipage.content.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.export.snipage.content.PageExport;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.recipe.FormatUtil;
import com.scrippsnetworks.wcm.recipe.Recipe;
import com.scrippsnetworks.wcm.recipe.ingredients.Ingredient;
import com.scrippsnetworks.wcm.recipe.instructions.Instruction;
import com.scrippsnetworks.wcm.video.Video;

public class RecipeExportTest {

	private Logger log = LoggerFactory.getLogger(RecipeExportTest.class);
			
    public static final String RECIPE_PATH = "/content/food/recipes/a/a-/a-r/a-re/a-recipe";
    public static final String RECIPE_IMAGE_PATH = "/content/dam/images/food/unsized/2013/07/31/0/an-image.jpg";
    public static final String RECIPE_PAGE_TYPE = "recipe";
    public static final String PROP_COOK_TIME = "10";
    public static final String PROP_COOK_TIME_MIN = "10";
    public static final String PROP_PREP_TIME = "5";
    public static final String PROP_PREP_TIME_MIN = "5";
    public static final String PROP_INACTIVE_PREP_TIME = "3";
    public static final String PROP_INACTIVE_PREP_TIME_MIN = "3";
    public static final String PROP_SERVINGS_PER_RECIPE = "4";
    public static final String PROP_YIELD = "about 4 cups";
    public static final String PROP_DIFFICULTY = "Flipping Impossible";
    public static final String PROP_COPYRIGHT = "Recipe courtesy Yeti Cat, 2013";
    public static final String PROP_RESTRICTED = "true";
    public static final String PROP_SOI = "1234";
    public static final String RECIPE_VIDEO_THUMBNAIL_URL = "http://video.thumbnails.r.us/12345678.jpg";
    public static final String WINE_RECOMMENDATION = "Ripple";
    public static final String SHOW_UID = "dead-beef-dead-beef";
    public static final String EPISODE_UID = "1337-1337-1337-1337";
    public static final String TALENT_PATH = "/content/food/chefs/a/a-chef";
    public static final String TALENT_UID = "aaaa-bbbb-cccc-dddd";
    public static final String TALENT_URL = "/chefs/a/aa/a-chef.html";
    public static final String TALENT_FRIENDLY_URL = "/chefs/a-chef.html";
    public static final String TALENT_NAME = "A Talent";
    public static final String WINE_PAIRING_RESOURCE_TYPE = "sni-food/components/modules/wine-pairing";
    public static final String PRIMARY_PAIRING_PATH = "/content/beverages/ripple";

    public static final String AMOUNT = "1 pound";
    public static final String INGREDIENT1 = "apple wood bacon";
    public static final String INGREDIENT2 = "hickory bacon";
    public static final String INGREDIENT3 = "For the extra bacon:";
    public static final String INGREDIENT4 = "smoked bbacon";

    public static final String INSTRUCTION1 = "Place 2 sheets of paper towel on a microwave safe plate, lay the bacon out on the paper towel not overlapping the slices. Place 2 more sheets of paper towel on top. Place in the microwave on high for 4 to 6 minutes.";
    public static final String INSTRUCTION2 = "For the extra bacon, repeat the above.";


    @Mock SniPage recipePage;
    @Mock SniPage talentPage;
    @Mock SniPage episodePage;
    @Mock SniPage showPage;
    @Mock Recipe recipe;
    @Mock Video video;

    @Mock Resource recipePageCR;
    @Mock ValueMap recipePageProperties;
    @Mock Resource contentWell;
    @Mock Resource winePairingModule;
    @Mock ValueMap winePairingProps;
    @Mock Page beveragePage;
    @Mock ValueMap beveragePageProperties;
    @Mock Resource beveragePageCR;

    @Mock Ingredient ingredient1;
    @Mock Ingredient ingredient2;
    @Mock Ingredient ingredient3;
    @Mock Ingredient ingredient4;

    @Mock Instruction instruction1;
    @Mock Instruction instruction2;

    @Mock PageManager pageManager;
    @Mock ResourceResolver resourceResolver;

    @Mock SniPage emptyRecipePage;
    @Mock Resource emptyRecipePageResource;
    @Mock Recipe nullMethodsRecipe;

    List<Resource> resourceList;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        when(recipePage.getProperties()).thenReturn(recipePageProperties);
        when(recipePage.getContentResource()).thenReturn(recipePageCR);
        when(recipePage.getPath()).thenReturn(RECIPE_PATH);
        when(recipePage.getContentResource(RecipeExport.CONTENT_WELL_PATH)).thenReturn(contentWell);
        when(recipePage.getPageType()).thenReturn(RECIPE_PAGE_TYPE);
        when(recipePage.hasContent()).thenReturn(true);
        when(recipePage.getPageManager()).thenReturn(pageManager);

        when(recipePageProperties.get(RecipeExport.PAGE_PROP_SNI_COOKTIME, String.class)).thenReturn(PROP_COOK_TIME);
        when(recipePageProperties.get(RecipeExport.PAGE_PROP_SNI_PREPARATION_TIME, String.class)).thenReturn(PROP_PREP_TIME);
        when(recipePageProperties.get(RecipeExport.PAGE_PROP_SNI_INACTIVE_PREPARATION_TIME, String.class)).thenReturn(PROP_INACTIVE_PREP_TIME);
        when(recipePageProperties.get(RecipeExport.PAGE_PROP_SNI_COOKTIME, RecipeExport.ZERO_STRING)).thenReturn(PROP_COOK_TIME);
        when(recipePageProperties.get(RecipeExport.PAGE_PROP_SNI_PREPARATION_TIME, RecipeExport.ZERO_STRING)).thenReturn(PROP_PREP_TIME);
        when(recipePageProperties.get(RecipeExport.PAGE_PROP_SNI_INACTIVE_PREPARATION_TIME, RecipeExport.ZERO_STRING)).thenReturn(PROP_INACTIVE_PREP_TIME);
        when(recipePageProperties.get(RecipeExport.PAGE_PROP_SNI_SERVINGS_PER_RECIPE, String.class)).thenReturn(PROP_SERVINGS_PER_RECIPE);
        when(recipePageProperties.get(RecipeExport.PAGE_PROP_SNI_YIELD, String.class)).thenReturn(PROP_YIELD);
        when(recipePageProperties.get(RecipeExport.PAGE_PROP_SNI_DIFFICULTY, String.class)).thenReturn(PROP_DIFFICULTY);
        when(recipePageProperties.get(RecipeExport.PAGE_PROP_SNI_SOI, String.class)).thenReturn(PROP_SOI);
        when(recipePageProperties.get(RecipeExport.PAGE_PROP_SNI_COPYRIGHT, String.class)).thenReturn(PROP_COPYRIGHT);
        when(recipePageProperties.get(RecipeExport.PAGE_PROP_SNI_RESTRICTED, String.class)).thenReturn(PROP_RESTRICTED);

        when(recipe.getImagePath()).thenReturn(RECIPE_IMAGE_PATH);
        when(recipe.getIngredients()).thenReturn(Arrays.asList(ingredient1, ingredient2, ingredient3, ingredient4));
        when(recipe.getInstructions()).thenReturn(Arrays.asList(instruction1, instruction2));
        when(recipe.getRelatedTalentPage()).thenReturn(talentPage);
        when(recipe.getRelatedEpisodePage()).thenReturn(episodePage);
        when(recipe.getRelatedShowPage()).thenReturn(showPage);
        when(recipe.getVideos()).thenReturn(Arrays.asList(video));

        when(showPage.getUid()).thenReturn(SHOW_UID);
        when(episodePage.getUid()).thenReturn(EPISODE_UID);
        when(talentPage.getPath()).thenReturn(TALENT_PATH);
        when(talentPage.getUid()).thenReturn(TALENT_UID);
        when(talentPage.getUrl()).thenReturn(TALENT_URL);
        when(talentPage.getFriendlyUrl()).thenReturn(TALENT_FRIENDLY_URL);
        when(talentPage.getTitle()).thenReturn(TALENT_NAME);

        when(ingredient1.getAmount()).thenReturn(AMOUNT);
        when(ingredient1.getName()).thenReturn(INGREDIENT1);
        when(ingredient2.getAmount()).thenReturn(AMOUNT);
        when(ingredient2.getName()).thenReturn(INGREDIENT2);
        when(ingredient3.getTitle()).thenReturn(INGREDIENT3);
        when(ingredient4.getAmount()).thenReturn(AMOUNT);
        when(ingredient4.getName()).thenReturn(INGREDIENT4);

        when(instruction1.getText()).thenReturn(INSTRUCTION1);
        when(instruction2.getText()).thenReturn(INSTRUCTION2);

        when(contentWell.getResourceType()).thenReturn(RecipeExport.PARSYS_RESOURCE_TYPE);
        when(contentWell.isResourceType(RecipeExport.PARSYS_RESOURCE_TYPE)).thenReturn(true);
        when(contentWell.getPath()).thenReturn(RECIPE_PATH + "/jcr:content/" + RecipeExport.CONTENT_WELL_PATH);
        when(contentWell.getResourceResolver()).thenReturn(resourceResolver);
        resourceList = new ArrayList<Resource>();
        resourceList.add(winePairingModule);
        when(resourceResolver.listChildren(contentWell)).thenReturn(resourceList.iterator());
        when(winePairingModule.adaptTo(ValueMap.class)).thenReturn(winePairingProps);
        when(winePairingModule.getResourceType()).thenReturn(RecipeExport.WINE_PAIRING_RESOURCE_TYPE);
        when(winePairingModule.isResourceType(RecipeExport.WINE_PAIRING_RESOURCE_TYPE)).thenReturn(true);
        when(winePairingProps.get(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY, String.class)).thenReturn(WINE_PAIRING_RESOURCE_TYPE);
        when(winePairingProps.get(RecipeExport.PROP_PRIMARY_PAIRING, String.class)).thenReturn(PRIMARY_PAIRING_PATH);
        when(pageManager.getPage(PRIMARY_PAIRING_PATH)).thenReturn(beveragePage);
        when(beveragePage.hasContent()).thenReturn(true);
        when(beveragePage.getProperties()).thenReturn(beveragePageProperties);
        when(beveragePage.getContentResource()).thenReturn(beveragePageCR);
        when(beveragePageCR.isResourceType(RecipeExport.BEVERAGE_RESOURCE_TYPE)).thenReturn(true);
        when(beveragePageProperties.containsKey(RecipeExport.PROP_SNI_TITLE)).thenReturn(true);
        when(beveragePageProperties.get(RecipeExport.PROP_SNI_TITLE, String.class)).thenReturn(WINE_RECOMMENDATION);

        when(video.getThumbnailUrl()).thenReturn(RECIPE_VIDEO_THUMBNAIL_URL);
    }

    /** Tests every property in RecipeExport.ExportProperty to ensure that a value is set,
     *
     * Essentially this tests that the mocking was sufficient for every property the export class provides,
     * and that the export class actually sets all the properties.
     */
    @Test
    public void testAllPropertiesSetup() {
        PageExport pageExport = new RecipeExport(recipePage, recipe);
        ValueMap exportProps = pageExport.getValueMap();

        for (RecipeExport.ExportProperty prop : RecipeExport.ExportProperty.values()) {
            assertNotNull(prop.name(), exportProps.get(prop.name(), prop.valueClass()));
        }

        EnumSet<SniPageExport.ExportProperty> overrides = EnumSet.of(
                SniPageExport.ExportProperty.CORE_TALENT_ID,
                SniPageExport.ExportProperty.CORE_TALENT_NAME,
                SniPageExport.ExportProperty.CORE_TALENT_URL,
                SniPageExport.ExportProperty.CORE_TALENT_FRIENDLY_URL);

        Iterator<SniPageExport.ExportProperty> iterator = overrides.iterator();
        while (iterator.hasNext()) {
            SniPageExport.ExportProperty prop = iterator.next();
            assertNotNull(prop.name(), exportProps.get(prop.name(), prop.valueClass()));
        }

    }

    @Test
    public void testPropertyValues() {
        PageExport pageExport = new RecipeExport(recipePage, recipe);
        ValueMap exportProps = pageExport.getValueMap();

        assertEquals(RecipeExport.ExportProperty.CORE_SOI.name(), PROP_SOI,
                exportProps.get(RecipeExport.ExportProperty.CORE_SOI.name(),
                        RecipeExport.ExportProperty.CORE_SOI.valueClass()));

        assertEquals(RecipeExport.ExportProperty.CORE_EPISODE_ID.name(), EPISODE_UID,
                exportProps.get(RecipeExport.ExportProperty.CORE_EPISODE_ID.name(),
                        RecipeExport.ExportProperty.CORE_EPISODE_ID.valueClass()));

        assertEquals(SniPageExport.ExportProperty.CORE_SHOW_ID.name(), SHOW_UID,
                exportProps.get(SniPageExport.ExportProperty.CORE_SHOW_ID.name(),
                        SniPageExport.ExportProperty.CORE_SHOW_ID.valueClass()));

        assertEquals(SniPageExport.ExportProperty.CORE_TALENT_ID.name(), TALENT_UID,
                exportProps.get(SniPageExport.ExportProperty.CORE_TALENT_ID.name(),
                        SniPageExport.ExportProperty.CORE_TALENT_ID.valueClass()));

        assertEquals(SniPageExport.ExportProperty.CORE_TALENT_NAME.name(), TALENT_NAME,
                exportProps.get(SniPageExport.ExportProperty.CORE_TALENT_NAME.name(),
                        SniPageExport.ExportProperty.CORE_TALENT_NAME.valueClass()));

        assertEquals(SniPageExport.ExportProperty.CORE_TALENT_URL.name(), TALENT_URL,
                exportProps.get(SniPageExport.ExportProperty.CORE_TALENT_URL.name(),
                        SniPageExport.ExportProperty.CORE_TALENT_URL.valueClass()));

        assertEquals(SniPageExport.ExportProperty.CORE_TALENT_FRIENDLY_URL.name(), TALENT_FRIENDLY_URL,
                exportProps.get(SniPageExport.ExportProperty.CORE_TALENT_FRIENDLY_URL.name(),
                        SniPageExport.ExportProperty.CORE_TALENT_FRIENDLY_URL.valueClass()));

        assertEquals(RecipeExport.ExportProperty.RECIPE_COOKTIME_MIN.name(), PROP_COOK_TIME_MIN,
                exportProps.get(RecipeExport.ExportProperty.RECIPE_COOKTIME_MIN.name(),
                        RecipeExport.ExportProperty.RECIPE_COOKTIME_MIN.valueClass()));
        
        assertEquals(RecipeExport.ExportProperty.RECIPE_COOKTIME.name(), FormatUtil.getDisplayTime(PROP_COOK_TIME),
                exportProps.get(RecipeExport.ExportProperty.RECIPE_COOKTIME.name(),
                        RecipeExport.ExportProperty.RECIPE_COOKTIME.valueClass()));

        assertEquals(RecipeExport.ExportProperty.RECIPE_PREPTIME_MIN.name(), PROP_PREP_TIME_MIN,
                exportProps.get(RecipeExport.ExportProperty.RECIPE_PREPTIME_MIN.name(),
                        RecipeExport.ExportProperty.RECIPE_PREPTIME_MIN.valueClass()));
        
        assertEquals(RecipeExport.ExportProperty.RECIPE_PREPTIME.name(), FormatUtil.getDisplayTime(PROP_PREP_TIME),
                exportProps.get(RecipeExport.ExportProperty.RECIPE_PREPTIME.name(),
                        RecipeExport.ExportProperty.RECIPE_PREPTIME.valueClass()));

        assertEquals(RecipeExport.ExportProperty.RECIPE_NAPREPTIME_MIN.name(), PROP_INACTIVE_PREP_TIME_MIN,
                exportProps.get(RecipeExport.ExportProperty.RECIPE_NAPREPTIME_MIN.name(),
                        RecipeExport.ExportProperty.RECIPE_NAPREPTIME_MIN.valueClass()));
        
        assertEquals(RecipeExport.ExportProperty.RECIPE_NAPREPTIME.name(), FormatUtil.getDisplayTime(PROP_INACTIVE_PREP_TIME),
                exportProps.get(RecipeExport.ExportProperty.RECIPE_NAPREPTIME.name(),
                        RecipeExport.ExportProperty.RECIPE_NAPREPTIME.valueClass()));

        
        assertEquals(RecipeExport.ExportProperty.RECIPE_TOTAL_TIME_MIN.name(),
        		String.valueOf(
                        Integer.valueOf(PROP_COOK_TIME)
                        + Integer.valueOf(PROP_PREP_TIME)
                        + Integer.valueOf(PROP_INACTIVE_PREP_TIME)),
                exportProps.get(RecipeExport.ExportProperty.RECIPE_TOTAL_TIME_MIN.name(),
                        RecipeExport.ExportProperty.RECIPE_TOTAL_TIME_MIN.valueClass()));
        
        assertEquals(RecipeExport.ExportProperty.RECIPE_TOTAL_TIME.name(),
                FormatUtil.getDisplayTime(String.valueOf(
                        Integer.valueOf(PROP_COOK_TIME)
                        + Integer.valueOf(PROP_PREP_TIME)
                        + Integer.valueOf(PROP_INACTIVE_PREP_TIME))),
                exportProps.get(RecipeExport.ExportProperty.RECIPE_TOTAL_TIME.name(),
                        RecipeExport.ExportProperty.RECIPE_TOTAL_TIME.valueClass()));

        assertEquals(RecipeExport.ExportProperty.RECIPE_COPYRIGHT.name(), PROP_COPYRIGHT,
                exportProps.get(RecipeExport.ExportProperty.RECIPE_COPYRIGHT.name(),
                        RecipeExport.ExportProperty.RECIPE_COPYRIGHT.valueClass()));

        assertEquals(RecipeExport.ExportProperty.RECIPE_RESTRICTED.name(), PROP_RESTRICTED,
                exportProps.get(RecipeExport.ExportProperty.RECIPE_RESTRICTED.name(),
                        RecipeExport.ExportProperty.RECIPE_RESTRICTED.valueClass()));

        assertEquals(RecipeExport.ExportProperty.RECIPE_SERVINGS_PER_RECIPE.name(), PROP_SERVINGS_PER_RECIPE,
                exportProps.get(RecipeExport.ExportProperty.RECIPE_SERVINGS_PER_RECIPE.name(),
                        RecipeExport.ExportProperty.RECIPE_SERVINGS_PER_RECIPE.valueClass()));

        assertEquals(RecipeExport.ExportProperty.RECIPE_YIELD.name(), PROP_YIELD,
                exportProps.get(RecipeExport.ExportProperty.RECIPE_YIELD.name(),
                        RecipeExport.ExportProperty.RECIPE_YIELD.valueClass()));

        assertEquals(RecipeExport.ExportProperty.RECIPE_WINE_RECOMMENDATION.name(), WINE_RECOMMENDATION,
                exportProps.get(RecipeExport.ExportProperty.RECIPE_WINE_RECOMMENDATION.name(),
                        RecipeExport.ExportProperty.RECIPE_WINE_RECOMMENDATION.valueClass()));

        assertEquals(RecipeExport.ExportProperty.RECIPE_DIFFICULTY_DISPLAY.name(), PROP_DIFFICULTY,
                exportProps.get(RecipeExport.ExportProperty.RECIPE_DIFFICULTY_DISPLAY.name(),
                        RecipeExport.ExportProperty.RECIPE_DIFFICULTY_DISPLAY.valueClass()));

        assertEquals(RecipeExport.ExportProperty.RECIPE_VIDEO_THUMBNAIL_URL.name(), RECIPE_VIDEO_THUMBNAIL_URL,
                exportProps.get(RecipeExport.ExportProperty.RECIPE_VIDEO_THUMBNAIL_URL.name(),
                        RecipeExport.ExportProperty.RECIPE_VIDEO_THUMBNAIL_URL.valueClass()));

        String[] ingredients = (String[])exportProps.get(RecipeExport.ExportProperty.RECIPE_INGREDIENTS.name(),
                                RecipeExport.ExportProperty.RECIPE_INGREDIENTS.valueClass());

        assertEquals(RecipeExport.ExportProperty.RECIPE_INGREDIENTS.name(), recipe.getIngredients().size(),
                ingredients.length);

        int i = 0;
        for (Ingredient ingredient : recipe.getIngredients()) {
            String spacer = ingredient.getAmount() == null ? "" : " ";
            String ingredientValue = ingredient.getTitle() != null ? ingredient.getTitle() : ingredient.getAmount() + spacer + ingredient.getName();
            assertEquals("ingredient " + String.valueOf(i), ingredientValue, ingredients[i++]);
        }

        String[] instructions = (String[])exportProps.get(RecipeExport.ExportProperty.RECIPE_INSTRUCTIONS.name(),
                                RecipeExport.ExportProperty.RECIPE_INSTRUCTIONS.valueClass());

        assertEquals(RecipeExport.ExportProperty.RECIPE_INSTRUCTIONS.name(), recipe.getInstructions().size(),
                instructions.length);

        i = 0;
        for (Instruction instruction : recipe.getInstructions()) {
            assertEquals("instruction " + String.valueOf(i), instruction.getText(), instructions[i++]);
        }
    }

    @Test
    public void testNoPageContent() {
        when(recipePage.hasContent()).thenReturn(false);

        PageExport pageExport = new RecipeExport(recipePage, recipe);
        assertNotNull("nonnull value map", pageExport.getValueMap());
    }

    @Test
    public void testNullRecipe() {
        PageExport pageExport = new RecipeExport(recipePage, null);
        assertNotNull("nonnull value map", pageExport.getValueMap());
    }

    @Test
    public void testNoPageProperties() {
        when(emptyRecipePage.getProperties()).thenReturn(ValueMap.EMPTY);
        when(emptyRecipePageResource.adaptTo(ValueMap.class)).thenReturn(ValueMap.EMPTY);
        when(emptyRecipePage.hasContent()).thenReturn(true);

        PageExport pageExport = new RecipeExport(emptyRecipePage, null);
        ValueMap exportProps = pageExport.getValueMap();
        assertNotNull("value map nonnull", exportProps);

        // the map is functional but empty
        for (String key : exportProps.keySet()) {
            // since booleans can default nonnull, they're exempt from null check
            if (exportProps.get(key) instanceof Boolean) {
                continue;
            }
            assertNull("key " + key + " null value", exportProps.get(key));
        }
    }

    @Test
    public void testNullRecipeMethods() {
        when(emptyRecipePage.getProperties()).thenReturn(ValueMap.EMPTY);
        when(emptyRecipePageResource.adaptTo(ValueMap.class)).thenReturn(ValueMap.EMPTY);
        when(emptyRecipePage.hasContent()).thenReturn(true);

        PageExport pageExport = new RecipeExport(emptyRecipePage, nullMethodsRecipe);
        ValueMap exportProps = pageExport.getValueMap();
        assertNotNull("value map nonnull", exportProps);

        // the map is functional but empty
        for (String key : exportProps.keySet()) {
            // since booleans can default nonnull, they're exempt from null check
        	if (exportProps.get(key) instanceof Boolean) {
                continue;
            }
            
            assertNull("key " + key + " null value", exportProps.get(key));
        }
    }

    @Test
    public void testMissingRecipeRelations() {
        when(recipe.getRelatedEpisodePage()).thenReturn(null);
        when(recipe.getRelatedShowPage()).thenReturn(null);
        when(recipe.getRelatedTalentPage()).thenReturn(null);

        PageExport pageExport = new RecipeExport(recipePage, recipe);
        ValueMap exportProps = pageExport.getValueMap();

        assertNotNull("nonnull value map", exportProps);
        assertFalse("value map not empty", exportProps.isEmpty());

        assertNull(RecipeExport.ExportProperty.CORE_EPISODE_ID.name(),
                exportProps.get(RecipeExport.ExportProperty.CORE_EPISODE_ID.name(),
                        RecipeExport.ExportProperty.CORE_EPISODE_ID.valueClass()));

        assertNull(SniPageExport.ExportProperty.CORE_SHOW_ID.name(),
                exportProps.get(SniPageExport.ExportProperty.CORE_SHOW_ID.name(),
                        SniPageExport.ExportProperty.CORE_SHOW_ID.valueClass()));

        assertNull(SniPageExport.ExportProperty.CORE_TALENT_ID.name(),
                exportProps.get(SniPageExport.ExportProperty.CORE_TALENT_ID.name(),
                        SniPageExport.ExportProperty.CORE_TALENT_ID.valueClass()));

        assertNull(SniPageExport.ExportProperty.CORE_TALENT_NAME.name(),
                exportProps.get(SniPageExport.ExportProperty.CORE_TALENT_NAME.name(),
                        SniPageExport.ExportProperty.CORE_TALENT_NAME.valueClass()));

        assertNull(SniPageExport.ExportProperty.CORE_TALENT_URL.name(),
                exportProps.get(SniPageExport.ExportProperty.CORE_TALENT_URL.name(),
                        SniPageExport.ExportProperty.CORE_TALENT_URL.valueClass()));

        assertNull(SniPageExport.ExportProperty.CORE_TALENT_FRIENDLY_URL.name(),
                exportProps.get(SniPageExport.ExportProperty.CORE_TALENT_FRIENDLY_URL.name(),
                        SniPageExport.ExportProperty.CORE_TALENT_FRIENDLY_URL.valueClass()));
    }
}
