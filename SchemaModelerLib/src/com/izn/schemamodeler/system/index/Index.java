package com.izn.schemamodeler.system.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Index {
	String name;
	String description;
	String hidden;
	String registryname;
	String attribute;
	String enable;
	String unique;
	String field;
	private List<Map<String,String>> lstAttribute=new ArrayList<Map<String,String>>();
   public Index(String name, String description, String hidden,String registryname) {
		
		super();
		this.name = name;
		this.description = description;
		this.registryname = registryname;
		this.hidden = (hidden.equalsIgnoreCase("true")) ? "hidden" : "!hidden";
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getHidden() {
		return hidden;
	}
	public void setHidden(String hidden) {
		this.hidden = hidden;
	}
	public String getRegistryname() {
		return registryname;
	}
	public void setRegistryname(String registryname) {
		this.registryname = registryname;
	}
	public String getAttribute() {
		return attribute;
	}
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	public String getEnable() {
		return enable;
	}
	public void setEnable(String enable) {
		this.enable = enable;
	}
	public String getUnique() {
		return unique;
	}
	public void setUnique(String unique) {
		this.unique = unique;
	}
	public List<Map<String, String>> getLstAttribute() {
		return lstAttribute;
	}
	public void setLstAttribute(List<Map<String, String>> lstAttribute) {
		this.lstAttribute = lstAttribute;
	}
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	
}
