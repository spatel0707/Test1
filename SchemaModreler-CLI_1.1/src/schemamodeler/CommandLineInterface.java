package schemamodeler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import com.matrixone.json.JSONObject;

public class CommandLineInterface
{
  static final String configFile = "config.properties";
  static String resourceURL;
  static JSONObject configurables;
  static String cliPrompt;
  final static String[] _aArryALLSchemas = { "association.xml","format.xml", "group.xml", "inquiry.xml", "policy.xml", "relationship.xml","attribute.xml", "type.xml", "policy.xml", "role.xml", "interface.xml", "dimension.xml", "program.xml", "channel.xml", "command.xml", "menu.xml","page.xml", "table.xml", "portal.xml","webform.xml","vault.xml","index.xml","store.xml" };
  final static String[] _aArryFolders = {"Pages", "Programs"};
  final static String[] _folders = {"SchemaObject", "BusinessObject", "Connection", "System", "Logs"};
  static List<String> listFileNames = null;
  static String schemaSeperator = "";
  static String user = "";
  static String pass = "";
  
  static
  {
    try
    {
      String currentDir = System.getProperty("user.dir");
      configurables = getConfigurables(currentDir + "\\config.properties");
      resourceURL = configurables.get("enoviaServerURL").toString();
      cliPrompt = configurables.get("commandLinePrompt").toString();
    }
    catch (Exception localException) {}
  }
  
	public static void main(String[] Args)
	{
		listFileNames = new ArrayList(Arrays.asList(_aArryALLSchemas));
		listFileNames.addAll(Arrays.asList(_aArryALLSchemas));
		JSONObject params = new JSONObject();
		Properties p = new Properties();
		String line = new String();
		String pattern = new String();
		String clPrompt = new String("CLI");
		if (cliPrompt != null) 
		{
		  clPrompt = cliPrompt;
		}
		int patternStartPos = 0;
		int patternEndPos = 0;
		long lineNo = 1L;
		System.out.println("Schema Modeler CLI(Command Line Interface) Tool");
		System.out.println("Copyright (c) 2018 Intelizign Engineering Services Pvt Ltd. - All rights reserved. \n");
				Parser parser = new Parser();
		try
		{
			String currentDir = System.getProperty("user.dir");
			configurables = getConfigurables(currentDir + "\\config.properties");
			String sURL = configurables.get("enoviaServerURL").toString();
			if ((sURL == null) || ("".equals(sURL))) {
				System.out.println("Please specify enoviaServerURL in config.properties");
			}
			resourceURL = sURL + "/resources/IZNSchemaModeler/RestfulCLI";
			user = configurables.get("user").toString();
			pass = configurables.get("password").toString();
			String version = configurables.get("enoviaVersion").toString();
			//String adminUser = configurables.get("adminUser").toString();
			//String adminPassword = configurables.get("adminPassword").toString();
			String adminContext = configurables.get("adminContext").toString();
			String serverPath = configurables.get("serverPath").toString();
			schemaSeperator = configurables.get("schemaSeperator").toString().trim();
			String logEverything = configurables.get("logEverything").toString().trim();
			if ((version == null) || ("".equals(version))) {
				System.out.println("Please specify enovia version in config.properties");
			}
			if ((user == null) || ("".equals(user)) || (pass == null)) {
				System.out.println("Please cross verify username and password in config.properties");
			}
			for (;;)
			{
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				System.out.print(clPrompt + " " + lineNo + " >");
				line = br.readLine();
				if (line.equalsIgnoreCase("quit")) 
				{
				  break;
				}
				if (!line.isEmpty())
				{
					parser.setCommand(line);
					parser.setadminContext(adminContext);
					parser.setserverPath(serverPath);

					parser.parse(); 
					params.put("user", user);
					params.put("pass", pass);
					params.put("version", version);
					params.put("schemaSeperator", schemaSeperator);
					params.put("logEverything", logEverything);
					String strExportPath = "";
					if (parser.getAction().equalsIgnoreCase("export"))
					{
						
						strExportPath = parser.getExportPath().replace("\\", "@");
						strExportPath = strExportPath.replace("\\", "@");
						strExportPath = strExportPath.replace(" ", "$SPACE$");
						if (parser.getExportType().equalsIgnoreCase("schema"))
						{
						  params.put("admintype", parser.getType());
						  params.put("pattern", parser.getName().replace(" ", "@"));
						  params.put("exportPath", strExportPath);
						  params.put("action", parser.getAction());
						  params.put("exportType", parser.getExportType());
						  params.put("modifiedDate", parser.getModifiedFromDate().replace("/", "@"));
						  if ( parser.getType().equalsIgnoreCase("all"))
							{	params.put("pattern", "*");					 
							}
						//-----------for CMD progress line ---->>>>>
						  char[] animationChars = new char[]{'|', '/', '-', '\\'};
							for (int i = 0; i <= 100; i++) {
					            System.out.print("Loading..: " + i + "% " + animationChars[i % 4] + "\r");

					            try {
					                Thread.sleep(300);
					            } catch (InterruptedException e) {
					                e.printStackTrace();
					            }
					        }
							System.out.println("Exporting Schema:.....          ");
						    System.out.print("\r");
						    System.out.print("wait...");
						    System.out.print("\r");
						//-----------for CMD progress line ---->>>>>
						}
						else if (parser.getExportType().equalsIgnoreCase("bus"))
						{
						  params.put("type", parser.getType().replace(" ", "@"));
						  params.put("pattern", parser.getName().replace(" ", "@"));
						  params.put("revision", parser.getRevision().replace(" ", "@"));
						  params.put("rel", parser.getExpandRel().replace(" ", "@"));
						  //params.put("exportPath", parser.getExportPath());//.replace("\\", "@"));
						  params.put("exportPath", strExportPath);
						  
						  params.put("action", parser.getAction());
						  params.put("exportType", parser.getExportType());
						  if (parser.getVault() != null && !"".equals(parser.getVault())){
							 params.put("vault", parser.getVault().replace(" ", "@"));
						  }
						  if (parser.getExact() != null && !"".equals(parser.getExact())){
							params.put("exact", parser.getExact());
						  }
						//-----------for CMD progress line ---->>>>>
						  char[] animationChars = new char[]{'|', '/', '-', '\\'};
							for (int i = 0; i <= 100; i++) {
					            System.out.print("Loading..: " + i + "% " + animationChars[i % 4] + "\r");

					            try {
					                Thread.sleep(300);
					            } catch (InterruptedException e) {
					                e.printStackTrace();
					            }
					        }
							System.out.println("Exporting Schema:.....          ");
						    System.out.print("\r");
						    System.out.print("wait...");
						    System.out.print("\r");
						//-----------for CMD progress line ---->>>>>
						}
						else if (parser.getExportType().equalsIgnoreCase("pno"))
						{

						  params.put("adminUser", user);
						  params.put("adminPassword", pass);
						  params.put("serverPath", parser.getserverPath());
						  params.put("adminContext", parser.getadminContext());	
						  params.put("admintype", parser.getType());
						  params.put("pattern", parser.getName().replace(" ", "@"));
						  params.put("exportPath", strExportPath);
						  params.put("action", parser.getAction());
						  params.put("exportType", parser.getExportType());
						  params.put("adminContext", parser.getadminContext().replace(" ", "@"));
						  params.put("serverPath", parser.getserverPath().replace("\\", "@"));
						  params.put("sURL", sURL);
						}
						
						if(!parser.isbError())
						restCall(resourceURL, params);
						
					}
					if(parser.getAction().equalsIgnoreCase("import"))
					{
						params.put("action","import");
						params.put("user",user);
						params.put("pass",pass);
						params.put("importtype",parser.getImportType());
						ArrayList<String> schemaNames = new ArrayList<String>();
						ArrayList<File> fileNames = new ArrayList<File>();
						File path = new File(parser.getImportPath());					 
						List<String> _listFolders = new ArrayList<>(Arrays.asList(_folders));
						File[] listFiles = path.listFiles();
						
						boolean bIsFolderStructure = true;
						for (File file : listFiles) {
							if(file.isDirectory() && _listFolders.contains(file.getName()))
							{
								File nextPath = new File(file.getPath());
								listFilesSchemas(nextPath, fileNames, schemaNames, parser.getImportType());													        
						    }
							else {
								bIsFolderStructure = false;
								break;
							}
						}
						if(bIsFolderStructure)
						{							
							if(!schemaNames.isEmpty() && !fileNames.isEmpty()){							
								String strFileNames = converttoParamsFileNames(fileNames);
								String strSchemaNames = converttoParams(schemaNames);
								strSchemaNames = strSchemaNames.replace(".xml", "");
								strFileNames = strFileNames.replace("\\", "@");
								params.put("fileNames", strFileNames);
								params.put("schemaNames", strSchemaNames);	
								if(!parser.isbError())
									restCall(resourceURL,params);
							}else{
								if(!parser.isbError())
									System.out.println("Required file for import not found in directory. Please add file and import.");
							}
						}
						else
						{
							System.out.println("Wrong SchemaModeler folder structure.");
						}
					}
					if(parser.getAction().equalsIgnoreCase(SchemaConstants.COMPARE_COMMAND))
					{
						params.put("action",parser.getAction());
						params.put("user",user);
						params.put("pass",pass);
						params.put("comparetype",parser.getCompareType());
						ArrayList<String> source_schemaNames = new ArrayList<String>();
						ArrayList<File> source_fileNames = new ArrayList<File>();
						File sourcePath = new File(parser.getSourcePath());					 
						File targetPath = new File(parser.getTargetPath());					 
						//path = new File(path.toString().replace("\\", "@"));
						List<String> _listFolders = new ArrayList<>(Arrays.asList(_folders));

						File[] listFiles = sourcePath.listFiles();
						for (File file : listFiles) {
							if(file.isDirectory() && _listFolders.contains(file.getName())){								
								listFilesForCompare(file, source_fileNames, source_schemaNames, parser.getCompareType());
							}
						}
						ArrayList<String> target_schemaNames = new ArrayList<String>();
						ArrayList<File> target_fileNames = new ArrayList<File>();
						
						listFiles = targetPath.listFiles();
						for (File file : listFiles) {
							if(file.isDirectory() && _listFolders.contains(file.getName())){								
								listFilesForCompare(file, target_fileNames, target_schemaNames, parser.getCompareType());
							}
						}
						if(!source_schemaNames.isEmpty() && !source_fileNames.isEmpty() && !target_schemaNames.isEmpty() && !target_fileNames.isEmpty()){							
							String strSchemaNames_Source = converttoParams(source_schemaNames).replace(".xml", "");
							String strFileNames_Source = converttoParamsFileNames(source_fileNames).replace("\\", "@");
							String strSchemaNames_Target = converttoParams(target_schemaNames).replace(".xml", "");
							String strFileNames_Target = converttoParamsFileNames(target_fileNames).replace("\\", "@");
							params.put(SchemaConstants.SOURCE_FILENAMES, strFileNames_Source);
							params.put(SchemaConstants.SOURCE_SCHEMANAMES, strSchemaNames_Source);	
							params.put(SchemaConstants.TARGET_FILENAMES, strFileNames_Target);
							params.put(SchemaConstants.TARGET_SCHEMANAMES, strSchemaNames_Target);	
							if(!parser.isbError())
							restCall(resourceURL,params);
						}else{
							if(!parser.isbError())
							System.out.println("Required file for compare not found in directory. Please add file and compare.");
						}
					}
				}
				lineNo += 1L;
				System.out.println("");
			}
			System.out.println("SchemaModeler CLI Terminated.");
		}
		catch (Exception ex)
		{
		  System.out.println(ex.getMessage());
		}
	}
  
	public static <E> String converttoParams (ArrayList<E> list) {
		String strList = "";
		strList = list.toString();
		strList = strList.replaceAll("\\[", "");
		strList = strList.replaceAll("\\]", "");
		strList = strList.replaceAll(" ", "");
		return strList;
	}
	public static <E> String converttoParamsFileNames (ArrayList<E> list) {
		String strList = "";
		strList = list.toString();
		strList = strList.replaceAll("\\[", "");
		strList = strList.replaceAll("\\]", "");
		strList = strList.replaceAll(", ", ",@");
		strList = strList.replace(" ", "$SPACE$");
		return strList;
	}

	public static void listFilesSchemas (File dir, ArrayList<File> fileNames, ArrayList<String> schemaNames, String importtype) {
	    
	    if(dir==null||dir.listFiles()==null){
	        return ;
	    }
		if("form".equalsIgnoreCase(importtype)){
			importtype = "webform";
		}
	    for (File entry : dir.listFiles()) {
			if("*".equalsIgnoreCase(importtype) && (listFileNames.contains(entry.getName()) || (entry.isFile() && entry.getName().startsWith("bo_")) || (entry.isFile() && entry.getName().startsWith("rel_"))))
	    	{
	    		if (entry.isFile()) {
	    			if (entry.getName().startsWith("bo_")) 
	    			{ schemaNames.add("businessobject");} 
	    			else if(entry.getName().startsWith("rel_")) 
	    			{schemaNames.add("connection");} 
	    			else {	 
	    				schemaNames.add(entry.getName());
	    			}    
	    			fileNames.add(entry);
	    		}
	    		else listFilesSchemas(entry, fileNames, schemaNames, importtype);
	    	}
			else if("bus".equalsIgnoreCase(importtype) && ((entry.isFile() && entry.getName().startsWith("bo_")) || (entry.isFile() && entry.getName().startsWith("rel_"))))
	    	{	    	
	    		if (entry.isFile()) {
	    			if (entry.getName().startsWith("bo_")) 
	    			{ schemaNames.add("businessobject");} 
	    			else if(entry.getName().startsWith("rel_")) 
	    			{schemaNames.add("connection");}     
	    			fileNames.add(entry);
	    		}
	    		else listFilesSchemas(entry, fileNames, schemaNames, importtype);
	    	}
			else if(entry.getName().equalsIgnoreCase(importtype+".xml") && listFileNames.contains(entry.getName()))
	    	{	
	    		if (entry.isFile()) { 
	    			schemaNames.add(entry.getName());	    		   
	    			fileNames.add(entry);
	    		}
	    		else listFilesSchemas(entry, fileNames, schemaNames, importtype);
	    	}
	    }
	    return ;
	}
	
	
	private static JSONObject getConfigurables(String propertyFileName) throws Exception
	{	
		FileReader reader = new FileReader(propertyFileName);
		Properties p = new Properties();
		p.load(reader);
		Set<Object> propertyKeys = p.keySet();
		Iterator<Object> propertyKeysItr = propertyKeys.iterator();
		JSONObject conf = new JSONObject();
		while (propertyKeysItr.hasNext())
		{
		  String key = propertyKeysItr.next().toString();
		  String value = p.getProperty(key);
		  conf.put(key, value);
		}
		return conf;
	}
  
	private static void restCall(String URL, JSONObject params)
	{
		String restURL = URL + formatJSONtoParam(params);
		try
		{
			URL url = new URL(restURL);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			if (conn.getResponseCode() != 200) 
			{
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String output;
			String sOutput = "";
			while ((output = br.readLine()) != null)
			{
				sOutput=output;
				System.out.print(output);
			}
			
			if("Compare Done".equalsIgnoreCase(sOutput) || "Compare Failed".equalsIgnoreCase(sOutput) || "Export Done".equalsIgnoreCase(sOutput) || "Import Done".equalsIgnoreCase(sOutput) || "Export Failed".equalsIgnoreCase(sOutput) || "Import Failed".equalsIgnoreCase(sOutput))
				System.out.println(". Please check log for more information.");
			conn.disconnect();
		}
		catch (Exception e)
		{
		  e.printStackTrace();
		}
	}
  
	private static String formatJSONtoParam(JSONObject params)
	{
		String parameters = new String();
		try {
			Iterator<String> paramItr = params.keys();
			int counter = 0;
			while (paramItr.hasNext())
			{
				String key = paramItr.next().toString();
				String value = params.get(key).toString();
				if (counter == 0) {
					parameters = "?" + key + "=" + value;
				} else {
					parameters = parameters + "&" + key + "=" + value;
				}
				counter++;
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return parameters;
	}
	
	public static void listFilesForCompare(File dir, ArrayList<File> fileNames, ArrayList<String> schemaNames, String comparetype) {
	    comparetype = "*";
	    if(dir==null||dir.listFiles()==null){
	        return ;
	    }
		if("form".equalsIgnoreCase(comparetype)){
			comparetype = "webform";
		}
	    for (File entry : dir.listFiles()) {
			if("*".equalsIgnoreCase(comparetype) && (listFileNames.contains(entry.getName()) || (entry.isFile() && entry.getName().startsWith("bo_")) || (entry.isFile() && entry.getName().startsWith("rel_"))))
	    	{
	    		if (entry.isFile()) {
	    			if (entry.getName().startsWith("bo_")) 
	    			{ schemaNames.add("businessobject");} 
	    			else if(entry.getName().startsWith("rel_")) 
	    			{schemaNames.add("connection");} 
	    			else {	 
	    				schemaNames.add(entry.getName());
	    			}    
	    			fileNames.add(entry);
	    		}
	    		else listFilesSchemas(entry, fileNames, schemaNames, comparetype);
	    	}
			else if("bus".equalsIgnoreCase(comparetype) && ((entry.isFile() && entry.getName().startsWith("bo_")) || (entry.isFile() && entry.getName().startsWith("rel_"))))
	    	{	    	
	    		if (entry.isFile()) {
	    			if (entry.getName().startsWith("bo_")) 
	    			{ schemaNames.add("businessobject");} 
	    			else if(entry.getName().startsWith("rel_")) 
	    			{schemaNames.add("connection");}     
	    			fileNames.add(entry);
	    		}
	    		else listFilesSchemas(entry, fileNames, schemaNames, comparetype);
	    	}
			else if(entry.getName().equalsIgnoreCase(comparetype+".xml") && listFileNames.contains(entry.getName()))
	    	{	
	    		if (entry.isFile()) { 
	    			schemaNames.add(entry.getName());	    		   
	    			fileNames.add(entry);
	    		}
	    		else listFilesSchemas(entry, fileNames, schemaNames, comparetype);
	    	}
	    }
	    return ;
	}
}
