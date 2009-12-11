//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.5-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.05.01 at 05:38:05 PM WEST 
//


package org.openxdm.xcap.client.appusage.presrules.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.w3c.dom.Element;


/**
 * <p>Java class for provideDevicePermission complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="provideDevicePermission">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="all-devices">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;sequence maxOccurs="unbounded" minOccurs="0">
 *           &lt;choice>
 *             &lt;element ref="{urn:ietf:params:xml:ns:pres-rules}deviceID"/>
 *             &lt;element ref="{urn:ietf:params:xml:ns:pres-rules}occurrence-id"/>
 *             &lt;element ref="{urn:ietf:params:xml:ns:pres-rules}class"/>
 *             &lt;any/>
 *           &lt;/choice>
 *         &lt;/sequence>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "provideDevicePermission", namespace = "urn:ietf:params:xml:ns:pres-rules", propOrder = {
    "allDevices",
    "deviceIDOrOccurrenceIdOrClazz"
})
@XmlRootElement(name = "provide-devices", namespace = "urn:ietf:params:xml:ns:pres-rules")
public class ProvideDevicePermission {

    @XmlElement(name = "all-devices")
    protected ProvideDevicePermission.AllDevices allDevices;
    @XmlElementRefs({
        @XmlElementRef(name = "class", namespace = "urn:ietf:params:xml:ns:pres-rules", type = JAXBElement.class),
        @XmlElementRef(name = "deviceID", namespace = "urn:ietf:params:xml:ns:pres-rules", type = JAXBElement.class),
        @XmlElementRef(name = "occurrence-id", namespace = "urn:ietf:params:xml:ns:pres-rules", type = JAXBElement.class)
    })
    @XmlAnyElement(lax = true)
    protected List<Object> deviceIDOrOccurrenceIdOrClazz;

    /**
     * Gets the value of the allDevices property.
     * 
     * @return
     *     possible object is
     *     {@link ProvideDevicePermission.AllDevices }
     *     
     */
    public ProvideDevicePermission.AllDevices getAllDevices() {
        return allDevices;
    }

    /**
     * Sets the value of the allDevices property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProvideDevicePermission.AllDevices }
     *     
     */
    public void setAllDevices(ProvideDevicePermission.AllDevices value) {
        this.allDevices = value;
    }

    /**
     * Gets the value of the deviceIDOrOccurrenceIdOrClazz property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the deviceIDOrOccurrenceIdOrClazz property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDeviceIDOrOccurrenceIdOrClazz().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link Element }
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * 
     * 
     */
    public List<Object> getDeviceIDOrOccurrenceIdOrClazz() {
        if (deviceIDOrOccurrenceIdOrClazz == null) {
            deviceIDOrOccurrenceIdOrClazz = new ArrayList<Object>();
        }
        return this.deviceIDOrOccurrenceIdOrClazz;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class AllDevices {


    }

}
