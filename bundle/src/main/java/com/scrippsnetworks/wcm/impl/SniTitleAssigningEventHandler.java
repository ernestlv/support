package com.scrippsnetworks.wcm.impl;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;

import com.day.cq.wcm.api.Page;

import com.scrippsnetworks.wcm.page.PagePropertyConstants;

/**
 * Event handler which automatically populates the property sni:title from the jcr:title
 * property.
 * 
 * @author Rahul Anand 06/06/2013
 */
@Component(immediate = true, enabled = true)
@Service
public class SniTitleAssigningEventHandler extends AbstractPropertySettingPageEventHandler {

    private static final String PN_SNI_TITLE = "sni:title";
    private static final String PN_JCR_TITLE = "jcr:title";
    private static final String IGNORED_USER = "migration";

    protected boolean updateProperties(Page page) throws RepositoryException {
    	logger.debug("need to update sni:title of page : {}", page.getPath());
        final Resource contentResource = page.getContentResource();
        if (contentResource != null) {
            final Node node = contentResource.adaptTo(Node.class);
            if (node.isCheckedOut()) {
                if (node.hasProperty(PN_JCR_TITLE) && userCanUpdate(node)) {
                    if (!node.hasProperty(PN_SNI_TITLE)) {
                        node.setProperty(PN_SNI_TITLE, node.getProperty(PN_JCR_TITLE).getString());
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    protected String getInitialQuery() {
        return String.format(
                "/jcr:root/content//element(*, cq:Page)[not(jcr:content/@%s)] order by jcr:content/@jcr:title",
                PN_SNI_TITLE);
    }

    private boolean userCanUpdate(Node node) throws RepositoryException {
        boolean safeToUpdate = true;

        if (node.hasProperty(PagePropertyConstants.PROP_SNI_ASSETLINK) &&
            node.hasProperty(PagePropertyConstants.PROP_CQ_LAST_MODIFIED_BY) &&
            node.getProperty(PagePropertyConstants.PROP_CQ_LAST_MODIFIED_BY).getString().equals(IGNORED_USER)) {
            safeToUpdate = false;
        }

        return safeToUpdate;
    }

}
