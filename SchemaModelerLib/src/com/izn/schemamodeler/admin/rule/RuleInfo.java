package com.izn.schemamodeler.admin.rule;

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


public class RuleInfo implements SchemaInfo {
	
	public RuleInfo() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String geSchemaInfo(Context context, String strRuleName, String tbd) throws Exception
	{
		 String strInfo=EMPTY_STRING;
	 try{  
		 String strResult = MqlUtil.mqlCommand(context, "list Rule $1", new String[] { strRuleName });
		 //System.out.println("Line 32 ruleInfo strResult is ::::::::::::               "+strResult);
				  
		 if(UIUtil.isNotNullAndNotEmpty(strRuleName))
			 try {
			 strResult = MqlUtil.mqlCommand(context, "print Rule $1 nothistory", new String[] { strRuleName});
			 } catch(Exception ex) {
				 
			 }
		 if (UIUtil.isNotNullAndNotEmpty(strResult)) { strInfo  =  parseRule(context,strResult); }  
 
	    
	     }catch(Exception e){
		
		 e.printStackTrace();
       }
	 return strInfo;
	}
	
	
	 private String parseRule(Context context,String strMQLResult) throws Exception{
		 ObjectMapper _objectMapper = new ObjectMapper();
        Map ruleInfoMap =new LinkedHashMap();
        Map mUsers=new HashMap();
        ArrayList slUsers = new ArrayList();
	  try{
		  
		  Map<String,String> mInfoMap=new HashMap<String,String>();	
	      Map<String,String> mSettings=new HashMap<String,String>();
	      Map<String,String> mProperties=new HashMap<String,String>();
	      
	      ruleInfoMap.put("adminType", "rule");
	      ruleInfoMap.put("action", "modify");
	      List lstEmpty=  new ArrayList();
	      ruleInfoMap.put("governedPrograms", lstEmpty);
	      ruleInfoMap.put("governedAttribute", lstEmpty);
	      ruleInfoMap.put("governedForms", lstEmpty);
	      ruleInfoMap.put("governedRelationships", lstEmpty); 
	      
	      
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
		        if(str1.startsWith("rule") && !bchildren){
		        	ruleInfoMap.put("name", str2);
		        	bchildren=true;
		        }  
		        else if (str1.startsWith("description"))
		        {
		        	ruleInfoMap.put("description", str2);
		        }
		        
		        else if (str1.startsWith("program"))
		        {
		        	ruleInfoMap.put("governedPrograms", str2.split(","));
		        }
		        else if (str1.startsWith("attribute"))
		        {
		        	ruleInfoMap.put("governedAttribute", str2.split(","));
		        }
		        else if(str1.startsWith("nothidden"))
		        {
		        	ruleInfoMap.put("hidden", "false");
		        	
		        }else if(str1.startsWith("hidden"))
		        {
		        	ruleInfoMap.put("hidden", "true");
		        }
		        else if(str1.startsWith("form"))
		        {
		        	ruleInfoMap.put("governedForms",  str2.split(","));
		        	
		        } else if(str1.startsWith("Relationship"))
		        {
		        	str2 = str2.replaceAll("Type:", ""); 
		        		
		        	//System.out.println("Str2 is ........"+str2);
		        	 ruleInfoMap.put("governedRelationships",  str2.split(","));
		        }
		        
		        else if (str1.startsWith("login user") || str1.startsWith("user"))
		        {
		        	//slUsers.add(str2);
		        	//System.out.println("Str2 is ........"+str2);
		        	mUsers=new HashMap();
		        	mUsers.put("name", str2.split(" ")[0]);
		        	mUsers.put("flag", str2.split(" ")[1]);
		        	slUsers.add(mUsers);
		         }
		        ruleInfoMap.put("objectAccess",slUsers);
		       }
		      //System.out.println("ruleInfoMap is. .........."+ruleInfoMap);
		  }catch(Exception e){
		  //System.out.println(" Error in getting rule Info");
		  e.printStackTrace();
	  }

	  return _objectMapper.writeValueAsString(ruleInfoMap);
    }

	@Override
	public String geSchemaInfoWithPath(Context context, String strSchemaName, String strExportPath) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
}
