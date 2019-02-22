package com.izn.schemamodeler.admin.format;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.izn.schemamodeler.SchemaInfo;
import com.matrixone.apps.cache.UITypeUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;

import matrix.db.Context;
 

public class FormatInfo implements SchemaInfo{
	
	public FormatInfo() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String geSchemaInfo(Context context, String strformatName, String tbd) throws Exception
	{
		String strInfo=EMPTY_STRING;
		String strResult = null;
		String strRegistryName = EMPTY_STRING;
	  try{	
		  
		 strResult = MqlUtil.mqlCommand(context, "list format $1", new String[] { strformatName });
		 //System.out.println("strResult is ::::::::::::               "+strResult);
				  
		 if(UIUtil.isNotNullAndNotEmpty(strformatName))
			 try {
				 	strResult = MqlUtil.mqlCommand(context, "print format $1 nothistory", new String[] { strformatName});
				 	strRegistryName = MqlUtil.mqlCommand(context, "list property to format $1", new String[] { strformatName });
					if(strRegistryName != null && !strRegistryName.equals("")){				 
						String[] split = strRegistryName.trim().split(" ");
						strRegistryName = split[0].trim();
						strRegistryName = strRegistryName.replaceFirst("format_", "");
					}
			 } catch(Exception ex) {
				 
			 }
		 if (UIUtil.isNotNullAndNotEmpty(strResult)) { strInfo  =  parseFormat(context,strResult,strRegistryName); }  
	     }catch(Exception e){
	    e.printStackTrace();
		return null; 
 	 } 
	 return strInfo;
	}
	
	
	 private String parseFormat(Context context,String strMQLResult,String strRegistryName) throws Exception{
		 ObjectMapper _objectMapper = new ObjectMapper();
       Map formatInfoMap =new LinkedHashMap();
	  try{
		
		  Map<String,String> pInfoMap=new HashMap<String,String>();	
	      Map<String,String> mSettings=new HashMap<String,String>();
	      Map<String,String> mProperties=new HashMap<String,String>();
	      formatInfoMap.put("adminType", "format");
	      formatInfoMap.put("action", "modify");
	      formatInfoMap.put("version", "");
	      formatInfoMap.put("description", "");  
	      formatInfoMap.put("filesuffix", "");
	      formatInfoMap.put("filecreator", "none");
	      formatInfoMap.put("filetype", "");
	      formatInfoMap.put("hidden", "");
		  formatInfoMap.put("registryname",strRegistryName);
	      
	 	    BufferedReader localBufferedReader = new BufferedReader(new StringReader(strMQLResult));
		    String str1;
		      while ((str1 = localBufferedReader.readLine()) != null)
		      {
		        str1 = str1.trim();
		        int j = str1.indexOf(' ');
		        String str2 = "";
		        if ((j != -1) && (str1.length() > j + 1)) {
		          str2 = UITypeUtil.unquote(str1.substring(j + 1));
		        }
		        if(str1.startsWith("format") ){
		        	formatInfoMap.put("name", str2);
		        }  
		        else if (str1.startsWith("description"))
		        {
		        	formatInfoMap.put("description", str2);
		        } else if (str1.startsWith("version"))
		        {
		        	formatInfoMap.put("version", str2);
		        }
		        else if (str1.startsWith("filesuffix") || (str1.startsWith("suffix") ))
		        {
		        	formatInfoMap.put("filesuffix", str2);
		        } else if (str1.startsWith("filecreator") || (str1.startsWith("creator")))
		        {
		        	formatInfoMap.put("filecreator", str2);
		        }else if (str1.startsWith("filetype") || (str1.startsWith("type")))
		        {
		        	formatInfoMap.put("filetype", str2);
		        }
		        
		        else if(str1.startsWith("nothidden"))
		        {
		        	formatInfoMap.put("hidden", "false");
		        }else if(str1.startsWith("hidden"))
		        {
		        	formatInfoMap.put("hidden", "true");
		        }
		       
		       }
		         
				 
			 
	  }catch(Exception e){
		  //System.out.println(" Error in getting format Info");
		  e.printStackTrace();
	  }
	  return _objectMapper.writeValueAsString(formatInfoMap);
   }

	@Override
	public String geSchemaInfoWithPath(Context context, String strSchemaName, String strExportPath) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
}
