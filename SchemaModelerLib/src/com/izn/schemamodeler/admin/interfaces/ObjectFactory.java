//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.01.23 at 03:43:57 PM IST 
//


package com.izn.schemamodeler.admin.interfaces;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.izn.schemamodeler.admin.xml.interfaces package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.izn.schemamodeler.admin.xml.interfaces
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link InterfaceXMLSchema }
     * 
     */
    public InterfaceXMLSchema createComponent() {
        return new InterfaceXMLSchema();
    }

    /**
     * Create an instance of {@link InterfaceXMLSchema.Schema }
     * 
     */
    public InterfaceXMLSchema.Schema createComponentSchema() {
        return new InterfaceXMLSchema.Schema();
    }

    /**
     * Create an instance of {@link InterfaceXMLSchema.Schema.Field }
     * 
     */
    public InterfaceXMLSchema.Schema.Field createComponentSchemaField() {
        return new InterfaceXMLSchema.Schema.Field();
    }

    /**
     * Create an instance of {@link InterfaceXMLSchema.Schema.Basic }
     * 
     */
    public InterfaceXMLSchema.Schema.Basic createComponentSchemaBasic() {
        return new InterfaceXMLSchema.Schema.Basic();
    }

    /**
     * Create an instance of {@link InterfaceXMLSchema.Schema.Field.Detail }
     * 
     */
    public InterfaceXMLSchema.Schema.Field.Detail createComponentSchemaFieldDetail() {
        return new InterfaceXMLSchema.Schema.Field.Detail();
    }

}
