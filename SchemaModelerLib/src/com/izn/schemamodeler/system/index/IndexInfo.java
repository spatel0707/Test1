package com.izn.schemamodeler.system.index;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.izn.schemamodeler.SchemaInfo;
import com.matrixone.apps.cache.UITypeUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;


public class IndexInfo implements SchemaInfo {

	public IndexInfo()
	{
	}
	@Override
	public String geSchemaInfo(Context context, String strSchemaName, String strSchemaFileName) throws Exception {
		// TODO Auto-generated method stub
		String strInfo=null;
		String strResult = null;
	    String strRegistryName = "";
	    try{
			 
	   	 strResult = MqlUtil.mqlCommand(context, "list index $1", new String[] { strSchemaName });
	    	 ContextUtil.pushContext(context);
	   		 if(UIUtil.isNotNullAndNotEmpty(strSchemaName)) {
	   			 try {
	   					strResult = MqlUtil.mqlCommand(context, "print index '$1' nothistory", new String[] { strSchemaName });
	   					strRegistryName = MqlUtil.mqlCommand(context, "list property to index $1", new String[] { strSchemaName });
	   					if(strRegistryName != null && !strRegistryName.equals("")){				 
	   						String[] split = strRegistryName.trim().split(" ");
	   						strRegistryName = split[0].trim();
	   						strRegistryName = strRegistryName.replaceFirst("index_", "");			
	   					}
	   			 } catch(Exception ex) {
	   				throw ex; 
	   			 }
	   		 }
	   		 if (UIUtil.isNotNullAndNotEmpty(strResult)) { strInfo  =  parseVault(context,strResult,strRegistryName,strSchemaName); }  
	   	     }catch(Exception e){
	   	    	ContextUtil.popContext(context);
	   	         throw e;  		
	    	 } 
	   	 return strInfo;
		
	}
	private String parseVault(Context context,String strMQLResult,String strRegistryName,String schemNane) throws Exception
	{
		 ObjectMapper _objectMapper = new ObjectMapper();
		 Map pInfoMap= new LinkedHashMap();
	     try{		      
		    BufferedReader localBufferedReader = new BufferedReader(new StringReader(strMQLResult));	    
		      pInfoMap.put("registryname", strRegistryName);
		      String str1;	     
		      while ((str1 = localBufferedReader.readLine()) != null)
		      {
		    	String str2 = "";
		        str1 = str1.trim();
		        int i = str1.indexOf(' ');
		        if ((i != -1) && (str1.length() > i + 1)) {
			       str2 = UITypeUtil.unquote(str1.substring(i + 1));
			    }
		        if(str1.startsWith("index"))
		        {       	
		           pInfoMap.put("name", str2);
		        }  
		        else if(str1.startsWith("description"))
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
		        else if(str1.startsWith("disabled"))
		        {
		           pInfoMap.put("enable", "false");
		        }else if(str1.startsWith("enabled"))
		        {
		           pInfoMap.put("enable", "true");
		        }
		        else if(str1.startsWith("notunique"))
		        {
		           pInfoMap.put("unique", "false");
		        }else if(str1.startsWith("unique"))
		        {
		           pInfoMap.put("unique", "true");
		        }       
		        String strMqlResult = MqlUtil.mqlCommand(context, "print index '$1' select field dump", new String[] { schemNane });	
		        pInfoMap.put("field", strMqlResult);
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