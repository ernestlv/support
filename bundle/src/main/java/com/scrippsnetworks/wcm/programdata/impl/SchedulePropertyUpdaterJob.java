package com.scrippsnetworks.wcm.programdata.impl;


import com.scrippsnetworks.wcm.util.Constant;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.util.Map;

@Component(immediate = true, metatype = true)
@Service(value = java.lang.Runnable.class)
@Properties({ @Property(name = "scheduler.period", longValue = 60),
        @Property(name="scheduler.concurrent", boolValue=false)
})
public class SchedulePropertyUpdaterJob implements Runnable {


    private final Logger log = LoggerFactory.getLogger(SchedulePropertyUpdaterJob.class);

    private static final String SCHEDULE_SORT_DATE = "sni:sortDate";

    @Property(boolValue = false, description = "Enable this job?", label = "job enabled")
    private static final String PROP_ENABLED = "SchedulePropertyUpdaterJob.enabled";
    private Boolean enabled = false;

    @Reference
    private SlingRepository repository;


    public void run() {

        //only run if enabled via config
        if (this.enabled) {
            log.debug("enabled");
            Session session = null;

            try {
                // get admin session since this is a job
                session = repository.loginAdministrative(null);

                // query for all schedule nodes that don't have the sortdate property
                QueryManager qm = session.getWorkspace().getQueryManager();
                StringBuilder query = new StringBuilder();
                query.append("/jcr:root/etc/sni-asset/schedules//element(*,cq:PageContent)[@sni:assetType='SCHEDULE'")
                    .append(" and not(@")
                    .append(Constant.SNI_SHOW)
                    .append(")]");

                log.debug(String.format("search query %s",query.toString()));

                Query xpathQuery = qm.createQuery(query.toString(), Query.XPATH);
                NodeIterator resultNodes = xpathQuery.execute().getNodes();
                int count = 0;

                //for each result, create a new property for the node that is the date and timeslot as a string for jcr queries,
                // in the format of yyyyMMddtt (with tt being timeslot ID with leading 0)
                while (resultNodes.hasNext()) {
                    Node node = resultNodes.nextNode();
                    try {

                        String year = node.getParent().getParent().getParent().getParent().getName();
                        String month = String.format("%02d",Integer.parseInt(node.getParent().getParent().getParent().getName()));
                        String day = String.format("%02d",Integer.parseInt(node.getParent().getParent().getName()));
                        String timeslot = String.format("%02d",Integer.parseInt(node.getParent().getName()));
                        String timeslotDate = year + month + day + timeslot;
                        node.setProperty(SCHEDULE_SORT_DATE,timeslotDate);
                        if(node.hasProperty(Constant.SNI_EPISODE)) {
                            String episode = node.getProperty(Constant.SNI_EPISODE).getString();
                            String show = episode.substring(0,episode.lastIndexOf("/"));
                            show = show.substring(0,show.lastIndexOf("/"));
                            node.setProperty(Constant.SNI_SHOW,show);
                        }
                        log.debug("updated " + node.getPath() + " with " + timeslotDate);
                        count+=1;
                    } catch (Exception ex) {
                        log.error("unable to update schedule properties on node " + node.getPath(),ex);
                    }

                    //save after every 1000 nodes
                    if(count%1000==0) {
                        session.save();
                    }
                }
                // save again if we have more nodes past 1000 or didn't update 1000 to begin with
                if(count!=0 && count%1000!=0) {
                    session.save();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            finally {
                // close administrative session so we don't have memory leak
                if (session != null) {
                    session.logout();
                }
            }
        }
    }

    @Activate
    @Modified
    protected void activate(final ComponentContext context,final Map<String, Object> configuration) {
        try {
            // see if job is enabled in config
            this.enabled = OsgiUtil.toBoolean(configuration.get(PROP_ENABLED), false);
        } catch (Exception ex) {
            log.error("Error in activation of bundle", ex);
            throw new ComponentException("Could not activate component, invalid configuration",ex);
        }
    }


}
