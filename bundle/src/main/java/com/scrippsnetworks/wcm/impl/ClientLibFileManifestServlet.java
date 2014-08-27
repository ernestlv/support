package com.scrippsnetworks.wcm.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.util.TraversingItemVisitor;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.iterator.NodeIterable;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.OptingServlet;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.service.component.ComponentContext;

import com.day.cq.commons.Externalizer;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import com.day.jcr.vault.packaging.JcrPackage;
import com.day.jcr.vault.packaging.JcrPackageDefinition;
import com.day.jcr.vault.packaging.Packaging;

@SuppressWarnings("serial")
@SlingServlet(resourceTypes = "nt/file", selectors = "clientlibfiles", extensions = "txt")
public class ClientLibFileManifestServlet extends SlingSafeMethodsServlet implements OptingServlet {

    private class FileLister extends TraversingItemVisitor.Default {

        private final ResourceResolver resourceResolver;

        private final PrintWriter writer;

        public FileLister(final ResourceResolver resourceResolver, final PrintWriter writer) {
            this.resourceResolver = resourceResolver;
            this.writer = writer;
        }

        @Override
        protected void leaving(Node node, int level) throws RepositoryException {
            if (shouldWritePath(node)) {
                writer.println(createAbsolutePath(resourceResolver, node.getPath()));
            }
        }

        private boolean shouldWritePath(Node node) throws RepositoryException {
            final String name = node.getName();
            return node.isNodeType(JcrConstants.NT_FILE) &&
                    name.indexOf('.') > -1 &&
                    !name.equals(FN_CSS) &&
                    !name.equals(FN_JS) &&
                    !name.endsWith(EXT_JS) &&
                    !name.endsWith(EXT_CSS);
        }
    }

    private static final String DEFAULT_EXTERNALIZER_DOMAIN = Externalizer.PUBLISH;

    private static final String EXT_CSS = "css";

    private static final String EXT_JS = "js";

    private static final String FN_CSS = "css.txt";

    private static final String FN_JS = "js.txt";

    @Property(label = "Externalizer Domain", description = "The domain configured in the Externalizer used to generate absolute paths", value = DEFAULT_EXTERNALIZER_DOMAIN)
    private static final String PROP_EXTERNALIZER_DOMAIN = "domain";

    @Reference
    private Externalizer externalizer;

    private String externalizerDomain;

    @Reference
    private Packaging packaging;

    @Reference
    private QueryBuilder queryBuilder;

    @Override
    public boolean accepts(SlingHttpServletRequest request) {
        final Node node = request.getResource().adaptTo(Node.class);
        try {
            final JcrPackage jcrPackage = packaging.open(node, false);
            return jcrPackage != null;
        } catch (RepositoryException e) {
        }
        return false;
    }

    protected void activate(final ComponentContext ctx) {
        this.externalizerDomain = OsgiUtil.toString(ctx.getProperties().get(PROP_EXTERNALIZER_DOMAIN),
                DEFAULT_EXTERNALIZER_DOMAIN);
    }

    private Map<String, String> buildQueryMap(final String[] filterRoots) {
        final Map<String, String> queryMap = new HashMap<String, String>();
        queryMap.put("type", "cq:ClientLibraryFolder");
        queryMap.put("group.p.or", "true");
        for (int i = 0; i < filterRoots.length; i++) {
            queryMap.put(String.format("group.%s_path", i), filterRoots[i]);
        }
        return queryMap;
    }

    private String createAbsolutePath(final ResourceResolver resolver, final String path) {
        return externalizer.externalLink(resolver, externalizerDomain, path);
    }

    private String createAbsolutePathWithExtension(final ResourceResolver resolver, final String path,
            final String extension) {
        return externalizer.externalLink(resolver, externalizerDomain, String.format("%s.%s", path, extension));
    }

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException,
            IOException {
        final PrintWriter writer = response.getWriter();

        try {
            final Node node = request.getResource().adaptTo(Node.class);
            final JcrPackage jcrPackage = packaging.open(node, false);

            final FileLister lister = new FileLister(request.getResourceResolver(), writer);

            final String[] filterRoots = extractFilter(jcrPackage);
            final Map<String, String> queryMap = buildQueryMap(filterRoots);
            final Query query = queryBuilder.createQuery(PredicateGroup.create(queryMap), node.getSession());
            query.setHitsPerPage(0); // unlimited results
            final SearchResult result = query.getResult();

            for (final Iterator<Node> nodes = result.getNodes(); nodes.hasNext();) {
                final Node clientlib = nodes.next();
                final String path = clientlib.getPath();
                if (clientlib.hasNode(FN_CSS)) {
                    String extension = EXT_CSS;
                    writer.println(createAbsolutePathWithExtension(request.getResourceResolver(), path, extension));
                }
                if (clientlib.hasNode(FN_JS)) {
                    String extension = EXT_JS;
                    writer.println(createAbsolutePathWithExtension(request.getResourceResolver(), path, extension));
                }

                clientlib.accept(lister);

            }

        } catch (RepositoryException e) {
            throw new ServletException("Unable to produce client lib content manifest.", e);
        }
    }

    private String[] extractFilter(final JcrPackage jcrPackage) throws RepositoryException {
        final Node packageDefinitionNode = jcrPackage.getDefinition().getNode();
        final Node filter = packageDefinitionNode.getNode(JcrPackageDefinition.NN_FILTER);
        final Set<String> filterRoots = new HashSet<String>();
        for (final Node filterDef : new NodeIterable(filter.getNodes())) {
            if (filterDef.hasProperty(JcrPackageDefinition.PN_ROOT)) {
                filterRoots.add(filterDef.getProperty(JcrPackageDefinition.PN_ROOT).getString());
            }
        }
        return filterRoots.toArray(new String[0]);
    }

}
