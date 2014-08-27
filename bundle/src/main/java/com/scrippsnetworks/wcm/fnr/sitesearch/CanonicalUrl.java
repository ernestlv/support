package com.scrippsnetworks.wcm.fnr.sitesearch;

import com.scrippsnetworks.wcm.page.SniPage;

/** Encapsulates the generation of canonical urls.
 *
 */
public class CanonicalUrl {
    SniPage sniPage;
    PageContainer pageContainer;
    UrlHelper urlHelper;
    String canonicalStub = null;

    public CanonicalUrl(SniPage sniPage, PageContainer pageContainer, UrlHelper urlHelper) {
        this.sniPage = sniPage;
        this.pageContainer = pageContainer;
        this.urlHelper = urlHelper;

        if (this.sniPage != null) {
            canonicalStub = sniPage.getCanonicalUrl();
            if (canonicalStub != null) {
                canonicalStub = canonicalStub.replace(".html", "");
            }
        }
    }

    public String getCurrentCanonicalUrl() {
        if (canonicalStub != null) {
            return urlHelper.getCanonicalSearchUrl(canonicalStub);
        }
        return null;
    }

    public String getPreviousCanonicalUrl() {
        Page previous = pageContainer.getPreviousPage();
        if (previous != null) {
            int pageNumber = previous.getPageNumber();
            return urlHelper.getCanonicalSearchUrlForPage(canonicalStub, pageNumber);
        }
        return null;
    }

    public String getNextCanonicalUrl() {
        Page next = pageContainer.getNextPage();
        if (next != null) {
            int pageNumber = next.getPageNumber();
            return urlHelper.getCanonicalSearchUrlForPage(canonicalStub, pageNumber);
        }
        return null;
    }
}
