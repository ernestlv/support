package com.scrippsnetworks.wcm.taglib.rum;

import org.apache.commons.lang.ArrayUtils;
import com.newrelic.api.agent.NewRelic;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;


/**
 * This tag renders the new relic header js include
 */
public class RumBrowserTimingHeaderTag extends TagSupport {

    private static final Logger LOG = LoggerFactory.getLogger(RumBrowserTimingHeaderTag.class);

    @Override
    public int doStartTag() {
        try {
            JspWriter out = pageContext.getOut();
            out.write(NewRelic.getBrowserTimingHeader());
        } catch (IOException ioe) {
            LOG.error("Caught IOException " + ioe.getMessage());
            return SKIP_BODY;
        } catch (Exception e) {
            LOG.error("Exception caught in RumBrowserTimingHeaderTag: "
                    + ArrayUtils.toString(e.getStackTrace()));
        }

        return SKIP_BODY;
    }

    @Override
    public int doEndTag() {
        return SKIP_BODY;
    }

}
