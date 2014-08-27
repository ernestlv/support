package com.scrippsnetworks.wcm.content.impl;


import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;


import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.Validate;
import com.scrippsnetworks.wcm.content.PackageUpdateService;
import com.scrippsnetworks.wcm.content.model.ContentPageBean;


@Component(immediate=true, metatype=true, label="Package update service", description="To update package")
@Service(value=PackageUpdateService.class)
public class PackageUpdateServiceImpl implements PackageUpdateService {
	/**
     * logger  object for handling log messages.
     */
    private static final Logger Log = LoggerFactory.getLogger(PackageUpdateServiceImpl.class);
    
    private static final String PROP_SPONSORSHIP="sni:sponsorship";
    private static final String PROP_PACKAGE="sni:package";
    private static final String RROP_RESOURCE_TYPE="sling:resourceType";

// while the following are ALSO anchors, only show can't be in a package itself,
// so it's filtered from candidate assignees
//    "sni-food/components/pagetypes/menu-listing",
//    "sni-food/components/pagetypes/photo-gallery-listing",
//    "sni-food/components/pagetypes/recipe-listing",
//    "sni-food/components/pagetypes/universal-landing"
    private static final String[] FILTERED_RESOURCE_TYPES = {
            "sni-food/components/pagetypes/show"
    };
        
	@Override
	public List<ContentPageBean> getContentPagesFromSponsorshipCode(Resource resource,String sponsorshipCode, String rootPath) {
		Validate.notNull(resource);				
		try{
					
			//String queryStatement = "SELECT * FROM [nt:base] AS s WHERE ISDESCENDANTNODE(["+rootPath+"]) AND s.[sni:sponsorship]='"+ sponsorshipCode +"'";
			
			StringBuffer queryStatement = new StringBuffer();
			
			queryStatement.append("/jcr:root").append(rootPath)
			.append("//element(*, cq:PageContent)[")
			.append("@sni:sponsorship")
			.append("='").append(sponsorshipCode).append("' ").append(']');
//			
			Log.error("queryStatement-SQL2 is for FNRHL-517 is "+queryStatement);

			Node queryRoot = resource.adaptTo(Node.class);
			QueryManager queryMgr = queryRoot.getSession().getWorkspace().getQueryManager();
			Query query = queryMgr.createQuery(queryStatement.toString(), Query.XPATH);
			QueryResult queryResults = query.execute();
			Node contentNode=null;
			if(null != queryResults){
				NodeIterator nodeItr = queryResults.getNodes();
				List <ContentPageBean>nodeList=new ArrayList<ContentPageBean>();
				ContentPageBean bean=new ContentPageBean();
				
				while(nodeItr.hasNext()){
					try{
						contentNode = nodeItr.nextNode();
                        boolean isFiltered = false;
                        String rt = contentNode.getProperty(RROP_RESOURCE_TYPE).getString();
                        for(String filter: FILTERED_RESOURCE_TYPES){
                            if(rt.equalsIgnoreCase(filter)){
                                isFiltered = true;
                            }
                        }
                        if(!isFiltered){
							bean.setSponsorShipCode(contentNode.getProperty(PROP_SPONSORSHIP).getString());													
							if(contentNode.hasProperty(PROP_PACKAGE)){
								bean.setPackagePath((contentNode.getProperty(PROP_PACKAGE)!=null)?contentNode.getProperty(PROP_PACKAGE).getString():"");
							}else{
								bean.setPackagePath("");
							}							
							bean.setPagePath(contentNode.getParent().getPath());
							nodeList.add(bean);
							bean=new ContentPageBean();
						}
					}catch(Exception ex){
						ex.printStackTrace();
						continue;
					}
				}
				
				return nodeList;
			}			
			
		}catch(Exception ex){
			
			Log.error("Error occurs in getContentPagesFromSponsorshipCode of ContentPageListImpl "+ex.getMessage());
		}
		return null;
	}
}
