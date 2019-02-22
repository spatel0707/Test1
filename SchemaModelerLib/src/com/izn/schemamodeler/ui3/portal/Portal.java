package com.izn.schemamodeler.ui3.portal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Portal {

	String name;
	String description;
	String hidden;
	String registryname;
	String href;
	String alt;
	String label;
	List<String> lstChannel = new ArrayList<String>();
	List<Map<String, String>> lstSetting = new ArrayList<Map<String, String>>();

	public Portal(String name, String description, String hidden, String registryname) {
		super();
		this.name = name;
		this.description = description;
		this.registryname = registryname;
		this.hidden = (hidden.equalsIgnoreCase("true")) ? "hidden" : "!hidden";
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
		this.alt = alt;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<String> getLstChannel() {
		return lstChannel;
	}

	public void setLstChannel(List<String> lstChannel) {
		this.lstChannel = lstChannel;
	}

	public List<Map<String, String>> getLstSetting() {
		return lstSetting;
	}

	public void setLstSetting(List<Map<String, String>> lstSetting) {
		this.lstSetting = lstSetting;
	}

}
