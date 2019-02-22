package com.izn.schemamodeler.ui3.portal;

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
 

public class PortalInfo implements SchemaInfo {
	
	public PortalInfo() {
		// TODO Auto-generated constructor stub
	}
	@Override
	public String geSchemaInfo(Context context, String strPortalName, String tbd) throws Exception
	{
		 String strInfo=EMPTY_STRING;
		 String strRegistryName = EMPTY_STRING;
	 try{  
		 String strResult = MqlUtil.mqlCommand(context, "list portal $1", new String[] { strPortalName });
		 //System.out.println("Line 32 PortalInfo strResult is ::::::::::::               "+strResult);
				  
		 if(UIUtil.isNotNullAndNotEmpty(strPortalName))
			 try {
					strResult = MqlUtil.mqlCommand(context, "print portal $1 nothistory", new String[] { strPortalName});
					strRegistryName = MqlUtil.mqlCommand(context, "list property to portal $1", new String[] { strPortalName });
					if(strRegistryName != null && !strRegistryName.equals("")){				 
						String[] split = strRegistryName.trim().split(" ");
						strRegistryName = split[0].trim();
						strRegistryName = strRegistryName.replaceFirst("portal_", "");
					}
			 } catch(Exception ex) {
				 
			 }
		 if (UIUtil.isNotNullAndNotEmpty(strResult)) { strInfo  =  parsePortal(context,strResult,strRegistryName); }  
 
 
	     }catch(Exception e){
		
		 e.printStackTrace();
 	 }
	 return strInfo;
	}
	
   private String parsePortal(Context context,String strMQLResult,String strRegistryName) throws Exception{
	   ObjectMapper _objectMapper = new ObjectMapper();
	   Map pInfoMap=new HashMap();	
	   try{
		      BufferedReader localBufferedReader = new BufferedReader(new StringReader(strMQLResult));
		    
		      pInfoMap.put("adminType", "portal");
		      pInfoMap.put("action", "modify");
			  pInfoMap.put("registryname", strRegistryName);
		      Map mChannels=new HashMap();
		      Map<String,String> mSettings=new HashMap<String,String>();
		      String str1;
		      int iCount=0;
		      List alSettings=new ArrayList();
		      List alChannels=new ArrayList();
		      while ((str1 = localBufferedReader.readLine()) != null)
		      {
		        str1 = str1.trim();
		        int i = str1.indexOf(' ');
		        String str2 = "";
		        if ((i != -1) && (str1.length() > i + 1)) {
		          str2 = UITypeUtil.unquote(str1.substring(i + 1));
		        }
		        if(str1.startsWith("portal")){
		        	pInfoMap.put("name", str2);
		        }
		        else if (str1.startsWith("description"))
		        {
		        	pInfoMap.put("description", str2);
		        }
		        else if (str1.startsWith("icon"))
		        {
		        	pInfoMap.put("icon", str2);
		        }
		        else if (str1.startsWith("label"))
		        {
		        	pInfoMap.put("label", str2);
		        }
		        else if (str1.startsWith("href"))
		        {
		        	pInfoMap.put("href", str2);
		        }
		        else if (str1.startsWith("alt"))
		        {
		        	pInfoMap.put("alt", str2);
		        }else if(str1.startsWith("nothidden")){
		        	pInfoMap.put("hidden", "false");
		        }else if(str1.startsWith("hidden")){
		        	pInfoMap.put("hidden", "true");
		        }
		        else if (str1.startsWith("channel"))
		        {
		        	mChannels=new HashMap();
		        	mChannels.put("name", str2.split(","));
		        	mChannels.put("flag", "");
		        	mChannels.put("order", iCount+1);
		            //mChannels.put("row"+iCount, str2);
		          //  mChannels.put("totalRows", String.valueOf(iCount)); 	
		            iCount++;
		            alChannels.add(mChannels);
		         }   
		        else if (str1.startsWith("setting"))
		         {
		         	  ArrayList slKeuValues= com.izn.schemamodeler.util.UIUtil.parseKeyAndValue(context,str2);
		         	  mSettings = new HashMap();
		        	  mSettings.put((String)slKeuValues.get(0), (String)slKeuValues.get(1));
		        	  mSettings.put("name",(String)slKeuValues.get(0));
			          mSettings.put("value", (String)slKeuValues.get(1));
			          mSettings.put("flag", "");
			          alSettings.add(mSettings);
		         }
		
		      }//end while
		      if(!pInfoMap.containsKey("label")){
		    	  pInfoMap.put("label", "");
		      }
		      pInfoMap.put("settings", alSettings);
		      pInfoMap.put("channels", alChannels);
	 	 }catch(Exception e){
		e.printStackTrace(); 
		 
	 }
	   return _objectMapper.writeValueAsString(pInfoMap);
	}
@Override
public String geSchemaInfoWithPath(Context context, String strSchemaName, String strExportPath) throws Exception {
	// TODO Auto-generated method stub
	return null;
}
	

}
