package com.scrippsnetworks.wcm.map.impl;

import com.scrippsnetworks.wcm.image.SniImage;
import com.scrippsnetworks.wcm.image.SniImageFactory;
import com.scrippsnetworks.wcm.map.MapEntry;
import com.scrippsnetworks.wcm.map.util.StateHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

public class MapEntryImpl implements MapEntry {
    private static final String CITY_PROP = "city";
    private static final String DESCRIPTION_PROP = "description";
    private static final String LOCATION_NAME_PROP = "locationName";
    private static final String PHOTOGRAPHER_PROP = "photographer";
    private static final String STATE_PROP = "state";
    private static final String TITLE_PROP = "title";
    private static final String ZIP_CODE_PROP = "zipCode";
    private static final String IMAGE_NODE_NAME = "image";
    private static final String STATE_CODE_PROP = "stateCode";
    private static final String IMAGE_FILE_REFERENCE_PROP = "fileReference";
    private static final String LATITUDE_PROP = "latitude";
    private static final String LONGITUDE_PROP = "longitude";
    private static String IMAGE_RESOURCE_FILE_PROPERTY = "fileReference";

    private Resource resource;
    private ValueMap vm;

    private String locationName;
    private String zipCode;
    private String city;
    private String state;
    private String title;
    private String description;
    private String photographer;
    private String stateCode;
    private Double longitude;
    private Double latitude;
    private SniImage image;
    private Boolean hasImage;
    private Boolean hasLatitudeAndLongitude;

    public MapEntryImpl(Resource resource){
        this.resource = resource;
        if (resource != null){
            this.vm = resource.adaptTo(ValueMap.class);
        }
    }

    @Override
    public String getLocationName() {
        if (locationName == null && vm != null){
            locationName = vm.get(LOCATION_NAME_PROP, "");
        }
        return locationName;
    }

    @Override
    public String getZipCode() {
        if (zipCode == null && vm != null){
            zipCode = vm.get(ZIP_CODE_PROP, "");
        }
        return zipCode;
    }

    @Override
    public String getCity() {
        if (city == null && vm != null){
            city = vm.get(CITY_PROP, "");
        }
        return city;
    }

    @Override
    public String getState() {
        if (state == null && vm != null){
            state = StateHelper.getStateByCode(getStateCode());
        }
        return state;
    }

    @Override
    public String getTitle() {
        if (title == null && vm != null){
            title = vm.get(TITLE_PROP, "");
        }
        return title;
    }

    @Override
    public String getDescription() {
        if (description == null && vm != null){
            description = vm.get(DESCRIPTION_PROP, "");
        }
        return description;
    }

    @Override
    public String getPhotographer() {
        if (photographer == null && vm != null){
            photographer = vm.get(PHOTOGRAPHER_PROP, "");
        }
        return photographer;
    }

    public SniImage getImage() {
        if (image == null && vm != null){
            Resource imageRes = resource.getChild(IMAGE_NODE_NAME);

            if (imageRes != null){
                ValueMap imageVm = imageRes.adaptTo(ValueMap.class);
                String imagePath = imageVm.get(IMAGE_FILE_REFERENCE_PROP, "");
                image = new SniImageFactory().withPath(imagePath).build();
            }
        }
        return image;
    }

    @Override
    public int compareTo(MapEntry o) {
        if (getState() == null){
            if (o.getState() == null){
                return 0;
            } else{
                return -1;
            }
        }
        if (o.getState() == null){
            return 1;
        }
        return getState().compareTo(o.getState());
    }

    @Override
    public String getStateCode() {
        if (stateCode == null && vm != null){
            stateCode = vm.get(STATE_CODE_PROP, "");
        }
        return stateCode;
    }

    @Override
    public Double getLatitude() {
        if (latitude == null && vm != null){
            latitude = Double.parseDouble(vm.get(LATITUDE_PROP, "0"));
        }
        return latitude;
    }

    @Override
    public Double getLongitude() {
        if (longitude == null && vm != null){
            longitude = Double.parseDouble(vm.get(LONGITUDE_PROP, "0"));
        }
        return longitude;
    }

    @Override
    public Boolean getHasLatitudeAndLongitude() {
        if (hasLatitudeAndLongitude == null && vm != null){
            hasLatitudeAndLongitude = vm.containsKey(LATITUDE_PROP) && vm.containsKey(LONGITUDE_PROP);

        } else{
            if (vm == null) {
                hasLatitudeAndLongitude = false;
            }
        }

        return hasLatitudeAndLongitude;
    }

    @Override
    public Boolean getHasImage() {
        if (hasImage == null && vm != null){
            if (getImage() == null){
                hasImage = false;
                return hasImage;
            }

            if (StringUtils.isEmpty(getImage().getPath())){
                hasImage = false;
                return hasImage;
            }

            hasImage = true;
        }
        return hasImage;
    }
}
