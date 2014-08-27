package com.scrippsnetworks.wcm.mobile.modal;

import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.AbstractComponent;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.page.impl.SniPageImpl;
import com.scrippsnetworks.wcm.video.Video;
import com.scrippsnetworks.wcm.video.VideoFactory;
import org.apache.sling.api.resource.Resource;

public class SingleVideoBean extends AbstractComponent {
    /**
     * Constants
     */
    public static final String SELECTED_VIDEO_PROP = "selectedVideo";


    public void getSingleVideo(Resource pageResource) {
        Page currentVideoPage = getPageManager().getPage(pageResource.getParent().getPath());
        if (currentVideoPage != null) {
            SniPage page = new SniPageImpl(currentVideoPage);
            Video video = new VideoFactory().withSniPage(page).build();
            getPageContext().setAttribute(SELECTED_VIDEO_PROP, video.getSnapPlayerPath());
        }
    }

    @Override
    public void doAction() throws Exception {
        Resource pageResource = (Resource)getRequest().getAttribute(MediaBean.MEDIA_RESOURCE);

        if(pageResource!=null){
            getSingleVideo(pageResource);
        }
    }
}
