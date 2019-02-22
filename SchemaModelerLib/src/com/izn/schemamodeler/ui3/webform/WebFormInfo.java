package com.izn.schemamodeler.ui3.webform;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.izn.schemamodeler.SchemaInfo;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
 
public class WebFormInfo implements SchemaInfo{
	
	public WebFormInfo() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String geSchemaInfo(Context context, String strWebformName, String tbd) throws Exception
	{
		 String strInfo=EMPTY_STRING;
		 String strRegistryName = EMPTY_STRING;		  
	 try{  
		 String strResult = MqlUtil.mqlCommand(context, "list form $1", new String[] { strWebformName });
		 //System.out.println("Line 28 WebformNameInfo strResult is ::::::::::::               "+strResult);
		 if(UIUtil.isNotNullAndNotEmpty(strWebformName))
			 try {
				 strResult = MqlUtil.mqlCommand(context, "print form $1 nothistory", new String[] { strWebformName});
				 strRegistryName = MqlUtil.mqlCommand(context, "list property to form $1", new String[] { strWebformName});
				 if(strRegistryName != null && !strRegistryName.equals("")){				 
					 String[] split = strRegistryName.trim().split(" ");
					 strRegistryName = split[0].trim();
					 strRegistryName = strRegistryName.replaceFirst("form_", "");
				 }
			 } catch(Exception ex) {
				 
			 }
		 if (UIUtil.isNotNullAndNotEmpty(strResult)) { strInfo  =  parseWebForm(context,strResult,strRegistryName); }  
 
	     }catch(Exception e){
	  	 e.printStackTrace();
 	 }
	 return strInfo;
	}
	
	
	 private String parseWebForm(Context context,String strMQLResult, String strRegistryName) throws Exception{
		 ObjectMapper _objectMapper = new ObjectMapper();
		 HashMap  webFormInfo = new LinkedHashMap();
	  try{
		    
		    webFormInfo.put("adminType", "webForm");
		    webFormInfo.put("action", "modify");
		    webFormInfo.put("registryname", strRegistryName);
		    Map formFieldInfo = new LinkedHashMap<>();
		    HashMap fieldData = new LinkedHashMap();
		    HashMap mUsers=new HashMap();
		    ArrayList slUsers = new ArrayList();
		    String str1 = "";
		    int i = 1;
		  /*    strMQLResult =   " setting      Editable value true\n"
            +"setting      Field Type value attribute\n"
             +"setting      Registered Suite value EngineeringCentral\n"
 +"setting      Show Clear Button value true\n"
 +"setting      format value user\n"
 +"user         all"*/;
		    BufferedReader localBufferedReader = new BufferedReader(new StringReader(strMQLResult));
		   
		    List alUsers = new ArrayList();
		    List alSettings=new ArrayList();
		    List alFields=new ArrayList();
		    String str2;
		    int iFielCount=1;
		    boolean bFieldStart=false;
		    boolean bAnyField=false;
		     Map mSettings=new HashMap();
		     String sOrderDB="1430403030";
		     String sCacheOderDB="";
		      while ((str2 = localBufferedReader.readLine()) != null)
		      {  
		        str2 = str2.trim();
		        int j = str2.indexOf(' ');
		        String str3 = "";
		        if ((j != -1) && (str2.length() > j + 1)) {
		          str3 = str2.substring(j + 1).trim();
		        }
		        if(str2.isEmpty()) continue;
		       if(str2.startsWith("form") && !bFieldStart){
		          webFormInfo.put("name", str3);
		        }
		       else if (str2.startsWith("description"))
		        {
		        	webFormInfo.put("description", str3);
		        }else if(str2.startsWith("nothidden"))
		        {
		        	webFormInfo.put("hidden", "false");
		        }else if(str2.startsWith("hidden"))
		        {
		        	webFormInfo.put("hidden", "true");
		        }else if(str2.startsWith("type")){
		        	
		        	webFormInfo.put("type", str3);
		        }
		        else
		        {
		          ArrayList localStringList2 = new ArrayList();
		          if (str2.startsWith("property"))
		          {
		         
		          }
		          else
		          {
		            
		            if (str2.startsWith("field#"))
		            {  
		            	bAnyField=true;
		              	sCacheOderDB=str2.substring(6,str2.indexOf("select")).trim();;
		            	 	
		            	bFieldStart=true;
		                if(bFieldStart && iFielCount>1){
		            	  
		                	
		            	 // fieldData.put("fieldBasic", fieldBasicInfo);
		            	 // fieldData.put("objectAccess", slUsers);
		           	     // formFieldInfo.put("field"+(iFielCount-1), fieldData);
		                	
			           	     fieldData.put("settings", alSettings);
			              	 fieldData.put("objectAccess", slUsers);
			              	 fieldData.put("orderDB", sOrderDB);
			              	 fieldData.put("order", String.valueOf(iFielCount-1));
			              	 alFields.add(fieldData);
		           	      
		           	      //clear for next push 
		            	  mSettings=new HashMap();
		            	  fieldData=new HashMap();
		            	  slUsers =new ArrayList();
		            	  alSettings=new ArrayList();
		              }
		              sOrderDB = str2.substring(6,str2.indexOf("select")).trim();
		              iFielCount++;
		              int k = str2.indexOf("select ");
		              if (k > 0)
		              {
		                str3 = str2.substring(k + "select ".length());
		              
		                fieldData.put("expression", str3);
		               }
		            }
		            else if (str2.startsWith("expressiontype"))
		            {
		              if ((((String)str3).equalsIgnoreCase("businessobject"))) {
		            	 // fieldData.put("appliesTo",str3); 
		            	  fieldData.put("columnType",str3);
		              } else if ( (((String)str3).equalsIgnoreCase("relationship"))) {
		            	 // fieldData.put("appliesTo",str3);
		            	  fieldData.put("columnType",str3);
		              } else{
		            	  fieldData.put("columnType","");
		              }
		            }
		            else if (str2.startsWith("label"))
		            {
		            	fieldData.put("label", str3);
		            }
		            else if (str2.startsWith("name"))
		            {
		            	//fieldData.put("name", str3);
		            	 fieldData.put("column", str3);
		            }
		            else if (str2.startsWith("href"))
		            {
		            	fieldData.put("href", str3);
		            }
		            else if (str2.startsWith("alt"))
		            {
		            	fieldData.put("alt", str3);
		            }
		            else if (str2.startsWith("order"))
		            {
		            	fieldData.put("order", str3);
		            }
		            else if (str2.startsWith("range"))
		            {
		            	fieldData.put("range", str3);
		            }
		            else if (str2.startsWith("program"))
		            {
		            	fieldData.put("program", str3);
		            }else if(str2.startsWith("update")){
		            	fieldData.put("update", str3); // Update URL
		            }

		            else if (str2.startsWith("user"))
		            {
		              if (((String)str3).length() > 0)
		              {
		             	 // slUsers.addElement((String)str3);
		             	  
		             	 mUsers=new HashMap();
				        	mUsers.put("name", str2.substring(4, str2.length()).trim());
				        	mUsers.put("flag", "");
				        	slUsers.add(mUsers);
		              }
		            }
		            else if (str2.startsWith("setting"))
		            {
		               	 ArrayList slKeyValues= com.izn.schemamodeler.util.UIUtil.parseKeyAndValue(context,str3);
			        	 //mSettings.put((String)slKeuValues.elementAt(0), (String)slKeuValues.elementAt(1));
		              	 if(slKeyValues.size()==2){
				        	 mSettings=new HashMap();
				        	 mSettings.put("name",(String)slKeyValues.get(0));
				        	 mSettings.put("value", (String)slKeyValues.get(1));
				        	 mSettings.put("flag", "");
				        	 alSettings.add(mSettings);
		               	 }
		              }
		            }
		          }
		        }
		      
		      // push last missed field entry ..... 
		       if(bAnyField) {
			       fieldData.put("order", String.valueOf(iFielCount-1));
			       fieldData.put("orderDB", sCacheOderDB);
			       fieldData.put("settings", alSettings);
	        	   fieldData.put("objectAccess", slUsers);
	        	   alFields.add(fieldData);
		       }
	       	       webFormInfo.put("fields", alFields);
		       
		     
       	       
		     
		      
    	  }catch(Exception e){
		  //System.out.println(" Error in getting webform Info");
		  e.printStackTrace();
	  }

	  return _objectMapper.writeValueAsString(webFormInfo);
	 }

	@Override
	public String geSchemaInfoWithPath(Context context, String strSchemaName, String strExportPath) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
}
