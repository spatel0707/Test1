package com.izn.schemamodeler.admin.interfaces;

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


public class InterfaceInfo implements SchemaInfo {
	
	public InterfaceInfo() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String geSchemaInfo(Context context, String strInterfaceName, String tbd) throws Exception
	{
		 String strInfo=EMPTY_STRING;
		 String strRegistryName = EMPTY_STRING;
		 String strAttributeResult = EMPTY_STRING;
	 try{  
		 String strResult = MqlUtil.mqlCommand(context, "list Interface $1", new String[] { strInterfaceName });
		 //System.out.println("Line 32 InterfaceInfo strResult is ::::::::::::               "+strResult);
				  
		 if(UIUtil.isNotNullAndNotEmpty(strInterfaceName))
			 try {
					strResult = MqlUtil.mqlCommand(context, "print Interface $1 nothistory", new String[] { strInterfaceName});
					strAttributeResult = MqlUtil.mqlCommand(context, "print Interface $1 select attribute dump", new String[] { strInterfaceName});
					strRegistryName = MqlUtil.mqlCommand(context, "list property to Interface $1",new String[] { strInterfaceName });
					if(strRegistryName != null && !strRegistryName.equals("")){				 
						String[] split = strRegistryName.trim().split(" ");
						strRegistryName = split[0].trim();
						strRegistryName = strRegistryName.replaceFirst("interface_", "");
					}
			 } catch(Exception ex) {
				 
			 }
		 if (UIUtil.isNotNullAndNotEmpty(strResult)) { strInfo  =  parseInterface(context,strResult,strRegistryName,strAttributeResult); }  
 
	    
	     }catch(Exception e){
		
		 e.printStackTrace();
       }
	 return strInfo;
	}
	
	
	 private String parseInterface(Context context,String strMQLResult, String strRegistryName, String strAttributeResult) throws Exception{
		 ObjectMapper _objectMapper = new ObjectMapper();
        Map interfaceInfoMap =new LinkedHashMap();
	  try{
		  
		  Map<String,String> mInfoMap=new HashMap<String,String>();	
	      Map<String,String> mSettings=new HashMap<String,String>();
	      Map<String,String> mProperties=new HashMap<String,String>();
	      interfaceInfoMap.put("adminType", "interface");
	      interfaceInfoMap.put("action", "modify");
	      List lstEmpty=  new ArrayList();
	      interfaceInfoMap.put("attribute", lstEmpty);
	      interfaceInfoMap.put("type", lstEmpty);
	      interfaceInfoMap.put("relationship", lstEmpty);
	      interfaceInfoMap.put("derived", "");
	      interfaceInfoMap.put("registryname", strRegistryName);
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
		        if(str1.startsWith("interface") && !bchildren){
		        	interfaceInfoMap.put("name", str2);
		        	bchildren=true;
		        }  
		        else if (str1.startsWith("description"))
		        {
		        	interfaceInfoMap.put("description", str2);
		        }
		        else if (str1.startsWith("derived"))
		        {
		        	interfaceInfoMap.put("derived", str2);
		        }
		        else if (str1.startsWith("abstract"))
		        {
		        	interfaceInfoMap.put("abstract", str2);
		        }
		        else if (str1.startsWith("attribute"))
		        {
		        	interfaceInfoMap.put("attribute", str2.split(","));
		        	//interfaceInfoMap.put("attribute", strAttributeResult.split(","));
		        	
		        }else if(str1.startsWith("inherited attribute")){
		        	//interfaceInfoMap.put("inheriedAttribute", str2.split(","));
		        	interfaceInfoMap.put("inheriedAttribute", strAttributeResult.split(","));
		        }
		        else if(str1.startsWith("nothidden"))
		        {
		        	interfaceInfoMap.put("hidden", "false");
		        }else if(str1.startsWith("hidden"))
		        {
		        	interfaceInfoMap.put("hidden", "true");
		        }
		        else if(str1.startsWith("type"))
		        {
		        	String[] sValue =  str2.split(",");
		        	if(sValue[0].contains("type")) {sValue[0] =  sValue[0].replace("type ", "");}
		        	 interfaceInfoMap.put("type",  sValue);
		        } else if(str1.startsWith("relationship"))
		        {
		        	String[] sValueRel =  str2.split(",");
		        	if(sValueRel[0].contains("relationship")) {sValueRel[0] =  sValueRel[0].replace("relationship ", "");}
		        	 interfaceInfoMap.put("relationship",  sValueRel);
		        }
		       }
		  }catch(Exception e){
		  //System.out.println(" Error in getting interface Info");
		  e.printStackTrace();
	  }

	  return _objectMapper.writeValueAsString(interfaceInfoMap);
    }

	@Override
	public String geSchemaInfoWithPath(Context context, String strSchemaName, String strExportPath) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
}
