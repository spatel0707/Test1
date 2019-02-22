package com.izn.schemamodeler.ui3.menu;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.izn.schemamodeler.SchemaInfo;
import com.matrixone.apps.cache.UITypeUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;

import matrix.db.Context;
 

public class MenuInfo implements SchemaInfo {

	public String geSchemaInfo(Context context, String strMenuName, String tbd) throws Exception
	{
		String strInfo=EMPTY_STRING;
		String strRegistryName = "";
	 try{  
		 String strResult = MqlUtil.mqlCommand(context, "list menu $1", new String[] { strMenuName });
		 //System.out.println("Line 32 MenuInfo strResult is ::::::::::::               "+strResult);
				  
		 if(UIUtil.isNotNullAndNotEmpty(strMenuName))
			 try {
				  strResult = MqlUtil.mqlCommand(context, "print menu $1 nothistory", new String[] { strMenuName});
		          strRegistryName = MqlUtil.mqlCommand(context, "list property to menu $1", new String[] { strMenuName });
		          if (strRegistryName != null)
		          {
		            String[] split = strRegistryName.split(" ");
		            strRegistryName = split[0];
					strRegistryName = strRegistryName.replaceFirst("menu_", "");
		            strResult = strResult + "\nregistryname " + strRegistryName + "\n";
		          }
			 } catch(Exception ex) {
				 
			 }
		 if (UIUtil.isNotNullAndNotEmpty(strResult)) { strInfo  =  parseMenu(context,strResult); }  
 
	    
	     }catch(Exception e){
		
		 e.printStackTrace();
 	 }
	 return strInfo;
	}
	
	
	 private String parseMenu(Context context,String strMQLResult) throws Exception{
     
		 ObjectMapper _objectMapper = new ObjectMapper();
		  Map menuInfoMap =new HashMap();
	  try{
		 
		  Map<String,String> pInfoMap=new HashMap<String,String>();	
	      Map<String,String> mSettings=new HashMap<String,String>();
	      Map<String,String> mProperties=new HashMap<String,String>();
			  menuInfoMap.put("adminType", "menu");
			  menuInfoMap.put("action", "modify");
		  ArrayList slMenus=new ArrayList();
		  List alItems=new ArrayList();
		  List alSettings=new ArrayList();
		  Map mItems=new HashMap();
		  int i=0;
		  int iCount=1;
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
		        if (str1.startsWith("registryname"))
		        {
		          menuInfoMap.put("registryname", str2);
		        }
		        if(str1.startsWith("menu") && !bchildren){
		        	menuInfoMap.put("name", str2);
		        	bchildren=true;
		        }  
		        else if (str1.startsWith("description"))
		        {
		        	menuInfoMap.put("description", str2);
		        }
		        else if (str1.startsWith("icon"))
		        {
		        	menuInfoMap.put("icon", str2);
		        }
		        else if (str1.startsWith("label"))
		        {
		        	menuInfoMap.put("label", str2);
		        }
		        else if (str1.startsWith("href"))
		        {
		        	menuInfoMap.put("href", str2);
		        }
		        else if (str1.startsWith("alt"))
		        {
		        	menuInfoMap.put("alt", str2);
		        }else if(str1.startsWith("nothidden"))
		        {
		        	menuInfoMap.put("hidden", "false");
		        }else if(str1.startsWith("hidden"))
		        {
		        	menuInfoMap.put("hidden", "true");
		        }
		        else if (str1.startsWith("input"))
		        {
		        	menuInfoMap.put("input", str2);
		        }
		        else  if ((str1.startsWith("menu")) && bchildren)
		          {
		        	mItems=new HashMap();
		        	mItems.put("name", str2);
		        	mItems.put("flag", "");
		        	mItems.put("type", "menu");
		        	mItems.put("order", String.valueOf(iCount));
		        	 alItems.add(mItems);
		        	 iCount++;
		          }
		          else if ((str1.startsWith("command")) && bchildren)
		          {
		        	 // slCommands.add(str2);
		        	  mItems=new HashMap();
			        	mItems.put("name", str2);
			        	mItems.put("flag", "");
			        	mItems.put("type", "command");
			        	mItems.put("order", String.valueOf(iCount));
			        	 alItems.add(mItems);
			        	 iCount++;
		          }
		          else if(str1.startsWith("property"))
		          
		            {
		            	  ArrayList slKeuValues = com.izn.schemamodeler.util.UIUtil.parseKeyAndValue(context, str2);
			          	 // mProperties.put((String)slKeuValues.get(0), (String)slKeuValues.get(1));
			      
		            }
		            else if (str1.startsWith("setting"))
		            {
		            	  ArrayList slKeyValues= com.izn.schemamodeler.util.UIUtil.parseKeyAndValue(context,str2);
			        	  mSettings=new HashMap();
			        	  mSettings.put("name", (String)slKeyValues.get(0));
			        	  mSettings.put("value", (String)slKeyValues.get(1));
			        	  mSettings.put("flag", "");
			        	  alSettings.add(mSettings);
			      
		            }
		          }
		      
		      
		      if(!menuInfoMap.containsKey("label")){
		    	  menuInfoMap.put("label", "");
		      }
		 
		         menuInfoMap.put("items", alItems);
		         menuInfoMap.put("settings", alSettings);
				  
				 
				 
		 
	  }catch(Exception e){
		  //System.out.println(" Error in getting menu Info");
		  e.printStackTrace();
	  }
	  return _objectMapper.writeValueAsString(menuInfoMap);
	 }


	@Override
	public String geSchemaInfoWithPath(Context context, String strSchemaName, String strExportPath) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}
