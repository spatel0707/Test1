//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.01.23 at 03:41:53 PM IST 
//


package com.izn.schemamodeler.admin.role;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.izn.schemamodeler.admin.xml.role package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.izn.schemamodeler.admin.xml.role
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RoleXMLSchema }
     * 
     */
    public RoleXMLSchema createComponent() {
        return new RoleXMLSchema();
    }

    /**
     * Create an instance of {@link RoleXMLSchema.Schema }
     * 
     */
    public RoleXMLSchema.Schema createComponentSchema() {
        return new RoleXMLSchema.Schema();
    }

    /**
     * Create an instance of {@link RoleXMLSchema.Schema.Field }
     * 
     */
    public RoleXMLSchema.Schema.Field createComponentSchemaField() {
        return new RoleXMLSchema.Schema.Field();
    }

    /**
     * Create an instance of {@link RoleXMLSchema.Schema.Field.Childs }
     * 
     */
    public RoleXMLSchema.Schema.Field.Childs createComponentSchemaFieldChilds() {
        return new RoleXMLSchema.Schema.Field.Childs();
    }

    /**
     * Create an instance of {@link RoleXMLSchema.Schema.Basic }
     * 
     */
    public RoleXMLSchema.Schema.Basic createComponentSchemaBasic() {
        return new RoleXMLSchema.Schema.Basic();
    }

    /**
     * Create an instance of {@link RoleXMLSchema.Schema.Field.Detail }
     * 
     */
    public RoleXMLSchema.Schema.Field.Detail createComponentSchemaFieldDetail() {
        return new RoleXMLSchema.Schema.Field.Detail();
    }

    /**
     * Create an instance of {@link RoleXMLSchema.Schema.Field.Childs.Child }
     * 
     */
    public RoleXMLSchema.Schema.Field.Childs.Child createComponentSchemaFieldChildsChild() {
        return new RoleXMLSchema.Schema.Field.Childs.Child();
    }

}