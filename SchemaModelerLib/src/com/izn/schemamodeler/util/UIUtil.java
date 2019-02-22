package com.izn.schemamodeler.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import matrix.db.Context;
import matrix.db.MQLCommand;
import com.matrixone.apps.domain.util.MqlUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class UIUtil {
	public static String removeCharecter = "@@";
	public static String specialCharecters = "[^a-zA-Z0-9_. ]";
	public static String sUnmarshalExceptionWarningMessage = "Please remove special characters which are not supported for XML and proceed.";
	public static ArrayList parseKeyAndValue(Context context,String strInput) throws Exception
	{
		 ArrayList<String> slKeyValues = new ArrayList<String>(2);
     try{		
		   
    	    int i = strInput.indexOf(" value");
    	    try
    	    {
    	      if (i != -1)
    	      {
    	        String str1 = strInput.substring(0, i);
    	        slKeyValues.add(str1.trim());
    	        String str2 = strInput.substring(i+6 , strInput.length());
    	        slKeyValues.add(str2.trim());
    	       
    	      }
    	    }
    	    catch (Exception localException)
    	    {
    	      localException.printStackTrace();
    	    }
         }catch(Exception e){
    	 e.printStackTrace();
     }
     return slKeyValues;
	}

	public static List<String>  spiltSingleStringToken(Context context,String strDelim,String strInputString) throws Exception
	{
	    List<String> slResult= new LinkedList<String>(); 
	   try{              
		   String[] sResult  = strInputString.split(strDelim);
		   slResult = new LinkedList<String>( Arrays.asList(sResult));// FrameworkUtil.split(strInputString, strDelim);
		 
	    }catch(Exception e ){
		 e.printStackTrace();
	  }
	 return slResult;
	}
	
	public static List getTriggerDetails(Context context,String strTriggerValue)  throws Exception{
		List lTrigger=new ArrayList();
		try{
		  //ArrayList slTriggers=   FrameworkUtil.split(strTriggerValue, ",");
			String[] sResult  = strTriggerValue.split("\\),");
			List<String> slTriggers=Arrays.asList(sResult);
	 	    Map<String,String> mTriggerInfo=new HashMap<String,String>();
		 String token="";
  	      for(int c=0;c<slTriggers.size();c++){
  	    	 mTriggerInfo=new HashMap();
  	    	 token =(String)slTriggers.get(c);
  	    	 if(token.indexOf("\\)")==-1) {
  	    		token=token+")";
  	    	 }
  	          List<String> slResult= spiltSingleStringToken(context,":",token);
  	         if(slResult.size()==1) continue;
  	    	 String token1= (String)slResult.get(0);
  	    	 String token2= (String)slResult.get(1);
  	    	 String strTriggerType="";
	  	    	 if(token1.endsWith("Action")){
	  	    		strTriggerType="Action";
	  	    	 }else if(token1.endsWith("Check")){
	  	    		strTriggerType="Check";
	  	    	 }
	  	    	 else if(token1.endsWith("Override")){
	  	    		strTriggerType="Override";
	  	    	 }
	  	    
	  	   	String triggerAction = token1.substring(0,token1.indexOf(strTriggerType));
	  	   	String strTriggerName = token2.substring(token2.indexOf("(")+1,token2.indexOf(")"));
	  	   	String strProgramName=token2.substring(0,token2.indexOf("("));
	  	   	strTriggerName =strTriggerName.replace(" ", ",");
  	    	strTriggerName =strTriggerName.replace(" ", ",");
	  	  	mTriggerInfo.put("type", strTriggerType);
	  		mTriggerInfo.put("action", triggerAction);
	  		mTriggerInfo.put("name", strTriggerName);
	  		mTriggerInfo.put("program", strProgramName);
	  		lTrigger.add(mTriggerInfo);
  	      }
		}catch(Exception e){
  	    	  e.printStackTrace();
  	      }
		
		return lTrigger;
  	    }
		
		
	public static List<Map> filteredListOMap(List<Map> lsNew,List<HashMap> lsDb){
		
		List<Map> mFiltered=new ArrayList<Map>();	
		try{
			
			Iterator<HashMap> itrDbDef = lsDb.iterator();
			String strNewAction="";
			String strDBAction="";
			String strNewType="";
			String strDBType="";
			String strNewProgram="";
			String strDBProgram="";
			String strNewInput="";
			String strDBInput="";
			boolean bMatchAll=false;
			while(itrDbDef.hasNext()){
				Map mDbDef =  itrDbDef.next();
				strDBAction = (String)mDbDef.get("action");
				Iterator<Map> itrNewDef = lsNew.iterator();
				while(itrNewDef.hasNext()){
				  Map mNewDef=	itrNewDef.next();
				  strNewAction = (String)mNewDef.get("action");
				  if(!strDBAction.equalsIgnoreCase(strNewAction)){
					  continue;
				  }else{
						  strNewType = (String)mNewDef.get("type");
						  strDBType = (String)mDbDef.get("type");
		                  strNewInput = (String)mNewDef.get("name");
						  strDBInput = (String)mDbDef.get("name");

			 			  strNewProgram = (String)mNewDef.get("program");
						  strDBProgram = (String)mDbDef.get("program");
						  if(strNewType.equalsIgnoreCase(strDBType) && strNewProgram.equalsIgnoreCase(strDBProgram) && strNewInput.equalsIgnoreCase(strDBInput) ){
					      }else{
					    	  bMatchAll=false;
					    	  mFiltered.add(mDbDef);
					    	  }
					      }
					
				}
				
			}
			
		   }catch(Exception e){
			e.printStackTrace();
			
		  }
		 
		 return mFiltered;
	  
	   }

	public static String  singleQuoteWithSpace(String strInput){
		
		return " "+singleQuotes(strInput)+" ";
		
	}
	
	public static String  padWithSpaces(String strInput){
		
		return " "+strInput+" ";
		
	}

	public static String  singleQuotes(String strInput){
		
		return "'"+strInput+"'";
		
	}
	
	public static String quoteArgument(String argument)
	{
		String cleanedArgument = argument.trim();
		
		while ((cleanedArgument.startsWith("'")) || (cleanedArgument.startsWith("\""))) {
			cleanedArgument = cleanedArgument.substring(1);
		}
		  
		while ((cleanedArgument.endsWith("'")) || (cleanedArgument.endsWith("\""))) {
			cleanedArgument = cleanedArgument.substring(0, cleanedArgument.length() - 1);
		}
		StringBuilder buf = new StringBuilder();
		if (cleanedArgument.indexOf("\"") > -1) {
			if (cleanedArgument.indexOf("'") > -1) {
				throw new IllegalArgumentException("Can't handle single and double quotes in same argument");
			}    
			return "'" + cleanedArgument + "'";
		}
		if ((cleanedArgument.indexOf("'") > -1) || (cleanedArgument.indexOf(" ") > -1))
		{
			return "\"" + cleanedArgument + "\"";
		}
		return cleanedArgument;
	}
	
		
	public static String removeSchemaObject(String param1, String param2) throws Exception
	{
		StringBuilder sbReturn = new StringBuilder();
		if(param1.startsWith(removeCharecter) && param1.endsWith(removeCharecter)){
			param1 = param1.replace(removeCharecter, "");
			sbReturn.append("delete ").append(param2).append(UIUtil.padWithSpaces(UIUtil.quoteArgument(param1.replace(removeCharecter, ""))));
			if("table".equalsIgnoreCase(param2)){				
				sbReturn.append(" system");
			}
		}else if(param1.indexOf(removeCharecter) > -1){
			throw new Exception("Schema name contain special charecter @ : "+param1);
		}
		return sbReturn.toString();
	}
	
	/**
	 * 
	 * @param param1 : Value
	 * @param param2 : <<attribute, type, relationship.....>>
	 * @return
	 */
	public static String removeFieldDetail(String param1, String param2)
	{
		StringBuilder sbReturn = new StringBuilder();
		if(param1.startsWith(removeCharecter) && param1.endsWith(removeCharecter)){
			param1 = param1.replace(removeCharecter, "");
			sbReturn.append(" remove ").append(param2).append(UIUtil.padWithSpaces(UIUtil.quoteArgument(param1)));
		}else if(!param1.startsWith(removeCharecter) && !param1.endsWith(removeCharecter)){
			sbReturn.append(" add ").append(param2).append(UIUtil.padWithSpaces(UIUtil.quoteArgument(param1)));
		}
		return sbReturn.toString();
	}
	
	/**
	 * 
	 * @param param1 : Value
	 * @param param2 : <<role, group, association>>
	 * @return
	 */
	public static String removeAssignment(String param1, String param2)
	{
		StringBuilder sbReturn = new StringBuilder();
		if(param1.startsWith(removeCharecter) && param1.endsWith(removeCharecter)){
			param1 = param1.replace(removeCharecter, "");
			sbReturn.append(" remove ").append(param2).append(UIUtil.padWithSpaces(UIUtil.quoteArgument(param1)));
		}else if(!param1.startsWith(removeCharecter) && !param1.endsWith(removeCharecter)){
			sbReturn.append(param2).append(UIUtil.padWithSpaces(UIUtil.quoteArgument(param1)));
		}
		return sbReturn.toString();
	}
	
	/**
	 * 
	 * @param param1 : User Name 
	 * @param param2 : User Access Value
	 * @param param3 : [user]
	 * @param param4 : key
	 * @return
	 */
	public static String removeFieldDetail(String param1, String param2, String param3, String param4)
	{
		StringBuilder sbReturn = new StringBuilder();
		if(param1.startsWith(removeCharecter) && param1.endsWith(removeCharecter)){
			param1 = param1.replace(removeCharecter, "");
			sbReturn.append(" remove ").append(param3).append(UIUtil.singleQuoteWithSpace(param1)).append(UIUtil.padWithSpaces(UIUtil.quoteArgument(param2)));
		}else if(!param1.startsWith(removeCharecter) && !param1.endsWith(removeCharecter)){
			String[] splitUserAccess = param2.split(",");
			StringBuilder sbAddedAccess = new StringBuilder();
			StringBuilder sbRemovedAccess = new StringBuilder();
			for(int i=0; i<splitUserAccess.length; i++){
				if(splitUserAccess[i].startsWith(removeCharecter) && splitUserAccess[i].endsWith(removeCharecter)){
					splitUserAccess[i] = splitUserAccess[i].replace(removeCharecter, "");
					sbRemovedAccess.append(splitUserAccess[i]);
					sbRemovedAccess.append(",");
				}else{
					sbAddedAccess.append(splitUserAccess[i]);
					sbAddedAccess.append(",");
				}				
			}
			if(!sbRemovedAccess.toString().isEmpty()){				
				sbRemovedAccess = sbRemovedAccess.deleteCharAt(sbRemovedAccess.toString().length()-1);
				sbReturn.append(" remove user ").append(UIUtil.singleQuoteWithSpace(param1)).append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sbRemovedAccess.toString())));	
			}
			if(!sbAddedAccess.toString().isEmpty()){
				sbAddedAccess = sbAddedAccess.deleteCharAt(sbAddedAccess.toString().length()-1);
				sbReturn.append(" add user ").append(UIUtil.singleQuoteWithSpace(param1));
				if (param4 != null && !param4.isEmpty()) {					
					sbReturn.append(" key ").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(param4)));
				}
				sbReturn.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sbAddedAccess.toString())));				
			}
		}
		return sbReturn.toString();
	}
	/**
	 * 
	 * @param param1 : Setting Name
	 * @param param2 : Setting Value
	 * @param param3 : [Setting]
	 * @return
	 */
	public static String removeSettingValue(String param1, String param2, String param3)
	{
		StringBuilder sbReturn = new StringBuilder();
		if(param1.startsWith(removeCharecter) && param1.endsWith(removeCharecter)){
			param1 = param1.replace(removeCharecter, "");
			sbReturn.append(" remove ").append(param3).append(UIUtil.singleQuoteWithSpace(param1));
		}else if(!param1.startsWith(removeCharecter) && !param1.endsWith(removeCharecter) && !param2.isEmpty()){
			sbReturn.append(" add ").append(param3).append(UIUtil.singleQuoteWithSpace(param1)).append(UIUtil.padWithSpaces(UIUtil.singleQuoteWithSpace(param2)));
		}
		return sbReturn.toString();
	}
	
	public static String removeObjectAccess(List<Map<String, String>> sUser)
	{
		StringBuilder sbReturn = new StringBuilder();
		String sUserValue = "";
		String[] users;
		for (Map<String, String> m : sUser) {
			sUserValue = m.get("value"); 
			users = sUserValue.split(",");	
			sbReturn.append(" remove user all ");
			for (String user : users) {
				if(user.startsWith(removeCharecter) && user.endsWith(removeCharecter)){
					user = user.replace(removeCharecter, "");
					sbReturn.append(" remove user").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(user)));
				}else if(!user.startsWith(removeCharecter) && !user.endsWith(removeCharecter)){
					if(user.isEmpty()){
						user = "all";
					}
					sbReturn.append(" add user").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(user)));
				}
			}
		}
		return sbReturn.toString();
	}
	public static String removeTrigger(String param1, String[] param2)
	{
		StringBuilder sbReturn = new StringBuilder();
		if(param1.startsWith(removeCharecter) && param1.endsWith(removeCharecter)){
			param1 = param1.replace(removeCharecter, "");
			sbReturn.append(" REMOVE TRIGGER").append(UIUtil.padWithSpaces(param1)).append(param2[1]);
		}else if(!param1.startsWith(removeCharecter) && !param1.endsWith(removeCharecter)){
			if((param2[1].startsWith(removeCharecter) && param2[1].endsWith(removeCharecter)) || removeArgumentWithoutQuote(param2[2]).isEmpty()){	
				param2[1] = param2[1].replace(removeCharecter, "");
				sbReturn.append(" REMOVE TRIGGER").append(UIUtil.padWithSpaces(param1)).append(param2[1]);
			}else if(!param2[1].startsWith(removeCharecter) && !param2[1].endsWith(removeCharecter) && removeArgumentWithoutQuote(param2[2]) != null && !removeArgumentWithoutQuote(param2[2]).isEmpty()){				
				sbReturn.append(" ADD TRIGGER").append(UIUtil.padWithSpaces(param1));
				sbReturn.append(param2[1]).append(UIUtil.padWithSpaces(param2[3]));
				sbReturn.append("input").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(removeArgumentWithoutQuote(param2[2]))));
			}
		}
		return sbReturn.toString();
	}
	public static String removeTrigger(List<Map<Object, Object>> listTriggerInfo)
	{
		StringBuilder sbReturn = new StringBuilder();
		Iterator<Map<Object, Object>> itrNTriggers = listTriggerInfo.iterator();
		while (itrNTriggers.hasNext()) {
			Map mTriggerInfo = itrNTriggers.next();
			String sAction = (String)mTriggerInfo.get("action");
			String sType = (String)mTriggerInfo.get("type");
			String sName = (String)mTriggerInfo.get("name");
			String sProgram = (String)mTriggerInfo.get("program");
			if (sAction != null && !sAction.isEmpty() && "trigger".contains(sAction)) {
				sAction = sAction.replace("trigger", "");
			}
			if(sAction.startsWith(removeCharecter) && sAction.endsWith(removeCharecter)){
				sAction = sAction.replace(removeCharecter, "");
				sbReturn.append(" REMOVE TRIGGER").append(UIUtil.padWithSpaces(sAction)).append(sType);
			}else if(!sAction.startsWith(removeCharecter) && !sAction.endsWith(removeCharecter)){
				if((sType.startsWith(removeCharecter) && sType.endsWith(removeCharecter)) || removeArgumentWithoutQuote(sName).isEmpty()){	
					sType = sType.replace(removeCharecter, "");
					sbReturn.append(" REMOVE TRIGGER").append(UIUtil.padWithSpaces(sAction)).append(sType);
				}else if(!sType.startsWith(removeCharecter) && !sType.endsWith(removeCharecter) && removeArgumentWithoutQuote(sName) != null && !removeArgumentWithoutQuote(sName).isEmpty()){				
					sbReturn.append(" ADD TRIGGER").append(UIUtil.padWithSpaces(sAction));
					sbReturn.append(sType).append(UIUtil.padWithSpaces(sProgram));
					sbReturn.append("input").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(removeArgumentWithoutQuote(sName))));
				}
			}
		}
		return sbReturn.toString();
	}
	
	public static String removeArgumentWithoutQuote(String param1)
	{
		String sReturn = "";
		if(param1 != null && !param1.isEmpty()){			
			StringBuilder sbReturn = new StringBuilder();
			String[] split = param1.split(",");
			for(int i=0; i<split.length; i++){			
				if(split[i].startsWith(removeCharecter) && split[i].endsWith(removeCharecter)){
					continue;
				}else if(!split[i].startsWith(removeCharecter) && !split[i].endsWith(removeCharecter)){
					sbReturn.append(split[i]);
					sbReturn.append(",");
				}
			}
			if(!sbReturn.toString().isEmpty() && sbReturn.toString().charAt(sbReturn.toString().length() - 1) == ','){
				sReturn = sbReturn.toString().substring(0, sbReturn.toString().length() - 1);
			}
		}
		return sReturn;
	}
	
	public static String removeArgumentWithQuote(String param1)
	{
		String sReturn = "";
		if(param1 != null && !param1.isEmpty()){			
			StringBuilder sbReturn = new StringBuilder();
			String[] split = param1.split(",");
			for(int i=0; i<split.length; i++){			
				if(split[i].startsWith(removeCharecter) && split[i].endsWith(removeCharecter)){
					continue;
				}else if(!split[i].startsWith(removeCharecter) && !split[i].endsWith(removeCharecter)){
					sbReturn.append(UIUtil.quoteArgument(split[i]));
					sbReturn.append(",");
				}
			}
			if(!sbReturn.toString().isEmpty() && sbReturn.toString().charAt(sbReturn.toString().length() - 1) == ','){
				sReturn = sbReturn.toString().substring(0, sbReturn.toString().length() - 1);
			}
		}
		return sReturn;
	}
	
	public static String getMQLPropertyQuery(Context context,String sSchemaType,String sSchemaName, String sRegistryName, String sOperation) throws Exception
	{
		String sListPropertyMQLCommand = "";
		StringBuilder sbMQL = new StringBuilder();
		String sResult = MQLCommand.exec(context,"version");
		String version = "";
		if(sResult != null && !"".equals(sResult)){
			int beginIndex = sResult.indexOf("R2");
			int lastIndex = sResult.indexOf("x");
			version = sResult.substring(beginIndex, lastIndex);
		}
		version = version + "x";
		SimpleDateFormat sdfDate = new SimpleDateFormat("MM-dd-yyyy");
		Date today = new Date();
		String date = sdfDate.format(today);
		String sName = UIUtil.quoteArgument(sSchemaName);
		String sExistingRegistryName = "";
		if(sRegistryName != null)
		{			
			if("add".equalsIgnoreCase(sOperation))
			{
				if(sRegistryName.isEmpty()){
					sRegistryName = sName.trim();	
				}
				sRegistryName = sRegistryName.replace(" ", "");
				sRegistryName = sSchemaType + "_" + sRegistryName.trim();
				sRegistryName = sRegistryName.replace("\"", "");				
				if("table".equalsIgnoreCase(sSchemaType)){	
					sName = UIUtil.quoteArgument(sSchemaName) + " system ";
				}
				sbMQL.append("add property ").append(sRegistryName).append(" on program eServiceSchemaVariableMapping.tcl to ").append(sSchemaType).append(" ").append(sName).append(";");
			}
			else
			{		
				if("table".equalsIgnoreCase(sSchemaType)){	
					sName = UIUtil.quoteArgument(sSchemaName) + " system ";
					sListPropertyMQLCommand = "list property to $1 $2 system";
				}else{
					sListPropertyMQLCommand = "list property to $1 $2";
				}
				if(!sRegistryName.isEmpty())
				{
					sRegistryName = sRegistryName.replace(" ", "");
					sRegistryName = sSchemaType + "_" + sRegistryName.trim();
					sRegistryName = sRegistryName.replace("\"", "");
					sExistingRegistryName = MqlUtil.mqlCommand(context, sListPropertyMQLCommand, new String[] { sSchemaType,sSchemaName });						
					if(sExistingRegistryName.isEmpty()){	
						sbMQL.append("add property ").append(sRegistryName).append(" on program eServiceSchemaVariableMapping.tcl to ").append(sSchemaType).append(" ").append(sName).append(";");
					}else{
						String[] split = sExistingRegistryName.trim().split(" ");
						sExistingRegistryName = split[0].trim();
						if(!sExistingRegistryName.equals(sRegistryName)){	
							sbMQL.append("delete property ").append(sExistingRegistryName).append(" on program eServiceSchemaVariableMapping.tcl to ").append(sSchemaType).append(" ").append(sName).append(";");	
							sbMQL.append("add property ").append(sRegistryName).append(" on program eServiceSchemaVariableMapping.tcl to ").append(sSchemaType).append(" ").append(sName).append(";");
						}
					}
				}
			}
		}
		sbMQL.append("modify ").append(sSchemaType).append(" ").append(sName).append(" property application value ").append(" Custom");
		sbMQL.append(";");
		sbMQL.append("modify ").append(sSchemaType).append(" ").append(sName).append(" property version value ").append(version);
		sbMQL.append(";");
		sbMQL.append("modify ").append(sSchemaType).append(" ").append(sName).append(" property installer value ").append(" Import");
		sbMQL.append(";");
		sbMQL.append("modify ").append(sSchemaType).append(" ").append(sName).append(" property 'installed date' value ").append(date);
		sbMQL.append(";");
		sbMQL.append("modify ").append(sSchemaType).append(" ").append(sName).append(" property 'original name' value ").append(UIUtil.quoteArgument(sSchemaName));
		sbMQL.append(";");
		return sbMQL.toString();
	}
	
	public static boolean isEqual(String str1, String str2) throws Exception{
		str1 = str1 != null? str1 : "";
		str2 = str2 != null? str2 : "";
		if(str1.equals(str2))
			return true;
		else
			return false;
	}
	
	public static boolean isEqual(List<Map<Object,Object>> list1, List<Map<Object,Object>> list2) throws Exception{
		if(list1.size() != list2.size()){
			return false;
		}
		for (Map<Object,Object> m : list1) {
			if(!list2.contains(m)){
				return false;
			}
		}
		return true;
	}
	
	public static boolean isEqualStringMap(List<Map<String,String>> list1, List<Map<String,String>> list2) throws Exception{
		if(list1.size() != list2.size()){
			return false;
		}
		for (Map<String,String> m : list1) {
			if(!list2.contains(m)){
				return false;
			}
		}
		return true;
	}
	
	public static boolean isEqualStringList(List<String> list1, List<String> list2) throws Exception{
		if(list1.size() != list2.size()){
			return false;
		}
		for (String s : list1) {
			if(!list2.contains(s)){
				return false;
			}
		}
		return true;
	} 
	
	public static boolean compareFile(String file1, String file2) throws Exception
    {    
		File fileFirst=new File(file1);;
		File fileSecond=new File(file2);;
		if(!fileFirst.exists() && !fileSecond.exists()){
			return true;
		} 
		else if(fileFirst.exists() && !fileSecond.exists()){
			if(fileFirst.length()!=0)
				return false;
			else
				return true;
		}
		else if(!fileFirst.exists() && fileSecond.exists()){
			if(fileSecond.length()!=0)
				return false;
			else
				return true;
		}
		else
		{
	        BufferedReader reader1 = new BufferedReader(new FileReader(file1));   
	        BufferedReader reader2 = new BufferedReader(new FileReader(file2)); 
	        String line1 = reader1.readLine();
	        String line2 = reader2.readLine();
	        boolean areEqual = true;
	        int lineNum = 1;
	         while (line1 != null || line2 != null)
	        {
	            if(line1 == null || line2 == null)
	            {
	                areEqual = false;	                
	                break;
	            }
	            else if(! line1.equalsIgnoreCase(line2))
	            {
	                areEqual = false;	                
	                break;
	            }	            
	            line1 = reader1.readLine();            
	            line2 = reader2.readLine();     
	            lineNum++;
	        }
			    
	        reader1.close();   
	        reader2.close();
	        return areEqual;
		}
    }
}
