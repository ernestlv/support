package com.scrippsnetworks.wcm.form;

import com.adobe.granite.xss.XSSAPI;
import com.day.cq.wcm.api.WCMMode;
import com.day.cq.wcm.foundation.forms.FieldHelper;
import com.day.cq.wcm.foundation.forms.FormsConstants;
import com.day.cq.wcm.foundation.forms.FormsHelper;
import com.day.cq.widget.HtmlLibraryManager;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.scripting.SlingBindings;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import static com.day.cq.wcm.foundation.forms.FormsHelper.*;

public class CustomFormsHelper {
    public static void startForm(final SlingHttpServletRequest request,
                                 final SlingHttpServletResponse response)
            throws IOException, ServletException {
        // get resource and properties
        final Resource formResource = request.getResource();
        initialize(request, formResource, response);

        writeJavaScript(request, response, formResource);

        final ValueMap properties = ResourceUtil.getValueMap(formResource);
        String formId = properties.get("id", "");
        if(StringUtils.isEmpty(formId))
            formId = getFormId(request);
        // write form element, we post to the same url we came from
        final PrintWriter out = response.getWriter();
        String url = request.getRequestURI();

        final String suffix = getActionSuffix(request);
        if (StringUtils.isNotBlank(suffix)) {
            url += (suffix.startsWith("/")) ? suffix : "/" + suffix;
        }

        SlingBindings bindings = (SlingBindings) request.getAttribute(SlingBindings.class.getName());
        XSSAPI xssAPI = bindings.getSling().getService(XSSAPI.class).getRequestSpecificAPI(request);
        String cssClass = properties.get("css", "cq-form");
        
        String formTypeClass = properties.get("typeFormClass", "");
        out.print("<form method=\"POST\" action=\"");
        String action = properties.get("formAction", "");
        if (StringUtils.isEmpty(action)){
            out.print(url);
        }   else{
            out.print(action);
        }
        out.print("\" id=\"");
        out.print(xssAPI.encodeForHTMLAttr(formId));
        out.print("\" name=\"");
        out.print(xssAPI.encodeForHTMLAttr(formId));
        if(!StringUtils.isEmpty(cssClass)){
            out.print("\" class=\"");
            out.print(xssAPI.encodeForHTMLAttr(cssClass));
            if(StringUtils.isNotBlank(formTypeClass)){
            	 out.print(xssAPI.encodeForHTMLAttr(" "+formTypeClass));
            }
           
        }
        out.print("\" enctype=\"multipart/form-data\">");

        // write form id as hidden field
        out.print("<input type=\"hidden\" name=\"");
        out.print(FormsConstants.REQUEST_PROPERTY_FORMID);
        out.print("\" value=\"");
        out.print(xssAPI.encodeForHTMLAttr(formId));
        out.print("\"/>");
        // write form start as hidden field
        out.print("<input type=\"hidden\" name=\"");
        out.print(FormsConstants.REQUEST_PROPERTY_FORM_START);
        out.print("\" value=\"");
        if ( formResource.getPath().startsWith(url) ) {
            // relative
            out.print(formResource.getPath().substring(url.length() + 1));
        } else {
            // absolute
            out.print(formResource.getPath());
        }
        out.print("\"/>");
        // write charset as hidden field
        out.print("<input type=\"hidden\" name=\"_charset_\" value=\"UTF-8\"/>");

        // check for redirect configuration
        String redirect = properties.get("redirect", "");
        if ( redirect.length() > 0 ) {
            redirect = request.getResourceResolver().map(request, redirect);
            final int lastSlash = redirect.lastIndexOf('/');
            if ( redirect.indexOf('.', lastSlash) == -1 ) {
                redirect = redirect + ".html";
            }
            out.print("<input type=\"hidden\" name=\"" + FormsConstants.REQUEST_PROPERTY_REDIRECT + "\" value=\"");
            out.print(xssAPI.encodeForHTMLAttr(redirect));
            out.print("\"/>");
        }

        // allow action to add form fields
        final String actionType = properties.get(FormsConstants.START_PROPERTY_ACTION_TYPE, "");
        if (actionType.length() > 0) {
            runAction(actionType, "addfields", formResource, request, response);
        }
    }

    private static void initialize(final SlingHttpServletRequest request,
                                   final Resource formResource,
                                   final SlingHttpServletResponse response)
            throws IOException, ServletException {
        final ValueMap properties = ResourceUtil.getValueMap(formResource);

        // set some "variables"
        final Boolean clientValidation = properties.get(FormsConstants.START_PROPERTY_CLIENT_VALIDATION, Boolean.FALSE);
        request.setAttribute(REQ_ATTR_CLIENT_VALIDATION, clientValidation);
        request.setAttribute(REQ_ATTR_FORMID, properties.get(FormsConstants.START_PROPERTY_FORMID, "new_form"));

        request.setAttribute(REQ_ATTR_IS_INIT, "true");

        // now invoke init script for action (if response is set)
        if (response != null) {
            final String actionType = properties.get(FormsConstants.START_PROPERTY_ACTION_TYPE, FormsConstants.DEFAULT_ACTION_TYPE);
            if (actionType.length() != 0) {
                runAction(actionType, "init", formResource, request, response);
            }
        }

        // check for load path
        if ( request.getAttribute(REQ_ATTR_GLOBAL_LOAD_MAP) == null ) {
            Resource loadResource = null;
            final String loadPath = properties.get(FormsConstants.START_PROPERTY_LOAD_PATH, "");
            if ( loadPath.length() > 0 ) {
                loadResource = formResource.getResourceResolver().getResource(loadPath);
            }
            FormsHelper.setFormLoadResource(request, loadResource);
        }

    }

    private static void writeJavaScript(final SlingHttpServletRequest req,
                                        final SlingHttpServletResponse response,
                                        final Resource formResource)
            throws IOException, ServletException {
        if ( doClientValidation(req) ) {
            final PrintWriter out = response.getWriter();

            // write general java script
            BundleContext bundleContext = FrameworkUtil.getBundle(CustomFormsHelper.class).getBundleContext();
            ServiceReference htmlLibraryManagerRef = bundleContext.getServiceReference(HtmlLibraryManager.class.getName());
            final HtmlLibraryManager htmlMgr = (HtmlLibraryManager)bundleContext.getService(htmlLibraryManagerRef);
            if (htmlMgr != null) {
                htmlMgr.writeJsInclude(req, out, "cq.forms");
            }

            // write form-specific java script
            out.println("<script type=\"text/javascript\">");
            if (WCMMode.fromRequest(req) != WCMMode.DISABLED) {
                // A page written in one mode can be switched to another mode without a refresh, so make
                // sure we have the reload flag for all of them.  On the other hand, if WCMMode is disabled
                // entirely (ie: on a publish instance), then we can't switch and don't want the flag.
                out.println("  cq5forms_reloadForPreview = true;");
            }
            out.println("  function " + getFormsPreCheckMethodName(req) + "(submitid) {");
            out.println("    var dMsgs = \"Please fill out the required field.\";");
            final Iterator<Resource> iter = getFormElements(formResource);
            while ( iter.hasNext() ) {
                final Resource formsField = iter.next();
                FieldHelper.initializeField(req, response, formsField);
                FormsHelper.includeResource(req, response, formsField, FormsConstants.SCRIPT_CLIENT_VALIDATION);
            }
            // in addition we check for a global validation RT
            // configured at the form resource
            final ValueMap properties = ResourceUtil.getValueMap(formResource);
            final String valScriptRT = properties.get(FormsConstants.START_PROPERTY_VALIDATION_RT, formResource.getResourceType());
            if ( valScriptRT != null && valScriptRT.length() > 0 ) {
                Resource valScriptResource = formResource;
                if ( !formResource.getResourceType().equals(valScriptRT) ) {
                    valScriptResource = new org.apache.sling.api.resource.ResourceWrapper(formResource) {
                        @Override
                        public String getResourceType() {
                            return valScriptRT;
                        }

                        @Override
                        public String getResourceSuperType() {
                            return formResource.getResourceType();
                        }

                    };
                }
                FormsHelper.includeResource(req, response, valScriptResource, FormsConstants.SCRIPT_FORM_CLIENT_VALIDATION);
            }
            out.println("    return true;");
            out.println("  }");
            out.println("</script>");
        }
    }
}
