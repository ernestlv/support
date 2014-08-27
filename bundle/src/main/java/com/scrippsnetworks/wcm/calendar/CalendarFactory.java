package com.scrippsnetworks.wcm.calendar;

import com.scrippsnetworks.wcm.calendar.impl.CalendarImpl;

import org.apache.sling.api.resource.Resource;

/**
 * Created with IntelliJ IDEA.
 * User: Anatoli_Zapolski
 * Date: 9/6/13
 * Time: 12:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class CalendarFactory {
    private Resource resource;

    public Calendar build(){
        if(resource == null){
            return null;
        }

        return new CalendarImpl(resource);
    }

    public CalendarFactory withResource(Resource resource){
        this.resource = resource;
        return this;
    }

}
