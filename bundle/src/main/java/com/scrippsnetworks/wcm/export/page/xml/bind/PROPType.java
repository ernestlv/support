package com.scrippsnetworks.wcm.export.page.xml.bind;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.util.List;
import java.util.ArrayList;

/**
 * <p>Java class for PROPType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PROPType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PVAL" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *       &lt;attribute name="NAME" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Srini Johnson
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PROPType", propOrder = { "pval" })
public class PROPType {

    @XmlElement(name = "PVAL", required = true)
    //@XmlJavaTypeAdapter(value=CDATAAdapter.class)
    public List<String> pval = new ArrayList<String>();
    @XmlAttribute(name = "NAME")
    public String name;

    /**
     * Gets the value of the pval property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public List<String> getPVAL() {
        return pval;
    }

    /**
     * Sets the value of the pval property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPVAL(List<String> value) {
        this.pval = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNAME() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNAME(String value) {
        this.name = value;
    }

}
