package com.izn.schemamodeler.ui3.table;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

import com.izn.schemamodeler.SchemaCompare;
import com.izn.schemamodeler.SchemaFactoryInfo;
import com.izn.schemamodeler.ui3.table.TableXMLSchema.Schema;
import com.izn.schemamodeler.ui3.table.TableXMLSchema.Schema.Basic;
import com.izn.schemamodeler.ui3.table.TableXMLSchema.Schema.Column;
import com.izn.schemamodeler.ui3.table.TableXMLSchema.Schema.Column.Data;
import com.izn.schemamodeler.ui3.table.TableXMLSchema.Schema.Column.Data.Accessdetail;
import com.izn.schemamodeler.ui3.table.TableXMLSchema.Schema.Column.Data.Accessdetail.Access;
import com.izn.schemamodeler.ui3.table.TableXMLSchema.Schema.Column.Data.Datadetail;
import com.izn.schemamodeler.ui3.table.TableXMLSchema.Schema.Column.Data.Setting;
import com.izn.schemamodeler.ui3.table.TableXMLSchema.Schema.Column.Data.Setting.Param;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.SchemaModelerConstants;
import com.izn.schemamodeler.util.UIUtil;

import matrix.db.Context;

public class TableCompare implements SchemaCompare {

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
	String sAdminType = "";
	//For write xml
	String version = "";
	String compareResultFolder = "";
	public TableCompare() {
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
			Table obj1 = null;
			Table obj2 = null;
			List<Object> listSchemaList1 = null;
			List<Object> listSchemaList2 = null;
			for (Map.Entry<String,Object> entry : list1.entrySet())  
			{
				sSchemaName = entry.getKey();
				listSchemaList1 = (ArrayList)entry.getValue();
				obj1 = (Table)listSchemaList1.get(0);
				if(list2.containsKey(sSchemaName))
				{
					listSchemaList2 = (ArrayList)list2.get(sSchemaName);
					obj2 = (Table)listSchemaList2.get(0);
					if(UIUtil.isEqual(obj1.name,obj2.name) && UIUtil.isEqual(obj1.description,obj2.description) && UIUtil.isEqual(obj1.hidden,obj2.hidden) && UIUtil.isEqual(obj1.registryname,obj2.registryname) && isEqual(obj1.getLstMapColumn(), obj2.getLstMapColumn())) {
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
	
	private Map<String,Object> readFromXML(Context context, String fileName) throws Exception{
		List<Object> listSchema = null;
		Map<String,Object> mSchemaInfo = new HashMap<String,Object>();
		Map<String,Table.Column> mColumnInfo = null;
		String sColumnName = "";
		try {
			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
			TableInfo tTable = (TableInfo) schemaFactory.getSchemaObject("table");
			JAXBContext jContext = JAXBContext.newInstance(TableXMLSchema.class);
			Unmarshaller unMarsheller = jContext.createUnmarshaller();
			JAXBElement<TableXMLSchema> tableSchema = unMarsheller.unmarshal(new StreamSource(new FileReader(fileName)),TableXMLSchema.class);
			TableXMLSchema _tableElem = tableSchema.getValue();
			List<Schema> lstSchema = _tableElem.getSchema();
			Iterator<Schema> itrSchema = lstSchema.iterator();
			Table table = null;
			String dName = "";
			String dValue = "";
			int iColumnCounter = 0;
			Map<String, String> mSettings = null;
			String sDescription = "";
			while (itrSchema.hasNext()) {
				listSchema = new ArrayList<Object>();
				mColumnInfo = new HashMap<String,Table.Column>();
				iColumnCounter = 0;
				_schemaElem = itrSchema.next();
				_basicElem = _schemaElem.getBasic();
				if (_basicElem.getDescription() != null) {
					sDescription = _basicElem.getDescription().trim();
				} else {
					sDescription = "";
				}
				table = new Table(_basicElem.getName(), sDescription, _basicElem.getHidden(),
						_basicElem.getRegistryName());
				table.hidden = _basicElem.getHidden();
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
							sColumnName = dValue;
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
					mColumnInfo.put(sColumnName, columnObj);
				}
				Arrays.sort(wbColumn);
				table.setLstColumn(lttTableColmn);
				table.setLstMapColumn(mColumnInfo);
				// Sorted one ??
				table.setWbColumn(wbColumn);
				listSchema.add(table);
				listSchema.add(_schemaElem);
				mSchemaInfo.put(table.name,listSchema);
			}
		} catch (UnmarshalException ume) {
			throw ume;
		} catch (Exception e) {
			throw e;
		}
		return mSchemaInfo;
	}
	
	private boolean isEqual(Map<String,Table.Column> list1, Map<String,Table.Column> list2)throws Exception{
		if(list1.size() != list2.size()){
			return false;
		}
		Table.Column obj1 = null;
		Table.Column obj2 = null;
		String sColumnName = "";
		for (Map.Entry<String,Table.Column> entry : list1.entrySet())  
		{
			sColumnName = entry.getKey();
			obj1 = (Table.Column)entry.getValue();
			if(list2.containsKey(sColumnName))
			{
				obj2 = (Table.Column)list2.get(sColumnName);
				if(!(UIUtil.isEqual(obj1.columnName,obj2.columnName) && UIUtil.isEqual(obj1.label,obj2.label) && UIUtil.isEqual(obj1.columnType,obj2.columnType) && UIUtil.isEqual(obj1.expression,obj2.expression) && UIUtil.isEqual(obj1.href,obj2.href) && UIUtil.isEqual(obj1.alt,obj2.alt) && UIUtil.isEqual(obj1.range,obj2.range) && UIUtil.isEqual(obj1.update,obj2.update) && UIUtil.isEqualStringMap(obj1.getLstSetting(), obj2.getLstSetting()) && UIUtil.isEqualStringMap(obj1.getLstAccessDetail(), obj2.getLstAccessDetail())&& UIUtil.isEqual(obj1.description,obj2.description) && UIUtil.isEqual(obj1.sortType,obj2.sortType) && UIUtil.isEqual(obj1.expression,obj2.expression) && UIUtil.isEqual(String.valueOf(obj1.order),String.valueOf(obj2.order)))) {					
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}
	
	private void writeToXML(Context context, String compareFileName, List<Object> list) throws Exception{
		TableXMLSchema.Schema _schema = null;
		ObjectFactory objectFactory = new ObjectFactory();
		try {
			TableXMLSchema pxs = objectFactory.createComponent();
			pxs.setName("table");
			pxs.setType("ui3");
			pxs.setVersion(version);
			pxs.schema = new ArrayList<TableXMLSchema.Schema>();
			int iCountSchema = 0;
			boolean bSchemaSeperate = false;
			boolean bLogEverything = false;
			File file = null;
			String filePath = "";
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { TableXMLSchema.class });
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty("jaxb.formatted.output", Boolean.valueOf(true));	
			int iSize = list.size();
			List<Object> listSchemaList = null;
			for (int iCount = 0; iCount < iSize; iCount++) 
			{	
				listSchemaList = (ArrayList)list.get(iCount);				
				_schema = (TableXMLSchema.Schema)listSchemaList.get(1);
				pxs.schema.add(_schema);
			}
			file = new File(compareResultFolder + "\\" + sAdminType);
			if(!(file.exists() && file.isDirectory())){
				file.mkdir();
			} 
			file = new File(file.getPath() + "\\" + compareFileName + SchemaModelerConstants.XML_EXTENSION);
			jaxbMarshaller.marshal(pxs, file);	
		} catch (Exception e) {
			throw e;
		}
	}
}
