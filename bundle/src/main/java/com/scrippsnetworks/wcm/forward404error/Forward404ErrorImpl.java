package com.scrippsnetworks.wcm.forward404error;

import com.scrippsnetworks.wcm.config.TemplateConfigService;
import com.scrippsnetworks.wcm.util.modalwindow.MobileModalPath;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.*;
import org.apache.felix.scr.annotations.Properties;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

@Component(label = "Forward 404 Error Service",
        description = "This service contains paths for forward to customized 404 error page",
        enabled = true,
        immediate = true,
        metatype = true)
@Service(value = Forward404Error.class)
public class Forward404ErrorImpl implements Forward404Error {
    private static Logger logger = LoggerFactory.getLogger(Forward404ErrorImpl.class);
    private Map<String, String> forwardRules = new HashMap<String, String>();

    @Property(label = "Forward rules", description = "Use next format for adding rules: \"/content/food/recipes=/content/food/recipes\", in this case all resources under /content/food/recipes will be redirected to /content/food/recipes.html",
            value = {"/content/food/recipes=/content/food/recipes"}, cardinality = 15)
    private static final String FORWARD_RULES;

    static {
        FORWARD_RULES = "forward404error.rules";
    }

    @Reference
    private TemplateConfigService config;

    @Activate
    protected void activate(ComponentContext ctx) {
        Dictionary props = ctx.getProperties();
        logger.debug("Activate: Forward 404 Error Service");
        updateCollection(props);
        logger.debug("Collection of forward rules filled");
    }

    /**
     * Deactivates this OSGi component, cleaning up any state.
     */
    @Deactivate
    protected void deactivate(ComponentContext ctx) {
        logger.debug("Deactivate: Forward 404 Error Service");
    }

    /**
     * Updates the state of this OSGi component when the ComponentContext has changed.
     */
    @Modified
    protected void modified(ComponentContext ctx) {
        Dictionary props = ctx.getProperties();
        logger.debug("Modified: Forward 404 Error Service");
        updateCollection(props);
        logger.debug("Collection of forward rules filled");
    }

    private void updateCollection(Dictionary props) {
        String[] rules = OsgiUtil.toStringArray(props.get(FORWARD_RULES));
        forwardRules = new HashMap<String, String>();
        Pattern p = Pattern.compile("=");
        for (String rule : rules) {
            String[] ruleArray = p.split(rule);
            if (ruleArray.length >= 2){
                forwardRules.put(ruleArray[0], ruleArray[1]);
            }
        }
    }

    @Override
    public String forwardPath(SlingHttpServletRequest slingRequest){
        RequestPathInfo pathInfo = slingRequest.getRequestPathInfo();
        String path = pathInfo.getResourcePath();

        for (Map.Entry<String, String> forwardRule : forwardRules.entrySet()) {
            String key = forwardRule.getKey();
            String value = forwardRule.getValue();

            if (!path.startsWith(key)){
                continue;
            }

            if (pathInfo.getSelectors() == null){
                return value + ".html";
            } else{
                List<String> pageSelectors = new ArrayList<String>(Arrays.asList(pathInfo.getSelectors()));
                String stringSelectors = "";
                if (pageSelectors.size() == 0){
                    stringSelectors = "";
                } else{
                    stringSelectors = StringUtils.join(pageSelectors, ".") + ".";
                }
                return value + "." + stringSelectors + "html";
            }
        }
        return null;
    }
}
