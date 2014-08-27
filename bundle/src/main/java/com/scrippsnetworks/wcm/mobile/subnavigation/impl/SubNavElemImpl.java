package com.scrippsnetworks.wcm.mobile.subnavigation.impl;

import com.scrippsnetworks.wcm.hub.Hub;
import com.scrippsnetworks.wcm.mobile.subnavigation.SubNavElem;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.util.HtmlUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

public class SubNavElemImpl implements SubNavElem{
    private static final String TITLE_PROP = "title";
    private Resource resource;
    private ValueMap vm;

    private String title;
    private boolean isActive;
    private String path = "";

    public SubNavElemImpl (Resource resource, SniPage page){
        this.resource = resource;
        if (resource != null){
            this.vm = resource.adaptTo(ValueMap.class);

            if (vm != null){
                title = vm.get(TITLE_PROP, "");
                path = HtmlUtil.getHrefFromLink(title);

                if (!StringUtils.isEmpty(path)){
                    title = "<a href=\"" + path + "\">" + title.replaceAll("</?a.*?>","") + "</a>";
                }

                path = path.replaceFirst("[.]html.*", "");

                Hub hub = page.getHub();
                SniPage hubMaster = page;
                if (hub != null){
                    hubMaster = hub.getHubMaster();
                }
                if (page != null && hubMaster != null){
                    isActive = path.equals(page.getPath()) || path.equals(hubMaster.getPath());
                }
            }
        }
    }


    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public String getPath() {
        return path;
    }
}
