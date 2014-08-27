package com.scrippsnetworks.wcm.video

import com.scrippsnetworks.wcm.video.VideoUtil
import spock.lang.Specification
import spock.lang.Unroll

/**
 *
 * @author Jason Clark
 * Date: 7/23/13
 */
class VideoUtilTest extends Specification {

    @Unroll
    def "test snap player path builder for single video"() {
        when:
        String videoResult = VideoUtil.formatVideoSnapPath(path)

        then:
        videoResult == expected

        where:
        path << ["/content/food/videos/foo", "/content/foo/recipes/blahdy/blahdy/blah/recipe/hubvideo"]
        expected << ["videos/foo.videoplayer", "recipes/blahdy/blahdy/blah/recipe/hubvideo.videoplayer"]
    }

    @Unroll
    def "test snap player path builder for channel"() {
        when:
        String channelResult = VideoUtil.formatChannelSnapPath(path)

        then:
        channelResult == expected

        where:
        path << ["/content/food/videos/foochannel", "/content/foo/recipes/blahdy/blahdy/blah/recipe/hubchannel"]
        expected << ["videos/foochannel.videochannel", "recipes/blahdy/blahdy/blah/recipe/hubchannel.videochannel"]

    }

}
