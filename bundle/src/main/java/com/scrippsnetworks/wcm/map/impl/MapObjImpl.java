package com.scrippsnetworks.wcm.map.impl;

import com.scrippsnetworks.wcm.map.MapEntry;
import com.scrippsnetworks.wcm.map.MapEntryFactory;
import com.scrippsnetworks.wcm.map.MapObj;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Anatoli_Zapolski
 * Date: 9/18/13
 * Time: 11:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class MapObjImpl implements MapObj {
    private static final String STATES_CONTAINER_NODE_NAME = "entries";
    private static final MapEntryFactory mapEntryFactory = new MapEntryFactory();
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    private static final String THEME = "colorTheme";
    private static final String DEFAULT_THEME = "1";

    private Resource resource;
    private ValueMap vm;
    private List<MapEntry> mapEntries;


    private String title;
    private String description;
    private String colorTheme;

    public MapObjImpl(Resource resource) {
        this.resource = resource;
        if (resource != null) {
            this.vm = resource.adaptTo(ValueMap.class);
        }
    }

    @Override
    public List<MapEntry> getMapEntries() {
        if (mapEntries == null) {
            mapEntries = new ArrayList<MapEntry>();

            Resource statesContainerRes = resource.getChild(STATES_CONTAINER_NODE_NAME);

            if (statesContainerRes == null) {
                return mapEntries;
            }

            Iterator<Resource> statesRes = statesContainerRes.listChildren();

            while (statesRes.hasNext()) {
                Resource stateRes = statesRes.next();
                Iterator<Resource> entriesRes = stateRes.listChildren();

                while (entriesRes.hasNext()) {
                    Resource entryRes = entriesRes.next();
                    MapEntry entry = mapEntryFactory.withResource(entryRes).build();
                    mapEntries.add(entry);
                }
            }
        }

        Collections.sort(mapEntries);

        return mapEntries;
    }

    @Override
    public String getTitle() {
        if (vm != null && StringUtils.isEmpty(title)) {
            title = vm.get(TITLE, "");
        }
        return title;
    }

    @Override
    public String getDescription() {
        if (vm != null && StringUtils.isEmpty(description)) {
            description = vm.get(DESCRIPTION, "");
        }
        return description;
    }

    @Override
    public String getColorTheme() {
        if (vm != null && StringUtils.isEmpty(colorTheme)) {
            colorTheme = vm.get(THEME, DEFAULT_THEME);
        }
        return colorTheme;
    }
}
