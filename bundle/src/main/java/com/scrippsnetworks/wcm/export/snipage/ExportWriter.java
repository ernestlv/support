package com.scrippsnetworks.wcm.export.snipage;

import java.io.Writer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;


import com.scrippsnetworks.wcm.page.SniPage;
import com.scrippsnetworks.wcm.export.snipage.content.PageExportFactory;
import com.scrippsnetworks.wcm.export.snipage.content.PageExport;
import com.scrippsnetworks.wcm.export.snipage.xml.bind.ObjectFactory;
import com.scrippsnetworks.wcm.export.snipage.xml.bind.*;

public class ExportWriter {

    public static void writeExportXml(SniPage sniPage, Writer writer) throws PageExportException {

        if (sniPage == null || !sniPage.hasContent() || writer == null) {
            return;
        }

        try {
            PageExport pageExport = PageExportFactory.createPageExport(sniPage);
            if (pageExport != null) {
            ValueMapToRecordAdapter adapter = new ValueMapToRecordAdapter();
            RECORDS records = (new ObjectFactory()).createRECORDS();
            records.setRECORD(adapter.marshal(pageExport.getValueMap()));
            JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(records, writer);
            }
        } catch (Exception e) {
            throw new PageExportException(e);
        }
    }
}
