package com.scrippsnetworks.wcm.map;

import com.scrippsnetworks.wcm.map.impl.MapEntryImpl;
import org.apache.sling.api.resource.Resource;

/**
 * Created with IntelliJ IDEA.
 * User: Anatoli_Zapolski
 * Date: 9/17/13
 * Time: 2:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class MapEntryFactory {
    private Resource resource;

    public MapEntry build(){
        if(resource == null){
            return null;
        }

        return new MapEntryImpl(resource);
    }

    public MapEntryFactory withResource(Resource resource){
        this.resource = resource;
        return this;
    }
}
