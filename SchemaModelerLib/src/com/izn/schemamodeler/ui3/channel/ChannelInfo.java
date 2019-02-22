package com.izn.schemamodeler.ui3.channel;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.izn.schemamodeler.SchemaInfo;
import com.matrixone.apps.cache.UITypeUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;

import matrix.db.Context;
 

public class ChannelInfo implements SchemaInfo{
	
	public ChannelInfo() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String geSchemaInfo(Context context, String strChannelName, String tbd) throws Exception
	{
		 String strInfo=EMPTY_STRING;
		 String strRegistryName = "";
	 try{  
		 String strResult = MqlUtil.mqlCommand(context, "list channel $1", new String[] { strChannelName });
		 //System.out.println("Line 32 ChannelInfo strResult is ::::::::::::               "+strResult);
				  
		 if(UIUtil.isNotNullAndNotEmpty(strChannelName))
			 try {
				  strResult = MqlUtil.mqlCommand(context, "print channel $1 nothistory", new String[] { strChannelName});
		          strRegistryName = MqlUtil.mqlCommand(context, "list property to channel $1", new String[] { strChannelName });
		          if (strRegistryName != null)
		          {
		            String[] split = strRegistryName.split(" ");
		            strRegistryName = split[0];
					strRegistryName = strRegistryName.replaceFirst("channel_", "");
		            strResult = strResult + "\nregistryname " + strRegistryName + "\n";
		          }
			 } catch(Exception ex) {
				 
			 }
		 if (UIUtil.isNotNullAndNotEmpty(strResult)) { strInfo  =  parseChannel(context,strResult); }  
 
	     }catch(Exception e){
		
		 e.printStackTrace();
 	 }
	 return strInfo;
	}
	
	
	 private String parseChannel(Context context,String strMQLResult) throws Exception{
		 ObjectMapper _objectMapper = new ObjectMapper();
		  Map channelMap=new HashMap();
	  try{
		  
		  BufferedReader localBufferedReader = new BufferedReader(new StringReader(strMQLResult));
		  channelMap.put("adminType", "channel");
		  channelMap.put("action", "modify");
		     Map<String,String> mProperties=new HashMap<String,String>();
		     Map mSettings=new HashMap();
		     Map mCommands=new HashMap();
		     List alSettings=new ArrayList();
		     List alCommands=new ArrayList();
		      String str1="";;
		      while ((str1 = localBufferedReader.readLine()) != null)
		      {
		        str1 = str1.trim();
		        int i = str1.indexOf(' ');
		        String str2 = "";
		        if ((i != -1) && (str1.length() > i + 1)) {
		          str2 = UITypeUtil.unquote(str1.substring(i + 1));
		        }
		        if (str1.startsWith("registryname"))
		        {
		        	channelMap.put("registryname", str2);
		        }
		        if(str1.startsWith("channel")){
		        	channelMap.put("name", str2);
		        }
		        else if (str1.startsWith("description"))
		        {
		        	channelMap.put("description", str2);
		        }
		        else if (str1.startsWith("icon"))
		        {
		        	channelMap.put("icon", str2);
		        }
		        else if (str1.startsWith("label"))
		        {
		        	channelMap.put("label", str2);
		        }
		        else if (str1.startsWith("href"))
		        {
		        	channelMap.put("href", str2);
		        }
		        else if (str1.startsWith("alt"))
		        {
		        	channelMap.put("alt", str2);
		        }else if(str1.startsWith("nothidden")){
		        	channelMap.put("hidden", "false");
		        }else if(str1.startsWith("hidden")){
		        	channelMap.put("hidden", "true");
		        }
		        else if (str1.startsWith("height"))
		        {
		          if (str2.indexOf("=") != -1) {
		            str2 = str2.substring(str2.indexOf("=") + 1, str2.length());
		          }
		          channelMap.put("height", str2);
		        }
		        else if (str1.startsWith("command"))
		        { int iCnt=1;
		          StringTokenizer localStringTokenizer = new StringTokenizer(str2, ",");
		          while (localStringTokenizer.hasMoreTokens())
		          {
		            // slCommands.add((String)localStringTokenizer.nextToken());
		             mCommands=new HashMap();
		             mCommands.put("name", (String)localStringTokenizer.nextToken());
		             mCommands.put("order", iCnt);
		             mCommands.put("flag", "");
		        	 alCommands.add(mCommands);
		        	 iCnt++;
		          }
		        }
		        else
		        {
		          
		          if (str1.startsWith("property"))
		          {
		        	  ArrayList slKeuValues = com.izn.schemamodeler.util.UIUtil.parseKeyAndValue(context, str2);
		          	  mProperties.put((String)slKeuValues.get(0), (String)slKeuValues.get(1));
		      
		          }
		          else if (str1.startsWith("setting"))
		          {
		        	  ArrayList slKeyValues= com.izn.schemamodeler.util.UIUtil.parseKeyAndValue(context,str2);
		        	  mSettings=new HashMap();
		        	  mSettings.put("name", (String)slKeyValues.get(0));
		        	  mSettings.put("value", (String)slKeyValues.get(1));
		        	  mSettings.put("flag", "");
		        	  alSettings.add(mSettings);
		        	 // mSettings.put((String)slKeuValues.get(0), (String)slKeuValues.get(1));
		           }
		        }
		      }
		       if(!channelMap.containsKey("label")){
		    	   channelMap.put("label", ""); 
		       }
		      channelMap.put("settings", alSettings);
		      channelMap.put("commands", alCommands);
		 	 
	  }catch(Exception e){
		  //System.out.println(" Error in getting Channel Info");
		  e.printStackTrace();
	  }
	  return _objectMapper.writeValueAsString(channelMap);
	 }

	@Override
	public String geSchemaInfoWithPath(Context context, String strSchemaName, String strExportPath) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
			

}
