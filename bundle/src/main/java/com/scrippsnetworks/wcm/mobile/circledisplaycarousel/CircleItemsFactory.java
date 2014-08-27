package com.scrippsnetworks.wcm.mobile.circledisplaycarousel;

import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.mobile.circledisplaycarousel.impl.CircleDisplayItemImpl;
import org.apache.sling.api.resource.Resource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;

public class CircleItemsFactory {
    private Resource resource;
    private PageManager pageManager;

    public CircleItemsFactory withResource(Resource resource){
        this.resource = resource;
        return this;
    }
    public CircleItemsFactory withPageManager(PageManager pageManager){
        this.pageManager = pageManager;
        return this;
    }

    public List<CircleDisplayItem> build(){
        List<CircleDisplayItem> result = new ArrayList<CircleDisplayItem>();
        Resource circleItemsRes = resource.getChild("parsys");
        Iterator<Resource> childrenIterator = circleItemsRes.listChildren();

        while (childrenIterator.hasNext()){
            Resource circleItemRes = childrenIterator.next();
            CircleDisplayItem circleDisplayItem = new CircleDisplayItemImpl(pageManager, circleItemRes);

            if (StringUtils.isNotEmpty(circleDisplayItem.getFirstName()) || StringUtils.isNotEmpty(circleDisplayItem.getLastName())){
                result.add(circleDisplayItem);
            }
        }

        return result;
    }
}
