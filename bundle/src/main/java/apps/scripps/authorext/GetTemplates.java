package apps.scripps.authorext;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.io.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

@SlingServlet(
        paths="/bin/scripps/authorext/templates",
        methods = "GET",
        metatype = false,
        label = "Author Extensions Templates Servlet"
)
public class GetTemplates extends SlingAllMethodsServlet {
    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(GetTemplates.class);

    private String COMPONENT = "component";
    private String TEMPLATE = "template";

    /**
     *
     * @param request
     * @param jw
     * @throws Exception
     */
    private void getForCombo(final SlingHttpServletRequest request, JSONWriter jw)throws Exception{
        String template = request.getParameter("query");
        String type = request.getParameter("type");

        if(StringUtils.isEmpty(type)){
            type = COMPONENT;
        }

        ResourceResolver resolver = request.getResourceResolver();
        Session session = resolver.adaptTo(Session.class);
        String[] templatesRootPaths = getTemplateRootPaths(request);

        QueryManager qm = session.getWorkspace().getQueryManager();

        jw.object();
        jw.key("data").array();

        for(String t : templatesRootPaths){
            String stmt = "select * from cq:Template";

            if(!"ALL".equals(t)){
                stmt = stmt + " where jcr:path = '" + t + "/%'";
            }

            if(StringUtils.isNotEmpty(template)){
                stmt = stmt + " and upper(jcr:title) like '%" + template.toUpperCase() + "%'";
            }

            stmt = stmt + "  order by jcr:title";

            Query q = qm.createQuery(stmt, Query.SQL);

            NodeIterator results = q.execute().getNodes();
            Node node = null; String path = null;

            while(results.hasNext()){
                node = results.nextNode();

                if(type.equals(TEMPLATE)){
                    path = node.getPath();
                }else{
                    path = node.getProperty("jcr:content/sling:resourceType").getString();

                    if(path.startsWith("/apps/")){
                        path = path.substring(6);//remove /apps/
                    }
                }

                jw.object();
                jw.key("id").value(path);
                jw.key("name").value(node.getProperty("jcr:title").getString());
                jw.endObject();
            }
        }

        jw.endArray();
        jw.endObject();
    }

    /**
     *
     * @param request
     * @return
     */
    private String[] getTemplateRootPaths(final SlingHttpServletRequest request){
        String templateRoot = request.getParameter("templateRoot");
        String[] templatesRootPaths = null;

        if(StringUtils.isEmpty(templateRoot)){
            templatesRootPaths = (String[])AuthorExtSettings.SETTINGS.get("templatesRoot");
        }else{
            templatesRootPaths = templateRoot.split(",");
        }

        return templatesRootPaths;
    }

    /**
     *
     * @param request
     * @param jw
     * @throws Exception
     */
    private void getForOptionsPredicate(final SlingHttpServletRequest request, JSONWriter jw)throws Exception{
        ResourceResolver resolver = request.getResourceResolver();
        String[] templatesRootPaths = getTemplateRootPaths(request);
        Resource tRoot = null;ValueMap props = null;

        Map<String, Resource> templates = new TreeMap<String, Resource>();

        for(String templatesRootPath : templatesRootPaths){
            tRoot = resolver.getResource(templatesRootPath);

            if(tRoot == null){
                continue;
            }

            Iterator<Resource> children = tRoot.listChildren();

            while (children.hasNext()) {
                Resource r = children.next();
                props = r.adaptTo(ValueMap.class);
                String title = props.get("jcr:title", String.class);

                if(StringUtils.isEmpty(title)){
                    continue;
                }

                templates.put(title, r);
            }
        }

        jw.object();
        jw.key("jcr:title");
        jw.value("Templates");

        for (String title : templates.keySet()) {
            Resource r = templates.get(title);
            props = r.adaptTo(ValueMap.class);
            if ("cq:Template".equals(props.get("jcr:primaryType")) && (!r.getName().equals("managed-region-editor-page"))) {
                jw.key(r.getPath());
                jw.object();
                jw.key("jcr:title");
                jw.value(title);
                jw.key("tagId");
                jw.value(r.getPath());
                jw.endObject();
            }
        }

        jw.endObject();
    }

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        JSONWriter jw = new JSONWriter(response.getWriter());
        String source = request.getParameter("source");

        try{
            if("combo".equals(source)){
                getForCombo(request, jw);
            }else{
                getForOptionsPredicate(request, jw);
            }
        }catch(Exception e){
            log.error("Error getting templates",e);
            throw new ServletException(e);
        }
    }
}
