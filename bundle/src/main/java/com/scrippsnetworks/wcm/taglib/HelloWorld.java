package com.scrippsnetworks.wcm.taglib;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public class HelloWorld extends TagSupport {

	private static final long serialVersionUID = 1L;

	public int doStartTag() throws JspException {
		try {
			JspWriter out = pageContext.getOut();
			out.write("Hello World!");
		}
		catch (IOException ioe) {
			throw new JspException("Error: " + ioe.getMessage());
		}
		return SKIP_BODY;
	}
	
	public int doEndTag() throws JspException {
		return SKIP_BODY;
	}
}
