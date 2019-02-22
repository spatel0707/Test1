package com.izn.schemamodeler.admin.program;

/**
 * @author IZNPun45
 *
 */
/**
 * @author IZNPun45
 *
 */
public class Program {
	 String name;
	 String hidden;
	 String description;
	 String registryName;
	private String code;
	private String type;
	private String user;
	private String execute;
	private String needsbusinessobject;
	private String downloadable;
	private String pipe;
	private String pool;
	private String filepath;
	
	public Program(String name, String hidden, String description, String registryName) {
		super();
		this.name = name;
		this.hidden = (hidden.equalsIgnoreCase("true")) ? "hidden" : "!hidden";
		this.description = description;
		this.registryName = registryName;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getExecute() {
		return execute;
	}

	public void setExecute(String execute) {
		this.execute = execute;
	}

	public String getNeedsbusinessobject() {
		return needsbusinessobject;
	}

	public void setNeedsbusinessobject(String needsbusinessobject) {
		this.needsbusinessobject = (needsbusinessobject.equalsIgnoreCase("true")) ? "needsbusinessobject" : "!needsbusinessobject" ;
	}

	public String getDownloadable() {
		return downloadable;
	}

	public void setDownloadable(String downloadable) {
		this.downloadable = (downloadable.equalsIgnoreCase("true")) ? "downloadable" : "!downloadable" ;
	}

	public String getPipe() {
		return pipe;
	}

	public void setPipe(String pipe) {
		this.pipe = (pipe.equalsIgnoreCase("true")) ? "pipe" : "!pipe" ;
	}

	public String getPool() {
		return pool;
	}

	public void setPool(String pool) {
		this.pool = (pool.equalsIgnoreCase("true")) ? "pooled" : "!pooled" ;;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}
	
	 
}
