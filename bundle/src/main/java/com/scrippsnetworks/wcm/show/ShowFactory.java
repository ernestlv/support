package com.scrippsnetworks.wcm.show;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.show.impl.ShowImpl;

/**
 *
 * @author Patrick
 */
public class ShowFactory {
    
    private SniPage sniPage;
    
    public ShowFactory() {
        this.sniPage = null;
    }
    
    public Show build() {
        return new ShowImpl(this.sniPage);
    }
    
    public ShowFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }
    
}
