package com.scrippsnetworks.wcm.opengraph;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.opengraph.impl.OpenGraphImpl;

/**
 * @author Jason Clark
 *         Date: 4/19/13
 */
public class OpenGraphFactory {

    public static OpenGraph createOpenGraph(SniPage page) {
        return new OpenGraphImpl(page);
    }

}
