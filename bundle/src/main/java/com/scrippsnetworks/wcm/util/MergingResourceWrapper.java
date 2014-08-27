package com.scrippsnetworks.wcm.util;

import java.util.Map;
import java.util.HashMap;
import org.apache.sling.api.resource.ValueMap; 
import org.apache.sling.api.resource.Resource; 
import org.apache.sling.api.resource.ResourceWrapper; 
import org.apache.sling.api.wrappers.ValueMapDecorator;

import com.scrippsnetworks.wcm.util.MergingValueMap;

/** Wraps two Resources into a single object with properties from both.
 *
 * Any methods not overridden pass through to the primary resource supplied in the constructor.
 *
 * While getChild is implemented, listChildren is not, so will pass through to the primaryResource.
 *
 * @author Scott Everett Johnson
 */
public class MergingResourceWrapper extends ResourceWrapper {

    /** The resource consulted first for properties or children. */
    private Resource primaryResource;
    /** The resource consulted second for properties or children. */
    private Resource secondaryResource;
    /** The merged properties of this resource. */
    private ValueMap properties = null;

    private HashMap<String, Resource> childCache = new HashMap<String, Resource>();

    /** Constructs a new MergingResourceWrapper backed by the given resources.
     *
     * If primaryResource is null, secondaryResource becomes effective primaryResource.
     *
     * @param primaryResource the resource consulted first for properties or children
     * @param secondaryResource the resource consulted second for properties or children
     * @throws IllegalArgumentException if both resources are null
     */
    public MergingResourceWrapper(Resource primaryResource, Resource secondaryResource) {
        super(primaryResource != null ? primaryResource : secondaryResource);
        if (primaryResource == null && secondaryResource == null) {
            throw new IllegalArgumentException("must wrap at least one resource");
        }
        this.primaryResource = primaryResource != null ? primaryResource : secondaryResource;
        this.secondaryResource = primaryResource != null ? secondaryResource : null;
    }

    /** Adaptor override to provide merging.
     *
     * If the type to adapt to is Map or ValueMap, a MergedValueMap is returned for the primaryResource and secondaryResource;
     * 
     * @param type the adaptor target or null if the resource cannot be adapted to the requested type
     * @return the 
     */
    @Override
    public <Type> Type adaptTo(Class<Type> type) {

        if (secondaryResource != null && (type == ValueMap.class || type == Map.class)) {
            if (properties != null) {
                return (Type) properties;
            }

            ValueMap contentMap = primaryResource.adaptTo(ValueMap.class);
            ValueMap assetMap = secondaryResource.adaptTo(ValueMap.class);

            if (type == ValueMap.class) {
                properties = new MergingValueMap(contentMap, assetMap);
            } else if (type == Map.class) {
                properties = new MergingValueMap(contentMap != null ? new ValueMapDecorator(contentMap) : ValueMap.EMPTY,
                        assetMap != null ? new ValueMapDecorator(assetMap) : ValueMap.EMPTY);
            }

            return (Type) properties;
        }

        return super.adaptTo(type);
    }

    /** Returns a merged child resource.
     *
     * If both the primaryResource and secondaryResource has the child, it is returned as a MergingResourceWrapper.
     * If only the primaryResource or secondaryResource has the child, the child is returned as an unwrapped resource.
     * If neither the primaryResource or secondaryResource has the child, null is returned;
     *
     * @param relPath the relative path to the desired resource;
     * @return the child resource, or null if it does not exist
     */
    @Override
    public Resource getChild(String relPath) {

        if (secondaryResource != null) {

            if (childCache.containsKey(relPath)) {
                return childCache.get(relPath);
            }

            Resource retVal = null;
            Resource secondaryChild = secondaryResource.getChild(relPath);
            Resource primaryChild = primaryResource.getChild(relPath);
            if (secondaryChild != null && primaryChild != null) {
                retVal = new MergingResourceWrapper(primaryChild, secondaryChild);
            } else if (secondaryChild != null && primaryChild == null) {
                retVal = secondaryChild;
            } else {
                retVal = primaryChild;
            }

            childCache.put(relPath, retVal);

            return retVal;

        }

        return super.getChild(relPath);
    }

    /** Returns the primary resource wrapped by this object. */
    public Resource getPrimaryResource() {
        return primaryResource;
    }

    /** Returns the primary resource wrapped by this object. */
    public Resource getSecondaryResource() {
        return secondaryResource;
    }
}
