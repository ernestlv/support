package com.scrippsnetworks.wcm.taglib;

import com.scrippsnetworks.wcm.hub.Hub;
import com.scrippsnetworks.wcm.hub.HubPageTypeKeys;
import com.scrippsnetworks.wcm.hub.button.HubButton;
import com.scrippsnetworks.wcm.hub.button.HubButtonContainer;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.snipackage.SniPackage;
import com.scrippsnetworks.wcm.util.PageTypes;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.scripting.jsp.util.TagUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.List;
import com.day.cq.wcm.api.WCMMode;

/**
 * @author Jason Clark
 *         Date: 5/13/13
 */
public class HubNavigationTag extends TagSupport {

    private static final Logger log = LoggerFactory.getLogger(HubNavigationTag.class);
    private static final String SUBNAVIGATION_TYPE = "sub-navigation";
    private static final String EMPTY_SECTION_DIV_FLAG = "<div class=\"section-empty\"></div>";

    /** necessary for building the Hub Navigation controls */
    private SniPage sniPage;

    /** Do that hub navigation thing that you do */
    @Override
    public int doStartTag() throws JspException {
        if (sniPage == null) {
            return SKIP_BODY;
        }
        Hub hub = sniPage.getHub();
        StringBuilder builder = new StringBuilder();
        if (hub != null && hub.isPageInHub(sniPage)) {
            SlingHttpServletRequest request = TagUtil.getRequest(pageContext);
            WCMMode wcmMode = WCMMode.fromRequest(request);
            if (wcmMode != WCMMode.EDIT && hasPackageNavigation(hub.getHubMaster())) {
                return SKIP_BODY;
            }
            HubButtonContainer hubButtonContainer = hub.getHubButtonContainer();
            if (hubButtonContainer != null) {
                List<HubButton> hubButtons = hubButtonContainer.getHubButtons();
                if (hubButtons != null && hubButtons.size() > 1) {
                    builder.append("<nav class=\"hub\"><ul>");
                    String currentPath = sniPage.getPath();
                    PageTypes pageType = PageTypes.findPageType(sniPage.getPageType());
                    for (HubButton button : hubButtons) {
                        if (button == null) {
                            continue;
                        }
                        String buttonPath = button.getPagePath();
                        boolean active = isButtonCurrentPage(buttonPath, currentPath)
                                || highlightEpisodePage(pageType, button);
                        if (active) {
                            builder.append("<li class=\"active\">");
                        } else {
                            builder.append("<li>");
                        }
                        builder
                                .append("<a href=\"")
                                .append(button.getHref())
                                .append("\">")
                                .append(StringUtils.capitalize(button.getButtonLabel()));
                        if (button.getCount() != null && button.getCount() > 0 && button.getKey() != HubPageTypeKeys.MAIN) {
                            builder.append(" (")
                                .append(button.getCount())
                                .append(")");
                        }
                        if (active) {
                            builder.append("<b class=\"caret\"></b>");
                        }
                        builder.append("</a></li>");
                    }
                    builder.append("</ul></nav>");
                }
            }
        }
        try {
            JspWriter out = pageContext.getOut();
            if(builder.length()==0){
            	builder.append(EMPTY_SECTION_DIV_FLAG);
            }
            out.write(builder.toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return SKIP_BODY;
    }

    /**
     *
     * @return
     * @throws JspException
     */
    @Override
    public int doEndTag() throws JspException {
        return SKIP_BODY;
    }

    /**
     * Setter for SniPage
     * @param sniPage SniPage
     */
    public void setSniPage(SniPage sniPage) {
        this.sniPage = sniPage;
    }

    /** Check if the button path matches the current page path, for highlighting the button in nav. */
    private boolean isButtonCurrentPage(String buttonPath, String currentPath) {
        return StringUtils.isNotBlank(buttonPath)
                && StringUtils.isNotBlank(currentPath)
                && buttonPath.equals(currentPath);
    }

    /** For episodes, the page isn't actually hubbed but we need to treat it as if it were.
     * Check for this condition.
     */
    private boolean highlightEpisodePage(final PageTypes pageType, final HubButton button) {
        return pageType == PageTypes.EPISODE && button.getKey() == HubPageTypeKeys.EPISODES;
    }

    private boolean hasPackageNavigation(SniPage hubMaster) {
        boolean hasNav = false;

        SniPackage masterPkg = hubMaster.getSniPackage();
        if (masterPkg != null) {
            List<Resource> pkgModules = masterPkg.getModules();
            for (Resource module : pkgModules) {
                if (module.getResourceType().endsWith(SUBNAVIGATION_TYPE)) {
                    hasNav = true;
                    break;
                }    
            }
        }

        return hasNav;
    }
}

