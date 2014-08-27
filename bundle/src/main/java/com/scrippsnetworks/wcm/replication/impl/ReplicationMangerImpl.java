package com.scrippsnetworks.wcm.replication.impl;

import java.util.Calendar;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.replication.ReplicationAction;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.ReplicationOptions;
import com.day.cq.replication.ReplicationStatus;
import com.day.cq.replication.Replicator;
import com.day.cq.replication.Preprocessor;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.scrippsnetworks.wcm.replication.ReplicationManager;
import com.scrippsnetworks.wcm.util.AssetPropertyNames;
import com.scrippsnetworks.wcm.util.PagePropertyNames;
import com.scrippsnetworks.wcm.fnr.util.AssetSlingResourceTypes;


@Component(label="SNI WCM Replication Manager",description="Determines appropriate replication action for content and sni-assets on content activation by regular users.",enabled=true,immediate=true,metatype=true)
@Service(value= {ReplicationManager.class, Preprocessor.class})
public class ReplicationMangerImpl implements ReplicationManager, Preprocessor{
	
	private final Logger LOGGER = LoggerFactory.getLogger(ReplicationMangerImpl.class);
	
	@Reference
	ResourceResolverFactory resolverFactory;
	
	@Reference
	private Replicator replicator;
	
	@Property(label = "Preprocess Enabled", description = "A boolean value to determine if Replication Preprocess function will run prior to any replication action. ")
	public static final String PREPROCESSENABLED = "preprocessEnabled";
	
	/**
     * Configurable property to determine if the Preprocess function will do anything. Put in place
     * to have more flexibility and control over what instances the pre-process will run. 
     * If enabled=true the preprocess function will run for every replication event on the specified instance
     */
    private boolean preprocessEnabled;
	
    /**
     * Hard coded values for special users
     */
	private static final String ADMIN_USER_ID = "admin";
	private static final String MIGRATION_USER_ID = "migration";
	
	private static final String PROP_SNI_SEARCHABLE = "sni:searchable";
	
	
	protected void activate(ComponentContext context) throws Exception {
		this.preprocessEnabled = OsgiUtil.toBoolean(context.getProperties().get(PREPROCESSENABLED), false);
		LOGGER.debug("Adding Replication Manager.");
	}
	
	protected void deactivate(ComponentContext componentContext) {
		LOGGER.debug("Removing Replication Manager.");
	}
	
	
	@Override
	public void checkContentPageReplication(ResourceResolver resourceResolver, ReplicationAction action) throws ReplicationException {
		
		PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
		String contentpath = action.getPath();
		Page contentPage = pageManager.getPage(contentpath);
		if(contentPage == null)
		{
			LOGGER.debug("Content page {} could not be resolved: ", contentpath);
			return;
		}		
		ValueMap contentPageProperties = contentPage.getProperties();

		String assetPath = checkforSNIAsset(contentPageProperties);  
		LOGGER.debug("Attempting to activate {} of content page : " + contentPage.getPath(), assetPath);
		Session localSession = null;
		try {		
			 //check to see if resource to be replicated has a corresponding sni-asset 
   			if(assetPath != null && !assetPath.isEmpty()){
   				Page assetPage = pageManager.getPage(assetPath);
   				if(assetPage != null){
					if(needsToBeActivated(assetPage)) {
						LOGGER.debug("Content {} has been activated, looking to activate " + assetPath, contentpath);
						localSession = resourceResolver.adaptTo(Session.class).impersonate(new SimpleCredentials(MIGRATION_USER_ID, new char[0]));   						
						replicator.replicate(localSession, ReplicationActionType.ACTIVATE, assetPath);
					}
   				}
   			}
   		
		}catch (NullPointerException e){
			LOGGER.error("An error occurred while trying to determine the replication action for a content page: " + contentpath + " {}", e);
			throw new ReplicationException("An unexpected error occurred while determining the replication status of " + contentpath);				
		} catch (javax.jcr.LoginException e) {
				LOGGER.error("An error occurred while trying to impersonate {} and trying to deactivate " + assetPath + " " + e.getMessage(), action.getUserId());
		} catch (RepositoryException e) {
				LOGGER.error("An error occurred while trying to impersonate {} and trying to deactivate " + assetPath + " " + e.getMessage(), action.getUserId());
				e.printStackTrace();
		} finally {
			//remember to close out our impersonated session
			if(localSession != null)
				localSession.logout();
		}
	}
	

	  /***
	    * Initial check to see if the resource that we are trying to activate contains a property sni:assetLink
	    * 
	    * @param path
	    * @return the string value of sni:assetLink or null if it doesn't exist
	    */
	   private String checkforSNIAsset(ValueMap pageProperties){
			   String assetLink = pageProperties.get(PagePropertyNames.SNI_ASSET_LINK.propertyName(), String.class);
			   return assetLink;
	   }
	   
	   
	   /***
	    * Method to check if a page at a given path has been modified since it's last activation action 
	    * or it has never been activated
	    * returns true if this page needs to be activated
	    * @param path
	    * @param resolver
	    * @return
	    */
	   private boolean needsToBeActivated(Page page)
	   {
           if (page != null) {
               ReplicationStatus rs = page.adaptTo(ReplicationStatus.class);
               if (rs != null && rs.getLastReplicationAction() == ReplicationActionType.ACTIVATE) {
            	   
            	   Calendar lastMod = page.getLastModified();
                   Calendar lastPub = rs.getLastPublished();
                   LOGGER.debug("checking to see if we need to re-activate the page based on lastModified {} and lastPublished {}", lastMod, lastPub);
                   if (lastMod != null && lastPub != null) {
                	   if(lastMod.after(lastPub)) {
                		   LOGGER.debug("last modified after last published, needs to be activated again");
                		   return true;
                	   }
                	   else 
                		   return false;
                   }
               }
               return true;
           }
           return false;

	   }

	   /***
	    * Method to check if a page has been previously activated 
	    * returns true if this page has been activated before
	    * @param path
	    * @return
	    */
	   private boolean isPageActivated(Page page){
           if (page != null) {
               ReplicationStatus rs = page.adaptTo(ReplicationStatus.class);
               if (rs != null) {
            	   return rs.isActivated() ;
               }
           }
           return false;
	   }	   
	    
	    /***
	     * This method is called when a sni-asset has been created. For now the rule is that if the sni-asset is created then
	     * it will be activated. This also means that any corresponding content pages are also activated 
	     * @param resolver
	     * @param replicator
	     * @param assetPath
	     * @throws ReplicationException 
	     */
	    public void sniAssetCreated(ResourceResolver resolver, Replicator replicator, String assetPath) throws ReplicationException
	    {
	    	PageManager pageManager = resolver.adaptTo(PageManager.class);
			Page assetPage = pageManager.getPage(assetPath);
			if(assetPage == null)
				return;
			
			LOGGER.debug("sni-asset {} created", assetPath);
			if(assetPage.hasContent()){
				String userId = assetPage.getLastModifiedBy();
				ValueMap assetProperties = assetPage.getProperties();
				
				Session session = resolver.adaptTo(Session.class);
				//Attempt to impersonate session for user who last modified sni-asset, should be 'migration'
				Session localSession = null;
				try {
					localSession = session.impersonate(new SimpleCredentials(userId, new char[0]));
						
					LOGGER.debug("Start Auto-activating sni-asset {} and activate corresponding content pages", assetPath);	
					replicator.replicate(localSession, ReplicationActionType.ACTIVATE, assetPath);
					LOGGER.debug("End Auto-activated sni-asset {} and activate corresponding content pages", assetPath);	
					
					//Don't auto activate content page of type Videos/Recipes if doNotSearch property is set to true
					if(!isAssetAutoActivationDisabled(assetPage)){
						//Don't auto-activate content page is asset is SHOW or TALENT
						if(!isAssetShowOrTalent(assetPage)){
							//auto-activated assets and also auto-activate coressponding content pages
							String[] pageLinks = assetProperties.get(AssetPropertyNames.SNI_PAGE_LINKS.propertyName(), String[].class);
							
							if(pageLinks != null) {
								for(String pageLink : pageLinks) {
										LOGGER.debug("Auto-activating content page {} of the newly created sni-asset " + assetPath, pageLink);
										replicator.replicate(localSession, ReplicationActionType.ACTIVATE, pageLink);									
									}
							}				
						}
					}
					
				} catch (javax.jcr.LoginException e) {
					LOGGER.error("an error occurred while trying to impersonate session with userid of: " + userId + " " + e.getMessage());
				} catch (RepositoryException e) {
					LOGGER.error("an error occurred while trying to impersonate session with userid of: " + userId + " " + e.getMessage());
				} finally {
					//remember to close out our impersonated session
					if(localSession != null)
						localSession.logout();
				}
	    	}else{
	    		LOGGER.debug("sni-asset {} does not have any CONTENT", assetPath);
	    	}
	    }
	   
		   /***
		    * Method to check if a sni-asset is Video and sni:searchable property is set to true
		    * returns true if this sni-asset is Video and sni:searchable property is set to true
		    * @param page
		    * @return boolean 
		    */
		   private boolean isAssetAutoActivationDisabled(Page page)
		   {
	           if (page != null) {
	        	   ValueMap assetPageProperties = page.getProperties();
	        	   String assetLink = assetPageProperties.get(PagePropertyNames.SLING_RESOURCE_TYPE.propertyName(), String.class);
	        	   if(assetLink != null){
		        	   if(assetLink.equalsIgnoreCase(AssetSlingResourceTypes.VIDEO.resourceType())){
		        		   Boolean searchableFlag = assetPageProperties.get(PROP_SNI_SEARCHABLE, Boolean.class);
		        		   if(searchableFlag != null){
		        			   if(!searchableFlag){
		        				   return true;   	  
		        			   }
		        		   }
		        	   }	            
	        	   }
	           }
	           return false;
		   }	    
		   
		   /***
		    * Method to check if a sni-asset is show or talent 
		    * returns true if a sni-asset is show or talent then don't auto activate corresponding content page
		    * @param page
		    * @return boolean 
		    */
		   private boolean isAssetShowOrTalent(Page page)
		   {
	           if (page != null) {
	        	   ValueMap assetPageProperties = page.getProperties();
	        	   String assetLink = assetPageProperties.get(PagePropertyNames.SLING_RESOURCE_TYPE.propertyName(), String.class);
	        	   if(assetLink != null){
		        	   if(assetLink.equalsIgnoreCase(AssetSlingResourceTypes.SHOW.resourceType()) ||  assetLink.equalsIgnoreCase(AssetSlingResourceTypes.PERSON.resourceType())){
		        		   return true;   	  
		        		   }
		        	   }
	        	   }	               
	           return false;
		   }		   
	    
    /***
     * This method is called when a sni-asset has been modified. For now the rule is that if the sni-asset is modified then
     * it will be activated. This also means that any corresponding content pages are also re-activated 
     * (even if the content page have not been modified, we still need to activate it in order to flush the dispatcher)
     * @param resolver
     * @param replicator
     * @param assetPath
     * @throws ReplicationException 
     */
    public void sniAssetModified(ResourceResolver resolver, Replicator replicator, String assetPath) throws ReplicationException
    {
    	PageManager pageManager = resolver.adaptTo(PageManager.class);
		Page assetPage = pageManager.getPage(assetPath);
		if(assetPage == null)
			return;
		
		LOGGER.debug("sni-asset {} modified", assetPath);
		String userId = assetPage.getLastModifiedBy();
		ValueMap assetProperties = assetPage.getProperties();
		
		Session session = resolver.adaptTo(Session.class);
		//Attempt to impersonate session for user who last modified sni-asset, should be 'migration'
		Session localSession = null;
		try {
			localSession = session.impersonate(new SimpleCredentials(userId, new char[0]));
			
			replicator.replicate(localSession, ReplicationActionType.ACTIVATE, assetPath);
			LOGGER.debug("Auto-activated sni-asset {}. and activate corresponding content pages", assetPath);	
			
			LOGGER.debug("isAssetAutoActivationDisabled {} ", isAssetAutoActivationDisabled(assetPage));
			
			//Don't auto activate content page of type Videos if sni:searchable property is set to true
			if(!isAssetAutoActivationDisabled(assetPage)){
					//Don't auto-activate content page is asset is SHOW or TALENT
					if(!isAssetShowOrTalent(assetPage)){
						//auto-activated assets and also auto-activate coressponding content pages
						String[] pageLinks = assetProperties.get(AssetPropertyNames.SNI_PAGE_LINKS.propertyName(), String[].class);
					
						if(pageLinks != null) {
							for(String pageLink : pageLinks) {
								LOGGER.debug("Content page " + pageLink);
								Page contentPage = pageManager.getPage(pageLink);
								//only auto-activate the content page if it's already activated
								if(isPageActivated(contentPage)){
									LOGGER.debug("Auto-activating content page {}. based on changes to sni-asset " + assetPath, pageLink);
									replicator.replicate(localSession, ReplicationActionType.ACTIVATE, pageLink);							
								}
							}
						}
				}
			}
		} catch (javax.jcr.LoginException e) {
			LOGGER.error("an error occurred while trying to impersonate session with userid of: " + userId + " " + e.getMessage());
		} catch (RepositoryException e) {
			LOGGER.error("an error occurred while trying to impersonate session with userid of: " + userId + " " + e.getMessage());
		} finally {
			//remember to close out our impersonated session
			if(localSession != null)
				localSession.logout();
		}
    }
    
	@Override
	public void preprocess(ReplicationAction action, ReplicationOptions options)
			throws ReplicationException {
		
			LOGGER.debug("preprocessEnabled {} ", preprocessEnabled);	
			if(action.getType().equals(ReplicationActionType.ACTIVATE)){	
				//run pre-process checks if the manager is enabled the user is not "admin" or "migration" 
				//and if the they are trying replicate anything under content
				if(preprocessEnabled && !action.getUserId().equals(ADMIN_USER_ID) && !action.getUserId().equals(MIGRATION_USER_ID)
						&& action.getPath().startsWith("/content")) {
		          // create resource resolver
		          ResourceResolver resourceResolver = null;
		          try {
		        		resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
						checkContentPageReplication(resourceResolver, action);							
					} catch (LoginException e) {
						throw new ReplicationException("Unable to create admin resource resolver for replication (started by " + action.getUserId()
								+ ")", e);
					} finally {
						 
						if (resourceResolver != null) {
			            	  resourceResolver.close();
			              }
					}
				}
			}
	}


}
