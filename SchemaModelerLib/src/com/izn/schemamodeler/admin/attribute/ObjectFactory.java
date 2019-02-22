package com.izn.schemamodeler.admin.attribute;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory
{
  public AttributeXMLSchema createAttributeXMLSchema()
  {
    return new AttributeXMLSchema();
  }
  
  public AttributeXMLSchema.Schema createAttributeXMLSchemaSchema()
  {
    return new AttributeXMLSchema.Schema();
  }
  
  public AttributeXMLSchema.Schema.Trigger createAttributeXMLSchemaSchemaTrigger()
  {
    return new AttributeXMLSchema.Schema.Trigger();
  }
  
  public AttributeXMLSchema.Schema.Trigger.Event createAttributeXMLSchemaSchemaTriggerEvent()
  {
    return new AttributeXMLSchema.Schema.Trigger.Event();
  }
  
  public AttributeXMLSchema.Schema.Range createAttributeXMLSchemaSchemaRange()
  {
    return new AttributeXMLSchema.Schema.Range();
  }
  
  public AttributeXMLSchema.Schema.Basic createAttributeXMLSchemaSchemaBasic()
  {
    return new AttributeXMLSchema.Schema.Basic();
  }
  
  public AttributeXMLSchema.Schema.Basic.Field createAttributeXMLSchemaSchemaBasicField()
  {
    return new AttributeXMLSchema.Schema.Basic.Field();
  }
  
  public AttributeXMLSchema.Schema.Trigger.Event.Eventdetail createAttributeXMLSchemaSchemaTriggerEventEventdetail()
  {
    return new AttributeXMLSchema.Schema.Trigger.Event.Eventdetail();
  }
  
  public AttributeXMLSchema.Schema.Range.Rangedetail createAttributeXMLSchemaSchemaRangeRangedetail()
  {
    return new AttributeXMLSchema.Schema.Range.Rangedetail();
  }
  
  public AttributeXMLSchema.Schema.Basic.Field.Detail createAttributeXMLSchemaSchemaBasicFieldDetail()
  {
    return new AttributeXMLSchema.Schema.Basic.Field.Detail();
  }
}
