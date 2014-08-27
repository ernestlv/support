package com.scrippsnetworks.wcm.export.page.xml.bind;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the generated package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 

 * @author Srini Johnson
 */

@XmlRegistry
public class ObjectFactory {

    private final static QName _RECORDS_QNAME = new QName("", "RECORDS");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: generated
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link PROPType }
     * 
     */
    public PROPType createPROPType() {
        return new PROPType();
    }

    /**
     * Create an instance of {@link RECORDS }
     * 
     */
    public RECORDS createRECORDSType() {
        return new RECORDS();
    }

    /**
     * Create an instance of {@link RECORDType }
     * 
     */
    public RECORDType createRECORDType() {
        return new RECORDType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RECORDS }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "RECORDS")
    public JAXBElement<RECORDS> createRECORDS(RECORDS value) {
        return new JAXBElement<RECORDS>(_RECORDS_QNAME, RECORDS.class, null, value);
    }

}
