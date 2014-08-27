package com.scrippsnetworks.wcm.taglib.episode;

import com.scrippsnetworks.wcm.asset.DataUtil;
import com.scrippsnetworks.wcm.util.NodeNames;
import com.scrippsnetworks.wcm.util.PagePropertyNames;

import java.util.*;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * JSTL tag to write out the "MORE EPISODES" block on episode pages
 * @author Jason Clark
 * Date: 7/31/12
 */
public class MoreEpisodesTag extends TagSupport {

    private static final String FORWARD_SLASH = "/";

    private Resource resource;
    private String episodeAssetPath;
    private String appType;
    public int doStartTag() throws JspException {

        if (resource == null || episodeAssetPath == null) {
            return SKIP_BODY;
        }

        JspWriter writer = pageContext.getOut();
        StringBuilder output = new StringBuilder();
        if(appType==null)
        	output.append("<ul class=\"list\">");
        else if(appType.equalsIgnoreCase("mob"))
        	output.append("<ul>");
        List<String> episodes = DataUtil.allEpisodesInSameSeason(resource, episodeAssetPath);

        int totalEpisodes = episodes.size();
        int currentEpisodeIndex = episodes.indexOf(episodeAssetPath);

        //if 5 or fewer episodes, highlight the current episode where it is in the list and render whole list
        if (totalEpisodes <= 5) {
            for (String episodePath : episodes) {
                output.append(formatEpisodeListItem(episodePath));
            }
        } else {
            //there are more than 5 episodes, now it gets interesting

            //current episode is within 2 spaces from the end of the list
            if (currentEpisodeIndex >= totalEpisodes - 3) {
                for (int i = totalEpisodes - 5; i < totalEpisodes; i++) {
                    output.append(formatEpisodeListItem(episodes.get(i)));
                }
            } else if (currentEpisodeIndex <= 1) { //current episode is within 2 spaces from beginning
                for (int i = 0; i < 5; i++) {
                    output.append(formatEpisodeListItem(episodes.get(i)));
                }
            } else { //somewhere in the middle
                for (int i = currentEpisodeIndex - 2; i <= currentEpisodeIndex + 2; i++) {
                    output.append(formatEpisodeListItem(episodes.get(i)));
                }
            }
        }


        output.append("</ul>");

        try {
            writer.print(output);
        } catch (IOException ioe) {
            throw new JspException(ioe);
        }

        return SKIP_BODY;
    }

    public int doEndTag() {
        return SKIP_BODY;
    }

    /**
     * Subroutine for processing lists of episode names/URLs into formatted output
     * @param episodePath String current path in loop
     * @return String formatted output
     */
    private String formatEpisodeListItem(final String episodePath) {
        StringBuilder output = new StringBuilder();
        String episodeContentPath = episodePath
                + FORWARD_SLASH
                + NodeNames.JCR_CONTENT.nodeName();
        Resource episode = resource.getResourceResolver().getResource(episodeContentPath);
        String episodeName = ResourceUtil.getValueMap(episode)
                .get(PagePropertyNames.JCR_TITLE.propertyName(), String.class);
        String episodePageUrl = DataUtil.showPageUrlFromShowAssetPath(episodePath,this.appType);
        if (episodePath.equals(episodeAssetPath)) {
            output.append("<li class=\"current-show\"><span>");
            output.append(episodeName);
            output.append("</span></li>");
        } else {
            output.append("<li><a href=\"");
            output.append(episodePageUrl);
            output.append("\">");
            output.append(episodeName);
            output.append("</a></li>");
        }
        return output.toString();
    }

    public void setResource(final Resource resource) {
        this.resource = resource;
    }
    public void setEpisodeAssetPath(final String path) {
        this.episodeAssetPath = path;
    }
   public void setApptype(String appType) {
        this.appType = appType;
    }
}
