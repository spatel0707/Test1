//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.01.29 at 11:21:44 AM IST 
//


package com.izn.schemamodeler.system.index;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.izn.schemamodeler.system.index package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.izn.schemamodeler.system.index
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Component }
     * 
     */
    public IndexXMLSchema createComponent() {
        return new IndexXMLSchema();
    }

    /**
     * Create an instance of {@link Component.Object }
     * 
     */
    public IndexXMLSchema.Object createComponentObject() {
        return new IndexXMLSchema.Object();
    }

    /**
     * Create an instance of {@link Component.Object.Field }
     * 
     */
    public IndexXMLSchema.Object.Field createComponentObjectField() {
        return new IndexXMLSchema.Object.Field();
    }

    /**
     * Create an instance of {@link Component.Object.Basic }
     * 
     */
    public IndexXMLSchema.Object.Basic createComponentObjectBasic() {
        return new IndexXMLSchema.Object.Basic();
    }

    /**
     * Create an instance of {@link Component.Object.Field.Detail }
     * 
     */
    public IndexXMLSchema.Object.Field.Detail createComponentObjectFieldDetail() {
        return new IndexXMLSchema.Object.Field.Detail();
    }

}
