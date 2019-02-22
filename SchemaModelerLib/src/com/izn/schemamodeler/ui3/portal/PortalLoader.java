package com.izn.schemamodeler.ui3.portal;

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
import com.izn.schemamodeler.ui3.portal.Portal;
import com.izn.schemamodeler.ui3.portal.PortalInfo;
import com.izn.schemamodeler.ui3.portal.PortalXMLSchema;
import com.izn.schemamodeler.ui3.portal.PortalXMLSchema.Schema;
import com.izn.schemamodeler.ui3.portal.PortalXMLSchema.Schema.Basic;
import com.izn.schemamodeler.ui3.portal.PortalXMLSchema.Schema.Field;
import com.izn.schemamodeler.ui3.portal.PortalXMLSchema.Schema.Field.Detail;
import com.izn.schemamodeler.ui3.portal.PortalXMLSchema.Schema.Field.Items;
import com.izn.schemamodeler.ui3.portal.PortalXMLSchema.Schema.Field.Items.Item;
import com.izn.schemamodeler.ui3.portal.PortalXMLSchema.Schema.Field.Setting;
import com.izn.schemamodeler.ui3.portal.PortalXMLSchema.Schema.Field.Setting.Param;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;
import matrix.util.StringList;

public class PortalLoader implements SchemaLoader {
	ObjectMapper _gson = new ObjectMapper();
	Basic _basicElem = null;
	Field _fieldElem = null;
	Setting _setting = null;
	Items _items = null;

	@Override
	public void loadSchema(Context context, String strSchemaName, String fileName, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty)  throws Exception{

		try {
			List<Portal> lstPortal = new ArrayList<Portal>();
			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
			PortalInfo PortalInfo = (PortalInfo) schemaFactory.getSchemaObject("Portal");
			JAXBContext jConext = JAXBContext.newInstance(PortalXMLSchema.class);
			Unmarshaller unmarshaller = jConext.createUnmarshaller();
			JAXBElement<PortalXMLSchema> portalElem = unmarshaller.unmarshal(new StreamSource(new File(fileName)),
					PortalXMLSchema.class);
			PortalXMLSchema portalXMLSchema = portalElem.getValue();
			List<Schema> lstSchema = portalXMLSchema.getSchema();
			Portal portal = null;
			String fValue = "";
			String fName = "";
			Map<String, String> mSettings = null;
			List<Map<String, String>> lstSetting = null;
			List<String> lstChannelItem = null;
			for (Schema _schema : lstSchema) {
				lstSetting = new ArrayList<Map<String, String>>();
				lstChannelItem = new ArrayList<String>();
				_basicElem = _schema.getBasic();
				portal = new Portal(_basicElem.getName(), _basicElem.getDescription(), _basicElem.getHidden(),
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
						portal.setHref(fValue);
					} else if (fName.equalsIgnoreCase("alt")) {
						portal.setAlt(fValue);
					} else if (fName.equalsIgnoreCase("label")) {
						portal.setLabel(fValue);
					}

				}
				_items = _fieldElem.getItems();
				List<Item> lstItem = _items.getItem();
				for (Item item : lstItem) {
					String[] strArray = item.getValue().split(",");
					lstChannelItem.add(FrameworkUtil.join(strArray, "|"));
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
				portal.setLstChannel(lstChannelItem);

				portal.setLstSetting(lstSetting);
				lstPortal.add(portal);
			}
			preparePortalMQL(context, lstPortal, schema_done_log, scmConfigProperty);
		} catch (UnmarshalException ume) {
			schema_done_log.error("UnmarshalException on ["+strSchemaName+".xml] : "+ume.getCause());
			schema_done_log.warn(UIUtil.sUnmarshalExceptionWarningMessage);
			throw ume;
		} catch (Exception e) {
			schema_done_log.error(""+e.getCause());
			throw e;
		}
	}

	private void preparePortalMQL(Context context, List<Portal> lstPortal, Logger schema_done_log,
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
		String strPortalName = "";
		String sOperation = "";
		List<String> listModified = new ArrayList<String>();
		List<String> listAdded = new ArrayList<String>();
		List<String> listDeleted = new ArrayList<String>();
		try {
			iCountTotal  = lstPortal.size();
			for (Portal portal : lstPortal) {
				
				schema_done_log.info("***********************Importing portal : ["
						+ portal.name.replace(UIUtil.removeCharecter, "") + "]");
				strPortalName = portal.name;
				ContextUtil.pushContext(context);
				try {
					String removeSchemaObject = UIUtil.removeSchemaObject(strPortalName, "portal");
					if (removeSchemaObject.isEmpty()) {
						String strResult = MQLCommand.exec(context, "list portal $1",
								new String[] { strPortalName.replace(UIUtil.removeCharecter, "") });
						if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
							sMQL = prepareModifyExistingSchemaMQL(context,portal, schema_done_log);
							iCountModify += 1;
							listModified.add(strPortalName);
							sOperation = "mod";
						} else {
							sMQL = prepareAddNewSchemaMQL(portal, schema_done_log);
							iCountAdd += 1;
							listAdded.add(strPortalName);
							sOperation = "add";
						}
						sMQLPropertyQuery = UIUtil.getMQLPropertyQuery(context, "portal", portal.name,
								portal.registryname, sOperation);
					} else {
						sMQL = removeSchemaObject;
						iCountDelete += 1;
						listDeleted.add(strPortalName);
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
					throw new Exception("Error occurred while importing schema portal : ["
							+ strPortalName.replace(UIUtil.removeCharecter, "") + "] : " + e.getMessage());
				}
				schema_done_log.info("-----------------------------------------------------------------");
			}
			schema_done_log
					.info("TOTAL  :" + iCountTotal + ", SUCCESS :" + iCountSuccess 
							+ ", ADDED  :" + iCountAdd + ", MODIFIED :" + iCountModify + ", DELETED :" + iCountDelete);
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
			Portal objInfo = (Portal) objectSchemaInfo;
			sbMQL.append(" add portal ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
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
			List<String> lstPortalItems = objInfo.getLstChannel();
			if (lstPortalItems != null && !lstPortalItems.isEmpty()) {
				for (String portalItem : lstPortalItems) {
					String[] sArrChannel = portalItem.split(",");
					for (String sChannel : sArrChannel) {
						StringList splitString = FrameworkUtil.splitString(sChannel, "|");
						if (splitString.size() > 1) {
							sChannel = (String) FrameworkUtil.join(splitString, ",");
						}
						sbMQL.append(" channel ").append(UIUtil.padWithSpaces(sChannel));
					}
				}
			}
			List<Map<String, String>> lstSettings = objInfo.getLstSetting();
			if (lstSettings != null && !lstSettings.isEmpty()) {
				for (Map<String, String> m : lstSettings) {
					sbMQL.append(" setting ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(m.get("name"))))
							.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(m.get("value"))));
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}

	private String prepareModifyExistingSchemaMQL(Context context,Object objectSchemaInfo, Logger schema_done_log) throws Exception {
		StringBuilder sbMQL = new StringBuilder();
		StringBuilder sbMQLChannel = new StringBuilder();
		try {
			Portal objInfo = (Portal) objectSchemaInfo;
			sbMQL.append(" mod portal ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			sbMQLChannel.append(" mod portal ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			
			if (objInfo.description != null && !objInfo.description.isEmpty()) {
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
			List<Map<String, String>> lstSettings = objInfo.getLstSetting();
			if (lstSettings != null && !lstSettings.isEmpty()) {
				for (Map<String, String> m : lstSettings) {
					sbMQL.append(UIUtil.removeSettingValue(m.get("name"), m.get("value"), "setting"));
				}
			}
			List<String> lstPortalItems = objInfo.getLstChannel();
			if (lstPortalItems != null && !lstPortalItems.isEmpty()) {
				//Get existing channels and remove
				String strResult = MQLCommand.exec(context, "print portal $1 select channel dump $2",new String[] { objInfo.name, "|" });
				if(strResult != null && !strResult.isEmpty()){					
					StringList slExistingChannels = FrameworkUtil.splitString(strResult, "|");
					sbMQL.append(" remove ").append("channel ");
					int iSize = slExistingChannels.size();
					for (int iCount=0; iCount<iSize; iCount++) {
						sbMQL.append(UIUtil.singleQuoteWithSpace(slExistingChannels.get(iCount)));
					}
				}
				for (String portalItem : lstPortalItems) {
					String[] sArrChannel = portalItem.split(",");
					for (String sChannel : sArrChannel) {
						if(!sChannel.isEmpty())
						{							
							StringList slChannels = FrameworkUtil.splitString(sChannel, "|");
							String previousChannel = "";
							for (Object object : slChannels) {
								String sChannelName = (String)object;
								if(!sChannelName.startsWith(UIUtil.removeCharecter) && !sChannelName.endsWith(UIUtil.removeCharecter)){								
									sbMQLChannel.append(" place ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(object.toString())));
									if (previousChannel.isEmpty()) {
										sbMQLChannel.append(" newrow after ''");
									} else {
										sbMQLChannel.append(" after ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(previousChannel)));
									}
									previousChannel = object.toString();
								}
							}
						}
					}
				}
			}else{
				sbMQLChannel.setLength(0);				
			}
			sbMQL.append(";");
			sbMQL.append(sbMQLChannel);			
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}
}
