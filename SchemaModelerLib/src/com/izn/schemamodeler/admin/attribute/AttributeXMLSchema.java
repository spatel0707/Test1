package com.izn.schemamodeler.admin.attribute;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="", propOrder={"schema"})
@XmlRootElement(name="component")
public class AttributeXMLSchema
{
  protected List<Schema> schema;
  @XmlAttribute(name="type")
  protected String type;
  @XmlAttribute(name="name")
  protected String name;
  @XmlAttribute(name="version")
  protected String version;
  
  public List<Schema> getSchema()
  {
    if (this.schema == null) {
      this.schema = new ArrayList();
    }
    return this.schema;
  }
  
  public String getType()
  {
    return this.type;
  }
  
  public void setType(String value)
  {
    this.type = value;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public void setName(String value)
  {
    this.name = value;
  }
  
  public String getVersion()
  {
    return this.version;
  }
  
  public void setVersion(String value)
  {
    this.version = value;
  }
  
  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlType(name="", propOrder={"basic", "range", "trigger"})
  public static class Schema
  {
    @XmlElement(required=true)
    protected Basic basic;
    @XmlElement(required=true)
    protected Range range;
    @XmlElement(required=true)
    protected Trigger trigger;
    
    public Basic getBasic()
    {
      return this.basic;
    }
    
    public void setBasic(Basic value)
    {
      this.basic = value;
    }
    
    public Range getRange()
    {
      return this.range;
    }
    
    public void setRange(Range value)
    {
      this.range = value;
    }
    
    public Trigger getTrigger()
    {
      return this.trigger;
    }
    
    public void setTrigger(Trigger value)
    {
      this.trigger = value;
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name="", propOrder={"field"})
    public static class Basic
    {
      @XmlElement(required=true)
      protected Field field;
      @XmlAttribute(name="name")
      protected String name;
      @XmlAttribute(name="description")
      protected String description;
      @XmlAttribute(name="hidden")
      protected String hidden;
      @XmlAttribute(name="registryName")
      protected String registryName;
      
      public Field getField()
      {
        return this.field;
      }
      
      public void setField(Field value)
      {
        this.field = value;
      }
      
      public String getName()
      {
        return this.name;
      }
      
      public void setName(String value)
      {
        this.name = value;
      }
      
      public String getDescription()
      {
        return this.description;
      }
      
      public void setDescription(String value)
      {
        this.description = value;
      }
      
      public String getHidden()
      {
        return this.hidden;
      }
      
      public void setHidden(String value)
      {
        this.hidden = value;
      }
      
      public String getRegistryName()
      {
        return this.registryName;
      }
      
      public void setRegistryName(String value)
      {
        this.registryName = value;
      }
      
      @XmlAccessorType(XmlAccessType.FIELD)
      @XmlType(name="", propOrder={"detail"})
      public static class Field
      {
        protected List<Detail> detail;
        @XmlAttribute(name="type")
        protected String type;
        
        public List<Detail> getDetail()
        {
          if (this.detail == null) {
            this.detail = new ArrayList();
          }
          return this.detail;
        }
        
        public String getType()
        {
          return this.type;
        }
        
        public void setType(String value)
        {
          this.type = value;
        }
        
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name="", propOrder={"value"})
        public static class Detail
        {
          @XmlValue
          protected String value;
          @XmlAttribute(name="name")
          protected String name;
          @XmlAttribute(name="value")
          protected String valueAttribute;
          
          public String getValue()
          {
            return this.value;
          }
          
          public void setValue(String value)
          {
            this.value = value;
          }
          
          public String getName()
          {
            return this.name;
          }
          
          public void setName(String value)
          {
            this.name = value;
          }
          
          public String getValueAttribute()
          {
            return this.valueAttribute;
          }
          
          public void setValueAttribute(String value)
          {
            this.valueAttribute = value;
          }
        }
      }
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name="", propOrder={"rangedetail"})
    public static class Range
    {
      protected List<Rangedetail> rangedetail;
      
      public List<Rangedetail> getRangedetail()
      {
        if (this.rangedetail == null) {
          this.rangedetail = new ArrayList();
        }
        return this.rangedetail;
      }
      
      @XmlAccessorType(XmlAccessType.FIELD)
      @XmlType(name="", propOrder={"value"})
      public static class Rangedetail
      {
        @XmlValue
        protected String value;
        @XmlAttribute(name="type")
        protected String type;
        @XmlAttribute(name="value")
        protected String valueAttribute;
        
        public String getValue()
        {
          return this.value;
        }
        
        public void setValue(String value)
        {
          this.value = value;
        }
        
        public String getType()
        {
          return this.type;
        }
        
        public void setType(String value)
        {
          this.type = value;
        }
        
        public String getValueAttribute()
        {
          return this.valueAttribute;
        }
        
        public void setValueAttribute(String value)
        {
          this.valueAttribute = value;
        }
      }
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name="", propOrder={"event"})
    public static class Trigger
    {
      protected List<Event> event;
      
      public List<Event> getEvent()
      {
        if (this.event == null) {
          this.event = new ArrayList();
        }
        return this.event;
      }
      
      @XmlAccessorType(XmlAccessType.FIELD)
      @XmlType(name="", propOrder={"eventdetail"})
      public static class Event
      {
        protected List<Eventdetail> eventdetail;
        @XmlAttribute(name="name")
        protected String name;
        
        public List<Eventdetail> getEventdetail()
        {
          if (this.eventdetail == null) {
            this.eventdetail = new ArrayList();
          }
          return this.eventdetail;
        }
        
        public String getName()
        {
          return this.name;
        }
        
        public void setName(String value)
        {
          this.name = value;
        }
        
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name="", propOrder={"value"})
        public static class Eventdetail
        {
          @XmlValue
          protected String value;
          @XmlAttribute(name="type")
          protected String type;
          @XmlAttribute(name="program")
          protected String program;
          @XmlAttribute(name="input")
          protected String input;
          
          public String getValue()
          {
            return this.value;
          }
          
          public void setValue(String value)
          {
            this.value = value;
          }
          
          public String getType()
          {
            return this.type;
          }
          
          public void setType(String value)
          {
            this.type = value;
          }
          
          public String getProgram()
          {
            return this.program;
          }
          
          public void setProgram(String value)
          {
            this.program = value;
          }
          
          public String getInput()
          {
            return this.input;
          }
          
          public void setInput(String value)
          {
            this.input = value;
          }
        }
      }
    }
  }
}
