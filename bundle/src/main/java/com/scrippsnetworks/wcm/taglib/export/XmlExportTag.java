package com.scrippsnetworks.wcm.taglib.export;

import com.scrippsnetworks.wcm.export.page.xml.*;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.scrippsnetworks.wcm.export.page.xml.bind.RECORDS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.Page;

/**
 * Entry point for the XML data exporting system, also contains
 * JAXB Marshalling logic for writing XML to JSP output stream.
 * @author Jason Clark
 *         Date: 12/11/12
 */
public class XmlExportTag extends TagSupport {

    private static final Logger log = LoggerFactory.getLogger(XmlExportTag.class);

    private Page currentPage;

    public int doStartTag() throws JspException {
        if (currentPage == null) {
            log.error("XML Export: currentPage was null in doStartTag()");
            return SKIP_BODY;
        }
        try {
            JspWriter out = pageContext.getOut();
            JAXBContext context = JAXBContext.newInstance(RECORDS.class);
            RECORDS record = ExportRecordFactory.createExportXml(currentPage);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.marshal(record, out);
        } catch (JAXBException e) {
            log.error("JAXBException caught " + e.getMessage());
            throw new JspException(e);
        } catch (Exception e) {
            log.error("XML Export: " + e.getMessage());
            throw new JspException(e);
        }
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        return SKIP_BODY;
    }

    public void setCurrentPage(final Page page) {
        this.currentPage = page;
    }
}
