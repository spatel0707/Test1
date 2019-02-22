package com.izn.schemamodeler.connection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Connection {
	private String tomid;
	private String frommid;
	private String fromType;
	private String fromName;
	private String fromRevision;
	private String toType;
	private String toName;
	private String toRevision;
	private String id;
    private List<Map<String,String>> lstAttribute=new ArrayList<Map<String,String>>();
    private List<Connection> lstRelationship=new ArrayList<Connection>();
	  
	public Connection() {
		
		super();
	}
	public Connection(String relId) {
		
		super();
		this.id = relId;
	}
	
	public String getTomid() {
		return tomid;
	}
	public void setTomid(String tomid) {
		this.tomid = tomid;
	}
	public String getFrommid() {
		return frommid;
	}
	public void setFrommid(String frommid) {
		this.frommid = frommid;
	}
	public String getFromType() {
		return fromType;
	}
	
	public void setFromType(String fromtype) {
		this.fromType = fromtype;
	}
	
	public String getToType() {
		return toType;
	}
	
	public void setToType(String totype) {
		this.toType = totype;
	}
	
	public String getFromName() {
		return fromName;
	}
	
	public void setFromName(String fromname) {
		this.fromName = fromname;
	}
	
	public String getToName() {
		return toName;
	}
	
	public void setToName(String toname) {
		this.toName = toname;
	}
	
	public String getFromRevision() {
		return fromRevision;
	}
	
	public void setFromRevision(String fromrevision) {
		this.fromRevision = fromrevision;
	}
	
	public String getToRevision() {
		return toRevision;
	}
	public void setToRevision(String torevision) {
		this.toRevision = torevision;
	}
	
	public List<Map<String,String>> getLstAttribute() {
		return lstAttribute;
	}
	public void setLstAttribute(List<Map<String,String>> lstAttribute) {
		this.lstAttribute = lstAttribute;
	}

	public List<Connection> getLstRelationship() {
		return lstRelationship;
	}

	public void setLstRelationship(List<Connection> lstRelationship) {
		this.lstRelationship = lstRelationship;
	}     
}
