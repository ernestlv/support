/* Charles E Lewis Jr, 2014-04-30
 *    adapted from GenericOneImageFactory.java
 *    now a static factory method
 *
 */

package com.scrippsnetworks.wcm.mobile.generic1video;

import com.scrippsnetworks.wcm.mobile.generic1video.impl.GenericOneVideoImpl;
import com.scrippsnetworks.wcm.util.HtmlUtil;
import com.scrippsnetworks.wcm.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.util.Arrays;
import java.util.List;

public class GenericOneVideoFactory {

    private static final String CHANNEL_URL_PROP = "videoChannelPath";
    private static final String TEXT_HEADLINE_PROP = "moduleTitle";
    private static final String TEXT_CAPTION_PROP = "captionOverride";
    private static final String RT_ENDSLIDELINK_PROP = "endSlideLink";     // not used (yet) mobile template
    
    private static final String IMAGE_NODE = "leftImage";
    private static final String IMAGE_REF_PROP = "fileReference";

    public static GenericOneVideo buildWithResource( Resource resource ) {

        String channelUrl = "";
        String headline = "";
        String caption = "";
        String imageDamPath ="";
        String endSlideLink = "";
    
        if (resource == null)  return null;

        ValueMap vm = resource.adaptTo(ValueMap.class);

        GenericOneVideo genericOneVideo = new GenericOneVideoImpl();

        if (vm != null) {
            //define base properties
            channelUrl = vm.get(CHANNEL_URL_PROP, "");
            headline = vm.get(TEXT_HEADLINE_PROP, "");
            caption = vm.get(TEXT_CAPTION_PROP, "");
            endSlideLink = vm.get(RT_ENDSLIDELINK_PROP, "");

            Resource imageRes = resource.getChild(IMAGE_NODE);
            if (imageRes != null) {
                ValueMap imageMap = imageRes.adaptTo(ValueMap.class);
                if(imageMap!=null){
                    imageDamPath = imageMap.get(IMAGE_REF_PROP, "");
                }
            }

            genericOneVideo.setHeadline(headline)
                           .setCaption(caption)
                           .setChannelUrl(channelUrl)
                           .setImageDamPath(imageDamPath)
                           .setEndSlideLink(endSlideLink);
        }

        return genericOneVideo;
    }

}
