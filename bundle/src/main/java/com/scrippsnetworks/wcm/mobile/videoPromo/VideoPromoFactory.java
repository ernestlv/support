package com.scrippsnetworks.wcm.mobile.videoPromo;

import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.mobile.base.link.Link;
import com.scrippsnetworks.wcm.mobile.base.link.impl.LinkImpl;
import com.scrippsnetworks.wcm.mobile.videoPromo.impl.VideoPromoImpl;
import com.scrippsnetworks.wcm.mobile.videoPromo.impl.VideoPromoItemImpl;
import com.scrippsnetworks.wcm.page.PageFactory;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.util.CarouselMobileHelper;
import com.scrippsnetworks.wcm.util.Constant;
import com.scrippsnetworks.wcm.util.HtmlUtil;
import com.scrippsnetworks.wcm.util.StringUtil;
import com.scrippsnetworks.wcm.video.Video;
import com.scrippsnetworks.wcm.video.VideoFactory;
import com.scrippsnetworks.wcm.video.channel.Channel;
import com.scrippsnetworks.wcm.video.channel.ChannelFactory;
import com.scrippsnetworks.wcm.video.player.Player;
import com.scrippsnetworks.wcm.video.player.PlayerFactory;
import com.scrippsnetworks.wcm.video.player.PlayerSelectorsFactory;
import com.scrippsnetworks.wcm.video.player.impl.PlayerSelectorsImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.util.*;

/**
 * Created by Dzmitry_Drepin on 2/17/14.
 */
public class VideoPromoFactory {
    private static final String VIDEO_PAGE_TYPE = "video";
    private static final String VIDEO_PLAYER_PAGE_TYPE = "video-player";
    private static final String HEADER_PROP = "header";
    private static final String LINKS_PROP = "linklist";//1-3
    private static final String MORELINK_PROP = "morelink";
    public static final String DOT = ".";


    private Resource resource;
    private ChannelFactory channelFactory = new ChannelFactory();

    private PlayerFactory playerFactory = new PlayerFactory();

    private VideoFactory videoFactory = new VideoFactory();


    public VideoPromoFactory withResource(Resource resource) {
        this.resource = resource;
        return this;
    }

    public VideoPromo build() {

        if (resource == null) {
            return null;
        }

        final PageManager pm = resource.getResourceResolver().adaptTo(PageManager.class);

        ValueMap vm = resource.adaptTo(ValueMap.class);

        VideoPromo videoPromo = null;


        if (vm != null) {

            String header = vm.get(HEADER_PROP, "");
            String morelinkProp = vm.get(MORELINK_PROP, "");

            videoPromo = new VideoPromoImpl();
            videoPromo.setHeader(header);
            if (StringUtils.isNotBlank(morelinkProp)) {
                Link moreLink = new LinkImpl();
                moreLink.setTarget(CarouselMobileHelper.getTargetLinkFromText(morelinkProp));
                moreLink.setLinksTitle(StringUtil.cleanToPlainText(morelinkProp));
                moreLink.setHref(HtmlUtil.getHrefFromLink(morelinkProp));
                videoPromo.setMoreLink(moreLink);
            }

            Set<String> itemUnion = null;

            for (int i = 1; i < 4; i++) {
                String[] links = vm.get(LINKS_PROP + i, String[].class);

                if (links != null && links.length > 0) {
                    if (null == itemUnion) {
                        itemUnion = new LinkedHashSet<String>(Arrays.asList(links));
                    } else {
                        itemUnion.addAll(Arrays.asList(links));
                    }
                }
            }

            if (videoPromo == null || itemUnion == null) {
                return videoPromo;
            }

            //add pages here for future search tagging
            PlayerSelectorsFactory playerSelectorsFactory = new PlayerSelectorsFactory();
            Iterator<String> iterator = itemUnion.iterator();
            int size = itemUnion.size();
            int half;
            if (size % 2 == 0) {
                half = size / 2;
            } else {
                half = size / 2 + 1;
            }

            if (size < 1) {
                return videoPromo;
            }

            List<VideoPromoItem> unOrderedItems = new ArrayList(size);
            List<VideoPromoItem> items = videoPromo.getItems();

            for (int i = 1; i < 17 && iterator.hasNext(); i++) {
                VideoPromoItem item = new VideoPromoItemImpl();

                String pageFromPagePath = null;
                String assetIdFromPagePath = null;
                String videoIdFromPagePath = null;

                String link = iterator.next();
                if (StringUtils.isNotBlank(link)) {
                    PlayerSelectorsImpl playerSelectors = playerSelectorsFactory.withSniPagePath(link);
                    pageFromPagePath = playerSelectors.getPageFromPagePath();
                    assetIdFromPagePath = playerSelectors.getAssetIdFromPagePath();
                    videoIdFromPagePath = playerSelectors.getVideoIdFromPagePath();
                }


                if (StringUtils.isBlank(pageFromPagePath)) {
                    continue;
                }

                SniPage sniPage = PageFactory.getSniPage(pm, pageFromPagePath);
                String pageType;

                if (sniPage != null) {
                    pageType = sniPage.getPageType();
                } else {
                    continue;
                }


                if (VIDEO_PAGE_TYPE.equals(pageType)) {
                    setVideoItem(item, sniPage, null);
                } else if (VIDEO_PLAYER_PAGE_TYPE.equals(pageType)) {
                    setVideoPlayerItem(item, sniPage, assetIdFromPagePath, videoIdFromPagePath);
                } else {
                    setVideoChannelItem(item, sniPage, assetIdFromPagePath, null);
                }

                unOrderedItems.add(item);
            }

            size = unOrderedItems.size();
            for (int i = 0; i < half; i++) {
                VideoPromoItem videoPromoItemSecondLine = null;
                VideoPromoItem videoPromoItemFirstLine = null;

                if (size > i) {
                    videoPromoItemFirstLine = unOrderedItems.get(i);
                }
                if (size > half + i) {
                    videoPromoItemSecondLine = unOrderedItems.get(half + i);
                }


                if (videoPromoItemFirstLine != null && isValidItem(videoPromoItemFirstLine)) {
                    items.add(videoPromoItemFirstLine);
                }

                if (videoPromoItemSecondLine != null && isValidItem(videoPromoItemSecondLine)) {
                    items.add(videoPromoItemSecondLine);
                }
            }
        }

        return videoPromo;
    }

    private boolean isValidItem(VideoPromoItem item) {
        if (item != null) {
            if (!(StringUtils.isBlank(item.getTitle()) && StringUtils.isBlank(item.getImageDamPath()))) {
                return true;
            }
        }
        return false;
    }

    private Video setVideoItem(VideoPromoItem item, SniPage sniPage, Video currentVideo) {
        Video entireVideo;

        if (null == currentVideo) {
            entireVideo = videoFactory.withSniPage(sniPage).build();
            item.setUrl(entireVideo.getSniPage().getPath());
        } else {
            entireVideo = currentVideo;
        }

        item.setTitle(entireVideo.getTitle());

        if (StringUtils.isNotBlank(entireVideo.getThumbnailImage16X9())) {
            item.setImageDamPath(entireVideo.getThumbnailImage16X9());
        } else {
            item.setImageDamPath(entireVideo.getThumbnailUrl());
        }

        item.setTime(entireVideo.getVideoRunTime());

        return currentVideo;
    }

    private Video setVideoChannelItem(VideoPromoItem item, SniPage sniPage, String videoIdFromPagePath, Channel channel) {
        Channel entireChannel;

        if (null == channel) {
            entireChannel = channelFactory.withSniPage(sniPage).build();
        } else {
            entireChannel = channel;
        }

        boolean notBlank = StringUtils.isNotBlank(videoIdFromPagePath);

        Video currentVideo = null;
        if (notBlank) {
            for (Video video : entireChannel.getVideos()) {
                String videoId = video.getVideoId();
                if (StringUtils.equals(videoId, videoIdFromPagePath)) {
                    currentVideo = video;

                    if (null == channel) {
                        item.setUrl(currentVideo.getSniPage().getPath() + DOT + videoIdFromPagePath + Constant.HTML); //case channel url
                    }

                    break;
                }
            }
        }

        if (null == currentVideo || !notBlank) {
            currentVideo = entireChannel.getFirstVideo();
            if (StringUtils.isEmpty(item.getUrl())) {
                item.setUrl(entireChannel.getSniPage().getPath());
            }
        }

        if (currentVideo != null) {
            currentVideo = setVideoItem(item, sniPage, currentVideo);
        }

        return currentVideo;
    }

    private Video setVideoPlayerItem(VideoPromoItem item, SniPage sniPage, String assetIdFromPagePath, String videoIdFromPagePath) {
        Video currentVideo = null;

        Player player = playerFactory.withSniPage(sniPage).build();
        Channel firstChannel = player.getFirstChannel();
        List<Channel> channels = player.getChannels();

        if (!channels.isEmpty()) {
            if (StringUtils.isNotEmpty(assetIdFromPagePath)) {
                for (Channel channel : channels) {
                    String uid = channel.getSniPage().getUid();
                    if (StringUtils.equals(uid, assetIdFromPagePath)) {
                        firstChannel = channel;
                        if (StringUtils.isNotBlank(videoIdFromPagePath)) {
                            item.setUrl(firstChannel.getSniPage().getPath() + DOT + assetIdFromPagePath + DOT + videoIdFromPagePath + Constant.HTML);
                        } else {
                            item.setUrl(firstChannel.getSniPage().getPath() + DOT + assetIdFromPagePath + Constant.HTML);
                        }

                        break;
                    }
                }
            } else {
                //else default value first Channel
                item.setUrl(player.getSniPage().getPath());
            }

            if (firstChannel != null) {
                currentVideo = setVideoChannelItem(item, sniPage, videoIdFromPagePath, firstChannel);
            }
        }

        return currentVideo;
    }

}