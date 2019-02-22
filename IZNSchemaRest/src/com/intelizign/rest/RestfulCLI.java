package com.intelizign.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.spi.LoggerRepository;

import com.dassault_systemes.platform.restServices.RestService;
import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.izn.schemamodeler.SchemaBusExport;
import com.izn.schemamodeler.SchemaCompare;
import com.izn.schemamodeler.SchemaExport;
import com.izn.schemamodeler.SchemaFactoryCompare;
import com.izn.schemamodeler.SchemaFactoryExport;
import com.izn.schemamodeler.SchemaFactoryLoader;
import com.izn.schemamodeler.SchemaLoader;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.SchemaModelerConstants;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.json.JSONObject;

import matrix.util.StringList;


@SuppressWarnings("unused")
public class RestfulCLI extends RestService {
	
	final static String[] _aArryExportALLAdmin = { "association", "format", "group", "inquiry", "policy", "form", "relationship",
			"attribute", "type", "role", "interface", "dimension", "program", "channel", "command", "menu",
			"page", "table", "portal","vault","index","store"};
	final static String[] _aArryImportALLAdmin = { "association", "format", "group", "inquiry", "policy", "form", "relationship",
			"attribute", "type", "role", "interface", "dimension", "program", "channel", "command", "menu","page", "table", "portal", "businessobject", "connection", "webform","vault","index","store"};
	String sExportObjectsForAll = "eService Number Generator|eService Object Generator|eService Trigger Program Parameters|MCADInteg-GlobalConfig";
	String serverURL;
	String strSchemaExportPath = "";
	SCMConfigProperty scmConfigProperty = null;
	String sExportConnectionPath = "";
	public static Logger logger = Logger.getLogger(RestfulCLI.class);
	
	@GET
	@Path("/RestfulCLI")
	public Response getRequest(@javax.ws.rs.core.Context HttpServletRequest req) throws Exception {
		matrix.db.Context context = null;
		JSONObject params = getParamsJSONObject(req);
		String serverURL = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort()
				+ req.getContextPath();
		String sAction = "";
		String sDoneMessage = "";
		String sVersion = "";
		boolean sVersionCheck=true;
		Properties props = new Properties();
		props.load(getClass().getResourceAsStream("/log4j.properties"));
		PropertyConfigurator.configure(props);
		try {
			scmConfigProperty = new SCMConfigProperty();
			scmConfigProperty.setSchemaSeperator((String) req.getParameter("schemaSeperator"));
			scmConfigProperty.setLogEverything((String) req.getParameter("logEverything"));
			String sUser = (String) req.getParameter("user");
			String sPassword = (String) req.getParameter("pass");
			context = getContext(serverURL, sUser, sPassword);
			sAction = params.get("action").toString();
			sVersion = params.get("version").toString();
			sVersionCheck = getVersion(context,sVersion);
			
			if(sVersionCheck) 
			{
				if (sAction.equalsIgnoreCase("export")) {
					sDoneMessage = "Export Done";
					strSchemaExportPath = params.get("exportPath").toString();
					createExportFolders(strSchemaExportPath);
					strSchemaExportPath = strSchemaExportPath+"//Logs//SchemaExportDone.log";
		            File logFile = new File(strSchemaExportPath);
		            if (logFile.exists()) {
		            	logFile.delete();
		            }
		            createEmptyLogFile(strSchemaExportPath);	            
					resetLogConfiguration();
					startLogger(strSchemaExportPath);
					exportData(context, params.toString(), new String());
				} else if (sAction.equalsIgnoreCase("import")) {
					sDoneMessage = "Import Done";
					List<String> fileNames = new ArrayList<>(Arrays.asList(req.getParameter("fileNames").split(",")));
					List<String> schemaNames = new ArrayList<>(Arrays.asList(req.getParameter("schemaNames").split(",")));
					String strImportPath = (String)fileNames.get(0);
					strImportPath = strImportPath.replace("@", "\\");
					strImportPath = strImportPath.replace("$SPACE$", " ");
					File localFile1 = new File(strImportPath);
					localFile1 = new File(localFile1.getAbsolutePath()); 
					strImportPath = localFile1.getParent();
					localFile1 = new File(strImportPath);
					strImportPath = localFile1.getParent()+"//Logs//SchemaImportDone.log";
		            File logFile = new File(strImportPath);
		            if (logFile.exists()) {
		            	logFile.delete();
		            }
		            createEmptyLogFile(strImportPath);		            
					resetLogConfiguration();
					startLogger(strImportPath);					
					List listAllSchema = new ArrayList(Arrays.asList(_aArryImportALLAdmin));				
				    SchemaFactoryLoader scl = new SchemaFactoryLoader();
					String sSchemaName = "";
					for (int i = 0; i < fileNames.size(); i++) {
						String strFileName = fileNames.get(i); // added to solve import issue
						strFileName = strFileName.replace("@", "\\");
						strFileName = strFileName.replace("$SPACE$", " ");
						sSchemaName = (String)schemaNames.get(i).trim();
						if(listAllSchema.contains(sSchemaName)){
							logger.info("**************IMPORTING "+sSchemaName.toUpperCase()+" STARTS**************");
							SchemaLoader ss = scl.getSchemaLoader(sSchemaName);
							ss.loadSchema(context, sSchemaName, strFileName, logger, scmConfigProperty);
							logger.info("**************IMPORTING "+sSchemaName.toUpperCase()+" ENDS**************");
						}
					}
				} else if (sAction.equalsIgnoreCase("compare")) {
					sDoneMessage = "Compare Done";
					List<String> fileNames_Source = new ArrayList<>(Arrays.asList(req.getParameter("Source_FileNames").split(",")));
					List<String> schemaNames_Source = new ArrayList<>(Arrays.asList(req.getParameter("Source_SchemaNames").split(",")));
					List<String> fileNames_Target = new ArrayList<>(Arrays.asList(req.getParameter("Target_FileNames").split(",")));
					List<String> schemaNames_Target = new ArrayList<>(Arrays.asList(req.getParameter("Target_SchemaNames").split(",")));
					String sCompare_Source_Path = (String)fileNames_Source.get(0);
					sCompare_Source_Path = sCompare_Source_Path.replace("@", "\\");
					sCompare_Source_Path = sCompare_Source_Path.replace("$SPACE$", " ");
					String sCompare_Target_Path = (String)fileNames_Target.get(0);
					sCompare_Target_Path = sCompare_Target_Path.replace("@", "\\");
					sCompare_Target_Path = sCompare_Target_Path.replace("$SPACE$", " ");
					
					File fileFolder = new File(sCompare_Source_Path);
					fileFolder = new File(fileFolder.getParent());
					fileFolder = new File(fileFolder.getParent());
					fileFolder = new File(fileFolder.getParent()+"\\Output");
					
					String compareFolder = fileFolder.getPath();
					String strLogPath = compareFolder+"//Logs//SchemaCompareDone.log";
		            File logFile = new File(strLogPath);
		            if (logFile.exists()) {
		            	logFile.delete();
		            }
					createExportFolders(compareFolder);
		            createEmptyLogFile(strLogPath);		            
					resetLogConfiguration();
					startLogger(strLogPath);
					if(!(fileFolder.exists() && fileFolder.isDirectory())){
						fileFolder.mkdir();
					} 
					List listAllSchema = new ArrayList(Arrays.asList(_aArryImportALLAdmin));				
				    SchemaFactoryLoader scl = new SchemaFactoryLoader();
					String sSchemaName = "";
					if(schemaNames_Source.size() == schemaNames_Target.size())
					{						
						for (int i = 0; i < fileNames_Source.size(); i++) {
							String strFileName = fileNames_Source.get(i);
							strFileName = strFileName.replace("@", "\\");
							strFileName = strFileName.replace("$SPACE$", " ");
							sSchemaName = (String)schemaNames_Source.get(i).trim();
							String strFileName_Target = fileNames_Target.get(i);
							strFileName_Target = strFileName_Target.replace("@", "\\");
							strFileName_Target = strFileName_Target.replace("$SPACE$", " ");
							sSchemaName = (String)schemaNames_Target.get(i).trim();
							if(sSchemaName.equalsIgnoreCase("vault")||sSchemaName.equalsIgnoreCase("index")||sSchemaName.equalsIgnoreCase("store"))
							{
							}
							else {
							if(listAllSchema.contains(sSchemaName)){
								logger.info("**************COMPARE "+sSchemaName.toUpperCase()+" STARTS**************");
								scmConfigProperty.setFirstFilePath(strFileName);
								scmConfigProperty.setSecondFilePath(strFileName_Target);
								scmConfigProperty.setCompareFolder(compareFolder);
								SchemaFactoryCompare sfc = new SchemaFactoryCompare();
								SchemaCompare sc = sfc.getSchemaCompare(sSchemaName);
								sc.compareSchema(context, sSchemaName, logger, scmConfigProperty);
								logger.info("**************COMPARE "+sSchemaName.toUpperCase()+" ENDS**************");
							}
						  }
						}					
					}
					else
					{	
						List listNotExistSchema = new ArrayList();
						for (int i = 0; i < schemaNames_Source.size(); i++) {						
							if(!schemaNames_Target.contains((String)schemaNames_Source.get(i))){
								listNotExistSchema.add((String)schemaNames_Source.get(i));
							}
						}
						throw new Exception("Following files are not available : "+listNotExistSchema);
					}					
				}
			}
			else
			{				
				sDoneMessage="Please correct 3DExperience Platform version in config.properties";
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			if(sAction.equalsIgnoreCase("export")){
				sDoneMessage = "Export Failed";				
			}else if(sAction.equalsIgnoreCase("import")){
				sDoneMessage = "Import Failed";		
			}else if(sAction.equalsIgnoreCase("compare")){
				sDoneMessage = "Compare Failed";		
			}
			//return Response.serverError().entity(new String("{\"status\":\"error\", \"message\":\"" + sDoneMessage + "\"}")).build();
		} 
		return Response.ok(sDoneMessage, "application/json").build();
	}
	
	private void createExportFolders(String strSchemaExportPath) throws Exception{
		File fExportPath = null;
		try {
			for (String folder : SchemaModelerConstants._folders) {				
				fExportPath = new File(strSchemaExportPath + "\\" + folder);
				if(!(fExportPath.exists() && fExportPath.isDirectory())) {
					fExportPath.mkdir();
				}
			}
		} catch (Exception e) {
			throw e;
		}
	}
	public static void startLogger(String filePath) throws Exception {
     
        String PATTERN = "%X{USER} %d{HH:mm:ss} %-5p - %m (%c{1}:%L) %n";
        RollingFileAppender fileAppender;
        try {
            fileAppender = new RollingFileAppender(new PatternLayout(PATTERN), filePath);
        } catch (IOException e) {
            throw new Exception("Failed to initialise file appender. Error:" + e.getMessage(), e);
        }
        fileAppender.setMaxFileSize("25MB");
        fileAppender.setName("FileLogger");
        fileAppender.setEncoding("UTF-8");
        fileAppender.activateOptions();

        Level loggingLevel = Level.ALL;

       Logger.getRootLogger().addAppender(fileAppender);

    }

	public static void createEmptyLogFile(String filePath) {
        if (filePath != null && !filePath.isEmpty()) {
            File logFile = new File(filePath);
            if (!logFile.exists()) {
                logFile.getParentFile().mkdirs();
            }
            try {
                PrintWriter writer = new PrintWriter(logFile);
                writer.print("");
                writer.close();
            } catch (IOException e) {
                System.out.println("Problem with debug log file location. Error: " + e.getMessage());
            }
        }
    }
	
	public static void resetLogConfiguration() {
       
        Logger log = Logger.getRootLogger();
        if (log != null) {
            LoggerRepository logRepo = log.getLoggerRepository();
            if (logRepo != null) {
                logRepo.resetConfiguration();
            }
        }
    }
	 
	public matrix.db.Context getContext(String serverURL, String user, String password) throws FrameworkException {
		matrix.db.Context eMatrixContext = null;
		try {
			eMatrixContext = new matrix.db.Context(serverURL);
			eMatrixContext.setUser(user);
			eMatrixContext.setPassword(password);
			eMatrixContext.connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return eMatrixContext;
	}
	
	public boolean getVersion(matrix.db.Context context, String sVersion) throws FrameworkException {
		boolean flag=true;
		try {
			String sMQLResult = MqlUtil.mqlCommand(context, "$1",new String[] { "version" });
			if (sMQLResult.contains(sVersion)) {
				flag=true;
			}
			else {
				flag=false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
		
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private JSONObject getParamsJSONObject(HttpServletRequest req) throws Exception {
		Map paramMap = req.getParameterMap();
		Set eSet = paramMap.entrySet();
		Iterator eSetItr = eSet.iterator();
		JSONObject params = new JSONObject();
		while (eSetItr.hasNext()) {
			Map.Entry<String, String[]> entry = (Map.Entry<String, String[]>) eSetItr.next();
			String key = entry.getKey();
			String value = entry.getValue()[0];
			if (key.equalsIgnoreCase("exportPath")) {
				value = value.replace("@", "\\");
				value = value.replace("$SPACE$", " ");
			} else if (key.equalsIgnoreCase("type") || key.equalsIgnoreCase("pattern")
					|| key.equalsIgnoreCase("revision") || key.equalsIgnoreCase("rel")
					|| key.equalsIgnoreCase("vault")) {
				value = value.replace("@", " ");
			} else if (key.equalsIgnoreCase("modifiedDate")) {
				value = value.replace("@", "/");
			}
			params.put(key, value);
		}
		return params;
	}

	public void exportData(matrix.db.Context context, String inputJSON, String anythignExtra) throws Exception {
		try {
			Map<String, Object> hmJSONInput = new ObjectMapper().readValue(inputJSON, HashMap.class);
			String sExportType = (String) hmJSONInput.get("exportType");
			String sVersion = (String) hmJSONInput.get("version");
			String sExportPath = (String) hmJSONInput.get("exportPath");
			
			if (UIUtil.isNotNullAndNotEmpty(sExportType) && sExportType.equalsIgnoreCase("schema")) {
				logger.info("**************EXPORTING ADMIN OBJECT STARTS**************");
				String sExportAdmintype = (String) hmJSONInput.get("admintype");
				String sPattern = (String) hmJSONInput.get("pattern");
				String sDate = (String) hmJSONInput.get("modifiedDate");				
				if ("*".equals(sExportAdmintype)) {
					exportSchemaAll(context, _aArryExportALLAdmin, sPattern, sDate, sExportPath, sVersion);
				} else if(sExportAdmintype.equalsIgnoreCase("all")) {
					exportSchemaAll(context,_aArryExportALLAdmin,"*","",sExportPath, sVersion);
					exportBusData(context,sExportObjectsForAll,"*","*","*","*","*","*", sExportPath,sVersion);					
				} else {
					String[] arrOfStr = sExportAdmintype.split("@");
					exportSchemaAll(context, arrOfStr, sPattern, sDate, sExportPath, sVersion);
				}
				logger.info("**************EXPORTING ADMIN OBJECT ENDS**************");
			} else if (UIUtil.isNotNullAndNotEmpty(sExportType) && sExportType.equalsIgnoreCase("bus")) {
				logger.info("**************EXPORTING BUSINESS OBJECT STARTS**************");
				String sType = (String) hmJSONInput.get("type");
				String sNamePattern = (String) hmJSONInput.get("pattern");
				String sRevPattern = (String) hmJSONInput.get("revision");
				String sExpandRel = (String) hmJSONInput.get("rel");
				String sExact = (String) hmJSONInput.get("exact");
				String sVault = (String) hmJSONInput.get("vault");
				String sFile = (String) hmJSONInput.get("file");
				sExportConnectionPath = sExportPath;
				exportBusData(context, sType, sNamePattern, sRevPattern, sExpandRel, sExact, sVault, sFile, sExportPath,
						sVersion);
				logger.info("**************EXPORTING BUSINESS OBJECT ENDS**************");
			} else if (UIUtil.isNotNullAndNotEmpty(sExportType) && sExportType.equalsIgnoreCase("pno")) {
				logger.info("**************EXPORTING PNO OBJECT STARTS**************");
				String sAdminUser = (String) hmJSONInput.get("adminUser");
				String sAdmintype = (String) hmJSONInput.get("admintype");
				String sPattern = (String) hmJSONInput.get("pattern");
				String sAdminContext = (String) hmJSONInput.get("adminContext");
				String sAdminPassword = (String) hmJSONInput.get("adminPassword");
				String serverPath = (String) hmJSONInput.get("serverPath");
				String sURL = (String) hmJSONInput.get("sURL");
				exportPnOData(sAdminUser,sAdmintype,sPattern,sAdminContext,sAdminPassword,serverPath,sURL,sExportPath);
				logger.info("**************EXPORTING PNO OBJECT ENDS**************");
			}
		} catch (Exception e) {
			throw e;
		}
	}

	private void exportPnOData(String sAdminUser, String sAdmintype, String sPattern, String sAdminContext, String sAdminPassword, String serverPath, String sURL, String sExportPath) throws Exception {
		try {
			serverPath = serverPath.replace("@", "\\");
			sAdminContext=sAdminContext.replace("@", " ");
			Runtime rt = Runtime.getRuntime();
			File fExportPath = new File(sExportPath + "\\" +SchemaModelerConstants.FOLDER_NAME_Pno);
			if(!(fExportPath.exists() && fExportPath.isDirectory())) {
				fExportPath.mkdir();
			}
			sExportPath = fExportPath.getAbsolutePath();
			if (sAdmintype.equalsIgnoreCase("*") && sPattern.equalsIgnoreCase("*")) {
				Process process = rt.exec(new String[]{"cmd.exe", "/c", "start "+serverPath+"\\VPLMPosExport.bat -server " +sURL+ " -user " +sAdminUser+ " -password " +sAdminPassword+ " -context " + "\"" +  sAdminContext + "\"" +  " -all  -file \""+sExportPath+"\\POSEXP\""});
			    InputStream inputStream = process.getInputStream();
			    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			}
			if(!sAdmintype.isEmpty() && !sPattern.isEmpty()) {
				Process process = rt.exec(new String[]{"cmd.exe", "/c", "start "+serverPath+"\\VPLMPosExport.bat -server " +sURL+ " -user " +sAdminUser+ " -password " +sAdminPassword+ " -context " + "\"" +  sAdminContext + "\"" +  " -object " +sAdmintype+":"+sPattern+"@local -file \""+sExportPath+"\\POSEXP\""});
			    InputStream inputStream = process.getInputStream();
			    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			}
		}catch (Exception e) {
			throw e;
		}
	}
	
	

	

	
	private void exportAll(matrix.db.Context context, String[] sAdmin, String pattarn, String strDate, String strPath,
			String strVersion) throws Exception{
		List<String> lstEmpty = new ArrayList();
		try {
			for (String key : sAdmin) {
				exportSchemaData(context, key, lstEmpty, pattarn, strDate, strPath, strVersion);
			}
		} catch (Exception e) {
			throw e;
		}
	}

	private void exportSchemaData(matrix.db.Context context, String strAdminType, List<String> lstNameList,
			String strPattern, String strDate, String strPath, String strVersion) throws Exception{
		try {
			StringBuilder sbMQL = new StringBuilder();
			String strMQLResult = "";
			if (lstNameList.size() == 0) {				
				strPattern = strPattern.replace("|",",");
				if (UIUtil.isNullOrEmpty(strDate)) {
					if (strAdminType.equalsIgnoreCase("table")) {
						strMQLResult = MqlUtil.mqlCommand(context, "list $1 system $2",
								new String[] { strAdminType, strPattern });
					} else {
						strMQLResult = MqlUtil.mqlCommand(context, "list $1 $2",new String[] { strAdminType, strPattern });
					}
				} else if (strAdminType.equalsIgnoreCase("table")) {
					strMQLResult = MqlUtil.mqlCommand(context, "list $1 system modified after $2 $3",
							new String[] { strAdminType, strDate, strPattern });
				} else {
					strMQLResult = MqlUtil.mqlCommand(context, "list $1 modified after $2 $3",
							new String[] { strAdminType, strDate, strPattern });
				}
				BufferedReader localBufferedReader = new BufferedReader(new StringReader(strMQLResult));
				String strLine = "";
				List<String> lstData = new ArrayList();
				while ((strLine = localBufferedReader.readLine()) != null) {
					if (!(strLine.contains("Warning"))) {
						lstNameList.add(strLine);
					}
				}
			}
			if("association".equalsIgnoreCase(strAdminType)){
				if(!"*".equalsIgnoreCase(strPattern))
					lstNameList.clear();
			}
			if(strMQLResult != null && !"".equals(strMQLResult) && !lstNameList.isEmpty()){
				SchemaFactoryExport sfe = new SchemaFactoryExport();
				SchemaExport se = sfe.getSchemaXML(context, strAdminType);
				se.exportSchema(context, strAdminType, lstNameList, strPath, strVersion, logger, scmConfigProperty);
				lstNameList.clear();
			}
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * private : to export all Admin if option choose is Global Export all read
	 * admin array and processes one by one
	 * 
	 * @return void
	 * @author Ravi
	 * 
	 */

	private void exportSchemaAll(matrix.db.Context context, String[] sAdmin, String pattarn, String strDate,
			String strPath, String strVersion) throws Exception{
		List<String> lstEmpty = new ArrayList<String>();
		try {
			for (String key : sAdmin) {
				exportSchemaData(context, key, lstEmpty, pattarn, strDate, strPath, strVersion);
			}
		} catch (Exception e) {
			throw e;
		}

	}

	/**
	 * private : process the businessobject and invokes bean method to export
	 * data
	 * 
	 * @return void
	 * @author kannan
	 * 
	 */
	private void exportBusData(matrix.db.Context context, String sType, String sNamePattern, String sRevPattern,
			String sExpandRel, String sExact, String sVault, String sFile, String strPath, String strVersion) throws Exception{
		List<String> lstNameList = new ArrayList<String>();
		List<String> lstAllNameList = new ArrayList<String>();
		Set<String> setRels = new HashSet<String>();
		String strMQLResult = "";
		try {

			if (UIUtil.isNotNullAndNotEmpty(sType) && UIUtil.isNotNullAndNotEmpty(strPath)
					&& UIUtil.isNotNullAndNotEmpty(strVersion)) {
				List<String> typeList = FrameworkUtil.split(sType, "|");
				for (String type : typeList) {
					if (UIUtil.isNotNullAndNotEmpty(sExact) && UIUtil.isNotNullAndNotEmpty(sVault)
							&& sExact.equalsIgnoreCase("true")) {					
						strMQLResult = MqlUtil.mqlCommand(context, "temp query bus $1 $2 $3 notexpand vault $4 select $5 dump $6",
								new String[] { type, sNamePattern, sRevPattern, sVault.replace("|",","),"id","|" });
					} else if (UIUtil.isNotNullAndNotEmpty(sExact) && UIUtil.isNotNullAndNotEmpty(sVault)
							&& sExact.equalsIgnoreCase("false")) {

						strMQLResult = MqlUtil.mqlCommand(context, "temp query bus $1 $2 $3 vault $4 select $5 dump $6",
								new String[] { type, sNamePattern, sRevPattern, sVault.replace("|",","),"id","|" });
					} else if (UIUtil.isNotNullAndNotEmpty(sExact) && UIUtil.isNullOrEmpty(sVault) && sExact.equalsIgnoreCase("true")) {
						
						strMQLResult = MqlUtil.mqlCommand(context, "temp query bus $1 $2 $3 notexpand select $4 dump $5",
								new String[] { type, sNamePattern, sRevPattern,"id","|" });
					} else {
						strMQLResult = MqlUtil.mqlCommand(context, "temp query bus $1 $2 $3 select $4 dump $5",
								new String[] { type, sNamePattern, sRevPattern, "id", "|" });
					}
					if (UIUtil.isNotNullAndNotEmpty(strMQLResult)) {
						BufferedReader localBufferedReader = new BufferedReader(new StringReader(strMQLResult));
						String strLine = DomainConstants.EMPTY_STRING;
						List<String> lstData = new ArrayList<String>();
						while ((strLine = localBufferedReader.readLine()) != null) {
							StringList lineList = FrameworkUtil.split(strLine, "|");
							lstNameList.add((String) lineList.get(3));
						}
						SchemaFactoryExport sfe = new SchemaFactoryExport();
						SchemaBusExport se = sfe.getBusSchemaXML(context, "businessobject");
						se.exportBus(context, type, lstNameList, strPath, strVersion, sFile, sExpandRel, logger, scmConfigProperty);
						lstAllNameList.addAll(lstNameList);
						lstNameList.clear();
					}
				}
				if (UIUtil.isNotNullAndNotEmpty(sExpandRel) && lstAllNameList.size() > 0) {
					List<String> relList = new ArrayList<String>();
					if("*".trim().equalsIgnoreCase(sExpandRel)){
						for (String strObjectId : lstAllNameList) {							
							String sMQLResult = MqlUtil.mqlCommand(context, "print bus $1 select relationship dump $2",new String[] { strObjectId, "|" });
							relList.addAll(FrameworkUtil.split(sMQLResult, "|"));
						}
						setRels.addAll(relList);
						relList.clear();
						relList.addAll(setRels);
					}else{						
						relList = FrameworkUtil.split(sExpandRel, "|");	
					}
					List<String> listBusIdList =  null; 
					HashSet<String> hsBusIdList = new HashSet<String>();
					Map<String,Set<String>> mObjectMap = new HashMap<String,Set<String>>();
					Map<String,Set<String>> mTemp =  null;
					for (String rel : relList) {
						SchemaFactoryExport sfe = new SchemaFactoryExport();
						SchemaBusExport sre = sfe.getBusSchemaXML(context, "connection");
						mTemp = sre.exportRel(context, rel, lstAllNameList, strPath, strVersion, sFile, sExpandRel, logger, scmConfigProperty);
						mObjectMap.putAll(mTemp);
					}
					SchemaFactoryExport sfe = new SchemaFactoryExport();
					SchemaBusExport se = sfe.getBusSchemaXML(context, "businessobject");
					for (Map.Entry<String,Set<String>> entry : mObjectMap.entrySet())  
					{     
						lstAllNameList = new ArrayList<String>(entry.getValue());
						se.exportBus(context, entry.getKey(), lstAllNameList, strPath, strVersion, sFile, sExpandRel, logger, scmConfigProperty);
			    	} 
				}
			}
		} catch (Exception e) {
			throw e;
		}
		
	
		
		
	}
}
