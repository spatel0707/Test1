package com.izn.schemamodeler.ui3.webform;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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
import com.izn.schemamodeler.ui3.webform.WebFormXMLSchema.Schema;
import com.izn.schemamodeler.ui3.webform.WebFormXMLSchema.Schema.Basic;
import com.izn.schemamodeler.ui3.webform.WebFormXMLSchema.Schema.Column;
import com.izn.schemamodeler.ui3.webform.WebFormXMLSchema.Schema.Column.Data;
import com.izn.schemamodeler.ui3.webform.WebFormXMLSchema.Schema.Column.Data.Datadetail;
import com.izn.schemamodeler.ui3.webform.WebFormXMLSchema.Schema.Column.Data.Setting;
import com.izn.schemamodeler.ui3.webform.WebFormXMLSchema.Schema.Column.Data.Setting.Param;
import com.izn.schemamodeler.ui3.webform.WebFormXMLSchema.Schema.Field;
import com.izn.schemamodeler.ui3.webform.WebFormXMLSchema.Schema.Field.Detail;
import com.izn.schemamodeler.ui3.webform.WebFormXMLSchema.Schema.Column.Data.Accessdetail;
import com.izn.schemamodeler.ui3.webform.WebFormXMLSchema.Schema.Column.Data.Accessdetail.Access;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class WebFormLoader implements SchemaLoader {
	ObjectMapper _gson = new ObjectMapper();
	Schema _schemaElem;
	Basic _basicElem;
	Field _fieldElem;
	Column _columnElem;
	Data _dataElem;
	Datadetail _datadetail;
	Setting _settingElem;
	Param _paramElem;
	Detail _detailElem;
	Accessdetail _accessdetail;
	Access _access;
	List<WebForm> lstWebForm = new ArrayList<WebForm>();

	public WebFormLoader() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void loadSchema(Context context, String strSchemaName, String fileName, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty)  throws Exception{
		// TODO Auto-generated method stub
		try {
			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
			WebFormInfo webForm = (WebFormInfo) schemaFactory.getSchemaObject("form");
			JAXBContext jContext = JAXBContext.newInstance(WebFormXMLSchema.class);
			Unmarshaller unMarsheller = jContext.createUnmarshaller();
			JAXBElement<WebFormXMLSchema> webFormSchema = unMarsheller
					.unmarshal(new StreamSource(new FileReader(fileName)), WebFormXMLSchema.class);
			WebFormXMLSchema _webformElem = webFormSchema.getValue();
			List<Schema> lstSchema = _webformElem.getSchema();
			Iterator<Schema> itrSchema = lstSchema.iterator();
			WebForm webform = null;
			String dName = "";
			String dValue = "";
			int iColumnCounter = 0;
			Map<String, String> mSettings = null;
			while (itrSchema.hasNext()) {
				iColumnCounter = 0;
				_schemaElem = itrSchema.next();
				_basicElem = _schemaElem.getBasic();
				webform = new WebForm(_basicElem.getName(), _basicElem.getDescription(), _basicElem.getHidden(),
						_basicElem.getRegistryName(), _basicElem.getType());
				webform.setOldName(webform.name);
				List<Column> lstColumns = _schemaElem.getColumn();
				Iterator<Column> itrColumns = lstColumns.iterator();
				WebForm.Column[] wbColumn = new WebForm.Column[lstColumns.size()];
				List<WebForm.Column> ltWebFormColmn = new ArrayList<WebForm.Column>();
				while (itrColumns.hasNext()) {
					List<Map<String, String>> slSettings = new ArrayList<Map<String, String>>();
					List<Map<String, String>> slAcess = new ArrayList<Map<String, String>>();
					WebForm.Column columnObj = webform.getColumnObejct();
					_columnElem = itrColumns.next();
					_dataElem = _columnElem.getData();
					List<Datadetail> lstDatadetails = _dataElem.getDatadetail();
					Iterator<Datadetail> itrDatadetails = lstDatadetails.iterator();
					while (itrDatadetails.hasNext()) {
						_datadetail = itrDatadetails.next();
						dName = _datadetail.getName();
						dValue = _datadetail.getValueAttribute();
						if (dValue != null) {
							dValue = _datadetail.getValueAttribute().trim();
						} else {
							dValue = "";
						}
						if (dName.equalsIgnoreCase("column")) {
							columnObj.setColumnName(dValue);
						} else if (dName.equalsIgnoreCase("label")) {
							columnObj.setLabel(dValue);
						} else if (dName.equalsIgnoreCase("description")) {
							columnObj.setDescription(dValue);
						} else if (dName.equalsIgnoreCase("columnType")) {
							columnObj.setColumnType(dValue);
						} else if (dName.equalsIgnoreCase("expression")) {
							columnObj.setExpression(dValue);
						} else if (dName.equalsIgnoreCase("href")) {
							columnObj.setHref(dValue);
						} else if (dName.equalsIgnoreCase("alt")) {
							columnObj.setAlt(dValue);
						} else if (dName.equalsIgnoreCase("range")) {
							columnObj.setRange(dValue);
						} else if (dName.equalsIgnoreCase("update")) {
							columnObj.setUpdate(dValue);
						} else if (dName.equalsIgnoreCase("sorttype")) {
							columnObj.setSortType(dValue);
						} else if (dName.equalsIgnoreCase("order")) {
							if (!dValue.isEmpty())
								columnObj.setOrder(Integer.parseInt(dValue));
						} else if (dName.equalsIgnoreCase("user")) {
							columnObj.setUser(dValue);
						}
					}

					_accessdetail = _dataElem.getAccessdetail();
					List<Access> lstAcess = _accessdetail.getAccess();
					Iterator<Access> itrAcess = lstAcess.iterator();
					Map<String, String> mAccess = new HashMap<String, String>();
					while (itrAcess.hasNext()) {
						mAccess = new HashMap<String, String>();
						_access = itrAcess.next();
						dValue = _access.getValue();
						if (dValue != null) {
							dValue = _access.getValue().trim();
						} else {
							dValue = "";
						}
						mAccess.put("name", _access.getName());
						mAccess.put("value", dValue);
						slAcess.add(mAccess);
					}
					columnObj.setLstAccessDetail(slAcess);
					_settingElem = _dataElem.getSetting();
					List<Param> lstParam = _settingElem.getParam();
					Iterator<Param> itrParam = lstParam.iterator();
					Map<String, String> hSetting = new HashMap<String, String>();
					while (itrParam.hasNext()) {
						mSettings = new HashMap<String, String>();
						_paramElem = itrParam.next();
						dValue = _paramElem.getValueAttribute();
						if (dValue != null) {
							dValue = _paramElem.getValueAttribute().trim();
						} else {
							dValue = "";
						}
						mSettings.put("name", _paramElem.getName());
						mSettings.put("value",dValue);
						slSettings.add(mSettings);
					}
					columnObj.setLstSetting(slSettings);
					ltWebFormColmn.add(columnObj);
					wbColumn[iColumnCounter] = columnObj;
					iColumnCounter++;
				}
				Arrays.sort(wbColumn);
				webform.setLstColumn(ltWebFormColmn);
				// Sorted one ??
				webform.setWbColumn(wbColumn);
				lstWebForm.add(webform);
			}
			prepareWebFormMQL(context, lstWebForm, schema_done_log, scmConfigProperty);
		} catch (UnmarshalException ume) {
			schema_done_log.error("UnmarshalException on ["+strSchemaName+".xml] : "+ume.getCause());
			schema_done_log.warn(UIUtil.sUnmarshalExceptionWarningMessage);
			throw ume;
		} catch (Exception e) {
			schema_done_log.error(""+e.getCause());
			throw e;
		}
	}

	private void prepareWebFormMQL(Context context, List<WebForm> lstWebForms, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty)throws Exception {
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
		String strWebformName = "";
		String sOperation = "";
		List<String> listModified = new ArrayList<String>();
		List<String> listAdded = new ArrayList<String>();
		List<String> listDeleted = new ArrayList<String>();
		try {
			Iterator<WebForm> itrWebForms = lstWebForms.iterator();
			WebForm webForm = null;
			iCountTotal  = lstWebForms.size();
			while (itrWebForms.hasNext()) {
				
				webForm = itrWebForms.next();
				strWebformName = webForm.name;
				schema_done_log.info("Webform : ["
						+ webForm.name.replace(UIUtil.removeCharecter, "") + "]");
				try {
					ContextUtil.pushContext(context);
					String removeSchemaObject = UIUtil.removeSchemaObject(strWebformName, "form");
					if (removeSchemaObject.isEmpty()) {
						String strResult = MQLCommand.exec(context, "list form $1",
								new String[] { strWebformName.replace(UIUtil.removeCharecter, "") });
						if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
							List<String> alColumnNames = new ArrayList<String>();
							SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
							WebFormInfo formInfo = (WebFormInfo) schemaFactory.getSchemaObject("form");
							String strDbDef = formInfo.geSchemaInfo(context, webForm.name, "tbd");
							HashMap<Object, Object> hDBDef = _gson.readValue(strDbDef, HashMap.class);
							if (hDBDef != null && !hDBDef.isEmpty()) {
								List<Map> slDBColumns = (List<Map>) hDBDef.get("fields");
								Iterator<Map> itrDBFields = slDBColumns.iterator();
								while (itrDBFields.hasNext()) {
									Map m = itrDBFields.next();
									alColumnNames.add((String) m.get("column"));
								}
							}
							sMQL = prepareModifyExistingSchemaMQL(webForm, alColumnNames, schema_done_log);
							iCountModify += 1;
							listModified.add(strWebformName);
							sOperation = "mod";
						} else {
							sMQL = prepareAddNewSchemaMQL(webForm, schema_done_log);
							iCountAdd += 1;
							listAdded.add(strWebformName);
							sOperation = "add";
						}
						sMQLPropertyQuery = UIUtil.getMQLPropertyQuery(context, "form", webForm.name,
								webForm.registryname, sOperation);
					} else {
						sMQL = removeSchemaObject;
						iCountDelete += 1;
						listDeleted.add(strWebformName);
					}
					schema_done_log.info("MQL QUERY FOR IMPORT : " + sMQL);
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
					schema_done_log.info("TOTAL :" + iCountTotal + ", SUCCESS :" + iCountSuccess + ", ADDED :" + iCountAdd + ", MODIFIED :" + iCountModify + ", DELETED :"	+ iCountDelete + ".");
					throw new Exception("Error occurred while importing schema form : ["
							+ strWebformName.replace(UIUtil.removeCharecter, "") + "] : " + e.getMessage());
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
			WebForm objInfo = (WebForm) objectSchemaInfo;
			sbMQL.append(" add form ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name))).append(" web ");
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.getType() != null && !objInfo.getType().isEmpty()) {
				String[] splitType = objInfo.getType().toString().split(",");
				for(String sType : splitType){					
					sbMQL.append("type").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(sType)));
				}
			}
			if (objInfo.getWbColumn() != null) {
				WebForm.Column[] wc = objInfo.getWbColumn();
				int iWebColumnnOrder = 0; 
				String sWebColumnName = "";
				String strWebWColumnDescription = "";
				String strWebWColumnExpression = "";
				String strWebWColumnLabel = "";
				String strWebWColumnHref = "";
				String strWebWColumnUpdate = "";
				String strWebWColumnRange = "";
				String strWebWColumnType = "";
				String strWebWColumnAlt = "";
				for (WebForm.Column webColumn : wc) {
					sWebColumnName = webColumn.getColumnName();
					iWebColumnnOrder = webColumn.getOrder();
					strWebWColumnDescription = webColumn.getDescription();
					strWebWColumnExpression = webColumn.getExpression();
					strWebWColumnType = webColumn.getColumnType() != null ? webColumn.getColumnType() : "";
					strWebWColumnLabel = webColumn.getLabel();
					strWebWColumnHref = webColumn.getHref();
					strWebWColumnUpdate = webColumn.getUpdate();
					strWebWColumnRange = webColumn.getRange();
					strWebWColumnAlt = webColumn.getAlt();
					if (sWebColumnName != null && !sWebColumnName.isEmpty()) {
						sbMQL.append(" field name ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sWebColumnName)));
						if (strWebWColumnLabel != null && !strWebWColumnLabel.isEmpty()) {
							sbMQL.append(" label")
									.append(UIUtil.padWithSpaces(UIUtil.singleQuotes(strWebWColumnLabel)));
						}
						if (strWebWColumnExpression != null) {
							sbMQL.append(UIUtil.padWithSpaces(strWebWColumnType))
									.append(UIUtil.padWithSpaces(UIUtil.singleQuotes(strWebWColumnExpression)));
						}
						if (strWebWColumnHref != null && !strWebWColumnHref.isEmpty()) {
							sbMQL.append(" href").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(strWebWColumnHref)));
						}
						if (strWebWColumnRange != null && !strWebWColumnRange.isEmpty()) {
							sbMQL.append(" range")
									.append(UIUtil.padWithSpaces(UIUtil.singleQuotes(strWebWColumnRange)));
						}
						if (strWebWColumnUpdate != null && !strWebWColumnUpdate.isEmpty()) {
							sbMQL.append(" update")
									.append(UIUtil.padWithSpaces(UIUtil.singleQuotes(strWebWColumnUpdate)));
						}
						if (strWebWColumnAlt != null && !strWebWColumnAlt.isEmpty()) {
							sbMQL.append(" alt").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(strWebWColumnAlt)));
						}
						List<Map<String, String>> lstSettings = webColumn.getLstSetting();
						if (lstSettings != null && !lstSettings.isEmpty()) {
							for (Map<String, String> m : lstSettings) {
								if (!m.get("value").isEmpty())
									sbMQL.append(" setting ")
											.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(m.get("name"))))
											.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(m.get("value"))));
							}
						}
						List<Map<String, String>> sUser = webColumn.getLstAccessDetail();
						if (sUser != null && !sUser.isEmpty()) {
							for (Map<String, String> m : sUser) {
								if ((m.get("value")).indexOf(",") > 0) {
									String[] users = (m.get("value")).split(",");
									for (String user : users) {
										sbMQL.append(" ")
												.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(m.get("name"))))
												.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(user)));
									}
								} else {
									if (!m.get("value").isEmpty() && !(m.get("value")).equalsIgnoreCase("all")) {
										sbMQL.append(" ")
												.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(m.get("name"))))
												.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(m.get("value"))));
									}
								}
							}
						}
						if (strWebWColumnDescription != null && !strWebWColumnDescription.isEmpty()) {
							sbMQL.append(" description ").append('"').append(strWebWColumnDescription).append('"');
						}
					}
				}
			}
			if (objInfo.description != null && !objInfo.description.isEmpty()) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}

	private String prepareModifyExistingSchemaMQL(Object objectSchemaInfo, List<String> alColumnNames,
			Logger schema_done_log) throws Exception {
		StringBuilder sbMQL = new StringBuilder();
		try {
			WebForm objInfo = (WebForm) objectSchemaInfo;
			sbMQL.append(" mod form ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name))).append(" web ");
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.getType() != null && !objInfo.getType().isEmpty()) {
				String[] splitType = objInfo.getType().toString().split(",");
				for(String sType : splitType){	
					if(sType.startsWith(UIUtil.removeCharecter) && sType.endsWith(UIUtil.removeCharecter)){
						sType = sType.replace(UIUtil.removeCharecter, "");
						sbMQL.append(" type delete").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(sType)));
					}else if(!sType.startsWith(UIUtil.removeCharecter) && !sType.endsWith(UIUtil.removeCharecter)){
						sbMQL.append(" type ").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(sType)));
					}					
				}
			}
			if (objInfo.getWbColumn() != null) {
				WebForm.Column[] wc = objInfo.getWbColumn();
				int iWebColumnnOrder = 0;
				String sWebColumnName = "";
				String sOrder = "";
				String strWebWColumnDescription = "";
				String strWebWColumnExpression = "";
				String strWebWColumnLabel = "";
				String strWebWColumnHref = "";
				String strWebWColumnUpdate = "";
				String strWebWColumnRange = "";
				String strWebWColumnType = "";
				String strWebWColumnAlt = "";
				boolean bColumnExist = false;
				boolean bCanRemove = false;
				for (WebForm.Column webColumn : wc) {
					sWebColumnName = webColumn.getColumnName();
					iWebColumnnOrder = webColumn.getOrder();
					strWebWColumnDescription = webColumn.getDescription();
					strWebWColumnExpression = webColumn.getExpression() != null ? webColumn.getExpression() : "";
					strWebWColumnType = webColumn.getColumnType() != null ? webColumn.getColumnType() : "";
					strWebWColumnLabel = webColumn.getLabel();
					strWebWColumnHref = webColumn.getHref();
					strWebWColumnUpdate = webColumn.getUpdate();
					strWebWColumnRange = webColumn.getRange();
					strWebWColumnAlt = webColumn.getAlt();
					sOrder = String.valueOf(iWebColumnnOrder);
					if (sWebColumnName != null && !sWebColumnName.isEmpty()) {
						if (sWebColumnName.startsWith(UIUtil.removeCharecter)
								&& sWebColumnName.endsWith(UIUtil.removeCharecter)) {
							sWebColumnName = sWebColumnName.replace(UIUtil.removeCharecter, "");
							bCanRemove = true;
						} else if (!sWebColumnName.startsWith(UIUtil.removeCharecter)
								&& !sWebColumnName.endsWith(UIUtil.removeCharecter)) {
							bCanRemove = false;
						}
						if (bCanRemove && alColumnNames.contains(sWebColumnName)) {
							sbMQL.append(" field delete name ")
									.append(UIUtil.padWithSpaces(UIUtil.singleQuotes(sWebColumnName)));
							continue;
						} else if (bCanRemove && !alColumnNames.contains(sWebColumnName)) {
							continue;// throw exception : not properly removed.
						}
						if (alColumnNames.contains(sWebColumnName)) {
							sbMQL.append(" field modify name ");
							bColumnExist = true;
						} else {
							sbMQL.append(" field name ");
							bColumnExist = false;
						}
						sbMQL.append(UIUtil.padWithSpaces(UIUtil.singleQuotes(sWebColumnName)));
						if (sOrder != null && !sOrder.isEmpty()) {
							sbMQL.append(" order ").append(UIUtil.padWithSpaces(sOrder));
						}
						if (strWebWColumnLabel != null && !strWebWColumnLabel.isEmpty()) {
							sbMQL.append(" label")
									.append(UIUtil.padWithSpaces(UIUtil.singleQuotes(strWebWColumnLabel)));
						}
						if (!strWebWColumnType.isEmpty()) {
							sbMQL.append(UIUtil.padWithSpaces(strWebWColumnType)).append(UIUtil.padWithSpaces(UIUtil.singleQuotes(strWebWColumnExpression)));
						}else if(strWebWColumnType.isEmpty() && !strWebWColumnExpression.isEmpty()){
							sbMQL.append(UIUtil.padWithSpaces("select")).append(UIUtil.padWithSpaces(UIUtil.singleQuotes(strWebWColumnExpression)));
						}
						if (strWebWColumnHref != null && !strWebWColumnHref.isEmpty()) {
							sbMQL.append(" href").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(strWebWColumnHref)));
						}
						if (strWebWColumnRange != null && !strWebWColumnRange.isEmpty()) {
							sbMQL.append(" range")
									.append(UIUtil.padWithSpaces(UIUtil.singleQuotes(strWebWColumnRange)));
						}
						if (strWebWColumnUpdate != null && !strWebWColumnUpdate.isEmpty()) {
							sbMQL.append(" update")
									.append(UIUtil.padWithSpaces(UIUtil.singleQuotes(strWebWColumnUpdate)));
						}
						if (strWebWColumnAlt != null && !strWebWColumnAlt.isEmpty()) {
							sbMQL.append(" alt").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(strWebWColumnAlt)));
						}

						List<Map<String, String>> lstSettings = webColumn.getLstSetting();
						if (lstSettings != null && !lstSettings.isEmpty()) {
							for (Map<String, String> m : lstSettings) {
								if (!m.get("value").isEmpty() && bColumnExist)
									sbMQL.append(UIUtil.removeSettingValue(m.get("name"), m.get("value"), "setting"));
								else if (!m.get("value").isEmpty() && !bColumnExist)
									sbMQL.append(" setting ")
											.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(m.get("name"))))
											.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(m.get("value"))));
							}
						}
						List<Map<String, String>> sUser = webColumn.getLstAccessDetail();
						if (sUser != null && !sUser.isEmpty() && bColumnExist) {
							sbMQL.append(UIUtil.removeObjectAccess(sUser));
						} else if (sUser != null && !sUser.isEmpty() && !bColumnExist) {
							for (Map<String, String> m : sUser) {
								if ((m.get("value")).indexOf(",") > 0) {
									String[] users = (m.get("value")).split(",");
									for (String user : users) {
										sbMQL.append(" ")
												.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(m.get("name"))))
												.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(user)));
									}
								} else {
									if (!m.get("value").isEmpty() && !(m.get("value")).equalsIgnoreCase("all")) {
										sbMQL.append(" ")
												.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(m.get("name"))))
												.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(m.get("value"))));
									}
								}
							}
						}
						if (strWebWColumnDescription != null) {
							sbMQL.append(" description ").append('"').append(strWebWColumnDescription).append('"');
						}
					}
				}
			}
			if (objInfo.description != null) {
				sbMQL.append(" description ").append('"').append(objInfo.description).append('"');
				
			}
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}
}
