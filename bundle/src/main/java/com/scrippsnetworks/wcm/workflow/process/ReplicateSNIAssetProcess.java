package com.scrippsnetworks.wcm.workflow.process;

/***
 * 
 * This workflow process step extracts the sni-asset path from the sni:assetLink property of a content page
 * it then kicks off a sub-workflow (SNI Activate) using the sni-asset path as the workflow payload. 
 * Before kicking off the sub-workflow, we check to ensure the sni:fastfwdstatus is set to PUBLISHED
 * 
 */

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import com.scrippsnetworks.wcm.config.TemplateConfigService;

@Service
@Component(metatype = false)
@Property(name = "process.label", value = "Replicate SNI Asset")
public class ReplicateSNIAssetProcess implements WorkflowProcess {
	
    public static final String TYPE_JCR_PATH = "JCR_PATH";
    public static final String TYPE_JCR_UUID = "JCR_UUID";
    private static final String FASTFWD_PUBLISHED_STATUS = "PUBLISHED";
    private static final String ACTIVATE_ACTION = "ACTIVATE";
    private static final String DEACTIVATE_ACTION = "DEACTIVATE";

	
	private static final Logger LOGGER = LoggerFactory.getLogger(ReplicateSNIAssetProcess.class);
	@Reference
	TemplateConfigService templateConfigService;
	 
	@Override
	public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap args)
		throws WorkflowException {
		/*try {
		Session session = workflowSession.getSession();
			WorkflowData data = workItem.getWorkflowData();
			WorkflowModel replicateSNIAsset;
			
			String replicationAction = args.get("PROCESS_ARGS", String.class);
		
			
        String type = data.getPayloadType();
        String contentPagePath = null;
        
        if (type.equals(TYPE_JCR_PATH) && data.getPayload() != null) {
            String payloadData = (String) data.getPayload();
            if (session.itemExists(payloadData)) {
                contentPagePath = payloadData;
            }
        } else if (data.getPayload() != null && type.equals(TYPE_JCR_UUID)) {
            Node node = (Node) data.getPayload();
            contentPagePath = node.getPath();
        }

        if(contentPagePath != null)
        {
        	Node contentJcrNode = session.getNode(contentPagePath + "/jcr:content");
        	   if(contentJcrNode != null)
        	   {
        		  String assetLink =  contentJcrNode.getProperty(DataUtil.PROPERTY_NAME_SNI_ASSET_LINK).getString();
        		  if(assetLink != null && !assetLink.isEmpty())
        		  {
        				  WorkflowData wfData = workflowSession.newWorkflowData("JCR_PATH", assetLink);
        				  
        				  if(replicationAction.equals(ACTIVATE_ACTION))
        				  {
        					  Node assetNode = session.getNode(assetLink + "/jcr:content");
                			  if(assetNode != null)
                			  {
        					  String fastfwdStatus = assetNode.getProperty(DataUtil.PROPERTY_NAME_SNI_FASTFWDSTATUS).getString();
        					  if(fastfwdStatus != null && fastfwdStatus.equals(FASTFWD_PUBLISHED_STATUS))
        					  {
        						  //kick off activate asset workflow
        						  replicateSNIAsset = workflowSession.getModel(templateConfigService.getSNIActivateAssetWorkflowPath() + "/jcr:content/model");
        						  workflowSession.startWorkflow(replicateSNIAsset, wfData, args);
        					  }
                			  }
        				  }
        				  else if(replicationAction.equals(DEACTIVATE_ACTION))
        				  {
        					  LOGGER.info("Deacviating using this workflowModel: " + templateConfigService.getSNIDeactivateAssetWorkflowPath());
        					  //kick off deactivate asset workflow
        					  replicateSNIAsset = workflowSession.getModel(templateConfigService.getSNIDeactivateAssetWorkflowPath() + "/jcr:content/model");
    						  workflowSession.startWorkflow(replicateSNIAsset, wfData, args);
        				  }
        				  else {
        			            LOGGER.warn("Cannot activate page or asset because path is null for this " + "workitem: "
        			                    + workItem.toString());
        			        }
        			  
        		  }
        	   }
        }
        
        
    } catch (RepositoryException e) {
        throw new WorkflowException(e);
    } 
	*/
		}
}
	

