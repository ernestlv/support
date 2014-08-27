package com.scrippsnetworks.wcm.programdata.impl;


import com.scrippsnetworks.wcm.fnr.util.AssetSlingResourceTypes;
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
@Service(value = Runnable.class)
@Properties({ @Property(name = "scheduler.period", longValue = 60),
        @Property(name="scheduler.concurrent", boolValue=false)
})
public class CookPageResourceTypeUpdaterJob implements Runnable {


    private final Logger log = LoggerFactory.getLogger(CookPageResourceTypeUpdaterJob.class);

    private static final String SCHEDULE_SORT_DATE = "sni:sortDate";

    @Property(boolValue = false, description = "Enable this job?", label = "job enabled")
    private static final String PROP_ENABLED = "CookPageResourceTypeUpdaterJob.enabled";
    private Boolean enabled = false;

    @Reference
    private SlingRepository repository;


    public void run() {

        //only run if enabled via config
        if (this.enabled) {
            log.debug("enabled");
            updateResourcetypes("sni-wcm/components/pagetypes/show","sni-food/components/pagetypes/show");
            updateResourcetypes("sni-wcm/components/pagetypes/episode","sni-food/components/pagetypes/episode");
            updateResourcetypes("sni-wcm/components/pagetypes/talent","sni-food/components/pagetypes/talent");
            updateResourcetypes("sni-wcm/components/pagetypes/recipe","sni-food/components/pagetypes/recipe");
        }
    }

    private void updateResourcetypes(String baseResourceType, String destResourceType) {
        // get admin session since this is a job
        Session session;
        try {
        session = repository.loginAdministrative(null);

        // query for all schedule nodes that don't have the sortdate property
        QueryManager qm = session.getWorkspace().getQueryManager();
        StringBuilder query = new StringBuilder();
        query.append("/jcr:root/content/cook//element(*,cq:PageContent)[@sling:resourceType='")
                .append(baseResourceType + "']");

        log.debug(String.format("search query %s",query.toString()));

        Query xpathQuery = qm.createQuery(query.toString(), Query.XPATH);
        NodeIterator resultNodes = xpathQuery.execute().getNodes();
        int count = 0;

        //for each result, create a new property for the node that is the date and timeslot as a string for jcr queries,
        // in the format of yyyyMMddtt (with tt being timeslot ID with leading 0)
        while (resultNodes.hasNext()) {
            Node node = resultNodes.nextNode();
            node.setProperty("sling:resourceType",destResourceType);

            //save after every 1000 nodes
            if(count%1000==0) {
                session.save();
            }
        }
        // save again if we have more nodes past 1000 or didn't update 1000 to begin with
        if(count!=0 && count%1000!=0) {
            session.save();
        }
        } catch (Exception ex) {}
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
