package com.scrippsnetworks.wcm.recipe.impl;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.common.collect.Sets;
import com.scrippsnetworks.wcm.fnr.util.PageSlingResourceTypes;
import com.scrippsnetworks.wcm.hub.Hub;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.PagePropertyConstants;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.recipe.FormatUtil;
import com.scrippsnetworks.wcm.recipe.Recipe;
import com.scrippsnetworks.wcm.recipe.categories.*;
import com.scrippsnetworks.wcm.recipe.data.*;
import com.scrippsnetworks.wcm.recipe.promotion.Promotion;
import com.scrippsnetworks.wcm.recipe.promotion.PromotionFactory;
import com.scrippsnetworks.wcm.recipe.ingredients.Ingredient;
import com.scrippsnetworks.wcm.recipe.ingredients.IngredientBlock;
import com.scrippsnetworks.wcm.recipe.ingredients.IngredientBlockFactory;
import com.scrippsnetworks.wcm.recipe.instructions.Instruction;
import com.scrippsnetworks.wcm.recipe.instructions.InstructionFactory;
import com.scrippsnetworks.wcm.recipe.notes.Note;
import com.scrippsnetworks.wcm.recipe.notes.NoteFactory;
import com.scrippsnetworks.wcm.recipe.nutrition.NutritionBlock;
import com.scrippsnetworks.wcm.recipe.related.Related;
import com.scrippsnetworks.wcm.recipe.related.RelatedFactory;
import com.scrippsnetworks.wcm.recipe.titlecombiner.TitleCombinerFactory;
import com.scrippsnetworks.wcm.recipe.warnings.WarningFactory;
import com.scrippsnetworks.wcm.relationship.RelationshipModel;
import com.scrippsnetworks.wcm.relationship.RelationshipModelFactory;
import com.scrippsnetworks.wcm.snitag.SniTag;
import com.scrippsnetworks.wcm.snitag.SniTagFactory;
import com.scrippsnetworks.wcm.util.PageTypes;
import com.scrippsnetworks.wcm.video.Video;
import com.scrippsnetworks.wcm.video.VideoFactory;
import com.scrippsnetworks.wcm.video.channel.Channel;
import com.scrippsnetworks.wcm.video.channel.ChannelFactory;

import java.text.SimpleDateFormat;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Recipe object for getting raw recipe data from recipe asset.
 * @author Jason Clark
 *         Date: 5/11/13
 */
public class RecipeImpl implements Recipe {
    private static final Logger LOG = LoggerFactory.getLogger(RecipeImpl.class);

    private static final String ASSET_LINK = "sni:assetLink";
    private static final String EXTRA_RELATED_CONTENT = "extra-related-content";
    private static final String SNI_IMAGES = "sni:images";

    private static final String PAGETYPE_VIDEO = "video";
    private static final String PAGETYPE_CHANNEL = "video-channel";

    private static final String CROSSLINK_SELECTOR = "a.crosslink";

    private static final String SNI_COPYRIGHT = "sni:copyright";
    private static final String SNI_COOK_TIME = "sni:cookTime";
    private static final String SNI_PREP_TIME = "sni:preparationTime";
    private static final String SNI_INACTIVE_PREP_TIME = "sni:inactivePreparationTime";
    private static final String SNI_DIFFICULTY = "sni:difficulty";
    private static final String SNI_RESTRICTED = "sni:restricted";
    
    private static final String CQ_LAST_REPLICATED = "cq:lastReplicated";
    
    private static final String CQ_TAGS = "cq:tags";
    private static final String HEALTHY = "healthy";
    
    /** The format to return the published date in. */
    private static SimpleDateFormat publishedDateFormat;

    private static final Logger log = LoggerFactory.getLogger(RecipeImpl.class);

    /** SniPage passed into constructor. */
    private SniPage sniPage;

    /** SniTags from SniPage. */
    private List<SniTag> tags;

    /** Path to recipe asset under sni-asset. */
    private String assetPath;

    /** Is the recipe restricted? */
    private boolean isRestricted;

    /** ValueMap of merged properties from SniPage. */
    private ValueMap properties;

    /** Member for ingredients blocks. */
    private List<Ingredient> ingredients;

    /** For internal use. Assemble ingredient blocks for title sharing. */
    private List<IngredientBlock> ingredientBlocks;

    /** Member for instruction blocks. */
    private List<Instruction> instructions;

    /** Member for recipe categories. */
    private List<Category> categories;

    /** Member for all recipe categories. */
    private List<SniTag> allTags;

    /** Member for content carousel pages. */
    private List<Related> relatedContentPages;

    /** Member for nutrition blocks. */
    private List<NutritionBlock> nutritionBlocks;

    /** Member for warnings. */
    private List<SniTag> warnings;

    /** Member for talent page related to this recipe. */
    private SniPage relatedTalentPage;

    /** Member for show page related to this recipe. */
    private SniPage relatedShowPage;

    /** Episdoe. */
    private SniPage relatedEpisodePage;

    /** Member for relationship model. */
    private RelationshipModel relationshipModel;

    /** Member for Hub in this Recipe. */
    private Hub recipeHub;

    /** Videos from hubbed video pages. */
    private List<Video> videos;

    /** Channels from the recipe hub. */
    private List<Channel> channels;

    /** Member for promotion */
    private Promotion promotion;

    /** Copyright from Recipe asset. */
    private String copyright;
    
    /** Date that the recipe page was published. */
    private String datePublished;

    /** Formatted total time (cook + prep + inactive prep). */
    private String totalTime;

    /** Cook time. */
    private String cookTime;

    /** Prep time. */
    private String prepTime;

    /** Inactive prep time. */
    private String inactivePrepTime;
    
    /** Difficulty for Recipe. */
    private String difficulty;

    /** Notes. */
    private List<Note> notes;
    
    /** Healthy. **/
    private boolean healthy = false;
    
    /** Healthy tag description **/
    
    private String healthyDescription;

    static {
        publishedDateFormat = new SimpleDateFormat("YYYY-MM-dd");
    }
    
    /**
     * Construct a new RecipeImpl.
     * @param sniPage SniPage for recipe page.
     */
    public RecipeImpl(SniPage sniPage) {
        this.sniPage = sniPage;
        if (sniPage != null) {
            properties = sniPage.getProperties();
            relationshipModel = new RelationshipModelFactory()
                    .withSniPage(sniPage)
                    .build();
        }
    }

    public RecipeImpl(Resource resource) {
        this.sniPage = PageFactory.getSniPage(resource.adaptTo(Page.class));
        if (sniPage != null) {
            properties = sniPage.getProperties();
            relationshipModel = new RelationshipModelFactory()
                    .withSniPage(sniPage)
                    .build();
        }
    }

    /**
     * Convenience method for returning merged properties of Recipe page/asset.
     * @return ValueMap of merged properties.
     */
    public ValueMap getProperties() {
        return properties;
    }

    /** If you need to retrieve the recipe page that was used in this recipe. */
    public SniPage getRecipePage() {
        return sniPage;
    }

    /** {@inheritDoc} */
    public List<SniTag> getSniTags() {
        if (tags == null) {
            tags = sniPage.getSniTags();
        }
        return tags;
    }


    /** {@inheritDoc} */
    public boolean getRestricted() {
        if (properties.containsKey(SNI_RESTRICTED)) {
            isRestricted = properties.get(SNI_RESTRICTED, Boolean.class);
        }
        return isRestricted;
    }

    /** Get the Recipe title */
    public String getTitle() {
        if (sniPage != null) {
            return sniPage.getTitle();
        }
        return "";
    }
    
    /** Get the Recipe description */
    public String getDescription() {
        if (sniPage != null) {
            return sniPage.getDescription();
        }
        return "";
    }


    /** Asset path to recipe under sni-asset. */
    public String getAssetPath() {
        if (assetPath == null) {
            if (properties.containsKey(ASSET_LINK)) {
                assetPath = properties.get(ASSET_LINK, String.class);
            }
        }
        return assetPath;
    }

    /**
     * Check if source is one of the sources which override the sponsorship assigned to the page.
     * @return boolean should you replace
     */
    public boolean hasOverrideAttribution() {
        return false;
    }

    /**
     * Retrieve a List of Ingredients.
     * Takes into account crosslinking, will choose crosslinked blocks over plain ones.
     * @return String ingredient blocks, sorted and aggregated.
     */
    public List<Ingredient> getIngredients() {
        if (ingredients == null) {
            ingredients = new ArrayList<Ingredient>();
            for (IngredientBlock ingredientBlock : getIngredientBlocks()) {
                List<Ingredient> ingredientList = ingredientBlock.getIngredients();
                if (ingredientList != null) {
                    ingredients.addAll(ingredientBlock.getIngredients());
                }
            }
        }
        return ingredients;
    }

    /* Convenience method for getting IngredientBlocks. */
    private List<IngredientBlock> getIngredientBlocks() {
        if (ingredientBlocks == null) {
            ingredientBlocks = new ArrayList<IngredientBlock>();
            DataReader dataReader = new DataReaderFactory()
                    .withSniPage(sniPage)
                    .readIngredients()
                    .build();
            if (dataReader != null) {
                List<Resource> blockResources = dataReader.getSortedBlocks();
                if (blockResources != null) {
                    for (Resource blockResource : dataReader.getSortedBlocks()) {
                        IngredientBlock ingredientBlock = new IngredientBlockFactory()
                                .withResource(blockResource)
                                .build();
                        if (ingredientBlock != null) {
                            ingredientBlocks.add(ingredientBlock);
                        }
                    }
                }
            }
        }
        return ingredientBlocks;
    }

    /**
     * Retrieve the blocks labeled Instructions from recipe asset.
     * Takes into account crosslinking, will choose crosslinked blocks over plain ones.
     * @return String instruction blocks, sorted and aggregated.
     */
    public List<Instruction> getInstructions() {
        if (instructions == null) {
            instructions = new ArrayList<Instruction>();
            DataReader reader = new DataReaderFactory()
                    .withSniPage(sniPage)
                    .readInstructions()
                    .readDirections()
                    .readOthers()
                    .shareTitles()
                    .build();
            if (reader != null) {
                List<Resource> blockResources = reader.getSortedBlocks();
                if (blockResources != null) {
                    List<Instruction> tempInstructions = new ArrayList<Instruction>();
                    for (Resource blockResource : reader.getSortedBlocks()) {
                        Instruction instruction = new InstructionFactory()
                                .withResource(blockResource)
                                .build();
                        if (instruction != null) {
                            tempInstructions.add(instruction);
                        }
                    }
                    if (tempInstructions.size() > 1) {
                        instructions = new TitleCombinerFactory()
                                .withInstructions(tempInstructions)
                                .withIngredientBlocks(getIngredientBlocks())
                                .build()
                                .getInstructions();
                    } else {
                        instructions = tempInstructions;
                    }
                }
            }
        }
        return instructions;
    }

    /**
     * Retrieve the blocks labeled notes from recipe asset.
     * Takes into account crosslinking, will choose crosslinked blocks over plain ones.
     * @return String notes blocks, sorted and aggregated.
     */
    public List<Note> getNotes() {
        if (notes == null) {
            DataReader reader = new DataReaderFactory()
                    .withSniPage(sniPage)
                    .readNotes()
                    .build();
            if (reader != null) {
                notes = new ArrayList<Note>();
                List<Resource> blockResources = reader.getSortedBlocks();
                if (blockResources != null) {
                    for (Resource blockResource : reader.getSortedBlocks()) {
                        Note note = new NoteFactory()
                                .withResource(blockResource)
                                .build();
                        if (note != null) {
                            notes.add(note);
                        }
                    }
                }
            }
        }
        return notes;
    }

    /** {@inheritDoc} */
    public List<Category> getCategories() {
        if (categories == null) {
            List<SniTag> categoryTags = getAllCategories();

            CategoryBlock categoryBlock = new CategoryBlockFactory()
                .withTags(categoryTags)
                .build();
            if (categoryBlock != null) {
                categories = categoryBlock.getCategories();
            }
        }
        return categories;
    }

    public List<SniTag> getAllCategories() {
        if (allTags == null) {
            allTags = new ArrayList<SniTag>();

            List<SniTag> contentTags = sniPage.getSniTags();
            allTags.addAll(contentTags);

            String[] rawCategories = sniPage.getProperties()
                .get(PagePropertyConstants.PROP_SNI_CATEGORIES, String[].class);

            if (rawCategories != null && rawCategories.length > 0) {
                for (String tag : rawCategories) {
                    String searchableTag = null;
                    if (StringUtils.isNotBlank(tag)) {
                        searchableTag = tag.replaceFirst("categories", "tags");
                    }
                    SniTag processedSniTag = new SniTagFactory()
                            .withTagText(searchableTag)
                            .withSniPage(sniPage)
                            .build();
                    if (processedSniTag != null) {
                        allTags.add(processedSniTag);
                    }
                }
            }

            Set<SniTag> uniqTags = Sets.newLinkedHashSet(allTags);
            allTags.clear();
            allTags.addAll(uniqTags);
        }

        return allTags;
    }

    /** {@inheritDoc} */
    public List<Related> getRelatedContentPages() {
        if (relatedContentPages == null) {
            relatedContentPages = new ArrayList<Related>();
            List<SniPage> extraPages = new ArrayList<SniPage>();
            if (sniPage != null) {
                if (recipeHub == null) {
                    recipeHub = sniPage.getHub();
                }
                if (recipeHub != null) {
                    for (SniPage hubPage : recipeHub.getHubChildren()) {
                        String resourceType = hubPage.getContentResource().getResourceType();
                        for (PageSlingResourceTypes type : PageSlingResourceTypes.values()) {
                            if (type.resourceType().equals(resourceType)) {
                                switch (type) {
                                    case ARTICLE_SIMPLE:
                                    case PHOTO_GALLERY:
                                        extraPages.add(hubPage);
                                }
                            }
                        }
                    }
                }
                if (properties.containsKey(EXTRA_RELATED_CONTENT)) {
                    String[] extraContent = properties.get(EXTRA_RELATED_CONTENT, String[].class);
                    PageManager pageManager = sniPage.getPageManager();
                    for (String extraPath : extraContent) {
                        SniPage extraPage = PageFactory.getSniPage(pageManager, extraPath);
                        extraPages.add(extraPage);
                    }
                }
            }

            for (SniPage extraPage : extraPages) {
                if (extraPage != null && extraPage.getContentResource() != null) {
                    String resourceType = extraPage.getContentResource().getResourceType();
                    for (PageSlingResourceTypes type : PageSlingResourceTypes.values()) {
                        if (type.resourceType().equals(resourceType)) {
                            switch (type) {
                                case ARTICLE_SIMPLE:
                                case PHOTO_GALLERY:
                                case VIDEO:
                                    Related relatedPage = new RelatedFactory()
                                        .withSniPage(extraPage)
                                        .build();
                                    relatedContentPages.add(relatedPage);
                            }
                        }
                    }
                }
            }
        }

        return relatedContentPages;
    }

    /** {@inheritDoc} */
    public List<NutritionBlock> getNutritionBlocks() {
        if (nutritionBlocks == null) {
            nutritionBlocks = new NutritionDataReaderFactory()
                    .withSniPage(sniPage)
                    .build()
                    .getNutritionBlocks();
        }
        return nutritionBlocks;
    }

    /** {@inheritDoc} */
    public List<SniTag> getWarnings() {
        if (warnings == null) {
            warnings = new WarningFactory()
                    .withSniPage(sniPage)
                    .build()
                    .getWarnings();
        }
        return warnings;
    }

    /** {@inheritDoc} */
    public SniPage getRelatedTalentPage() {
        if (relatedTalentPage == null && relationshipModel != null) {
            List<SniPage> talentPages = relationshipModel.getTalentPages();
            if (talentPages != null && talentPages.size() > 0) {
                relatedTalentPage = talentPages.get(0);
            }
        }
        return relatedTalentPage;
    }

    /** {@inheritDoc} */
    public SniPage getRelatedShowPage() {
        if (relatedShowPage == null && relationshipModel != null) {
            List<SniPage> showPages = relationshipModel.getShowPages();
            if (showPages != null && showPages.size() > 0) {
                relatedShowPage = showPages.get(0);
            }
        }
        return relatedShowPage;
    }

    /** {@inheritDoc} */
    public SniPage getRelatedEpisodePage() {
        if (relatedEpisodePage == null && relationshipModel != null) {
            List<SniPage> episodePages = relationshipModel.getEpisodePages();
            if (episodePages != null && episodePages.size() > 0) {
                relatedEpisodePage = episodePages.get(0);
            }
        }
        return relatedEpisodePage;
    }

    /** {@inheritDoc} */
    public String getImagePath() {
        String imagePath = null;
        if (properties != null && properties.containsKey(SNI_IMAGES)) {
            String[] images = properties.get(SNI_IMAGES, String[].class);
            if (images != null && images.length > 0) {
                imagePath = images[0];
            }
        }
        return imagePath;
    }

    /** {@inheritDoc} */
    public List<Video> getVideos() {
        if (videos == null) {
            videos = new ArrayList<Video>();
            if (sniPage != null) {
                if (recipeHub == null) {
                    recipeHub = sniPage.getHub();
                }
                if (recipeHub != null) {
                    for (SniPage hubPage : recipeHub.getHubChildren()) {
                        String type = hubPage.getPageType();
                        if (type.equals(PAGETYPE_VIDEO)) {
                            Video video = new VideoFactory()
                                        .withSniPage(hubPage)
                                        .build();
                            if (video != null) {
                                videos.add(video);
                            }
                        }
                    }
                }
            }
        }
        return videos;
    }

    /** {@inheritDoc} */
    public List<Channel> getChannels() {
        if (channels == null) {
            channels = new ArrayList<Channel>();
            if (sniPage != null) {
                if (recipeHub == null) {
                    recipeHub = sniPage.getHub();
                }
                if (recipeHub != null) {
                    for (SniPage hubPage : recipeHub.getHubChildren()) {
                        String type = hubPage.getPageType();
                        if (type.equals(PAGETYPE_CHANNEL)) {
                            Channel channel = new ChannelFactory()
                                .withSniPage(hubPage)
                                .build();
                            if (channel.getVideos().size() > 0){
                                channels.add(channel);
                            }
                        }
                    }
                }
            }
        }
        return channels;
    }

    /** {@inheritDoc} */
    public Promotion getPromotion() {
        if (promotion == null) {
            promotion = new PromotionFactory()
                .withSniPage(sniPage)
                .build();
        }

        return promotion;
    }
    
    /** {@inheritDoc} */
    public List<String> getCrosslinkedTerms() {
        List<String> allTerms = new ArrayList<String>();
        StringBuilder textBlocks = new StringBuilder();

        try {
            for (Instruction ctIn : getInstructions()) {
                textBlocks.append(ctIn.getText());
            }
            for (Ingredient ctIn : getIngredients()) {
                textBlocks.append(ctIn.getName());
            }

            Document fragmentDoc = Jsoup.parse(textBlocks.toString());
            Elements termElements = fragmentDoc.select(CROSSLINK_SELECTOR);

            for (Element tEl : termElements) {
                allTerms.add(tEl.text());
            }
        } catch (Exception e) {
            log.error("Parse error {}", e);
        }

        return allTerms;
    }

    /** {@inheritDoc} */
    public String getCopyright() {
        if (copyright == null) {
            if (properties != null && properties.containsKey(SNI_COPYRIGHT)) {
                copyright = properties.get(SNI_COPYRIGHT, String.class);
            }
        }
        return copyright;
    }

    /** {@inheritDoc} */
    public String getDatePublished() {
        if (datePublished == null) {
            if (properties != null && properties.containsKey(CQ_LAST_REPLICATED)) {
                datePublished = publishedDateFormat.format(properties.get(CQ_LAST_REPLICATED, Date.class));
            }
        }
        return datePublished;
    }
    
    /** {@inheritDoc} */
    public String getTotalTime() {
        if (totalTime == null) {
            if (properties != null) {
                String cook = properties.get(SNI_COOK_TIME, String.class);
                String prep = properties.get(SNI_PREP_TIME, String.class);
                String inactive = properties.get(SNI_INACTIVE_PREP_TIME, String.class);
                Integer cookInt = 0;
                Integer prepInt = 0;
                Integer inactiveInt = 0;
                try {
                    if (cook != null) {
                        cookInt = Integer.valueOf(cook);
                    }
                    if (prep != null) {
                        prepInt = Integer.valueOf(prep);
                    }
                    if (inactive != null) {
                        inactiveInt = Integer.valueOf(inactive);
                    }
                } catch (NumberFormatException nfe) {
                    log.error("NumberFormatException caught: {}", nfe);
                }
                totalTime = FormatUtil.getDisplayTime(cookInt + prepInt + inactiveInt);
            }
        }
        return totalTime;
    }

    /** {@inheritDoc} */
    public String getCookTime() {
        if (cookTime == null) {
            if (properties != null && properties.containsKey(SNI_COOK_TIME)) {
                cookTime = FormatUtil
                        .getDisplayTime(properties.get(SNI_COOK_TIME, String.class));
            }
        }
        return cookTime;
    }

    /** {@inheritDoc} */
    public String getPrepTime() {
        if (prepTime == null) {
            if (properties != null && properties.containsKey(SNI_PREP_TIME)) {
                prepTime = FormatUtil
                        .getDisplayTime(properties.get(SNI_PREP_TIME, String.class));
            }
        }
        return prepTime;
    }

    /** {@inheritDoc} */
    public String getInactivePrepTime() {
        if (inactivePrepTime == null) {
            if (properties != null && properties.containsKey(SNI_INACTIVE_PREP_TIME)) {
                inactivePrepTime = FormatUtil
                        .getDisplayTime(properties.get(SNI_INACTIVE_PREP_TIME, String.class));
            }
        }
        return inactivePrepTime;
    }

    /** {@inheritDoc} */
    public String getDifficulty() {
        if (difficulty == null) {
            if (properties != null && properties.containsKey(SNI_DIFFICULTY)) {
                difficulty = properties.get(SNI_DIFFICULTY, String.class);
                if (difficulty.equalsIgnoreCase("none")) {
                    difficulty = "";
                } else if (difficulty.equalsIgnoreCase("difficult")) {
                    difficulty = "Advanced";
                }
            }
        }
        return difficulty;
    }

    /** {@inheritDoc} */
    public String getHubOverlayType() {
        String overlayType = null;

        if (recipeHub == null) {
            recipeHub = sniPage.getHub();
        }

        if (recipeHub != null) {
            for (SniPage hubPage : recipeHub.getHubChildren()) {
                PageTypes type = PageTypes.findPageType(hubPage.getPageType());
                switch (type) {
                    case VIDEO:
                    case VIDEO_CHANNEL:
                    case VIDEO_PLAYER:
                        overlayType = "ss-play";
                        break;
                    default:
                        break;
                }
                if (overlayType != null) {
                    break;
                }
            }
        }

        return overlayType == null ? "" : overlayType;
    }
    
    /** {@inheritDoc} */
    public boolean getHealthy() {
        if (healthyDescription == null) {
            setHealthyFlagAndDescription();
        }
        return healthy;
    }    
    
    /** {@inheritDoc} */
    public String getHealthyDescription() {
        if (healthyDescription == null) {
            setHealthyFlagAndDescription();
        }
        return healthyDescription;
    }

    /** Convenience method for healthy flag and description logic. */
    private void setHealthyFlagAndDescription() {
        if (healthyDescription == null) {
            List<SniTag> sniTags = getSniTags();
            if (sniTags.size() > 0) {
                for (SniTag tag : sniTags) {
                    String value = tag.getValue();
                    if (StringUtils.isNotBlank(value) && value.equals(HEALTHY)) {
                        healthyDescription = tag.getDescription();
                        healthy = true;
                        break;
                    }
                }
            }
        }
        if (healthyDescription == null) {
            healthyDescription = "";
        }
    }
    
}

