package com.izn.schemamodeler.ui3.menu;

import java.io.File;
import java.util.ArrayList;
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
import com.izn.schemamodeler.SchemaLoader;
import com.izn.schemamodeler.ui3.menu.Menu;
import com.izn.schemamodeler.ui3.menu.MenuXMLSchema;
import com.izn.schemamodeler.ui3.menu.MenuXMLSchema.Schema;
import com.izn.schemamodeler.ui3.menu.MenuXMLSchema.Schema.Basic;
import com.izn.schemamodeler.ui3.menu.MenuXMLSchema.Schema.Field;
import com.izn.schemamodeler.ui3.menu.MenuXMLSchema.Schema.Field.Detail;
import com.izn.schemamodeler.ui3.menu.MenuXMLSchema.Schema.Field.Items;
import com.izn.schemamodeler.ui3.menu.MenuXMLSchema.Schema.Field.Items.Item;
import com.izn.schemamodeler.ui3.menu.MenuXMLSchema.Schema.Field.Setting;
import com.izn.schemamodeler.ui3.menu.MenuXMLSchema.Schema.Field.Setting.Param;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class MenuLoader implements SchemaLoader {
	ObjectMapper _gson = new ObjectMapper();
	Basic _basicElem = null;
	Field _fieldElem = null;
	Setting _setting = null;
	Items _items = null;

	public MenuLoader() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void loadSchema(Context context, String strSchemaName, String fileName, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty)  throws Exception{
		try {
			List<Menu> lstMenu = new ArrayList<Menu>();
			JAXBContext jConext = JAXBContext.newInstance(MenuXMLSchema.class);
			Unmarshaller unmarshaller = jConext.createUnmarshaller();
			JAXBElement<MenuXMLSchema> menuElem = unmarshaller.unmarshal(new StreamSource(new File(fileName)),
					MenuXMLSchema.class);
			MenuXMLSchema menuXMLSchema = menuElem.getValue();
			List<Schema> lstSchema = menuXMLSchema.getSchema();
			Menu menu = null;
			String fValue = "";
			String fName = "";
			Map<String, String> mSettings = null;
			List<Map<String, String>> lstSetting = null;
			List<String> lstMenuItem = null;
			List<String> lstCommandItem = null;
			for (Schema _schema : lstSchema) {
				lstSetting = new ArrayList<Map<String, String>>();
				lstCommandItem = new ArrayList<String>();
				lstMenuItem = new ArrayList<String>();
				_basicElem = _schema.getBasic();
				menu = new Menu(_basicElem.getName(), _basicElem.getDescription(), _basicElem.getHidden(),
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
					if (fName.equalsIgnoreCase("href")) {
						menu.setHref(fValue);
					} else if (fName.equalsIgnoreCase("alt")) {
						menu.setAlt(fValue);
					} else if (fName.equalsIgnoreCase("label")) {
						menu.setLabel(fValue);
					}
				}
				_items = _fieldElem.getItems();
				List<Item> lstItem = _items.getItem();
				for (Item item : lstItem) {
					if (item.getType().equalsIgnoreCase("menu")) {
						lstMenuItem.add(item.getValue());
					} else {
						lstCommandItem.add(item.getValue());
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
				menu.setLstMenu(lstMenuItem);
				menu.setLstCommands(lstCommandItem);
				menu.setLstSetting(lstSetting);
				lstMenu.add(menu);
			}

			prepareMenuMQL(context, lstMenu, schema_done_log, scmConfigProperty);
		} catch (UnmarshalException ume) {
			schema_done_log.error("UnmarshalException on ["+strSchemaName+".xml] : "+ume.getCause());
			schema_done_log.warn(UIUtil.sUnmarshalExceptionWarningMessage);
			throw ume;
		} catch (Exception e) {
			schema_done_log.error(""+e.getCause());
			throw e;
		}
	}

	private void prepareMenuMQL(Context context, List<Menu> lstMenu, Logger schema_done_log,
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
		String strMenuName = "";
		String sOperation = "";
		List<String> listModified = new ArrayList<String>();
		List<String> listAdded = new ArrayList<String>();
		List<String> listDeleted = new ArrayList<String>();
		try {
			iCountTotal = lstMenu.size();
			for (Menu menu : lstMenu) {
				
				strMenuName = menu.name;
				schema_done_log.info("Menu : ["
						+ menu.name.replace(UIUtil.removeCharecter, "") + "]");
				try {
					ContextUtil.pushContext(context);
					String removeSchemaObject = UIUtil.removeSchemaObject(strMenuName, "menu");
					if (removeSchemaObject.isEmpty()) {
						String strResult = MQLCommand.exec(context, "list menu $1",
								new String[] { strMenuName.replace(UIUtil.removeCharecter, "") });
						if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
							sMQL = prepareModifyExistingSchemaMQL(menu, schema_done_log);
							iCountModify += 1;
							listModified.add(strMenuName);
							sOperation = "mod";
						} else {
							sMQL = prepareAddNewSchemaMQL(menu, schema_done_log);
							iCountAdd += 1;
							listAdded.add(strMenuName);
							sOperation = "add";
						}
						sMQLPropertyQuery = UIUtil.getMQLPropertyQuery(context, "menu", menu.name, menu.registryname,
								sOperation);
					} else {
						sMQL = removeSchemaObject;
						iCountDelete += 1;
						listDeleted.add(strMenuName);
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
					ContextUtil.popContext(context);schema_done_log.info("TOTAL :" + iCountTotal + ", SUCCESS :" + iCountSuccess + ", ADDED :" + iCountAdd + ", MODIFIED :" + iCountModify + ", DELETED :"	+ iCountDelete + ".");
					throw new Exception("Error occurred while importing schema menu : ["
							+ strMenuName.replace(UIUtil.removeCharecter, "") + "] : " + e.getMessage());
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
			Menu objInfo = (Menu) objectSchemaInfo;
			sbMQL.append(" add menu ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
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
			List<String> lstMenuCommandItems = objInfo.getLstMenu();
			if (lstMenuCommandItems != null && !lstMenuCommandItems.isEmpty()) {
				for (String menuItem : lstMenuCommandItems) {
					sbMQL.append(" menu ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(menuItem)));
				}
			}
			lstMenuCommandItems = objInfo.getLstCommands();
			if (lstMenuCommandItems != null && !lstMenuCommandItems.isEmpty()) {
				for (String commandItem : lstMenuCommandItems) {
					sbMQL.append(" command ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(commandItem)));
				}
			}
			List<Map<String, String>> lstSettings = objInfo.getLstSetting();
			if (lstSettings != null && !lstSettings.isEmpty()) {
				for (Map<String, String> m : lstSettings) {
					if (!m.get("value").isEmpty())
						sbMQL.append(" setting ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(m.get("name"))))
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
			Menu objInfo = (Menu) objectSchemaInfo;
			sbMQL.append(" mod menu ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
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
			List<String> lstMenuCommandItems = objInfo.getLstMenu();
			if (lstMenuCommandItems != null && !lstMenuCommandItems.isEmpty()) {
				for (String menuItem : lstMenuCommandItems) {
					sbMQL.append(UIUtil.removeFieldDetail(menuItem, "menu"));
				}
			}
			lstMenuCommandItems = objInfo.getLstCommands();
			if (lstMenuCommandItems != null && !lstMenuCommandItems.isEmpty()) {
				for (String commandItem : lstMenuCommandItems) {
					sbMQL.append(UIUtil.removeFieldDetail(commandItem, "command"));
				}
			}
			List<Map<String, String>> lstSettings = objInfo.getLstSetting();
			if (lstSettings != null && !lstSettings.isEmpty()) {
				for (Map<String, String> m : lstSettings) {
					sbMQL.append(UIUtil.removeSettingValue(m.get("name"), m.get("value"), "setting"));
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}
}
