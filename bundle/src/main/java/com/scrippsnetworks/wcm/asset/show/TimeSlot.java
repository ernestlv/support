package com.scrippsnetworks.wcm.asset.show;

import com.day.cq.commons.jcr.JcrConstants;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.scrippsnetworks.wcm.page.PagePropertyConstants;

/**
 * Class representing the time slot asset node under Schedule
 *
 * @author mei-yichang
 *
 */
public class TimeSlot {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Episode episode;
    private Resource resource;
    private int depts;

    protected int index;
    protected Node node;
    private String episodePath;
    private Calendar startTime;

    public TimeSlot(Resource resource, Calendar cal, int depts) {
        index = Integer.parseInt(resource.getName());
        startTime = (Calendar) cal.clone();
        this.depts=depts;
        this.resource=resource;
        this.node = resource.adaptTo(Node.class);
        try {
            String propPath = JcrConstants.JCR_CONTENT + "/"
                    + PagePropertyConstants.PROP_SNI_EPISODE;
            if (this.node.hasProperty(propPath)) {
                this.episodePath = node.getProperty(propPath).getString();
            }
        } catch (RepositoryException e) {
            log.error(ExceptionUtils.getFullStackTrace(e));
        }
        // start time of the time slot is determined by the index, with 1
        // being 6:30 and 30 minutes increment with index increment by 1
        startTime.add(Calendar.MINUTE, (index - 1) * 30);
    }


    public Episode getEpisode() {
        if(episode==null) {
            try {
                String propPath = JcrConstants.JCR_CONTENT + "/"
                        + PagePropertyConstants.PROP_SNI_EPISODE;
                if (node.hasProperty(propPath)) {
                    String epPath = node.getProperty(propPath).getString();
                    Resource epRes = resource.getResourceResolver()
                            .getResource(epPath);
                    episode = new Episode(epRes, depts);
                }
            } catch (RepositoryException e) {
                log.error(ExceptionUtils.getFullStackTrace(e));
            }
        }
        return episode;
    }

    public String getEpisodePath() {
        return episodePath;
    }

    public String getShowPath() {
        if(episodePath!=null) {
            String show = episodePath.substring(0,episodePath.lastIndexOf("/"));
            return show.substring(0,show.lastIndexOf("/"));
        }
        return null;
    }

    public int getIndex() {
        return index;
    }

    public Calendar getStartTime() {
        return startTime;
    }

    /**
     * This timestamp format is specific to Episodes, used in the Tune-In time module
     * @return String of timestamp formatted for Short Tune-In times of timeslot (9|8c | 10a|9c(Morning))
     */
    public  String getShortEpisdoeTuneInTime() {
        Calendar showTime = (Calendar) startTime.clone();
        StringBuilder output = new StringBuilder();
        Calendar centralTime = (Calendar) showTime.clone();
        int currentHour = showTime.get(Calendar.HOUR);
        //Display format changes based on the minute
        SimpleDateFormat showDate = new SimpleDateFormat(showTime.get(Calendar.MINUTE)==30 ? "h:mm" : "h");
        output.append(showDate.format(showTime.getTime()));
        //If am show a with the time
        if(showTime.get(Calendar.AM_PM) == 0 ){
	        showDate = new SimpleDateFormat("a|");
	        // SimpleDateFormat returns upppercase am/pm, do this so we can lowercase it
	        output.append((showDate.format(showTime.getTime()).toLowerCase()).replace("am","a"));
        }else{
        	output.append("|");
        }
        // add central time
        showTime.add(Calendar.HOUR_OF_DAY,-1);
        //Display format changes based on the minute
        showDate = new SimpleDateFormat(showTime.get(Calendar.MINUTE)==30 ? "h:mm'c'" : "h'c'");
        output.append(showDate.format(showTime.getTime()));
        return output.toString();
    }
    
    /**
     * This timestamp format is specific to Episodes, used in the Tune-In time module
     * @return String of timestamp formatted for Short Tune-In times of timeslot (tue 1:00pm/12:00c)
     */
    public  String getShortTuneInDateTime() {
        Calendar showTime = (Calendar) startTime.clone();
        StringBuilder output = new StringBuilder();
        Calendar centralTime = (Calendar) showTime.clone();
        int currentHour = showTime.get(Calendar.HOUR);
        SimpleDateFormat showDate = new SimpleDateFormat("E h:mm");
        output.append(showDate.format(showTime.getTime()));
        showDate = new SimpleDateFormat("a/");
        // SimpleDateFormat returns upppercase am/pm, do this so we can lowercase it
        output.append(showDate.format(showTime.getTime()).toLowerCase());
        // add central time
        showTime.add(Calendar.HOUR_OF_DAY,-1);
        showDate = new SimpleDateFormat("h:mm'c'");
        output.append(showDate.format(showTime.getTime()));
        return output.toString();
    }

    /**
     * This timestamp format is specific to Episodes, used in the Tune-In time module
     * @return String of timestamp formatted for Short Tune-In times of timeslot  (9PM/8C)
     */
    public  String getShortTuneInTime() {
        Calendar showTime = (Calendar) startTime.clone();
        StringBuilder output = new StringBuilder();
        Calendar centralTime = (Calendar) showTime.clone();
        int currentHour = showTime.get(Calendar.HOUR);
        SimpleDateFormat showDate = new SimpleDateFormat();
        if(showTime.get(Calendar.MINUTE)==0) {
            showDate = new SimpleDateFormat("ha/");
        } else {
            showDate = new SimpleDateFormat("h:mma/");
        }
        output.append(showDate.format(showTime.getTime()));
        // add central time
        showTime.add(Calendar.HOUR_OF_DAY,-1);
        if(showTime.get(Calendar.MINUTE)==0) {
            showDate = new SimpleDateFormat("h'C'");
        } else {
            showDate = new SimpleDateFormat("h:mm'C'");
        }
        output.append(showDate.format(showTime.getTime()));
        return output.toString();
    }

}
