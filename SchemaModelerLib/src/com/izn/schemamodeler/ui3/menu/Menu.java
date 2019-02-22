package com.izn.schemamodeler.ui3.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Menu {

	String name;
	String description;
	String hidden;
	String registryname;
	private String href;
	private String alt;
	private String label;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	List<Map<String, String>> lstSetting = new ArrayList<Map<String, String>>();
	List<String> lstCommands = new ArrayList<String>();
	List<String> lstMenu = new ArrayList<String>();

	public Menu(String name, String description, String hidden, String registryname) {
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

	public List<Map<String, String>> getLstSetting() {
		return lstSetting;
	}

	public void setLstSetting(List<Map<String, String>> lstSetting) {
		this.lstSetting = lstSetting;
	}

	public List<String> getLstCommands() {
		return lstCommands;
	}

	public void setLstCommands(List<String> lstCommands) {
		this.lstCommands = lstCommands;
	}

	public List<String> getLstMenu() {
		return lstMenu;
	}

	public void setLstMenu(List<String> lstMenu) {
		this.lstMenu = lstMenu;
	}

}
