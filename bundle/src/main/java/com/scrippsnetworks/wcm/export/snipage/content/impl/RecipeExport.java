package com.scrippsnetworks.wcm.export.snipage.content.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.foundation.Paragraph;
import com.day.cq.wcm.foundation.ParagraphSystem;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.recipe.FormatUtil;
import com.scrippsnetworks.wcm.recipe.Recipe;
import com.scrippsnetworks.wcm.recipe.RecipeFactory;
import com.scrippsnetworks.wcm.recipe.ingredients.Ingredient;
import com.scrippsnetworks.wcm.recipe.instructions.Instruction;
import com.scrippsnetworks.wcm.video.Video;

public class RecipeExport extends SniPageExport {

    public enum ExportProperty {
        RECIPE_COOKTIME(String.class),
        RECIPE_COOKTIME_MIN(String.class),
        RECIPE_PREPTIME(String.class),
        RECIPE_PREPTIME_MIN(String.class),
        RECIPE_NAPREPTIME(String.class),
        RECIPE_NAPREPTIME_MIN(String.class),
        RECIPE_TOTAL_TIME(String.class),
        RECIPE_TOTAL_TIME_MIN(String.class),
        RECIPE_DIFFICULTY_DISPLAY(String.class),
        RECIPE_COPYRIGHT(String.class),
        RECIPE_VIDEO_THUMBNAIL_URL(String.class),
        RECIPE_INGREDIENTS(String[].class),
        RECIPE_INSTRUCTIONS(String[].class),
        RECIPE_SERVINGS_PER_RECIPE(String.class),
        RECIPE_YIELD(String.class),
        RECIPE_WINE_RECOMMENDATION(String.class),
        RECIPE_RESTRICTED(String.class),
        CORE_EPISODE_ID(String.class),
        CORE_SOI(String.class);

        final Class clazz;

        ExportProperty(Class clazz) {
            this.clazz = clazz;
        }

        public Class valueClass() {
            return clazz;
        }
    }

    public static final String PAGE_PROP_SNI_COOKTIME = "sni:cookTime";
    public static final String PAGE_PROP_SNI_PREPARATION_TIME = "sni:preparationTime";
    public static final String PAGE_PROP_SNI_INACTIVE_PREPARATION_TIME = "sni:inactivePreparationTime";
    public static final String PAGE_PROP_SNI_DIFFICULTY = "sni:difficulty";
    public static final String PAGE_PROP_SNI_COPYRIGHT = "sni:copyright";
    public static final String PAGE_PROP_SNI_SOI = "sni:soi";
    public static final String PAGE_PROP_SNI_SERVINGS_PER_RECIPE = "sni:servingsPerRecipe";
    public static final String PAGE_PROP_SNI_YIELD = "sni:yield";
    public static final String PAGE_PROP_SNI_RESTRICTED = "sni:restricted";
    // public static final String PAGE_PROP_SNI_BEVERAGE_PAIRING = "sni:beveragePairing";
    public static final String PROP_PRIMARY_PAIRING = "primaryPairing";
    public static final String PROP_SNI_TITLE = "sni:title";
    public static final String CONTENT_WELL_PATH = "content-well";
    public static final String WINE_PAIRING_RESOURCE_TYPE = "sni-core/components/modules/wine-pairing";
    public static final String BEVERAGE_RESOURCE_TYPE = "sni-core/components/util/beverage";
    public static final String PARSYS_RESOURCE_TYPE = "foundation/components/parsys";
    public static final String ZERO_STRING = "0";

    private final Recipe recipe;

    public RecipeExport(SniPage sniPage) {
        super(sniPage);
        this.recipe = (new RecipeFactory()).withSniPage(sniPage).build();
        initialize();
    }

    protected RecipeExport(SniPage sniPage, Recipe recipe) {
        super(sniPage);
        this.recipe = recipe;
        initialize();
    }

    private void initialize() {
        if (sniPage == null || !sniPage.hasContent() || recipe == null) {
            return;
        }

       String cookTime = sniPage.getProperties().get(PAGE_PROP_SNI_COOKTIME, String.class);
        if (cookTime != null) {
        	setProperty(ExportProperty.RECIPE_COOKTIME_MIN.name(), cookTime);
            setProperty(ExportProperty.RECIPE_COOKTIME.name(), FormatUtil.getDisplayTime(cookTime));
        }

        String prepTime = sniPage.getProperties().get(PAGE_PROP_SNI_PREPARATION_TIME, String.class);
        if (prepTime != null) {
        	setProperty(ExportProperty.RECIPE_PREPTIME_MIN.name(), prepTime);
        	setProperty(ExportProperty.RECIPE_PREPTIME.name(), FormatUtil.getDisplayTime(prepTime));
        }

        String inactivePrepTime = sniPage.getProperties().get(PAGE_PROP_SNI_INACTIVE_PREPARATION_TIME, String.class);
        if (inactivePrepTime != null) {
        	setProperty(ExportProperty.RECIPE_NAPREPTIME_MIN.name(), inactivePrepTime);
            setProperty(ExportProperty.RECIPE_NAPREPTIME.name(), FormatUtil.getDisplayTime(inactivePrepTime));
        }

        String totalTimeInMin = getTotalTime(sniPage);
        setProperty(ExportProperty.RECIPE_TOTAL_TIME_MIN.name(), totalTimeInMin);
        setProperty(ExportProperty.RECIPE_TOTAL_TIME.name(), getFormattedTotalTime(totalTimeInMin));
        setProperty(ExportProperty.RECIPE_SERVINGS_PER_RECIPE.name(), sniPage.getProperties().get(PAGE_PROP_SNI_SERVINGS_PER_RECIPE, String.class));
        setProperty(ExportProperty.RECIPE_YIELD.name(), sniPage.getProperties().get(PAGE_PROP_SNI_YIELD, String.class));
        setProperty(ExportProperty.RECIPE_DIFFICULTY_DISPLAY.name(), sniPage.getProperties().get(PAGE_PROP_SNI_DIFFICULTY, String.class));
        setProperty(ExportProperty.RECIPE_COPYRIGHT.name(), sniPage.getProperties().get(PAGE_PROP_SNI_COPYRIGHT, String.class));
        setProperty(ExportProperty.CORE_SOI.name(), sniPage.getProperties().get(PAGE_PROP_SNI_SOI, String.class));
        setProperty(ExportProperty.RECIPE_VIDEO_THUMBNAIL_URL.name(), getVideoThumbnail(recipe));
        setProperty(ExportProperty.RECIPE_INGREDIENTS.name(), getIngredients(recipe));
        setProperty(ExportProperty.RECIPE_INSTRUCTIONS.name(), getInstructions(recipe));
        setProperty(ExportProperty.RECIPE_WINE_RECOMMENDATION.name(), getBeveragePairing(sniPage));
        setProperty(ExportProperty.RECIPE_RESTRICTED.name(), sniPage.getProperties().get(PAGE_PROP_SNI_RESTRICTED, String.class));
        
        SniPage talentPage = recipe.getRelatedTalentPage();
        if (talentPage != null) {
            setProperty(SniPageExport.ExportProperty.CORE_TALENT_ID.name(), talentPage.getUid());
            setProperty(SniPageExport.ExportProperty.CORE_TALENT_NAME.name(), talentPage.getTitle());
            setProperty(SniPageExport.ExportProperty.CORE_TALENT_URL.name(), talentPage.getUrl());
            setProperty(SniPageExport.ExportProperty.CORE_TALENT_FRIENDLY_URL.name(), talentPage.getFriendlyUrl());
        }

        SniPage showPage = recipe.getRelatedShowPage();
        if (showPage != null) {
            setProperty(SniPageExport.ExportProperty.CORE_SHOW_ID.name(), showPage.getUid());
        }

        SniPage episodePage = recipe.getRelatedEpisodePage();
        if (episodePage != null) {
            setProperty(ExportProperty.CORE_EPISODE_ID.name(), episodePage.getUid());
        }
    }

    private static String getTotalTime(SniPage sniPage) {
        String cookTime = sniPage.getProperties().get(PAGE_PROP_SNI_COOKTIME, ZERO_STRING);
        String prepTime = sniPage.getProperties().get(PAGE_PROP_SNI_PREPARATION_TIME, ZERO_STRING);
        String inactivePrepTime = sniPage.getProperties().get(PAGE_PROP_SNI_INACTIVE_PREPARATION_TIME, ZERO_STRING);

        Integer cookTimeInt;
        Integer prepTimeInt;
        Integer inactivePrepTimeInt;
        Integer totalTimeInt;
        try {
            cookTimeInt = Integer.parseInt(cookTime);
            prepTimeInt = Integer.parseInt(prepTime);
            inactivePrepTimeInt = Integer.parseInt(inactivePrepTime);
            totalTimeInt = cookTimeInt + prepTimeInt + inactivePrepTimeInt;
        } catch (NumberFormatException e) {
            return "";
        }
        String totalTime = String.valueOf(totalTimeInt);
        if (StringUtils.isNotBlank(totalTime) && !totalTime.equals("0")) {
        	return totalTime;
        }
        return "";
    }
    
    private static String getFormattedTotalTime(String totalTime) {
    	if(totalTime != null) {
    		return FormatUtil.getDisplayTime(totalTime);
    	}
    	return "";
    }
    
    private static String getVideoThumbnail(Recipe recipe) {
        if (recipe == null) {
            return null;
        }
        String thumbnailUrl = null;
        List<Video> videos = recipe.getVideos();
        if (videos != null && videos.size() > 0) {
            Video firstVideo = videos.get(0);
            thumbnailUrl = firstVideo.getThumbnailUrl();
        }
        return thumbnailUrl;
    }

    private static String[] getIngredients(Recipe recipe) {
        List<String> ingredientStr = new ArrayList<String>();
        for (Ingredient ingredient : recipe.getIngredients()) {
            if (ingredient.getTitle() != null) {
                ingredientStr.add(ingredient.getTitle());
            } else {
                String spacer = ingredient.getAmount() != null ? " " : "";
                ingredientStr.add(ingredient.getAmount() + spacer + ingredient.getName());
            }
        }
        return ingredientStr.toArray(new String[ingredientStr.size()]);
    }

    private static String[] getInstructions(Recipe recipe) {
        List<String> instructionStr = new ArrayList<String>();
        for (Instruction instruction : recipe.getInstructions()) {
            if (instruction.getText() != null) {
                instructionStr.add(instruction.getText());
            }
        }
        return instructionStr.toArray(new String[instructionStr.size()]);
    }

    private static String getBeveragePairing(SniPage sniPage) {
        if (sniPage == null || !sniPage.hasContent()) {
            return null;
        }

        String beveragePairing = null;

        /* Display code doesn't use migrated value, so we won't.
        beveragePairing = sniPage.getProperties().get(PAGE_PROP_SNI_BEVERAGE_PAIRING, String.class);
        */

        Resource contentWellRes = sniPage.getContentResource(CONTENT_WELL_PATH);
        if (contentWellRes != null) {
            if (contentWellRes.isResourceType(PARSYS_RESOURCE_TYPE)) {
                ParagraphSystem parsys = new ParagraphSystem(contentWellRes);
                for (Paragraph p : parsys.paragraphs()) {
                    if (p.getType() == Paragraph.Type.NORMAL
                            && p.isResourceType(WINE_PAIRING_RESOURCE_TYPE)) {
                        ValueMap wpProps = p.adaptTo(ValueMap.class);
                        if (wpProps != null) {
                            String beveragePairingPath = wpProps.get(PROP_PRIMARY_PAIRING, String.class);
                            if (beveragePairingPath != null) {
                                PageManager pm = sniPage.getPageManager();
                                if (pm != null) {
                                    Page beveragePairingPage = pm.getPage(beveragePairingPath);
                                    if (beveragePairingPage != null
                                            && beveragePairingPage.hasContent()
                                            && beveragePairingPage.getContentResource().isResourceType(BEVERAGE_RESOURCE_TYPE)) {
                                        beveragePairing = beveragePairingPage.getProperties().containsKey(PROP_SNI_TITLE)
                                                ? beveragePairingPage.getProperties().get(PROP_SNI_TITLE, String.class)
                                                : beveragePairingPage.getTitle();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return beveragePairing;
    }
}
