package com.izn.schemamodeler.admin.dimension;

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
import com.matrixone.apps.domain.util.FrameworkUtil;
import matrix.db.Context;
import matrix.util.StringList;
 

public class DimensionInfo implements SchemaInfo{

   
	 public DimensionInfo() {
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	public String geSchemaInfo(Context context, String strDimensionName, String tbd) throws Exception
	{
		 String strInfo=EMPTY_STRING;
		 String strRegistryName = EMPTY_STRING;
		 Map defaultUnitMap = new HashMap();
		 Map defaultUnitDescriptionMap = new HashMap();
	   try{  
			 String strResult = MqlUtil.mqlCommand(context, "list dimension $1", new String[] { strDimensionName });
			 //System.out.println("Line 32 DimentionInfo strResult is ::::::::::::               "+strResult);
					  
			 if(UIUtil.isNotNullAndNotEmpty(strDimensionName))
				 try {
					strResult = MqlUtil.mqlCommand(context, "print dimension $1 nothistory", new String[] { strDimensionName});
				 	strRegistryName = MqlUtil.mqlCommand(context, "list property to dimension $1", new String[] { strDimensionName });
					if(strRegistryName != null && !strRegistryName.equals("")){				 
						String[] split = strRegistryName.trim().split(" ");
						strRegistryName = split[0].trim();
						strRegistryName = strRegistryName.replaceFirst("dimension_", "");
					}
					String strUnit = MqlUtil.mqlCommand(context, "print dimension $1 select $2 dump $3", new String[] { strDimensionName , "unit", "|"});
					StringList unitSplit = FrameworkUtil.split(strUnit, "|");
					for (int i =0; i < unitSplit.size(); i++){
						String sUnit = (String)unitSplit.get(i);
						String strUnitDefault = MqlUtil.mqlCommand(context, "print dimension $1 select $2 dump ", new String[] { strDimensionName, "unit["+sUnit+"].default"});
						defaultUnitMap.put(sUnit,strUnitDefault);
						String strUnitDescription = MqlUtil.mqlCommand(context, "print dimension $1 select $2 dump ", new String[] { strDimensionName, "unit["+sUnit+"].description"});
						defaultUnitDescriptionMap.put(sUnit,strUnitDescription);
					}
				 } catch(Exception ex) {
					 
				 }
			 if (UIUtil.isNotNullAndNotEmpty(strResult)) { strInfo  =  parseDimension(context,strResult,strRegistryName,defaultUnitMap,defaultUnitDescriptionMap); }  
	 
	 	 
	     }catch(Exception e){
		
		 e.printStackTrace();
 	 }
	   return strInfo;
	}
	
	
	 private String parseDimension(Context context,String strMQLResult,String strRegistryName, Map defaultUnitMap,Map defaultUnitDescriptionMap) throws Exception{
		 ObjectMapper _objectMapper = new ObjectMapper();
       Map dimensionInfoMap =new LinkedHashMap();
       List<Map> lstSettings = new ArrayList<Map>();
	  try{
		    dimensionInfoMap.put("adminType", "dimension");
		    dimensionInfoMap.put("action", "modify");
		    dimensionInfoMap.put("registryname", strRegistryName);
		  Map  mUnits=new LinkedHashMap<String,String>();	
	      List alUnits=new ArrayList();
	      String strUnit="";
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
		          if(str1.startsWith("dimension") ){
		        	dimensionInfoMap.put("name", str2);
		        	
		        }  
		        else if (str1.startsWith("description"))
		        {
		        	dimensionInfoMap.put("description", str2);
		        }
		        else if(str1.startsWith("nothidden"))
		        {
		        	dimensionInfoMap.put("hidden", "false");
		        }else if(str1.startsWith("hidden"))
		        {
		        	dimensionInfoMap.put("hidden", "true");
		        }
		        else if(str1.startsWith("unit"))
		        {
					
		          if(!strUnit.equalsIgnoreCase(str2) && !mUnits.isEmpty()){
		        	  mUnits.put("setting", lstSettings);
		        		 alUnits.add(mUnits);
		        		 mUnits=new HashMap();
		        		 lstSettings=new ArrayList<Map>();
		        	}
		        	 strUnit=str2;
		        	 mUnits.put("dbunit", str2) ;
		        	 mUnits.put("unit", str2) ;
		        	 mUnits.put("unitdescription", defaultUnitDescriptionMap.get(str2)) ;
					 mUnits.put("default", (String)defaultUnitMap.get(str2) ) ;
			     }else if(str1.startsWith("label")){
		        		mUnits.put("label", str2.trim()) ;
		         }else if(str1.startsWith("multiplier")) {
		        		mUnits.put("multiplier", str2.trim()) ;
		         }else if(str1.startsWith("offset")) {
		        		mUnits.put("offset", str2.trim()) ;
		         }else if(str1.startsWith("setting")){
		        	 Map mSetting = new HashMap();
		          	 ArrayList slSetting  = com.izn.schemamodeler.util.UIUtil.parseKeyAndValue(context, str2);
		         	 mSetting.put("name", (String)slSetting.get(0));
		         	 mSetting.put("value", (String)slSetting.get(1));
		         	 lstSettings.add(mSetting);
		         }
		       }
		       mUnits.put("setting", lstSettings);
		       alUnits.add(mUnits);
		      dimensionInfoMap.put("units", alUnits);

				 
		 
	  }catch(Exception e){
		  //System.out.println(" Error in getting dimension Info");
		  e.printStackTrace();
	  }
	  return _objectMapper.writeValueAsString(dimensionInfoMap);	  
}


	@Override
	public String geSchemaInfoWithPath(Context context, String strSchemaName, String strExportPath) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
