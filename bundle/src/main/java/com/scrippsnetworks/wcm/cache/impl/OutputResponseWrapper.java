package com.scrippsnetworks.wcm.cache.impl;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.CharArrayWriter;
import java.io.PrintWriter;


public class OutputResponseWrapper extends HttpServletResponseWrapper {

    private CharArrayWriter output;

    public OutputResponseWrapper(HttpServletResponse response) {
        super(response);
        this.output = new CharArrayWriter();
    }

    public String toString() {
        return output.toString();
    }

    public PrintWriter getWriter() {
        return new PrintWriter(output);
    }

}
