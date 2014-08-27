package com.scrippsnetworks.wcm.url.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentException;
import org.osgi.framework.Constants;

import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import com.day.cq.rewriter.pipeline.RequestLinkChecker;
import com.day.cq.rewriter.linkchecker.LinkCheckerSettings;
import com.day.cq.rewriter.linkchecker.Link;
import com.day.cq.rewriter.linkchecker.LinkValidity;
import java.lang.String;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Dictionary;
import com.day.text.Text;
import com.scrippsnetworks.wcm.url.impl.LinkImpl;
import com.scrippsnetworks.wcm.url.impl.PathHelper;

@Component(label="SNI WCM RequestLinkChecker",description="RequestLinkChecker service for CQ request link checker pipeline.",enabled=true,immediate=true,metatype=false)
@Service(value=RequestLinkChecker.class)
public class SniRequestLinkChecker implements RequestLinkChecker {
	
	private static Logger logger = LoggerFactory.getLogger(SniRequestLinkChecker.class);
	
    /** Indicates errors during component configuration. */
    private static class SniRequestLinkCheckerConfigException extends Exception {
        public SniRequestLinkCheckerConfigException(String msg, Throwable cause) {
            super(msg, cause);
        }
    };	
                
    /**
     * Activates this OSGi component, setting its properties from the ComponentContext and
     * initializing its state.
     */
    @Activate
    protected void activate(ComponentContext ctx) {
        Dictionary props = ctx.getProperties();
        try {
            internalActivate(props);
        } catch (SniRequestLinkCheckerConfigException e) {
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
    protected void internalActivate(Dictionary props) throws SniRequestLinkCheckerConfigException {
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
        } catch (SniRequestLinkCheckerConfigException e) {
            String pid = (String)props.get(Constants.SERVICE_PID);
            ctx.disableComponent(pid);
            logger.error("disabling {} due to error during activation", pid);
            // rethrowing since there's no other way to signal failure of this method;
            throw new ComponentException("disabling service due to activation failure",e);
        }
    }    
    
    /**
     * Parses and normalizes the given link based on the given settings.
     * If the resulting URI is relative to the webapp context, then only
     * that relative path is returned instead of the full absolute URI.
     *
     * @param href     the link to be parsed
     * @param settings link checker settings
     * @return the resolved URI, or <code>null</code> if the URI malformed
     */
    LinkImpl parseLink(String href, LinkCheckerSettings settings) {
        LinkImpl link = new LinkImpl(href, false);
        if (link.isSpecial()) {
            return link;
        }
        try {
            // we strip off the fragment
            final int pos = href.indexOf('#');
            String fragment = null;
            if ( pos > 0 ) {
                fragment = href.substring(pos+1);
                href = href.substring(0, pos);
            }
            URI uri;
            try {
                uri = new URI(href);
            } catch (URISyntaxException e) {
                if (href.startsWith("/")) {
                    logger.debug("auto escaping invalid uri: " + e.getMessage());
                    uri = new URI(Text.escape(href, '%', true));
                } else {
                    throw e;
                }
            }

            // reappend the unencoded fragment if there is one
            if(fragment != null){
                uri = new URI(uri.getScheme(), uri.getSchemeSpecificPart(), fragment);
            }

            link.setUri(uri);

            URI base = settings.getBaseURI();
            if (base != null) {
                uri = base.resolve(uri);
            }

            URI context = settings.getContextURI();
            if (context != null) {
                uri = context.relativize(uri);
            }
            link.setRelUri(uri);
        } catch (URISyntaxException e) {
            logger.warn("Ignoring malformed URI: {}", e.toString());
        }
        return link;
    }
    
    
	public Link getLink(String href, LinkCheckerSettings settings) {
										
		//Check if the link contains any selectors		
		String[] s2 = href.split("\\.");	    
	    String selector1="";
	    String selector2="";
	    String extension="";
	    
	    Boolean hrefHasSelector=false;
	    
		for (int i = 0; i < s2.length; i++){
			if (i == 1) {
				selector1=s2[i];
			}
			if (i == 2) {
				selector2=s2[i];
			}
			if (i == (s2.length -1)) {
				extension=s2[i];
			}
		}	    
		
		if (selector1!=extension && selector1!="")
		{
			hrefHasSelector=true;
		}
		if (selector2!=extension && selector2!="")
		{
			hrefHasSelector=true;
		}				
			
		if (hrefHasSelector)
		{						
			LinkImpl link = parseLink(href, settings);	
			
			URI uri = link.getRelUri();
			
			if (uri!=null)
			{
				if (uri.getScheme()!=null)
				{
					// getScheme is http or https and is an external link
					// return null to let CQs default linkchecker deal with it
					//return null;
					link.setContextRelative(false);		
					link.setValidity(LinkValidity.VALID);	
					return link;
				}
				else
				{
				    //Check if resource exits for the path
					String uriPath=PathHelper.getCQPathHTML(uri.getPath());
					ResourceResolver resolver = settings.getResourceResolver();			
					Resource resource = resolver.resolve(settings.getRequest(), uriPath);			
					if (resource!=null)
					{	
						//Link has selectors so mark the link valid	
						link.setContextRelative(true);		
						link.setValidity(LinkValidity.VALID);		
						return link;
					}
					else
					{
						// return null to let CQ's default linkchecker handle it
						return null;
					}			
				}
			}
			else
			{
				// return null to let CQ's default linkchecker handle it
				return null;
			}
		
		}
		else
		{
			//Link has no selectors so return null so that the default link checker checks the link			
			return null;
		}

	}
	
}