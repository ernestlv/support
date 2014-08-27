package com.scrippsnetworks.wcm.taglib;

/**
 * @author jason clark
 * Date: 5/12/12
 * Time: 8:10 PM
 *
 * This tag accepts a sling resource and retrieves the defaultRegion property,
 * which is used to write a div around the body contents with the property's value
 * as a CSS id.  This is used for formatting modules in the module-editor. Also
 * accepts optional attribute cssClass which will create a class attribute in the div.
 */

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.lang.StringBuilder;

public class ModuleEditorRegionsTag extends TagSupport {

    private static final long serialVersionUID = 1L;

    private Resource resource; //resource whose properties are read
    private String cssClass = ""; //additional css values can be added as classes
    private String propertyName = "defaultRegion"; //may want to override this
    private String[] regionIds;

    public int doStartTag() throws JspException {
        try {
            JspWriter out = pageContext.getOut();
            StringBuilder output = new StringBuilder();
            if (resource != null) {
                regionIds = ResourceUtil.getValueMap(resource).get(propertyName, new String[0]);
                for (String regionId : regionIds) {
                    output.append("<div");
                    if (regionId != null && regionId.length() > 0) {
                        output.append(" id='");
                        output.append(regionId);
                        output.append("'");
                    }
                    if (cssClass.length() > 0) {
                        output.append(" class='");
                        output.append(cssClass);
                        output.append("'");
                    }
                    output.append(">");
                }
            }
            out.print(output);
        } catch (IOException ioe) {
            throw new JspException(ioe.getMessage());
        }
        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() throws JspException {
        try {
            JspWriter out = pageContext.getOut();
            StringBuilder output = new StringBuilder();
            if (regionIds != null) {
                for (String regionId : regionIds) {
                    output.append("</div>");
                }
            }
            out.print(output);

        } catch (IOException ioe) {
            throw new JspException(ioe.getMessage());
        }
        return SKIP_BODY;
    }

    public void setResource(Resource pResource) {
        resource = pResource;
    }

    public void setCssClass(String pCssClass) {
        cssClass = pCssClass;
    }

    public void setPropertyName(String pPropertyName) {
        propertyName = pPropertyName;
    }
}
