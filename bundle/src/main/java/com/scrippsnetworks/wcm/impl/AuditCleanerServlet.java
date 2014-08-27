package com.scrippsnetworks.wcm.impl;

import java.io.IOException;
import java.io.Writer;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrRecursiveRemove;

/**
 * Servlet that removes all nodes under /var/audit.
 */
@SuppressWarnings("serial")
@Component
@Service(Servlet.class)
@Property(name = "sling.servlet.paths", value = "/bin/cleanaudit")
public class AuditCleanerServlet extends SlingSafeMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(AuditCleanerServlet.class);
    
    private boolean active;
    
    @Activate
    protected void activate(ComponentContext ctx) {
        this.active = true;
    }
    
    @Deactivate
    protected void deactivate(ComponentContext ctx) {
        this.active = false;
    }

    @Override
    protected void doGet(SlingHttpServletRequest request,
            SlingHttpServletResponse response) throws ServletException,
            IOException {

        response.setContentType("text/plain");

        Writer w = response.getWriter();

        Session session = request.getResourceResolver().adaptTo(Session.class);
        try {
            Node auditHome = session.getNode("/var/audit");

            JcrRecursiveRemove removal = new JcrRecursiveRemove();

            final NodeIterator auditCategoryNodes = auditHome.getNodes();
            while (auditCategoryNodes.hasNext()) {
                final Node categoryNode = auditCategoryNodes.nextNode();
                final NodeIterator topLevelNodesWithinCategory = categoryNode
                        .getNodes();
                while (topLevelNodesWithinCategory.hasNext()) {
                    if (active) {
                        final Node topLevelNodeWithinCategory = topLevelNodesWithinCategory
                                .nextNode();
                        log(w, "Deleting " + topLevelNodeWithinCategory.getPath());
                        final int removed = removal.removeRecursive(
                                topLevelNodeWithinCategory, 0);
                        log(w, "" + removed + " removed");
                    } else {
                        log(w, "deactivated. stopping execution");
                        return;
                    }
                }
            }
        } catch (RepositoryException e) {
            throw new ServletException(e);
        }

    }

    private void log(Writer w, String string) throws IOException {
        log.info(string);
        w.write(string);
        w.write("\n");
    }

}
