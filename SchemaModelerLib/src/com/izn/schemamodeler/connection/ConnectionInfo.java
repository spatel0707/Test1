package com.izn.schemamodeler.connection;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.izn.schemamodeler.SchemaInfo;
import com.matrixone.apps.cache.UITypeUtil;
import com.matrixone.apps.domain.util.MqlUtil;

import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;

import matrix.db.Context;

public class ConnectionInfo implements SchemaInfo {

	@Override
	public String geSchemaInfo(Context context, String strBusinessObject, String strExpandRel) throws Exception {
		String strInfo = EMPTY_STRING;
		try {
			if (strBusinessObject != null) {
				String strResult = MqlUtil.mqlCommand(context,
						"expand bus $1 relationship $2 select rel $3 $4 $5 $6 $7 $8 $9 $10 $11 $12 $13",
						new String[] { strBusinessObject, strExpandRel,"from.id", "from.type", "from.name", "from.revision","to.id",
								"to.type", "to.name", "to.revision", "attribute.value", "tomid.id", "frommid.id" });
				if (strResult != null && !strResult.isEmpty())
					strInfo = parseConnections(context, strResult, strBusinessObject);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strInfo;

	}

	private String parseConnections(Context context, String strMQLResult, String sBasic) throws Exception {
		ObjectMapper _objectMapper = new ObjectMapper();
		Map pInfoMap = new LinkedHashMap();
		Map<String, List> finalInfoMap = new HashMap<String, List>();
		List<String> listFrommid = new ArrayList<String>();
		List<String> listTomid = new ArrayList<String>();
		Map<String, List> mRelToRelInfoMap = new LinkedHashMap<String, List>();
		List<Map> listFromMapInfo = new ArrayList<Map>();
		List<Map> listToMapInfo = new ArrayList<Map>();
		Map mRelMap = new LinkedHashMap();
		try {

			List<String> lstAttribute = new ArrayList<String>();
			List<Map> lstRel = new ArrayList<Map>();
			BufferedReader localBufferedReader = new BufferedReader(new StringReader(strMQLResult));
			String str1;
			int count = 0;
			while ((str1 = localBufferedReader.readLine()) != null) {
				Map<String, String> keyValue = new LinkedHashMap<String, String>();
				str1 = str1.trim();
				int i = str1.indexOf('=');
				String str2 = "";
				if ((i != -1) && (str1.length() > i + 1)) {
					str2 = UITypeUtil.unquote(str1.substring(i + 2));
				}
				if (str1.startsWith("from.type")) {
					pInfoMap.put("from_type", str2);
				} else if (str1.startsWith("from.name")) {
					pInfoMap.put("from_name", str2);
				} else if (str1.startsWith("from.revision")) {
					pInfoMap.put("from_revision", str2);
				} else if (str1.startsWith("to.type")) {
					pInfoMap.put("to_type", str2);
				} else if (str1.startsWith("to.name")) {
					pInfoMap.put("to_name", str2);
				} else if (str1.startsWith("to.revision")) {
					pInfoMap.put("to_revision", str2);
				} else if (str1.startsWith("to.id")) {
					pInfoMap.put("to_id", str2);
				} else if (str1.startsWith("from.id")) {
					pInfoMap.put("from_id", str2);
				} else if (str1.startsWith("attribute")) {
					String sAttributeName = str1.substring(str1.indexOf("[") + 1, str1.indexOf("]"));
					pInfoMap.put(sAttributeName, str2);
					if (!lstAttribute.contains(sAttributeName)) {
						lstAttribute.add(sAttributeName);
					}
				} else if (str1.startsWith("1")) {
					if (count != 0) {
						pInfoMap.put("attributes", lstAttribute);
						lstRel.add(pInfoMap);
						pInfoMap = new LinkedHashMap();
					}
					count = count + 1;
				}
				else if (str1.startsWith("frommid")) {
					listFrommid.add(str2);
				}
				else if (str1.startsWith("tomid")) {
					listTomid.add(str2);
				}
			}

			//*******Rel to rel-----------------------STARTS
			String strRelToRelResult = "";
			if(!listTomid.isEmpty()){
				for(String sTommId : listTomid){
					strRelToRelResult = MqlUtil.mqlCommand(context, "print connection $1 select $2 $3 $4 $5 $6 $7 $8 $9",
							new String[] { sTommId, "name", "to.type", "to.name", "to.revision","from.type", "from.name", "from.revision", "attribute.value"});
					listToMapInfo.add(parseReltoRelConnections(context,strRelToRelResult,"tomid"));
				}
			}
			mRelToRelInfoMap.put("tomid", listToMapInfo);
			if(!listFrommid.isEmpty()){
				for(String sFrommmId : listFrommid){
					strRelToRelResult = MqlUtil.mqlCommand(context, "print connection $1 select $2 $3 $4 $5 $6 $7 $8 $9",
							new String[] { sFrommmId, "name", "to.type", "to.name", "to.revision","from.type", "from.name", "from.revision", "attribute.value"});
					listFromMapInfo.add(parseReltoRelConnections(context,strRelToRelResult,"frommid"));
				}
			}
			mRelToRelInfoMap.put("frommid", listFromMapInfo);
			//*****Rel to rel-----------------------ENDS
			
			pInfoMap.put("attributes", lstAttribute);
			pInfoMap.put("relationships", mRelToRelInfoMap);
			lstRel.add(pInfoMap);
			finalInfoMap.put(sBasic, lstRel);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return _objectMapper.writeValueAsString(finalInfoMap);
	}
	
	private Map parseReltoRelConnections(Context context, String strRelToRelResult, String relMId) throws Exception {
		Map pInfoMap = new LinkedHashMap();
		try {
			List<String> lstAttribute = new ArrayList<String>();
			List<Map> lstRel = new ArrayList<Map>();
			BufferedReader localBufferedReader = new BufferedReader(new StringReader(strRelToRelResult));
			String str1;
			int count = 0;
			while ((str1 = localBufferedReader.readLine()) != null) {
				Map<String, String> keyValue = new LinkedHashMap<String, String>();
				str1 = str1.trim();
				int i = str1.indexOf('=');
				String str2 = "";
				if ((i != -1) && (str1.length() > i + 1)) {
					str2 = UITypeUtil.unquote(str1.substring(i + 2));
				}
				if (str1.startsWith("name")) {
					pInfoMap.put(relMId, str2);
				}
				else if (str1.startsWith("from.type")) {
					pInfoMap.put("from_type", str2);
				} else if (str1.startsWith("from.name")) {
					pInfoMap.put("from_name", str2);
				} else if (str1.startsWith("from.revision")) {
					pInfoMap.put("from_revision", str2);
				} else if (str1.startsWith("to.type")) {
					pInfoMap.put("to_type", str2);
				} else if (str1.startsWith("to.name")) {
					pInfoMap.put("to_name", str2);
				} else if (str1.startsWith("to.revision")) {
					pInfoMap.put("to_revision", str2);
				} else if (str1.startsWith("attribute")) {
					String sAttributeName = str1.substring(str1.indexOf("[") + 1, str1.indexOf("]"));
					pInfoMap.put(sAttributeName, str2);
					if (!lstAttribute.contains(sAttributeName)) {
						lstAttribute.add(sAttributeName);
					}
				} else if (str1.startsWith("1")) {
					if (count != 0) {
						pInfoMap.put("attributes", lstAttribute);
						lstRel.add(pInfoMap);
						pInfoMap = new LinkedHashMap();
					}
					count = count + 1;
				}
			}
			pInfoMap.put("attributes", lstAttribute);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pInfoMap;
	}
	@Override
	public String geSchemaInfoWithPath(Context context, String strSchemaName, String strExportPath) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
