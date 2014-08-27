package com.scrippsnetworks.wcm.export.page.xml.bind;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RECORDType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RECORDType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PROP" type="{}PROPType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 * @author Srini Johnson
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RECORDType", propOrder = { "prop" })
public class RECORDType {

    @XmlElement(name = "PROP")
    public List<PROPType> prop = new ArrayList<PROPType>();

    /**
     * Gets the value of the prop property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the prop property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPROP().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PROPType }
     * 
     * 
     */
    public List<PROPType> getPROP() {
        if (prop == null) {
            prop = new ArrayList<PROPType>();
        }
        return this.prop;
    }

}
