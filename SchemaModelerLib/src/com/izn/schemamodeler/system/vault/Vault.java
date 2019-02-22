package com.izn.schemamodeler.system.vault;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Vault {
	
	String name;
	String description;
	String hidden;
	String registryname;
	String tablespace;
	String indexspace;
	String status;
  private List<Map<String,String>> lstAttribute=new ArrayList<Map<String,String>>();
  
  public Vault(String name, String description, String hidden,String registryname) {
		
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
	public List<Map<String, String>> getLstAttribute() {
		return lstAttribute;
	}
	public void setLstAttribute(List<Map<String, String>> lstAttribute) {
		this.lstAttribute = lstAttribute;
	}
	public String getTablespace() {
		return tablespace;
	}
	public void setTablespace(String tablespace) {
		this.tablespace = tablespace;
	}
	public String getIndexspace() {
		return indexspace;
	}
	public void setIndexspace(String indexspace) {
		this.indexspace = indexspace;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

}
