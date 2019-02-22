package schemamodeler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.StringTokenizer;
import matrix.util.StringList;

public class Parser {
	String command;
	String action;// IMPORT | EXPORT | PATH
	String exportType;// SCHEMA | BUS | pno
	String importType;// ADMIN TYPES| BUS
	String compareType;// *,bus,schema,pno
	String sourcePath;
	String targetPath;

	String name, revision, type, expandRel, modifiedDate, adminContext, serverPath,vault,exact;
	String currentPath, importPath, exportPath;
	StringList paramList;
	final String[] schemaTypeArray = { "*", "policy", "form", "relationship", "attribute", "type", "role", "interface",
			"dimension", "program", "channel", "command", "menu", "page", "table", "portal", "association", "format",
			"group","page", "webform", "inquiry","vault","index","store"};
	final String[] pnoTypeArray = { "*", "person", "company", "project", "context", "role", "businessunit" ,"department"};
	final StringList schemaTypeList;
	final StringList pnoTypeList;
	final StringList optionalBusArgumentList;
	final StringList optionalSchemaArgumentList;
	final StringList optionalPnoArgumentList;
	final String parametersMissing = "Required parameter(s) missing.";
	final String invalidType = "Schema Type doesn't exists.";
	final String invalidPath = "Path not found.";
	final String invalidCommand = "Command not identified.";
	final String invalidParameterCommand = "Command not identified. Found invalid parameter.";
	final String emptyCommand = "Empty command. Nothing to parse.";
	final String quoteMissing = "quote(s) missing";
	final String wrongInputDateFormat = " is wrong date format. Expected date format is [mm/dd/yyyy].";
	final String wrongImportInputConfirmation = "Wrong input. Please enter valid input <Y/N> and proceed.";
	final String[] optionalBusArguments = { "relationship", "rel", "exact", "vault", "output"};
	final String[] optionalSchemaArguments = { "moddate","output"};
	final String[] optionalPnoArguments = {""};
	int currentParsePos;
	StringList tempParamList;

	boolean bError;
	
	public Parser() {
		bError = false;
		command = new String();
		action = new String();
		exportType = new String();
		type = new String();
		name = new String();
		revision = new String();
		expandRel = new String();
		currentPath = new String();
		importPath = new String();
		exportPath = new String();
		modifiedDate = new String();
		adminContext = new String();
		serverPath = new String();
		paramList = new StringList();
		tempParamList = new StringList();
		currentParsePos = 0;
		schemaTypeList = new StringList();
		pnoTypeList = new StringList();
		optionalBusArgumentList = new StringList();
		optionalSchemaArgumentList = new StringList();
		optionalPnoArgumentList = new StringList();
		vault = new String();
		exact = new String();
		for (String s : schemaTypeArray) {
			schemaTypeList.add(s);
		}
		for (String s : pnoTypeArray) {
			pnoTypeList.add(s);
		}
		for (String s : optionalBusArguments) {
			optionalBusArgumentList.add(s);
		}
		for (String s : optionalSchemaArguments) {
			optionalSchemaArgumentList.add(s);
		}
		for (String s : optionalPnoArguments) {
			optionalPnoArgumentList.add(s);
		}
	}
	
	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	public String getTargetPath() {
		return targetPath;
	}

	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}
	public String getCompareType() {
		return compareType;
	}

	public void setCompareType(String compareType) {
		this.compareType = compareType;
	}
	public String getImportType() {
		return importType;
	}

	public void setImportType(String importType) {
		this.importType = importType;
	}

	public boolean isbError() {
		return bError;
	}

	public void setbError(boolean bError) {
		this.bError = bError;
	}
	public void setCommand(String command) {
		this.command = command;
	}

	public String getCommand() {
		return command;
	}

	private void setAction(String action) {
		this.action = action;
	}

	public String getAction() {
		return action;
	}

	private void setExportType(String expType) {
		this.exportType = expType;
	}

	public String getExportType() {
		return exportType;
	}

	public String getExact() {
		return exact;
	}

	public String getVault() {
		return vault;
	}
	
	public String getType() {
		return type;
	}
	
	public String getModifiedFromDate() {
		return modifiedDate;
	}
	
	public String getadminContext() {
		return adminContext;
	}
	
	public String getserverPath() {
		return serverPath;
	}


	public void setType(String type) {
		this.type = type;
	}

	private void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setVault(String vault) {
		this.vault = vault;
	}
	
	public void setExact(String exact) {
		this.exact = exact;
	}
	
	private void setRevision(String revision) {
		this.revision = revision;
	}

	public String getRevision() {
		return revision;
	}

	private void setExpandRel(String rel) {
		this.expandRel = rel;
	}

	public String getExpandRel() {
		return expandRel;
	}

	public void setModifiedFromDate(String modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	
	public void setadminContext(String adminContext) {
		this.adminContext = adminContext;
	}
	
	public void setserverPath(String serverPath) {
		this.serverPath = serverPath;
	}

	private void setCurrentPath(String currentPath) {
		this.currentPath = currentPath;
	}

	public String getCurrentPath() {
		return currentPath;
	}

	private void setImportPath(String importPath) {
		this.importPath = importPath;
	}

	public String getImportPath() {
		return importPath;
	}

	private void setExportPath(String exportPath) {
		this.exportPath = exportPath;
	}

	public String getExportPath() {
		return exportPath;
	}

	private void setParamList(StringList paramList) {
		this.paramList = paramList;
	}

	public StringList getParamList() {
		return paramList;
	}
	
	private void setExportDirPath(String exportPath) {
		this.exportPath = exportPath;
	}

	public String getExportDirPath() {
		return exportPath;
	}

	void init() {
		setAction(new String());
		setExportType(new String());
		setType(new String());
		setName(new String());
		setRevision(new String());
		setImportPath(new String());
		setExportPath(new String());
		setParamList(new StringList());
		setVault(new String());
		setExact(new String());
		currentParsePos = 0;
	}

	// int countCharOccurance(String text, char chr) {
		// Long count = text.chars().filter(ch -> ch == chr).count();
		// return count.intValue();
	// }
	
	int countCharOccurance(String text, char chr) {
	    int count = 0;
	    for(int i=0; i < text.length(); i++)
	    {    if(text.charAt(i) == chr)
	            count++;
	    }
	    return count;
	}
	
	boolean isPathValid(String path) {
		File tempPath = new File(path.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "").replaceAll("\'", ""));
		return (tempPath.exists() && tempPath.isDirectory());
	}
	boolean isPathValidSingleQuote(String path) {
		File tempPath = new File(path.replaceAll("\\\\", "\\\\\\\\").replaceAll("\'", ""));
		return (tempPath.exists() && tempPath.isDirectory());
	}

	// method for path setting
	void pathCommand(StringList paramList) throws Exception {
		while (currentParsePos < paramList.size() - 1 && paramList.get(currentParsePos).toString().isEmpty())
			currentParsePos++;
		String path = new String();
		int endQuotePos = -1;
		if (paramList.size() == 1 || paramList.get(currentParsePos).toString().isEmpty()) {
			if (getCurrentPath().isEmpty()) {
				System.out.print("Path not set.");
			} else {
				System.out.print(getCurrentPath().replaceAll("\\\\\\\\", "\\\\"));
			}
		} else if (paramList.get(currentParsePos).toString().charAt(0) == '"'
				&& countCharOccurance(paramList.get(currentParsePos).toString(), '"') == 1 && paramList.size() > 2) {
			path = paramList.get(currentParsePos).toString();
			for (int i = currentParsePos + 1; i < paramList.size(); i++) {
				path = path + " " + paramList.get(i).toString();
				if (paramList.get(i).toString().charAt(paramList.get(i).toString().length() - 1) == '"') {
					endQuotePos = i;
					break;
				} else if (paramList.get(i).toString().charAt(paramList.get(i).toString().length() - 1) == '\'') {
					endQuotePos = i;
					break;
				}
			}
			if (endQuotePos == -1)
				throw new InvalidSyntaxError(quoteMissing);
			if (isPathValid(path)) {
				setCurrentPath(path.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", ""));
				System.out.print("new path set.");
			} else
				throw new InvalidPathError(invalidPath);

		} else if (paramList.get(currentParsePos).toString().charAt(0) == '\''
				&& countCharOccurance(paramList.get(currentParsePos).toString(), '\'') == 1 && paramList.size() > 2) {
			path = paramList.get(currentParsePos).toString();
			for (int i = currentParsePos + 1; i < paramList.size(); i++) {
				path = path + " " + paramList.get(i).toString();
				if (paramList.get(i).toString().charAt(paramList.get(i).toString().length() - 1) == '\'') {
					endQuotePos = i;
					break;
				}
			}
			if (endQuotePos == -1)
				throw new InvalidSyntaxError(quoteMissing);
			if (isPathValidSingleQuote(path)) {
				setCurrentPath(path.replaceAll("\\\\", "\\\\\\\\").replaceAll("\'", ""));
				System.out.print("new path set.");
			} else
				throw new InvalidPathError(invalidPath);

		} else if (paramList.get(currentParsePos).toString().charAt(0) == '"'
				&& countCharOccurance(paramList.get(currentParsePos).toString(), '"') == 2 && paramList.size() == 2) {
			if (isPathValid((paramList.get(1).toString()))) {
				setCurrentPath((paramList.get(1).toString()).replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", ""));
				System.out.print("new path set.");
			} else
				throw new InvalidPathError(invalidPath);

		} else if (paramList.get(currentParsePos).toString().charAt(0) == '\''
				&& countCharOccurance(paramList.get(currentParsePos).toString(), '\'') == 2 && paramList.size() == 2) {
			if (isPathValidSingleQuote((paramList.get(1).toString()))) {
				setCurrentPath((paramList.get(1).toString()).replaceAll("\\\\", "\\\\\\\\").replaceAll("\'", ""));
				System.out.print("new path set.");
			} else
				throw new InvalidPathError(invalidPath);

		} else if (paramList.get(currentParsePos).toString().charAt(0) != '"'
				&& countCharOccurance(paramList.get(currentParsePos).toString(), '"') == 0 && paramList.size() == 2) {
			if (isPathValid((paramList.get(currentParsePos).toString()))) {
				setCurrentPath((paramList.get(currentParsePos).toString()).replaceAll("\\\\", "\\\\\\\\")
						.replaceAll("\"", ""));
				System.out.print("new path set.");
			} else
				throw new InvalidPathError(invalidPath);

		} else if (paramList.get(currentParsePos).toString().charAt(0) != '\''
				&& countCharOccurance(paramList.get(currentParsePos).toString(), '\'') == 0 && paramList.size() == 2) {

			if (isPathValidSingleQuote((paramList.get(currentParsePos).toString()))) {
				setCurrentPath((paramList.get(currentParsePos).toString()).replaceAll("\\\\", "\\\\\\\\")
						.replaceAll("\'", ""));
				System.out.print("new path set.");
			} else
				throw new InvalidPathError(invalidPath);

		} else
			throw new InvalidSyntaxError(quoteMissing);
	}

	void importCommand(StringList paramList) throws Exception {
		if(paramList.size() >= 3)
		{
			String sCurrentParsePosValue = paramList.get(currentParsePos).toString();
			if (schemaTypeList.contains(sCurrentParsePosValue.toLowerCase())|| "bus".equalsIgnoreCase(sCurrentParsePosValue)) {
				currentParsePos++;
				setImportType(sCurrentParsePosValue.toLowerCase());
				sCurrentParsePosValue = paramList.get(currentParsePos).toString();
			}else{
				throw new InvalidSyntaxError(invalidType);
			}
			String path = new String();
			int iParameterListSize = paramList.size();
			int endQuotePos = -1;

			if (iParameterListSize == 1 || sCurrentParsePosValue.isEmpty()) {
				if (getCurrentPath().isEmpty())
					throw new InvalidSyntaxError(parametersMissing);
				else {
					checkValidPath(getCurrentPath());
				}
			} else if (((sCurrentParsePosValue.charAt(0) == '"' && countCharOccurance(sCurrentParsePosValue, '"') == 1) || (sCurrentParsePosValue.charAt(0) == '\'' && countCharOccurance(sCurrentParsePosValue, '\'') == 1))) {
				path = paramList.get(currentParsePos).toString();
				for (int i = currentParsePos + 1; i < paramList.size(); i++) {
					path = path + " " + paramList.get(i).toString();
					if (paramList.get(i).toString().charAt(paramList.get(i).toString().length() - 1) == '"' || paramList.get(i).toString().charAt(paramList.get(i).toString().length() - 1) == '\'') {
						endQuotePos = i;
						break;
					}
				}
				if (endQuotePos == -1)
					throw new InvalidSyntaxError(quoteMissing);
				if(iParameterListSize == endQuotePos+1)
					checkValidPath(path);	
				else
					throw new InvalidSyntaxError(invalidParameterCommand);
			} else if (((sCurrentParsePosValue.charAt(0) == '"' && countCharOccurance(sCurrentParsePosValue, '"') == 2) || (sCurrentParsePosValue.charAt(0) == '\'' && countCharOccurance(sCurrentParsePosValue, '\'') == 2)) || ((sCurrentParsePosValue.charAt(0) != '"' && sCurrentParsePosValue.charAt(0) != '"' && countCharOccurance(sCurrentParsePosValue, '"') == 0) && (sCurrentParsePosValue.charAt(0) != '\'' && countCharOccurance(sCurrentParsePosValue, '\'') == 0))) {
				if(iParameterListSize == currentParsePos+1)
					checkValidPath(sCurrentParsePosValue);
				else
					throw new InvalidSyntaxError(invalidParameterCommand);
			} else{			
				throw new InvalidSyntaxError(quoteMissing);
			}
		}
		else
		{
			throw new InvalidSyntaxError(parametersMissing);
		}
	}

	void parseExportDir(StringList paramList) throws Exception {
			while (currentParsePos < paramList.size() - 1 && paramList.get(currentParsePos).toString().isEmpty())
				currentParsePos++;
			if (paramList.get(currentParsePos).toString().isEmpty())
				throw new InvalidSyntaxError(invalidPath);
			String path = new String();
			int endQuotePos = -1;
			if (paramList.get(currentParsePos).toString().charAt(0) == '"'
					&& countCharOccurance(paramList.get(currentParsePos).toString(), '"') == 1
					&& paramList.size() > (currentParsePos + 1)) {// if 2nd param
				path = paramList.get(currentParsePos).toString();
				tempParamList.add(path);
				for (int i = currentParsePos + 1; i < paramList.size(); i++) {
					path = path + " " + paramList.get(i).toString();
					tempParamList.add(paramList.get(i).toString());
					if (paramList.get(i).toString().charAt(paramList.get(i).toString().length() - 1) == '"') {
						endQuotePos = i;
						break;
					}
				}
				if (endQuotePos == -1)
					throw new InvalidSyntaxError(quoteMissing);
				if (isPathValid(path)) {
					setExportPath(path.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", ""));
				} else
					throw new InvalidPathError(invalidPath);
				
			} else if (paramList.get(currentParsePos).toString().charAt(0) == '\''
					&& countCharOccurance(paramList.get(currentParsePos).toString(), '\'') == 1
					&& paramList.size() > (currentParsePos + 1)) {// if 2nd param

				path = paramList.get(currentParsePos).toString();
				tempParamList.add(path);
				for (int i = currentParsePos + 1; i < paramList.size(); i++) {
					path = path + " " + paramList.get(i).toString();
					tempParamList.add(paramList.get(i).toString());
					if (paramList.get(i).toString().charAt(paramList.get(i).toString().length() - 1) == '\'') {
						endQuotePos = i;
						break;
					}
				}
				if (endQuotePos == -1)
					throw new InvalidSyntaxError(quoteMissing);
				if (isPathValidSingleQuote(path)) {
					setExportPath(path.replaceAll("\\\\", "\\\\\\\\").replaceAll("\'", ""));
				} else
					throw new InvalidPathError(invalidPath);
				
			} else if (paramList.get(currentParsePos).toString().charAt(0) == '"'
					&& countCharOccurance(paramList.get(currentParsePos).toString(), '"') == 2) {// if

				if (isPathValid((paramList.get(currentParsePos).toString()))) {
					tempParamList.add(paramList.get(currentParsePos).toString());
					setExportPath((paramList.get(currentParsePos).toString()).replaceAll("\\\\", "\\\\\\\\")
							.replaceAll("\"", ""));
					
				} else
					throw new InvalidPathError(invalidPath);
				
			} else if (paramList.get(currentParsePos).toString().charAt(0) == '\''
					&& countCharOccurance(paramList.get(currentParsePos).toString(), '\'') == 2) {// if

				if (isPathValidSingleQuote((paramList.get(currentParsePos).toString()))) {
					tempParamList.add(paramList.get(currentParsePos).toString());
					setExportPath((paramList.get(currentParsePos).toString()).replaceAll("\\\\", "\\\\\\\\")
							.replaceAll("\'", ""));
					
				} else
					throw new InvalidPathError(invalidPath);
				
			} else if (paramList.get(currentParsePos).toString().charAt(0) != '"'
					&& countCharOccurance(paramList.get(currentParsePos).toString(), '"') == 0) {// if

				if (isPathValid((paramList.get(currentParsePos).toString()))) {
					tempParamList.add(paramList.get(currentParsePos).toString());
					setExportPath((paramList.get(currentParsePos).toString()).replaceAll("\\\\", "\\\\\\\\")
							.replaceAll("\"", ""));
					
				} else
					throw new InvalidPathError(invalidPath);
				
			} else if (paramList.get(currentParsePos).toString().charAt(0) != '\''
					&& countCharOccurance(paramList.get(currentParsePos).toString(), '\'') == 0) {// if

				if (isPathValidSingleQuote((paramList.get(currentParsePos).toString()))) {
					tempParamList.add(paramList.get(currentParsePos).toString());
					setExportPath((paramList.get(currentParsePos).toString()).replaceAll("\\\\", "\\\\\\\\")
							.replaceAll("\'", ""));
					
				} else
					throw new InvalidPathError(invalidPath);
				
			} else
				throw new InvalidSyntaxError(quoteMissing);
	}
	
	String processNextArgument(StringList paramList) throws Exception 
	{
		String sCurrentParsePosValue = "";
		int endQuotePos = -1;
		if (currentParsePos > paramList.size() - 1)
		{			
			throw new InvalidSyntaxError(parametersMissing);
		}		
		else if (!paramList.get(currentParsePos).toString().isEmpty()) 
		{
			if (paramList.get(currentParsePos).toString().charAt(0) == '"' && countCharOccurance(paramList.get(currentParsePos).toString(), '"') == 1 && paramList.size() > (currentParsePos + 1)) 
			{
				sCurrentParsePosValue = paramList.get(currentParsePos).toString();
				tempParamList.add(sCurrentParsePosValue);
				for (int i = currentParsePos + 1; i < paramList.size(); i++) {
					sCurrentParsePosValue = sCurrentParsePosValue + " " + paramList.get(i).toString();
					tempParamList.add(paramList.get(i).toString());
					if (paramList.get(i).toString().charAt(paramList.get(i).toString().length() - 1) == '"') {
						endQuotePos = i;
						break;
					}
				}
				currentParsePos = endQuotePos + 1;
			}
			else if (paramList.get(currentParsePos).toString().charAt(0) == '\'' && countCharOccurance(paramList.get(currentParsePos).toString(), '\'') == 1 && paramList.size() > (currentParsePos + 1)) 
			{				
				sCurrentParsePosValue = paramList.get(currentParsePos).toString();	
				tempParamList.add(sCurrentParsePosValue);
				for (int i = currentParsePos + 1; i < paramList.size(); i++) {
					sCurrentParsePosValue = sCurrentParsePosValue + " " + paramList.get(i).toString();
					tempParamList.add(paramList.get(i).toString());
					if (paramList.get(i).toString().charAt(paramList.get(i).toString().length() - 1) == '\'') {
						endQuotePos = i;
						break;
					}
				}
				currentParsePos = endQuotePos + 1;
			} 
			else if (countCharOccurance(paramList.get(currentParsePos).toString(), '"') == 0 || countCharOccurance(paramList.get(currentParsePos).toString(), '"') == 2) 
			{				
				sCurrentParsePosValue = paramList.get(currentParsePos).toString();
				tempParamList.add(paramList.get(currentParsePos).toString());
				endQuotePos = currentParsePos;
				currentParsePos = endQuotePos + 1;
			} 
			else if (countCharOccurance(paramList.get(currentParsePos).toString(), '\'') == 0	|| countCharOccurance(paramList.get(currentParsePos).toString(), '\'') == 2) 
			{				
				sCurrentParsePosValue = paramList.get(currentParsePos).toString();
				tempParamList.add(paramList.get(currentParsePos).toString());
				endQuotePos = currentParsePos;
				currentParsePos = endQuotePos + 1;
			}
			if(endQuotePos == -1)
			{				
				throw new InvalidSyntaxError(quoteMissing);
			}
		}
		else
		{			
			throw new InvalidSyntaxError(invalidCommand);
		}
		return sCurrentParsePosValue;
	}
	String processBusOptionalArgument(StringList paramList, String sArgument) throws Exception 
	{
		String sCurrentParsePosValue = "";
		int iOptionalArgPos = -1;
		for (int i = 0; i < paramList.size(); i++) 
		{
			if (paramList.get(i).toString().equalsIgnoreCase(sArgument)) 
			{
				iOptionalArgPos = i;
				break;
			}
		}
		if (iOptionalArgPos > - 1)
		{	
			if(sArgument.equalsIgnoreCase("exact")){
				sCurrentParsePosValue = "true";
				tempParamList.add(sArgument);
			}else if(sArgument.equalsIgnoreCase("output")){
				currentParsePos = iOptionalArgPos + 1;
				tempParamList.add(sArgument);
				if(currentParsePos > paramList.size()-1){
					throw new InvalidSyntaxError(sArgument+" can not be empty.");
				}else{
					parseExportDir(paramList);
					sCurrentParsePosValue = getExportDirPath();
				}
			}else{
				tempParamList.add(sArgument);
				currentParsePos = iOptionalArgPos + 1;
				int endQuotePos = -1;
				if (currentParsePos > paramList.size() - 1 || optionalBusArgumentList.contains(paramList.get(currentParsePos).toString()))
				{			
					throw new InvalidSyntaxError(sArgument+" can not be empty.");
				}		
				else if (!paramList.get(currentParsePos).toString().isEmpty()) 
				{
					if(optionalBusArgumentList.contains(paramList.get(currentParsePos).toString())){
						throw new InvalidSyntaxError(sArgument+" can not be empty.");
					}
					if (paramList.get(currentParsePos).toString().charAt(0) == '"' && countCharOccurance(paramList.get(currentParsePos).toString(), '"') == 1 && paramList.size() > (currentParsePos + 1)) 
					{
						sCurrentParsePosValue = paramList.get(currentParsePos).toString();
						tempParamList.add(sCurrentParsePosValue);
						for (int i = currentParsePos + 1; i < paramList.size(); i++) {
							sCurrentParsePosValue = sCurrentParsePosValue + " " + paramList.get(i).toString();
							tempParamList.add(paramList.get(i).toString());
							if (paramList.get(i).toString().charAt(paramList.get(i).toString().length() - 1) == '"') {
								endQuotePos = i;
								break;
							}
						}
						currentParsePos = endQuotePos + 1;
					}
					else if (paramList.get(currentParsePos).toString().charAt(0) == '\'' && countCharOccurance(paramList.get(currentParsePos).toString(), '\'') == 1 && paramList.size() > (currentParsePos + 1)) 
					{				
						sCurrentParsePosValue = paramList.get(currentParsePos).toString();	
						tempParamList.add(sCurrentParsePosValue);
						for (int i = currentParsePos + 1; i < paramList.size(); i++) {
							sCurrentParsePosValue = sCurrentParsePosValue + " " + paramList.get(i).toString();
							tempParamList.add(paramList.get(i).toString());
							if (paramList.get(i).toString().charAt(paramList.get(i).toString().length() - 1) == '\'') {
								endQuotePos = i;
								break;
							}
						}
						currentParsePos = endQuotePos + 1;
					} 
					else if (countCharOccurance(paramList.get(currentParsePos).toString(), '"') == 0 || countCharOccurance(paramList.get(currentParsePos).toString(), '"') == 2) 
					{				
						sCurrentParsePosValue = paramList.get(currentParsePos).toString();
						tempParamList.add(paramList.get(currentParsePos).toString());
						endQuotePos = currentParsePos;
						currentParsePos = endQuotePos + 1;
					} 
					else if (countCharOccurance(paramList.get(currentParsePos).toString(), '\'') == 0	|| countCharOccurance(paramList.get(currentParsePos).toString(), '\'') == 2) 
					{				
						sCurrentParsePosValue = paramList.get(currentParsePos).toString();
						tempParamList.add(paramList.get(currentParsePos).toString());
						endQuotePos = currentParsePos;
						currentParsePos = endQuotePos + 1;
					}
					if(endQuotePos == -1)
					{				
						throw new InvalidSyntaxError(quoteMissing);
					}
				}
				else
				{			
					throw new InvalidSyntaxError(invalidCommand);
				}
				
			}
		}
		else
		{
			if(sArgument.equalsIgnoreCase("exact")){
				sCurrentParsePosValue = "false";
			}else{				
				sCurrentParsePosValue = "";
			}
		}
		return sCurrentParsePosValue;
	}
	
	void exportBus(StringList paramList) throws Exception {
		String typePattern = "";
		String namePattern = "";
		String revPattern = "";
		String relPattern = "";
		String exactPattern = "";
		String vaultPattern = "";
		String exportPath = "";
		StringList typeList = new StringList();
		/** TYPE ***********STARTS*************/
		typePattern = processNextArgument(paramList);
		typePattern = typePattern.replaceAll("\"", "");
		typePattern = typePattern.replaceAll("\'", "");
		setType(typePattern.trim());
		/** TYPE ***********ENDS**************/
		
		/** NAME ***********STARTS*************/
		namePattern = processNextArgument(paramList);
		namePattern = namePattern.replaceAll("\"", "");
		namePattern = namePattern.replaceAll("\'", "");
		namePattern = namePattern.replace("|", "@");
		setName(namePattern.trim());
		/** NAME ***********ENDS*************/
		
		/** REVISION ***********STARTS*************/
		revPattern = processNextArgument(paramList);
		revPattern = revPattern.replaceAll("\"", "");
		revPattern = revPattern.replaceAll("\'", "");
		setRevision(revPattern.trim());
		/** REVISION ***********ENDS*************/
		
		if(currentParsePos > paramList.size()-1)
		{
			setExpandRel("");
			setExact("");
			setVault("");
			exportPath = "";
		}
		else
		{
			relPattern = processBusOptionalArgument(paramList,"relationship");
			relPattern = relPattern.replaceAll("\"", "");
			relPattern = relPattern.replaceAll("\'", "");
			//relPattern = relPattern.replace("|", "@");
			if(relPattern.isEmpty()){
				relPattern = processBusOptionalArgument(paramList,"rel");
				relPattern = relPattern.replaceAll("\"", "");
				relPattern = relPattern.replaceAll("\'", "");
				//relPattern = relPattern.replace("|", "@");
			}
			setExpandRel(relPattern.trim());
			exactPattern = processBusOptionalArgument(paramList,"exact");
			exactPattern = exactPattern.replaceAll("\"", "");
			exactPattern = exactPattern.replaceAll("\'", "");
			setExact(exactPattern.trim());
			vaultPattern = processBusOptionalArgument(paramList,"vault");
			vaultPattern = vaultPattern.replaceAll("\"", "");
			vaultPattern = vaultPattern.replaceAll("\'", "");
			vaultPattern = vaultPattern.replace("|", "@");
			setVault(vaultPattern.trim());
			exportPath = processBusOptionalArgument(paramList,"output");			
		}
		if(paramList.size() == tempParamList.size())
		{
			if(exportPath.isEmpty()){
				createSchemaModelerDirectory();
			}
		}
		else
		{
			throw new InvalidSyntaxError(invalidParameterCommand);
		}
	}
	
	String processSchemaOptionalArgument(StringList paramList, String sArgument) throws Exception 
	{
		String sCurrentParsePosValue = "";
		int iOptionalArgPos = -1;
		for (int i = 0; i < paramList.size(); i++) 
		{
			if (paramList.get(i).toString().equalsIgnoreCase(sArgument)) 
			{
				iOptionalArgPos = i;
				break;
			}
		}
		if (iOptionalArgPos > - 1)
		{	
			if(sArgument.equalsIgnoreCase("output")){
				currentParsePos = iOptionalArgPos + 1;
				tempParamList.add(sArgument);
				if(currentParsePos > paramList.size()-1 || optionalSchemaArgumentList.contains(paramList.get(currentParsePos).toString())){
					throw new InvalidSyntaxError(sArgument+" can not be empty.");
				}else{
					parseExportDir(paramList);
					sCurrentParsePosValue = getExportDirPath();
				}
			}else{
				tempParamList.add(sArgument);
				currentParsePos = iOptionalArgPos + 1;
				int endQuotePos = -1;
				if (currentParsePos > paramList.size() - 1)
				{			
					throw new InvalidSyntaxError(sArgument+" can not be empty.");
				}		
				else if (!paramList.get(currentParsePos).toString().isEmpty()) 
				{
					if(optionalSchemaArgumentList.contains(paramList.get(currentParsePos).toString())){
						throw new InvalidSyntaxError(sArgument+" can not be empty.");
					}
					if (paramList.get(currentParsePos).toString().charAt(0) == '"' && countCharOccurance(paramList.get(currentParsePos).toString(), '"') == 1 && paramList.size() > (currentParsePos + 1)) 
					{
						sCurrentParsePosValue = paramList.get(currentParsePos).toString();
						tempParamList.add(sCurrentParsePosValue);
						for (int i = currentParsePos + 1; i < paramList.size(); i++) {
							sCurrentParsePosValue = sCurrentParsePosValue + " " + paramList.get(i).toString();
							tempParamList.add(paramList.get(i).toString());
							if (paramList.get(i).toString().charAt(paramList.get(i).toString().length() - 1) == '"') {
								endQuotePos = i;
								break;
							}
						}
						currentParsePos = endQuotePos + 1;
					}
					else if (paramList.get(currentParsePos).toString().charAt(0) == '\'' && countCharOccurance(paramList.get(currentParsePos).toString(), '\'') == 1 && paramList.size() > (currentParsePos + 1)) 
					{				
						sCurrentParsePosValue = paramList.get(currentParsePos).toString();	
						tempParamList.add(sCurrentParsePosValue);
						for (int i = currentParsePos + 1; i < paramList.size(); i++) {
							sCurrentParsePosValue = sCurrentParsePosValue + " " + paramList.get(i).toString();
							tempParamList.add(paramList.get(i).toString());
							if (paramList.get(i).toString().charAt(paramList.get(i).toString().length() - 1) == '\'') {
								endQuotePos = i;
								break;
							}
						}
						currentParsePos = endQuotePos + 1;
					} 
					else if (countCharOccurance(paramList.get(currentParsePos).toString(), '"') == 0 || countCharOccurance(paramList.get(currentParsePos).toString(), '"') == 2) 
					{				
						sCurrentParsePosValue = paramList.get(currentParsePos).toString();
						tempParamList.add(paramList.get(currentParsePos).toString());
						endQuotePos = currentParsePos;
						currentParsePos = endQuotePos + 1;
					} 
					else if (countCharOccurance(paramList.get(currentParsePos).toString(), '\'') == 0	|| countCharOccurance(paramList.get(currentParsePos).toString(), '\'') == 2) 
					{				
						sCurrentParsePosValue = paramList.get(currentParsePos).toString();
						tempParamList.add(paramList.get(currentParsePos).toString());
						endQuotePos = currentParsePos;
						currentParsePos = endQuotePos + 1;
					}
					if(endQuotePos == -1)
					{				
						throw new InvalidSyntaxError(quoteMissing);
					}
				}
				else
				{			
					throw new InvalidSyntaxError(invalidCommand);
				}				
			}
		}
		else
		{				
			sCurrentParsePosValue = "";
		}
		return sCurrentParsePosValue;
	}
	void exportSchema(StringList paramList) throws Exception {
		String schemaTypePattern = "";
		String schemaNamePattern = "";
		String modDate = "";
		String exportPath = "";

		/** SCHEMATYPE ***********STARTS*************/
		schemaTypePattern = processNextArgument(paramList);
		schemaTypePattern = schemaTypePattern.replaceAll("\"", "");
		schemaTypePattern = schemaTypePattern.replaceAll("\'", "");
		schemaTypePattern = schemaTypePattern.replace("|", "@");
		setType(schemaTypePattern.trim());
		/** SCHEMATYPE ***********ENDS**************/
		
		/** SCHEMANAME ***********STARTS*************/
		schemaNamePattern = processNextArgument(paramList);
		schemaNamePattern = schemaNamePattern.replaceAll("\"", "");
		schemaNamePattern = schemaNamePattern.replaceAll("\'", "");
		schemaNamePattern = schemaNamePattern.replace("|", "@");
		setName(schemaNamePattern.trim());
		/** SCHEMANAME ***********ENDS*************/
		
		if(currentParsePos > paramList.size()-1)
		{
			setModifiedFromDate("");
			exportPath = "";
		}
		else
		{
			modDate = processSchemaOptionalArgument(paramList,"moddate");
			modDate = modDate.replaceAll("\"", "");
			modDate = modDate.replaceAll("\'", "");
			if(!modDate.isEmpty()){				
				validateDate(modDate);
			}
			setModifiedFromDate(modDate.trim());
			exportPath = processSchemaOptionalArgument(paramList,"output");			
		}
		if(paramList.size() == tempParamList.size())
		{
			if(exportPath.isEmpty()){
				createSchemaModelerDirectory();
			}
		}
		else
		{
			throw new InvalidSyntaxError(invalidParameterCommand);
		}
	}
	
	String processPnoOptionalArgument(StringList paramList, String sArgument) throws Exception 
	{
		String sCurrentParsePosValue = "";
		int iOptionalArgPos = -1;
		for (int i = 0; i < paramList.size(); i++) 
		{
			if (paramList.get(i).toString().equalsIgnoreCase(sArgument)) 
			{
				iOptionalArgPos = i;
				break;
			}
		}
		if (iOptionalArgPos > - 1)
		{	
			if(sArgument.equalsIgnoreCase("output")){
				currentParsePos = iOptionalArgPos + 1;
				tempParamList.add(sArgument);
				if(currentParsePos > paramList.size()-1 || optionalPnoArgumentList.contains(paramList.get(currentParsePos).toString())){
					throw new InvalidSyntaxError(sArgument+" can not be empty.");
				}else{
					parseExportDir(paramList);
					sCurrentParsePosValue = getExportDirPath();
				}
			}else{
				tempParamList.add(sArgument);
				currentParsePos = iOptionalArgPos + 1;
				int endQuotePos = -1;
				if (currentParsePos > paramList.size() - 1)
				{			
					throw new InvalidSyntaxError(sArgument+" can not be empty.");
				}		
				else if (!paramList.get(currentParsePos).toString().isEmpty()) 
				{
					if(optionalPnoArgumentList.contains(paramList.get(currentParsePos).toString())){
						throw new InvalidSyntaxError(sArgument+" can not be empty.");
					}
					if (paramList.get(currentParsePos).toString().charAt(0) == '"' && countCharOccurance(paramList.get(currentParsePos).toString(), '"') == 1 && paramList.size() > (currentParsePos + 1)) 
					{
						sCurrentParsePosValue = paramList.get(currentParsePos).toString();
						tempParamList.add(sCurrentParsePosValue);
						for (int i = currentParsePos + 1; i < paramList.size(); i++) {
							sCurrentParsePosValue = sCurrentParsePosValue + " " + paramList.get(i).toString();
							tempParamList.add(paramList.get(i).toString());
							if (paramList.get(i).toString().charAt(paramList.get(i).toString().length() - 1) == '"') {
								endQuotePos = i;
								break;
							}
						}
						currentParsePos = endQuotePos + 1;
					}
					else if (paramList.get(currentParsePos).toString().charAt(0) == '\'' && countCharOccurance(paramList.get(currentParsePos).toString(), '\'') == 1 && paramList.size() > (currentParsePos + 1)) 
					{				
						sCurrentParsePosValue = paramList.get(currentParsePos).toString();	
						tempParamList.add(sCurrentParsePosValue);
						for (int i = currentParsePos + 1; i < paramList.size(); i++) {
							sCurrentParsePosValue = sCurrentParsePosValue + " " + paramList.get(i).toString();
							tempParamList.add(paramList.get(i).toString());
							if (paramList.get(i).toString().charAt(paramList.get(i).toString().length() - 1) == '\'') {
								endQuotePos = i;
								break;
							}
						}
						currentParsePos = endQuotePos + 1;
					} 
					else if (countCharOccurance(paramList.get(currentParsePos).toString(), '"') == 0 || countCharOccurance(paramList.get(currentParsePos).toString(), '"') == 2) 
					{				
						sCurrentParsePosValue = paramList.get(currentParsePos).toString();
						tempParamList.add(paramList.get(currentParsePos).toString());
						endQuotePos = currentParsePos;
						currentParsePos = endQuotePos + 1;
					} 
					else if (countCharOccurance(paramList.get(currentParsePos).toString(), '\'') == 0	|| countCharOccurance(paramList.get(currentParsePos).toString(), '\'') == 2) 
					{				
						sCurrentParsePosValue = paramList.get(currentParsePos).toString();
						tempParamList.add(paramList.get(currentParsePos).toString());
						endQuotePos = currentParsePos;
						currentParsePos = endQuotePos + 1;
					}
					if(endQuotePos == -1)
					{				
						throw new InvalidSyntaxError(quoteMissing);
					}
				}
				else
				{			
					throw new InvalidSyntaxError(invalidCommand);
				}				
			}
		}
		else
		{				
			sCurrentParsePosValue = "";
		}
		return sCurrentParsePosValue;
	}
	
	void exportpno(StringList paramList) throws Exception {
		String schemaTypePattern = "";
		String schemaNamePattern = "";
		String exportPath = "";

		/** SCHEMATYPE ***********STARTS*************/
		schemaTypePattern = processNextArgument(paramList);
		schemaTypePattern = schemaTypePattern.replaceAll("\"", "");
		schemaTypePattern = schemaTypePattern.replaceAll("\'", "");
		schemaTypePattern = schemaTypePattern.replace("|", "@");
		setType(schemaTypePattern.trim());
		/** SCHEMATYPE ***********ENDS**************/
		
		/** SCHEMANAME ***********STARTS*************/
		schemaNamePattern = processNextArgument(paramList);
		schemaNamePattern = schemaNamePattern.replaceAll("\"", "");
		schemaNamePattern = schemaNamePattern.replaceAll("\'", "");
		schemaNamePattern = schemaNamePattern.replace("|", "@");
		setName(schemaNamePattern.trim());
		/** SCHEMANAME ***********ENDS*************/
		
		if(currentParsePos > paramList.size()-1)
		{
			exportPath = "";
		}
		else
		{
			exportPath = processSchemaOptionalArgument(paramList,"output");			
		}
		if(paramList.size() == tempParamList.size())
		{
			if(exportPath.isEmpty()){
				createSchemaModelerDirectory();
			}
		}
		else
		{
			throw new InvalidSyntaxError(invalidParameterCommand);
		}
	}
	
	String processCompareOptionalArgument(StringList paramList, String sArgument) throws Exception 
	{
		String sCurrentParsePosValue = "";
		int iOptionalArgPos = -1;
		for (int i = 0; i < paramList.size(); i++) 
		{
			if (paramList.get(i).toString().equalsIgnoreCase(sArgument)) 
			{
				iOptionalArgPos = i;
				break;
			}
		}
		if (iOptionalArgPos > - 1)
		{	
			if(sArgument.equalsIgnoreCase(SchemaConstants.COMPARE_SOURCE) || sArgument.equalsIgnoreCase(SchemaConstants.COMPARE_TARGET)){
				currentParsePos = iOptionalArgPos + 1;
				tempParamList.add(sArgument);
				if(currentParsePos > paramList.size()-1 || optionalSchemaArgumentList.contains(paramList.get(currentParsePos).toString())){
					throw new InvalidSyntaxError(sArgument+" can not be empty.");
				}else{
					parseExportDir(paramList);
					sCurrentParsePosValue = getExportDirPath();
				}
			}else{
				tempParamList.add(sArgument);
				currentParsePos = iOptionalArgPos + 1;
				int endQuotePos = -1;
				if (currentParsePos > paramList.size() - 1)
				{			
					throw new InvalidSyntaxError(sArgument+" can not be empty.");
				}		
				else if (!paramList.get(currentParsePos).toString().isEmpty()) 
				{
					if(optionalSchemaArgumentList.contains(paramList.get(currentParsePos).toString())){
						throw new InvalidSyntaxError(sArgument+" can not be empty.");
					}
					if (paramList.get(currentParsePos).toString().charAt(0) == '"' && countCharOccurance(paramList.get(currentParsePos).toString(), '"') == 1 && paramList.size() > (currentParsePos + 1)) 
					{
						sCurrentParsePosValue = paramList.get(currentParsePos).toString();
						tempParamList.add(sCurrentParsePosValue);
						for (int i = currentParsePos + 1; i < paramList.size(); i++) {
							sCurrentParsePosValue = sCurrentParsePosValue + " " + paramList.get(i).toString();
							tempParamList.add(paramList.get(i).toString());
							if (paramList.get(i).toString().charAt(paramList.get(i).toString().length() - 1) == '"') {
								endQuotePos = i;
								break;
							}
						}
						currentParsePos = endQuotePos + 1;
					}
					else if (paramList.get(currentParsePos).toString().charAt(0) == '\'' && countCharOccurance(paramList.get(currentParsePos).toString(), '\'') == 1 && paramList.size() > (currentParsePos + 1)) 
					{				
						sCurrentParsePosValue = paramList.get(currentParsePos).toString();	
						tempParamList.add(sCurrentParsePosValue);
						for (int i = currentParsePos + 1; i < paramList.size(); i++) {
							sCurrentParsePosValue = sCurrentParsePosValue + " " + paramList.get(i).toString();
							tempParamList.add(paramList.get(i).toString());
							if (paramList.get(i).toString().charAt(paramList.get(i).toString().length() - 1) == '\'') {
								endQuotePos = i;
								break;
							}
						}
						currentParsePos = endQuotePos + 1;
					} 
					else if (countCharOccurance(paramList.get(currentParsePos).toString(), '"') == 0 || countCharOccurance(paramList.get(currentParsePos).toString(), '"') == 2) 
					{				
						sCurrentParsePosValue = paramList.get(currentParsePos).toString();
						tempParamList.add(paramList.get(currentParsePos).toString());
						endQuotePos = currentParsePos;
						currentParsePos = endQuotePos + 1;
					} 
					else if (countCharOccurance(paramList.get(currentParsePos).toString(), '\'') == 0	|| countCharOccurance(paramList.get(currentParsePos).toString(), '\'') == 2) 
					{				
						sCurrentParsePosValue = paramList.get(currentParsePos).toString();
						tempParamList.add(paramList.get(currentParsePos).toString());
						endQuotePos = currentParsePos;
						currentParsePos = endQuotePos + 1;
					}
					if(endQuotePos == -1)
					{				
						throw new InvalidSyntaxError(quoteMissing);
					}
				}
				else
				{			
					throw new InvalidSyntaxError(invalidCommand);
				}				
			}
		}
		else
		{				
			sCurrentParsePosValue = "";
		}
		return sCurrentParsePosValue;
	}
	private void compareCommand(StringList paramList2) throws Exception{
		
		String compareTypePattern = "";
		String sourcePath = "";
		String targetPath = "";

		/** COMPARETYPE ***********STARTS*************/
	//	compareTypePattern = processNextArgument(paramList);
		compareTypePattern = compareTypePattern.replaceAll("\"", "");
		compareTypePattern = compareTypePattern.replaceAll("\'", "");
	//	setCompareType(compareTypePattern.trim());
		/** COMPARETYPE ***********ENDS**************/
			
		if(currentParsePos > paramList.size()-1) {
			//No source and target path exist
		} else {
			sourcePath = processCompareOptionalArgument(paramList,SchemaConstants.COMPARE_SOURCE);	
			setSourcePath(sourcePath);
			if(sourcePath.isEmpty()) {
				
			} 
			if(currentParsePos > paramList.size()-1) {
				
			} else {
				targetPath = processCompareOptionalArgument(paramList,SchemaConstants.COMPARE_TARGET);	
				setTargetPath(targetPath);
			}
		}
		if(paramList.size() == tempParamList.size()) {
		} else {
			throw new InvalidSyntaxError(invalidParameterCommand);
		}
	}

	/**
	 * For exporting all.
	 * @param paramList
	 * @throws Exception
	 */
	String processAllOptionalArgument(StringList paramList, String sArgument) throws Exception 
	{
		String sCurrentParsePosValue = "";
		int iOptionalArgPos = -1;
		for (int i = 0; i < paramList.size(); i++) 
		{
			if (paramList.get(i).toString().equalsIgnoreCase(sArgument)) 
			{
				iOptionalArgPos = i;
				break;
			}
		}
		if (iOptionalArgPos > - 1)
		{	
			if(sArgument.equalsIgnoreCase("output")){
				currentParsePos = iOptionalArgPos + 1;
				tempParamList.add(sArgument);
				if(currentParsePos > paramList.size()-1 || optionalSchemaArgumentList.contains(paramList.get(currentParsePos).toString())){
					throw new InvalidSyntaxError(sArgument+" can not be empty.");
				}else{
					parseExportDir(paramList);
					sCurrentParsePosValue = getExportDirPath();
				}
			}else{
				tempParamList.add(sArgument);
				currentParsePos = iOptionalArgPos + 1;
				int endQuotePos = -1;
				if (currentParsePos > paramList.size() - 1)
				{			
					throw new InvalidSyntaxError(sArgument+" can not be empty.");
				}		
				else if (!paramList.get(currentParsePos).toString().isEmpty()) 
				{
					if(optionalSchemaArgumentList.contains(paramList.get(currentParsePos).toString())){
						throw new InvalidSyntaxError(sArgument+" can not be empty.");
					}
					if (paramList.get(currentParsePos).toString().charAt(0) == '"' && countCharOccurance(paramList.get(currentParsePos).toString(), '"') == 1 && paramList.size() > (currentParsePos + 1)) 
					{
						sCurrentParsePosValue = paramList.get(currentParsePos).toString();
						tempParamList.add(sCurrentParsePosValue);
						for (int i = currentParsePos + 1; i < paramList.size(); i++) {
							sCurrentParsePosValue = sCurrentParsePosValue + " " + paramList.get(i).toString();
							tempParamList.add(paramList.get(i).toString());
							if (paramList.get(i).toString().charAt(paramList.get(i).toString().length() - 1) == '"') {
								endQuotePos = i;
								break;
							}
						}
						currentParsePos = endQuotePos + 1;
					}
					else if (paramList.get(currentParsePos).toString().charAt(0) == '\'' && countCharOccurance(paramList.get(currentParsePos).toString(), '\'') == 1 && paramList.size() > (currentParsePos + 1)) 
					{				
						sCurrentParsePosValue = paramList.get(currentParsePos).toString();	
						tempParamList.add(sCurrentParsePosValue);
						for (int i = currentParsePos + 1; i < paramList.size(); i++) {
							sCurrentParsePosValue = sCurrentParsePosValue + " " + paramList.get(i).toString();
							tempParamList.add(paramList.get(i).toString());
							if (paramList.get(i).toString().charAt(paramList.get(i).toString().length() - 1) == '\'') {
								endQuotePos = i;
								break;
							}
						}
						currentParsePos = endQuotePos + 1;
					} 
					else if (countCharOccurance(paramList.get(currentParsePos).toString(), '"') == 0 || countCharOccurance(paramList.get(currentParsePos).toString(), '"') == 2) 
					{				
						sCurrentParsePosValue = paramList.get(currentParsePos).toString();
						tempParamList.add(paramList.get(currentParsePos).toString());
						endQuotePos = currentParsePos;
						currentParsePos = endQuotePos + 1;
					} 
					else if (countCharOccurance(paramList.get(currentParsePos).toString(), '\'') == 0	|| countCharOccurance(paramList.get(currentParsePos).toString(), '\'') == 2) 
					{				
						sCurrentParsePosValue = paramList.get(currentParsePos).toString();
						tempParamList.add(paramList.get(currentParsePos).toString());
						endQuotePos = currentParsePos;
						currentParsePos = endQuotePos + 1;
					}
					if(endQuotePos == -1)
					{				
						throw new InvalidSyntaxError(quoteMissing);
					}
				}
				else
				{			
					throw new InvalidSyntaxError(invalidCommand);
				}				
			}
		}
		else
		{				
			sCurrentParsePosValue = "";
		}
		return sCurrentParsePosValue;
	}	
	
	/**
	 * Added for export all.
	 * @param paramList
	 * @throws Exception
	 */
	void exportAll(StringList paramList) throws Exception {

		String exportPath = "";	
		if(currentParsePos > paramList.size()-1)
		{
			exportPath = "";
		}
		else
		{
			exportPath = processAllOptionalArgument(paramList,"output");			
		}
		if(paramList.size() == tempParamList.size())
		{
			if(exportPath.isEmpty()){
				createSchemaModelerDirectory();
			}
		}
		else
		{
			throw new InvalidSyntaxError(invalidParameterCommand);
		}
	}
	
	// method to parse the command
	public void parse() {
		init();
		File tempPath;

		try {
			paramList = split(command, " ");
			paramList = removeSpace(paramList);
			tempParamList = new StringList();
			if (command.isEmpty()) {
				//setbError(false);				
				throw new Exception(emptyCommand);
			}
			if (paramList.get(currentParsePos).toString().equalsIgnoreCase("path")) {
				currentParsePos++;

				pathCommand(paramList);
			} else if (paramList.get(currentParsePos).toString().equalsIgnoreCase("clrpath")) {
				setCurrentPath(new String());
				System.out.print("Path cleared");
			} 	
			else if (paramList.get(currentParsePos).toString().equalsIgnoreCase(SchemaConstants.COMPARE_COMMAND)) {
					currentParsePos++;
					//setExportType("schema");
					setAction(SchemaConstants.COMPARE_COMMAND);
					tempParamList.add(SchemaConstants.COMPARE_COMMAND);
					compareCommand(paramList);
					System.out.println("");
				}
			else if (paramList.get(currentParsePos).toString().equalsIgnoreCase("import")) {
				currentParsePos++;
				setAction("import");
				importCommand(paramList);
			} else if (paramList.get(currentParsePos).toString().toLowerCase().equalsIgnoreCase("help")) {
				currentParsePos++;
				setAction("help");
				if(currentParsePos <= paramList.size()-1)
				{
					switch (paramList.get(currentParsePos).toString().toLowerCase()) {
					case "export":
						currentParsePos++;
						if(currentParsePos <= paramList.size()-1)
						{							
							switch (paramList.get(currentParsePos).toString().toLowerCase()) {
							case "bus":SchemaErrorHandler.displayUsageForExportBus();break;
							case "schema":SchemaErrorHandler.displayUsageForExportSchema();break;
							case "pno":SchemaErrorHandler.displayUsageForExportPnO();break;
							default:
								SchemaErrorHandler.displayUsageForExportSchema();
								SchemaErrorHandler.displayUsageForExportBus();
								SchemaErrorHandler.displayUsageForExportPnO();
								break;
							}
						}
						else
						{
							SchemaErrorHandler.displayUsageForExportSchema();
							SchemaErrorHandler.displayUsageForExportBus();
							SchemaErrorHandler.displayUsageForExportPnO();
						}
						break;
					case "import":SchemaErrorHandler.displayUsageForImport();break;
					default:
						SchemaErrorHandler.displayUsageForImport();
						SchemaErrorHandler.displayUsageForExportSchema();
						SchemaErrorHandler.displayUsageForExportBus();
						SchemaErrorHandler.displayUsageForExportPnO();
						break;
					}					
				}
				else
				{
					SchemaErrorHandler.displayUsageForImport();
					SchemaErrorHandler.displayUsageForExportSchema();
					SchemaErrorHandler.displayUsageForExportBus();
					SchemaErrorHandler.displayUsageForExportPnO();
				}
			}else if (paramList.get(currentParsePos).toString().equalsIgnoreCase("export")) {
				currentParsePos++;
				setAction("export");
				tempParamList.add("export");
				while (currentParsePos < paramList.size() - 1 && paramList.get(currentParsePos).toString().isEmpty())
					currentParsePos++;
				if (currentParsePos >= paramList.size()) {
					//setbError(false);
					throw new InvalidSyntaxError(parametersMissing);
				}
				else if (paramList.get(currentParsePos).toString().equalsIgnoreCase("schema")) {
					currentParsePos++;
					setExportType("schema");
					tempParamList.add("schema");
				  if(paramList.get(currentParsePos).toString().equalsIgnoreCase("all")) {
						currentParsePos++;
						setType("all");
						setName("*");
						setModifiedFromDate("");
						tempParamList.add("all");
						exportAll(paramList);
					}
				  else {
					exportSchema(paramList);
				  }
				} else if (paramList.get(currentParsePos).toString().equalsIgnoreCase("bus")) {
					currentParsePos++;
					setExportType("bus");
					tempParamList.add("bus");
					exportBus(paramList);
				} else if (paramList.get(currentParsePos).toString().equalsIgnoreCase("pno")) {
					currentParsePos++;
					setExportType("pno");
					tempParamList.add("pno");
					exportpno(paramList);
				} else {
					//setbError(false);
					setExportType("ALLExport");
					throw new InvalidSyntaxError(
							invalidCommand + " [" + paramList.get(currentParsePos).toString() + "] ");
				}
			}      else {
				//setbError(false);
				setAction("ALL");
				throw new InvalidSyntaxError(invalidCommand + " [" + paramList.get(currentParsePos).toString() + "] ");
			}
			setbError(false);
		} catch (Exception ex) {
			setbError(true);
			SchemaErrorHandler.errorHandler(getAction(), getExportType(), ex);
			//throw ex;
		}
		
	}

	public static StringList split(String paramString1, String paramString2)
	{
		 if (paramString1 == null) {
		   return new StringList(0);
		 }
		 if (paramString2 == null) {
		   paramString2 = "~";
		 }
		 StringTokenizer localStringTokenizer = new StringTokenizer(paramString1, paramString2, true);
		 StringList localStringList = new StringList(localStringTokenizer.countTokens());
		 Object localObject = null;	     
		 while (localStringTokenizer.hasMoreTokens())
		 {
		   String str = localStringTokenizer.nextToken(); 
		   if (paramString2.equals(str)) {
		     if (paramString2.equals(localObject)) {
		       localStringList.addElement("");
		     }
		   } else {
		     localStringList.addElement((String)str);
		   }	       
		   localObject = str;
		 }
		
		 if (paramString2.equals(localObject)) {
		   localStringList.addElement("");
		 }	     
		 return localStringList;
	}
	
	public void checkValidPath(String path) throws Exception
	{
		try {			
			if (isPathValid(path)) {
				setImportPath(path.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "").replaceAll("\'", ""));
				System.out.print("Import from path : " + getImportPath().replaceAll("\\\\\\\\", "\\\\") + " ? <Y/N>");
				
				Scanner sc = new Scanner(System.in);
				char confirmation = sc.nextLine().charAt(0);
				if (confirmation == 'y' || confirmation == 'Y') {
					 System.out.print("Import Successful.");
			/*	//-----------for CMD progress line ---->>>>>
					char[] animationChars = new char[]{'|', '/', '-', '\\'};
					for (int i = 0; i <= 100; i++) {
			            System.out.print("Importing: " + i + "% " + animationChars[i % 4] + "\r");

			            try {
			                Thread.sleep(200);
			            } catch (InterruptedException e) {
			                e.printStackTrace();
			            }
			        }
					System.out.println("Importing: Done!          ");
				//-----------for CMD progress line ---->>>>>	
			*/		
					
				} else {
					throw new InvalidPathError("Import aborted by user.");
				}
			} else{
				throw new InvalidPathError(invalidPath);
			}
		} catch (StringIndexOutOfBoundsException e) {
			throw new InvalidPathError(wrongImportInputConfirmation);
		}
	}
	public static StringList removeSpace(StringList slParamList)
	{
		StringList slNewParamList = new StringList();
		for (int i = 0; i < slParamList.size(); i++) {
			if(!slParamList.get(i).toString().isEmpty()){
				slNewParamList.add(slParamList.get(i).toString());
			}
		}
		return slNewParamList;
	}
	
	public void validateDate(String sDate) throws Exception
	{
		try {
			 DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
			 dateFormat.setLenient(false);
			 Date parse = dateFormat.parse(sDate);
			 sDate = dateFormat.format(parse);
		} catch (IllegalArgumentException e) {
			throw new InvalidSyntaxError(sDate+ "" +wrongInputDateFormat);
		} catch (ParseException e) {
			throw new InvalidSyntaxError(sDate+ "" +wrongInputDateFormat);
		} catch (Exception e) {
			throw new InvalidSyntaxError(sDate+ "" +wrongInputDateFormat);
		}
	}

	public void createSchemaModelerDirectory() {
	//get a current time stamp
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_hh_mm_ss");
		Date date = new Date();
		String sFileName = "SchemaModeler_"+dateFormat.format(date);
		Path path = Paths.get("C:\\temp\\"+sFileName);
		String strPath = path.toString();
		setExportPath((strPath).replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", ""));
		//setExportDirPath(strPath);
		//if directory exists?
		if (!Files.exists(path)) {
			try {
				Files.createDirectories(path);
			} catch (IOException e) {
				//fail to create directory
				e.printStackTrace();
			}
		}
	}
}
