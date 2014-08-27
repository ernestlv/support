package com.scrippsnetworks.wcm.search.impl;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;

import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.impl.AbstractPropertySettingPageEventHandler;

/**
 * Component which clears out the sni:firstLetter and sni:sortTitle properties.
 * Used only during development.
 */
@Component(enabled = false, immediate = true)
@Service
public class ClearSearchSupportProperties extends AbstractPropertySettingPageEventHandler {

    @Override
    protected String getInitialQuery() {
        return String
                .format("/jcr:root/content//element(*, cq:Page)[(jcr:content/@%s or jcr:content/@%s)] order by jcr:content/@jcr:title",
                        SearchSupportEventHandler.PN_SORT_TITLE, SearchSupportEventHandler.PN_FIRST_LETTER);
    }

    @Override
    protected boolean updateProperties(Page page) throws RepositoryException {
        final Resource contentResource = page.getContentResource();
        if (contentResource != null) {
            Node contentNode = contentResource.adaptTo(Node.class);
            if (contentNode.isCheckedOut()) {
                if (contentNode.hasProperty(SearchSupportEventHandler.PN_SORT_TITLE)
                        || contentNode.hasProperty(SearchSupportEventHandler.PN_FIRST_LETTER)) {
                    contentNode.setProperty(SearchSupportEventHandler.PN_SORT_TITLE, (Value) null);
                    contentNode.setProperty(SearchSupportEventHandler.PN_FIRST_LETTER, (Value) null);
                    return true;
                }
            }
        }
        return false;
    }
}
