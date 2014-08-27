package com.scrippsnetworks.wcm.asset.person;

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.resource.Resource;

import org.apache.commons.lang.Validate;

import com.scrippsnetworks.wcm.asset.show.AbstractResourceObject;
import com.scrippsnetworks.wcm.util.PagePropertyNames;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a representation of recipe data for consumption by web display
 * formatters
 * 
 * @author Danny Gordon 7.24.12
 * 
 */
@Deprecated
public class Person {
	
	protected final Logger log = LoggerFactory.getLogger(getClass());

	public static final String PERSON_CONTENT_ROOT = AbstractResourceObject.CONTENT_ROOT
			+ "/people";
	public static final String ASSETLINK = "sni:assetLink";
	public static final String ASSETUID = "sni:assetUId";
	public static final String FIRSTLETTER = "sni:firstLetter";
	public static final String IMAGEPATH = "sni:images";
	public static final String SORTTITLE = "sni:sortTitle";
    public static final String PN_PERSON_NAME="jcr:title";
    public static final String AVATARIMAGE = "sni:avatarImage";
    
    //overrides from the person data node located under content
    private static final String[] allowedOverrides = {PN_PERSON_NAME, FIRSTLETTER, IMAGEPATH, SORTTITLE};
    
	private String personContentPath; //path to person page typically under /content/people/chefs (includes jcr:node)
	private Resource personResource;  
	private Map<String, Object> mergeMap; //map of properties of a person merged from /content/people and from /etc/sni-asset/people
	private String personPageURL;

	/**
	 * Construct an empty Person object
	 */
	public Person() {
	}

	/**
	 * Construct a Person object given a Sling Resource of the calling page and
	 * the String representing a path to the content page containing the Person.
	 * 
	 * @param resource
	 *            Sling Resource of page calling for data.
	 * @param path
	 *            String representing a path to the Person page (in most cases is a Chef)
	 *            
	 */
	public Person(final Resource resource, final String path) {
		Validate.notNull(resource);
		Validate.notNull(path);
		personResource = resource;
		personContentPath = path + "/jcr:content";
		personPageURL = path + ".html";
		setPersonValueMap();
	
	}
	
	/***
	 * 
	 */
	private void setPersonValueMap()
	{
			 //get properties under /content/people..
			 ValueMap contentPersonValues = ResourceUtil
					 					.getValueMap(personResource.getResourceResolver().getResource(personContentPath));
			 
			 	//check to make sure we have a valid person and assetlink property
		        if (contentPersonValues != null && contentPersonValues.containsKey(ASSETLINK))
		        {
		        	
		        	String assetLinkLoc = (String)contentPersonValues.get(ASSETLINK) + "/jcr:content";
		      
		        		//get properties under /etc/sni-asset/people...
		        		ValueMap assetPersonValues = 
		        				ResourceUtil.getValueMap(personResource.getResourceResolver().getResource(assetLinkLoc));
		        		
		        		//merge properties
		        		mergeValueMaps(contentPersonValues, assetPersonValues);
		        		
		        	}
		        	
		}

	/***Merges properties of a person from /content/people and from /etc/sni-asset/people
	 * uses a static array allowedOverrides to determine which properties from /content/people are pushed in
	 * @param contentPersonValues
	 * @param assetPersonValues
	 */
	private void mergeValueMaps(ValueMap contentPersonValues, ValueMap assetPersonValues)
	{
		mergeMap = new HashMap<String, Object>();
        if (assetPersonValues != null) {
        	
        	  for (Map.Entry<String,Object> entry : assetPersonValues.entrySet()) {
                  mergeMap.put(entry.getKey(), entry.getValue());
              }
            if (contentPersonValues != null) {
                for (String override : allowedOverrides) {
                    if (contentPersonValues.containsKey(override)){
                    	mergeMap.put(override, contentPersonValues.get(override));
                    }
                }
            }
        }
	}
	
	/***
	 * returns a full map of properties of a person merged from both /content/people and /etc/sni-asset/people
	 * @return
	 */
	public Map<String, Object> getMergedValues()
	{
		if(mergeMap != null)
			return mergeMap;
		else
			return null;
	}
	/***
	 * 
	 * @return a persons name from the jcr:title property. 
	 * the title property under /content/people will override the property under /etc/sni-asset/people
	 * 					
	 */
	public String getName()
	{
		if(mergeMap != null && mergeMap.containsKey(PN_PERSON_NAME) && mergeMap.get(PN_PERSON_NAME) instanceof String)
		{
			return (String)mergeMap.get(PN_PERSON_NAME);
		}
		
		return "";
		
	}
	
	public String getPageURL()
	{
		if(personPageURL != null)
			return personPageURL;
		
		return "";
	}
	
	public String getImagepath()
	{
		if(mergeMap != null && mergeMap.containsKey(IMAGEPATH) && mergeMap.get(IMAGEPATH) instanceof Object[])
		{
			 Object[] imagePaths = (Object[]) mergeMap.get(IMAGEPATH);	 
			 return imagePaths[0].toString();
		}
		
		return "";
	}
	
	
	public String getAvatarimage()
	{
		if(mergeMap != null && mergeMap.containsKey(AVATARIMAGE) && mergeMap.get(AVATARIMAGE) instanceof String)
		{
			return (String)mergeMap.get(AVATARIMAGE);
		}
		
		return "";
	}
	
	private static boolean valueMapIsType(final ValueMap valueMap, final String slingResourceType, final String assetLinkProperty) {
	        return (!(valueMap == null || slingResourceType == null)
	                && valueMap.containsKey(PagePropertyNames.SLING_RESOURCE_TYPE.propertyName())
	                && valueMap.get(PagePropertyNames.SLING_RESOURCE_TYPE.propertyName())
	                .toString().equals(slingResourceType)
	                && valueMap.containsKey(assetLinkProperty));
	    }

	
	
}
