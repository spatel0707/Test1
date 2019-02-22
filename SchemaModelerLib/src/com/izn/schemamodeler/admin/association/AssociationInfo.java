package com.izn.schemamodeler.admin.association;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.izn.schemamodeler.SchemaInfo;
import com.matrixone.apps.cache.UITypeUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;

import matrix.db.Context;

public class AssociationInfo implements SchemaInfo{
	
	public AssociationInfo() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String geSchemaInfo(Context context, String strAssociationName, String tbd) throws Exception
	{
		String strInfo=EMPTY_STRING;
		String strResult=""; 
		String strRegistryName = "";

	  try{ 					  
		 if(UIUtil.isNotNullAndNotEmpty(strAssociationName))
			 try {
			 strResult = MqlUtil.mqlCommand(context, "print association $1 nothistory", new String[] { strAssociationName});
			 strRegistryName = MqlUtil.mqlCommand(context, "list property to association $1", new String[] { strAssociationName });
			 if(strRegistryName != null && !strRegistryName.equals("")){				 
					String[] split = strRegistryName.trim().split(" ");
					strRegistryName = split[0].trim();
					strRegistryName = strRegistryName.replaceFirst("association_", "");
				}
			 } catch(Exception ex) {
				 
			 }
		 if (UIUtil.isNotNullAndNotEmpty(strResult)) { strInfo  =  parseAssociation(context,strResult,strRegistryName); }  
 

	     }catch(Exception e){
		
		 e.printStackTrace();
 	    }
	  return strInfo;
	}
	
	
	 private String parseAssociation(Context context,String strMQLResult,String strRegistryName) throws Exception{
		 ObjectMapper _objectMapper = new ObjectMapper();
       Map assoInfoMap =new LinkedHashMap();
	   try{
          assoInfoMap.put("adminType", "association");
		  assoInfoMap.put("action", "modify");
		  assoInfoMap.put("registryname",strRegistryName);
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
		        if(str1.startsWith("association") ){
		        	assoInfoMap.put("name", str2);
		        	
		        }  
		        else if (str1.startsWith("description"))
		        {
		        	assoInfoMap.put("description", str2);
		        }
		        else if(str1.startsWith("nothidden"))
		        {
		        	assoInfoMap.put("hidden", "false");
		        }else if(str1.startsWith("hidden"))
		        {
		        	assoInfoMap.put("hidden", "true");
		        }
		        else if(str1.startsWith("definition"))
		        {
		        	assoInfoMap.put("definition", str2);
		       }
		      
		      }
				 
				 
		 
	  }catch(Exception e){
		  //System.out.println(" Error in getting Association Info");
		  e.printStackTrace();
	  }
	   return _objectMapper.writeValueAsString(assoInfoMap);
   }

	@Override
	public String geSchemaInfoWithPath(Context context, String strSchemaName, String strExportPath) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
