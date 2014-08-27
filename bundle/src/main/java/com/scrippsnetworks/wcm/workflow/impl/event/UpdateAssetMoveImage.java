package com.scrippsnetworks.wcm.workflow.impl.event;

import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.Replicator;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkItem;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.exec.WorkflowProcess;
import com.day.cq.workflow.metadata.MetaDataMap;
import com.scrippsnetworks.wcm.util.Constant;
import com.scrippsnetworks.wcm.util.PagePropertyNames;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Constants;
import com.day.cq.commons.jcr.JcrUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

/**
 * SNI Custom workflow process that is called in the Update Asset worklfow to move the image to the current date folder.
 */
@Component
@Service
@Properties({
        @Property(name = Constants.SERVICE_DESCRIPTION, value = "SNI custom move image process to move the image to the current date folder."),
        @Property(name = Constants.SERVICE_VENDOR, value = "SNI"),
        @Property(name = "process.label", value = "SNI Move Image Process")})
public class UpdateAssetMoveImage implements WorkflowProcess {

    @Reference
    private Replicator replicator;

    /** the logger */
    private static final Logger log = LoggerFactory.getLogger(UpdateAssetMoveImage.class);
    
    private static int MAX_IMAGES = 50;
    private static final String TYPE_JCR_PATH = "JCR_PATH";
    //private static final String SLING_ORDERED_FODLER = "sling:OrderedFolder";
    private static final String NT_FOLDER = "nt:folder";
    private static final String DAM_ASSET = "dam:Asset";
    private static final String SLASH = "/";
    private static final String UNSIZED = "unsized";
    private static final String FULLSET = "fullset";
    private static final String SLASH_FULLSET = "/fullset";
    private static final String SLASH_UNSIZED = "/unsized";
    private static final String SECURED = "/secured/";
    private static final String ZERO = "0";

    public void execute(WorkItem item, WorkflowSession session, MetaDataMap args) throws WorkflowException {
        //log.info("Inside UpdateAssetMoveImage execute");
        WorkflowData workflowData = item.getWorkflowData();
        if (workflowData.getPayloadType().equals(TYPE_JCR_PATH)) {
            String path = workflowData.getPayload().toString();
            String newPath = null;
            try {
            	if((path.indexOf(SLASH_FULLSET) != -1) || (path.indexOf(SLASH_UNSIZED) != -1)){ 
            		//Only move image if the path does not have the year/month/day
            		if(moveImage(path)){	
	                    String damInitialPath = null;
	             	   
	                    //Checking the path to create the DAM initial path
	                    if(path.indexOf(SLASH_UNSIZED) != -1){       
	                        damInitialPath = path.substring(0, path.indexOf(UNSIZED)+7);
	                    }else if(path.indexOf(SLASH_FULLSET) != -1){       
	                        damInitialPath = path.substring(0, path.indexOf(FULLSET)+7); 
	                    }
	            		            		
		                Node node = (Node) session.getSession().getItem(path);
		                if (node != null) {		                    
		                    //navigate to the top image node from the original rendition node
		                    Node imageNode =  node.getNode("../../../");
		                    String actualPath = imageNode.getPath();
		 
		                    Calendar cal = Calendar.getInstance();
		                    int day = cal.get(Calendar.DATE);
		                    int month = cal.get(Calendar.MONTH) + 1;
		                    int year = cal.get(Calendar.YEAR);
		                    String damPathYear = damInitialPath+SLASH+year;
		                    String damPathYearMonth = damPathYear+SLASH+month;
		                    String damPathYearMonthDay = damPathYearMonth+SLASH+day;
		                    
		                    //Creating the year, month, day folder if they don't exist
		                    JcrUtil.createPath(damPathYear, NT_FOLDER, session.getSession());
		                    JcrUtil.createPath(damPathYearMonth, NT_FOLDER, session.getSession());
		                    Node dayFolder = JcrUtil.createPath(damPathYearMonthDay, NT_FOLDER, session.getSession());  
		                    newPath = damPathYearMonthDay+SLASH+imageNode.getName()+SLASH;
		                    
		                    //Getting the child nodes of the day folder
		                    NodeIterator childNodes = dayFolder.getNodes();        
		                    int childNodesSize = getChildFolders(childNodes);
		                    log.debug("Number of enum folders: " + childNodesSize);
		                    
		                    //If there are no child nodes of the current day folder then create initial "0" enum folder under it
		                    if(childNodesSize <= 0){
		                        JcrUtil.createPath(damPathYearMonthDay+SLASH+ZERO, NT_FOLDER, session.getSession());    
		                        newPath = damPathYearMonthDay+SLASH+ZERO+SLASH+imageNode.getName()+SLASH;
		                        session.getSession().move(actualPath, newPath);
		                    }
		                    else {
		                        //if there are enum folders under the day folder, get the top number and child nodes under it
		                        //This condition is there as the initial enum folder requirement is 0
		                        childNodesSize = childNodesSize - 1;
		                            
		                        Node enumFolder = (Node)session.getSession().getItem(damPathYearMonthDay+SLASH+childNodesSize);
		                        int enumFolderChildNodes = getDamAssets(enumFolder.getNodes());
		                         //if the number of child images is less than the MAX desired number, move the image to this folder
		                         if(enumFolderChildNodes < MAX_IMAGES){
		                             // move to this folder
		                             newPath = damPathYearMonthDay+SLASH+childNodesSize+SLASH+imageNode.getName()+SLASH;
		                             
		                             moveImage(session, newPath, actualPath);
		                         }
		                         else{
		                            //number of images are equal to the desired number, create a new enum folder +1 and move the image under it.
		                            int newFolderName = Integer.parseInt(enumFolder.getName())+1;
		                            //Dead store removed below
		                            //enumFolder = JcrUtil.createPath(damPathYearMonthDay+SLASH+newFolderName, NT_FOLDER, session.getSession());
		                            newPath = damPathYearMonthDay+SLASH+newFolderName+SLASH+imageNode.getName()+SLASH;
		                            moveImage(session, newPath, actualPath);	
		                        }
		                    }    
		                    log.debug("Moving DAM image to: " + newPath);
		                }
		            }
            	}
                // activate image if it hasn't already been activated in the past, pass in new path if we moved it, otherwise the original path to the image
                String finalPath = "";
                if(newPath==null) {
                    //the path of the current node is at the original rendition, need to move to top level node
                    finalPath = getImagePathFromOriginalRendition(path,session);
                } else {
                    // the moved path of the image is at the right top level image location
                    finalPath = newPath;
                }
                if(!finalPath.toLowerCase().contains(SECURED)) {
                    activateImage(session,finalPath);
                }

            } catch (RepositoryException e) {
                throw new WorkflowException(e.getMessage(), e);
            } catch (ReplicationException e) {
                //throw exception if we couldn't activate image
                throw new WorkflowException(e.getMessage(), e);
            }				
        }
    }
    
	private boolean isInteger( String input ){  
	       try {  
	          Integer.parseInt( input );  
	          return true;  
	       }  
	       catch( Exception e) {  
	          return false;  
	       }  
	    }
	
	private int getChildFolders(NodeIterator nodeIterator) throws RepositoryException {
		int childFolders = 0;
		while(nodeIterator.hasNext()) {
			Node childNode = nodeIterator.nextNode();
			if(childNode.getPrimaryNodeType().getName().equals(NT_FOLDER)) {
				childFolders++;
			}
			
		}
		return childFolders;
	}
	
	private int getDamAssets(NodeIterator nodeIterator) throws RepositoryException {
		int damAssets = 0;
		while(nodeIterator.hasNext()) {
			Node childNode = nodeIterator.nextNode();
			if(childNode.getPrimaryNodeType().getName().equals(DAM_ASSET)) {
				damAssets++;
			}
			
		}
		return damAssets;
	}
	    
    private  boolean moveImage( String path ){  
    	String pathYear = null;
		if(path.indexOf(SLASH_UNSIZED) != -1) {
			pathYear = path.substring(path.indexOf(UNSIZED)+8, path.indexOf(UNSIZED)+12);
			if(isInteger( pathYear ))
				return false;
		}
		else if(path.indexOf(SLASH_FULLSET) != -1){ 
			pathYear = path.substring(path.indexOf(FULLSET)+8, path.indexOf(FULLSET)+12);
			if(isInteger( pathYear ))
				return false;		
		}
    	return true;
     }
    
    private void moveImage (WorkflowSession session, String newPath, String actualPath) throws RepositoryException{
        //If the image already exist in that location 
        if(session.getSession().nodeExists(newPath)){
        	//delete the image, then move
            session.getSession().removeItem(newPath);
            session.getSession().move(actualPath, newPath);
        }else{
        	//Just move the image
            session.getSession().move(actualPath, newPath);
        }   
    }
	
    private String getImagePathFromOriginalRendition(String renditionPath, WorkflowSession session) throws RepositoryException {
        Node node = (Node) session.getSession().getItem(renditionPath);
        Node imageNode =  node.getNode("../../../");
        return imageNode.getPath() + "/";
    }
	
    private void activateImage (WorkflowSession session, String path) throws RepositoryException, ReplicationException {
        Node imageNode = session.getSession().getNode(path + Constant.JCR_CONTENT);
        if(imageNode!=null && !imageNode.hasProperty(PagePropertyNames.CQ_LAST_REPLICATED.propertyName())) {
            // the image exists, and it doesn't have a cq:lastReplicated property, so lets activate it now
            replicator.replicate(session.getSession(), ReplicationActionType.ACTIVATE, path);
            log.debug("activated new image at path:" + path);
        }
    }
}