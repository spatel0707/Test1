package com.izn.schemamodeler.admin.role;

import java.util.ArrayList;
import java.util.List;

public class Role {
	String name;
	String description;
	String hidden;
	String registryname;
	private String parent;
	private String askindof;
	private String site;
	private String roletype;
	private String maturity;
	private String child;
	private String assigment;

	public Role(String name, String description, String hidden, String registryname) {
		super();
		this.name = name;
		this.description = description;
		this.registryname = registryname;
		this.hidden = (hidden.equalsIgnoreCase("true")) ? "hidden" : "!hidden";
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

	public String getRoletype() {
		return roletype;
	}

	public void setRoletype(String roletype) {
		this.roletype = roletype;
	}

	public String getChild() {
		return child;
	}

	public void setChild(String child) {
		this.child = child;
	}

	public String getAskindof() {
		return askindof;
	}

	public void setAskindof(String askindof) {
		this.askindof = askindof;
	}

	public String getMaturity() {
		return maturity;
	}

	public void setMaturity(String maturity) {
		this.maturity = (maturity == null || maturity.isEmpty()) ? "none" : maturity;
	}

	public String getAssigment() {
		return assigment;
	}

	public void setAssigment(String assigment) {
		this.assigment = assigment;
	}

}
