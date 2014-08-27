package com.scrippsnetworks.wcm.credentialstore.impl;

import com.scrippsnetworks.wcm.credentialstore.CredentialStoreService;
import com.scrippsnetworks.wcm.credentialstore.CredentialStore;
import com.scrippsnetworks.wcm.page.SniPage;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.api.SlingRepository;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * @author  Jonathan Bell
 *          Date: 09/19/2013
 * 
 * Bare-bones initial storage of external credentials.
 * A true implementation should store values securely.
 */
@Component(enabled=true, immediate=true)
@Service
public class CredentialStoreServiceImpl implements CredentialStoreService {

    private static final Logger log = LoggerFactory.getLogger(CredentialStoreServiceImpl.class);
    private static final String EMPTY_STRING = "";
    private static final String STORE_ROOT_PATH = "/apps/sni-";
    private static final String STORE_RELATIVE_PATH = "config/credentialstore";
    private static final String STORE_CONTENT_PATH = CredentialStore.class.getName();

    private HashMap credentials = new HashMap<String,String>();
    private String storeBrand;

    @Reference
    private ResourceResolverFactory resolverFactory;

    public void setCredentials() {
        credentials = getProperties();        
    }

    private PropertyIterator getStore() {
        PropertyIterator props = null;

        if (resolverFactory != null) {
            StringBuilder nodePath = new StringBuilder();
            nodePath
                .append(STORE_ROOT_PATH)
                .append(storeBrand)
                .append("/")
                .append(STORE_RELATIVE_PATH)
                .append("/")
                .append(STORE_CONTENT_PATH);
            try {
                ResourceResolver resolver = resolverFactory.getAdministrativeResourceResolver(null);
                if (resolver != null) {
                    Session session = resolver.adaptTo(Session.class);
                    if (session != null) {
                        Node node = session.getNode(nodePath.toString());
                        props = node.getProperties();
                    }
                }
            } catch (PathNotFoundException pnfe) {
                log.error("Credential store not found at {}", nodePath.toString());
            } catch (RepositoryException re) {
                log.error("RepositoryException caught: ", re);
            } catch (Exception e) {
                log.error("Exception caught: ", e);
            }
        } else {
            log.error("Cannot access credential store without a resolver factory.");
        }

        return props;
    }

    private HashMap getProperties() {
        PropertyIterator props = getStore();

        if (props != null) {
            while (props.hasNext()) {
                try {
                    Property prop = props.nextProperty();
                    credentials.put(prop.getName(), prop.getValue().getString());
                } catch (RepositoryException re) {
                    log.error("RepositoryException caught: ", re);
                }
            }
        } else {
            log.warn("Credential store contained no properties");
        }

        return credentials;
    }

    public String getCredential(String brand, String key) {
        String value = EMPTY_STRING;

        if (storeBrand == null && brand != null) {
            storeBrand = brand;
            setCredentials();
        }

        if (credentials != null && credentials.containsKey(key)) {
            value = credentials.get(key).toString();
        }

        return value;
    }
}

