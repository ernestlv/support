package com.scrippsnetworks.wcm.calendar;

import com.scrippsnetworks.wcm.calendar.impl.CalendarImpl;
import com.scrippsnetworks.wcm.calendar.impl.CalendarSlotImpl;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.Node;

/**
 * Created with IntelliJ IDEA.
 * User: Anatoli_Zapolski
 * Date: 9/9/13
 * Time: 12:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class CalendarSlotFactory {
    private Node currentNode;
    private ResourceResolver resolver;
    private int dayIndex = 1;

    public CalendarSlot build(){
        if(currentNode == null){
            return null;
        }

        if(resolver == null){
            return null;
        }

        return new CalendarSlotImpl(currentNode, resolver, dayIndex);
    }



    public CalendarSlotFactory withNode(Node currentNode){
        this.currentNode = currentNode;
        return this;
    }

    public CalendarSlotFactory withResourceResolver(ResourceResolver resolver){
        this.resolver = resolver;
        return this;
    }

    public CalendarSlotFactory withDayIndex(int dayIndex){
        this.dayIndex = dayIndex;
        return this;
    }
}
