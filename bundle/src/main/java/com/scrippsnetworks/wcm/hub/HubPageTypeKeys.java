package com.scrippsnetworks.wcm.hub;

import com.scrippsnetworks.wcm.fnr.util.PageSlingResourceTypes;
import com.scrippsnetworks.wcm.page.SniPage;

/** THE ORDER OF THE VALUES DETERMINES THE ORDER THE HUB PAGES ARE SORTED WHEN RENDERED.
 * This Enum's values are the keys used to identify Hubbed page types.
 * Contains the singular and plural default labels for these page types.
 * Contains static method for finding this key value based on SniPage type.
 * @author Jason Clark
 *         Date: 5/13/13
 */
public enum HubPageTypeKeys {

    /**
     * The order of these values determines the
     * order the buttons will appear when rendered.
     */
    MAIN ("main", "main"),
    BIO ("about", "about"),
    EPISODE ("episode", "episodes"),
    EPISODES ("episodes", "episodes"),
    RECIPE ("recipe", "recipes"),
    RECIPES ("recipes", "recipes"),
    PHOTO ("photo", "photos"),
    VIDEO ("video", "videos"),
    ARTICLE ("article", "articles"),
    MENU ("menu", "menus");

    private String keyName;
    private String pluralKeyName;

    private HubPageTypeKeys(final String key, final String pluralKey) {
        this.keyName = key;
        this.pluralKeyName = pluralKey;
    }

    /**
     * Returns a lower case String containing button label, singular.
     * @return String
     */
    public String keyName() {
        return keyName;
    }

    /**
     * Returns a lower case String containing button label, plural.
     * @return String
     */
    public String pluralKeyName() {
        return pluralKeyName;
    }

    /** Retrieve the HubPageTypeKeys Enum value corresponding to the given SniPage type.
     * @param sniPage SniPage for which you want a key.
     * @return HubPageTypeKeys enum that matches this SniPage's resourceType.
     */
    public static HubPageTypeKeys getKeyForSniPage(final SniPage sniPage) {
        String resourceType = sniPage.getContentResource().getResourceType();
        HubPageTypeKeys key = null;
        for (PageSlingResourceTypes type : PageSlingResourceTypes.values()) {
            if (resourceType.equals(type.resourceType())) {
                switch (type) {
                    case VIDEO_CHANNEL:
                    case VIDEO_PLAYER:
                    case VIDEO:
                        key = HubPageTypeKeys.VIDEO;
                        break;
                    case PHOTO_GALLERY:
                    case PHOTO_GALLERY_LISTING:
                        key = HubPageTypeKeys.PHOTO;
                        break;
                    case MENU:
                    case MENU_LISTING:
                        key = HubPageTypeKeys.MENU;
                        break;
                    case RECIPE_LISTING:
                    case ASSET_RECIPES:
                        key = HubPageTypeKeys.RECIPES;
                        break;
                    case EPISODE:
                        key = HubPageTypeKeys.EPISODE;
                        break;
                    case EPISODE_LISTING:
                    case EPISODE_ARCHIVE:
                        key = HubPageTypeKeys.EPISODES;
                        break;
                    case BIO:
                        key = HubPageTypeKeys.BIO;
                        break;
                    case ARTICLE_SIMPLE:
                        key = HubPageTypeKeys.ARTICLE;
                        break;
                    default:
                        key = HubPageTypeKeys.ARTICLE;
                        break;
                }
                break;
            }
        }
        return key;
    }

}
