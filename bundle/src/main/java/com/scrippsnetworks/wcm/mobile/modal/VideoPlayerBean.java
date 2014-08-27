package com.scrippsnetworks.wcm.mobile.modal;

import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.AbstractComponent;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.page.impl.SniPageImpl;
import com.scrippsnetworks.wcm.video.Video;
import com.scrippsnetworks.wcm.video.channel.Channel;
import com.scrippsnetworks.wcm.video.player.Player;
import com.scrippsnetworks.wcm.video.player.PlayerFactory;
import com.scrippsnetworks.wcm.video.player.PlayerSelectorsFactory;
import com.scrippsnetworks.wcm.video.player.impl.PlayerSelectorsImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoPlayerBean extends AbstractComponent {
    /**
     * Constants
     */
    public static final String VIDEO_ID = "videoId";
    public static final String CHN_ASSET_U_ID = "chnAssetUId";
    public static final String PARAM = "param";
    /**
     * Variables
     */
    private Map<String, String> parametrs = new HashMap<String, String>();
    private PlayerSelectorsFactory playerSelectorsFactory = new PlayerSelectorsFactory();

    private void getVideoPlayer(Resource pageResource) {
        Page currentVideoPage = getPageManager().getPage(pageResource.getParent().getPath());
        SniPage page = new SniPageImpl(currentVideoPage);
        Player player = new PlayerFactory().withSniPage(page).build();
        Channel firstChannel = player.getFirstChannel();
        List<Channel> channels = player.getChannels();
        String channelAssetId="";
        String videoId="";
        PlayerSelectorsImpl playerSelectors = playerSelectorsFactory.withSniPageSelectors(page);
        if(page.getSelectors()!=null) {
            channelAssetId = playerSelectors.getChannelAssetId();
            videoId=playerSelectors.getVideoId();
        }

        if(!channels.isEmpty()){
            if(StringUtils.isNotEmpty(channelAssetId)){
                for(Channel channel:channels){
                    String uid = channel.getSniPage().getUid();
                    if(StringUtils.equals(uid,channelAssetId)){
                        firstChannel=channel;
                        break;
                    }
                }
            }//else default value first Channel
        }

        Video firstVideo = firstChannel.getFirstVideo();
        String snapPlayerPath = firstChannel.getSnapPlayerPath();
        List<Video> firstChannelVideos = firstChannel.getVideos();

        getPageContext().setAttribute(VideoChannelBean.VIDEO_ARRAY, firstChannelVideos);
        getPageContext().setAttribute(VideoChannelBean.VIDEO_OBJECT, firstVideo);
        parametrs.put(CHN_ASSET_U_ID, channelAssetId);
        parametrs.put(VideoChannelBean.VIDEO_PATH_STUB,snapPlayerPath);
        parametrs.put(VIDEO_ID, videoId);
        parametrs.put(VideoChannelBean.AUTOPLAY, "false");   //hardcoded for now
        getPageContext().setAttribute(PARAM,parametrs);
    }

    @Override
    public void doAction() throws Exception {
        Resource pageResource = (Resource)getRequest().getAttribute(MediaBean.MEDIA_RESOURCE);

        if(pageResource!=null){
            getVideoPlayer(pageResource);
        }
    }
}
