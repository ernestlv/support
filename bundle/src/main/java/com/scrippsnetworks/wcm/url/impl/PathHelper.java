package com.scrippsnetworks.wcm.url.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashSet;
import java.lang.StringBuffer;

/**
 * Helper class to implement the UrlMapper for Food Network, which provides
 * bidirectional mapping of resource paths to url paths, as an OSGI service.
 * 
 * 
 * @author Tyrone Tse
 */

public class PathHelper {

	/*
	 * 
	 * Possible deep link URL Pattern =
	 * /content/food/<context>/<contentType>/<contentStart>/<beforeSuffix>/<suffix>.<extension> e.g
	 * /content/food/recipes/photos/fn-recipes-every-week/fn-recipes-every-week-monday/fn-recipe.html
	 */
	private String path;
	private String context;
	private String contextType;
	private String contentStart;
	private String suffix;
	private String nochefSuffix;
	private String beforeSuffix;
	private String extension;
	private String cqPath;
	private String recipeChefNode;

	private String oneLetterBucketPath;
	private String twoLetterBucketPath;
	private String threeLetterBucketPath;
	private String fourLetterBucketPath;
	
	
	private String nochefOneLetterBucketPath;
	private String nochefTwoLetterBucketPath;
	private String nochefThreeLetterBucketPath;
	private String nochefFourLetterBucketPath;

    /** Namespaces recognized by unmangleNamespacePrefixes. */
    private static final HashSet<String> namespaces = new HashSet<String>();
    static {
        namespaces.add("jcr");
    }


	/***
	 * Constructor
	 * 
	 * @param path
	 *            is the full CQ content path like /content/food/recipes/
	 * 
	 *            This method then splits the CQ path into the variables that is
	 *            required to resolve the Food URLs to their full CQ path, by
	 *            the method String
	 *            UrlMapperImp.internalResolve(ResourceResolver
	 *            resourceResolver,HttpServletRequest request, String path)
	 */

	PathHelper(String path) {
		String pathNodes[] = path.split("/");
        path = unmangleNamespacePrefixes(path);
		this.path = path;

		/* Initialize to avoid null pointer errors */
		this.recipeChefNode = "";
		this.context = "";
		this.contextType = "";
		this.beforeSuffix = "";
		this.contentStart = "";
		this.suffix = "";
		this.nochefSuffix="";

		// path should be /content/food/*
		for (int i = 0; i < pathNodes.length; i++) {
			// System.out.println("i="+i+" "+pathNodes[i]);
			if (i == 3) {
				this.context = pathNodes[i];
			}
			if (i == 4) {
				this.contextType = pathNodes[i];
			}
			if (i == 5) {
				this.contentStart = pathNodes[i];
			}
			if (i == (pathNodes.length - 2)) {
				this.beforeSuffix = pathNodes[i];
			}
			if (i == (pathNodes.length - 1)) {
				this.suffix = pathNodes[i];
			}
		}

		// recipe Chef node
		if (context.matches("recipes")) {

			// Set recipeChefNode
			this.recipeChefNode = "/content/food/" + context + "/"
					+ contextType;

		}

		if (context.matches("shows|sponsored")) {
			// content/food/shows/good-eats/episodes =>
			// /content/food/shows/g/good-eats/episodes
						
			if (!contextType
					.matches("videos|photos|packages|web-series|articles|menus|reviews")) {
				suffix = path.substring(path.indexOf(contextType));
			}
			else
			{
				suffix = path.substring(path.indexOf(this.contentStart));
			}
					
		} else if (contextType
				.matches("videos|photos|packages|web-series|articles|menus|reviews")
				&& !contentStart.contentEquals("")) {
			// content/food/healthy/packages/

			/*
			 * e.g. set the suffix to url after contextType
			 * 
			 * /content/food/holidays/packages/holidays/holiday-central-recipes-
			 * budget.html
			 * 
			 * suffix = /holidays/holiday-central-recipes-budget.html
			 */

			String result[] = path.split(contextType + "/");
			if (result.length > 1) {
				suffix = result[1];
			}
		} else if (context.matches("recipes")
				&& !contextType
						.matches("videos|photos|packages|web-series|articles|menus")) {

			/*
			if (!similarString(contextType,suffix))
			{
				// e.g
				// contextType will be altonBrown
				// suffix will be deep-fried-turkey2.html
				// path=/content/food/recipes/alton-brown/d/de/dee/deep/deep-fried-turkey-recipe/deep-fried-turkey2.html
				
				String result[] = path.split(contextType + "/");
				if (result.length > 1) {
					suffix = result[1];
				}
			} 
			else
			{
				// e.g
				// path=/content/food/recipes/grilled-fruit-salad-with-honey-yogurt-dressing-recipe/grilled-fruit-salad.html
				
				String result[] = path.split("recipes/");
				if (result.length > 1) {
					suffix = result[1];
				}				
			}*/
			
			//Normal Suffix
			String result[] = path.split(contextType + "/");
			if (result.length > 1) {
				suffix = result[1];
			}
			
			//No Chef Suffix
			nochefSuffix="";
			String result2[] = path.split("recipes/");
			if (result2.length > 1) {
				nochefSuffix = result2[1];
			}				
			
		}		
		else if (context.matches("videos")) {
			if (contextType
					.matches("channels|players")) {
				String result[] = path.split(contextType + "/");
				if (result.length > 1) {
					suffix = result[1];					
				}
			} 
			else
			{
				String result[] = path.split(context + "/");
				if (result.length > 1) {
					suffix = result[1];				
				}
			}
			
		}
				

		try {
			extension = path.substring(path.indexOf("."));
		} catch (Exception e) {

			extension = "";
		}

		cqPath = path.replace(extension, "");

		String oneLetterBucket = suffix;
		String twoLetterBucket = suffix;
		String threeLetterBucket = suffix;
		String fourLetterBucket = suffix;
		
		
		String nochefOneLetterBucket = nochefSuffix;
		String nochefTwoLetterBucket = nochefSuffix;
		String nochefThreeLetterBucket = nochefSuffix;
		String nochefFourLetterBucket = nochefSuffix;	

		int suffixLength = suffix.length();

		if (suffixLength >= 1) {
			oneLetterBucket = suffix.substring(0, 1).toLowerCase();
		}

		if (suffixLength >= 2) {
			twoLetterBucket = suffix.substring(0, 2).toLowerCase();
		}
		if (suffixLength >= 3) {
			threeLetterBucket = suffix.substring(0, 3).toLowerCase();
		}
		if (suffixLength >= 4) {
			fourLetterBucket = suffix.substring(0, 4).toLowerCase();
		}
		
		
		int nochefSuffixLength = nochefSuffix.length();
		if (nochefSuffixLength >= 1) {
			nochefOneLetterBucket=nochefSuffix.substring(0, 1).toLowerCase();
		}

		if (nochefSuffixLength >= 2) {
			nochefTwoLetterBucket=nochefSuffix.substring(0, 2).toLowerCase();
		}
		if (nochefSuffixLength >= 3) {
			nochefThreeLetterBucket=nochefSuffix.substring(0, 3).toLowerCase();
		}
		if (nochefSuffixLength >= 4) {
			nochefFourLetterBucket=nochefSuffix.substring(0, 4).toLowerCase();
		}		

		// Test if path contains videos paths that contain more than 250 videos
		
		//if (path.matches(".*/videos/[0-9]/*.*") && context.matches("videos")) {
			// content/food/videos/1/awesome-video-number-251-27960056.html
		//	suffix = beforeSuffix + "/" + suffix;			
		//}
		

		// if (!(context.matches("how-tos") || context.matches("how-to")) )
		if (!(context.matches("how-tos") && contextType.contains("how-to") || context
				.matches("how-to") && contextType.contains("how-to"))) {
			// e.g shows/a
			this.oneLetterBucketPath = path.replace(suffix, oneLetterBucket
					+ "/" + suffix);
			
			this.nochefOneLetterBucketPath = path.replace(nochefSuffix, nochefOneLetterBucket
					+ "/" + nochefSuffix);
			
			

			// e.g shows/a/aw
			this.twoLetterBucketPath = path.replace(suffix, oneLetterBucket
					+ "/" + twoLetterBucket + "/" + suffix);

			this.nochefTwoLetterBucketPath = path.replace(nochefSuffix, nochefOneLetterBucket
					+ "/" + nochefSuffix);			

			// e.g shows/a/aw/awe
			this.threeLetterBucketPath = path.replace(suffix, oneLetterBucket
					+ "/" + twoLetterBucket + "/" + threeLetterBucket + "/"
					+ suffix);
			
			this.nochefThreeLetterBucketPath = path.replace(nochefSuffix, nochefOneLetterBucket
					+ "/" + nochefTwoLetterBucket + "/" + nochefThreeLetterBucket + "/"
					+ nochefSuffix);			

			// e.g shows/a/aw/awe/awes
			this.fourLetterBucketPath = path.replace(suffix, oneLetterBucket
					+ "/" + twoLetterBucket + "/" + threeLetterBucket + "/"
					+ fourLetterBucket + "/" + suffix);
			
			this.nochefFourLetterBucketPath = path.replace(nochefSuffix, nochefOneLetterBucket
					+ "/" + nochefTwoLetterBucket + "/" + nochefThreeLetterBucket + "/"
					+ nochefFourLetterBucket + "/" + nochefSuffix);			
		} else {
			/*
			 * Only 1 letter bucketing for how-tos e.g
			 * /content/food/how-tos/how-to-cut-an-onion =>
			 * /content/food/how-tos/c/how-to-cut-an-onion
			 */

			// Set oneLetterBucket to the first letter following how-to-
			oneLetterBucket = suffix.replaceFirst("how-to-", "")
					.substring(0, 1).toLowerCase();
			this.oneLetterBucketPath = path.replace(suffix, oneLetterBucket
					+ "/" + suffix);
			this.twoLetterBucketPath = "";
			this.threeLetterBucketPath = "";
			this.fourLetterBucketPath = "";
			
			
			this.nochefOneLetterBucketPath = path.replace(suffix, nochefOneLetterBucket
					+ "/" + nochefSuffix);
			this.nochefTwoLetterBucketPath = "";
			this.nochefThreeLetterBucketPath = "";
			this.nochefFourLetterBucketPath = "";			
		}
	}

	/***
	 * @param pathHTML
	 *            : Path of file name to be converted to a CQ path
	 * 
	 *            Static helper method to strip the .html (or any extension)
	 *            after the last "." in the path name
	 * 
	 * 
	 */
	static public String getCQPathHTML(String pathHTML) {
		String cqPath = "";
		try {

			int dotPosition=pathHTML.indexOf(".");
			
			if (dotPosition>=0)
			{	
				cqPath = pathHTML.substring(0, dotPosition);
			}
			else
			{
				cqPath = pathHTML;
			}
			
			return cqPath;
		} catch (Exception e) {
			return pathHTML;
		}
	}
		
	/***
	 * @param path
	 *            : Bucketed path e.g
	 *            /content/food/recipes/no-chef/f/fr/fri/frie
	 *            /fried-chicken/videos
	 * 
	 *            Static helper method to take a bucketed path and convert it to
	 *            an unbucketed path
	 * 
	 *            Used by the the method String
	 *            UrlMapperImp.map(ResourceResolver
	 *            resourceResolver,HttpServletRequest request, String path)
	 */
	static public String unBucketPath(String path) {
		final String singleBuckets = "((?<=/shows|/topics|/channels|/channel|/photos|/player|/players|/articles|/packages|/menus|/sponsored|/restaurants)/[^/]/)";
		// final String fourBuckets = "(/[^/]/[^/]{2}/[^/]{3}/[^/]{4}/)";

		final String fourThreeTwoBuckets = "(/[^/]/[^/]{2}/[^/]{3}/[^/]{4}/)|(/[^/]/[^/]{2}/[^/]{3}/)|(/[^/]/[^/]{2}/)";
		
		
		final String resturantStateCity = "/restaurants/[a-z]{2}/[\\w\\s-]+/";
		
		final String resturantStateCityBucket ="/restaurants/[a-z]{2}/[\\w\\s-]+/[a-z0-9]/";
		
		

		final Pattern p1 = Pattern.compile(fourThreeTwoBuckets);
		final Pattern p2 = Pattern.compile(singleBuckets);
		
		final Pattern p3 = Pattern.compile(resturantStateCity);
		
		final Pattern p4 = Pattern.compile(resturantStateCityBucket);

		String retPath = path;

		// Filter on fourBuckets first
		Matcher m1 = p1.matcher(retPath);
		
		// Filter on oneBucktet next
		Matcher m2 = p2.matcher(retPath);
		
		// Filter on restaurants		
		Matcher m3 = p3.matcher(retPath);
		Matcher m4 = p4.matcher(retPath);		
		
		
		
		if (m1.find()) {
			// Single Bucketing
			retPath = m1.replaceFirst("/");			
		} 
		else if (m2.find()) {
			// Four, Three & Two Bucketing			
			retPath = m2.replaceFirst("/");
		}				
		else if (m3.find() && m4.find()) {
			// Restaurant bucketing			
			String m3find=retPath.substring(m3.start(), m3.end());
			String m4find=retPath.substring(m4.start(), m4.end());						
			retPath = retPath.replaceFirst(m4find, m3find);		
		}
				
		retPath = retPath.replace("/recipes/no-chef/", "/recipes/");

		return retPath;

	}

	public String getContext() {
		return this.context;
	}
	
	public String getContextType() {
		return this.contextType;
	}	

	public String getSuffix() {
		return this.suffix;
	}

	public String getExtension() {
		return this.extension;
	}

	public String getCQPath() {
		return this.cqPath;
	}

	public String getOneLetterBucketPath() {
		return this.oneLetterBucketPath;
	}

	public String getTwoLetterBucketPath() {
		return this.twoLetterBucketPath;
	}

	public String getThreeLetterBucketPath() {
		return this.threeLetterBucketPath;
	}

	public String getFourLetterBucketPath() {
		return this.fourLetterBucketPath;
	}

	public String getNochefOneLetterBucketPath() {
		return this.nochefOneLetterBucketPath;
	}

	public String getNochefTwoLetterBucketPath() {
		return this.nochefTwoLetterBucketPath;
	}

	public String getNoChefThreeLetterBucketPath() {
		return this.nochefThreeLetterBucketPath;
	}

	public String getNoChefFourLetterBucketPath() {
		return this.nochefFourLetterBucketPath;
	}
	
	
	
	public String getRecipeChefNode() {
		return this.recipeChefNode;
	}

    /** Imperfect emulation of Sling namespace unmangling.
     *
     * The real private method in ResourceResolverImpl interrogates the session for actual namespaces.
     * This method will use static set of namespaces. Using as a quickie fix for FNRHL-1283.
     */
    private static String unmangleNamespacePrefixes(String absPath) {

        if (absPath != null && absPath.contains("/_")) {
            Pattern p = Pattern.compile("/_([^_/]+)_");
            Matcher m = p.matcher(absPath);
            StringBuffer buf = new StringBuffer();
            while (m.find()) {
                String namespace = m.group(1);
                if (namespaces.contains(namespace)) {
                    String replacement = "/" + namespace + ":";
                    m.appendReplacement(buf, replacement);
                }
            }
            m.appendTail(buf);
            absPath = buf.toString();
        }

        return absPath;
    }

}
