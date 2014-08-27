package com.scrippsnetworks.wcm.topic;

import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.topic.impl.TopicImpl;

/**
 * @author Venkata Naga Sudheer Donaboina
 * Date 9/20/13
 */
public class TopicFactory {
	private SniPage sniPage;

    public Topic build() {
        if (sniPage != null) {
            return new TopicImpl(sniPage);
        }
        return null;
    }

    /**
     * Add SniPage to Article Builder
     * @param page SniPage in hand
     * @return this ArticleFactory
     */
    public TopicFactory withSniPage(SniPage page) {
        this.sniPage = page;
        return this;
    }
}
