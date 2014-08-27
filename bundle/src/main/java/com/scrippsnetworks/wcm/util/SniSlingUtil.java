package com.scrippsnetworks.wcm.util;

import com.scrippsnetworks.wcm.paginator.ParagraphPaginator;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

/**
 * These are utilities that I would expect the Sling API to expose naturally,
 * but they either do not exist or I was unable to find them.
 *
 * TODO add cache
 * TODO add memoization to test for circular references or decendant-level in #isPropertyOnResourceOrAncestor
 *
 * @author Ken Shih (156223)
 * @created 7/15/13 4:10 PM
 */
public class SniSlingUtil {

    public static final String DIR_LIB = "/lib";
    public static final String DIR_APPS = "/apps";
    /**
     * Uses Sling rules to return a reference to the concrete node for a relative sling path
     * Currently, this does not support full sling resolution like selectors, etc
     *
     * This was build originally just to find a component node from the string sling:resourceType
     * since that is relative and assumes sling resolution (try /apps first, then /lib) and no util
     * exists in Sling API, this was added
     *
     * @param slingRelativePath
     * @return null if no resource of that name is found
     */
    public static Resource getResourceWithSlingPath(String slingRelativePath, ResourceResolver resourceResolver){
        if(slingRelativePath==null || resourceResolver == null){
            return null;
        }
        Resource resource = null;

        //path is absolute, just get it
        if(slingRelativePath.startsWith("/")){
            resource = resourceResolver.getResource(slingRelativePath);
        }

        //attempt to get in /apps
        if(resource==null){
            StringBuilder sb = new StringBuilder(DIR_APPS).append("/").append(slingRelativePath);
            resource = resourceResolver.getResource(sb.toString());
        }

        //attempt to get in /lib
        if(resource==null){
            StringBuilder sb = new StringBuilder(DIR_LIB).append("/").append(slingRelativePath);
            resource = resourceResolver.getResource(sb.toString());
        }
        return resource;
    }

    /**
     * Given a resource (could be content, component, or anything with a sling:resourceType of sling:superResourceType
     */
    public static boolean isPropertyOnResourceOrAncestor(String absolutePath,
                                                         String propertyNameToCheck,
                                                         String valueToCheck,
                                                         ResourceResolver resourceResolver){
        Resource rc = getResourceWithSlingPath(absolutePath, resourceResolver);
        if(rc == null){
            return false;
        }
        return recursiveIsPropertyOnResourceOrAncestor(rc, propertyNameToCheck, valueToCheck, resourceResolver);
    }

    private static boolean recursiveIsPropertyOnResourceOrAncestor(Resource rc,
                                                                   String propertyNameToCheck,
                                                                   String valueToCheck,
                                                                   ResourceResolver resourceResolver){
        ValueMap valueMap = rc.adaptTo(ValueMap.class);
        if(valueMap!=null){
            String property = valueMap.get(propertyNameToCheck, null);
            //found it already?
            if(valueToCheck.equalsIgnoreCase(property)){
                return true;
            }
        }

        String rootResourceType = rc.getResourceType();

        //drill into resourceType
        if( rootResourceType != null){
           Resource drilledIntoRc = getResourceWithSlingPath(rootResourceType, resourceResolver);
           if( drilledIntoRc!=null && recursiveIsPropertyOnResourceOrAncestor(
                   drilledIntoRc, propertyNameToCheck, valueToCheck, resourceResolver)){
               return true;
           }
        }
        //look at super type
        //new root
        rootResourceType=rc.getResourceSuperType();
        if(rootResourceType==null){
            return false;
        }

        //drill down to super type and recurse
        Resource resource = getResourceWithSlingPath(rootResourceType, resourceResolver);
        if(resource==null){
            return false;
        }
        return recursiveIsPropertyOnResourceOrAncestor(resource, propertyNameToCheck, valueToCheck, resourceResolver);
    }
}
