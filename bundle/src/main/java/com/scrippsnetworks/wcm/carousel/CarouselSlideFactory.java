package com.scrippsnetworks.wcm.carousel;

import com.scrippsnetworks.wcm.carousel.impl.CarouselSlideImpl;
import com.scrippsnetworks.wcm.carousel.impl.SuperleadCarouselSlideImpl;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Patrick Armstrong (parmstrong@siteworx.com)
 */
public class CarouselSlideFactory {
    private static final Logger log = LoggerFactory.getLogger(CarouselSlideFactory.class);
    
    public static CarouselSlide getSlide(String link, String imgPath, String desc, boolean showIcon, boolean endFrame) {
        return new CarouselSlideImpl(link, imgPath, desc, showIcon, endFrame);
    }
    
    public static SuperleadCarouselSlide getSuperleadSlide(Resource slideRes) {
        return new SuperleadCarouselSlideImpl(slideRes);
    }
    
}
