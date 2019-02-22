package com.izn.schemamodeler.ui3.channel;

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
import com.izn.schemamodeler.SchemaFactoryInfo;
import com.izn.schemamodeler.SchemaLoader;
import com.izn.schemamodeler.ui3.channel.Channel;
import com.izn.schemamodeler.ui3.channel.ChannelInfo;
import com.izn.schemamodeler.ui3.channel.ChannelXMLSchema;
import com.izn.schemamodeler.ui3.channel.ChannelXMLSchema.Schema;
import com.izn.schemamodeler.ui3.channel.ChannelXMLSchema.Schema.Basic;
import com.izn.schemamodeler.ui3.channel.ChannelXMLSchema.Schema.Field;
import com.izn.schemamodeler.ui3.channel.ChannelXMLSchema.Schema.Field.Detail;
import com.izn.schemamodeler.ui3.channel.ChannelXMLSchema.Schema.Field.Setting;
import com.izn.schemamodeler.ui3.channel.ChannelXMLSchema.Schema.Field.Setting.Param;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class ChannelLoader implements SchemaLoader {
	ObjectMapper _gson = new ObjectMapper();
	Basic _basicElem = null;
	Field _fieldElem = null;
	Setting _setting = null;

	public ChannelLoader() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void loadSchema(Context context, String strSchemaName, String fileName, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty)  throws Exception{
		try {
			List<Channel> lstChannel = new ArrayList<Channel>();
			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
			ChannelInfo channelInfo = (ChannelInfo) schemaFactory.getSchemaObject("channel");

			JAXBContext jConext = JAXBContext.newInstance(ChannelXMLSchema.class);
			Unmarshaller unmarshaller = jConext.createUnmarshaller();
			JAXBElement<ChannelXMLSchema> channelElem = unmarshaller.unmarshal(new StreamSource(new File(fileName)),
					ChannelXMLSchema.class);
			ChannelXMLSchema channelXMLSchema = channelElem.getValue();
			List<Schema> lstSchema = channelXMLSchema.getSchema();
			Channel channel = null;
			String fValue = "";
			String fName = "";
			Map<String, String> mSettings = null;
			List<Map<String, String>> lstSetting = null;
			List<String> lstChannelItem = null;
			List<String> lstCommandItem = null;
			for (Schema _schema : lstSchema) {
				lstSetting = new ArrayList<Map<String, String>>();
				lstCommandItem = new ArrayList<String>();
				lstChannelItem = new ArrayList<String>();
				_basicElem = _schema.getBasic();

				// this condition is for channel name validation :on OCT 9th
				/*
				 * if((_basicElem.getName() != null) &&
				 * !("".equalsIgnoreCase(_basicElem.getName().trim()))) {
				 */
				channel = new Channel(_basicElem.getName().trim(), _basicElem.getDescription(), _basicElem.getHidden(),
						_basicElem.getRegistryName());
				_fieldElem = _schema.getField();
				if (_fieldElem.getDetail() != null) {
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
							channel.setHref(fValue);
						} else if (fName.equalsIgnoreCase("alt")) {
							channel.setAlt(fValue);
						} else if (fName.equalsIgnoreCase("height")) {
							channel.setHeight(fValue);
						} else if (fName.equalsIgnoreCase("label")) {
							channel.setLabel(fValue);
						} else if (fName.equalsIgnoreCase("command")) {
							String[] sArr = fValue.split(",");
							for (String sCommand : sArr) {
								lstCommandItem.add(sCommand);
							}

						}

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
				channel.setLstCommand(lstCommandItem);
				channel.setLstSetting(lstSetting);
				lstChannel.add(channel);
			}
			// this condition is for channel name validation :on OCT 9th
			/* } */
			/* if(!(lstChannel != null) && (lstChannel.isEmpty())) */
			prepareChannelMQL(context, lstChannel, schema_done_log, scmConfigProperty);
		} catch (UnmarshalException ume) {
			schema_done_log.error("UnmarshalException on ["+strSchemaName+".xml] : "+ume.getCause());
			schema_done_log.warn(UIUtil.sUnmarshalExceptionWarningMessage);
			throw ume;
		} catch (Exception e) {
			schema_done_log.error(""+e.getCause());
			throw e;
		}
	}

	private void prepareChannelMQL(Context context, List<Channel> lstChannel, Logger schema_done_log,
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
		String strChannelName = "";
		String sOperation = "";
		List<String> listModified = new ArrayList<String>();
		List<String> listAdded = new ArrayList<String>();
		List<String> listDeleted = new ArrayList<String>();
		try {
			iCountTotal = lstChannel.size();
			for (Channel channel : lstChannel) {
				
				schema_done_log.info("Channel : ["
						+ channel.name.replace(UIUtil.removeCharecter, "") + "]");
				try {
					ContextUtil.pushContext(context);
					strChannelName = channel.name;
					String removeSchemaObject = UIUtil.removeSchemaObject(strChannelName, "channel");
					if (removeSchemaObject.isEmpty()) {
						String strResult = MQLCommand.exec(context, "list channel $1",
								new String[] { strChannelName.replace(UIUtil.removeCharecter, "") });
						if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
							sMQL = prepareModifyExistingSchemaMQL(channel, schema_done_log);
							iCountModify += 1;
							listModified.add(strChannelName);
							sOperation = "mod";
						} else {
							sMQL = prepareAddNewSchemaMQL(channel, schema_done_log);
							iCountAdd += 1;
							listAdded.add(strChannelName);
							sOperation = "add";
						}
						sMQLPropertyQuery = UIUtil.getMQLPropertyQuery(context, "channel", channel.name,
								channel.registryname, sOperation);
					} else {
						sMQL = removeSchemaObject;
						iCountDelete += 1;
						listDeleted.add(strChannelName);
					}
					ContextUtil.pushContext(context);
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
					schema_done_log.info("TOTAL :" + iCountTotal + ", SUCCESS :" + iCountSuccess +  ", ADDED :" + iCountAdd + ", MODIFIED :" + iCountModify + ", DELETED :"	+ iCountDelete + ".");
					throw new Exception("Error occurred while importing schema channel : ["
							+ strChannelName.replace(UIUtil.removeCharecter, "") + "] : " + e.getMessage());
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
			Channel objInfo = (Channel) objectSchemaInfo;
			sbMQL.append(" add channel ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null && !objInfo.description.isEmpty()) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.getHeight() != null && !objInfo.getHeight().isEmpty()) {
				sbMQL.append(" height ").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(objInfo.getHeight())));
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
			List<String> listCommand = objInfo.getLstCommand();
			if (listCommand != null && !listCommand.isEmpty()) {
				for (String commandItem : listCommand) {
					sbMQL.append(" command ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(commandItem)));
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

	private String prepareModifyExistingSchemaMQL(Object objectSchemaInfo, Logger schema_done_log) throws Exception {
		StringBuilder sbMQL = new StringBuilder();
		try {
			Channel objInfo = (Channel) objectSchemaInfo;
			sbMQL.append(" mod channel ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.getHeight() != null && !objInfo.getHeight().isEmpty()) {
				sbMQL.append(" height ").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(objInfo.getHeight())));
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
			List<String> listCommand = objInfo.getLstCommand();
			if (listCommand != null && !listCommand.isEmpty()) {
				String sPreviousCommand = "";
				for (String commandItem : listCommand) {
					if (commandItem.startsWith(UIUtil.removeCharecter)
							&& commandItem.endsWith(UIUtil.removeCharecter)) {
						String param1 = commandItem.replace(UIUtil.removeCharecter, "");
						sbMQL.append(" remove command ").append(UIUtil.singleQuoteWithSpace(param1));
					} else if (!commandItem.startsWith(UIUtil.removeCharecter)
							&& !commandItem.endsWith(UIUtil.removeCharecter)) {
						if ((commandItem != null && !commandItem.isEmpty())) {
							sbMQL.append(" place ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(commandItem)));
							sbMQL.append(" after '").append(sPreviousCommand).append("'");
						}
						sPreviousCommand = commandItem;
					}
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
