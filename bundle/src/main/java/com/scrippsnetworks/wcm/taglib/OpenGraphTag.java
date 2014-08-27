package com.scrippsnetworks.wcm.taglib;

import com.scrippsnetworks.wcm.util.StringUtil;
import org.apache.commons.lang.ArrayUtils;
import com.scrippsnetworks.wcm.opengraph.OpenGraph;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.io.IOException;

/**
 * This tag accepts an OpenGraph object and writes out the group of
 * metadata tags for the OpenGraph properties/values.
 *
 * @author Jason Clark
 *         Date: 4/18/13
 */
public class OpenGraphTag extends TagSupport {

    private static final Logger LOG = LoggerFactory.getLogger(OpenGraphTag.class);

    private OpenGraph openGraph;

    @Override
    public int doStartTag() {
        if (openGraph == null) {
            LOG.error("OpenGraph object was null");
            return SKIP_BODY;
        }

        try {
            JspWriter out = pageContext.getOut();
            StringBuilder builder = new StringBuilder();
            Map<String, String> openGraphData = openGraph.getData();
            for (Map.Entry<String,String> entry : openGraphData.entrySet()) {
                builder.append(buildMetaTag(entry.getKey(), entry.getValue()));
            }
            out.write(builder.toString());
        } catch (IOException ioe) {
            LOG.error("Caught IOException " + ioe.getMessage());
            return SKIP_BODY;
        } catch (Exception e) {
            LOG.error("Exception caught in OpenGraphTag: "
                    + ArrayUtils.toString(e.getStackTrace()));
        }

        return SKIP_BODY;
    }

    @Override
    public int doEndTag() {
        return SKIP_BODY;
    }

    private static String buildMetaTag(final String property, final String content) {
        String outProp = property == null ? "" : property;
        String outContent = content == null ? "" : StringUtil.cleanToEscapedText(content);
        return "<meta property=\"" + outProp + "\" content=\"" + outContent + "\" />\n";
    }

    /** Set the OpenGraph object used by this tag */
    public void setOpenGraph(OpenGraph openGraph) {
        this.openGraph = openGraph;
    }
}
