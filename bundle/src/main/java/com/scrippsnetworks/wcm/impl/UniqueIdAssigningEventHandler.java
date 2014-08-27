package com.scrippsnetworks.wcm.impl;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.felix.scr.annotations.ConfigurationPolicy;

import com.day.cq.wcm.api.Page;

/**
 * Event handler which automatically populates the property sni:assetUid
 * property.
 */
/*
@Component(immediate = true, enabled = true)
*/
/*
 * Set a sling:OsgiConfig node for this class to enable
 * e.g in config.author
 */
@Component(immediate = true, policy= ConfigurationPolicy.REQUIRE)
@Service
public class UniqueIdAssigningEventHandler extends AbstractPropertySettingPageEventHandler {

    private static final String PN_SNI_UID = "sni:assetUId";

    protected boolean updateProperties(Page page) throws RepositoryException {
        logger.debug("need to update {}", page.getPath());
        final Resource contentResource = page.getContentResource();
        if (contentResource != null) {
            final Node node = contentResource.adaptTo(Node.class);
            if (node.isCheckedOut()) {
                if (!node.hasProperty(PN_SNI_UID)) {
                    node.setProperty(PN_SNI_UID, node.getIdentifier());
                    return true;
                } else {
                    logger.debug("Already has PN_SNI_UID="+node.getProperty(PN_SNI_UID));
                    Property references=node.getProperty(PN_SNI_UID);
                    Value value=references.getValue();
                    logger.debug(PN_SNI_UID+"="+value.getString());
                    String nodeIdentifier=node.getIdentifier();
                    if (!value.getString().equals(nodeIdentifier)) {
                        logger.debug("Copied node id "+value.getString()+" is not equal to node.getIdentifier()=>"+nodeIdentifier+" so set "+PN_SNI_UID+" to "+nodeIdentifier);
                        node.setProperty(PN_SNI_UID, node.getIdentifier());
                        return true;
                    } else {
                        logger.debug(PN_SNI_UID+" not updated.");
                        return false;
                    }
                }
            } else {
                logger.error("Content node checked in for {}", page.getPath());
            }
        } else {
            logger.warn("Content resource was null for {}", page.getPath());
        }

        return false;
    }

    @Override
    protected String getInitialQuery() {
        return String.format(
                "/jcr:root/content//element(*, cq:Page)[not(jcr:content/@%s)] order by jcr:content/@jcr:title",
                PN_SNI_UID);
    }

}
