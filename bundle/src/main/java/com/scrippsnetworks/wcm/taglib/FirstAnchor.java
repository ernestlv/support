package com.scrippsnetworks.wcm.taglib;

/*
 * @author jason clark
 *
 * 03.28.12
 *
 * this tag accepts a parameter named "text", then hunts around in that text
 * for an anchor. if an anchor is found, grab it and wrap the text within the
 * body of this tag with the found anchor.
 *
 */

import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;

public class FirstAnchor extends BodyTagSupport {

	private static final long serialVersionUID = 1L;
    
	private String mText = "";
	
	public void setText(String pText) {
		mText = pText;
	}
    
    public int doAfterBody() throws JspException {
        try {
            BodyContent bc = getBodyContent();
            String body = bc.getString();
            JspWriter out = bc.getEnclosingWriter();
			String titleAnchor = "";
            if (body != null && mText != null && !mText.equals("")) {
                try {
                    int pos = mText.indexOf("<a");
                    titleAnchor = mText.substring(pos, mText.indexOf('>', pos) + 1);
                } catch (StringIndexOutOfBoundsException e) {}
               	out.write(titleAnchor + body + (titleAnchor.equals("") ? "" : "</a>") );
            } else if (body != null && !body.equals("")) {
                out.write(body);
            } else {
            	return SKIP_BODY;
            }
        } catch (IOException ioe) {
            throw new JspException( ioe.getMessage() );
        }
        return SKIP_BODY;
    }
}