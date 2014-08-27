package com.scrippsnetworks.wcm.taglib;

import com.scrippsnetworks.wcm.config.SiteConfigService;
import com.scrippsnetworks.wcm.page.SniPage;
import org.apache.commons.lang.ArrayUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.scripting.jsp.util.TagUtil;
import org.apache.sling.settings.SlingSettingsService;
import java.io.IOException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This tag accepts a widget ID and OB template name and returns HTML markup for an Outbrain component;
 * If the current environment is not production, a test URL is used as the data-src property on the tag;
 * Front-end presentation is expected to integrate loading data into its own JS framework rather than
 * having a JavaScript tag written into the page here.
 *
 * @author Jonathan Bell
 *         Date: 6/21/2013
 */
public class OutbrainTag extends TagSupport {

    private static final Logger LOG = LoggerFactory.getLogger(OutbrainTag.class);
    private static final String PREVIEW_SUBDOMAIN = "preview";
    private static final String TEST_URL =
        "http://www.foodnetwork.com/recipes/alton-brown/baked-greens-chips-recipe/index.html";

    private String widgetId;
    private String obTemplate;

    @Override
    public int doStartTag() {
        if (widgetId == null || obTemplate == null) {
            LOG.error("widgetId or obTemplate was null");
            return SKIP_BODY;
        }

        try {
            JspWriter out = pageContext.getOut();
            StringBuilder builder = new StringBuilder();
            String pageUrl = TEST_URL;

            SlingBindings bindings = (SlingBindings)this.pageContext.getRequest().getAttribute(SlingBindings.class.getName());
            SlingScriptHelper sling = bindings.getSling();
            SlingHttpServletRequest request = TagUtil.getRequest(this.pageContext);
            SniPage sniPage = (SniPage)request.getAttribute("currentSniPage");

            if (sling != null && sniPage != null) {
                SlingSettingsService sss = sling.getService(SlingSettingsService.class);
                SiteConfigService sc = sniPage.getSiteConfigService();
                String canonicalUrl = sniPage.getCanonicalUrl();
                if (sss != null && sss.getRunModes().contains("prod") && !sc.getDomain().startsWith(PREVIEW_SUBDOMAIN)) {
                    pageUrl = canonicalUrl;
                }
            }

            String outbrainTag =
                "<div class=\"OUTBRAIN\" data-src=\"" + pageUrl + "\" data-widget-id=\"" + widgetId + 
                "\" data-ob-template=\"" + obTemplate + "\"></div>";

            builder.append(outbrainTag);
            out.write(builder.toString());
        } catch (IOException ioe) {
            LOG.error("Caught IOException " + ioe.getMessage());
            return SKIP_BODY;
        } catch (Exception e) {
            LOG.error("Exception caught in OutbrainTag: "
                    + ArrayUtils.toString(e.getStackTrace()));
        }

        return SKIP_BODY;
    }

    @Override
    public int doEndTag() {
        return SKIP_BODY;
    }

    public void setWidgetId(String pWidgetId) {
        widgetId = pWidgetId;
    }

    public void setObTemplate(String pObTemplate) {
        obTemplate = pObTemplate;
    }
}
