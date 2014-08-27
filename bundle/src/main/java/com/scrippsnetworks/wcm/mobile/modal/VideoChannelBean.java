package com.scrippsnetworks.wcm.mobile.modal;

import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.AbstractComponent;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.page.impl.SniPageImpl;
import com.scrippsnetworks.wcm.video.Video;
import com.scrippsnetworks.wcm.video.channel.Channel;
import com.scrippsnetworks.wcm.video.channel.ChannelFactory;
import com.scrippsnetworks.wcm.video.player.PlayerSelectorsFactory;
import org.apache.sling.api.resource.Resource;

import java.util.HashMap;
import java.util.Map;


public class VideoChannelBean extends AbstractComponent {
    /**
     * Constants
     */
    public static final String VIDEO_PATH_STUB = "videoPathStub";
    public static final String DEEP_LINK_VIDEO_ID = "deepLinkVideoId";
    public static final String AUTOPLAY = "autoplay";
    public static final String VIDEO_ARRAY = "videoArray";
    public static final String VIDEO_OBJECT = "firstVideoObject";
    /**
     * Variables
     */
    private Map<String, String> parametrs = new HashMap<String, String>();
    private PlayerSelectorsFactory playerSelectorsFactory = new PlayerSelectorsFactory();


    private void getVideoChannel(Resource pageResource) {
        if(pageResource.getParent()!=null){
            Resource parentResource = pageResource.getParent();
            if(parentResource!=null){
                Page currentVideoPage = getPageManager().getPage(parentResource.getPath());
                SniPage page = new SniPageImpl(currentVideoPage);
                Channel channelPage = new ChannelFactory().withSniPage(page).build();
                Video firstVideo = channelPage.getFirstVideo();
                String channelAssetId="";
                if(page.getSelectors()!=null) {
                    channelAssetId = playerSelectorsFactory.withSniPageSelectors(page).getChannelAssetId();
                }
                getPageContext().setAttribute(VIDEO_OBJECT, firstVideo);
                getPageContext().setAttribute(VIDEO_ARRAY, channelPage.getVideos());
                parametrs.put(VIDEO_PATH_STUB, channelPage.getSnapPlayerPath());
                parametrs.put(DEEP_LINK_VIDEO_ID, channelAssetId);
                parametrs.put(AUTOPLAY, "false");    //hardcoded for now
                getPageContext().setAttribute(VideoPlayerBean.PARAM,parametrs);
            }
        }
    }

    @Override
    public void doAction() throws Exception {
        Resource pageResource = (Resource)getRequest().getAttribute(MediaBean.MEDIA_RESOURCE);

        if(pageResource!=null){
            getVideoChannel(pageResource);
        }
    }
}
