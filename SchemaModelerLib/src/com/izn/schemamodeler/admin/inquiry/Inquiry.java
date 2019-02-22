package com.izn.schemamodeler.admin.inquiry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Inquiry {

	String name;
	String description;
	String hidden;
	String registryname;
 	//String href;
 	//String alt;
 	//String label;
 	String pattern;
 	String format;
 	//String user;
 	String code;
 	//List<Map<String, String>> lstSetting=new ArrayList<Map<String, String>>();
 	List<Map<String, String>> lstArgument=new ArrayList<Map<String, String>>();
	public Inquiry(String name, String description, String hidden, String registryname) {
		super();
		this.name = name;
		this.description = description;
		this.registryname = registryname;
		this.hidden = (hidden.equalsIgnoreCase("false")) ? "!hidden" : "hidden";
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
//	public String getLabel() {
//		return label;
//	}
//	public void setLabel(String label) {
//		this.label = label;
//	}
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public String getCode() {
	//	System.out.println("getCode--------->>>>>>>>"+getCode());
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public List<Map<String, String>> getLstArgument() {
		return lstArgument;
	}
	public void setLstArgument(List<Map<String, String>> lstArgument) {
		this.lstArgument = lstArgument;
	}
	
	
	
}