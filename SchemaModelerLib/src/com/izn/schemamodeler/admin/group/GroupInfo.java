package com.izn.schemamodeler.admin.group;

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
 

public class GroupInfo implements SchemaInfo{
	
	public GroupInfo() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String geSchemaInfo(Context context, String strGroupName, String strExtraInfo) throws Exception
	{
		String strInfo = EMPTY_STRING;
		String strResult = null;
		String strRegistryName = EMPTY_STRING;
	  try{	
		  
		 strResult = MqlUtil.mqlCommand(context, "list group $1", new String[] { strGroupName });
		 //System.out.println("strResult is ::::::::::::               "+strResult);
				  
		 if(UIUtil.isNotNullAndNotEmpty(strGroupName))
			 try {
					strResult = MqlUtil.mqlCommand(context, "print group $1 nothistory", new String[] { strGroupName});
					strRegistryName = MqlUtil.mqlCommand(context, "list property to group $1", new String[] { strGroupName });
					if(strRegistryName != null && !strRegistryName.equals("")){				 
						String[] split = strRegistryName.trim().split(" ");
						strRegistryName = split[0].trim();
						strRegistryName = strRegistryName.replaceFirst("group_", "");
					}
			 } catch(Exception ex) {
				 
			 }
		 if (UIUtil.isNotNullAndNotEmpty(strResult)) { strInfo  =  parseGroup(context,strResult,strRegistryName); }  
	     }catch(Exception e){
	    e.printStackTrace();
		return null; 
 	 } 
	 return strInfo;
	}
	
	
	 private String parseGroup(Context context,String strMQLResult,String strRegistryName) throws Exception{
		 ObjectMapper _objectMapper = new ObjectMapper();
       Map groupInfoMap =new LinkedHashMap();
	  try{
		
		  Map<String,String> pInfoMap=new HashMap<String,String>();	
	      Map<String,String> mSettings=new HashMap<String,String>();
	      Map<String,String> mProperties=new HashMap<String,String>();
	      groupInfoMap.put("adminType", "group");
	      groupInfoMap.put("action", "modify");
	      groupInfoMap.put("parent", "");
	      //groupInfoMap.put("asssignments", new ArrayList()); // to stick to format 
	     // groupInfoMap.put("maturity", "none");
	      groupInfoMap.put("site", "");
	      groupInfoMap.put("child", new ArrayList());
	      groupInfoMap.put("iconFile", ""); 
		  groupInfoMap.put("registryname", strRegistryName);		  
	      Map mChilds =new HashMap();
		  List alChildGroups=new ArrayList();
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
		        if(str1.startsWith("group ") && !bchildren){
		        	groupInfoMap.put("name", str2);
		        	bchildren=true;
		        }  
		        else if (str1.startsWith("description"))
		        {
		        	groupInfoMap.put("description", str2);
		        } else if (str1.startsWith("site"))
		        {
		        	groupInfoMap.put("site", str2);
		        }
		        else if (str1.startsWith("parent"))
		        {
		        	groupInfoMap.put("parent", str2);
		        } 
		        else if (str1.startsWith("child"))
		        {
		        	groupInfoMap.put("child", str2.split(","));
		        	String[] sArray=str2.split(",");
		        	for(String sGroup:sArray){
		        		mChilds=new HashMap();
		        		mChilds.put("name", sGroup);
		        		mChilds.put("flag", "");
		        		alChildGroups.add(mChilds);
		        	}
		        	groupInfoMap.put("child", alChildGroups);
		        }
		        
		        else if(str1.startsWith("nothidden"))
		        {
		        	groupInfoMap.put("hidden", "false");
		        }else if(str1.startsWith("hidden"))
		        {
		        	groupInfoMap.put("hidden", "true");
		        }
		        else if(str1.startsWith("assign person"))
		        {
		        	mChilds=new HashMap();
	        		mChilds.put("name", str2.replace("person ", ""));
	        		mChilds.put("flag", "");
	        		alAssignment.add(mChilds);
		        }
		       }
		      //System.out.println("Groupinfo line 125.........alAssignment......."+alAssignment);
		        groupInfoMap.put("assignment", alAssignment);
				 
			 
	  }catch(Exception e){
		  //System.out.println(" Error in getting role Info");
		  e.printStackTrace();
	  }

	  return _objectMapper.writeValueAsString(groupInfoMap);
	  
   }

	@Override
	public String geSchemaInfoWithPath(Context context, String strSchemaName, String strExportPath) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
}
