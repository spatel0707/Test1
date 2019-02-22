/*
 *  RoleInfo.java
 *
 *
 * (c) Intelizign Engineering services PVT.  All rights reserved
 *
 *
 * 
 */
package com.izn.schemamodeler.admin.role;

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
 

public class RoleInfo implements SchemaInfo{
	
	public RoleInfo() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String geSchemaInfo(Context context, String strRoleName, String tbd) throws Exception
	{
		String strInfo = EMPTY_STRING;
		String strRegistryName = EMPTY_STRING;
		String strResult = null;
	  try{	
		  
		 strResult = MqlUtil.mqlCommand(context, "list role $1", new String[] { strRoleName });
			  
		 if(UIUtil.isNotNullAndNotEmpty(strRoleName))
			 try {
					strResult = MqlUtil.mqlCommand(context, "print role $1 nothistory", new String[] { strRoleName});
					strRegistryName = MqlUtil.mqlCommand(context, "list property to role $1", new String[] { strRoleName });
					if(strRegistryName != null && !strRegistryName.equals("")){				 
						String[] split = strRegistryName.trim().split(" ");
						strRegistryName = split[0].trim();
						strRegistryName = strRegistryName.replaceFirst("role_", "");
					}
			 } catch(Exception ex) {
				 
			 }
		 if (UIUtil.isNotNullAndNotEmpty(strResult)) { strInfo  =  parseRole(context,strResult,strRoleName,strRegistryName); }  
	     }catch(Exception e){
	    e.printStackTrace();
		return null; 
 	 } 
	 return strInfo;
	}
	
	
	 private String parseRole(Context context,String strMQLResult, String strRoleName, String strRegistryName) throws Exception{
	  ObjectMapper _objectMapper = new ObjectMapper();
      Map<String, Object> roleInfoMap =new LinkedHashMap();
	  try{
		
		  Map<String,String> pInfoMap=new HashMap<String,String>();	
	      Map<String,String> mSettings=new HashMap<String,String>();
	      Map<String,String> mProperties=new HashMap<String,String>();
	      roleInfoMap.put("adminType", "role");
	      roleInfoMap.put("action", "modify");
	      roleInfoMap.put("parent", "");
	      roleInfoMap.put("child", new ArrayList()); // to stick to format 
	      roleInfoMap.put("site", "");
	      roleInfoMap.put("maturity", "none");
	      roleInfoMap.put("registryname", strRegistryName);
	      // roleInfoMap.put("roletype", "");  /// no way to identify using print
	      
	      Map mChilds =new HashMap();
		  List alChildRoles=new ArrayList();
		  List alAssignment=new ArrayList();
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
		        
		        if(str1.startsWith("role") && !bchildren){
		        	roleInfoMap.put("name", str2);
		        	bchildren=true;
		        }  
		        else if (str1.startsWith("description"))
		        {
		        	roleInfoMap.put("description", str2);
		        } else if (str1.startsWith("site"))
		        {
		        	roleInfoMap.put("site", str2);
		        }
		        else if (str1.startsWith("parent"))
		        {
		        	roleInfoMap.put("parent", str2);
		        } else if (str1.startsWith("maturity"))
		        {
		        	roleInfoMap.put("maturity", str2);
		        }
		        else if (str1.startsWith("child"))
		        {
		        	roleInfoMap.put("child", str2.split(","));
		        	String[] sArray=str2.split(",");
		        	for(String sRole:sArray){
		        		mChilds=new HashMap();
		        		mChilds.put("name", sRole);
		        		mChilds.put("flag", "");
		        		alChildRoles.add(mChilds);
		        	}
		        	roleInfoMap.put("child", alChildRoles);
		        }
		        
		        else if(str1.startsWith("nothidden"))
		        {
		        	roleInfoMap.put("hidden", "false");
		        }else if(str1.startsWith("hidden"))
		        {
		        	roleInfoMap.put("hidden", "true");
		        }
		        else if(str1.startsWith("people"))
		        {
		        	mChilds=new HashMap();
	        		mChilds.put("name", str2);
	        		mChilds.put("flag", "");
	        		alAssignment.add(mChilds);
		        }
		       }
		        roleInfoMap.put("assignment", alAssignment);
				 
		        String isaPrjOrg = MqlUtil.mqlCommand(context, "print role $1 select $2 $3 dump", new String[] { strRoleName,"isaproject", "isanorg"  });
		        String[] isaPrjOrgResult = isaPrjOrg.split(",");
		        String isaProject = isaPrjOrgResult[0];
		        String isanOrg = isaPrjOrgResult[1];
		        if (isaProject.equalsIgnoreCase("true")) { roleInfoMap.put("roletype","isaproject"); }
		        else if (isanOrg.equalsIgnoreCase("true")) { roleInfoMap.put("roletype","isanorg"); }
		        else { roleInfoMap.put("roletype","isarole"); }
		        	         
	  }catch(Exception e){
		  //System.out.println(" Error in getting role Info");
		  e.printStackTrace();
	  }

	  return _objectMapper.writeValueAsString(roleInfoMap);	  
   }

	@Override
	public String geSchemaInfoWithPath(Context context, String strSchemaName, String strExportPath) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
}
