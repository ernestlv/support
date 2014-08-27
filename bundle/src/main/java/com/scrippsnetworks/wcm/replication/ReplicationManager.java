package com.scrippsnetworks.wcm.replication;
 
import java.lang.String;

import org.apache.sling.api.resource.ResourceResolver;

import com.day.cq.replication.ReplicationAction;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.ReplicationOptions;
import com.day.cq.replication.Replicator;


/** 
 * <p>
 * The Replication Manager is a collection of methods that contain logic related to the integration/migration process and 
 * activation/deactivation of sni-assets and related content
 * </p>
 * <p>
 * The primary need for a Replication Manger is the bi-directional relationship between an sni-asset and a content page. A content
 * page depends on it's corresponding sni-asset to render correctly, thus when content pages are activated we need to be sure that
 * the sni-asset is also activated as well. On the flip side some of the sni-assets contain a property, fastfwdStatus. This property determines
 * if the asset and corresponding page are even allowed to be activated. Also an update to the fastfwdStatus may warrant a de-activation
 * of the asset and content page.
 * </p>
 *
 * <p>
 * A content page is requested to be activated. The request could have originated from a content editor or triggered by 
 * migration/integration. In this case we need to check if the content page has a corresponding sni-asset. If the content page has an sni-asset
 * we then need to check the fastfwdstatus to see if the page/asset is allowed to be published. We also need to check the replication status
 * of the sni-asset, to see whether it has already been activated. 
 * </p>
 * 
 *
 *
 * @author Rahul Anand
 */
public interface ReplicationManager {


    public void checkContentPageReplication(ResourceResolver resourceResolver, ReplicationAction action) throws ReplicationException ;
        
    public void sniAssetModified(ResourceResolver resolver, Replicator replicator, String assetPath) throws ReplicationException;
    
    public void sniAssetCreated(ResourceResolver resolver, Replicator replicator, String assetPath) throws ReplicationException;
    	
    public void preprocess(ReplicationAction action, ReplicationOptions options) throws ReplicationException;
	
}