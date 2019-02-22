package com.izn.schemamodeler.ui3.table;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.izn.schemamodeler.SchemaInfo;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.UOMUtil;
import com.matrixone.apps.framework.ui.UIExpression;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;

public class TableInfo implements SchemaInfo {

	public TableInfo() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String geSchemaInfo(Context context, String strTableName, String tbd) throws Exception {
		String strInfo = EMPTY_STRING;
		String strRegistryName = EMPTY_STRING;
		try {
			String strResult = MqlUtil.mqlCommand(context, "list table system $1", new String[] { strTableName });
			//System.out.println("Line 32 TableInfo strResult is ::::::::::::               " + strResult);

			if (UIUtil.isNotNullAndNotEmpty(strTableName))
				try {
						strResult = MqlUtil.mqlCommand(context, "print table $1 system nothistory",	new String[] { strTableName });
						strRegistryName = MqlUtil.mqlCommand(context, "list property to table $1 system", new String[] { strTableName });
						if(strRegistryName != null && !strRegistryName.equals("")){				 
							String[] split = strRegistryName.trim().split(" ");
							strRegistryName = split[0].trim();
							strRegistryName = strRegistryName.replaceFirst("table_", "");
						}
				} catch (Exception ex) {

				}
			if (UIUtil.isNotNullAndNotEmpty(strResult)) {
				strInfo = parseTable(context, strResult, strRegistryName);
			}

		} catch (Exception e) {

			e.printStackTrace();
		}
		return strInfo;
	}

	private String parseTable(Context context, String strMQLResult, String strRegistryName) throws Exception {
		ObjectMapper _objectMapper = new ObjectMapper();
		HashMap tableInfo = new HashMap();
		try {
			tableInfo.put("adminType", "table");
			tableInfo.put("action", "modify");
			tableInfo.put("registryname", strRegistryName);
			Map columnFieldInfo = new LinkedHashMap<>();
			HashMap columnData = new HashMap();
			ArrayList slUsers = new ArrayList();
			List alSettings = new ArrayList();
			List alColumns = new ArrayList();
			BufferedReader localBufferedReader = new BufferedReader(new StringReader(strMQLResult));

			String str2 = "";
			String str1;
			boolean bFieldStart = false;
			int iColumnCount = 1;
			Map mSettings = new HashMap();
			Map mUsers = new HashMap();
			boolean bAnyColumn = false;
			String sCacheOderDB = "";
			String sOrderDB = "";
			while ((str1 = localBufferedReader.readLine()) != null) {
				str1 = str1.trim();
				int i = str1.indexOf(' ');

				if ((i != -1) && (str1.length() > i + 1)) {
					str2 = str1.substring(i + 1).trim();
				}
				if (str1.startsWith("table")) {
					tableInfo.put("name", str2);
				}
				if (str1.startsWith("description")) {
					tableInfo.put("description", str2);
				} else if (str1.startsWith("nothidden")) {
					tableInfo.put("hidden", "false");
				} else if (str1.startsWith("hidden")) {
					tableInfo.put("hidden", "true");
				} else {

					if (str1.startsWith("property")) {

					} else if (str1.startsWith("#")) {
						bAnyColumn = true;
						sCacheOderDB = str1.substring(str1.indexOf("#") + 1, str1.indexOf("column")).trim();
						bFieldStart = true;
						if (bFieldStart && iColumnCount > 1) {

							columnData.put("settings", alSettings);
							columnData.put("objectAccess", slUsers);
							alColumns.add(columnData);
							columnData.put("order", String.valueOf(iColumnCount - 1));
							columnData.put("orderDB", sOrderDB);
							columnData = new HashMap();
							slUsers = new ArrayList();
							alSettings = new ArrayList();
						}
						sOrderDB = str1.substring(str1.indexOf("#") + 1, str1.indexOf("column")).trim();
						iColumnCount++;
						str2 = "";
					} else if (str1.startsWith("setting")) {
						ArrayList slKeuValues = com.izn.schemamodeler.util.UIUtil.parseKeyAndValue(context, str2);
						mSettings = new HashMap();
						mSettings.put("name", (String) slKeuValues.get(0));
						mSettings.put("value", (String) slKeuValues.get(1));
						mSettings.put("flag", "");
						alSettings.add(mSettings);

					} else if (str1.startsWith("label")) {
						columnData.put("label", str2);
					} else if (str1.startsWith("name")) {
						// columnData.put("name", str2);
						// changed name to column wrt table component xml file
						columnData.put("column", str2);
					} else if (str1.startsWith("href")) {
						columnData.put("href", str2);
					} else if (str1.startsWith("alt")) {
						columnData.put("alt", str2);
					} else if (str1.startsWith("range")) {
						columnData.put("range", str2);
					} else if (str1.startsWith("update")) {
						columnData.put("update", str2);
					} else if (str1.startsWith("sorttype")) {
						columnData.put("sorttype", str2);
					} 
					else if ((str1.startsWith("businessobject"))) {	
						
						columnData.put("columnType", "businessobject");
						String[] split = str1.split(" ");
						if(split.length>1) {							
							columnData.put("expression", str2);
						}								
					}
					else if((str1.startsWith("set")))
					{
						
						columnData.put("columnType", "set");
						String[] split = str1.split(" ");
						if(split.length>1) {												
							columnData.put("expression", str2);
						}
						
				    }
					else if ((str1.startsWith("relationship"))) {
						
						columnData.put("columnType", "relationship");
						String[] split = str1.split(" ");
						if(split.length>1) {							
							columnData.put("expression", "str2");
						}

					}
					/*else if ((str1.startsWith("businessobject")) || (str1.equals("set"))) {
						columnData.put("columnType", "businessobject");
						columnData.put("expression", str2);
					} else if ((str1.startsWith("relationship"))) {
						columnData.put("columnType", "relationship");
						columnData.put("expression", str2);

					} */
					
					else if (str1.startsWith("user")) {
						if (((String) str2).length() > 0) {
							mUsers = new HashMap();
							mUsers.put("name", (String) str2);
							mUsers.put("flag", "");
							slUsers.add(mUsers);
						}
					}
				}
			}
			if (bAnyColumn) {
				columnData.put("settings", alSettings);
				columnData.put("objectAccess", slUsers);
				columnData.put("order", String.valueOf(iColumnCount - 1));
				columnData.put("orderDB", sCacheOderDB);
				alColumns.add(columnData);
			}
			tableInfo.put("columns", alColumns);

		} catch (Exception e) {
			//System.out.println(" Error in getting table Info");
			e.printStackTrace();
		}
		return _objectMapper.writeValueAsString(tableInfo);
	}

	@Override
	public String geSchemaInfoWithPath(Context context, String strSchemaName, String strExportPath) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
