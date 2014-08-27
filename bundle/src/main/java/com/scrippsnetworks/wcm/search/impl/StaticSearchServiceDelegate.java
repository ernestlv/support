package com.scrippsnetworks.wcm.search.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.search.SearchRequestHandler;

public class StaticSearchServiceDelegate implements SearchServiceDelegate {

    private Map<String, String> responses = new HashMap<String, String>();
    private final ResourceResolverFactory resolverFactory;
    private static Logger log = LoggerFactory.getLogger(StaticSearchServiceDelegate.class);
    
    public StaticSearchServiceDelegate(ResourceResolverFactory resolverFactory) {
        this.resolverFactory = resolverFactory;
    }
    
    @Override
    public boolean isActive() {
        return !responses.isEmpty();
    }

    @Override
    public void deactivate() {
        responses.clear();
    }

    @Override
    public void activate(Dictionary props) throws ConfigStateCreationException {
        String resources = (String) props.get(RESOURCE_LOCATIONS);
        if (StringUtils.isBlank(resources) || resources.split(",").length == 0) {
            throw new ConfigStateCreationException("The Static Search Service has not been configured properly.");
        }
        ResourceResolver rr = null;
        try {
            rr = resolverFactory.getAdministrativeResourceResolver(null);
        } catch (LoginException e) {
            throw new ConfigStateCreationException(e.getMessage());
        }
        String [] resourcePaths = resources.split(",");
        for (String resourcePath : resourcePaths) {
            Resource resource = rr.getResource(resourcePath);
            if (resource == null || Resource.RESOURCE_TYPE_NON_EXISTING.equals(resource.getResourceType())) {
                log.warn("Resource Path {} was null or non existing", resourcePath);
                continue;
            }
            Iterator<Resource> it = resource.listChildren();
            Resource childResource;
            while (it.hasNext()) {
                childResource = it.next();
                try {
                    if (log.isInfoEnabled()) {
                        log.info("Adding static response for service name {}", childResource.getName());
                    }
                    InputStream is = childResource.adaptTo(InputStream.class);
                    if(is != null){
                        responses.put(childResource.getName(), IOUtils.toString(is));
                    } else {
                        log.warn("Unable to adapt resource to InputStream, as expected on resource named: {}", childResource.getName());
                    }
                } catch (IOException ioe) {
                    log.warn("Encountered an IOException while processing {}", childResource.getName());
                }
            }
        }
        if (rr != null) {
            rr.close();
        }
    }

    @Override
    public void modified(Dictionary props) throws ConfigStateCreationException {
        deactivate();
        activate(props);
    }

    @Override
    public SearchRequestHandler getSearchRequestHandler() {
        return new StaticSearchRequestHandlerImpl(responses);
    }

}
