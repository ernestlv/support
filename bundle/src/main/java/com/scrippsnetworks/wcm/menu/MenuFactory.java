/**
 * 
 */
package com.scrippsnetworks.wcm.menu;

import com.scrippsnetworks.wcm.menu.impl.MenuImpl;
import com.scrippsnetworks.wcm.page.SniPage;
import org.apache.sling.api.resource.Resource;

/**
 * @author Venkata Naga Sudheer Donaboina
 * Date: 8/20/13
 *
 */
public class MenuFactory {

    private SniPage sniPage;
    private Resource resource;
    private Menu menu;
    
    /** Build a new Menu given an SniPage. */
    public Menu build() {
        menu = null;

        if (sniPage != null) {
            menu = new MenuImpl(sniPage);
        } else if (resource != null) {
            menu = new MenuImpl(resource);
        }

        return menu;
    }
    
    /** Add an SniPage to this builder. */
    public MenuFactory withSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
        return this;
    }

    public MenuFactory withResource(Resource resource) {
        this.resource = resource;
        return this;        
    }
}
