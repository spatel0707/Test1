package com.izn.schemamodeler.ui3.command;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.izn.schemamodeler.SchemaFactoryInfo;
import com.izn.schemamodeler.SchemaLoader;
import com.izn.schemamodeler.ui3.command.CommandXMLSchema.Schema;
import com.izn.schemamodeler.ui3.command.CommandXMLSchema.Schema.Basic;
import com.izn.schemamodeler.ui3.command.CommandXMLSchema.Schema.Field;
import com.izn.schemamodeler.ui3.command.CommandXMLSchema.Schema.Field.Accessdetail;
import com.izn.schemamodeler.ui3.command.CommandXMLSchema.Schema.Field.Accessdetail.Access;
import com.izn.schemamodeler.ui3.command.CommandXMLSchema.Schema.Field.Detail;
import com.izn.schemamodeler.ui3.command.CommandXMLSchema.Schema.Field.Setting;
import com.izn.schemamodeler.ui3.command.CommandXMLSchema.Schema.Field.Setting.Param;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class CommandLoader implements SchemaLoader {
	ObjectMapper _gson = new ObjectMapper();
	Basic _basicElem = null;
	Field _fieldElem = null;
	Setting _setting = null;
	Accessdetail _accessdetail = null;

	public CommandLoader() {
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("null")
	@Override
	public void loadSchema(Context context, String strSchemaName, String fileName, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty)  throws Exception{
		try {
			List<Command> lstCommand = new ArrayList<Command>();
			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
			CommandInfo commandInfo = (CommandInfo) schemaFactory.getSchemaObject("command");
			JAXBContext jConext = JAXBContext.newInstance(CommandXMLSchema.class);
			Unmarshaller unmarshaller = jConext.createUnmarshaller();
			JAXBElement<CommandXMLSchema> commandElem = unmarshaller.unmarshal(new StreamSource(new File(fileName)),
					CommandXMLSchema.class);
			CommandXMLSchema commandXMLSchema = commandElem.getValue();
			List<Schema> lstSchema = commandXMLSchema.getSchema();
			Command command = null;
			String fValue = "";
			String fName = "";
			Map<String, String> mSettings = null;
			Map<String, String> mAccessdetail = null;
			List<Map<String, String>> lstSetting = null;
			List<Map<String, String>> lstaccessdetail = new ArrayList<Map<String, String>>();

			for (Schema _schema : lstSchema) {
				lstSetting = new ArrayList<Map<String, String>>();
				_basicElem = _schema.getBasic();
				command = new Command(_basicElem.getName().trim(), _basicElem.getDescription(), _basicElem.getHidden(),
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
					if (fName.equalsIgnoreCase("label")) {
						command.setLabel(fValue);
					} else if (fName.equalsIgnoreCase("href")) {
						command.setHref(fValue);
					} else if (fName.equalsIgnoreCase("alt")) {
						command.setAlt(fValue);
					} else if (fName.equalsIgnoreCase("code")) {
						command.setCode(fValue);
					} else if (fName.equalsIgnoreCase("user")) {
						command.setUser(fValue);
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

				command.setLstSetting(lstSetting);

				_accessdetail = _fieldElem.getAccessdetail();
				if (_accessdetail != null) {
					List<Access> lstObjAccess = _accessdetail.getAccess();
					if (!lstObjAccess.isEmpty()) {
						for (Access _access : lstObjAccess) {
							mAccessdetail = new HashMap<String, String>();
							fValue = _access.getValue();
							if (fValue != null) {
								fValue = _access.getValue().trim();
							} else {
								fValue = "";
							}
							mAccessdetail.put("name", _access.getName());
							mAccessdetail.put("value", fValue);
							lstaccessdetail.add(mAccessdetail);
						}
					}
				}
				command.setLstaccessdetail(lstaccessdetail);
				lstCommand.add(command);
			}
			prepareCommandMQL(context, lstCommand, schema_done_log, scmConfigProperty);
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
	private void prepareCommandMQL(Context context, List<Command> lstCommand, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty) throws Exception{
		boolean bMQLResult;
		int iCountTotal = 0;
		int iCountSuccess = 0;
		int iCountFailure = 0;
		int iCountAdd = 0;
		int iCountModify = 0;
		int iCountDelete = 0;
		String sMQL = "";
		String sMQLPropertyQuery = "";
		MQLCommand localMQLCommand = null;
		String strCommandName = "";
		String sOperation = "";
		List<String> listModified = new ArrayList<String>();
		List<String> listAdded = new ArrayList<String>();
		List<String> listDeleted = new ArrayList<String>();
		try {
			iCountTotal = lstCommand.size();
			for (Command command : lstCommand) {
				
				schema_done_log.info("Command : ["
						+ command.name.replace(UIUtil.removeCharecter, "") + "]");
				strCommandName = command.name;
				// String strResult = MQLCommand.exec(context, "list command $1", new String[]
				// {"'"+strCommandName+""});
				ContextUtil.pushContext(context);
				try {
					String removeSchemaObject = UIUtil.removeSchemaObject(strCommandName, "command");
					if (removeSchemaObject.isEmpty()) {
						String strResult = MQLCommand.exec(context, "list command $1",
								new String[] { strCommandName.replace(UIUtil.removeCharecter, "") });
						if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
							sMQL = prepareModifyExistingSchemaMQL(command, schema_done_log);
							iCountModify += 1;
							listModified.add(strCommandName);
							sOperation = "mod";
						} else {
							sMQL = prepareAddNewSchemaMQL(command, schema_done_log);
							iCountAdd += 1;
							listAdded.add(strCommandName);
							sOperation = "add";
						}
						sMQLPropertyQuery = UIUtil.getMQLPropertyQuery(context, "command", command.name,
								command.registryname, sOperation);
					} else {
						sMQL = removeSchemaObject;
						iCountDelete += 1;
						listDeleted.add(strCommandName);
					}
					schema_done_log.info("MQL QUERY FOR IMPORT : " + sMQL);

					ContextUtil.pushContext(context);
					localMQLCommand = new MQLCommand();
					bMQLResult = localMQLCommand.executeCommand(context, sMQL, true);
					schema_done_log.info("MQL QUERY EXECUTION RESULT : " + bMQLResult);
					String sMQLError = (String) localMQLCommand.getError();
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
					throw new Exception("Error occurred while importing schema command : ["
							+ strCommandName.replace(UIUtil.removeCharecter, "") + "] : " + e.getMessage());
				}
				schema_done_log.info("-----------------------------------------------------------------");
			}
			schema_done_log.info("TOTAL  :" + iCountTotal + ", SUCCESS :" + iCountSuccess +
					 ", ADDED  :" + iCountAdd + ", MODIFIED :" + iCountModify + ", DELETED :"
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
			Command objInfo = (Command) objectSchemaInfo;
			sbMQL.append(" add command ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null && !objInfo.description.isEmpty()) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.getLabel() != null && !objInfo.getLabel().isEmpty()) {
				sbMQL.append(" label ").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(objInfo.getLabel())));
			}
			if (objInfo.getHref() != null && !objInfo.getHref().isEmpty()) {
				sbMQL.append(" href ").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(objInfo.getHref())));
			}
			if (objInfo.getAlt() != null && !objInfo.getAlt().isEmpty()) {
				sbMQL.append(" alt ").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(objInfo.getAlt())));
			}
			if (objInfo.getCode() != null && !objInfo.getCode().isEmpty()) {
				sbMQL.append(" code ").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(objInfo.getCode())));
			}

			List<Map<String, String>> lstSettings = objInfo.getLstSetting();
			if (lstSettings != null && !lstSettings.isEmpty()) {
				for (Map<String, String> m : lstSettings) {
					sbMQL.append(" setting ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(m.get("name"))))
							.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(m.get("value"))));
				}
			}

			List<Map<String, String>> sUser = objInfo.getLstaccessdetail();
			if (sUser != null && !sUser.isEmpty()) {
				for (Map<String, String> m : sUser) {
					if ((m.get("value")).indexOf(",") > 0) {
						String[] users = (m.get("value")).split(",");
						for (String user : users) {
							sbMQL.append(" ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(m.get("name"))))
									.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(user)));
						}
					} else {
						if (!(m.get("value")).equalsIgnoreCase("all")) {
							sbMQL.append(" ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(m.get("name"))))
									.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(m.get("value"))));
						}
					}
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
			Command objInfo = (Command) objectSchemaInfo;
			sbMQL.append(" mod command ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.getLabel() != null) {
				sbMQL.append(" label ").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(objInfo.getLabel())));
			}
			if (objInfo.getHref() != null) {
				sbMQL.append(" href ").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(objInfo.getHref())));
			}
			if (objInfo.getAlt() != null) {
				sbMQL.append(" alt ").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(objInfo.getAlt())));
			}
			if (objInfo.getCode() != null) {
				sbMQL.append(" code ").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(objInfo.getCode())));
			}
			List<Map<String, String>> lstSettings = objInfo.getLstSetting();
			if (lstSettings != null && !lstSettings.isEmpty()) {
				for (Map<String, String> m : lstSettings) {
					sbMQL.append(UIUtil.removeSettingValue(m.get("name"), m.get("value"), "setting"));
				}
			}
			List<Map<String, String>> sUser = objInfo.getLstaccessdetail();
			if (sUser != null && !sUser.isEmpty()) {
				sbMQL.append(UIUtil.removeObjectAccess(sUser));
			}
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}
}
