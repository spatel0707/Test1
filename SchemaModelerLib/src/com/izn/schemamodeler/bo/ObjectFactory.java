//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.02.20 at 10:36:54 AM IST 
//


package com.izn.schemamodeler.bo;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.jaxb.output package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.jabx.output
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link BusinessObjectXMLSchema }
     * 
     */
    public BusinessObjectXMLSchema createBusinessObjectXMLSchema() {
        return new BusinessObjectXMLSchema();
    }

    /**
     * Create an instance of {@link BusinessObjectXMLSchema.Object }
     * 
     */
    public BusinessObjectXMLSchema.Object createBusinessObjectXMLSchemaObject() {
        return new BusinessObjectXMLSchema.Object();
    }

   

    /**
     * Create an instance of {@link BusinessObjectXMLSchema.Object.AttributeInfo }
     * 
     */
    public BusinessObjectXMLSchema.Object.AttributeInfo createBusinessObjectXMLSchemaObjectAttributeInfo() {
        return new BusinessObjectXMLSchema.Object.AttributeInfo();
    }

    /**
     * Create an instance of {@link BusinessObjectXMLSchema.Object.Basic }
     * 
     */
    public BusinessObjectXMLSchema.Object.Basic createBusinessObjectXMLSchemaObjectBasic() {
        return new BusinessObjectXMLSchema.Object.Basic();
    }

    /**
     * Create an instance of {@link BusinessObjectXMLSchema.Object.Basic.Field }
     * 
     */
    public BusinessObjectXMLSchema.Object.Basic.Field createBusinessObjectXMLSchemaObjectBasicField() {
        return new BusinessObjectXMLSchema.Object.Basic.Field();
    }

    /**
     * Create an instance of {@link BusinessObjectXMLSchema.Object.AttributeInfo.AttrDetail }
     * 
     */
    public BusinessObjectXMLSchema.Object.AttributeInfo.AttrDetail createBusinessObjectXMLSchemaObjectAttributeInfoAttrdetail() {
        return new BusinessObjectXMLSchema.Object.AttributeInfo.AttrDetail();
    }

    /**
     * Create an instance of {@link BusinessObjectXMLSchema.Object.Basic.Field.Detail }
     * 
     */
    public BusinessObjectXMLSchema.Object.Basic.Field.Detail createBusinessObjectXMLSchemaObjectBasicFieldDetail() {
        return new BusinessObjectXMLSchema.Object.Basic.Field.Detail();
    }

}