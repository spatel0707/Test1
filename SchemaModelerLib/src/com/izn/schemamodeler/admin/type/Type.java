package com.izn.schemamodeler.admin.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Type {

	String name;
	String description;
	String hidden;
	String registryname;
	String nameOld;
	String deFault;
	String derived;
	String sparse;
	String sabstract;
	String attributes;
	String methods;
	List<Map<Object, Object>> slTriggers = new ArrayList<Map<Object, Object>>();
	List<Map> slFilterTrigger = new ArrayList<Map>();

	public Type(String name, String description, String hidden, String registryname) {
		super();
		this.name = name;
		this.description = description;
		this.registryname = registryname;
		this.hidden = (hidden.equalsIgnoreCase("true")) ? "hidden" : "!hidden";
	}

	public String getNameOld() {
		return nameOld;
	}

	public void setNameOld(String nameOld) {
		this.nameOld = nameOld;
	}

	public String getDerived() {
		return derived;
	}

	public void setDerived(String derived) {
		this.derived = derived;
	}

	public String getSparse() {
		return sparse;
	}

	public void setSparse(String sparse) {
		this.sparse = sparse;
	}

	public String getSabstract() {
		return sabstract;
	}

	public void setSabstract(String sabstract) {
		this.sabstract = sabstract;
	}

	public String getAttributes() {
		return attributes;
	}

	public void setAttributes(String attributes) {
		this.attributes = attributes;
	}

	public String getMethods() {
		return methods;
	}

	public void setMethods(String methods) {
		this.methods = methods;
	}

	public List<Map<Object, Object>> getSlTriggers() {
		return slTriggers;
	}

	public void setSlTriggers(List<Map<Object, Object>> slTriggers) {
		this.slTriggers = slTriggers;
	}

	public List<Map> getSlFilterTrigger() {
		return slFilterTrigger;
	}

	public void setSlFilterTrigger(List<Map> slFilterTrigger) {
		this.slFilterTrigger = slFilterTrigger;
	}

}
