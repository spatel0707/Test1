package com.izn.schemamodeler.admin.attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Attribute
{
  String name;
  String description;
  String hidden;
  String nameOld;
  String deFault;
  String type;
  String owner;
  String ownerKind;
  String oldDimension;
  boolean bIsDimensionChanged = false;
  boolean bIsNameChanged = false;
  String maxLength;
  String valueType;
  String multiLine;
  String dimension;
  String registryname;
  
  public Attribute(String name, String description, String hidden, String registryname)
  {
    this.name = name;
    this.description = description;
    this.registryname = registryname;
    this.hidden = (hidden.equalsIgnoreCase("true") ? "hidden" : "!hidden");
  }
  
  String resetOnClone = "resetonclone";
  String resetOnRevision = "resetonrevision";
  List<Map<Object, Object>> slTriggers = new ArrayList();
  List<Map> slFilterTrigger = new ArrayList();
  List<Map<Object, Object>> slRanges = new ArrayList();
  List<Map<Object, Object>> slFilterRanges = new ArrayList();
  
  public List<Map<Object, Object>> getSlFilterRanges()
  {
    return this.slFilterRanges;
  }
  
  public void setSlFilterRanges(List<Map<Object, Object>> slFilterRanges)
  {
    this.slFilterRanges = slFilterRanges;
  }
  
  public List<Map<Object, Object>> getSlTriggers()
  {
    return this.slTriggers;
  }
  
  public void setSlTriggers(List<Map<Object, Object>> slTriggers)
  {
    this.slTriggers = slTriggers;
  }
  
  public List<Map> getSlFilterTrigger()
  {
    return this.slFilterTrigger;
  }
  
  public void setSlFilterTrigger(List<Map> slFilterTrigger)
  {
    this.slFilterTrigger = slFilterTrigger;
  }
  
  public List<Map<Object, Object>> getSlRanges()
  {
    return this.slRanges;
  }
  
  public void setSlRanges(List<Map<Object, Object>> slRanges)
  {
    this.slRanges = slRanges;
  }
  
  public String getNameOld()
  {
    return this.nameOld;
  }
  
  public void setNameOld(String nameOld)
  {
    this.nameOld = nameOld;
    if (!nameOld.equals(this.name)) {
      this.bIsNameChanged = true;
    }
  }
  
  public String getMaxLength()
  {
    return this.maxLength;
  }
  
  public void setMaxLength(String maxLength)
  {
    this.maxLength = maxLength;
  }
  
  public String getValueType()
  {
    return this.valueType;
  }
  
  public void setValueType(String valueType)
  {
    if ((this.type.equalsIgnoreCase("integer")) || (this.type.equalsIgnoreCase("real")) || (this.type.equalsIgnoreCase("timestamp")))
    {
      if (valueType.equalsIgnoreCase("singleValue")) {
        this.valueType = "notmultivalue notrangevalue";
      } else if (valueType.equalsIgnoreCase("multiValue")) {
        this.valueType = "multivalue notrangevalue";
      } else {
        this.valueType = "notmultivalue rangevalue";
      }
    }
    else {
      this.valueType = (valueType.equalsIgnoreCase("singleValue") ? "notmultivalue" : "multivalue");
    }
  }
  
  public String getMultiLine()
  {
    return this.multiLine;
  }
  
  public void setMultiLine(String multiLine)
  {
    this.multiLine = (multiLine.equalsIgnoreCase("true") ? "multiLine" : "!multiLine");
  }
  
  public String getDimension()
  {
    return this.dimension;
  }
  
  public void setDimension(String dimension)
  {
    this.dimension = dimension;
  }
  
  public String getResetOnClone()
  {
    return this.resetOnClone;
  }
  
  public void setResetOnClone(String resetOnClone)
  {
    this.resetOnClone = (resetOnClone.equalsIgnoreCase("true") ? "resetonclone" : "notresetonclone");
  }
  
  public String getResetOnRevision()
  {
    return this.resetOnRevision;
  }
  
  public void setResetOnRevision(String resetOnRevision)
  {
    this.resetOnRevision = (resetOnRevision.equalsIgnoreCase("true") ? "resetonrevision" : "notresetonrevision");
  }
  
  public String getOldDimension()
  {
    return this.oldDimension;
  }
  
  public void setOldDimension(String oldDimension)
  {
    this.bIsDimensionChanged = (!oldDimension.equalsIgnoreCase(this.dimension));
    this.oldDimension = oldDimension;
  }
  
  public String getOwner()
  {
    return this.owner;
  }
  
  public void setOwner(String owner)
  {
    this.owner = owner;
  }
  
  public String getOwnerKind()
  {
    return this.ownerKind;
  }
  
  public void setOwnerKind(String ownerKind)
  {
    this.ownerKind = ownerKind;
  }
  
  public String getType()
  {
    return this.type;
  }
  
  public void setType(String type)
  {
    this.type = type;
  }
  
  public String getDeFault()
  {
    return this.deFault;
  }
  
  public void setDeFault(String deFault)
  {
    this.deFault = deFault;
  }
}
