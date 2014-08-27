package com.scrippsnetworks.wcm.taglib;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import java.util.Arrays;

import org.apache.sling.scripting.jsp.taglib.IncludeTagHandler;
import org.apache.sling.api.resource.ResourceNotFoundException;
import com.day.cq.wcm.api.WCMMode;
import com.day.cq.wcm.api.components.IncludeOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SafeSlingIncludeTag extends IncludeTagHandler
{
    private static final long serialVersionUID = 1L;

    private WCMMode oldWcmMode;
    private boolean forceSameContext = false;
    private boolean disableWcmMode = true;
    private String decorationTagName;
    private String cssClassNames;
    private IncludeOptions incOpts;

    Logger log = LoggerFactory.getLogger(SafeSlingIncludeTag.class);

    protected void dispatch(RequestDispatcher dispatcher, ServletRequest request, ServletResponse response)
        throws IOException, ServletException
    {
        if (disableWcmMode) {
            oldWcmMode = WCMMode.fromRequest(request);
            WCMMode.DISABLED.toRequest(request);
        }

        if (forceSameContext || decorationTagName != null || cssClassNames != null) {
            incOpts = IncludeOptions.getOptions(request, true);

            if (forceSameContext) {
                incOpts.forceSameContext(forceSameContext);
            } else {
                if (decorationTagName != null) {
                    incOpts.setDecorationTagName(decorationTagName);
                }
                if (cssClassNames != null) {
                    incOpts.getCssClassNames().addAll(Arrays.asList(cssClassNames.trim().split("\\s+")));
                }
            }
        }

        try {
            super.dispatch(dispatcher, request, response);
        } catch (ResourceNotFoundException e) {
            if (incOpts != null) {
                incOpts.clear(request);
            }
        } finally {
            if (oldWcmMode != null) {
                oldWcmMode.toRequest(request);
            }
        }
    }

    public void setDisableWcmMode(boolean pDisableWcmMode) {
        disableWcmMode = pDisableWcmMode;
    }

    public void setForceSameContext(boolean pForceSameContext) {
        forceSameContext = pForceSameContext;
    }

    public void setDecorationTagName(String pDecorationTagName) {
        decorationTagName = pDecorationTagName;
    }

    public void setCssClassNames(String pCssClassNames) {
        cssClassNames = pCssClassNames;
    }
}
