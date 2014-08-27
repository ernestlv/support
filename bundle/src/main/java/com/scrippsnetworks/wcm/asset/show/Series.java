package com.scrippsnetworks.wcm.asset.show;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.sling.api.resource.Resource;

/**
 * Class representing the Series asset resource
 * 
 * @author mei-yichang
 * 
 */

public class Series extends AbstractResourceObject {

	public static final String ASSET_TYPE = "SERIES";

	public static final String PROPERTY_SERIES_TYPE = "sni:seriesType";

	/* value for the "special" series type */
	public static final String CON_SPECIAL = "special";

	private List<Episode> episodes;

	public Series(Resource resource, int depts) {
		super(resource);
		if (depts > 0) {
			loadEpisodes(depts);
		}
	}

	private void loadEpisodes(int depts) {
		episodes = new ArrayList<Episode>();
		for (Iterator<Resource> iter = resource.listChildren(); iter.hasNext();) {
			Resource next = iter.next();
			if (checkResourceAssetType(next, Episode.ASSET_TYPE)) {
				Episode temp = new Episode(next, depts - 1);
				episodes.add(temp);
			}
		}
		Collections.sort(episodes, new Comparator<Episode>() {
			public int compare(Episode o1, Episode o2) {
				return o1.getEpisodeId().compareTo(o2.getEpisodeId());
			}
		});
	}

	public boolean isSpecial() {
		return assetProperties != null
				&& assetProperties.get(PROPERTY_SERIES_TYPE, "")
						.equalsIgnoreCase(CON_SPECIAL) ? true : false;
	}

	public Show getShow() {
		if (resource != null) {
			Resource parent = resource.getParent();
			if (checkResourceAssetType(parent, Show.ASSET_TYPE)) {
				return new Show(parent, 0);
			}
		}
		return null;
	}

	public List<Episode> getEpisodes() {
		return episodes;
	}
	
	public String getSeasonTitle(){
		String curTitle = title;
		
		if(curTitle != null && !curTitle.isEmpty()) {
			if(curTitle.startsWith("Specials")) {
				return "Specials";
			} else {
				return "Season " + curTitle.charAt(0);
			}
		}
		
		return "";
	}
}
