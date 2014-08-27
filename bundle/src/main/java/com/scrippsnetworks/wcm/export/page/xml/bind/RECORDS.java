package com.scrippsnetworks.wcm.export.page.xml.bind;

import javax.xml.bind.annotation.*;


/**
 * <p>Java class for RECORDSType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RECORDSType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RECORD" type="{}RECORDType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 *  @author Srini Johnson
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RECORDS", propOrder = {
    "record"
})
public class RECORDS {

    @XmlElement(name = "RECORD", required = true)
    protected RECORDType record;

    /**
     * Gets the value of the record property.
     * 
     * @return
     *     possible object is
     *     {@link RECORDType }
     *     
     */
    public RECORDType getRECORD() {
        return record;
    }

    /**
     * Sets the value of the record property.
     * 
     * @param value
     *     allowed object is
     *     {@link RECORDType }
     *     
     */
    public void setRECORD(RECORDType value) {
        this.record = value;
    }

}
