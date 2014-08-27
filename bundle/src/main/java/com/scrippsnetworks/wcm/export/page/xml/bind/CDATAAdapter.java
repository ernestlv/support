package com.scrippsnetworks.wcm.export.page.xml.bind;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Jason Clark
 *         Date: 2/4/13
 */
public class CDATAAdapter extends XmlAdapter<String, String> {

    @Override
    public String marshal(String v) throws Exception {
        return "<![CDATA[" + v + "]]>";
    }

    @Override
    public String unmarshal(String v) throws Exception {
        return v;
    }
}