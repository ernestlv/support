package com.scrippsnetworks.wcm.mobile.secondaryinline;

import com.scrippsnetworks.wcm.mobile.secondary3imageacross.impl.ImageAcrossImpl;
import com.scrippsnetworks.wcm.mobile.secondaryinline.impl.SecondaryInlineVideoImpl;
import com.scrippsnetworks.wcm.mobile.superleadcarousel.SuperleadItem;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.jcr.resource.JcrResourceConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SecondaryInlineVideoFactory {
    private Resource resource;

    public SecondaryInlineVideoFactory withResource(Resource resource){
        this.resource = resource;
        return this;
    }

    public SecondaryInlineVideo build(){
        if(resource == null){
            return null;
        }

        SecondaryInlineVideo secondaryInlineVideo = new SecondaryInlineVideoImpl(resource);
        return secondaryInlineVideo;
    }
}
