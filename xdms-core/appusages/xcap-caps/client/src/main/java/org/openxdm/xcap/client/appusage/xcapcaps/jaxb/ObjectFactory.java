//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.1.5-b01-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.05.01 at 05:18:55 PM WEST 
//


package org.openxdm.xcap.client.appusage.xcapcaps.jaxb;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.openxdm.xcap.client.appusage.xcapcaps.jaxb package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.openxdm.xcap.client.appusage.xcapcaps.jaxb
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link XcapCaps.Namespaces }
     * 
     */
    public XcapCaps.Namespaces createXcapCapsNamespaces() {
        return new XcapCaps.Namespaces();
    }

    /**
     * Create an instance of {@link XcapCaps }
     * 
     */
    public XcapCaps createXcapCaps() {
        return new XcapCaps();
    }

    /**
     * Create an instance of {@link XcapCaps.Extensions }
     * 
     */
    public XcapCaps.Extensions createXcapCapsExtensions() {
        return new XcapCaps.Extensions();
    }

    /**
     * Create an instance of {@link XcapCaps.Auids }
     * 
     */
    public XcapCaps.Auids createXcapCapsAuids() {
        return new XcapCaps.Auids();
    }

}
