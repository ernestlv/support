package com.scrippsnetworks.wcm.episode;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.series.Series;

import java.util.List;

/**
 * @author Jason Clark
 *         Date: 8/6/13
 */
public interface Episode {

    /** Returns the SniPage wrapped by this Episode. */
    public SniPage getSniPage();

    /** Returns the SniPage for the Show which this Episode airs. */
    public SniPage getRelatedShowPage();

    /** Find the Recipe pages associated with this episode. */
    public List<SniPage> getRecipePages();

    public List<SniPage> getCompanyPages();

    /** Look for the previous episode SniPage in the series. */
    public SniPage getPreviousEpisodePage();

    /** Look for the next episode SniPage in the series. */
    public SniPage getNextEpisodePage();

    /** Returns the sni:episodeNo for this Episode. */
    public String getEpisodeNumber();

    /** Talent page for primary talent on this episode. */
    public SniPage getPrimaryTalentPage();

    /** Title for this Episode. */
    public String getTitle();

    /** Episode description from sni:description. */
    public String getDescription();

    /** Image for Episode. */
    public String getImagePath();

    /** Series which contains this Episode. */
    public Series getSeries();
    
    /** Returns the sni:shortTitle for this Episode. */
    public String getShortTitle();
    
    /** Returns the sni:episodeType for this Episode. */
    public String getEpisodeType();
    
    /** Returns if this episode page is inheriting a banner-simple or banner-custom from its parent show. */
    public boolean isShowBannerInherited();

    /** Returns true if this episode page is inheriting a sub navigation element from its parent show. */
    public boolean isSubNavInherited();
}
