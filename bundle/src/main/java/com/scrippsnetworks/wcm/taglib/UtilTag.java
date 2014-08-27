package com.scrippsnetworks.wcm.taglib;


import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.ServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.json.*;
import java.lang.String;
import java.util.*;
import com.scrippsnetworks.wcm.taglib.ListBean;
import com.scrippsnetworks.wcm.util.Constant;

import com.day.cq.wcm.api.PageManager;

/*
 * Provides the sni:util tag that encapsulates image logic so that a single custom tag can be used instead of scriptlet code.
 * @author Sreeni Johnson
 */
public class UtilTag extends TagSupport {

	private static final long serialVersionUID = 1L;
	private ListBean collection;
	private Resource resource;

	public int doStartTag() throws JspException {
		try {
			ServletRequest request = pageContext.getRequest();
			
			ValueMap props=null;
			String assetPath=null;
			PageManager pageManager=null;

            if (collection == null) {
                throw new JspException("Error:Pass resource list************* ");
            }
			int collectionSize = collection.getList().size();
			// need ResourceResolver to de-reference talent URIs
			// this looks complicated, but really
			// so we can have a structure like this
			// entryMaps["inSeasonNowEntryMap"]["Breakfast"]["Onion"]
			// which can be used by jstl
			Map<String, Map<String, List<String>>> entryMaps = new HashMap<String, Map<String, List<String>>>();

			String[] entryStrings = (String[]) collection.getList().toArray(
					new String[collectionSize]);
			if (entryStrings != null) {
				// since stored as String array, i use JSON lib as a util to
				// string
				// parse
				for (String entryString : entryStrings) {
					Resource entryRc = resource.getChild(entryString);
					if (entryRc == null) {
						break;
					}
					Map<String, List<String>> entryMap = new LinkedHashMap<String, List<String>>();
					entryMaps.put(entryString, entryMap);
					String[] arr = entryRc.adaptTo(String[].class);
					for (String s : arr) {
						JSONObject obj = new JSONObject(s);
						String cat = obj.getString("text");
						List<String> items = new ArrayList<String>();
						entryMap.put(cat, items);
						JSONArray jsonArr = obj.getJSONArray("list");
						for (int i = 0; i < jsonArr.length(); i++) {
							String item = jsonArr.getString(i);
							// get a uri for Talent
							// TODO use real Talent asset, association, etc
							// current Talent asset is placeholder
							if (entryString
									.equalsIgnoreCase("chef-recipe-entries")) {
								Resource currentResource = resource
										.getResourceResolver()
										.getResource(item);
								// TODO check for nulls or switch to groovy
								// note... iterative cost of dereferencing many
								// lists?
								
								if (currentResource != null) {
							
									pageManager=(PageManager)pageContext.getAttribute("pageManager");
									props=pageManager.getPage(currentResource.getPath()).getProperties();
									item=props.get(Constant.JCR_TITLE,String.class);
									if(item==null){
										assetPath=props.get(Constant.ASSET_LINK,String.class);
										props=pageManager.getPage(assetPath).getProperties();
										item=props.get(Constant.JCR_TITLE,String.class);
								
							  	}
							 }
							}
							items.add(item);
						}// end innermost-for
					}// end middle-for
				}// end outer-for
			}// end if

			// option of using map of maps
			request.setAttribute("", entryMaps);
			// split them up in usuable maps
			request.setAttribute("inSeasonNowEntryMap",
					entryMaps.get("in-season-now-entries"));
			// to make testing easier only; remove this overriden record after
			// dev
			// request.setAttribute("inSeasonNowEntryMap",entryMaps.get("chef-recipe-entries"));
			request.setAttribute("quickMealEntryMap",
					entryMaps.get("quick-meal-entries"));
			request.setAttribute("chefRecipeEntryMap",
					entryMaps.get("chef-recipe-entries"));

		} catch (JSONException jsonexception) {
			throw new JspException("Error: " + jsonexception.getMessage());
		}

		return SKIP_BODY;
	}

	public int doEndTag() throws JspException {
		return SKIP_BODY;
	}

	public void setCollection(ListBean pCollection) {
		collection = pCollection;
	}

	public void setResource(Resource pResource) {
		resource = pResource;
	}
}
