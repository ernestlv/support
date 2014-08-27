package com.scrippsnetworks.wcm.topic;

import com.scrippsnetworks.wcm.page.SniPage;

/**
 * @author Venkata Naga Sudheer Donaboina
 * Date 9/20/13
 */
public interface Topic {
    
    public static final int ITEMS_PER_PAGE = 10;
	
	/** Returns the SniPage wrapped by this Topic. */
    public SniPage getSniPage();
	
	/** Primary Tag for Topic. */
	public String getPrimaryTopicTag();
	
	/** Secondary Tag for Topic. */
	public String getSecondaryTopicTag();
	
	/** Topic Tags. */
	public String[] getTopicTags();
        
        /** Topic tags as to be used by the search component bean */
        public String getTopicTagsForSearch();
        
        /** Keywords as to be used by the search component bean */
        public String getKeywordsForSearch();
        
        /** Asset restrictions for a topic page */
        public String getAssetRestrictions();

}
