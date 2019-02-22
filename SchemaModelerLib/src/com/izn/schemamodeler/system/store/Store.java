package com.izn.schemamodeler.system.store;

public class Store {

	String name;
	String description;
	String hidden;
	String registryname;
	String type;
	String permission;
	String protocol;
	String port;
	String host;
	String Path;
	String user; 	
	String password;
	String fcs;
	String lock;
	
   public Store(String name, String description, String hidden,String registryname) {
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
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getPermission() {
		return permission;
	}
	public void setPermission(String permission) {
		this.permission = permission;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getPath() {
		return Path;
	}
	public void setPath(String path) {
		Path = path;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getFcs() {
		return fcs;
	}
	public void setFcs(String fcs) {
		this.fcs = fcs;
	}
	public String getLock() {
		return lock;
	}
	public void setLock(String lock) {
		this.lock = lock;
	}
}
