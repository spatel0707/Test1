package com.izn.schemamodeler.ui3.table;

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
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.transform.stream.StreamSource;
import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.izn.schemamodeler.SchemaFactoryInfo;
import com.izn.schemamodeler.SchemaLoader;
import com.izn.schemamodeler.ui3.table.TableXMLSchema.Schema;

import com.izn.schemamodeler.ui3.table.TableXMLSchema.Schema.Basic;
import com.izn.schemamodeler.ui3.table.TableXMLSchema.Schema.Column;

import com.izn.schemamodeler.ui3.table.TableXMLSchema.Schema.Column.Data;
import com.izn.schemamodeler.ui3.table.TableXMLSchema.Schema.Column.Data.Datadetail;
import com.izn.schemamodeler.ui3.table.TableXMLSchema.Schema.Column.Data.Setting;
import com.izn.schemamodeler.ui3.table.TableXMLSchema.Schema.Column.Data.Setting.Param;
import com.izn.schemamodeler.ui3.table.TableXMLSchema.Schema.Column.Data.Accessdetail;
import com.izn.schemamodeler.ui3.table.TableXMLSchema.Schema.Column.Data.Accessdetail.Access;
import com.izn.schemamodeler.ui3.table.Table;
import com.izn.schemamodeler.ui3.table.TableInfo;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class TableLoader implements SchemaLoader {
	ObjectMapper _gson = new ObjectMapper();
	Schema _schemaElem;
	Basic _basicElem;
	Column _columnElem;
	Data _dataElem;
	Datadetail _datadetail;
	Setting _settingElem;
	Param _paramElem;
	Accessdetail _accessdetail;
	Access _acces;
	List<Table> lstTable = new ArrayList<Table>();

	public TableLoader() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void loadSchema(Context context, String strSchemaName, String fileName, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty)  throws Exception{

		// TODO Auto-generated method stub
		try {
			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();

			TableInfo tTable = (TableInfo) schemaFactory.getSchemaObject("table");
			JAXBContext jContext = JAXBContext.newInstance(TableXMLSchema.class);
			Unmarshaller unMarsheller = jContext.createUnmarshaller();
			JAXBElement<TableXMLSchema> tableSchema = unMarsheller.unmarshal(new StreamSource(new FileReader(fileName)),
					TableXMLSchema.class);
			TableXMLSchema _tableElem = tableSchema.getValue();
			List<Schema> lstSchema = _tableElem.getSchema();
			Iterator<Schema> itrSchema = lstSchema.iterator();
			Table table = null;
			String dName = "";
			String dValue = "";
			int iColumnCounter = 0;
			Map<String, String> mSettings = null;
			while (itrSchema.hasNext()) {
				iColumnCounter = 0;
				_schemaElem = itrSchema.next();
				_basicElem = _schemaElem.getBasic();
				table = new Table(_basicElem.getName(), _basicElem.getDescription(), _basicElem.getHidden(),
						_basicElem.getRegistryName());
				table.setOldName(table.name);
				List<Column> lstColumns = _schemaElem.getColumn();
				Iterator<Column> itrColumns = lstColumns.iterator();
				Table.Column[] wbColumn = new Table.Column[lstColumns.size()];
				List<Table.Column> lttTableColmn = new ArrayList<Table.Column>();
				while (itrColumns.hasNext()) {
					List<Map<String, String>> slSettings = new ArrayList<Map<String, String>>();
					List<Map<String, String>> slAcess = new ArrayList<Map<String, String>>();
					Table.Column columnObj = table.getColumnObejct();
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
						_acces = itrAcess.next();
						dValue = _acces.getValue();
						if (dValue != null) {
							dValue = _acces.getValue().trim();
						} else {
							dValue = "";
						}
						mAccess.put("name", _acces.getName());
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
						mSettings.put("value", dValue);
						slSettings.add(mSettings);
					}
					if (slSettings != null) {
						columnObj.setLstSetting(slSettings);
					}
					lttTableColmn.add(columnObj);
					wbColumn[iColumnCounter] = columnObj;
					iColumnCounter++;
				}
				Arrays.sort(wbColumn);
				table.setLstColumn(lttTableColmn);
				// Sorted one ??
				table.setWbColumn(wbColumn);
				lstTable.add(table);
			}
			prepareTableMQL(context, lstTable, schema_done_log, scmConfigProperty);
		} catch (UnmarshalException ume) {
			schema_done_log.error("UnmarshalException on ["+strSchemaName+".xml] : "+ume.getCause());
			schema_done_log.warn(UIUtil.sUnmarshalExceptionWarningMessage);
			throw ume;
		} catch (Exception e) {
			schema_done_log.error(""+e.getCause());
			throw e;
		}
	}

	private void prepareTableMQL(Context context, List<Table> lstTable, Logger schema_done_log,
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
		String strTableName = "";
		String sOperation = "";
		List<String> listModified = new ArrayList<String>();
		List<String> listAdded = new ArrayList<String>();
		List<String> listDeleted = new ArrayList<String>();
		try {
			Iterator<Table> itrTable = lstTable.iterator();
			Table table = null;
			iCountTotal  = lstTable.size();
			while (itrTable.hasNext()) {
				
				table = itrTable.next();
				strTableName = table.name;
				schema_done_log.info("Table : ["
						+ table.name.replace(UIUtil.removeCharecter, "") + "]");
				try {
					ContextUtil.pushContext(context);
					String removeSchemaObject = UIUtil.removeSchemaObject(strTableName, "table");
					if (removeSchemaObject.isEmpty()) {
						String strResult = MQLCommand.exec(context, "list table system $1",
								new String[] { strTableName.replace(UIUtil.removeCharecter, "") });
						if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
							List<String> alColumnNames = new ArrayList<String>();
							SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
							TableInfo tableInfo = (TableInfo) schemaFactory.getSchemaObject("table");
							String strDbDef = tableInfo.geSchemaInfo(context, table.name, "tbd");
							HashMap<Object, Object> hDBDef = _gson.readValue(strDbDef, HashMap.class);
							if (hDBDef != null && !hDBDef.isEmpty()) {
								List<Map> slDBColumns = (List<Map>) hDBDef.get("columns");
								Iterator<Map> itrDBFields = slDBColumns.iterator();
								while (itrDBFields.hasNext()) {
									Map m = itrDBFields.next();
									alColumnNames.add((String) m.get("column"));
								}
							}
							sMQL = prepareModifyExistingSchemaMQL(table, alColumnNames, schema_done_log);
							iCountModify += 1;
							listModified.add(strTableName);
							sOperation = "mod";
						} else {
							sMQL = prepareAddNewSchemaMQL(table, schema_done_log);
							iCountAdd += 1;
							listAdded.add(strTableName);
							sOperation = "add";
						}
						sMQLPropertyQuery = UIUtil.getMQLPropertyQuery(context, "table", table.name, table.registryname,
								sOperation);
					} else {
						sMQL = removeSchemaObject;
						iCountDelete += 1;
						listDeleted.add(strTableName);
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
					throw new Exception("Error occurred while importing schema table : ["
							+ strTableName.replace(UIUtil.removeCharecter, "") + "] : " + e.getMessage());
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
			Table objInfo = (Table) objectSchemaInfo;
			sbMQL.append(" add table ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)))
					.append(" system ");
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.geTbColumn() != null) {
				Table.Column[] tc = objInfo.geTbColumn();
				int iTBColumnnOrder = 0;
				String sTBColumnName = "";
				String strTBColumnDescription = "";
				String strTBColumnExpression = "";
				String strTBColumnLabel = "";
				String strTBColumnHref = "";
				String strTBColumnUpdate = "";
				String strTBColumnRange = "";
				String strTBColumnType = "";
				String strTBColumnAlt = "";
				String strTBColumnSortType = "";
				for (Table.Column tbColumn : tc) {
					sTBColumnName = tbColumn.getColumnName();
					iTBColumnnOrder = tbColumn.getOrder();
					strTBColumnDescription = tbColumn.getDescription();
					strTBColumnExpression = tbColumn.getExpression() != null ? tbColumn.getExpression() : "";
					strTBColumnType = tbColumn.getColumnType() != null ? tbColumn.getColumnType() : "";
					strTBColumnLabel = tbColumn.getLabel();
					strTBColumnHref = tbColumn.getHref();
					strTBColumnUpdate = tbColumn.getUpdate();
					strTBColumnRange = tbColumn.getRange();
					strTBColumnAlt = tbColumn.getAlt();
					strTBColumnSortType = tbColumn.getSortType();
					if (sTBColumnName != null && !sTBColumnName.isEmpty()) {
						sbMQL.append(" column name ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sTBColumnName)));
						if (strTBColumnLabel != null && !strTBColumnLabel.isEmpty()) {
							sbMQL.append(" label").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(strTBColumnLabel)));
						}
						if (!strTBColumnType.isEmpty()) {
							sbMQL.append(UIUtil.padWithSpaces(strTBColumnType)).append(UIUtil.padWithSpaces(UIUtil.singleQuotes(strTBColumnExpression)));
						}
						if (strTBColumnHref != null && !strTBColumnHref.isEmpty()) {
							sbMQL.append(" href").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(strTBColumnHref)));
						}
						if (strTBColumnRange != null && !strTBColumnRange.isEmpty()) {
							sbMQL.append(" range").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(strTBColumnRange)));
						}
						if (strTBColumnUpdate != null && !strTBColumnUpdate.isEmpty()) {
							sbMQL.append(" update")
									.append(UIUtil.padWithSpaces(UIUtil.singleQuotes(strTBColumnUpdate)));
						}
						if (strTBColumnAlt != null && !strTBColumnAlt.isEmpty()) {
							sbMQL.append(" alt").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(strTBColumnAlt)));
						}
						if (strTBColumnSortType != null && !strTBColumnSortType.isEmpty()) {
							sbMQL.append(" sorttype").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(strTBColumnSortType)));
						}
						List<Map<String, String>> lstSettings = tbColumn.getLstSetting();
						if (lstSettings != null && !lstSettings.isEmpty()) {
							for (Map<String, String> m : lstSettings) {
								if (!m.get("value").isEmpty())
									sbMQL.append(" setting ")
											.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(m.get("name"))))
											.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(m.get("value"))));
							}
						}
						List<Map<String, String>> sUser = tbColumn.getLstAccessDetail();
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
						if (strTBColumnDescription != null && !strTBColumnDescription.isEmpty()) {
							sbMQL.append(" description ").append('"').append(strTBColumnDescription).append('"');
						}
					}
				}
			}
			if (objInfo.description != null && !objInfo.description.isEmpty()) {
				sbMQL.append(" description ").append('"').append(objInfo.description).append('"');
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
			Table objInfo = (Table) objectSchemaInfo;
			sbMQL.append(" mod table ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)))
					.append(" system ");
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.geTbColumn() != null) {
				Table.Column[] tc = objInfo.geTbColumn();
				int iTBColumnnOrder = 0;
				String sOrder = "";
				String sTBColumnName = "";
				String strTBColumnDescription = "";
				String strTBColumnExpression = "";
				String strTBColumnLabel = "";
				String strTBColumnHref = "";
				String strTBColumnUpdate = "";
				String strTBColumnRange = "";
				String strTBColumnType = "";
				String strTBColumnAlt = "";
				String strTBColumnSortType = "";
				boolean bColumnExist = false;
				boolean bCanRemove = false;
				for (Table.Column tbColumn : tc) {
					sTBColumnName = tbColumn.getColumnName();
					iTBColumnnOrder = tbColumn.getOrder();
					sOrder = String.valueOf(iTBColumnnOrder);
					strTBColumnDescription = tbColumn.getDescription();
					strTBColumnExpression = tbColumn.getExpression() != null ? tbColumn.getExpression() : "";
					strTBColumnType = tbColumn.getColumnType() != null ? tbColumn.getColumnType() : "";
					strTBColumnLabel = tbColumn.getLabel();
					strTBColumnHref = tbColumn.getHref();
					strTBColumnUpdate = tbColumn.getUpdate();
					strTBColumnRange = tbColumn.getRange();
					strTBColumnAlt = tbColumn.getAlt();
					strTBColumnSortType = tbColumn.getSortType();
					if (sTBColumnName != null && !sTBColumnName.isEmpty()) {
						if (sTBColumnName.startsWith(UIUtil.removeCharecter)
								&& sTBColumnName.endsWith(UIUtil.removeCharecter)) {
							sTBColumnName = sTBColumnName.replace(UIUtil.removeCharecter, "");
							bCanRemove = true;
						} else if (!sTBColumnName.startsWith(UIUtil.removeCharecter)
								&& !sTBColumnName.endsWith(UIUtil.removeCharecter)) {
							bCanRemove = false;
						}
						if (bCanRemove && alColumnNames.contains(sTBColumnName)) {
							sbMQL.append(" column delete name ")
									.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sTBColumnName)));
							continue;
						} else if (bCanRemove && !alColumnNames.contains(sTBColumnName)) {
							continue;// throw exception : not properly removed.
						}
						if (alColumnNames.contains(sTBColumnName)) {
							sbMQL.append(" column modify name ");
							bColumnExist = true;
						} else {
							sbMQL.append(" column name ");
							bColumnExist = false;
						}
						sbMQL.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sTBColumnName)));
						if (sOrder != null && !sOrder.isEmpty()) {
							sbMQL.append(" order ").append(UIUtil.padWithSpaces(sOrder));
						}
						if (strTBColumnLabel != null) {
							sbMQL.append(" label").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(strTBColumnLabel)));
						}
						if (!strTBColumnType.isEmpty() && !strTBColumnExpression.isEmpty()) {
							sbMQL.append(UIUtil.padWithSpaces(strTBColumnType)).append(UIUtil.padWithSpaces(UIUtil.singleQuotes(strTBColumnExpression)));
						}
						if (strTBColumnHref != null) {
							sbMQL.append(" href").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(strTBColumnHref)));
						}
						if (strTBColumnRange != null) {
							sbMQL.append(" range").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(strTBColumnRange)));
						}
						if (strTBColumnUpdate != null) {
							sbMQL.append(" update")
									.append(UIUtil.padWithSpaces(UIUtil.singleQuotes(strTBColumnUpdate)));
						}
						if (strTBColumnAlt != null) {
							sbMQL.append(" alt").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(strTBColumnAlt)));
						}
						if (strTBColumnSortType != null && !strTBColumnSortType.isEmpty()) {
							sbMQL.append(" sorttype").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(strTBColumnSortType)));
						}
						List<Map<String, String>> lstSettings = tbColumn.getLstSetting();
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
						List<Map<String, String>> sUser = tbColumn.getLstAccessDetail();
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
						if (strTBColumnDescription != null) {
							sbMQL.append(" description ").append('"').append(strTBColumnDescription).append('"');
						}
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}
}
