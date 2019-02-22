package com.izn.schemamodeler.ui3.page;

public class Page {
	
	String name;
	String description;
	String hidden;
	private String mime;
	private String filepath;
	private String content;
	
	public Page(String name, String description, String hidden) {
		super();
		this.name = name;
		this.description = description;
		this.hidden = (hidden.equalsIgnoreCase("true")) ? "hidden" : "!hidden";
	}

	public String getMime() {
		return mime;
	}

	public void setMime(String mime) {
		this.mime = mime;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
 	

}
