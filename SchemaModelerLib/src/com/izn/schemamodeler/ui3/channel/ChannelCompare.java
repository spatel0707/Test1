package com.izn.schemamodeler.ui3.channel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.transform.stream.StreamSource;

import com.izn.schemamodeler.SchemaCompare;
import com.izn.schemamodeler.ui3.channel.Channel;
import com.izn.schemamodeler.ui3.channel.ChannelXMLSchema;
import com.izn.schemamodeler.ui3.channel.ChannelXMLSchema.Schema;
import com.izn.schemamodeler.ui3.channel.ChannelXMLSchema.Schema.Basic;
import com.izn.schemamodeler.ui3.channel.ChannelXMLSchema.Schema.Field;
import com.izn.schemamodeler.ui3.channel.ChannelXMLSchema.Schema.Field.Detail;
import com.izn.schemamodeler.ui3.channel.ChannelXMLSchema.Schema.Field.Setting;
import com.izn.schemamodeler.ui3.channel.ChannelXMLSchema.Schema.Field.Setting.Param;
import com.izn.schemamodeler.ui3.menu.MenuXMLSchema.Schema.Field.Items;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.SchemaModelerConstants;
import com.izn.schemamodeler.util.UIUtil;

import matrix.db.Context;

public class ChannelCompare implements SchemaCompare {

	Basic _basicElem = null;
	Field _fieldElem = null;
	Setting _setting = null;
	Items _items = null;
	String version = "";
	String compareResultFolder = "";
	String sAdminType = "";
	
	
	ChannelXMLSchema.Schema _schema = null;
	ChannelXMLSchema.Schema.Field.Detail _detail = null;
	ChannelXMLSchema.Schema.Field.Setting.Param _param = null;

	public ChannelCompare() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void compareSchema(Context context, String strAdminName, Logger schema_done_log,SCMConfigProperty scmConfigProperty)  throws Exception{
		try {
			sAdminType = strAdminName;
			version = scmConfigProperty.getVersion();
			compareResultFolder = scmConfigProperty.getCompareFolder() + "\\" + SchemaModelerConstants.FOLDER_NAME_SchemaObject;
			Map<String,Object> list1 = readFromXML(context, scmConfigProperty.getFirstFilePath());
			Map<String,Object> list2 = readFromXML(context, scmConfigProperty.getSecondFilePath());
			Map<String, List> compareSchema_1 = processSchemaCompare(context,list1,list2);
			Map<String, List> compareSchema_2 = processSchemaCompare(context,list2,list1);
		    List<Object> matchList = compareSchema_1.get(SchemaModelerConstants.MATCH);
		    List<Object> deltaList_1 = compareSchema_1.get(SchemaModelerConstants.DELTA);
		    List<Object> deltaList_2 = compareSchema_2.get(SchemaModelerConstants.DELTA);
		    List<Object> uniqueList_1 = compareSchema_1.get(SchemaModelerConstants.UNIQUE);
			List<Object> uniqueList_2 = compareSchema_2.get(SchemaModelerConstants.UNIQUE);
		    
			schema_done_log.info(sAdminType +"|"+SchemaModelerConstants.MATCH_COUNT+matchList.size()+","+SchemaModelerConstants.DELTA_COUNT1+deltaList_1.size()+","+SchemaModelerConstants.DELTA_COUNT2+deltaList_2.size()+","+SchemaModelerConstants.UNIQUE_COUNT1+uniqueList_1.size()+","+SchemaModelerConstants.UNIQUE_COUNT2+uniqueList_2.size());
			
			writeToXML(context, SchemaModelerConstants.COMPARE_FILENAME+strAdminName+SchemaModelerConstants.COMPARE_MATCH, matchList);
			writeToXML(context, SchemaModelerConstants.COMPARE_FILENAME+strAdminName+SchemaModelerConstants.COMPARE_DELTA1, deltaList_1);
			writeToXML(context, SchemaModelerConstants.COMPARE_FILENAME+strAdminName+SchemaModelerConstants.COMPARE_DELTA2, deltaList_2);
			writeToXML(context, SchemaModelerConstants.COMPARE_FILENAME+strAdminName+SchemaModelerConstants.COMPARE_UNIQUE1, uniqueList_1);
			writeToXML(context, SchemaModelerConstants.COMPARE_FILENAME+strAdminName+SchemaModelerConstants.COMPARE_UNIQUE2, uniqueList_2);
			
		} catch (UnmarshalException ume) {
			schema_done_log.error("UnmarshalException on ["+strAdminName+".xml] : "+ume.getCause());
			schema_done_log.warn(UIUtil.sUnmarshalExceptionWarningMessage);
			throw ume;
		} catch (Exception e) {
			throw e;
		}
	}
	private Map<String,List> processSchemaCompare(Context context, Map<String,Object> list1, Map<String,Object> list2)  throws Exception{
		Map<String,List> mCompareResultInfo = new HashMap<String,List>();
		try {
			List<Object> matchList = new ArrayList<Object>();
			List<Object> deltaList = new ArrayList<Object>();
			List<Object> uniqueList = new ArrayList<Object>();
			String sSchemaName = "";
			Channel obj1 = null;
			Channel obj2 = null;
			List<Object> listSchemaList1 = null;
			List<Object> listSchemaList2 = null;
			for (Map.Entry<String,Object> entry : list1.entrySet())  
			{
				sSchemaName = entry.getKey();
				listSchemaList1 = (ArrayList)entry.getValue();
				obj1 = (Channel)listSchemaList1.get(0);
				if(list2.containsKey(sSchemaName))
				{
					listSchemaList2 = (ArrayList)list2.get(sSchemaName);
					obj2 = (Channel)listSchemaList2.get(0);
					if(UIUtil.isEqual(obj1.name,obj2.name) && UIUtil.isEqual(obj1.description,obj2.description) && UIUtil.isEqual(obj1.hidden,obj2.hidden) && UIUtil.isEqual(obj1.registryname,obj2.registryname) && UIUtil.isEqual(obj1.getHref(),obj2.getHref())&& UIUtil.isEqual(obj1.getAlt(),obj2.getAlt())&& UIUtil.isEqual(obj1.getLabel(),obj2.getLabel()) && UIUtil.isEqualStringList(obj2.getLstCommand(), obj1.getLstCommand()) && UIUtil.isEqualStringMap(obj2.getLstSetting(), obj1.getLstSetting()))
					{
							matchList.add(listSchemaList1);
					}
					else {
						deltaList.add(listSchemaList1);
					}
				} else {
					uniqueList.add(listSchemaList1);
				}
			}
			mCompareResultInfo.put(SchemaModelerConstants.MATCH, matchList);
			mCompareResultInfo.put(SchemaModelerConstants.DELTA, deltaList);
			mCompareResultInfo.put(SchemaModelerConstants.UNIQUE, uniqueList);
		}catch (Exception e) {
			throw e;
		}
		return mCompareResultInfo;
	}
	
	private Map<String,Object> readFromXML(Context context, String fileName)  throws Exception{
		List<Object> listSchema = null;
		Map<String,Object> mSchemaInfo = new HashMap<String,Object>();
		try {
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
			List<String> lstCommandItem = null;
			List<String> lstChannelItem = null;
			for (Schema _schema : lstSchema)
			{
				listSchema = new ArrayList<Object>();
				lstSetting = new ArrayList<Map<String, String>>();
				lstCommandItem = new ArrayList<String>();
				lstChannelItem = new ArrayList<String>();
				_basicElem = _schema.getBasic();
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
				listSchema.add(channel);
				listSchema.add(_schema);
				mSchemaInfo.put(channel.name, listSchema);
			}
		} catch (UnmarshalException ume) {
			throw ume;
		} catch (Exception e) {
			throw e;
		}
		return mSchemaInfo;
	}	
	
	private void writeToXML(Context context, String compareFileName, List<Object> list) throws Exception
	{
		ObjectFactory objectFactory = new ObjectFactory();
		try {
			ChannelXMLSchema txc = objectFactory.createComponent();
			txc.setName(sAdminType);
			txc.setType("admin");
			txc.setVersion(version);
			txc.schema = new ArrayList<ChannelXMLSchema.Schema>();
			File file = null;
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { ChannelXMLSchema.class });
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty("jaxb.formatted.output", Boolean.valueOf(true));
			int iSize = list.size();
			List<Object> listSchemaList = null;
			for (int iCount = 0; iCount < iSize; iCount++) 
			{
				listSchemaList = (ArrayList)list.get(iCount);		
				_schema = (ChannelXMLSchema.Schema)listSchemaList.get(1);
				txc.schema.add(_schema);
			}
			file = new File(compareResultFolder + "\\" + sAdminType);
			if(!(file.exists() && file.isDirectory())){
				file.mkdir();
			} 
			file = new File(file.getPath() + "\\" + compareFileName + SchemaModelerConstants.XML_EXTENSION);
			jaxbMarshaller.marshal(txc, file);	
		} catch (Exception e) {
			throw e;
		}
	}
}
