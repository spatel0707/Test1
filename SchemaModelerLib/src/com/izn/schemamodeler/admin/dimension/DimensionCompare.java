package com.izn.schemamodeler.admin.dimension;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.transform.stream.StreamSource;

import com.izn.schemamodeler.SchemaFactoryInfo;
import com.izn.schemamodeler.SchemaInfo;
import com.izn.schemamodeler.SchemaCompare;
import com.izn.schemamodeler.admin.dimension.Dimension;
import com.izn.schemamodeler.admin.dimension.DimensionInfo;
import com.izn.schemamodeler.admin.dimension.DimensionXMLSchema;
import com.izn.schemamodeler.admin.dimension.DimensionXMLSchema.Schema;
import com.izn.schemamodeler.admin.dimension.DimensionXMLSchema.Schema.Basic;
import com.izn.schemamodeler.admin.dimension.DimensionXMLSchema.Schema.Field;
import com.izn.schemamodeler.admin.dimension.DimensionXMLSchema.Schema.Field.Data;
import com.izn.schemamodeler.admin.dimension.DimensionXMLSchema.Schema.Field.Data.Datadetail;
import com.izn.schemamodeler.admin.dimension.DimensionXMLSchema.Schema.Field.Data.Setting;
import com.izn.schemamodeler.admin.dimension.DimensionXMLSchema.Schema.Field.Data.Setting.Param;
import com.izn.schemamodeler.admin.type.Type;
import com.izn.schemamodeler.admin.type.TypeInfo;
import com.izn.schemamodeler.admin.type.TypeXMLSchema;
import com.izn.schemamodeler.admin.type.TypeXMLSchema.Schema.Trigger;
import com.izn.schemamodeler.admin.type.TypeXMLSchema.Schema.Field.Detail;
import com.izn.schemamodeler.admin.type.TypeXMLSchema.Schema.Trigger.Event;
import com.izn.schemamodeler.admin.type.TypeXMLSchema.Schema.Trigger.Event.Eventdetail;
import com.izn.schemamodeler.ui3.webform.WebForm;
import com.izn.schemamodeler.admin.dimension.ObjectFactory;

import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.SchemaModelerConstants;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MapList;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class DimensionCompare implements SchemaCompare {
	String sAdminType = "";
	Schema _schemaElem = null;
	Basic _basicElem = null;
	Field _fieldElem = null;
	Detail _detailElem = null;
	Trigger _triggerElem = null;
	Event _eventElem = null;
	Eventdetail _eventdetailElem = null;

	//For write xml
	String version = "";
	String compareResultFolder = "";
	DimensionXMLSchema.Schema _schema = null;
	DimensionXMLSchema.Schema.Basic _basic = null;
	DimensionXMLSchema.Schema.Field _field = null;
	DimensionXMLSchema.Schema.Field.Data _data = null;
	DimensionXMLSchema.Schema.Field.Data.Datadetail _datadetail = null;
	DimensionXMLSchema.Schema.Field.Data.Setting _setting = null;
	DimensionXMLSchema.Schema.Field.Data.Setting.Param _param = null;

	public DimensionCompare() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void compareSchema(Context context, String strAdminName, Logger schema_done_log,	SCMConfigProperty scmConfigProperty)  throws Exception{
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
			Dimension obj1 = null;
			Dimension obj2 = null;
			List<Object> listSchemaList1 = null;
			List<Object> listSchemaList2 = null;
			for (Map.Entry<String,Object> entry : list1.entrySet())  
			{
				sSchemaName = entry.getKey();
				listSchemaList1 = (ArrayList)entry.getValue();
				obj1 = (Dimension)listSchemaList1.get(0);
				if(list2.containsKey(sSchemaName))
				{
					listSchemaList2 = (ArrayList)list2.get(sSchemaName);
					obj2 = (Dimension)listSchemaList2.get(0);
					if(UIUtil.isEqual(obj1.name,obj2.name) && UIUtil.isEqual(obj1.description,obj2.description) && UIUtil.isEqual(obj1.hidden,obj2.hidden) && UIUtil.isEqual(obj1.registryname,obj2.registryname) && isEqual(obj1.getLstMapUnit(), obj2.getLstMapUnit())) {
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
	
	private boolean isEqual(Map<String,Dimension.Data> list1, Map<String,Dimension.Data> list2) throws Exception
	{
		if(list1.size() != list2.size()){
			return false;
		}
		Dimension.Data obj1 = null;
		Dimension.Data obj2 = null;
		String sColumnName = "";
		for (Map.Entry<String,Dimension.Data> entry : list1.entrySet())  
		{
			sColumnName = entry.getKey();
			obj1 = (Dimension.Data)entry.getValue();
			if(list2.containsKey(sColumnName))
			{
				obj2 = (Dimension.Data)list2.get(sColumnName);
				if(!(UIUtil.isEqual(obj1.getDbunit(),obj2.getDbunit()) && UIUtil.isEqual(obj1.getUnit(),obj2.getUnit()) && UIUtil.isEqual(obj1.getLabel(),obj2.getLabel()) && UIUtil.isEqual(obj1.getUnitdescription(),obj2.getUnitdescription()) && UIUtil.isEqual(obj1.getDefault(),obj2.getDefault()) && UIUtil.isEqual(obj1.getMultiplier(),obj2.getMultiplier()) && UIUtil.isEqual(obj1.getOffset(),obj2.getOffset()) && UIUtil.isEqualStringMap(obj1.getLstSetting(), obj1.getLstSetting()))) {					
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}
	
	private Map<String,Object> readFromXML(Context context, String fileName)  throws Exception{
		List<Object> listSchema = null;
		Map<String,Object> mSchemaInfo = new HashMap<String,Object>();
		Map<String,Dimension.Data> mUnitInfo = null;
		try {
			JAXBContext jConext = JAXBContext.newInstance(DimensionXMLSchema.class);
			Unmarshaller unmarshaller = jConext.createUnmarshaller();
			JAXBElement<DimensionXMLSchema> dimensionElem = unmarshaller.unmarshal(new StreamSource(new File(fileName)),
					DimensionXMLSchema.class);
			DimensionXMLSchema dimensionXMLSchema = dimensionElem.getValue();
			List<Schema> lstSchema = dimensionXMLSchema.getSchema();
			Dimension dimension = null;
			String fValue = "";
			String fName = "";
			Map<String, String> mSettings = null;
			List<Map<String, String>> lstSetting = null;
			List<String> lstDimensionItem = null;
			HashMap map = new HashMap();
			String sUnitName = "";
			for (Schema _schema : lstSchema) {
				mUnitInfo = new HashMap<String,Dimension.Data>();
				listSchema = new ArrayList<Object>();
				MapList mapList = new MapList();
				List<Dimension.Data> lstDData = new ArrayList<Dimension.Data>();
				lstDimensionItem = new ArrayList<String>();
				_basicElem = _schema.getBasic();
				dimension = new Dimension(_basicElem.getName(), _basicElem.getHidden(),_basicElem.getDescription(),	_basicElem.getRegistryName());
				dimension.hidden = this._basicElem.getHidden();
				_fieldElem = _schema.getField();
				List<Data> lstData = _fieldElem.getData();
				HashMap<String, String> unitValues;
				for (Data _data : lstData) 
				{
					List<Datadetail> lstDataDetail = _data.getDatadetail();
					Dimension.Data dData = dimension.creatNewDataInstance();
					unitValues = new HashMap<String, String>();
					for (Datadetail datadetail : lstDataDetail) 
					{
						lstSetting = new ArrayList<Map<String, String>>();
						fName = datadetail.getName();
						fValue = datadetail.getValueAttribute();
						unitValues.put(fName, fValue);
						if (fValue != null) {
							fValue = datadetail.getValueAttribute().trim();
						} else {
							fValue = "";
						}
						if (fName.equalsIgnoreCase("dbunit")) {
							dData.setDbunit(fValue);
							sUnitName = fValue; 
						} else if (fName.equalsIgnoreCase("unit")) {
							dData.setUnit(fValue);
						} else if (fName.equalsIgnoreCase("label")) {
							dData.setLabel(fValue);
						} else if (fName.equalsIgnoreCase("unitdescription")) {
							dData.setUnitdescription(fValue);
						} else if (fName.equalsIgnoreCase("default")) {
							dData.setDefault(fValue);
						} else if (fName.equalsIgnoreCase("multiplier")) {
							dData.setMultiplier(fValue);
						} else if (fName.equalsIgnoreCase("offset")) {
							dData.setOffset(fValue);
						}
					}
					// process settings if any
					_setting = _data.getSetting();
					if (_setting != null) {
						List<Param> lstParam = _setting.getParam();
						for (Param param : lstParam) {
							Map m = new HashMap();
							m.put("name", param.getName());
							m.put("value", param.getValueAttribute());
							lstSetting.add(m);
						}
						unitValues.put("setting", lstSetting.toString());
						mapList.add(unitValues);
						dData.setLstSetting(lstSetting);
					}
					lstDData.add(dData);
					mUnitInfo.put(sUnitName, dData);
				}
				dimension.setLstMapUnit(mUnitInfo);
				map.put("units", mapList);
				dimension.setLstData(lstDData);
				map.clear(); 
				listSchema.add(dimension);
				listSchema.add(_schema);
				mSchemaInfo.put(dimension.name,listSchema);
			}
		} catch (UnmarshalException ume) {
			throw ume;
		} catch (Exception e) {
			throw e;
		}
		return mSchemaInfo;
	}	
	
	private void writeToXML(Context context, String compareFileName, List<Object> list) throws Exception{
		ObjectFactory objectFactory = new ObjectFactory();
		File file = null;
		try { 
			DimensionXMLSchema dxs = objectFactory.createComponent();
			dxs.setName("dimension");
			dxs.setType("admin");
			dxs.setVersion(version);
			dxs.schema = new ArrayList<DimensionXMLSchema.Schema>();
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { DimensionXMLSchema.class });
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty("jaxb.formatted.output", Boolean.valueOf(true));
			int iSize = list.size();
			List<Object> listSchemaList = null;
			for (int iCount = 0; iCount < iSize; iCount++) 
			{	
				listSchemaList = (ArrayList)list.get(iCount);				
				_schema = (DimensionXMLSchema.Schema)listSchemaList.get(1);
				dxs.schema.add(_schema);
			}
			file = new File(compareResultFolder + "\\" + sAdminType);
			if(!(file.exists() && file.isDirectory())){
				file.mkdir();
			} 
			file = new File(file.getPath() + "\\" + compareFileName + SchemaModelerConstants.XML_EXTENSION);
			jaxbMarshaller.marshal(dxs, file);	
		} catch (Exception e) {
			throw e;
		}
	}
}
