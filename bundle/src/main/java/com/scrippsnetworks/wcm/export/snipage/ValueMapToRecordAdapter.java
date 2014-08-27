package com.scrippsnetworks.wcm.export.snipage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.sling.api.resource.ValueMap;

import com.scrippsnetworks.wcm.export.snipage.xml.bind.ObjectFactory;
import com.scrippsnetworks.wcm.export.snipage.xml.bind.PROP;
import com.scrippsnetworks.wcm.export.snipage.xml.bind.PVAL;
import com.scrippsnetworks.wcm.export.snipage.xml.bind.RECORD;
import com.scrippsnetworks.wcm.page.ExportConstants;

/** Adapts a value map to a RECORD.
 *
 * This is written as a JAXB XmlAdapter, though it need not be called by the JAXB implementation. Making it an
 * XmlAdapter makes it possible to use it in that fashion, but it can just as easily be called when hand-constructing
 * the bind classes.
 */
public class ValueMapToRecordAdapter extends XmlAdapter<RECORD, ValueMap> {

    // Date format for Calendar objects in a ValueMap.
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

    /**
     * @see XmlAdapter#marshal
     * @param valueMap a ValueMap of properties
     * @return a RECORD suitable for XML serialization
     */
    public RECORD marshal(ValueMap valueMap) {
        ObjectFactory factory = new ObjectFactory();
        RECORD record = factory.createRECORD();
        for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
            Object o = entry.getValue();
            if (o != null) {
                PROP prop = factory.createPROP();
                prop.setNAME(entry.getKey());
                PVAL pval = factory.createPVAL();
                if (o instanceof String) {
                	pval.setValue((String)o);
                	prop.getPVAL().add(pval);
                } else if (o instanceof String[]) {
                    String[] strArray = (String[])o;
                    for (String val : strArray) {
                        if (val != null) {
                        	pval = factory.createPVAL();
                        	pval.setValue(val);
                        	prop.getPVAL().add(pval);
                        }
                    }
                } else if (o instanceof Calendar) {
                    Calendar cal = (Calendar)o;
                    String val = dateFormat.format(cal.getTime());
                    pval.setValue(val);
                    prop.getPVAL().add(pval);
                } else if (o instanceof Integer) {
                	pval.setValue(String.valueOf(o));
                	prop.getPVAL().add(pval);
                } else if (o instanceof Integer[]) {
                    Integer[] intArray = (Integer[])o;
                    for (Integer val : intArray) {
                        if (val != null) {
                        	pval.setValue(String.valueOf(val));
                        	prop.getPVAL().add(pval);
                        }
                    }
                } else if (o instanceof Boolean) {
                	pval.setValue(String.valueOf(o).toUpperCase());
                	prop.getPVAL().add(pval);
                } else if(o instanceof List) {
                	List list = (List)o;
                	for(int i = 0 ; i < list.size(); i++) {
                		Object obj = list.get(i);
                		if(obj != null && obj instanceof Map) {
                			pval = factory.createPVAL();
                        	Map<String, Object> pvalMap = (Map<String, Object>) obj;
                        	if(pvalMap.containsKey(ExportConstants.PAGETYPE)) {
                        		pval.setPAGETYPE(String.valueOf(pvalMap.get(ExportConstants.PAGETYPE)));
                        	}
                        	if(pvalMap.containsKey(ExportConstants.PATH)) {
                        		pval.setPATH(String.valueOf(pvalMap.get(ExportConstants.PATH)));
                        	}
                        	if(pvalMap.containsKey(ExportConstants.CONTENT)) {
                        		pval.setValue(String.valueOf(pvalMap.get(ExportConstants.CONTENT)));
                        	}
                        	if(pvalMap.containsKey(ExportConstants.WIDTH)) {
                        		pval.setWIDTH(String.valueOf(pvalMap.get(ExportConstants.WIDTH)));
                        	}
                        	if(pvalMap.containsKey(ExportConstants.HEIGHT)) {
                        		pval.setHEIGHT(String.valueOf(pvalMap.get(ExportConstants.HEIGHT)));
                        	}
                        	if(pvalMap.containsKey(ExportConstants.USE)) {
                        		pval.setUSE(String.valueOf(pvalMap.get(ExportConstants.USE)));
                        	}
                            prop.getPVAL().add(pval);
                		}
                	}
                }
                
                record.getPROP().add(prop);
            }
        }
        return record;
    }

    /**
     * @see XmlAdapter#unmarshal
     * @param record a RECORD instance to unmarshal
     * @return a ValueMap of properties
     */
    public ValueMap unmarshal(RECORD record) {
        throw new UnsupportedOperationException("cannot currently unmarshal a RECORD");
    }

}
