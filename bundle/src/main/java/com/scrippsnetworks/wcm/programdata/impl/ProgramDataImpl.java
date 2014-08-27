package com.scrippsnetworks.wcm.programdata.impl;

import java.io.PrintWriter;
import java.util.Calendar;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.Externalizer;
import com.day.cq.commons.TidyJSONWriter;
import com.scrippsnetworks.wcm.util.ContentRootPaths;
import com.scrippsnetworks.wcm.url.UrlMapper;

/***
 * Base class for the JSON objects we will return
 * representing Program Data for asynchronous consumption 
 * by components
 * 
 * @author Danny Gordon
 * 
 *
 */
public class ProgramDataImpl {
	
	protected ResourceResolver resourceResolver;
	protected UrlMapper urlMapper;
	protected TidyJSONWriter tidyWriter;
	protected Externalizer externalizer;
	protected boolean isAuthorMode;
	protected Calendar now;
	protected Logger logger = LoggerFactory.getLogger(ProgramDataImpl.class);
	
	public ProgramDataImpl (ResourceResolver _resourceResolver, String _timeStamp, PrintWriter writer, UrlMapper _urlMapper, Boolean _isAuthorMode)
	{
		resourceResolver = _resourceResolver;
		tidyWriter = new TidyJSONWriter(writer);
		urlMapper = _urlMapper;
		externalizer = resourceResolver.adaptTo(Externalizer.class);
		isAuthorMode = _isAuthorMode;
		
	}
	
	public void writeJSON() throws JSONException
	{
		writeHeader("", now);
		writeFooter();
	}
	protected void writeHeader(String moduleName, Calendar current) throws JSONException
	{
		tidyWriter.object();
		tidyWriter.key("scheduleData").object();
		tidyWriter.key("moduleName").value(moduleName);
		tidyWriter.key("parameters").object();	
			if(current != null) {
			tidyWriter.key("year").value(current.get(Calendar.YEAR));
			tidyWriter.key("month").value(current.get(Calendar.MONTH));
			tidyWriter.key("day").value(current.get(Calendar.DAY_OF_MONTH));
			tidyWriter.key("hour").value(current.get(Calendar.HOUR_OF_DAY));
			tidyWriter.key("minute").value(current.get(Calendar.MINUTE));
			}
		tidyWriter.endObject();
	}
	
	protected void writeFooter() throws JSONException
	{
		tidyWriter.endObject();
		tidyWriter.endObject();
	}
	
	/***
	 * Checks if the string is a url, if it is:
	 * Since url will be returned through json, link checker will not have time 
	 * to update it. So we pre-emptively convert it to shortened URL
	 * 
	 * if it is a mobile request we also convert the url to mobile path
	 * 
	 * @param rawValue
	 * @return
	 */
	protected String checkForURL(String rawValue)
	{
		if(rawValue.startsWith(ContentRootPaths.CONTENT_COOK.path()))
		{
			String mappedURL = urlMapper.map(resourceResolver, null, rawValue);
			if(isAuthorMode)
				return mappedURL;
			else
			{
				//if we are in publish mode we strip out the content prefix
				return mappedURL.replace(ContentRootPaths.CONTENT_COOK.path(), "");
			}
		}
		else
		{
			return rawValue;
		}
		
	}
	
	/***
	 * Calculates the calendar to be used based on a UTC string timestamp
	 * Converts the UTC time to Eastern Standard Timezone (Subtract 5 hours). It will also step back a day to account for the 'interesting' way
	 * in which we store program information: Schedule data for 12am to 6am is stored in the previous day's node. To account for this 
	 * we subtract one day if the hour happens to land in that time period.
	 * @param timeStamp
	 * @return
	 */
	protected Calendar calculateCurrentTime(String timeStamp) {
		Calendar calendar = Calendar.getInstance();
		 String[] timePieces = timeStamp.split("_");
		 if(timePieces != null && timePieces.length == 5) {
			 calendar.set(Integer.parseInt(timePieces[0]), Integer.parseInt(timePieces[1]), 
					 Integer.parseInt(timePieces[2]), Integer.parseInt(timePieces[3]), Integer.parseInt(timePieces[4]));
			 
			 //Convert to Eastern Stnd Time by subtracting 5 hours
			 calendar.add(Calendar.HOUR_OF_DAY, -5);
			 
			 //Step back a full day if hour is between midnight and 7am
			 int hr = calendar.get(Calendar.HOUR_OF_DAY);
			 if(hr >=0 && hr < 7) {
					logger.debug("Edge Case: request hour is less than 7 or equal to 24, subtracting one day");
					calendar.add(Calendar.DAY_OF_MONTH, -1);
					
				}
			 
			 //Round minute to nearest half hour
			 int min = calendar.get(Calendar.MINUTE);
			 if(min >= 30) 
					calendar.set(Calendar.MINUTE, 30);
			 else
				 calendar.set(Calendar.MINUTE, 0);
			 
			return calendar;
		 }
		 return null;
	}
	
}
