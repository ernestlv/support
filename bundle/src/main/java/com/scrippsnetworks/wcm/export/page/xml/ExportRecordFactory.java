package com.scrippsnetworks.wcm.export.page.xml;

import com.scrippsnetworks.wcm.asset.DataUtil;
import com.scrippsnetworks.wcm.export.page.content.*;
import com.scrippsnetworks.wcm.asset.hub.Hub;

import com.day.cq.wcm.api.Page;

import com.scrippsnetworks.wcm.export.page.xml.bind.ObjectFactory;
import com.scrippsnetworks.wcm.export.page.xml.bind.PROPType;
import com.scrippsnetworks.wcm.export.page.xml.bind.RECORDS;
import com.scrippsnetworks.wcm.export.page.xml.bind.RECORDType;

import com.scrippsnetworks.wcm.util.PagePropertyNames;
import org.apache.sling.api.resource.ValueMap;

import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Jason Clark
 *         Date: 12/4/12
 */
public class ExportRecordFactory {

    private static final Logger log = LoggerFactory.getLogger(ExportRecordFactory.class);

    private static final String FORWARD_SLASH = "/";
    private static final String PIPE = "|";
    private static final String TRUE = "TRUE";

    private ExportRecordFactory() {}

    /**
     * Constructs an export XML record using JAXB
     * @param page Current Page object
     * @return RECORDS object
     */
    public static RECORDS createExportXml(final Page page) {
        try {
            ObjectFactory factory = new ObjectFactory();
            RECORDS records = factory.createRECORDSType();
            RECORDType record = factory.createRECORDType();

            //add common props to the record
            //TODO however, "common" assumes the VIEW of the data for export xml is the same as the
            //TODO the view for the HTML. This may be generally true, but is it?
            //TODO for example, sponsorship is not the same. I came upon this when trying to get
            //TODO export XML show the path of sponsorship, not the MetadataManager view of it
            //TODO which is all-caps and without the path info (ks)...
            record.prop.addAll(buildCommonProperties(factory, page));

            //add pagetype-specific props to the record
            record.prop.addAll(buildPageSpecificProperties(factory, page));

            records.setRECORD(record);

            return records;
        } catch (Exception e) {
            log.error("exception in XML Export Builder: " + e);
            return null;
        }
    }

    /**
     * Build a List of properties that are common to all page types
     * @param factory JAXB Object Factory
     * @param page Page object to retrieve properties from
     * @return List of PROPType objects
     */
    private static List<PROPType> buildCommonProperties(final ObjectFactory factory, final Page page) {
        try {
            MetaData pageData = new CommonData(page);
            Map<String, Object> pageProperties = pageData.getProperties();

            List<PROPType> commonProperties = new ArrayList<PROPType>();

            for (String key : pageProperties.keySet()) {
                CommonFieldMappings fieldMapping = CommonFieldMappings.getFieldMapping(key);
                if (fieldMapping == null) {
                    continue;
                }
                PROPType propType;
                if (fieldMapping.isStringType()) {
                    String value = pageProperties.get(fieldMapping.jcrProperty()).toString();
                    propType = createProp(factory, fieldMapping.xmlProperty(), value);
                } else if (fieldMapping.isStringArrayType()) {
                    String[] value = (String[]) pageProperties.get(fieldMapping.jcrProperty());
                    propType = createProp(factory, fieldMapping.xmlProperty(), value);
                } else if (fieldMapping.isCalendarType()) {
                    Calendar value = (Calendar) pageProperties.get(fieldMapping.jcrProperty());
                    propType = createProp(factory, fieldMapping.xmlProperty(), value);
                } else { //should never get here
                    log.error("Found unexpected CommonFieldMapping :: " + fieldMapping.name());
                    continue;
                }
                commonProperties.add(propType);
            }

            Hub pageHub = pageData.getHub();

            if (pageHub != null) {
                Page hubMaster = pageHub.getMasterPage();
                String hubMasterType = pageHub.getMasterPageType();
                List<Page> hubChildren = pageHub.getChildPages();
                if (hubMaster != null) {
                    ValueMap hubMasterProperties = hubMaster.getProperties();
                    StringBuilder builder = new StringBuilder();
                    if (StringUtils.isNotBlank(hubMasterType)) {
                        builder.append(hubMasterType);
                    }
                    builder.append(PIPE);
                    if (hubMasterProperties.containsKey(PagePropertyNames.SNI_ASSET_UID.propertyName())) {
                        String uid = hubMasterProperties
                                .get(PagePropertyNames.SNI_ASSET_UID.propertyName(), String.class);
                        builder.append(uid);
                    }
                    builder
                            .append(PIPE)
                            .append(hubMaster.getPath())
                            .append(PIPE)
                            .append(TRUE);
                    PROPType propType = createProp(factory, "CORE_HUBMASTER", builder.toString());
                    commonProperties.add(propType);
                }
                if (hubChildren != null && hubChildren.size() > 0) {
                    for (Page child : hubChildren) {
                        ValueMap hubChildProperties = child.getProperties();
                        StringBuilder builder = new StringBuilder();
                        String type = DataUtil.getPageType(child);
                        builder.append(type).append(PIPE);
                        if (hubChildProperties.containsKey(PagePropertyNames.SNI_ASSET_UID.propertyName())) {
                            String uid = hubChildProperties
                                    .get(PagePropertyNames.SNI_ASSET_UID.propertyName(), String.class);
                            builder.append(uid);
                        }
                        builder
                                .append(PIPE)
                                .append(child.getPath())
                                .append(PIPE)
                                .append(TRUE);
                        PROPType propType = createProp(factory, "CORE_HUBCHILD", builder.toString());
                        commonProperties.add(propType);
                    }
                }
            }

            return commonProperties;

        } catch (Exception e) {
            log.error("exception in XML Export Builder: " + e);
            return Collections.emptyList();
        }
    }

    /**
     *
     * @param factory
     * @param page
     * @return
     */
    public static List<PROPType> buildPageSpecificProperties(final ObjectFactory factory, final Page page) {
        return Collections.emptyList();
    }

    /**
     * Construct a PROPType object for export XML records
     * @param factory ObjectFactory used to create property instance
     * @param name name for property
     * @param val value of property
     * @return PROPType object full of data
     */
    private static PROPType createProp(final ObjectFactory factory, final String name, final String val) {
        if (factory != null && StringUtils.isNotBlank(name) && StringUtils.isNotBlank(val)) {
            String value;

            //specific handling for CORE_PAGE_TYPE
            if (name.equals(CommonFieldMappings.SLING_RESOURCE_TYPE.xmlProperty())) {
                value = DataUtil.getPageType(val);
            } else {
                value = val;
            }

            PROPType prop = factory.createPROPType();
            prop.setNAME(name);
            prop.pval.add(value);
            return prop;
        } else {
            return null;
        }
    }

    /**
     *
     * @param factory
     * @param name
     * @param val
     * @return
     */
    private static PROPType createProp(final ObjectFactory factory, final String name, final String[] val) {
        if (factory != null && StringUtils.isNotBlank(name) && val != null && val.length > 0) {
            PROPType prop = factory.createPROPType();
            prop.setNAME(name);
            for (String pval : val) {
                if (StringUtils.isNotBlank(pval)) {
                    prop.pval.add(pval);
                }
            }
            return prop;
        } else {
            return null;
        }
    }

    /**
     *
     * @param factory
     * @param name
     * @param val
     * @return
     */
    private static PROPType createProp(final ObjectFactory factory, final String name, final Calendar val) {
        if (factory != null && StringUtils.isNotBlank(name) && val != null) {
            PROPType prop = factory.createPROPType();
            prop.setNAME(name);
            String dateStamp = DataUtil.dateStampFromCalendar(val)
                    + DataUtil.SPACE + DataUtil.timeStampFromCalendar(val);
            prop.pval.add(dateStamp);
            return prop;
        } else {
            return null;
        }
    }
}
