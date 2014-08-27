package apps.scripps.authorext;

import com.day.cq.commons.JSONWriterUtil;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.core.contentfinder.Hit;
import com.day.cq.wcm.core.contentfinder.ViewHandler;
import com.day.cq.wcm.core.contentfinder.ViewQuery;
import com.day.cq.xss.ProtectionContext;
import com.day.cq.xss.XSSProtectionException;
import com.day.cq.xss.XSSProtectionService;
import com.day.text.Text;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.jackrabbit.commons.query.GQL;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static com.day.cq.commons.jcr.JcrConstants.JCR_PATH;

@SlingServlet(
        paths="/bin/scripps/authorext/pageresults",
        methods = "GET",
        metatype = false,
        label = "Author Extensions Page Results Servlet"
)
public class PageResultsViewHandler extends ViewHandler {
    private static final long serialVersionUID = 0L;
    private static Logger log = LoggerFactory.getLogger(PageResultsViewHandler.class);

    private static final String NT_CQ_PAGE = "cq:Page";
    private static final String NT_CQ_PAGE_CONTENT = "cq:PageContent";
    private static final String DEFAULT_START_PATH = "/content";
    private static final String LAST_MOD_REL_PATH = JCR_CONTENT + "/cq:lastModified";

    public static final String PAGE_PATH = "path";
    public static final String TYPE = "type";
    public static final String TEMPLATE = "template";

    /**
     * @scr.reference policy="static"
     */
    private XSSProtectionService xss;

    private String getDefaultIfEmpty(SlingHttpServletRequest request, String paramName, String dValue){
        String value = request.getParameter(paramName);

        if (StringUtils.isEmpty(value)) {
            value = dValue;
        }

        return value.trim();
    }

    @Override
    protected ViewQuery createQuery(SlingHttpServletRequest request, Session session, String queryString)
                                        throws RepositoryException {
        boolean isFullTextSearch = getDefaultIfEmpty(request,"fullText", "true").equalsIgnoreCase("true");
        String path = getDefaultIfEmpty(request, PAGE_PATH, DEFAULT_START_PATH);
        String template = getDefaultIfEmpty(request,TEMPLATE, "");

        //for numeric search string execute xpath, FNRHL-1024
        if( StringUtils.isNotEmpty(queryString) && NumberUtils.isNumber(queryString)){
            return new XPathQuery(request, xss, queryString, isFullTextSearch, path, template, session);
        }

        ParserCallback cb = new ParserCallback();

        String stmt = null;

        if(isFullTextSearch){
            stmt = queryString;
            GQL.parse(stmt, session, cb);
        }else{
            GQL.parse("", session, cb);
            cb.term("jcr:title", queryString, false);
        }

        StringBuilder gql = cb.getQuery();

        path = "path:\"" + path + "\"";

        if (StringUtils.isNotEmpty(gql)) {
            gql.append(" ");
        }

        gql.append(path).append(" ");

        String limit = getDefaultIfEmpty(request, LIMIT, "20");
        limit = "limit:" + limit;

        gql.append(limit).append(" ");

        if (isFullTextSearch && StringUtils.isEmpty(queryString) && StringUtils.isEmpty(template)) {
            return new MostRecentPages(request, session, gql, xss);
        }

        String type = getDefaultIfEmpty(request, TYPE, NT_CQ_PAGE);
        type = "type:\"" + type + "\"";
        gql.append(type).append(" ");

        if(StringUtils.isNotEmpty(template)){
            cb.term("sling:resourceType", template, false);
        }

        String order = "order:-" + LAST_MOD_REL_PATH;
        gql.append(order).append(" ");

        return new GQLViewQuery(request, gql.toString(), session, xss, template);
    }

    private static Hit createHit(Page page, String excerpt, XSSProtectionService xss)
            throws RepositoryException {
        Hit hit = new Hit();
        hit.set("name", page.getName());
        hit.set("path", page.getPath());
        hit.set("excerpt", excerpt);

        if(page.getTitle() != null) {
            hit.set("title", page.getTitle());

            if (xss != null) {
                try {
                    hit.set("title" + JSONWriterUtil.KEY_SUFFIX_XSS, xss.protectForContext(
                            ProtectionContext.PLAIN_HTML_CONTENT,page.getTitle()));
                } catch (XSSProtectionException e) {
                    log.warn("Unable to protect title {}", page.getTitle());
                }
            }
        } else {
            hit.set("title", page.getName());
        }

        if(page.getLastModified() != null) {
            hit.set("lastModified", page.getLastModified());
        }

        return hit;
    }

    private class ParserCallback implements GQL.ParserCallback {
        private StringBuilder query = new StringBuilder();

        public void term(String property, String value, boolean optional)
                throws RepositoryException {
            if(StringUtils.isEmpty(value)){
                return;
            }

            if (optional) {
                query.append("OR ");
            }

            if (StringUtils.isEmpty(property)) {
                query.append("\"jcr:content/.\":\"");
                query.append(value).append("\"");
            } else {
                property = "jcr:content/" + property;
                query.append("\"").append(property).append("\":");
                query.append("\"").append(value).append("\" ");
            }
        }

        public StringBuilder getQuery() {
            return query;
        }
    }

    private static class MostRecentPages implements ViewQuery {
        private final SlingHttpServletRequest request;
        private final Session session;
        private final String gql;
        private final XSSProtectionService xss;

        public MostRecentPages(SlingHttpServletRequest request, Session session, StringBuilder gql,
                               XSSProtectionService xss) {
            this.request = request;
            this.session = session;

            gql.append("type:\"").append(NT_CQ_PAGE_CONTENT).append("\" ");
            gql.append("order:-").append(NameConstants.PN_PAGE_LAST_MOD).append(" ");

            this.gql = gql.toString();
            this.xss = xss;
        }

        public Collection<Hit> execute() {
            List<Hit> hits = new ArrayList<Hit>();
            ResourceResolver resolver = request.getResourceResolver();
            RowIterator rows = GQL.execute(gql, session);

            try {
                while (rows.hasNext()) {
                    Row row = rows.nextRow();
                    String path = row.getValue(JCR_PATH).getString();
                    path = Text.getRelativeParent(path, 1);
                    Resource resource = resolver.getResource(path);

                    if (resource == null) {
                        continue;
                    }

                    Page page = resource.adaptTo(Page.class);
                    if (page == null) {
                        continue;
                    }

                    String excerpt;
                    try {
                        excerpt = row.getValue("rep:excerpt()").getString();
                    } catch (Exception e) {
                        excerpt = "";
                    }

                    hits.add(createHit(page, excerpt, xss));
                }
            } catch (RepositoryException re) {
                log.error("A repository error occurred", re);
            }

            return hits;
        }
    }

    private static class GQLViewQuery implements ViewQuery {
        private final SlingHttpServletRequest request;
        private final String queryStr;
        private final Session session;
        private final String template;
        private final XSSProtectionService xss;

        public GQLViewQuery(SlingHttpServletRequest request, String queryStr, Session session, XSSProtectionService xss, String template){
            this.request = request;
            this.queryStr = queryStr;
            this.session = session;
            this.xss = xss;
            this.template = template;
        }

        public Collection<Hit> execute() {
            List<Hit> hits = new ArrayList<Hit>();
            Property property = null;

            ResourceResolver resolver = request.getResourceResolver();
            RowIterator rows = GQL.execute(queryStr, session);

            try {
                while (rows.hasNext()) {
                    Row row = rows.nextRow();

                    if(row.getNode().hasProperty("jcr:content/sling:resourceType")){
                        property = row.getNode().getProperty("jcr:content/sling:resourceType");

                        //GQL always does a jcr:contains and never a straight property comparison, so request for
                        //page of template type /apps/sni-food/templates/pagetypes/video would return
                        //results of type /apps/sni-food/templates/pagetypes/video-channel as well
                        if(!StringUtils.isEmpty(template) && !property.getString().equals(template)){
                            continue;
                        }
                    }

                    String path = row.getValue(JCR_PATH).getString();
                    Page page = resolver.getResource(path).adaptTo(Page.class);

                    String excerpt;

                    try {
                        excerpt = row.getValue("rep:excerpt()").getString();
                    } catch (Exception e) {
                        excerpt = "";
                    }

                    hits.add(createHit(page, excerpt, xss));
                }
            } catch (RepositoryException re) {
                log.error("Error occurred during search", re);
            }
            return hits;
        }
    }

    private static class XPathQuery implements ViewQuery{
        private final String queryStr;
        private final Session session;
        private final boolean isFullTextSearch;
        private final String path;
        private final String template;
        private final SlingHttpServletRequest request;
        private final XSSProtectionService xss;

        public XPathQuery(SlingHttpServletRequest request, XSSProtectionService xss, String queryStr,
                          boolean isFullTextSearch, String path, String template, Session session){
            this.request = request;
            this.queryStr = queryStr;
            this.session = session;
            this.isFullTextSearch = isFullTextSearch;
            this.path = StringUtils.isEmpty(path) ? "/content/food" : path;
            this.template = StringUtils.isNotEmpty(template) ? "/apps/" + template.replace("/components/", "/templates/") : "";
            this.xss = xss;
        }

        public Collection<Hit> execute() {
            List<Hit> hits = new ArrayList<Hit>();

            try{
                QueryManager qm = session.getWorkspace().getQueryManager();
                StringBuilder xpath = new StringBuilder();

                xpath.append("/jcr:root").append(path).append("/")
                        .append("/element(*,").append(NT_CQ_PAGE).append(")");

                if(isFullTextSearch){
                    xpath.append("[jcr:contains(., '").append(queryStr).append("')");
                }else{
                    xpath.append("[jcr:like(fn:lower-case(jcr:content/@jcr:title), '").append(queryStr).append("')");
                }

                if(StringUtils.isNotEmpty(template)){
                    xpath.append(" and ((jcr:content/@cq:template = '").append(template).append("'))");
                }

                xpath.append("] order by jcr:content/@jcr:title");

                ResourceResolver resolver = request.getResourceResolver();
                QueryResult qResult = qm.createQuery(xpath.toString(), javax.jcr.query.Query.XPATH).execute();

                RowIterator rowIter = qResult.getRows();
                String pagePath = null, excerpt = null;

                while (rowIter.hasNext()) {
                    Row row = rowIter.nextRow();

                    pagePath = row.getValue(JCR_PATH).getString();
                    Page page = resolver.getResource(pagePath).adaptTo(Page.class);

                    try {
                        excerpt = row.getValue("rep:excerpt()").getString();
                    } catch (Exception e) {
                        excerpt = "";
                    }

                    hits.add(createHit(page, excerpt, xss));
                }
            }catch(Exception e){
                log.warn("Error occurred during xpath search", e);
            }

            return hits;
        }
    }
}
