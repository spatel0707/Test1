package com.izn.schemamodeler.admin.inquiry;


import javax.xml.bind.annotation.XmlRegistry;

import com.izn.schemamodeler.admin.inquiry.InquiryXMLSchema;


/**
* This object contains factory methods for each 
* Java content interface and Java element interface 
* generated in the com.izn.schemamodeler.uiw.xml.inquiry package. 
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
  * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.izn.schemamodeler.uiw.xml.command
  * 
  */
 public ObjectFactory() {
 }

 /**
  * Create an instance of {@link InquiryXMLSchema }
  * 
  */
 public InquiryXMLSchema createInquiry() {
     return new InquiryXMLSchema();
 }

 /**
  * Create an instance of {@link InquiryXMLSchema.Schema }
  * 
  */
 public InquiryXMLSchema.Schema createInquirySchema() {
     return new InquiryXMLSchema.Schema();
 }

 /**
  * Create an instance of {@link InquiryXMLSchema.Schema.Field }
  * 
  */
 public InquiryXMLSchema.Schema.Field createInquirySchemaField() {
     return new InquiryXMLSchema.Schema.Field();
 }

 /**
  * Create an instance of {@link InquiryXMLSchema.Schema.Field.Setting }
  * 
  */
 public InquiryXMLSchema.Schema.Field.Setting createInquirySchemaFieldSetting() {
     return new InquiryXMLSchema.Schema.Field.Setting();
 }

 /**
  * Create an instance of {@link InquiryXMLSchema.Schema.Basic }
  * 
  */
 public InquiryXMLSchema.Schema.Basic createInquirySchemaBasic() {
     return new InquiryXMLSchema.Schema.Basic();
 }

 /**
  * Create an instance of {@link InquiryXMLSchema.Schema.Field.Detail }
  * 
  */
 public InquiryXMLSchema.Schema.Field.Detail createInquirySchemaFieldDetail() {
     return new InquiryXMLSchema.Schema.Field.Detail();
 }

 /**
  * Create an instance of {@link InquiryXMLSchema.Schema.Field.Setting.Param }
  * 
  */
 public InquiryXMLSchema.Schema.Field.Setting.Param createInquirySchemaFieldSettingParam() {
     return new InquiryXMLSchema.Schema.Field.Setting.Param();
 }

}
