package com.scrippsnetworks.wcm.taglib.episode;

import java.io.IOException;
import java.util.*;
import com.scrippsnetworks.wcm.asset.DataUtil;
import com.scrippsnetworks.wcm.taglib.Functions;
import org.apache.sling.api.resource.Resource;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Tag to write out the previous/next links on Episode pages
 * @author Jason Clark
 * Date: 8/1/12
 */
public class PreviousNextTag extends TagSupport {

    private Resource resource;
    private String episodeAssetPath;
    private String appType;
    private static final String NEXT = "Next Episode";
    private static final String PREVIOUS = "Previous Episode";
    private static final String CSSNEXT = "next";
    private static final String CSSPREVIOUS = "prev";

    public int doStartTag() throws JspException {
        if (resource == null || episodeAssetPath == null) {
            return SKIP_BODY;
        }
        JspWriter writer = pageContext.getOut();
        StringBuilder output = new StringBuilder();

        List<String> episodes = DataUtil.allEpisodesInSameSeason(resource, episodeAssetPath);

        int totalEpisodes = episodes.size();
        int currentEpisodeIndex = episodes.indexOf(episodeAssetPath);

        if (totalEpisodes > 1 && null==appType) {
        	formatWeb(output, currentEpisodeIndex,totalEpisodes, episodes);
        	
        }else if(totalEpisodes > 1 && appType.equalsIgnoreCase("mob")){
        	formatMobile(output, currentEpisodeIndex,totalEpisodes, episodes);
        }

        try {
            writer.print(output);
        } catch (IOException ioe) {
            throw new JspException(ioe);
        }

        return SKIP_BODY;
    }
    
    public void formatWeb(StringBuilder output, int currentEpisodeIndex,int totalEpisodes, List <String>episodes){
    	output.append("<div id=\"episode-nav\">");
        if (currentEpisodeIndex == 0) { //no previous, has next
            output.append(writeAnchor(episodes.get(currentEpisodeIndex + 1), NEXT));
        } else if (currentEpisodeIndex == totalEpisodes - 1) { //no next, has previous
            output.append(writeAnchor(episodes.get(currentEpisodeIndex - 1), PREVIOUS));
        } else { //somewhere in the middle
            output.append(writeAnchor(episodes.get(currentEpisodeIndex - 1), PREVIOUS));
            output.append("&nbsp;|&nbsp;");
            output.append(writeAnchor(episodes.get(currentEpisodeIndex + 1), NEXT));
        }
        output.append("</div>");
    }
    
    
    public void formatMobile(StringBuilder output, int currentEpisodeIndex,int totalEpisodes, List <String>episodes){
    	output.append("<div class=\"prev-next cf\">");
        if (currentEpisodeIndex == 0) { //no previous, has next
            output.append(writeMobileAnchor(episodes.get(currentEpisodeIndex + 1), NEXT+" &raquo;",CSSNEXT));
        } else if (currentEpisodeIndex == totalEpisodes - 1) { //no next, has previous
            output.append(writeMobileAnchor(episodes.get(currentEpisodeIndex - 1), "&laquo; "+PREVIOUS,CSSPREVIOUS));
        } else { //somewhere in the middle
            output.append(writeMobileAnchor(episodes.get(currentEpisodeIndex - 1), "&laquo; "+PREVIOUS,CSSPREVIOUS));
            output.append("&nbsp;&nbsp;");
            output.append(writeMobileAnchor(episodes.get(currentEpisodeIndex + 1), NEXT+" &raquo;",CSSNEXT));
        }
        output.append("</div>");
    	
    }

    
    /**
     * helper method for writing out previous/next links using an asset path for mobile page
     * @param assetPath String should be a path to an  episode asset
     * @param linkText previous/next text to appear in link
     * @return String containing an anchor tag whipped up from the given path
     */
    private String writeMobileAnchor(final String assetPath, final String linkText,final String cssClass) {
        
    	String pageUrl = DataUtil
                .showPageUrlFromShowAssetPath(Functions.getBasePath(assetPath),this.appType);
        StringBuilder output = new StringBuilder();
        output.append("<a href=\"");
        output.append(pageUrl);
        output.append("\" class=\""+cssClass+"\">");
        output.append(linkText);
        output.append("</a>");
        return output.toString();
    }
    /**
     * helper method for writing out previous/next links using an asset path
     * @param assetPath String should be a path to an  episode asset
     * @param linkText previous/next text to appear in link
     * @return String containing an anchor tag whipped up from the given path
     */
    private String writeAnchor(final String assetPath, final String linkText) {
        
    	String pageUrl = DataUtil
                .showPageUrlFromShowAssetPath(Functions.getBasePath(assetPath),this.appType);
        StringBuilder output = new StringBuilder();
        output.append("<a href=\"");
        output.append(pageUrl);
        output.append("\">");
        output.append(linkText);
        output.append("</a>");
        return output.toString();
    }

    public int doEndTag() throws JspException {
        return SKIP_BODY;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }
    public void setEpisodeAssetPath(String episodeAssetPath) {
        this.episodeAssetPath = episodeAssetPath;
    }
    
    public void setApptype(String appType) {
        this.appType = appType;
    }
}
