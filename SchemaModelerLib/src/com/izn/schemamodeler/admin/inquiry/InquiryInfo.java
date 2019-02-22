package com.izn.schemamodeler.admin.inquiry;

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

public class InquiryInfo implements SchemaInfo {

	public InquiryInfo() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String geSchemaInfo(Context context, String strInquiryName, String tbd) throws Exception {
		String strInfo = EMPTY_STRING;
		String strRegistryName = EMPTY_STRING;
		try {
			String strResult = MqlUtil.mqlCommand(context, "list inquiry $1", new String[] { strInquiryName });
			// System.out.println("Line 32 InquiryInfo strResult is ::::::::::::
			// "+strResult);

			if (UIUtil.isNotNullAndNotEmpty(strInquiryName))
				try {
					strResult = MqlUtil.mqlCommand(context, "print inquiry $1 nothistory", new String[] { strInquiryName });
					strRegistryName = MqlUtil.mqlCommand(context, "list property to inquiry $1",new String[] { strInquiryName });
					if(strRegistryName != null && !strRegistryName.equals("")){				 
						String[] split = strRegistryName.trim().split(" ");
						strRegistryName = split[0].trim();
						strRegistryName = strRegistryName.replaceFirst("inquiry_", "");
					}
				} catch (Exception ex) {

				}
			if (UIUtil.isNotNullAndNotEmpty(strResult)) {
				strInfo = parseInquiry(context, strResult, strRegistryName);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return strInfo;
	}

	private String parseInquiry(Context context, String strMQLResult, String strRegistryName) throws Exception {
		ObjectMapper _objectMapper = new ObjectMapper();
		Map inquiryInfoMap = new HashMap();
		try {
			ArrayList localStringList1 = new ArrayList();
			ArrayList slUsers = new ArrayList();
			Map<String, String> mProperties = new HashMap<String, String>();
			Map mUsers = new HashMap();
			Map<String, String> mSettings = new HashMap<String, String>();
			List alSettings = new ArrayList();
			List alUsers = new ArrayList();
			BufferedReader localBufferedReader = new BufferedReader(new StringReader(strMQLResult));
			inquiryInfoMap.put("adminType", "inquiry");
			inquiryInfoMap.put("action", "modify");
			inquiryInfoMap.put("code", "");
			inquiryInfoMap.put("registryname", strRegistryName);
			String str1;
			while ((str1 = localBufferedReader.readLine()) != null) {
				str1 = str1.trim();
				int i = str1.indexOf(' ');
				String str2 = "";
				if ((i != -1) && (str1.length() > i + 1)) {
					str2 = UITypeUtil.unquote(str1.substring(i + 1));
				}
				if (str1.startsWith("inquiry")) {
					inquiryInfoMap.put("name", str2);
				} else if (str1.startsWith("description")) {
					inquiryInfoMap.put("description", str2);
				} else if (str1.startsWith("pattern")) {
					inquiryInfoMap.put("pattern", str2);
				}
				// else if (str1.startsWith("label"))
				// {
				// inquiryInfoMap.put("label", str2);
				// }
				else if (str1.startsWith("format")) {
					inquiryInfoMap.put("format", str2);
				}

				else if (str1.startsWith("input")) {
					inquiryInfoMap.put("input", str2);

				} else if (str1.startsWith("code")) {

					inquiryInfoMap.put("code", str2);

				} else if (str1.startsWith("nothidden")) {

					inquiryInfoMap.put("hidden", "false");

				} else if (str1.startsWith("hidden")) {

					inquiryInfoMap.put("hidden", "true");
				}

				else {
					ArrayList localStringList3 = new ArrayList();
					if (str1.startsWith("property")) {
						// ArrayList slKeuValues =
						// com.izn.schemamodeler.util.UIUtil.parseKeyAndValue(context,
						// str2);
						// mProperties.put((String)slKeuValues.elementAt(0),
						// (String)slKeuValues.elementAt(1));

					} else if (str1.startsWith("argument")) {
						ArrayList slKeyValues = com.izn.schemamodeler.util.UIUtil.parseKeyAndValue(context, str2);
						mSettings = new HashMap();
						mSettings.put("name", (String) slKeyValues.get(0));
						mSettings.put("value", (String) slKeyValues.get(1));
						mSettings.put("flag", "");
						alSettings.add(mSettings);
					}
				}
			}
			// if(!inquiryInfoMap.containsKey("label")){
			// inquiryInfoMap.put("label", "");
			// }
			inquiryInfoMap.put("argument", alSettings);
			// inquiryInfoMap.put("objectAccess", slUsers);
		} catch (Exception e) {
			System.out.println(" Error in getting inquiry Info");
			e.printStackTrace();
		}

		return _objectMapper.writeValueAsString(inquiryInfoMap);
	}

/*	public void updateDBInquiry(Context context, String sInputXML, String strInquiryName) throws Exception {
		ObjectMapper _objectMapper = new ObjectMapper();
		try {
			String strInquiryDBDef = geSchemaInfo(context, strInquiryName, "tbd");
			Map strInquiryInfo = gson.fromJson(strInquiryDBDef, HashMap.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

	@Override
	public String geSchemaInfoWithPath(Context context, String strSchemaName, String strExportPath) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
