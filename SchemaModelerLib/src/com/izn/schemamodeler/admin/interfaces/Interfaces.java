package com.izn.schemamodeler.admin.interfaces;

public class Interfaces {
	
	String name;
	String description;
	String hidden;
	String derived;
 	String sabstract;
 	String attribute;
 	String type;
 	String relationship;
 	String registryname;
 	 
	public Interfaces(String name, String description, String hidden, String registryname) {
		super();
		this.name = name;
		this.description = description;
		this.registryname = registryname;
		this.hidden = (hidden.endsWith("true")) ? "hidden" : "!hidden";
	}
	public String getDerived() {
		return derived;
	}
	public void setDerived(String derived) {
		this.derived = derived;
	}
	public String getSabstract() {
		return sabstract;
	}
	public void setSabstract(String sabstract) {
		this.sabstract = sabstract;
	}
	public String getAttribute() {
		return attribute;
	}
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getRelationship() {
		return relationship;
	}
	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}
	
  
}
