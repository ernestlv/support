
package com.scrippsnetworks.wcm.collection;



import com.scrippsnetworks.wcm.collection.impl.CollectionImpl;
import com.scrippsnetworks.wcm.page.SniPage;

/**
 *
 * @author Mallik Vamaraju Date: 9/23/13
 */
public class CollectionFactory {
    
    private SniPage sniPage;
    
   
    public Collection build() {
    	if (sniPage != null) {
    		return new CollectionImpl(sniPage);
        }
        return null;
        
    }
    
    public CollectionFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }
    
}
