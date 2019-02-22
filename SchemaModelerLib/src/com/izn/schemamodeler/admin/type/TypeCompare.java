package com.izn.schemamodeler.admin.type;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import com.izn.schemamodeler.SchemaCompare;
import com.izn.schemamodeler.admin.type.TypeXMLSchema.Schema;
import com.izn.schemamodeler.admin.type.TypeXMLSchema.Schema.Basic;
import com.izn.schemamodeler.admin.type.TypeXMLSchema.Schema.Field;
import com.izn.schemamodeler.admin.type.TypeXMLSchema.Schema.Field.Detail;
import com.izn.schemamodeler.admin.type.TypeXMLSchema.Schema.Trigger;
import com.izn.schemamodeler.admin.type.TypeXMLSchema.Schema.Trigger.Event;
import com.izn.schemamodeler.admin.type.TypeXMLSchema.Schema.Trigger.Event.Eventdetail;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.SchemaModelerConstants;
import com.izn.schemamodeler.util.UIUtil;
import matrix.db.Context;

public class TypeCompare implements SchemaCompare {
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
	TypeXMLSchema.Schema _schema = null;
	TypeXMLSchema.Schema.Basic _basic = null;
	TypeXMLSchema.Schema.Field _field = null;
	TypeXMLSchema.Schema.Field.Detail _detail = null;
	TypeXMLSchema.Schema.Trigger _trigger = null;
	TypeXMLSchema.Schema.Trigger.Event _event = null;
	TypeXMLSchema.Schema.Trigger.Event.Eventdetail _eventdetail = null;
	String[] fieldDetail = { "derived", "abstract", "sparse", "attribute", "method" };
	
	public TypeCompare() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void compareSchema(Context context, String strAdminName, Logger schema_done_log, SCMConfigProperty scmConfigProperty)  throws Exception{
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
			Type obj1 = null;
			Type obj2 = null;
			List<Object> listSchemaList1 = null;
			List<Object> listSchemaList2 = null;
			for (Map.Entry<String,Object> entry : list1.entrySet())  
			{
				sSchemaName = entry.getKey();
				listSchemaList1 = (ArrayList)entry.getValue();
				obj1 = (Type)listSchemaList1.get(0);
				if(list2.containsKey(sSchemaName))
				{
					listSchemaList2 = (ArrayList)list2.get(sSchemaName);
					obj2 = (Type)listSchemaList2.get(0);
					if(UIUtil.isEqual(obj1.name,obj2.name) && UIUtil.isEqual(obj1.description,obj2.description) && UIUtil.isEqual(obj1.hidden,obj2.hidden) && UIUtil.isEqual(obj1.registryname,obj2.registryname) && UIUtil.isEqual(obj1.getDerived(),obj2.getDerived()) && UIUtil.isEqual(obj1.getSabstract(),obj2.getSabstract()) && UIUtil.isEqual(obj1.getSparse(),obj2.getSparse()) && UIUtil.isEqual(obj1.getMethods(),obj2.getMethods()) && UIUtil.isEqual(obj1.getAttributes(),obj2.getAttributes()) && UIUtil.isEqual(obj1.getSlTriggers(),obj2.getSlTriggers()) ) {
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
			JAXBContext jContext = JAXBContext.newInstance(TypeXMLSchema.class);
			Unmarshaller unMarsheller = jContext.createUnmarshaller();
			JAXBElement<TypeXMLSchema> typeJaxbElem = unMarsheller.unmarshal((new StreamSource(new FileReader(fileName))), TypeXMLSchema.class);
			TypeXMLSchema typeElem = typeJaxbElem.getValue();
			List<Schema> lstSchema = typeElem.getSchema();
			Iterator<Schema> itrSchema = lstSchema.iterator();
			Type type = null;
			String strFName = "";
			String strFValue = "";
			List slTrigger = null;
			while (itrSchema.hasNext()) 
			{
				listSchema = new ArrayList<Object>();
				slTrigger = new ArrayList();
				_schemaElem = itrSchema.next();
				_basicElem = _schemaElem.getBasic();
				type = new Type(_basicElem.getName(), _basicElem.getDescription(), _basicElem.getHidden(),_basicElem.getRegistryName());
				type.hidden = _basicElem.getHidden();
				_fieldElem = _schemaElem.getField();
				List<Detail> lstDetail = _fieldElem.getDetail();
				Iterator<Detail> itrDetail = lstDetail.iterator();
				while (itrDetail.hasNext()) {
					_detailElem = itrDetail.next();
					strFName = _detailElem.getName();
					strFValue = _detailElem.getValueAttribute();
					if (strFValue != null) {
						strFValue = _detailElem.getValueAttribute().trim();
					} else {
						strFValue = "";
					}
					if (strFName.equalsIgnoreCase("derived")) {
						type.setDerived(strFValue);
					} else if (strFName.equalsIgnoreCase("abstract")) {
						type.setSabstract(strFValue);
					} else if (strFName.equalsIgnoreCase("sparse")) {
						type.setSparse(strFValue);
					} else if (strFName.equalsIgnoreCase("attribute")) {
						type.setAttributes(strFValue);
					} else if (strFName.equalsIgnoreCase("method")) {
						type.setMethods(strFValue);
					}
				}
				_triggerElem = _schemaElem.getTrigger();
				if (_triggerElem != null) {
					List<Event> lstEvent = _triggerElem.getEvent();
					Iterator<Event> itrEvent = lstEvent.iterator();
					Map<String, String> mTriggerDetails = null;
					String strEvent = "";
					while (itrEvent.hasNext()) {
						_eventElem = itrEvent.next();
						strEvent = _eventElem.getName();
						List<Eventdetail> lstEventdetails = _eventElem.getEventdetail();
						Iterator<Eventdetail> itrEventdetails = lstEventdetails.iterator();
						while (itrEventdetails.hasNext()) {
							_eventdetailElem = itrEventdetails.next();
							mTriggerDetails = new HashMap<String, String>();
							mTriggerDetails.put(ACTION, strEvent);
							mTriggerDetails.put(TYPE, _eventdetailElem.getType());
							mTriggerDetails.put(PROGRAM, _eventdetailElem.getProgram());
							mTriggerDetails.put(NAME, _eventdetailElem.getInput());
							slTrigger.add(mTriggerDetails);
						}
					}
					type.setSlTriggers(slTrigger);
					type.name = _basicElem.getName();
				}
				listSchema.add(type);
				listSchema.add(_schemaElem);
				mSchemaInfo.put(type.name,listSchema);
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
		try {
			TypeXMLSchema txc = objectFactory.createComponent();
			txc.setName(sAdminType);
			txc.setType("admin");
			txc.setVersion(version);
			txc.schema = new ArrayList<TypeXMLSchema.Schema>();
			File file = null;
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { TypeXMLSchema.class });
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty("jaxb.formatted.output", Boolean.valueOf(true));
			int iSize = list.size();
			List<Object> listSchemaList = null;
			for (int iCount = 0; iCount < iSize; iCount++) 
			{	
				listSchemaList = (ArrayList)list.get(iCount);				
				_schema = (TypeXMLSchema.Schema)listSchemaList.get(1);
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
