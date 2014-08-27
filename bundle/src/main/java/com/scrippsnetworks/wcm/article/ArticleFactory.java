package com.scrippsnetworks.wcm.article;

import com.scrippsnetworks.wcm.article.impl.ArticleImpl;
import com.scrippsnetworks.wcm.page.SniPage;

/**
 * @author Jason Clark
 *         Date: 4/28/13
 */
public class ArticleFactory {

    private SniPage page;

    /**
     * Can return null
     * @return
     */
    public Article build() {
        if (page != null) {
            return new ArticleImpl(page);
        }
        return new ArticleImpl();
    }

    /**
     * Add SniPage to Article Builder
     * @param page SniPage in hand
     * @return this ArticleFactory
     */
    public ArticleFactory withSniPage(SniPage page) {
        this.page = page;
        return this;
    }

}
