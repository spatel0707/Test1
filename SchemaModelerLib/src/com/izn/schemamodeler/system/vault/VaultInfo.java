package com.izn.schemamodeler.system.vault;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.izn.schemamodeler.SchemaInfo;
import com.matrixone.apps.cache.UITypeUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;

public class VaultInfo implements SchemaInfo {

	public VaultInfo()
	{
	}

	@Override
	public String geSchemaInfo(Context context, String strSchemaName, String strSchemaFileName) throws Exception {
		// TODO Auto-generated method stub\
		String strInfo=null;
		String strResult = null;
	    String strRegistryName = "";
	    try{
			 
	   	 strResult = MqlUtil.mqlCommand(context, "list vault $1", new String[] { strSchemaName });
	    	 ContextUtil.pushContext(context);
	   		 if(UIUtil.isNotNullAndNotEmpty(strSchemaName)) {
	   			 try {
	   					strResult = MqlUtil.mqlCommand(context, "print vault '$1' nothistory", new String[] { strSchemaName });
	   					strRegistryName = MqlUtil.mqlCommand(context, "list property to vault $1", new String[] { strSchemaName });
	   					if(strRegistryName != null && !strRegistryName.equals("")){				 
	   						String[] split = strRegistryName.trim().split(" ");
	   						strRegistryName = split[0].trim();
	   						strRegistryName = strRegistryName.replaceFirst("vault_", "");
	   						
	   					}
	   			 } catch(Exception ex) {
	   				 
	   			 }
	   		 }
	   		 if (UIUtil.isNotNullAndNotEmpty(strResult)) { strInfo  =  parseVault(context,strResult,strRegistryName); }  
	   	     }catch(Exception e){
	   	    	ContextUtil.popContext(context);
	   	        throw e;
	    	 } 
	   	 return strInfo;
		
	}
	private String parseVault(Context context,String strMQLResult,String strRegistryName) throws Exception
	{
		 ObjectMapper _objectMapper = new ObjectMapper();
		    Map pInfoMap= new LinkedHashMap();
	    try{		    
			    
		    List<String>  lstAttribute =new ArrayList<String>();    
		    List<Map>  lstRel =new ArrayList<Map>();  
		     
		    BufferedReader localBufferedReader = new BufferedReader(new StringReader(strMQLResult));
		    
		    pInfoMap.put("registryname", strRegistryName);
		      String str1;
		      while ((str1 = localBufferedReader.readLine()) != null)
		      {
		    	Map<String,String> keyValue  = new LinkedHashMap<String,String>(); 
		    	String str2 = "";
		        str1 = str1.trim();
		        int i = str1.indexOf(' ');
		        if ((i != -1) && (str1.length() > i + 1)) {
			          str2 = UITypeUtil.unquote(str1.substring(i + 1));
			        }
		        if (str1.startsWith("lattice"))
		        {
		        	
		        	pInfoMap.put("name", str2);
		        }  
		        else if (str1.startsWith("description"))
		        {
		        	pInfoMap.put("description", str2);
		        }
		        else if(str1.startsWith("nothidden"))
		        {
		        	pInfoMap.put("hidden", "false");
		        }else if(str1.startsWith("hidden"))
		        {
		        	pInfoMap.put("hidden", "true");
		        }
		        else if(str1.startsWith("data tablespace"))
		        {
		        	str2 = str1.substring("data tablespace".length());
		        	pInfoMap.put("tablespace", str2);
		        }
		        else if(str1.startsWith("index tablespace"))
		        { 	
		        	str2 = str1.substring("index tablespace".length());
		        	pInfoMap.put("indexspace",str2);
		        }
		        else if(str1.startsWith("status"))
		        {
		        	 int j = str1.indexOf('=');
		        	 if ((j != -1) && (str1.length() > j + 1)) {
		   	          str2 = UITypeUtil.unquote(str1.substring(j + 1));
		   	        }
		        	pInfoMap.put("status", str2);
		        }	
		      } 	 
	  }catch(Exception e){
		  throw e;
	  }
	  return _objectMapper.writeValueAsString(pInfoMap);
	}

	@Override
	public String geSchemaInfoWithPath(Context context, String strSchemaName, String strExportPath) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	

}
