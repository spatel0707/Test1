package com.izn.schemamodeler.admin.program;

import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.izn.schemamodeler.SchemaInfo;
import com.matrixone.apps.cache.UITypeUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;

import matrix.db.Context;
import matrix.util.StringList;

public class ProgramInfo implements SchemaInfo {
	String _path = "";

	public ProgramInfo() {

	}

	public String geSchemaInfo(Context context, String strProgramName, String tbd) throws Exception {
		String strInfo = EMPTY_STRING;
		try {
			String strResult = MqlUtil.mqlCommand(context, "list program $1", new String[] { strProgramName });

			if (UIUtil.isNotNullAndNotEmpty(strProgramName))
				try {
					strResult = MqlUtil.mqlCommand(context, "print program $1 nothistory",
							new String[] { strProgramName });
					MqlUtil.mqlCommand(context, "print program $1 select code output $2",
							new String[] { strProgramName, _path });
					if (UIUtil.isNotNullAndNotEmpty(strResult))
						strInfo = parseProgram(context, strResult);
				} catch (Exception ex) {

				}
			if (UIUtil.isNotNullAndNotEmpty(strResult)) {
				strInfo = parseProgram(context, strResult);
			}

		} catch (Exception e) {
			throw e;
		}
		return strInfo;
	}

	private String parseProgram(Context context, String strMQLResult, String strExportPath) throws Exception {
		ObjectMapper _objectMapper = new ObjectMapper();
		Map pInfoMap = new LinkedHashMap();
		try {
			Map<String, String> mProgramType = new HashMap();
			mProgramType.put("mql", "false");
			mProgramType.put("java", "false");
			mProgramType.put("external", "false");
			Map<String, String> mExecute = new HashMap();
			mExecute.put("immediate", "false");
			mExecute.put("deferred", "false");
			BufferedReader localBufferedReader = new BufferedReader(new StringReader(strMQLResult));
			pInfoMap.put("adminType", "program");
			pInfoMap.put("action", "modify");
			pInfoMap.put("needsbusinessobject", "false");
			pInfoMap.put("downloadable", "false");
			pInfoMap.put("pipe", "false");
			pInfoMap.put("pooled", "false");
			pInfoMap.put("description", "");
			pInfoMap.put("execute", "");
			String str1;
			while ((str1 = localBufferedReader.readLine()) != null) {
				str1 = str1.trim();
				int i = str1.indexOf(' ');
				String str2 = "";
				if ((i != -1) && (str1.length() > i + 1)) {
					str2 = UITypeUtil.unquote(str1.substring(i + 1));
				}
				if (str1.startsWith("program")) {
					pInfoMap.put("name", str2);
				} else if (str1.startsWith("description") && ((String) pInfoMap.get("description")).isEmpty()) {
					pInfoMap.put("description", str2);
				} else if ((str1.startsWith("mql")) || (str1.startsWith("java")) || (str1.startsWith("external"))) {
					mProgramType.put(str1, "true");
				} else if (str1.startsWith("execute") && ((String) pInfoMap.get("description")).isEmpty()) {
					if (str1.indexOf("user") != -1) {
						pInfoMap.put("user", str2.substring(4, str2.length()));
					} else {
						mExecute.put(str2, "true");
					}
				} else if (pInfoMap.containsKey(str1)) {
					pInfoMap.put(str1, "true");
				} else if (str1.startsWith("code")) {
					pInfoMap.put("code", str2);
					pInfoMap.put("filepath", strExportPath);
				} else if (str1.startsWith("nothidden")) {
					pInfoMap.put("hidden", "false");
				} else if (str1.startsWith("hidden")) {
					pInfoMap.put("hidden", "true");
				}
			}
			pInfoMap.put("type", mProgramType);
			pInfoMap.put("execute", mExecute);
		} catch (Exception e) {
			throw e;
		}
		return _objectMapper.writeValueAsString(pInfoMap);
	}

	private String parseProgram(Context context, String strMQLResult) throws Exception {
		ObjectMapper _objectMapper = new ObjectMapper();
		Map pInfoMap = new LinkedHashMap();
		try {
			Map<String, String> mProgramType = new HashMap<String, String>();
			mProgramType.put("mql", "false");
			mProgramType.put("java", "false");
			mProgramType.put("external", "false");
			Map<String, String> mExecute = new HashMap<String, String>();
			mExecute.put("immediate", "false");
			mExecute.put("deferred", "false");
			BufferedReader localBufferedReader = new BufferedReader(new StringReader(strMQLResult));
			pInfoMap.put("adminType", "program");
			pInfoMap.put("action", "modify");
			pInfoMap.put("needsbusinessobject", "false");
			pInfoMap.put("downloadable", "false");
			pInfoMap.put("pipe", "false");
			pInfoMap.put("pooled", "false");
			pInfoMap.put("description", "");
			pInfoMap.put("execute", "");
			String str1;
			while ((str1 = localBufferedReader.readLine()) != null) {
				str1 = str1.trim();
				int i = str1.indexOf(' ');
				String str2 = "";
				if ((i != -1) && (str1.length() > i + 1)) {
					str2 = UITypeUtil.unquote(str1.substring(i + 1));
				}
				if (str1.startsWith("program")) {
					pInfoMap.put("name", str2);
				} else if (str1.startsWith("description") && ((String) pInfoMap.get("description")).isEmpty()) {
					pInfoMap.put("description", str2);
				} else if (str1.startsWith("mql") || str1.startsWith("java") || str1.startsWith("external")) {
					mProgramType.put(str1, "true");
				} else if (str1.startsWith("execute") && ((String) pInfoMap.get("execute")).isEmpty()) {
					if (str1.indexOf("user") != -1) {
						pInfoMap.put("user", str2.substring(4, str2.length()));
					} else {
						mExecute.put(str2, "true");
					}
				} else if (pInfoMap.containsKey(str1)) {
					pInfoMap.put(str1, "true");
				} else if (str1.startsWith("code")) {
					pInfoMap.put("code", str2);
					pInfoMap.put("filepath", _path);
				} else if (str1.startsWith("nothidden")) {
					pInfoMap.put("hidden", "false");
				} else if (str1.startsWith("hidden")) {
					pInfoMap.put("hidden", "true");
				}
			}
			pInfoMap.put("type", mProgramType);
			pInfoMap.put("execute", mExecute);
		} catch (Exception e) {
			throw e;
		}
		return _objectMapper.writeValueAsString(pInfoMap);
	}

	@Override
	public String geSchemaInfoWithPath(Context context, String strProgramName, String strExportPath) throws Exception {

		String strResult1 = MqlUtil.mqlCommand(context, "list program $1", new String[] { strProgramName });

		String strInfo = "";
		try {

			String strResult = MqlUtil.mqlCommand(context, "print program $1 nothistory",
					new String[] { strProgramName });
			File file = new File(strExportPath + "\\Programs");
			if (!file.exists()) {
				file.mkdir();
			}
			strExportPath = strExportPath + "\\Programs\\";
			System.out.println("execute program " + strProgramName + " source " + strExportPath);
			String strQueryResult = MqlUtil.mqlCommand(context, "print program $1 select $2 $3 dump $4",
					new String[] { strProgramName, "isjavaprogram", "code", "|" });
			boolean isEmptyFile = false;
			StringList sPlit = FrameworkUtil.split(strQueryResult.toString(), "|");

			String strProgramType = sPlit.get(0);
			if (sPlit.get(1) == null || sPlit.get(1).isEmpty()) {
				isEmptyFile = true;
			}
			if (strProgramType.equalsIgnoreCase("TRUE")) {
				MqlUtil.mqlCommand(context,
						"extract program '" + strProgramName + "' source '" + strExportPath + "\\'");
				if (isEmptyFile) {
					strExportPath = strExportPath + "\\" + strProgramName + "_mxJPO.java";
					;
					File emptyfile = new File(strExportPath);
					emptyfile.createNewFile();
				}
			} else {
				strExportPath = strExportPath + strProgramName;
				MqlUtil.mqlCommand(context, "print program $1 select code output $2",
						new String[] { strProgramName, strExportPath });
			}
			if (UIUtil.isNotNullAndNotEmpty(strResult)) {
				strInfo = parseProgram(context, strResult, strExportPath);
			}
		} catch (Exception e) {
			throw e;
		}
		return strInfo;
	}
}
