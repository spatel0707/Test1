package com.izn.schemamodeler.bo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BusinessObject {
  String type;
  String name;
  String revision;
  private String description;
  private String state;
  private String owner;
  private String policy;
  private String vault;
  private String filePath;
  private String modified;
  private String grantee;
  private List<Map<String,String>> lstAttribute=new ArrayList<Map<String,String>>();
  
	public BusinessObject(String type, String name, String revision) {
		
		super();
		this.type = type;
		this.name = name;
		this.revision = revision;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getPolicy() {
		return policy;
	}
	public void setPolicy(String policy) {
		this.policy = policy;
	}
	
	public String getVault() {
		return vault;
	}
	public void setVault(String vault) {
		this.vault = vault;
	}
	
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public List<Map<String,String>> getLstAttribute() {
		return lstAttribute;
	}
	public void setLstAttribute(List<Map<String,String>> lstAttribute) {
		this.lstAttribute = lstAttribute;
	}   
	public String getModified() {
		return modified;
	}
	public void setModified(String modified) {
		this.modified = modified;
	}
	public String getGrantee() {
		return grantee;
	}
	public void setGrantee(String grantee) {
		this.grantee = grantee;
	}
}
