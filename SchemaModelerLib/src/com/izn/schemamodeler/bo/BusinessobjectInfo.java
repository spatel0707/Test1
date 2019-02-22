package com.izn.schemamodeler.bo;

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
import com.matrixone.apps.domain.util.MqlUtil;

import matrix.db.Context;

public class BusinessobjectInfo implements SchemaInfo {

	public String geSchemaInfo(Context context, String strBusinessObject, String strExtraInfo) throws Exception
	{
		 String strInfo=EMPTY_STRING;
		 try{	
			 if(strBusinessObject != null)
			 {
				 String strResult = MqlUtil.mqlCommand(context, "print bus $1 select $2 $3 $4 $5 $6 $7 $8 $9 $10 $11 $12 $13", new String[] { strBusinessObject, "type", "name", "revision", "policy", "vault", "current", "owner", "attribute.value", "description", "originated", "modified", "grantee" });
				 if(strResult!=null && !strResult.isEmpty())
					  strInfo= parseBusinessObjects(context,strResult,strBusinessObject);
			 }
	     }catch(Exception e){
			 e.printStackTrace();
	     }
	 return strInfo;
	}
	
	
	
	private String parseBusinessObjects(Context context,String strMQLResult,String sBasic) throws Exception{
	    ObjectMapper _objectMapper = new ObjectMapper();
	    Map pInfoMap= new LinkedHashMap();
    try{		    
		    
	    List<String>  lstAttribute =new ArrayList<String>();    
	    List<Map>  lstRel =new ArrayList<Map>();  
	     
	    BufferedReader localBufferedReader = new BufferedReader(new StringReader(strMQLResult));
	    
	    
	      String str1;
	      while ((str1 = localBufferedReader.readLine()) != null)
	      {
	    	Map<String,String> keyValue  = new LinkedHashMap<String,String>(); 
	        str1 = str1.trim();
	        int i = str1.indexOf('=');
	        String str2 = "";
	        if ((i != -1) && (str1.length() > i + 1)) {
	          str2 = UITypeUtil.unquote(str1.substring(i + 2));
	        }

	        if (str1.startsWith("type"))
	        {
	        	pInfoMap.put("type", str2);
	        } 
	        else if (str1.startsWith("name"))
	        {
	        	pInfoMap.put("name", str2);
	        } 
	        else if (str1.startsWith("revision"))
	        {
	        	pInfoMap.put("revision", str2);
	        } 
	        else if (str1.startsWith("description"))
	        {
	        	pInfoMap.put("description", str2);
	        }
	        else if (str1.startsWith("owner"))
	        {
	        	pInfoMap.put("owner", str2);
	        }
	        else if(str1.startsWith("current")){
	        	pInfoMap.put("state", str2);
	        }
	        else if(str1.startsWith("policy")){
	        	pInfoMap.put("policy", str2);
	        } 
	        else if(str1.startsWith("vault")){
	        	pInfoMap.put("vault", str2);
	        } 
	        else if (str1.startsWith("attribute"))
	        {       
	        	String sAttributeName = str1.substring(str1.indexOf("[")+1,str1.indexOf("]"));
	        	pInfoMap.put(sAttributeName, str2);
	        	lstAttribute.add(sAttributeName);
	        }
	        else if(str1.startsWith("originated")){
	        	pInfoMap.put("originated", str2);
	        } 
	        else if(str1.startsWith("modified")){
	        	pInfoMap.put("modified", str2);
	        } 
	        else if(str1.startsWith("grantee")){
	        	pInfoMap.put("grantee", str2);
	        } 
	        
	      }
          pInfoMap.put("attributes", lstAttribute);
      
 	 
	  }catch(Exception e){
		  //System.out.println(" Error in getting Business object Info");
		  e.printStackTrace();
	  }
    
    	//return gson.toJson(pInfoMap);
    	return _objectMapper.writeValueAsString(pInfoMap);
	}



	@Override
	public String geSchemaInfoWithPath(Context context, String strSchemaName, String strExportPath) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}


	
}
