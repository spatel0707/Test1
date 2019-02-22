package com.izn.schemamodeler.admin.type;

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
import com.matrixone.apps.framework.ui.UIUtil;

import matrix.db.Context;
 

public class TypeInfo implements SchemaInfo{

	public TypeInfo() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String geSchemaInfo(Context context, String strTypeName, String tbd) throws Exception
	{
		String strInfo=null;
		String strResult = null;
	    String strRegistryName = "";
	 try{
		 
	 strResult = MqlUtil.mqlCommand(context, "list type $1", new String[] { strTypeName });
 	 //System.out.println("strResult is ::::::::::::               "+strResult);
		  
		 if(UIUtil.isNotNullAndNotEmpty(strTypeName))
			 try {
					strResult = MqlUtil.mqlCommand(context, "print type $1 nothistory", new String[] { strTypeName });
					strRegistryName = MqlUtil.mqlCommand(context, "list property to type $1", new String[] { strTypeName });
					if(strRegistryName != null && !strRegistryName.equals("")){				 
						String[] split = strRegistryName.trim().split(" ");
						strRegistryName = split[0].trim();
						strRegistryName = strRegistryName.replaceFirst("type_", "");
					}
			 } catch(Exception ex) {
				 
			 }
		 if (UIUtil.isNotNullAndNotEmpty(strResult)) { strInfo  =  parseType(context,strResult,strRegistryName); }  
	     }catch(Exception e){
	    e.printStackTrace();
		return null; 
 	 } 
	 return strInfo;
	}
	
	
	 private String parseType(Context context,String strMQLResult,String strRegistryName) throws Exception{
		 ObjectMapper _objectMapper = new ObjectMapper();
       Map typeInfoMap =new LinkedHashMap();
	  try{
		 
		  Map<String,String> pInfoMap=new HashMap<String,String>();	
	      Map<String,String> mSettings=new HashMap<String,String>();
	      Map<String,String> mProperties=new HashMap<String,String>();
	      typeInfoMap.put("adminType", "type");
	      typeInfoMap.put("action", "modify");
	      typeInfoMap.put("sparse", "false");
	      typeInfoMap.put("trigger", new ArrayList());
	      typeInfoMap.put("derived", "");
	      typeInfoMap.put("attribute", new ArrayList());
	      typeInfoMap.put("registryname", strRegistryName);
		  ArrayList slMenus=new ArrayList();
		  ArrayList slCommands=new ArrayList();
		  int i=0;
	 	    BufferedReader localBufferedReader = new BufferedReader(new StringReader(strMQLResult));
		      boolean bchildren=false;
		      String str1;

		      while ((str1 = localBufferedReader.readLine()) != null)
		      {
		        str1 = str1.trim();
		        int j = str1.indexOf(' ');
		        String str2 = "";
		        if ((j != -1) && (str1.length() > j + 1)) {
		          str2 = UITypeUtil.unquote(str1.substring(j + 1));
		        }
		        if(str1.startsWith("type") && !bchildren){
		        	typeInfoMap.put("name", str2);
		        	bchildren=true;
		        }  
		        else if (str1.startsWith("description"))
		        {
		        	
		        	typeInfoMap.put("description", str2);
		        	
		        }
		        else if (str1.startsWith("derived"))
		        {
		        	typeInfoMap.put("derived", str2);
		        }
		        else if (str1.startsWith("abstract"))
		        {
		        	typeInfoMap.put("abstract", str2);
		        }
		        else if (str1.startsWith("attribute"))
		        {
		        	typeInfoMap.put("attribute", str2.split(","));
		        }else if(str1.startsWith("inherited attribute")){
		        	if(str2.startsWith("attribute")) {str2=str2.replace("attribute ", "");}
		        	//typeInfoMap.put("attribute", str2.split(","));
		        }else if (str1.startsWith("method"))
		        {
		        	typeInfoMap.put("method", str2.split(","));
		        }
		        else if(str1.startsWith("nothidden"))
		        {
		        	typeInfoMap.put("hidden", "false");
		        }else if(str1.startsWith("hidden"))
		        {
		        	typeInfoMap.put("hidden", "true");
		        }
		        else if(str1.startsWith("trigger"))
		        {
			        List ltTrigger =   com.izn.schemamodeler.util.UIUtil.getTriggerDetails(context, str2);
			        typeInfoMap.put("trigger", ltTrigger);
			    }/*else if(str1.startsWith("inherited trigger"))
					{
			          	if(str2.startsWith("inherited trigger")) {str2=str2.replace("inherited ", "");
			        		str2=str2.replace("trigger", "");}
			        	if(str2.startsWith("trigger")) {str2=str2.replace("trigger", "");}
		        	 
			        	List ltInheritedTrigger =   com.izn.schemamodeler.util.UIUtil.getTriggerDetails(context, str2);
						ltInheritedTrigger.addAll(ltTrigger);
		        	    typeInfoMap.put("trigger", ltInheritedTrigger);  
		         }*/
		     }
 	  }catch(Exception e){
		  //System.out.println(" Error in getting type Info");
		  e.printStackTrace();
	  }
	  return _objectMapper.writeValueAsString(typeInfoMap);
	  
	 }

	@Override
	public String geSchemaInfoWithPath(Context context, String strSchemaName, String strExportPath) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}
