package com.scrippsnetworks.wcm.util;

/**
 * @author Jason Clark
 *         Date: 11/16/12
 */
public enum NodeNames {

    JCR_CONTENT ("jcr:content"),
    CRXDAO_META ("crxdao:meta");

    private String nodeName;

    private NodeNames(final String nodeName) {
        this.nodeName = nodeName;
    }

    public String nodeName() {return this.nodeName;}

}
