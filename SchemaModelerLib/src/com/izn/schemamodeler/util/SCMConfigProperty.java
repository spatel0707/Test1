package com.izn.schemamodeler.util;

public class SCMConfigProperty {
	String enoviaServerURL = "";
	String enoviaVersion = "";
	String user = "";
	String password = "";
	String adminUser = "";
	String adminPassword = "";
	String adminContext = "";
	String serverPath = "";
	String schemaSeperator = "";
	String logEverything = "";
	String firstFilePath = "";
	String secondFilePath = "";
	String compareFolder = "";
	String version = "";
	public String getSchemaSeperator() {
		return schemaSeperator;
	}
	public void setSchemaSeperator(String schemaSeperator) {
		this.schemaSeperator = schemaSeperator;
	}
	public String getLogEverything() {
		return logEverything;
	}
	public void setLogEverything(String logEverything) {
		this.logEverything = logEverything;
	}
	public String getFirstFilePath() {
		return firstFilePath;
	}
	public void setFirstFilePath(String firstFilePath) {
		this.firstFilePath = firstFilePath;
	}
	public String getSecondFilePath() {
		return secondFilePath;
	}
	public void setSecondFilePath(String secondFilePath) {
		this.secondFilePath = secondFilePath;
	}
	public String getCompareFolder() {
		return compareFolder;
	}
	public void setCompareFolder(String compareFolder) {
		this.compareFolder = compareFolder;
	}	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
}
