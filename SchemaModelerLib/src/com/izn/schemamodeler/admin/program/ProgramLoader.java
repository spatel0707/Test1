package com.izn.schemamodeler.admin.program;

import java.io.File;
import java.util.ArrayList;
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
import com.izn.schemamodeler.admin.program.Program;
import com.izn.schemamodeler.admin.program.ProgramInfo;
import com.izn.schemamodeler.admin.program.ProgramXMLSchema;
import com.izn.schemamodeler.admin.program.ProgramXMLSchema.Schema;
import com.izn.schemamodeler.admin.program.ProgramXMLSchema.Schema.Basic;
import com.izn.schemamodeler.admin.program.ProgramXMLSchema.Schema.Field;
import com.izn.schemamodeler.admin.program.ProgramXMLSchema.Schema.Field.Detail;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class ProgramLoader implements SchemaLoader {
	ObjectMapper _gson = new ObjectMapper();
	Basic _basicElem = null;
	Field _fieldElem = null;

	public ProgramLoader() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void loadSchema(Context context, String strSchemaName, String fileName, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty)  throws Exception{
		try {
			List<Program> lstProgram = new ArrayList<Program>();
			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
			ProgramInfo programInfo = (ProgramInfo) schemaFactory.getSchemaObject("program");
			// String strProgramInfo = programInfo.geSchemaInfo(context,
			// "TestProgram");
			// Map mDBInfo = _gson.fromJson(strProgramInfo, HashMap.class);

			JAXBContext jConext = JAXBContext.newInstance(ProgramXMLSchema.class);
			Unmarshaller unmarshaller = jConext.createUnmarshaller();
			JAXBElement<ProgramXMLSchema> programElem = unmarshaller.unmarshal(new StreamSource(new File(fileName)),
					ProgramXMLSchema.class);
			ProgramXMLSchema programXMLSchema = programElem.getValue();
			List<Schema> lstSchema = programXMLSchema.getSchema();
			Program program = null;
			String fValue = "";
			String fName = "";
			Map<String, String> mSettings = null;
			List<Map<String, String>> lstSetting = null;
			List<String> lstProgramItem = null;

			// To get program folder path
			File file = new File(fileName);
			String sParentPath = file.getParent() + "\\Programs";
			String sProgramName = "";
			for (Schema _schema : lstSchema) {
				lstProgramItem = new ArrayList<String>();
				_basicElem = _schema.getBasic();
				program = new Program(_basicElem.getName(), _basicElem.getHidden(), _basicElem.getDescription(), _basicElem.getRegistryName());
				sProgramName = _basicElem.getName();
				_fieldElem = _schema.getField();
				List<Detail> lstDetail = _fieldElem.getDetail();
				for (Detail detail : lstDetail) {
					fName = detail.getName();
					fValue = detail.getValueAttribute();
					if (fValue != null) {
						fValue = detail.getValueAttribute().trim();
					} else {
						fValue = "";
					}
					if (fName.equalsIgnoreCase("type")) 
					{
						program.setType(fValue);
						if("java".equals(fValue)) {							
							sProgramName = sProgramName.replace(".","\\");
							sProgramName = sProgramName + "_mxJPO.java";
						}
						if (sParentPath != null && !sParentPath.isEmpty()) {
							program.setFilepath(sParentPath + "\\" + sProgramName);
						}
					} else if (fName.equalsIgnoreCase("code")) {
						program.setCode(fValue);
					} else if (fName.equalsIgnoreCase("user")) {
						program.setUser(fValue);
					} else if (fName.equalsIgnoreCase("execute")) {
						program.setExecute(fValue);
					} else if (fName.equalsIgnoreCase("needsbusinessobject")) {
						program.setNeedsbusinessobject(fValue);
					} else if (fName.equalsIgnoreCase("downloadable")) {
						program.setDownloadable(fValue);
					} else if (fName.equalsIgnoreCase("pipe")) {
						program.setPipe(fValue);
					} else if (fName.equalsIgnoreCase("pooled")) {
						program.setPool(fValue);
					}
				}

				lstProgram.add(program);
			}

			prepareProgramMQL(context, lstProgram, schema_done_log, scmConfigProperty);
		} catch (UnmarshalException ume) {
			schema_done_log.error("UnmarshalException on ["+strSchemaName+".xml] : "+ume.getCause());
			schema_done_log.warn(UIUtil.sUnmarshalExceptionWarningMessage);
			throw ume;
		} catch (Exception e) {
			schema_done_log.error(""+e.getCause());
			throw e;
		}
	}

	private void prepareProgramMQL(Context context, List<Program> lstProgram, Logger schema_done_log,
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
		MQLCommand localMQLCommand = null;
		String strProgramName = "";
		List<String> listModified = new ArrayList<String>();
		List<String> listAdded = new ArrayList<String>();
		List<String> listDeleted = new ArrayList<String>();
		try {
			iCountTotal = lstProgram.size();
			for (Program program : lstProgram) {
				
				schema_done_log.info("Program : [" + program.name.replace(UIUtil.removeCharecter, "") + "]");
				strProgramName = program.name;
				ContextUtil.pushContext(context);
				try {
					String removeSchemaObject = UIUtil.removeSchemaObject(strProgramName, "program");
					if (removeSchemaObject.isEmpty()) {
						String strResult = MQLCommand.exec(context, "list program $1",
								new String[] { strProgramName.replace(UIUtil.removeCharecter, "") });
						if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
							sMQL = prepareModifyExistingSchemaMQL(program, schema_done_log);
							iCountModify += 1;
							listModified.add(strProgramName);
							sOperation = "mod";
						} else {
							sMQL = prepareAddNewSchemaMQL(program, schema_done_log);
							iCountAdd += 1;
							listAdded.add(strProgramName);
							sOperation = "add";
						}
					} else {
						sMQL = removeSchemaObject;
						iCountDelete += 1;
						listDeleted.add(strProgramName);
					}
					schema_done_log.info("MQL QUERY FOR IMPORT : " + sMQL);
					localMQLCommand = new MQLCommand();
					bMQLResult = localMQLCommand.executeCommand(context, sMQL, true);
					String sMQLError = (String) localMQLCommand.getError();
					schema_done_log.info("MQL QUERY EXECUTION RESULT : " + bMQLResult);
					if (bMQLResult) {
						iCountSuccess += 1;
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
					schema_done_log.info("TOTAL :" + iCountTotal + ", SUCCESS :" + iCountSuccess + ", ADDED :" + iCountAdd + ", MODIFIED :" + iCountModify + ", DELETED :"	+ iCountDelete + ".");
					throw new Exception("Error occurred while importing schema program : ["
							+ strProgramName.replace(UIUtil.removeCharecter, "") + "] : " + e.getMessage());
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
			Program objInfo = (Program) objectSchemaInfo;
			sbMQL.append(" add program ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null && !objInfo.description.isEmpty()) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.getType() != null && !objInfo.getType().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.getType()));
			}
			if (objInfo.getFilepath() != null && !objInfo.getFilepath().isEmpty()) {
				sbMQL.append(" file ").append(UIUtil.singleQuoteWithSpace(objInfo.getFilepath()));
				// if(objInfo.getType().equalsIgnoreCase("java")) {
					// sbMQL.setLength(sbMQL.length() - 2);
					// sbMQL.append("_mxJPO.java ");
					// sbMQL.append("'");
				// }
			} else {
				sbMQL.append(" code ").append(UIUtil.singleQuoteWithSpace(objInfo.getCode()));
			}
			if (objInfo.getExecute() != null && !objInfo.getExecute().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces("execute")).append(UIUtil.quoteArgument(objInfo.getExecute()));
			}
			if (objInfo.getUser() != null && !objInfo.getUser().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces("execute user")).append(UIUtil.quoteArgument(objInfo.getUser()));
			}
			if (objInfo.getPipe() != null && !objInfo.getPipe().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.getPipe()));
			}
			if (objInfo.getExecute() != null && !objInfo.getExecute().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces("execute")).append(UIUtil.quoteArgument(objInfo.getExecute()));
			}
			sbMQL.append(UIUtil.padWithSpaces(objInfo.getDownloadable()));
			sbMQL.append(objInfo.getPool());
			sbMQL.append(UIUtil.padWithSpaces(objInfo.getNeedsbusinessobject()));
			if(objInfo.getType().equalsIgnoreCase("java")) {
				sbMQL.append(" ; compile Program "+objInfo.name+" force update ;");
				}
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}

	private String prepareModifyExistingSchemaMQL(Object objectSchemaInfo, Logger schema_done_log) throws Exception {
		StringBuilder sbMQL = new StringBuilder();
		try {
			Program objInfo = (Program) objectSchemaInfo;
			sbMQL.append(" mod program ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.getType() != null && !objInfo.getType().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.getType()));
			}
			if (objInfo.getFilepath() != null && !objInfo.getFilepath().isEmpty()) {
				sbMQL.append(" file ").append(UIUtil.singleQuoteWithSpace(objInfo.getFilepath()));
				// if(objInfo.getType().equalsIgnoreCase("java")) {
					// sbMQL.setLength(sbMQL.length() - 2);
					// sbMQL.append("_mxJPO.java ");
					// sbMQL.append("'");
				// }
			} else {
				sbMQL.append(" code ").append(UIUtil.singleQuoteWithSpace(objInfo.getCode()));
			}
			if (objInfo.getExecute() != null && !objInfo.getExecute().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces("execute")).append(UIUtil.quoteArgument(objInfo.getExecute()));
			}
			if (objInfo.getUser() != null && !objInfo.getUser().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces("execute user")).append(UIUtil.quoteArgument(objInfo.getUser()));
			}
			if (objInfo.getPipe() != null && !objInfo.getPipe().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.getPipe()));
			}
			if (objInfo.getExecute() != null && !objInfo.getExecute().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces("execute")).append(UIUtil.quoteArgument(objInfo.getExecute()));
			}
			sbMQL.append(UIUtil.padWithSpaces(objInfo.getDownloadable()));
			sbMQL.append(objInfo.getPool());
			sbMQL.append(UIUtil.padWithSpaces(objInfo.getNeedsbusinessobject()));
			if(objInfo.getType().equalsIgnoreCase("java")) {
				sbMQL.append(" ; compile Program "+objInfo.name+" force update ;");
			}
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}
}
