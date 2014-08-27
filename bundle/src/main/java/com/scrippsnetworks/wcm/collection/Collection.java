package com.scrippsnetworks.wcm.collection;

import java.util.List;
import java.util.Map;

/**
 * @author Mallik Vamaraju Date: 9/23/13
 * @updated Venkata Naga Sudheer Donaboina: 11/21/2013
 */

public interface Collection {

	/** Returns collection text associated with this collection page */
	public List<String> getCollectedText();

	/** Returns all asset Path assetUid, pagetype or the image path associated with this collection page. */
	public List<Map<String, String>> getCollectionAssetMapList();

}
