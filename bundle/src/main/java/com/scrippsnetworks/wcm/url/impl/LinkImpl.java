/*
 * Copyright 1997-2009 Day Management AG
 * Barfuesserplatz 6, 4001 Basel, Switzerland
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Day Management AG, ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Day.
 */
package com.scrippsnetworks.wcm.url.impl;

import java.net.URI;
import com.day.cq.rewriter.linkchecker.Link;
import com.day.cq.rewriter.linkchecker.LinkValidity;

/**
 * Encapsulates all information of a link.
 */
public class LinkImpl implements Link {

    /**
     * original href
     */
    private final String href;

    /**
     * indicates a special link
     */
    private final boolean isSpecial;

    /**
     * uri of href
     */
    private URI uri;

    /**
     * context relative uri
     */
    private URI relUri;

    /**
     * validity of link
     */
    private LinkValidity validity = LinkValidity.VALID;

    /**
     * indicates that this link is context relative
     */
    private boolean isContextRelative;

    /**
     * Creates a new link
     * 
     * @param href href of the link
     * @param isSpecial indicates if this is a special link.
     */
    public LinkImpl(String href, boolean isSpecial) {
        this.href = href;
        this.isSpecial = isSpecial;
    }

    /**
     * Returns the href as passed in the constructor
     * @return the href.
     */
    public String getHref() {
        return href;
    }

    /**
     * Returns the URI or <code>null</code>
     * @return the URI
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Returns the context relative URI or <code>null</code>
     * @return the URI
     */
    public URI getRelUri() {
        return relUri;
    }

    /**
     * Returns the link validity.
     * @return the validity
     */
    public LinkValidity getValidity() {
        return validity;
    }

    /**
     * Checks if this is a special link (like mailto: javascript: etc)
     * @return <code>true</code> if this is a special link
     */
    public boolean isSpecial() {
        return isSpecial;
    }

    /**
     * Checks if this is a context relative link.
     * @return <code>true</code> if this is a context relative link.
     */
    public boolean isContextRelative() {
        return isContextRelative;
    }

    /**
     * Sets the uri
     * @param uri the uri
     */
    public void setUri(URI uri) {
        this.uri = uri;
    }

    /**
     * Sets the context relative uri
     * @param relUri the relative uri
     */
    public void setRelUri(URI relUri) {
        this.relUri = relUri;
    }

    /**
     * Sets the validity
     * @param validity the validity
     * @return this
     */
    public LinkImpl setValidity(LinkValidity validity) {
        this.validity = validity;
        return this;
    }

    /**
     * Sets the context relative flag.
     * @param contextRelative the flag
     */
    public void setContextRelative(boolean contextRelative) {
        isContextRelative = contextRelative;
    }
}