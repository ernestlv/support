package com.scrippsnetworks.wcm.config.impl;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.config.SiteConfigService;

/**
 * Servlet to output site data based a SiteConfigService instance.
 */
@SuppressWarnings("serial")
@SlingServlet(resourceTypes = "sni-wcm/config", extensions = "js")
public class SiteConfigJsonServlet extends SlingSafeMethodsServlet {

    private BundleContext bundleContext;

    private Logger logger = LoggerFactory.getLogger(SiteConfigJsonServlet.class);

    private ServiceReference findSiteConfig(String siteName) {
        try {
            ServiceReference[] refs = bundleContext.getServiceReferences(SiteConfigService.class.getName(), "(siteName="
                    + siteName + ")");
            if (refs != null && refs.length >= 1) {
                if (refs.length > 1) {
                    logger.warn("More than one service reference for site name [] found.");
                }
                return refs[0];
            } else {
                return null;
            }
        } catch (InvalidSyntaxException e) {
            logger.error("Unable to obtain config service", e);
            return null;
        }
    }

    private void outputBody(SiteConfigService cs, PrintWriter writer) throws JSONException {
        JSONWriter json = new JSONWriter(writer);
        json.object();
        json.key("env");
        json.value(cs.getEnvironment());
        json.key("site");
        json.value(cs.getSiteName());
        json.key("domain");
        json.value(cs.getDomain());
        json.key("adServerUrl");
        json.value(cs.getAdServerUrl());
        json.key("snapPlayListUrl");
        json.value(cs.getSnapPlayListUrl());
        json.key("snapBinary");
        json.value(cs.getSnapBinary());
        json.key("snapConfigs");
        json.value(cs.getSnapConfigs());
        json.key("snapEnableHTML5");
        json.value(cs.isSnapEnableHTML5());
        json.key("autoSuggestContainer");
        json.value(cs.getAutoSuggestContainer());
        json.key("autoSuggestService");
        json.value(cs.getAutoSuggestService());
        json.key("omnitureMultiVariable");
        json.value(StringUtils.join(cs.getOmnitureMultiVariables(), ','));
        json.key("omnitureSingleVariable");
        json.value(cs.getOmnitureSingleVariable());

        json.key("rsiKeyWord");
        json.value(cs.getRsiKeyWord());
        
        json.key("UR3");
        json.value(cs.isUR3());

        json.key("AIM");
        json.value(cs.isAIM());
        
        json.key("animationSpeed");
        json.value(cs.getAnimationSpeed());

        json.key("recipeOriginRootUrl");
        json.value(cs.getRecipeOriginRootUrl());

        writeBrand(json, cs);

        writeBreadcrumb(json, cs);
        
        writeCommunity(json, cs);
        
        writeMobile(json, cs);

        writeNielsen(json, cs);
        
        writePhotoGallery(json, cs);
        
        writeFacebook(json, cs);
        
        writePinterest(json, cs);

        writeRecaptcha(json, cs);

        writeSchedule(json, cs);

        writeSite(json, cs);

        json.endObject();
    }

    private void outputPostfix(PrintWriter writer) {
        writer.println(";");
    }

    private void outputPrefix(PrintWriter writer) {
        writer.println("if (typeof SNI==\"undefined\" || !SNI) { var SNI={}; }");
        writer.print("SNI.Config = ");
    }

    private void writeBrand(JSONWriter json, SiteConfigService cs) throws JSONException {
        json.key("Brand");
        json.object();

        json.key("logoDamPath");
        json.value(cs.getBrandLogoDamPath());

        json.key("logoDesignsPath");
        json.value(cs.getBrandLogoDesignsPath());

        json.key("siteTitle");
        json.value(cs.getBrandSiteTitle());

        json.endObject();
    }

    private void writeBreadcrumb(JSONWriter json, SiteConfigService cs) throws JSONException {
        json.key("breadcrumb");
        json.object();

        json.key("aToZSections");
        json.value(StringUtils.join(cs.getBreadcrumbAToZSections(), ","));

        json.key("hiddenSections");
        json.value(StringUtils.join(cs.getBreadcrumbHiddenSections(), ","));

        json.key("indexSections");
        json.value(StringUtils.join(cs.getBreadcrumbIndexSections(), ","));

        json.endObject();
    }

    private void writeCommunity(JSONWriter json, SiteConfigService cs) throws JSONException {
        json.key("Community");
        json.object();
        
        json.key("ur3Domain");
        json.value(cs.getCommunityUR3Domain());

        json.key("aimDomain");
        json.value(cs.getCommunityAIMDomain());
        
        json.key("ssoControllerPath");
        json.value(cs.getCommunitySsoControllerPath());

        json.key("url");
        json.value(cs.getCommunityUrl());

        json.key("recipeDomain");
        json.value(cs.getCommunityRecipeDomain());

        json.endObject();
    }

    private void writeFacebook(JSONWriter json, SiteConfigService cs) throws JSONException {
        json.key("FB");
        json.object();
        json.key("ChannelUrl");
        json.value(cs.getFacebookChannelUrl());
        json.endObject();
    }

    private void writeMobile(JSONWriter json, SiteConfigService cs) throws JSONException {
        json.key("Mobile");
        json.object();

        json.key("domain");
        json.value(cs.getMobileDomain());

        json.endObject();
    }

    private void writeNielsen(JSONWriter json, SiteConfigService cs) throws JSONException {
        json.key("Nielsen");
        json.object();
        
        json.key("autoTrackPages");
        json.value(cs.isNielsenAutoTrackPages());
        
        json.key("useIframeTracking");
        json.value(cs.isNielsenUseIframeTracking());
        
        json.key("hitCountHtmlUrl");
        json.value(cs.getNielsenHitCountHtmlUrl());
        
        json.endObject();
    }

    private void writePhotoGallery(JSONWriter json, SiteConfigService cs) throws JSONException {
        json.key("PhotoGallery");
        json.object();
        
        json.key("thumbnail_loader");
        json.value(cs.getPhotoGalleryThumbnailLoader());

        json.key("thumbnail_page_size");
        json.value(cs.getPhotoGalleryThumbnailPageSize());
        
        json.key("default_product_image_sm");
        json.value(cs.getPhotoGalleryDefaultProductImageSmall());
        
        json.key("default_product_image_lg");
        json.value(cs.getPhotoGalleryDefaultProductImageLarge());
        
        json.key("animationSpeed");
        json.value(cs.getPhotoGalleryAnimationSpeed());
        
        json.key("longTitleLength");
        json.value(cs.getPhotoGalleryLongTitleLength());
        
        json.key("longCaptionLength");
        json.value(cs.getPhotoGalleryLongCaptionLength());
        
        json.key("shortCaptionLength");
        json.value(cs.getPhotoGalleryShortCaptionLength());
        
        json.key("imageWidthNarrow");
        json.value(cs.getPhotoGalleryImageWidthNarrow());
        
        json.key("imageWidthWide");
        json.value(cs.getPhotoGalleryImageWidthWide());
        
        json.endObject();
    }

    private void writePinterest(JSONWriter json, SiteConfigService cs) throws JSONException {
        json.key("Pinterest");
        json.object();
        json.key("defaultImgUrl");
        json.value(cs.getPinterestDefaultImageUrl());
        
        json.key("defaultFromMsg");
        json.value(cs.getPinterestDefaultFromMessage());
        
        json.endObject();
    }

    private void writeRecaptcha(JSONWriter json, SiteConfigService cs) throws JSONException {
        json.key("Recaptcha");
        json.object();

        json.key("publicKey");
        json.value(cs.getRecaptchaPublicKey());

        json.endObject();
    }

    private void writeSchedule(JSONWriter json, SiteConfigService cs) throws JSONException {
        json.key("Schedule");
        json.object();

        json.key("rootContentPath");
        json.value(cs.getScheduleRootContentPath());

        json.endObject();
    }

    private void writeSite(JSONWriter json, SiteConfigService cs) throws JSONException {
        json.key("Site");
        json.object();

        json.key("homepagePath");
        json.value(cs.getSiteHomepagePath());

        json.endObject();
    }

    protected void activate(ComponentContext ctx) {
        this.bundleContext = ctx.getBundleContext();
    }

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException,
            IOException {
        response.setContentType("application/javascript");
        Resource resource = request.getResource();
        ValueMap properties = resource.adaptTo(ValueMap.class);
        String siteName = properties.get("siteName", String.class);

        PrintWriter writer = response.getWriter();

        ServiceReference ref = findSiteConfig(siteName);

        if (ref != null) {
            try {
                SiteConfigService configService = (SiteConfigService) bundleContext.getService(ref);

                outputPrefix(writer);
                outputBody(configService, writer);
                outputPostfix(writer);

            } catch (JSONException e) {
                logger.error("Unable to produce JSON object", e);
            } finally {
                bundleContext.ungetService(ref);
            }
        } else {
            outputPrefix(writer);
            writer.println("{}");
            outputPostfix(writer);
        }
    }

}
