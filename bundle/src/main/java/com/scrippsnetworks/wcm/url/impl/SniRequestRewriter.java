package com.scrippsnetworks.wcm.url.impl;
 
import com.scrippsnetworks.wcm.config.TemplateConfigService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentException;
import org.osgi.framework.Constants;

import org.apache.felix.scr.annotations.*;

import com.day.cq.rewriter.pipeline.RequestRewriter;
import com.day.cq.rewriter.linkchecker.LinkCheckerSettings;
import com.day.cq.rewriter.linkchecker.Link;
import org.xml.sax.Attributes;

import java.io.IOException;
import java.lang.String;
import java.lang.Integer;
import java.util.Dictionary;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.net.URI;
import java.net.URISyntaxException;

import com.scrippsnetworks.wcm.url.UrlMapper;

/**
 * Implements the Search Service, which provides search request handler objects using pooled persistent HTTP connections.
 * @author Scott Everett Johnson
 */
@Component(label="SNI WCM Request Rewriter",description="Rewriter service for CQ rewriter pipeline.",enabled=true,immediate=true,metatype=false)
@Service(value=RequestRewriter.class)
public class SniRequestRewriter implements RequestRewriter {

    private static Logger logger = LoggerFactory.getLogger(SniRequestRewriter.class);

    @Reference
    private UrlMapper urlMapper;

    @Reference
    private TemplateConfigService configService;

    /** Indicates errors during component configuration. */
    private static class SniRequestRewriterConfigException extends Exception {
        public SniRequestRewriterConfigException(String msg, Throwable cause) {
            super(msg, cause);
        }
    };

    public static final Pattern rewriteUrlPattern = Pattern.compile("^(?!/etc|/content/dam|/libs|/bin|/system|/crx|/apps/cf)/.*");

    /**
     * Activates this OSGi component, setting its properties from the ComponentContext and
     * initializing its state.
     */
    @Activate
    protected void activate(ComponentContext ctx) {
        Dictionary props = ctx.getProperties();
        try {
            internalActivate(props);
        } catch (SniRequestRewriterConfigException e) {
            String pid = (String)props.get(Constants.SERVICE_PID);
            ctx.disableComponent(pid);
            logger.error("disabling {} due to error during activation", pid);
            // rethrowing since there's no other way to signal failure of this method;
            throw new ComponentException("disabling search service due to activation failure",e);
        }
    }

    /**
     * Activation method that doesn't rely on OSGi ComponentContext for parameters,
     * instead allowing them to be passed in directly.
     *
     * @param props Dictionary of properties to set.
     */
    protected void internalActivate(Dictionary props) throws SniRequestRewriterConfigException {
        logger.info("activate");
    }

    /**
     * Deactivates this OSGi component, cleaning up any state.
     */
    @Deactivate
    protected void deactivate(ComponentContext ctx) {
        internalDeactivate();
    }

    /**
     * Deactivation method that doesn't rely on OSGi ComponentContext
     */
    protected void internalDeactivate() {
        logger.info("deactivate");
    }

    /**
     * Updates the state of this OSGi component when the ComponentContext has changed.
     */
    @Modified
    protected void modified(ComponentContext ctx) {
        Dictionary props = ctx.getProperties();
        internalDeactivate();
        try {
            internalActivate(props);
        } catch (SniRequestRewriterConfigException e) {
            String pid = (String)props.get(Constants.SERVICE_PID);
            ctx.disableComponent(pid);
            logger.error("disabling {} due to error during activation", pid);
            // rethrowing since there's no other way to signal failure of this method;
            throw new ComponentException("disabling service due to activation failure",e);
        }
    }

    public Attributes rewrite(String elementName, Attributes attributes, LinkCheckerSettings settings) {
        return null;
    }

    /**
     * {@inherit}
     *
     * Rewrites the links using the LinkChecker's notion of what's a context relative link. If the linkchecker
     * thinks the URL is context relative (for our purposes, a link to a local resource) then the link is
     * rewritten using the the UrlMapper's mapping logic.
     */
    public String rewriteLink(Link link, LinkCheckerSettings settings) {

        // A relUri is already relativized to the base, so it's only context relative when it's internal. (I think....)
        if (link.isContextRelative() && !link.isSpecial()) {
           URI relUri = link.getRelUri();
           if (relUri != null) { // theoretically possible, but practically?
               // CQ sets relUri relative to http://hostname/, so doesn't include the leading slash.
               String relPath = "/" + relUri.getPath().toString();
               String modalWindowPath=null;
               Pattern modalWindowPathPattern = Pattern.compile("");
               if(configService!=null){
                   modalWindowPath = configService.getModalWindowPath();
                   modalWindowPathPattern = Pattern.compile(modalWindowPath.replace(".html",".*([.][\\w-]+)*[.]html.*"));
               }else{
                   logger.warn("template config service not available");
               }

               Matcher m = rewriteUrlPattern.matcher(relPath); // don't try to map things we don't care about
               if (m.matches() && !modalWindowPathPattern.matcher(relPath).matches()) { //don't care about modal window
                   if (urlMapper != null) {
                       StringBuilder newRelPath = new StringBuilder(urlMapper.map(settings.getResourceResolver(), settings.getRequest(), relPath));
                       if (!relPath.equals(newRelPath.toString())) {
                           if (relUri.getRawQuery() != null) {
                               newRelPath.append("?").append(relUri.getRawQuery());
                           }
                           if (relUri.getRawFragment() != null) {
                               newRelPath.append("#").append(relUri.getRawFragment());
                           }
                           String retVal = newRelPath.toString();
                           logger.debug("rewrote {} to {}", link.getHref(), retVal);
                           return retVal;
                       } else {
                           return null;
                       }
                   } else {
                       logger.warn("url mapper not available");
                   }
               } else {
                   logger.debug("{} did not match filter expression", link.getHref());
               }
           } else {
               logger.warn("got null relUri from link", link.getHref());
           }
        } else {
            logger.debug("skipped {} as non-context-relative or special", link.getHref());
        }
        return null;
    }
}
