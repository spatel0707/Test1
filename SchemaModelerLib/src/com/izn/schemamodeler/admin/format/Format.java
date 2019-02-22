package com.izn.schemamodeler.admin.format;

public class Format {

	String name;
	String description;
	String hidden;
	String version;
 	String filesuffix;
 	String filecreator;
 	String filetype;
 	String registryname;
 	
	public Format(String name, String description, String hidden, String registryname) {
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
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getFilesuffix() {
		return filesuffix;
	}
	public void setFilesuffix(String filesuffix) {
		this.filesuffix = filesuffix;
	}
	public String getFilecreator() {
		return filecreator;
	}
	public void setFilecreator(String filecreator) {
		this.filecreator = filecreator;
	}
	public String getFiletype() {
		return filetype;
	}
	public void setFiletype(String filetype) {
		this.filetype = filetype;
	} 
}
