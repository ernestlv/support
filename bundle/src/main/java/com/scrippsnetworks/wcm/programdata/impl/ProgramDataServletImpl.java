package com.scrippsnetworks.wcm.programdata.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.jcr.Session;
import javax.servlet.ServletException;

import com.day.cq.commons.TidyJSONWriter;
import com.scrippsnetworks.wcm.asset.show.TimeSlot;
import com.scrippsnetworks.wcm.config.SiteConfigService;
import com.scrippsnetworks.wcm.config.impl.SiteConfigUtil;
import com.scrippsnetworks.wcm.util.ContentRootPaths;
import org.apache.felix.scr.annotations.*;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.WCMMode;
import com.scrippsnetworks.wcm.url.UrlMapper;


/**
 * Servlet to output site data based a SiteConfigService instance.
 */

@SlingServlet(selectors = {"ontvnow","recipesontv"}, methods = "GET", extensions = "json", resourceTypes = "sling/servlet/default")
public class ProgramDataServletImpl extends SlingSafeMethodsServlet {

	@Reference
	private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private SlingSettingsService slingSettingsService;

	@Reference
	private UrlMapper urlMapper;
    protected TidyJSONWriter tidyWriter;
	private static final long serialVersionUID = -3960692666512058118L;
	private static final String ONTVNOW_METHOD = "ontvnow";
	private static final String RECIPESONTV_METHOD = "recipesontv";
	private ResourceResolver resourceResolver;
    private SlingHttpServletRequest request;
	private Logger log = LoggerFactory.getLogger(ProgramDataServletImpl.class);
	
	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
		
		response.setHeader("Content-Type", "application/json");
        tidyWriter = new TidyJSONWriter(response.getWriter());
		
		try {
            ProgramDataQueryImpl programDataQuery = new ProgramDataQueryImpl();
            this.request = request;
			resourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);

			String[] selectors = request.getRequestPathInfo().getSelectors();
			boolean isAuthorMode = WCMMode.fromRequest(request) != WCMMode.DISABLED;
			int numSelectors = selectors==null?0:selectors.length;

            ProgramDataImpl programData = null;
            if(numSelectors<2) {
                response.sendError(500, "selectors not valid");
            }
            Calendar now = calculateCurrentTime(selectors[1]);
            writeHeader(selectors[0], now);
            if(selectors[0].equals(ONTVNOW_METHOD))
            {
                //query program data for most recent show
                List<TimeSlot> onNow = programDataQuery.getTimeslots(request, now, null, null, true, 1, false);
                if(onNow!=null && onNow.size()>0 && onNow.get(0).getEpisode()!=null) {
                    String onNowText = "";
                    Calendar currentTime = Calendar.getInstance();

                    if(onNow.get(0).getStartTime().after(currentTime) && ((onNow.get(0).getStartTime().getTime().getTime()/60000) - (currentTime.getTime().getTime()/60000)) > 10) {
                        //if the show that is on now is an hour long, and we 20 to 30 minutes into it, we want to still display that it is on now
                        //but need to be careful that we aren't in paid procgramming or just a slot that has a missing episode
                    	List<TimeSlot> onNowBack = null;
                        onNowBack = programDataQuery.getTimeslots(request, now, null, null, true, 1, true);
                        
                        //we now have the previous timeslot, see if it seems like it should in fact be on now based on how old it is compared to now
                        if(onNowBack != null && onNowBack.size() > 0 && ((currentTime.getTime().getTime()/60000) - (onNowBack.get(0).getStartTime().getTime().getTime()/60000)) > 60) {
                            //looks like it is on now
                            onNowText = "On Now";
                            onNow = onNowBack;
                        } else {
                            // looks like it was on in the past, and we don't have any data for now, display what is up next
                            onNowText = "Up Next";
                        }

                    } else if (onNow.get(0).getStartTime().after(currentTime)) {
                        onNowText = "Up Next";
                    } else {
                        onNowText = "On Now";
                    }
                    if(onNow!=null && onNow.size()>0 && onNow.get(0).getEpisode()!=null) {
                        writeItem("onTVNow",onNow.get(0),onNowText);
                    }

                }
                //query program data for featured timeslot
                Calendar featuredTime = programDataQuery.getDefaultFeaturedTime(request);
                if(featuredTime!=null) {
                    List<TimeSlot> featured = programDataQuery.getTimeslots(request, featuredTime, null, null, true, 1, false);
                    if(featured!=null && featured.size()>0) {
                        if(featured.get(0).getEpisode() != null){
                        	writeItem("featured",featured.get(0),"on tonight");
                        }
                    } else {
                        log.info("didn't find featured");
                    }
                }  else {
                    log.info("featured time was null");
                }


            }
            else if(selectors[0].equals(RECIPESONTV_METHOD) && numSelectors > 1)
            {
                throw new UnsupportedOperationException("servlet doesn't handle recipes on tv yet");

            }
            writeFooter();
			
		} catch (LoginException e) {
			log.error("Login exception in ProgramDataServletImpl: " + e.getMessage());
		}catch (JSONException e) {
			String stackTrace = ExceptionUtils.getStackTrace(e);
			log.error("JSON exception in ProgramDataServletImpl: " + e.getMessage() + "/n" + stackTrace);
		}catch (NullPointerException e) {
			log.error(e.getMessage(),e);
		}
		catch (NumberFormatException e){
			log.error("NumberFormatException in ProgramDataServletImpl: " + e.getMessage(),e);
		} finally {
            resourceResolver.close();
        }

	}

    /***
     * writes the header of the json response
     * @param moduleName name of module to be written to json
     * @param current the date to write to the header
     * @return
     */
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

    /***
     * writes an item to the json response
     * @param key key to use for json item
     * @param item timeslot tied to json item
     * @return
     */
    private void writeItem(String key, TimeSlot item, String airTimeHeader) throws JSONException
    {
        tidyWriter.key(key).array();
        tidyWriter.object();
        String show = "";
        String url = "";
        if(item.getEpisode()!=null && item.getEpisode().getSeries()!=null && item.getEpisode().getSeries().getShow()!=null) {
            show = item.getEpisode().getSeries().getShow().getTitle();
            url =  checkForURL(item.getEpisode().getSeries().getShow().getContentPath());
        }
        tidyWriter.key("onAirTitle").value(show);

        //call the url mapper to convert raw url to the bucketed url before outputting
        tidyWriter.key("onAirUrl").value(url);

        tidyWriter.key("onAirText").value(item.getShortEpisdoeTuneInTime());
        tidyWriter.key("airTimeHeader").value(airTimeHeader);
        tidyWriter.endObject();
        tidyWriter.endArray();
    }

    /***
     * writes the footer of the json response
     * @return
     */
    protected void writeFooter() throws JSONException
    {
        tidyWriter.endObject();
        tidyWriter.endObject();
    }

    /***
     * Calculates the calendar to be used based on a UTC string timestamp
     * Converts the UTC time to Eastern Standard Timezone (Subtract 5 hours).
     * @param timeStamp
     * @return
     */
    protected Calendar calculateCurrentTime(String timeStamp) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        DateFormat formatter = new SimpleDateFormat("yyyy_M_d_HH_mm");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {

            log.debug("timestamp is:" + timeStamp);
            Date date = formatter.parse(timeStamp);
            calendar.setTime(date);
            if(calendar.get(Calendar.MINUTE)<20) {
                calendar.set(Calendar.MINUTE,0);
            } else if(calendar.get(Calendar.MINUTE)>=20 && calendar.get(Calendar.MINUTE)<50) {
                calendar.set(Calendar.MINUTE,30);
            } else {
                calendar.set(Calendar.MINUTE,0);
                calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) +1);
            }

        } catch (Exception ex) {
            log.error(ex.getMessage(),ex);
            // date will default to current time
        }
        calendar.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        formatter.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        log.debug("cal hour:" + calendar.get(Calendar.HOUR_OF_DAY));
        log.debug("after set timezone:" + formatter.format(calendar.getTime()));
        return stringToCalendar(formatter.format(calendar.getTime()),TimeZone.getTimeZone("GMT"));

    }

    public static Calendar stringToCalendar(String strDate, TimeZone timezone) {
        try {
            String FORMAT_DATETIME = "yyyy_M_d_HH_mm";
            SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_DATETIME);
            sdf.setTimeZone(timezone);
            Date date = sdf.parse(strDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return cal;
        } catch (Exception ex) {

        }
        return null;
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
    protected String checkForURL(String rawValue) {
        SiteConfigService config = SiteConfigUtil.getSiteConfigService(request);
        if(config!=null) {
            if(rawValue.startsWith(config.getScheduleRootContentPath())) {
                String mappedURL = urlMapper.map(resourceResolver, null, rawValue);
                if(isAuthor())
                    return mappedURL;
                else
                {
                    //if we are in publish mode we strip out the content prefix
                    return mappedURL.replace(config.getScheduleRootContentPath(), "");
                }
            }
        }
        return  rawValue;
    }

    /***
     * Checks if author runmode is set on server
     *
     * @return
     */
    public boolean isAuthor() {
        final Set<String> runmodes = slingSettingsService.getRunModes();
        for (String runmode : runmodes) {
            if(runmode.equals("author")) {
                return true;
            }
        }
        return false;
    }
	
	
 
}
