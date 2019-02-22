package com.izn.schemamodeler.admin.format;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import com.izn.schemamodeler.admin.format.FormatXMLSchema.Schema;
import com.izn.schemamodeler.admin.format.FormatXMLSchema.Schema.Basic;
import com.izn.schemamodeler.admin.format.FormatXMLSchema.Schema.Field;
import com.izn.schemamodeler.admin.format.FormatXMLSchema.Schema.Field.Detail;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class FormatLoader implements SchemaLoader {
	ObjectMapper _gson = new ObjectMapper();
	Basic _basicElem = null;
	Field _fieldElem = null;

	public FormatLoader() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void loadSchema(Context context, String strSchemaName, String fileName, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty)  throws Exception{
		try {
			List<Format> lstFormat = new ArrayList<Format>();
			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
			FormatInfo formatInfo = (FormatInfo) schemaFactory.getSchemaObject("format");
			JAXBContext jConext = JAXBContext.newInstance(FormatXMLSchema.class);
			Unmarshaller unmarshaller = jConext.createUnmarshaller();
			JAXBElement<FormatXMLSchema> formatElem = unmarshaller.unmarshal(new StreamSource(new File(fileName)),
					FormatXMLSchema.class);
			FormatXMLSchema formatXMLSchema = formatElem.getValue();
			List<Schema> lstSchema = formatXMLSchema.getSchema();
			Format format = null;
			String fValue = "";
			String fName = "";
			String sFilePath = "";
			Map<String, String> mSettings = null;
			List<Map<String, String>> lstSetting = null;
			List<String> lstFormatItem = null;
			List<String> lstAChild = null;
			for (Schema _schema : lstSchema) {
				lstSetting = new ArrayList<Map<String, String>>();
				lstAChild = new ArrayList<String>();
				lstFormatItem = new ArrayList<String>();
				_basicElem = _schema.getBasic();
				String formatName = _basicElem.getName();
				format = new Format(_basicElem.getName(), _basicElem.getDescription(), _basicElem.getHidden(),
						_basicElem.getRegistryName());
				_fieldElem = _schema.getField();
				List<Detail> lstDetail = _fieldElem.getDetail();
				for (Detail _detail : lstDetail) {
					fName = _detail.getName();
					fValue = _detail.getvalueAttribute();
					if (fValue != null) {
						fValue = _detail.getvalueAttribute().trim();
					} else {
						fValue = "";
					}
					if (fName.equalsIgnoreCase("version")) {
						format.setVersion(fValue.trim());
					} else if (fName.equalsIgnoreCase("filesuffix")) {
						format.setFilesuffix(fValue.trim());
					} else if (fName.equalsIgnoreCase("filecreator")) {
						format.setFilecreator(fValue.trim());
					} else if (fName.equalsIgnoreCase("filetype")) {
						format.setFiletype(fValue.trim());
					}

				}
				lstFormat.add(format);

			}

			// Gson gson = new Gson();
			// String json = gson.toJson(lstFormat);
			prepareFormatMQL(context, lstFormat, schema_done_log, scmConfigProperty);
		} catch (UnmarshalException ume) {
			schema_done_log.error("UnmarshalException on ["+strSchemaName+".xml] : "+ume.getCause());
			schema_done_log.warn(UIUtil.sUnmarshalExceptionWarningMessage);
			throw ume;
		} catch (Exception e) {
			schema_done_log.error(""+e.getCause());
			throw e;
		}

		// TODO Auto-generated method stub

	}

	private void prepareFormatMQL(Context context, List<Format> lstFormat, Logger schema_done_log,
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
		MQLCommand localMQLCommand = null;
		String strFormatName = "";
		List<String> listModified = new ArrayList<String>();
		List<String> listAdded = new ArrayList<String>();
		List<String> listDeleted = new ArrayList<String>();
		try {
			iCountTotal = lstFormat.size();
			for (Format format : lstFormat) {
				schema_done_log.info("Format : ["+ format.name.replace(UIUtil.removeCharecter, "") + "]");
				ContextUtil.pushContext(context);
				strFormatName = format.name;
				try {
					String removeSchemaObject = UIUtil.removeSchemaObject(strFormatName, "format");
					if (removeSchemaObject.isEmpty()) {
						String strResult = MQLCommand.exec(context, "list format $1",
								new String[] { strFormatName.replace(UIUtil.removeCharecter, "") });
						if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
							sMQL = prepareModifyExistingSchemaMQL(format, schema_done_log);
							iCountModify += 1;
							listModified.add(strFormatName);
							sOperation = "mod";
						} else {
							sMQL = prepareAddNewSchemaMQL(format, schema_done_log);
							iCountAdd += 1;
							listAdded.add(strFormatName);
							sOperation = "add";
						}
						sMQLPropertyQuery = UIUtil.getMQLPropertyQuery(context, "format", format.name,
								format.registryname, sOperation);
					} else {
						sMQL = removeSchemaObject;
						iCountDelete += 1;
						listDeleted.add(strFormatName);
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
					schema_done_log.info("TOTAL :" + iCountTotal + ", SUCCESS :" + iCountSuccess  + ", ADDED :" + iCountAdd + ", MODIFIED :" + iCountModify + ", DELETED :"	+ iCountDelete + ".");
					throw new Exception("Error occurred while importing schema attribute : ["
							+ strFormatName.replace(UIUtil.removeCharecter, "") + "] : " + e.getMessage());

				}
				schema_done_log.info("-----------------------------------------------------------------");
			}
			schema_done_log.info("TOTAL  :" + iCountTotal + ", SUCCESS :" + iCountSuccess + ", ADDED  :" + iCountAdd + ", MODIFIED :" + iCountModify + ", DELETED :"
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
			Format objInfo = (Format) objectSchemaInfo;
			sbMQL.append(" add format ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null && !objInfo.description.isEmpty()) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.version != null && !objInfo.version.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces("version")).append(UIUtil.quoteArgument(objInfo.version));
			}
			if (objInfo.filesuffix != null && !objInfo.filesuffix.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces("suffix")).append(UIUtil.quoteArgument(objInfo.filesuffix));
			}
			if (objInfo.filecreator != null && !objInfo.filecreator.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces("creator")).append(UIUtil.quoteArgument(objInfo.filecreator));
			}
			if (objInfo.filetype != null && !objInfo.filetype.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces("type")).append(UIUtil.quoteArgument(objInfo.filetype));
			}
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}

	private String prepareModifyExistingSchemaMQL(Object objectSchemaInfo, Logger schema_done_log) throws Exception {
		StringBuilder sbMQL = new StringBuilder();
		try {
			Format objInfo = (Format) objectSchemaInfo;
			sbMQL.append(" mod format ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.version != null && !objInfo.version.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces("version")).append(UIUtil.quoteArgument(objInfo.version));
			}
			if (objInfo.filesuffix != null && !objInfo.filesuffix.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces("suffix")).append(UIUtil.quoteArgument(objInfo.filesuffix));
			}
			if (objInfo.filecreator != null && !objInfo.filecreator.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces("creator")).append(UIUtil.quoteArgument(objInfo.filecreator));
			}
			if (objInfo.filetype != null && !objInfo.filetype.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces("type")).append(UIUtil.quoteArgument(objInfo.filetype));
			}
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}
}
