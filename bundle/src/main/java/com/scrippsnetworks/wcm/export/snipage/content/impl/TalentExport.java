package com.scrippsnetworks.wcm.export.snipage.content.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.scrippsnetworks.wcm.image.SniImage;
import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.talent.Talent;
import com.scrippsnetworks.wcm.talent.TalentFactory;

/**
 * This class generates the Talent page specific properties.
 * @author Venkata Naga Sudheer Donaboina
 *
 */
public class TalentExport extends SniPageExport {

    private static final Logger LOG = LoggerFactory.getLogger(TalentExport.class);

    public enum ExportProperty {

        TALENT_IMAGES_PATHS(String[].class),
        TALENT_AVATAR_IMAGE_PATH(String.class),
        TALENT_BANNER_IMAGE_PATH(String.class);

        final Class clazz;

        ExportProperty(Class clazz) {
            this.clazz = clazz;
        }

        public Class valueClass() {
            return clazz;
        }
    }

    private final Talent talent;

    public TalentExport(SniPage sniPage) {
        super(sniPage);
        this.talent = new TalentFactory().withSniPage(sniPage).build();
        initialize();
    }

    protected TalentExport(SniPage sniPage, Talent talent) {
        super(sniPage);
        this.talent = talent;
        initialize();
    }

    public void initialize() {
        if (sniPage == null || !sniPage.hasContent() || talent == null) {
            return;
        }

        SniImage sniImg = talent.getCanonicalImage();
        if(sniImg != null) {
            setProperty(ExportProperty.TALENT_AVATAR_IMAGE_PATH.name(), sniImg.getPath());
        }

        SniImage sniBannerImg = talent.getBannerImage();
        if(sniBannerImg != null) {
            setProperty(ExportProperty.TALENT_BANNER_IMAGE_PATH.name(), sniBannerImg.getPath());
        }

        String[] talent_images_paths = talent.getImagePaths();

        setProperty(ExportProperty.TALENT_IMAGES_PATHS.name(), talent_images_paths);

    }
}
