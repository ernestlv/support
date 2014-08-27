package com.scrippsnetworks.wcm.mobile.secondary3imageacross;

import com.scrippsnetworks.wcm.mobile.secondary3imageacross.impl.ImageAcrossImpl;
import com.scrippsnetworks.wcm.mobile.superleadcarousel.SuperleadItem;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.jcr.resource.JcrResourceConstants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ImageAcrossFactory {
    private Resource resource;

    public ImageAcrossFactory withResource(Resource resource){
        this.resource = resource;
        return this;
    }

    public List<ImageAcross> build(){
        if(resource == null){
            return null;
        }

        ValueMap vm = resource.adaptTo(ValueMap.class);

        List<ImageAcross> resList = new ArrayList<ImageAcross>();

        Iterator<Resource> childrenIterator = resource.listChildren();

        while(childrenIterator.hasNext()){
            Resource child = childrenIterator.next();
            ImageAcross imageAcross = new ImageAcrossImpl(child);
            if (StringUtils.isNotEmpty(imageAcross.getHeader()) || StringUtils.isNotEmpty(imageAcross.getSubheader())){
            	resList.add(imageAcross);
            }
        }

        return resList;
    }
}
