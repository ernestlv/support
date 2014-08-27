package com.scrippsnetworks.wcm.page;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.page.impl.SniPageImpl;
import org.apache.sling.api.scripting.SlingScriptHelper;

import java.util.regex.Pattern;

public class PageFactory {

    /**
     * Construct an SniPage given a CQ Page and a SlingScriptHelper.
     * @param page Current Page
     * @param ssh A SlingScriptHelper object
     * @return SniPage
     */
    public static SniPage getSniPage(Page page, SlingScriptHelper ssh) {
        if (page == null) {
            return null;
        }
        if (SniPage.class.isAssignableFrom(page.getClass())) {
            return (SniPage)page;
        }
        return new SniPageImpl(page, ssh);
    }

    public static SniPage getSniPage(Page page) {
        return getSniPage(page, null);
    }

    public static SniPage getSniPage(PageManager pageManager, String path) {
        if (pageManager == null || path == null || path.isEmpty()) {
            return null;
        }

        Pattern isContainHtml = Pattern.compile(".*[.]html$");
        if(isContainHtml.matcher(path).matches()){
            path = path.replaceFirst("[.]html$", "");
        }
        Page thePage = pageManager.getPage(path);
        if (thePage != null) {
            return getSniPage(thePage);
        } else {
            return null;
        }
    }
}