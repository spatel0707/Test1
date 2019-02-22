package com.izn.schemamodeler.admin.inquiry;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.transform.stream.StreamSource;

import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.izn.schemamodeler.SchemaFactoryInfo;
import com.izn.schemamodeler.SchemaLoader;
import com.izn.schemamodeler.admin.inquiry.InquiryXMLSchema.Schema;
import com.izn.schemamodeler.admin.inquiry.InquiryXMLSchema.Schema.Basic;
import com.izn.schemamodeler.admin.inquiry.InquiryXMLSchema.Schema.Field;
import com.izn.schemamodeler.admin.inquiry.InquiryXMLSchema.Schema.Field.Detail;
import com.izn.schemamodeler.admin.inquiry.InquiryXMLSchema.Schema.Field.Setting;
import com.izn.schemamodeler.admin.inquiry.InquiryXMLSchema.Schema.Field.Setting.Param;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class InquiryLoader implements SchemaLoader {
	ObjectMapper _gson = new ObjectMapper();
	Basic _basicElem = null;
	Field _fieldElem = null;
	Setting _setting = null;

	public InquiryLoader() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void loadSchema(Context context, String strSchemaName, String fileName, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty)  throws Exception{
		try {
			List<Inquiry> lstInquiry = new ArrayList<Inquiry>();
			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
			InquiryInfo inquiryInfo = (InquiryInfo) schemaFactory.getSchemaObject("inquiry");
			JAXBContext jConext = JAXBContext.newInstance(InquiryXMLSchema.class);
			Unmarshaller unmarshaller = jConext.createUnmarshaller();
			JAXBElement<InquiryXMLSchema> inquiryElem = unmarshaller.unmarshal(new StreamSource(new File(fileName)),
					InquiryXMLSchema.class);
			InquiryXMLSchema inquiryXMLSchema = inquiryElem.getValue();
			List<Schema> lstSchema = inquiryXMLSchema.getSchema();
			Inquiry inquiry = null;
			String fValue = "";
			String fName = "";
			Map<String, String> mSettings = null;
			List<Map<String, String>> lstSetting = null;
			for (Schema _schema : lstSchema) {
				lstSetting = new ArrayList<Map<String, String>>();
				_basicElem = _schema.getBasic();
				inquiry = new Inquiry(_basicElem.getName(), _basicElem.getDescription(), _basicElem.getHidden(),
						_basicElem.getRegistryName());
				_fieldElem = _schema.getField();
				List<Detail> lstDetail = _fieldElem.getDetail();
				for (Detail _detail : lstDetail) {
					fName = _detail.getName();
					fValue = _detail.getValueAttribute();
					if (fValue != null) {
						fValue = _detail.getValueAttribute().trim();
					} else {
						fValue = "";
					}
					if (fName.equalsIgnoreCase("pattern")) {
						inquiry.setPattern(fValue.trim());
					} else if (fName.equalsIgnoreCase("code")) {
						inquiry.setCode(fValue.trim());
					} else if (fName.equalsIgnoreCase("format")) {
						inquiry.setFormat(fValue.trim());
					}

				}
				_setting = _fieldElem.getSetting();
				List<Param> lstParem = _setting.getParam();
				for (Param _param : lstParem) {
					mSettings = new HashMap<String, String>();
					fValue =  _param.getValueAttribute();
					if (fValue != null) {
						fValue =  _param.getValueAttribute().trim();
					} else {
						fValue = "";
					}
					mSettings.put("name", _param.getName());
					mSettings.put("value", fValue);
					lstSetting.add(mSettings);
				}
				inquiry.setLstArgument(lstSetting);
				lstInquiry.add(inquiry);
			}
			prepareInquiryMQL(context, lstInquiry, schema_done_log, scmConfigProperty);
		} catch (UnmarshalException ume) {
			schema_done_log.error("UnmarshalException on ["+strSchemaName+".xml] : "+ume.getCause());
			schema_done_log.warn(UIUtil.sUnmarshalExceptionWarningMessage);
			throw ume;
		} catch (Exception e) {
			schema_done_log.error(""+e.getCause());
			throw e;
		}
	}

	@SuppressWarnings("deprecation")
	private void prepareInquiryMQL(Context context, List<Inquiry> lstInquiry, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty) throws Exception{
		boolean bMQLResult;
		int iCountTotal = 0;
		int iCountSuccess = 0;
		int iCountFailure = 0;
		int iCountAdd = 0;
		int iCountModify = 0;
		int iCountDelete = 0;
		String sMQL = "";
		String sOperation = "";
		String sMQLPropertyQuery = "";
		String strInquiryName = "";
		MQLCommand localMQLCommand = null;
		List<String> listModified = new ArrayList<String>();
		List<String> listAdded = new ArrayList<String>();
		List<String> listDeleted = new ArrayList<String>();
		try {
			iCountTotal = lstInquiry.size();
			for (Inquiry inquiry : lstInquiry) {				
				schema_done_log.info("Inquiry : [" + inquiry.name.replace(UIUtil.removeCharecter, "") + "]");
				ContextUtil.pushContext(context);
				strInquiryName = inquiry.name;
				try {
					String removeSchemaObject = UIUtil.removeSchemaObject(strInquiryName, "inquiry");
					if (removeSchemaObject.isEmpty()) {
						String strResult = MQLCommand.exec(context, "list inquiry $1",
								new String[] { strInquiryName.replace(UIUtil.removeCharecter, "") });
						if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
							sMQL = prepareModifyExistingSchemaMQL(inquiry, schema_done_log);
							iCountModify += 1;
							listModified.add(strInquiryName);
							sOperation = "mod";
						} else {
							sMQL = prepareAddNewSchemaMQL(inquiry, schema_done_log);
							iCountAdd += 1;
							listAdded.add(strInquiryName);
							sOperation = "add";
						}
						sMQLPropertyQuery = UIUtil.getMQLPropertyQuery(context, "inquiry", inquiry.name,
								inquiry.registryname,sOperation);
					} else {
						sMQL = removeSchemaObject;
						iCountDelete += 1;
						listDeleted.add(strInquiryName);
					}
					schema_done_log.info("MQL QUERY FOR IMPORT : " + sMQL);
					localMQLCommand = new MQLCommand();
					bMQLResult = localMQLCommand.executeCommand(context, sMQL, true);
					String sMQLError = (String) localMQLCommand.getError();
					schema_done_log.info("MQL QUERY EXECUTION RESULT : " + bMQLResult);
					if (bMQLResult) {
						iCountSuccess += 1;
						if (sMQLPropertyQuery != null && !"".equals(sMQLPropertyQuery)) {
							schema_done_log.info("MQL QUERY {PROPERTY} : " + sMQLPropertyQuery);
							localMQLCommand.executeCommand(context, sMQLPropertyQuery, true);
						}
					} else {
						iCountFailure += 1;
						if (sMQL.trim().toLowerCase().startsWith("add"))
							iCountAdd -= 1;
						else if (sMQL.trim().toLowerCase().startsWith("mod"))
							iCountModify -= 1;
						else
							iCountDelete -= 1;
						throw new MatrixException(sMQLError);
					}
					ContextUtil.popContext(context);
				} catch (Exception e) {
					ContextUtil.popContext(context);
					schema_done_log.info("TOTAL :" + iCountTotal + ", SUCCESS :" + iCountSuccess +  ", ADDED :" + iCountAdd + ", MODIFIED :" + iCountModify + ", DELETED :"	+ iCountDelete + ".");
					throw new Exception("Error occurred while importing schema inquiry : ["
							+ strInquiryName.replace(UIUtil.removeCharecter, "") + "] : " + e.getMessage());
				}
				schema_done_log.info("-----------------------------------------------------------------");
			}
			schema_done_log.info("TOTAL  :" + iCountTotal + ", SUCCESS :" + iCountSuccess  
					 + ", ADDED  :" + iCountAdd + ", MODIFIED :" + iCountModify + ", DELETED :"
					+ iCountDelete + ".");
			if ("true".equalsIgnoreCase(scmConfigProperty.getLogEverything())) {
				schema_done_log.info("ADDED\t\t:"+listAdded.toString());
				schema_done_log.info("MODIFIED\t:"+listModified.toString());
				schema_done_log.info("DELETED\t:"+listDeleted.toString());
			}
		} catch (Exception e) {
			throw e;
		}
	}

	private String prepareAddNewSchemaMQL(Object objectSchemaInfo, Logger schema_done_log) throws Exception {
		StringBuilder sbMQL = new StringBuilder();
		try {
			Inquiry objInfo = (Inquiry) objectSchemaInfo;
			sbMQL.append(" add inquiry ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null && !objInfo.description.isEmpty()) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.getPattern() != null && !objInfo.getPattern().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces("pattern")).append(UIUtil.quoteArgument(objInfo.getPattern()));
			}
			if (objInfo.getFormat() != null && !objInfo.getFormat().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces("format"))
						.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.getFormat())));
			}
			if (objInfo.getCode() != null && !objInfo.getCode().isEmpty()) {
				String getCode = objInfo.getCode().replaceAll("\"", "&quot;");
				sbMQL.append(" code ").append("\" " + getCode + " \" ");
			}
			if (objInfo.getLstArgument() != null && !objInfo.getLstArgument().isEmpty()) {
				List<Map<String, String>> lstSettings = objInfo.getLstArgument();
				for (Map<String, String> m : lstSettings) {
					sbMQL.append(" argument ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(m.get("name"))))
							.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(m.get("value"))));
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}

	private String prepareModifyExistingSchemaMQL(Object objectSchemaInfo, Logger schema_done_log) throws Exception {
		StringBuilder sbMQL = new StringBuilder();
		try {
			Inquiry objInfo = (Inquiry) objectSchemaInfo;
			sbMQL.append(" mod inquiry ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.getPattern() != null) {
				sbMQL.append(UIUtil.padWithSpaces("pattern")).append(UIUtil.singleQuotes(objInfo.getPattern()));
			}
			if (objInfo.getFormat() != null) {
				sbMQL.append(UIUtil.padWithSpaces("format"))
						.append(UIUtil.padWithSpaces(UIUtil.singleQuotes(objInfo.getFormat())));
			}
			if (objInfo.getCode() != null) {
				if(objInfo.getCode().isEmpty()){
					sbMQL.append(" code ").append(UIUtil.singleQuotes(objInfo.getCode()));
				}else{					
					String getCode = objInfo.getCode().replaceAll("\"", "&quot;");
					sbMQL.append(" code ").append("\"" + getCode + "\"");
				}
			}
			if (objInfo.getLstArgument() != null && !objInfo.getLstArgument().isEmpty()) {
				List<Map<String, String>> lstSettings = objInfo.getLstArgument();
				for (Map<String, String> m : lstSettings) {
					sbMQL.append(removeArgumentSetting(m.get("name"), m.get("value")));
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}

	private String removeArgumentSetting(String name, String value) {
		StringBuilder sbReturn = new StringBuilder();
		if (name.startsWith(UIUtil.removeCharecter) && name.endsWith(UIUtil.removeCharecter)) {
			name = name.replace(UIUtil.removeCharecter, "");
			sbReturn.append(" remove ").append("argument").append(UIUtil.singleQuoteWithSpace(name));
		} else if (!name.startsWith(UIUtil.removeCharecter) && !name.endsWith(UIUtil.removeCharecter)) {
			sbReturn.append(" add ").append("argument ").append(UIUtil.singleQuoteWithSpace(name))
					.append(UIUtil.padWithSpaces(UIUtil.singleQuoteWithSpace(value)));
		}
		return sbReturn.toString();
	}
}
