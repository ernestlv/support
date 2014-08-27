package com.scrippsnetworks.wcm.search.impl;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.impl.AbstractPropertySettingPageEventHandler;

/**
 * Event handler which updates the sort title and first letter properties.
 */
@Component(immediate = true, enabled = true)
@Service
public class SearchSupportEventHandler extends AbstractPropertySettingPageEventHandler {

    static final String PN_FIRST_LETTER = "sni:firstLetter";

    static final String PN_SORT_TITLE = "sni:sortTitle";

    private static final String X = "X";

    private static final String ZERO = "0";

    private String getFirstLetter(final String sortTitle) {
        if (sortTitle.length() == 0) {
            return "";
        }
        final char firstLetter = sortTitle.charAt(0);
        switch (firstLetter) {
        case 'Y':
        case 'Z':
            return X;
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            return ZERO;
        }
        return Character.toString(firstLetter);
    }

    private String getSortTitle(final String title) {
        return title.replaceAll("[^a-zA-Z0-9 ]", "").toUpperCase();
    }

    @Override
    protected boolean updateProperties(Page page) throws RepositoryException {
        boolean result = false;

        logger.debug("need to update {}", page.getPath());
        final Resource contentResource = page.getContentResource();
        if (contentResource != null) {
            final Node node = contentResource.adaptTo(Node.class);
            if (node.isCheckedOut()) {
                if (node.hasProperty(NameConstants.PN_TITLE)) {
                    final String title = node.getProperty(NameConstants.PN_TITLE).getString();
                    logger.debug("updating node with title {}", title);
                    final String sortTitle = getSortTitle(title);
                    final String firstLetter = getFirstLetter(sortTitle);

                    if (node.hasProperty(PN_SORT_TITLE)) {
                        String currentValue = node.getProperty(PN_SORT_TITLE).getString();
                        if (!currentValue.equals(sortTitle)) {
                            node.setProperty(PN_SORT_TITLE, sortTitle);
                            result = true;
                        }
                    } else {
                        node.setProperty(PN_SORT_TITLE, sortTitle);
                        result = true;
                    }
                    if (node.hasProperty(PN_FIRST_LETTER)) {
                        String currentValue = node.getProperty(PN_FIRST_LETTER).getString();
                        if (!currentValue.equals(sortTitle)) {
                            node.setProperty(PN_FIRST_LETTER, firstLetter);
                            result = true;
                        }
                    } else {
                        node.setProperty(PN_FIRST_LETTER, firstLetter);
                        result = true;
                    }
                }
            }
        }

        return result;
    }

    @Override
    protected String getInitialQuery() {
        return String
                .format("/jcr:root/content//element(*, cq:Page)[(not(jcr:content/@%s) or not(jcr:content/@%s)) and jcr:content/@jcr:title] order by jcr:content/@jcr:title",
                        PN_SORT_TITLE, PN_FIRST_LETTER);
    }
}
