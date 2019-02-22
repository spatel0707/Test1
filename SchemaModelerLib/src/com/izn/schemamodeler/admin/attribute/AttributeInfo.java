package com.izn.schemamodeler.admin.attribute;

import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.izn.schemamodeler.SchemaInfo;
import com.matrixone.apps.cache.UITypeUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import matrix.db.Context;

public class AttributeInfo
  implements SchemaInfo
{
  Map mOpearators = new HashMap();
  
  public AttributeInfo()
  {
    this.mOpearators.put("=", "equal");
    this.mOpearators.put("!=", "notEqual");
    this.mOpearators.put(">", "greaterThan");
    this.mOpearators.put(">=", "greaterThanEqual");
    this.mOpearators.put("<", "lessThan");
    this.mOpearators.put("<=", "lessThanEqual");
    this.mOpearators.put("!match", "notMatch");
    this.mOpearators.put("!match", "notMatch");
    this.mOpearators.put("match", "match");
    this.mOpearators.put("!smatch", "notStringMatch");
    this.mOpearators.put("smatch", "stringMatch");
    this.mOpearators.put("between", "between");
    this.mOpearators.put("programRange", "programRange");
  }
  
  public String geSchemaInfo(Context context, String strAttributeName, String tbd)
    throws Exception
  {
    String strInfo = EMPTY_STRING;
    String strResult = null;
    String strRegistryName = EMPTY_STRING;
    try
    {
      strResult = MqlUtil.mqlCommand(context, "list attribute '$1'", new String[] { strAttributeName });
      if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strAttributeName)) {
        try
        {
			strResult = MqlUtil.mqlCommand(context, "print attribute '$1' nothistory", new String[] { strAttributeName });
			strRegistryName = MqlUtil.mqlCommand(context, "list property to attribute $1", new String[] { strAttributeName });
			if(strRegistryName != null && !strRegistryName.equals("")){				 
				String[] split = strRegistryName.trim().split(" ");
				strRegistryName = split[0].trim();
				strRegistryName = strRegistryName.replaceFirst("attribute_", "");
			}
        }
        catch (Exception localException1) {}
      }
      if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
        strInfo = parseAttribute(context, strResult, strRegistryName);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
    return strInfo;
  }
  
  private String parseAttribute(Context context, String strMQLResult, String strRegistryName)
    throws Exception
  {
	ObjectMapper _objectMapper = new ObjectMapper();
    Map attributeInfoMap = new LinkedHashMap();
    try
    {
      Map<String, String> pInfoMap = new HashMap();
      Map<String, String> mSettings = new HashMap();
      Map<String, Map<String, ArrayList>> mRangeValues = new HashMap();
      Map mCloneInfo = new HashMap();
      mCloneInfo.put("revision", "false");
      mCloneInfo.put("clone", "false");
      Map mValueTypeInfo = new HashMap();
      mValueTypeInfo.put("multiValue", "false");
      mValueTypeInfo.put("singleValue", "false");
      mValueTypeInfo.put("rangeValue", "false");
      Map<String, String> mProperties = new HashMap();
      attributeInfoMap.put("adminType", "attribute");
      attributeInfoMap.put("action", "modify");
      attributeInfoMap.put("dimension", "");
      attributeInfoMap.put("trigger", new ArrayList());
      Map mScopeInfo = new HashMap();
      mScopeInfo.put("relationship", "false");
      mScopeInfo.put("global", "false");
      mScopeInfo.put("type", "false");
      mScopeInfo.put("interface", "false");
      attributeInfoMap.put("ownerkind", "");
      attributeInfoMap.put("maxlength", "0");
      attributeInfoMap.put("registryname", strRegistryName);
	  
      List slRanges = new ArrayList();
      
      int i = 0;
      BufferedReader localBufferedReader = new BufferedReader(new StringReader(strMQLResult));
      boolean bchildren = false;
      String str1;
      while ((str1 = localBufferedReader.readLine()) != null)
      {
         str1 = str1.trim();
        int j = str1.indexOf(' ');
        String str2 = "";
        if ((j != -1) && (str1.length() > j + 1)) {
          str2 = UITypeUtil.unquote(str1.substring(j + 1));
        }
        if (str1.startsWith("attribute")) {
          attributeInfoMap.put("name", str2);
        }
        if (str1.startsWith("type")) {
          attributeInfoMap.put("type", str2);
        }
        if (str1.startsWith("nameOld")) {
          attributeInfoMap.put("nameOld", str2);
        }
        if (str1.startsWith("description")) {
          attributeInfoMap.put("description", str2);
        }
        if (str1.startsWith("dimension")) {
          attributeInfoMap.put("dimension", str2);
        }
        if (str1.startsWith("default"))
        {
          attributeInfoMap.put("default", str2);
        }
        else if (str1.startsWith("multiline"))
        {
          attributeInfoMap.put("multiline", "true");
        }
        else if (str1.startsWith("multivalue"))
        {
          mValueTypeInfo.replace("multiValue", "true");
        }
        else if (str1.startsWith("rangevalue"))
        {
          mValueTypeInfo.replace("rangeValue", "true");
        }
        else if (str1.startsWith("abstract"))
        {
          attributeInfoMap.put("abstract", str2);
        }
        else if (str1.startsWith("nothidden"))
        {
          attributeInfoMap.put("hidden", "false");
        }
        else if (str1.startsWith("hidden"))
        {
          attributeInfoMap.put("hidden", "true");
        }
        else if (str1.startsWith("trigger"))
        {
          List ltTrigger = com.izn.schemamodeler.util.UIUtil.getTriggerDetails(context, str2);
          attributeInfoMap.put("trigger", ltTrigger);
        }
        else if (str1.startsWith("maxlength"))
        {
          attributeInfoMap.put("maxlength", str2);
        }
        else if (str1.startsWith("resetOnClone"))
        {
          mCloneInfo.put("clone", "true");
        }
        else if (str1.startsWith("resetOnRevision"))
        {
          mCloneInfo.put("revision", "true");
        }
        else if (str1.startsWith("ownerkind"))
        {
          attributeInfoMap.put("ownerkind", str2);
          mScopeInfo.replace(str2, "true");
        }
        else if (str1.startsWith("owner"))
        {
          attributeInfoMap.put("owner", str2);
        }
        else if (str1.startsWith("range"))
        {
          String strRange = "";
          ArrayList sl = new ArrayList();
          if ((str2.startsWith("=")) || (str2.startsWith("!=")) || 
            (str2.startsWith("!match")) || (str2.startsWith("match")) || 
            (str2.startsWith("!smatch")) || (str2.startsWith("smatch")) || 
            (str2.startsWith(">")) || (str2.startsWith(">=")) || 
            (str2.startsWith("<")) || (str2.startsWith("<=")))
          {
            String sOperator = "";
            if (str2.length() > 1)
            {
              strRange = str2.substring(str2.indexOf(" "), str2.length()).trim();
              sOperator = str2.substring(0, str2.indexOf(" "));
            }
            else
            {
              strRange = " ";
              sOperator = str2.substring(0, str2.length());
            }
            if (mRangeValues.containsKey("ranges"))
            {
              Map m = (Map)mRangeValues.get("ranges");
              if (m.containsKey(this.mOpearators.get(sOperator))) {
                sl = (ArrayList)m.get(this.mOpearators.get(sOperator));
              }
              sl.add(strRange);
              m.put(this.mOpearators.get(sOperator), sl);
              


              mRangeValues.put("ranges", m);
            }
            else
            {
              sl.add(strRange);
              Map m = new HashMap();
              m.put(this.mOpearators.get(sOperator), sl);
              mRangeValues.put("ranges", m);
            }
          }
          else if (str2.startsWith("between"))
          {
            strRange = str2.substring(str2.indexOf("between"), str2.length()).trim();
            List slResult = com.izn.schemamodeler.util.UIUtil.spiltSingleStringToken(context, " ", strRange);
            slResult.remove("between");
            String strToken2 = (String)slResult.get(1);
            if (strToken2.equalsIgnoreCase("Inclusive")) {
              slResult.set(1, "true");
            } else {
              slResult.set(1, "false");
            }
            String strToken3 = (String)slResult.get(3);
            if (strToken3.equalsIgnoreCase("Inclusive")) {
              slResult.set(3, "true");
            } else {
              slResult.set(3, "false");
            }
            Map m = new HashMap();
            m.put("value", slResult);
            m.put("operator", "between");
            slRanges.add(m);
          }
          else if (str2.startsWith("uses"))
          {
            strRange = str2.substring(str2.indexOf("uses"), str2.length()).trim();
            List slResult = com.izn.schemamodeler.util.UIUtil.spiltSingleStringToken(context, " ", str2);
            slResult.remove("uses");
            slResult.remove("program");
            slResult.remove("with");
            slResult.remove("input");
            Map m = new HashMap();
            m.put("value", slResult);
            m.put("operator", "programRange");
            
            slRanges.add(m);
          }
        }
      }
      if (!attributeInfoMap.containsKey("multiline")) {
        attributeInfoMap.put("multiline", "false");
      }
      if (!mValueTypeInfo.containsValue("true")) {
        mValueTypeInfo.put("singleValue", "true");
      }
      if (!mRangeValues.isEmpty())
      {
        Map m = (Map)mRangeValues.get("ranges");
        Set set = m.keySet();
        Iterator itr = set.iterator();
        Map mIn = new HashMap();
        String strKey = "";
        while (itr.hasNext())
        {
          mIn = new HashMap();
          strKey = (String)itr.next();
          mIn.put("operator", strKey);
          mIn.put("value", m.get(strKey));
          slRanges.add(mIn);
        }
      }
      attributeInfoMap.put("ranges", slRanges);
      attributeInfoMap.put("resetOn", mCloneInfo);
      attributeInfoMap.put("valueType", mValueTypeInfo);
      attributeInfoMap.put("scope", mScopeInfo);
    }
    catch (Exception e)
    {
      //System.out.println(" Error in getting attribute Info");
      e.printStackTrace();
    }

    return _objectMapper.writeValueAsString(attributeInfoMap);
  }
  
  public String geSchemaInfoWithPath(Context context, String strSchemaName, String strExportPath)
    throws Exception
  {
    return null;
  }
}
