package com.scrippsnetworks.wcm.util;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;

/**
 * Exposes SniSlingUtil as a service
 * Right now this is implemented as a concrete type rather than as an iface
 * @author Ken Shih (156223)
 * @created 7/24/13 11:55 AM
 */
@Component(immediate=true, metatype=true, label="SniSlingUtilService",
        description="service with convenience methods to resolve sling resources")
@Service(value=SniSlingUtilService.class)
public class SniSlingUtilService {
    @Reference ResourceResolverFactory resourceResolverFactory;

    public Resource getResourceWithSlingPath(String slingRelativePath) {
        ResourceResolver resourceResolver = null;
        try {
            resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
            return SniSlingUtil.getResourceWithSlingPath(slingRelativePath, resourceResolver);
        } catch (LoginException e){
            throw new RuntimeException("login exception attempting to get a resourceResolver",e);
        } finally {
            if (resourceResolver !=null) {
                resourceResolver.close();
            }
        }
    }
    public boolean isPropertyOnResourceOrAncestor(String absolutePath,
                                                  String propertyNameToCheck,
                                                  String valueToCheck) {
        ResourceResolver resourceResolver = null;
        try {
            resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
            return SniSlingUtil.isPropertyOnResourceOrAncestor(
                    absolutePath, propertyNameToCheck, valueToCheck, resourceResolver);
        } catch (LoginException e){
            throw new RuntimeException("login exception attempting to get a resourceResolver",e);
        } finally {
            if (resourceResolver !=null) {
                resourceResolver.close();
            }
        }
    }

}
