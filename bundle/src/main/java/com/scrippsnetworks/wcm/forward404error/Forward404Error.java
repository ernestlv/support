package com.scrippsnetworks.wcm.forward404error;

import org.apache.sling.api.SlingHttpServletRequest;

public interface Forward404Error {
    String forwardPath(SlingHttpServletRequest slingRequest);
}
