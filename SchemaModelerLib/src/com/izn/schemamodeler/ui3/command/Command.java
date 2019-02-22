package com.izn.schemamodeler.ui3.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Command {

	String name;
	String description;
	String hidden;
	String registryname;
	String href;
	String alt;
	String label;
	String user;
	String code;

	private String filepath;
	List<Map<String, String>> lstSetting = new ArrayList<Map<String, String>>();
	List<Map<String, String>> lstaccessdetail = new ArrayList<Map<String, String>>();

	public Command(String name, String description, String hidden, String registryname) {
		super();
		this.name = name;
		this.description = description;
		this.registryname = registryname;
		this.hidden = (hidden.equalsIgnoreCase("false")) ? "!hidden" : "hidden";
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getAlt() {
		return alt;
	}

	public void setAlt(String alt) {
		this.alt = (alt.isEmpty()) ? " " : alt;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public List<Map<String, String>> getLstSetting() {
		return lstSetting;
	}

	public void setLstSetting(List<Map<String, String>> lstSetting) {
		this.lstSetting = lstSetting;
	}

	public List<Map<String, String>> getLstaccessdetail() {
		return lstaccessdetail;
	}

	public void setLstaccessdetail(List<Map<String, String>> lstaccessdetail) {
		this.lstaccessdetail = lstaccessdetail;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

}
