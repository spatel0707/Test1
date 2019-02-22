package com.izn.schemamodeler.ui3.channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Channel {
	
	 
		String name;
		String description;
		String hidden;
		String href;
	 	String alt;
	 	String height;
	 	String label;
	 	String registryname;
	 	List<Map<String, String>> lstSetting=new ArrayList<Map<String, String>>();
	 	List<String> lstCommand =new ArrayList<String>();
	 

		public Channel(String name, String description, String hidden,String registryname) {
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

		public List<String> getLstCommand() {
			return lstCommand;
		}

		public void setLstCommand(List<String> lstCommand) {
			this.lstCommand = lstCommand;
		}

		public String getHeight() {
			return height;
		}

		public void setHeight(String height) {
			this.height = height;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}
		
		
}
