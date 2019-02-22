package com.izn.schemamodeler.system.store;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dassault_systemes.platform.ven.jackson.core.JsonProcessingException;
import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.izn.schemamodeler.SchemaInfo;
import com.matrixone.apps.cache.UITypeUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;

import matrix.db.Context;

public class StoreInfo implements SchemaInfo {

	
	public StoreInfo()
	{
		
	}
	@Override
	public String geSchemaInfo(Context context, String strSchemaName, String strSchemaFileName) throws Exception {
		// TODO Auto-generated method stub
		String strInfo=null;
		String strResult = null;
	    String strRegistryName = "";
	    try{	 
	   	 strResult = MqlUtil.mqlCommand(context, "list store $1", new String[] { strSchemaName });
	     ContextUtil.pushContext(context);
	   	 if(UIUtil.isNotNullAndNotEmpty(strSchemaName)) {
	   	 try {
				strResult = MqlUtil.mqlCommand(context, "print store '$1' nothistory", new String[] { strSchemaName });
				strRegistryName = MqlUtil.mqlCommand(context, "list property to store $1", new String[] { strSchemaName });
				if(strRegistryName != null && !strRegistryName.equals("")){				 
				String[] split = strRegistryName.trim().split(" ");
				strRegistryName = split[0].trim();
	   			strRegistryName = strRegistryName.replaceFirst("store_", "");
	   		     }
	   		 } catch(Exception ex) {
	   			throw ex;	 
	   		 }
	   		 }
	   		 if (UIUtil.isNotNullAndNotEmpty(strResult)) { strInfo  =  parseIndex(context,strResult,strRegistryName); }  
	   	     }catch(Exception e){
	   	    	ContextUtil.popContext(context);
	   	        throw e;
	    	 } 
	   	 return strInfo;
	}
	private String parseIndex(Context context, String strResult, String strRegistryName) throws  Exception{
		// TODO Auto-generated method stub
		ObjectMapper _objectMapper = new ObjectMapper();
	    Map pInfoMap= new LinkedHashMap();
    try{		    
		    
	    List<String>  lstAttribute =new ArrayList<String>();    
	    List<Map>  lstRel =new ArrayList<Map>();  
	     
	    BufferedReader localBufferedReader = new BufferedReader(new StringReader(strResult));
	    
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
	        if (str1.startsWith("store"))
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
	        else if(str1.startsWith("type"))
	        {
	        	pInfoMap.put("type", str2);
	        }
	        else if(str1.startsWith("permission"))
	        {
	        	pInfoMap.put("permission",str2);
	        }
	        else if(str1.startsWith("protocol"))
	        {
	        	pInfoMap.put("protocol",str2);
	        }
	        else if(str1.startsWith("port"))
	        {
	        	pInfoMap.put("port",str2);
	        }
	        else if(str1.startsWith("host"))
	        {
	        	pInfoMap.put("host",str2);
	        }
	        else if(str1.startsWith("path"))
	        {
	        	pInfoMap.put("path",str2);
	        }
	        else if(str1.startsWith("user"))
	        {
	        	pInfoMap.put("user",str2);
	        }
	        else if(str1.startsWith("password"))
	        {
	        	pInfoMap.put("password",str2);
	        }
	        else if(str1.startsWith("fcs"))
	        {
	        	pInfoMap.put("fcs",str2);
	        }
	        else if(str1.startsWith("unlocked"))
	        {
	        	pInfoMap.put("lock", "false");
	        }else if(str1.startsWith("locked"))
	        {
	        	pInfoMap.put("lock", "true");
	        }
	        else if(str1.startsWith("icon"))
	        {
	        	pInfoMap.put("Icon",str2);
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
