package com.izn.schemamodeler.admin.dimension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dimension {
    
	String name;
	String hidden;
	String description;
	String registryname;
	private List<Dimension.Data> lstData=new ArrayList<Dimension.Data>();
	Map<String,Data> lstMapUnit = new HashMap<String,Data>();
	public Dimension(String name, String hidden, String description, String registryname) {
		super();
		this.name = name;
		this.hidden = (hidden.equalsIgnoreCase("true")) ? "hidden" : "!hidden" ;
		this.description = description;
		this.registryname = registryname;
	}
	
	
	
	public String getRegistryname() {
		return registryname;
	}

	public void setRegistryname(String registryname) {
		this.registryname = registryname;
	}

	public List<Dimension.Data> getLstData() {
		return lstData;
	}

	public void setLstData(List<Dimension.Data> lstData) {
		this.lstData = lstData;
	}

    public Dimension.Data creatNewDataInstance(){
    	
    	return new Dimension.Data();
    }
    
	public Map<String, Data> getLstMapUnit() {
		return lstMapUnit;
	}
	public void setLstMapUnit(Map<String, Data> lstMapUnit) {
		this.lstMapUnit = lstMapUnit;
	}

	public class Data{
	
      private String dbunit;
      private String unit;
      private String label;
      private String unitdescription;
	  private String dDefault;
      private String multiplier;
      private String offset;
      private List<Map<String, String>> lstSetting=new ArrayList<Map<String, String>>();
	

		Data(){
			
		}

		public String getDbunit() {
			return dbunit;
		}

		public void setDbunit(String dbunit) {
			this.dbunit = dbunit;
		}

		public String getUnit() {
			return unit;
		}

		public void setUnit(String unit) {
			this.unit = unit;
		}

		public String getLabel() {
			return label;
		}
		
		public String getDefault() {
			return dDefault;
		}
		
		public void setLabel(String label) {
			this.label = label;
		}

		public String getUnitdescription() {
			return unitdescription;
		}
		
		public void setDefault(String dDefault) {
			this.dDefault = dDefault;
		}

		public void setUnitdescription(String unitdescription) {
			this.unitdescription = unitdescription;
		}

		public String getMultiplier() {
			return multiplier;
		}

		public void setMultiplier(String multiplier) {
			this.multiplier = multiplier;
		}

		public String getOffset() {
			return offset;
		}

		public void setOffset(String offset) {
			this.offset = offset;
		}
		
	 	public List<Map<String, String>> getLstSetting() {
			return lstSetting;
		}

		public void setLstSetting(List<Map<String, String>> lstSetting) {
			this.lstSetting = lstSetting;
		}
		
 	}
	
 	
}
