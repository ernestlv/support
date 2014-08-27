package com.scrippsnetworks.wcm.relationship;

import com.scrippsnetworks.wcm.page.SniPage;

import org.apache.sling.api.resource.Resource;

import java.util.List;

/** Associate related pieces of content and data.
 * This is for building relationships between associated pieces of content.
 * Handy for when you are on a recipe page, for instance, and need talent,
 * show and episode information.
 * @author Jason Clark
 *         Date: 6/29/13
 * Updated Venakta Naga Sudheer Donaboina
 * Date: 8/20/13
 */
public interface RelationshipModel {

    /** Get any Recipe pages associated to the given page or asset. */
    public List<SniPage> getRecipePages();
    
    /** Get any Company pages associated to the given page or asset. */
    public List<SniPage> getCompanyPages();
    
        /** Get any Recipe assets associated to the given page or asset. */
    public List<Resource> getRecipeAssets();

    /** Gets the primary talent page **/
    public SniPage getPrimaryTalent();
    /** Get any Talent pages associated to the given page or asset. */
    public List<SniPage> getTalentPages();
    /** Get any Talent assets associated to the given page or asset. */
    public List<Resource> getTalentAssets();

    /** Get any Episode pages associated to the given page or asset. */
    public List<SniPage> getEpisodePages();
    /** Get any Episode assets associated to the given page or asset. */
    public List<Resource> getEpisodeAssets();

    /** Get any Show pages associated to the given page or asset. */
    public List<SniPage> getShowPages();
    /** Get any Show assets associated to the given page or asset. */
    public List<Resource> getShowAssets();

    /** Get any Schedule assets associated to the given page or asset. */
    public List<Resource> getScheduleAssets();
    
    /** Get mealtype and recipe Ids associated to the given page or asset. */
    public String[] getMealTypeRecipes();

}
