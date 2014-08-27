package com.scrippsnetworks.wcm.util;

import java.lang.IllegalArgumentException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Iterator;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.Template;
import com.day.cq.wcm.api.WCMException;
import com.day.cq.tagging.Tag;
import com.day.cq.commons.Filter;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

/** Wrapper for CQ Page.
 *
 * The PageWrapper is a wrapper for any Page delegating all method calls to the wrapped resource by default. Extensions of
 * this class may overwrite any method to return different values as appropriate.
 */
public class PageWrapper implements Page {

    /** The page we're wrapping. */
    private Page wrappedPage = null;

    /** Default constructor */
    public PageWrapper() {}

    /** Constructs a new wrapper.
     *
     * @param wrappedPage the page to wrap
     * @throws IllegalArgumentException if the wrappedPage argument is null
     */
    public PageWrapper(Page wrappedPage) {
        if (wrappedPage == null) {
            throw new IllegalArgumentException("PageWrapper must be constructed with a nonnull page");
        }
        this.wrappedPage = wrappedPage;
    }

    /** Returns the page wrapped by this object. */
    public Page getWrappedPage() {
        return wrappedPage;
    }

    public boolean canUnlock() {
        return wrappedPage.canUnlock();
    }

    public Page getAbsoluteParent(int level) {
        return wrappedPage.getAbsoluteParent(level);
    }

    public Resource getContentResource() {
        return wrappedPage.getContentResource();
    }

    public Resource getContentResource(String relPath) {
        return wrappedPage.getContentResource(relPath);
    }

    public int getDepth() {
        return wrappedPage.getDepth();
    }

    public Locale getLanguage(boolean ignoreContent) {
        return wrappedPage.getLanguage(ignoreContent);
    }

    public Calendar getLastModified() {
        return wrappedPage.getLastModified();
    }

    public String getLastModifiedBy() {
        return wrappedPage.getLastModifiedBy();
    }

    public String getLockOwner() {
        return wrappedPage.getLockOwner();
    }

    public String getName() {
        return wrappedPage.getName();
    }

    public String getNavigationTitle() {
        return wrappedPage.getNavigationTitle();
    }

    public Calendar getOffTime() {
        return wrappedPage.getOffTime();
    }

    public Calendar getOnTime() {
        return wrappedPage.getOnTime();
    }

    public PageManager getPageManager() {
        return wrappedPage.getPageManager();
    }

    public String getPageTitle() {
        return wrappedPage.getPageTitle();
    }

    public Page getParent() {
        return wrappedPage.getParent();
    }

    public Page getParent(int level) {
        return wrappedPage.getParent(level);
    }

    public String getPath() {
        return wrappedPage.getPath();
    }

    public ValueMap getProperties() {
        return wrappedPage.getProperties();
    }

    public ValueMap getProperties (String relPath) {
        return wrappedPage.getProperties(relPath);
    }

    public Tag[] getTags() {
        return wrappedPage.getTags();
    }

    public Template getTemplate() {
        return wrappedPage.getTemplate();
    }

    public String getTitle() {
        return wrappedPage.getTitle();
    }

    public String getVanityUrl() {
        return wrappedPage.getVanityUrl();
    }

    public boolean hasChild(String name) {
        return wrappedPage.hasChild(name);
    }

    public boolean hasContent() {
        return wrappedPage.hasContent();
    }

    public boolean isHideInNav() {
        return wrappedPage.isHideInNav();
    }

    public boolean isLocked() {
        return wrappedPage.isLocked();
    }

    public boolean isValid() {
        return wrappedPage.isValid();
    }

    public Iterator<Page> listChildren() {
        return wrappedPage.listChildren();
    }

    public Iterator<Page> listChildren(Filter<Page> filter) {
        return wrappedPage.listChildren(filter);
    }

    //public Iterator<Page> listChildren(Filter<Page> filter, boolean deep) {
    //    return wrappedPage.listChildren(filter, deep);
    //}

    public void lock() throws WCMException {
        wrappedPage.lock();
    }

    public long timeUntilValid() {
        return wrappedPage.timeUntilValid();
    }

    public void unlock() throws WCMException {
        wrappedPage.unlock();
    }

    public <AdapterType> AdapterType adaptTo(Class<AdapterType> type) {
        return wrappedPage.adaptTo(type);
    }

    public String getDescription() {
        return wrappedPage.getDescription();
    }
}
