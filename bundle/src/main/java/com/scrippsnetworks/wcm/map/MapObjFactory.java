package com.scrippsnetworks.wcm.map;

import com.scrippsnetworks.wcm.map.impl.MapEntryImpl;
import com.scrippsnetworks.wcm.map.impl.MapObjImpl;
import org.apache.sling.api.resource.Resource;

/**
 * Created with IntelliJ IDEA.
 * User: Anatoli_Zapolski
 * Date: 9/18/13
 * Time: 11:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class MapObjFactory {
    private Resource resource;

    public MapObj build(){
        if(resource == null){
            return null;
        }

        return new MapObjImpl(resource);
    }

    public MapObjFactory withResource(Resource resource){
        this.resource = resource;
        return this;
    }
}
