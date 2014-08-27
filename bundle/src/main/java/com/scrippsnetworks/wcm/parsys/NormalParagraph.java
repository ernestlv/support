package com.scrippsnetworks.wcm.parsys;

import org.apache.sling.api.resource.Resource;
import com.day.cq.wcm.foundation.Paragraph;

public class NormalParagraph extends Paragraph {
    public NormalParagraph(Resource resource) {
        super(resource, Paragraph.Type.NORMAL);
    }
}
