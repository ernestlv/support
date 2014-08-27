package com.scrippsnetworks.wcm.fnr.sitesearch;
// Since this class is referred to by absolute name in JSPs, if you move it the package needs to be changed in
// any JSPs using it..

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.query.Query;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Simple bean for getting an image path from an sni:fastfwdId.
 *
 * Use it like
 * &lt;jsp:useBean id=&quot;fastfwdImage&quot; class=&quot;com.scrippsnetworks.wcm.fnr.sitesearch.FastfwdImage&quot;&gt;
 *    &lt;jsp:setProperty name=&quot;fastfwdImage&quot; property=&quot;imageId&quot; value=&quot;${imageId}&quot; /&gt;
 *    &lt;jsp:setProperty name=&quot;fastfwdImage&quot; property=&quot;resourceResolver&quot; value=&quot;${resourceResolver}&quot; /&gt;
 * &lt;/jsp:useBean&gt;
 * &lt;c:set var=&quot;imagePath&quot; value=&quot;${fastfwdImage.imagePath}&quot; /&gt;
 *
 */
public class FastfwdImage {
    private static final Logger logger = LoggerFactory.getLogger(FastfwdImage.class);
    private static final String queryTemplate = "SELECT * FROM [nt:base] AS s WHERE ISDESCENDANTNODE(s, '/content/dam/images') and NAME(s) = 'metadata' and s.[sni:fastfwdId] = %s.0";

    private String imageId;
    ResourceResolver resourceResolver;

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public void setResourceResolver(ResourceResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
    }

    public String getImagePath() {
        String retVal = null;
        try {
            if (imageId != null && !imageId.trim().isEmpty() && resourceResolver != null) {
                String query = String.format(queryTemplate, imageId);
                Iterator<Resource> iter = resourceResolver.findResources(query, Query.JCR_SQL2);
                if (iter.hasNext()) {
                    retVal = iter.next().getParent().getParent().getPath();
                }
            }
        } catch (Exception e) {
            logger.warn("could not get image from fastfwd id", e);
        }
        return retVal;
    }

}
