package apps.scripps.authorext;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.security.User;
import com.day.cq.wcm.api.NameConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.io.JSONWriter;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;

@SlingServlet(
        paths="/bin/scripps/authorext/settings",
        methods = "GET",
        metatype = false,
        label = "Author Extensions Settings Servlet"
)
public class AuthorExtSettings extends SlingAllMethodsServlet {
    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(AuthorExtSettings.class);
    protected static Dictionary SETTINGS = null;

    /**
     *
     * @param context
     * @throws Exception
     */
    protected void activate(ComponentContext context) throws Exception {
        SETTINGS = context.getProperties();

        if(log.isInfoEnabled()){
            log.info("Activating AuthorExtSettings bundle, SETTINGS : " + SETTINGS);
        }
    }

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        JSONWriter jw = new JSONWriter(response.getWriter());

        try{
            ResourceResolver resolver = request.getResourceResolver();

            jw.object();
            Session userSession = resolver.adaptTo(Session.class);

            jw.key("user").value(userSession.getUserID());

            Enumeration pIter = SETTINGS.keys();
            String prop = null; Object value = null;

            String draftsPath = "/content/food/drafts";

            while(pIter.hasMoreElements()){
                prop = (String)pIter.nextElement();
                value = SETTINGS.get(prop);

                if(value instanceof String[]){
                    String values[] = (String[])value;

                    StringBuilder sb = new StringBuilder();

                    for(String v : values){
                        sb.append(v).append(",");
                    }

                    sb.deleteCharAt(sb.lastIndexOf(","));
                    jw.key(prop).value(sb.toString());
                }else{
                    jw.key(prop).value(String.valueOf(value));
                }

                if("draftsPath".equals(prop)){
                    draftsPath = String.valueOf(value);
                }
            }

            String userFolderPath = createDraftPaths(resolver, draftsPath);
            jw.key("userDraftsPath").value(userFolderPath);

            jw.endObject();
        }catch(Exception e){
            log.error("Error getting SETTINGS",e);
            throw new ServletException(e);
        }
    }

    /**
     *
     * @param resolver
     * @param draftsPath
     * @throws Exception
     */
    private String createDraftPaths(ResourceResolver resolver, String draftsPath) throws Exception{
        Session userSession = resolver.adaptTo(Session.class);
        Resource fResource = resolver.getResource(draftsPath);
        boolean modified = false;

        Node fNode = null;

        if(fResource == null){
            Node siteNode = resolver.getResource(draftsPath.substring(0,draftsPath.lastIndexOf("/"))).adaptTo(Node.class);
            String folderName = draftsPath.substring(draftsPath.lastIndexOf("/") + 1);

            fNode = JcrUtil.createUniqueNode(siteNode, folderName, "sling:OrderedFolder", userSession);
            Node fContentNode = fNode.addNode(NameConstants.NN_CONTENT, JcrConstants.NT_UNSTRUCTURED);

            fContentNode.setProperty(NameConstants.PN_TITLE, folderName);
            modified = true;
        }else{
            fNode = fResource.adaptTo(Node.class);
        }

        String userDraftsFolder = null;
        String userFolderPath = null;

        try{
            User user = resolver.adaptTo(User.class);
            userDraftsFolder = user.getName();
        }catch(Exception e){
            log.warn("Error retrieving user firstname and lastname", e);
        }

        if(StringUtils.isEmpty(userDraftsFolder)){
            userDraftsFolder = userSession.getUserID();
        }

        userFolderPath = draftsPath + "/" + userDraftsFolder;
        Resource uResource = resolver.getResource(userFolderPath);

        if(uResource == null){
            Node uNode = JcrUtil.createUniqueNode(fNode, userDraftsFolder, "sling:OrderedFolder", userSession);
            Node uContentNode = uNode.addNode(NameConstants.NN_CONTENT, JcrConstants.NT_UNSTRUCTURED);
            uContentNode.setProperty(NameConstants.PN_TITLE, userDraftsFolder);

            modified = true;
        }

        if(modified){
            userSession.save();
        }

        return userFolderPath;
    }
}