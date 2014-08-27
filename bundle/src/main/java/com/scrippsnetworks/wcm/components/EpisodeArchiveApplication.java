package com.scrippsnetworks.wcm.components;

import java.util.Collections;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import com.day.cq.wcm.api.Page;
import com.scrippsnetworks.wcm.AbstractComponent;
import com.scrippsnetworks.wcm.asset.show.Show;

/**
 * Class to load the data for Episode Archive Application
 * 
 * @author mei-yichang
 * 
 */
public class EpisodeArchiveApplication extends AbstractComponent {

	/* unique id for the show asset */
	private Page showPage;
	
	@Override
	public void doAction() throws Exception {

		//Can override default behavior through edit dialog
		String showPath = getProperty("showPath");
		
		if(showPath !=null && !showPath.isEmpty())
			showPage = getPageManager().getPage(showPath);
		else
			showPage = getCurrentPage().getParent();
	}

	public Show getShow() {
		try {

		if(showPage != null){
			ResourceResolver resolver = getSlingRequest().getResourceResolver();
			Object sniAsset = showPage.getProperties().get("sni:assetLink");

		if(sniAsset != null && sniAsset instanceof String)
		{
			String sniAssetPath = (String)sniAsset;
			Resource resource = resolver.getResource(sniAssetPath);
			if (resource != null) {
				
				Show show = new Show(resource, 3);
				Collections.reverse(show.getSeries());
				return show;
			}
			
		}
		}
		}catch(Exception e){
			log.error("Episode Archive Application" + e.getMessage());
			return null;
		}
		return null;
	}
}
