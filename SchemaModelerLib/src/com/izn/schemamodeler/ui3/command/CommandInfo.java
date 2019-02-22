package com.izn.schemamodeler.ui3.command;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.izn.schemamodeler.SchemaInfo;
import com.matrixone.apps.cache.UITypeUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;

import matrix.db.Context;
 

public class CommandInfo implements SchemaInfo{
	String _path="";
	
	 public CommandInfo() {
		// TODO Auto-generated constructor stub
	}
	  

	public String geSchemaInfo(Context context, String strCommandName, String tbd) throws Exception
	{
		 String strInfo=EMPTY_STRING;
	 try{  
		 String strResult = MqlUtil.mqlCommand(context, "list command $1", new String[] { strCommandName });			  
		 if(UIUtil.isNotNullAndNotEmpty(strCommandName))
			 try {
					strResult = MqlUtil.mqlCommand(context, "print command $1 nothistory", new String[] { strCommandName});			
			 } catch(Exception ex) {
				 
			 }
		 
		 if (UIUtil.isNotNullAndNotEmpty(strResult)) { strInfo  =  parseCommand(context,strResult); }  
	 }catch(Exception e){
	 	   e.printStackTrace();
	 	   throw e;
 	 }
	 return strInfo;
	}
	
	
	 private String parseCommand(Context context,String strMQLResult,String strExportPath, String strRegistryName) throws Exception{
		 ObjectMapper _objectMapper = new ObjectMapper();
		 Map commandInfoMap= new HashMap();
	  try{
		    ArrayList localStringList1 = new ArrayList();
		    ArrayList slUsers = new ArrayList();
		    Map<String,String> mProperties=new HashMap<String,String>();
		    Map mUsers=new HashMap();
		    Map<String,String> mSettings=new HashMap<String,String>();
		    List alSettings=new ArrayList();
		    List alUsers=new ArrayList();
		    BufferedReader localBufferedReader = new BufferedReader(new StringReader(strMQLResult));
	    	commandInfoMap.put("adminType", "command");
	    	commandInfoMap.put("action", "modify");
	    	commandInfoMap.put("code", "");
	    	commandInfoMap.put("registryname", strRegistryName);
		      String str1;
		      String sNameforcode ="";
		      while ((str1 = localBufferedReader.readLine()) != null)
		      {
		        str1 = str1.trim();
		        int i = str1.indexOf(' ');
		        String str2 = "";
		        if ((i != -1) && (str1.length() > i + 1)) {
		          str2 = UITypeUtil.unquote(str1.substring(i + 1));
		        }
		        
		        if(str1.startsWith("command")){
		        	
		        	commandInfoMap.put("name", str2);
		        	sNameforcode = str2;
		        	
		        }
		        else if (str1.startsWith("description"))
		        {
		        	commandInfoMap.put("description", str2);
		        }
		        else if (str1.startsWith("icon"))
		        {
		        	commandInfoMap.put("icon", str2);
		        }
		        else if (str1.startsWith("label"))
		        {
		        	commandInfoMap.put("label", str2);
		        }
		        else if (str1.startsWith("href"))
		        {
		        	commandInfoMap.put("href", str2);
		        }
		        else if (str1.startsWith("alt"))
		        {
		        	commandInfoMap.put("alt", str2);
		        }
		        else if (str1.startsWith("input"))
		        {
		        	commandInfoMap.put("input", str2);
		        }else if(str1.startsWith("code")){
		        	String sCode = MqlUtil.mqlCommand(context, "print command $1 select code dump", new String[] { sNameforcode });
		        	if((sCode != null) && !(sCode.isEmpty())){
		        	commandInfoMap.put("code", sCode);
		        	}
		        }else if(str1.startsWith("nothidden")){
		        	commandInfoMap.put("hidden", "false");
		        }else if(str1.startsWith("hidden")){
		        	commandInfoMap.put("hidden", "true");
		        }
		        else if (str1.startsWith("user"))
		        {
		        	
		        	mUsers=new HashMap();
		        	mUsers.put("name", str2);
		        	mUsers.put("flag", "");
		        	slUsers.add(mUsers);
		         }
		        else
		        {
		          ArrayList localStringList3=new ArrayList();
		          if (str1.startsWith("property"))
		          {
		        	 // ArrayList slKeuValues = com.izn.schemamodeler.util.UIUtil.parseKeyAndValue(context, str2);
		          	 // mProperties.put((String)slKeuValues.elementAt(0), (String)slKeuValues.elementAt(1));
		      
		          }
		          else if (str1.startsWith("setting"))
		          {
		        	  ArrayList slKeyValues= com.izn.schemamodeler.util.UIUtil.parseKeyAndValue(context,str2);
		        	  mSettings=new HashMap();
		        	  mSettings.put("name", (String)slKeyValues.get(0));
		        	  mSettings.put("value", (String)slKeyValues.get(1));
		        	  mSettings.put("flag", "");
		        	  alSettings.add(mSettings);
		            }
		        }
		      }
		       if(!commandInfoMap.containsKey("label")){
		    	  commandInfoMap.put("label", ""); 
		       }
		      commandInfoMap.put("settings", alSettings);
		      commandInfoMap.put("objectAccess", slUsers);
 	  }catch(Exception e){
		  System.out.println(" Error in getting command Info");
		  e.printStackTrace();
	  }
	  return _objectMapper.writeValueAsString(commandInfoMap);
	 }
	 private String parseCommand(Context context,String strMQLResult) throws Exception{
		 ObjectMapper _objectMapper = new ObjectMapper();
	 Map commandInfoMap= new HashMap();
  try{
	    ArrayList localStringList1 = new ArrayList();
	    ArrayList slUsers = new ArrayList();
	    Map<String,String> mProperties=new HashMap<String,String>();
	    Map mUsers=new HashMap();
	    Map<String,String> mSettings=new HashMap<String,String>();
	    List alSettings=new ArrayList();
	    List alUsers=new ArrayList();
	    BufferedReader localBufferedReader = new BufferedReader(new StringReader(strMQLResult));
    	commandInfoMap.put("adminType", "command");
    	commandInfoMap.put("action", "modify");
    	commandInfoMap.put("code", "");
	      String str1;
	      while ((str1 = localBufferedReader.readLine()) != null)
	      {
	        str1 = str1.trim();
	        int i = str1.indexOf(' ');
	        String str2 = "";
	        if ((i != -1) && (str1.length() > i + 1)) {
	          str2 = UITypeUtil.unquote(str1.substring(i + 1));
	        }
	        if(str1.startsWith("command")){
	        	commandInfoMap.put("name", str2);
	        }
	        else if (str1.startsWith("description"))
	        {
	        	commandInfoMap.put("description", str2);
	        }
	        else if (str1.startsWith("icon"))
	        {
	        	commandInfoMap.put("icon", str2);
	        }
	        else if (str1.startsWith("label"))
	        {
	        	commandInfoMap.put("label", str2);
	        }
	        else if (str1.startsWith("href"))
	        {
	        	commandInfoMap.put("href", str2);
	        }
	        else if (str1.startsWith("alt"))
	        {
	        	commandInfoMap.put("alt", str2);
	        }
	        else if (str1.startsWith("input"))
	        {
	        	commandInfoMap.put("input", str2);
	        }else if(str1.startsWith("code")){
	        	commandInfoMap.put("code", str2);
	        	//commandInfoMap.put("filepath", _path);
	        }else if(str1.startsWith("nothidden")){
	        	commandInfoMap.put("hidden", "false");
	        }else if(str1.startsWith("hidden")){
	        	commandInfoMap.put("hidden", "true");
	        }
	        else if (str1.startsWith("user"))
	        {
	        	//slUsers.add(str2);
	        	mUsers=new HashMap();
	        	mUsers.put("name", str2);
	        	mUsers.put("flag", "");
	        	slUsers.add(mUsers);
	         }
	        else
	        {
	          ArrayList localStringList3=new ArrayList();
	          if (str1.startsWith("property"))
	          {
	        	 // ArrayList slKeuValues = com.izn.schemamodeler.util.UIUtil.parseKeyAndValue(context, str2);
	          	 // mProperties.put((String)slKeuValues.elementAt(0), (String)slKeuValues.elementAt(1));
	      
	          }
	          else if (str1.startsWith("setting"))
	          {
	        	  ArrayList slKeyValues= com.izn.schemamodeler.util.UIUtil.parseKeyAndValue(context,str2);
	        	  mSettings=new HashMap();
	        	  mSettings.put("name", (String)slKeyValues.get(0));
	        	  mSettings.put("value", (String)slKeyValues.get(1));
	        	  mSettings.put("flag", "");
	        	  alSettings.add(mSettings);
	            }
	        }
	      }
	       if(!commandInfoMap.containsKey("label")){
	    	  commandInfoMap.put("label", ""); 
	       }
	      commandInfoMap.put("settings", alSettings);
	      commandInfoMap.put("objectAccess", slUsers);
	  }catch(Exception e){
	  System.out.println(" Error in getting command Info");
	  e.printStackTrace();
  }
  return _objectMapper.writeValueAsString(commandInfoMap);
}
	 @Override
		public String geSchemaInfoWithPath(Context context, String strCommandName, String strExportPath) throws Exception {
			String strInfo = "";
			String strRegistryName = EMPTY_STRING;
		    try
		    {
				String strResult = MqlUtil.mqlCommand(context, "print command $1 nothistory", new String[] { strCommandName });
				strRegistryName = MqlUtil.mqlCommand(context, "list property to command $1", new String[] { strCommandName });
				if(strRegistryName != null && !strRegistryName.equals("")){				 
					String[] split = strRegistryName.trim().split(" ");
					strRegistryName = split[0].trim();
					strRegistryName = strRegistryName.replaceFirst("command_", "");
				}
		      /*File file = new File(strExportPath + "\\Programs");
		      if (!file.exists()) {
		        file.mkdir();
		      }
		     // strExportPath = strExportPath + "\\Programs\\" + strCommandName;		      
		      //MqlUtil.mqlCommand(context, "print command $1 select code dump output $2", new String[] { strCommandName, strExportPath });*/
				
		      if (UIUtil.isNotNullAndNotEmpty(strResult)) {
		        strInfo = parseCommand(context, strResult, strExportPath, strRegistryName);		        
		      }
		    }
		    catch (Exception e)
		    {
		      e.printStackTrace();
		    }
		    return strInfo;
		}

	public void  updateDBCommand(Context context,String sInputXML, String strCommandName) throws Exception {
		//Gson gson=new Gson();
		try{
	 		//String strCommandDBDef= geSchemaInfo(context, strCommandName, "tbd");
			//Map mCommandInfo=gson.fromJson(strCommandDBDef, HashMap.class); 
 		 }catch(Exception e ){
			e.printStackTrace();
		}
	}

	
}
