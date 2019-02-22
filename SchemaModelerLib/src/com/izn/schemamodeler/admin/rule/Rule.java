package com.izn.schemamodeler.admin.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Rule {
	
	String name;
	String description;
	String hidden;
	//String derived;
 	//String sabstract;
 	String governedAttribute;
 	//String type;
 	String user;
 	String governedRelationships; 
 	String governedForms;
 	String governedPrograms;
 	String accessDetails;
	List<Map<Object, Object>> slObjectAccess= new ArrayList<Map<Object, Object>>();


	public List<Map<Object, Object>> getSlObjectAccess() {
		return slObjectAccess;
	}


	public void setSlObjectAccess(List<Map<Object, Object>> slObjectAccess) {
		this.slObjectAccess = slObjectAccess;
	}


	public String getAccessDetails() {
		return accessDetails;
	}


	public void setAccessDetails(String accessDetails) {
		this.accessDetails = accessDetails;
	}


	public Rule(String name, String description, String hidden) {
		super();
		this.name = name;
		this.description = description;
		this.hidden = (hidden.endsWith("true")) ? "hidden" : "!hidden";
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


//	public String getDerived() {
//		return derived;
//	}
//
//
//	public void setDerived(String derived) {
//		this.derived = derived;
//	}


	public String getGovernedAttribute() {
		return governedAttribute;
	}


	public void setGovernedAttribute(String governedAttribute) {
		this.governedAttribute = governedAttribute;
	}


//	public String getType() {
//		return type;
//	}
//
//
//	public void setType(String type) {
//		this.type = type;
//	}


//	public String getObjectAccess() {
//		return objectAccess;
//	}
//
//
//	public void setObjectAccess(String objectAccess) {
//		this.objectAccess = objectAccess;
//	}

	public String getUser() {
		return user;
	}


	public void setUser(String user) {
		this.user = user;
	}
	public String getGovernedRelationships() {
		return governedRelationships;
	}


	public void setGovernedRelationships(String governedRelationships) {
		this.governedRelationships = governedRelationships;
	}


	public String getGovernedForms() {
		return governedForms;
	}


	public void setGovernedForms(String governedForms) {
		this.governedForms = governedForms;
	}


	public String getGovernedPrograms() {
		return governedPrograms;
	}


	public void setGovernedPrograms(String governedPrograms) {
		this.governedPrograms = governedPrograms;
	}
	
  
}
