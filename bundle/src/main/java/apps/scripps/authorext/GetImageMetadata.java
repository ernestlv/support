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
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Value;
import javax.servlet.ServletException;
import java.io.IOException;
import java.text.SimpleDateFormat;

@SlingServlet(
        paths="/bin/scripps/authorext/imagemetadata",
        methods = "GET",
        metatype = false,
        label = "Author Extensions Image Metadata Servlet"
)
public class GetImageMetadata extends SlingAllMethodsServlet {

    private static final Logger log = LoggerFactory.getLogger(GetImageMetadata.class);
    private static SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
            throws ServletException, IOException{
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        try{
            JSONWriter jw = new JSONWriter(response.getWriter());
            String pathStr = request.getParameter("paths");

            if(StringUtils.isEmpty(pathStr)){
                jw.object();
                jw.endObject();
                return;
            }

            ResourceResolver resolver = request.getResourceResolver();
            String paths[] = pathStr.split(",");

            String[] imProperties = (String[])AuthorExtSettings.SETTINGS.get("imageProperties");

            if( (imProperties == null) || (imProperties.length == 0)){
                imProperties = new String[]{ "jcr:lastModified", "metadata/sni:imageType", "metadata/sni:sniExpirationDate",
                        "metadata/sni:ownerBrand", "metadata/sni:orientation","metadata/sni:additionalInstructions",
                        "metadata/xmpRights:UsageTerms"};
            }

            Node node = null;Property pNode = null;
            String name = null;

            jw.object();

            for(String path : paths){
                if(StringUtils.isEmpty(path)){
                    continue;
                }

                try{
                    node = resolver.getResource(path + "/jcr:content").adaptTo(Node.class);
                    jw.key(path).object();

                    for(String prop : imProperties){
                        if(prop.contains("=")){
                            name = prop.substring(0, prop.indexOf("="));
                            prop = prop.substring(prop.indexOf("=") + 1);
                        }

                        if(!node.hasProperty(prop)){
                            continue;
                        }

                        pNode = node.getProperty(prop);

                        jw.key(pNode.getName()).object();
                        jw.key("name").value(name);

                        if(pNode.getType() == 5){
                            jw.key("value").value(FORMATTER.format(pNode.getDate().getTime()));
                        }else if(pNode.isMultiple()){
                            Value[] values = pNode.getValues();
                            StringBuilder value = new StringBuilder();

                            for(Value v : values){
                                value.append(v.getString()).append(",");
                            }

                            value.deleteCharAt(value.lastIndexOf(","));
                            jw.key("value").value(value);
                        }else{
                            jw.key("value").value(pNode.getString());
                        }

                        jw.endObject();
                    }

                    jw.endObject();
                }catch(Exception e){
                    log.warn("Missing node : " + path, e);
                }
            }

            jw.endObject();
        }catch(Exception e){
            log.error("Error getting templates",e);
            throw new ServletException(e);
        }
    }
}
