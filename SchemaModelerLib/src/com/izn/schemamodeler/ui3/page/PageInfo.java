package com.izn.schemamodeler.ui3.page;

import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.izn.schemamodeler.SchemaInfo;
import com.matrixone.apps.cache.UITypeUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import matrix.db.Context;

public class PageInfo implements SchemaInfo {
	String _path = "";

	public PageInfo() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String geSchemaInfo(Context context, String strPageName, String strPageFileName) throws Exception {
		String strInfo = EMPTY_STRING;
		strPageName = "";
		_path = "D:\\schemaExport\\content\\" + strPageFileName;
		try {
			String strResult = MqlUtil.mqlCommand(context, "list page $1", new String[] { strPageName });
			MqlUtil.mqlCommand(context, "print page $1 select content output $2",
					new String[] { strPageFileName, _path });

			if (UIUtil.isNotNullAndNotEmpty(strPageName))
				try {
					strResult = MqlUtil.mqlCommand(context, "print page $1 nothistory", new String[] { strPageName });
				} catch (Exception ex) {

				}
			if (UIUtil.isNotNullAndNotEmpty(strResult)) {
				strInfo = parsePageFile(context, strResult);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strInfo;
	}

	private String parsePageFile(Context context, String strMQLResult) throws Exception{
		ObjectMapper _objectMapper = new ObjectMapper();
		Map pInfoMap = new LinkedHashMap();
		try {

			BufferedReader localBufferedReader = new BufferedReader(new StringReader(strMQLResult));
			pInfoMap.put("adminType", "page");
			pInfoMap.put("action", "modify");
			ArrayList slContent = new ArrayList();
			String str1;
			boolean bContent = false;
			while ((str1 = localBufferedReader.readLine()) != null) {
				str1 = str1.trim();
				int i = str1.indexOf(' ');
				String str2 = "";
				if ((i != -1) && (str1.length() > i + 1)) {
					str2 = UITypeUtil.unquote(str1.substring(i + 1));
				}
				if (str1.isEmpty())
					continue;
				if (str1.startsWith("page")) {
					pInfoMap.put("name", str2);
				} else if (str1.startsWith("description")) {
					pInfoMap.put("description", str2);
				} else if (str2.startsWith("nothidden")) {
					pInfoMap.put("hidden", "false");
				} else if (str2.startsWith("hidden")) {
					pInfoMap.put("hidden", "true");
				} else if (str1.startsWith("content")) {
					str1 = str2;
					bContent = true;
					pInfoMap.put("content", "");
					pInfoMap.put("filepath", _path);
				} else if (str1.startsWith("mime")) {
					pInfoMap.put("mime", str2);
					bContent = false;
				} else if (str1.startsWith("alt")) {
					pInfoMap.put("alt", str2);
				} else if (str1.startsWith("nothidden")) {
					pInfoMap.put("hidden", "false");
				} else if (str1.startsWith("hidden")) {
					pInfoMap.put("hidden", "true");
				}
				if (bContent) {
					slContent.add(str1);
				}

			} // end while

			// pInfoMap.put("content", slContent);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return _objectMapper.writeValueAsString(pInfoMap);
	}

	@Override
	public String geSchemaInfoWithPath(Context context, String strPageFileName, String strExportPath) throws Exception {

		String strInfo = "";
		try {
			String strResult = MqlUtil.mqlCommand(context, "print page $1 nothistory",
					new String[] { strPageFileName });
			File file = new File(strExportPath + "\\Pages");
			if (!file.exists()) {
				file.mkdir();
			}
			strExportPath = strExportPath + "\\Pages\\" + strPageFileName;
			MqlUtil.mqlCommand(context, "print page $1 select content dump output $2",
					new String[] { strPageFileName, strExportPath });
			if (UIUtil.isNotNullAndNotEmpty(strResult)) {
				strInfo = parsePageFile(context, strResult, strExportPath);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return strInfo;
	}

	private String parsePageFile(Context context, String strMQLResult, String strExportPath) throws Exception{
		ObjectMapper _objectMapper = new ObjectMapper();
		Map pInfoMap = new LinkedHashMap();
		try {
			BufferedReader localBufferedReader = new BufferedReader(new StringReader(strMQLResult));
			pInfoMap.put("adminType", "page");
			pInfoMap.put("action", "modify");
			ArrayList slContent = new ArrayList();

			boolean bContent = false;
			String str1;
			while ((str1 = localBufferedReader.readLine()) != null) {
				str1 = str1.trim();
				int i = str1.indexOf(' ');
				String str2 = "";
				if ((i != -1) && (str1.length() > i + 1)) {
					str2 = UITypeUtil.unquote(str1.substring(i + 1));
				}
				if (!str1.isEmpty()) {
					if (str1.startsWith("page")) {
						pInfoMap.put("name", str2);
					} else if (str1.startsWith("description")) {
						pInfoMap.put("description", str2);
					} else if (str2.startsWith("nothidden")) {
						pInfoMap.put("hidden", "false");
					} else if (str2.startsWith("hidden")) {
						pInfoMap.put("hidden", "true");
					} else if (str1.startsWith("content")) {
						str1 = str2;
						bContent = true;
						pInfoMap.put("content", "");
						pInfoMap.put("filepath", strExportPath);
					} else if (str1.startsWith("mime")) {
						pInfoMap.put("mime", str2);
						bContent = false;
					} else if (str1.startsWith("alt")) {
						pInfoMap.put("alt", str2);
					} else if (str1.startsWith("nothidden")) {
						pInfoMap.put("hidden", "false");
					} else if (str1.startsWith("hidden")) {
						pInfoMap.put("hidden", "true");
					} else if (str1.startsWith("created")) {
						pInfoMap.put("originated", str2);
					} else if (str1.startsWith("modified")) {
						pInfoMap.put("modified", str2);
					}
					if (bContent) {
						slContent.add(str1);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return _objectMapper.writeValueAsString(pInfoMap);
	}

}
