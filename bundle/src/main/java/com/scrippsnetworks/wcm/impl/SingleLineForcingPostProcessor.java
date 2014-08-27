
package com.scrippsnetworks.wcm.impl;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.servlets.post.Modification;
import org.apache.sling.servlets.post.SlingPostProcessor;

/**
 * PostProcessor which removes p and br tags from properties annotated with @SingleLine.
 * 
 * For example, posting to /content/foo/bar/jcr:content with
 * ./text = &lt;p&gt;hello&lt;/p&gt;
 * ./text@SingleLine = true
 * 
 * Will result in the text property set to hello
 * 
 * Updated: 5/14/2013 Rahul Anand (Added support for RTE in multified)
 */
@Component(metatype = false)
@Service
public class SingleLineForcingPostProcessor implements SlingPostProcessor {

    private static final String POSTFIX_SINGLE_LINE = "@SingleLine";
    
    @Override
    public void process(SlingHttpServletRequest request, List<Modification> changes) throws Exception {
        Set<String> singleLineParameters = getSingleLineParameters(request);
        if (singleLineParameters.isEmpty()) {
            return;
        }

        Session session = request.getResourceResolver().adaptTo(Session.class);

        for (final Modification mod : changes) {
            switch (mod.getType()) {
            case CREATE:
            case MODIFY:
                String source = mod.getSource();
                if (isSingleLine(singleLineParameters, source)) {
                	if(!session.getProperty(source).isMultiple()){
                		fixProperty(session.getProperty(source));
                	}else{
                		//RTE in the Multifield support added
                		Value[] values = session.getProperty(source).getValues();
                		String[] newValues = new String[values.length];
                		for(int i=0;i<values.length;i++){
                			Value value = values[i];
                			String propVal = fixProperty(value.getString());
                			newValues[i] = propVal;                			
                		}
                		session.getProperty(source).setValue(newValues);
                	}
                } else {
                    if (source.endsWith(POSTFIX_SINGLE_LINE) && session.propertyExists(source)) {
                        session.getProperty(source).remove();
                    }
                }
                break;
            default:
            }	
        }

    }

    private void fixProperty(Property property) throws RepositoryException {
        String propVal = property.getString();
        propVal = propVal.replaceAll("<p>", " ");
        propVal = propVal.replaceAll("</p>", " ");
        propVal = propVal.replaceAll("<p/>", " ");
        propVal = propVal.replaceAll("<br>", " ");
        propVal = propVal.replaceAll("<br/>", " ");
        propVal = propVal.replaceAll("\\s+", " ");
        propVal = propVal.trim();
        property.setValue(propVal);
    }

    private String fixProperty(String propVal) throws RepositoryException {
    	propVal = propVal.replaceAll("<p>", " ");
        propVal = propVal.replaceAll("</p>", " ");
        propVal = propVal.replaceAll("<p/>", " ");
        propVal = propVal.replaceAll("<br>", " ");
        propVal = propVal.replaceAll("<br/>", " ");
        propVal = propVal.replaceAll("\\s+", " ");
        propVal = propVal.trim();
        return propVal;
    }
    
    @SuppressWarnings("unchecked")
    private Set<String> getSingleLineParameters(SlingHttpServletRequest request) {
        Set<String> encryptedParams = new HashSet<String>();
        Enumeration<String> e = request.getParameterNames();
        while (e.hasMoreElements()) {
            String reqParam = e.nextElement();
            int hintIdx = reqParam.indexOf(POSTFIX_SINGLE_LINE);
            if (hintIdx > -1) {
                String paramName = reqParam.substring(0, hintIdx);
                String param = request.getParameter(paramName);
                if (param != null) {
                    encryptedParams.add(StringUtils.removeStart(paramName, "./"));
                }
            }
        }
        return encryptedParams;
    }

    private boolean isSingleLine(Set<String> singleLined, String source) {
        for (String param : singleLined) {
            if (source.endsWith(param)) {
                return true;
            }
        }
        return false;
    }
}
