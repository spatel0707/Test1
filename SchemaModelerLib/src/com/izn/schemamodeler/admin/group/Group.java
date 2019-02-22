package com.izn.schemamodeler.admin.group;

import java.util.ArrayList;
import java.util.List;

public class Group {
	String name;
	String description;
	String hidden;
	String registryname;
	private String parent;
	private String site;
	private String child;
	//private String grouptype;
	//private String maturity;
	private String asssignment;
	private String iconFile;

	/*
    String parent;
    String site;
    String child;
    String asssignment;
    String iconFile;
    */
	public Group(String name, String description, String hidden, String registryname) {
		super();
		this.name = name;
		this.description = description;
		this.registryname = registryname;
		this.hidden = (hidden.equalsIgnoreCase("true")) ? "hidden" : "!hidden";
	}
   
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getChild() {
		return child;
	}

	public void setChild(String child) {
		this.child = child;
	}
/*
	public String getMaturity() {
		return maturity;
	}

	public void setMaturity(String maturity) {
		this.maturity = maturity;
	}
*/
	public String getAsssignment() {
		return asssignment;
	}

	public void setAsssignment(String asssignment) {
		this.asssignment = asssignment;
	}

	public String getIconFile() {
		return iconFile;
	}

	public void setIconFile(String iconFile) {
		this.iconFile = iconFile;
	}

	

	
}
