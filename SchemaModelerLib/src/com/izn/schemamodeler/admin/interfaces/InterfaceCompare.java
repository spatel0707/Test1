package com.izn.schemamodeler.admin.interfaces;

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
import com.izn.schemamodeler.admin.interfaces.Interfaces;
import com.izn.schemamodeler.admin.interfaces.InterfaceInfo;
import com.izn.schemamodeler.admin.interfaces.InterfaceXMLSchema;
import com.izn.schemamodeler.admin.interfaces.InterfaceXMLSchema.Schema;
import com.izn.schemamodeler.admin.interfaces.InterfaceXMLSchema.Schema.Basic;
import com.izn.schemamodeler.admin.interfaces.InterfaceXMLSchema.Schema.Field;
import com.izn.schemamodeler.admin.interfaces.InterfaceXMLSchema.Schema.Field.Detail;
import com.izn.schemamodeler.admin.type.Type;
import com.izn.schemamodeler.admin.type.TypeInfo;
import com.izn.schemamodeler.admin.type.TypeXMLSchema;
import com.izn.schemamodeler.admin.type.TypeXMLSchema.Schema.Trigger.Event;
import com.izn.schemamodeler.admin.type.TypeXMLSchema.Schema.Trigger.Event.Eventdetail;
import com.izn.schemamodeler.admin.interfaces.ObjectFactory;

import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.SchemaModelerConstants;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class InterfaceCompare implements SchemaCompare {

	Basic _basicElem = null;
	Field _fieldElem = null;
	String sAdminType = "";
	String version = "";
	String compareResultFolder = "";
	Schema _schemaElem = null;
	Detail _detailElem = null;


	String _sDBInfo = "";
	Map _hDBInfo = new HashMap();
	InterfaceXMLSchema.Schema _schema = null;
	InterfaceXMLSchema.Schema.Basic _basic = null;
	InterfaceXMLSchema.Schema.Field _field = null;
	InterfaceXMLSchema.Schema.Field.Detail _detail = null;
	public InterfaceCompare() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void compareSchema(Context context, String strAdminName, Logger schema_done_log, SCMConfigProperty scmConfigProperty)  throws Exception{
		try {
			sAdminType = strAdminName;
			version = scmConfigProperty.getVersion();
			compareResultFolder = scmConfigProperty.getCompareFolder() + "\\" + SchemaModelerConstants.FOLDER_NAME_SchemaObject;
			System.out.println(scmConfigProperty.getFirstFilePath());
			System.out.println(scmConfigProperty.getSecondFilePath());
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
	//main 
	private Map<String,List> processSchemaCompare(Context context, Map<String,Object> list1, Map<String,Object> list2)  throws Exception{
		Map<String,List> mCompareResultInfo = new HashMap<String,List>();
		try {
			List<Object> matchList = new ArrayList<Object>();
			List<Object> deltaList = new ArrayList<Object>();
			List<Object> uniqueList = new ArrayList<Object>();
			String sSchemaName = "";
			Interfaces obj1 = null;
			Interfaces obj2 = null;
			List<Object> listSchemaList1 = null;
			List<Object> listSchemaList2 = null;
			for (Map.Entry<String,Object> entry : list1.entrySet())  
			{
				sSchemaName = entry.getKey();
				listSchemaList1 = (ArrayList)entry.getValue();
				obj1 = (Interfaces)listSchemaList1.get(0);
				if(list2.containsKey(sSchemaName))
				{
					listSchemaList2 = (ArrayList)list2.get(sSchemaName);
					obj2 = (Interfaces)listSchemaList2.get(0);

					if(UIUtil.isEqual(obj1.name,obj2.name) && UIUtil.isEqual(obj1.description,obj2.description) && UIUtil.isEqual(obj1.hidden,obj2.hidden) && UIUtil.isEqual(obj1.registryname,obj2.registryname) && UIUtil.isEqual(obj1.getDerived(),obj2.getDerived()) && UIUtil.isEqual(obj1.getSabstract(),obj2.getSabstract()) && UIUtil.isEqual(obj1.getAttribute(),obj2.getAttribute()) && UIUtil.isEqual(obj1.getType(),obj2.getType()) && UIUtil.isEqual(obj1.getRelationship(),obj2.getRelationship())) {
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
			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
			//InterfaceInfo typeInfo = (InterfaceInfo) schemaFactory.getSchemaObject("type");
			JAXBContext jContext = JAXBContext.newInstance(InterfaceXMLSchema.class);
			Unmarshaller unMarsheller = jContext.createUnmarshaller();
			JAXBElement<InterfaceXMLSchema> interfaceJaxbElem = unMarsheller.unmarshal((new StreamSource(new FileReader(fileName))), InterfaceXMLSchema.class);
			InterfaceXMLSchema interfaceElem = interfaceJaxbElem.getValue();
			List<Schema> lstSchema = interfaceElem.getSchema();
			Iterator<Schema> itrSchema = lstSchema.iterator();
			Interfaces interfaces = null;
			String strFName = "";
			String strFValue = "";
			String typeName = null; // Added to check if element exists
			while (itrSchema.hasNext()) {
				listSchema = new ArrayList<Object>();
				_schemaElem = itrSchema.next();
				_basicElem = _schemaElem.getBasic();
				interfaces = new Interfaces(_basicElem.getName(), _basicElem.getDescription(), _basicElem.getHidden(),_basicElem.getRegistryName());
				interfaces.hidden = _basicElem.getHidden();
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
						interfaces.setDerived(strFValue);
					} else if (strFName.equalsIgnoreCase("abstract")) {
						interfaces.setSabstract(strFValue);
					} else if (strFName.equalsIgnoreCase("attribute")) {
						interfaces.setAttribute(strFValue);
					} else if (strFName.equalsIgnoreCase("type")) {
						interfaces.setType(strFValue);
					} else if (strFName.equalsIgnoreCase("relationship")) {
						interfaces.setRelationship(strFValue);
					}
				}
				listSchema.add(interfaces);
				listSchema.add(_schemaElem);
				mSchemaInfo.put(interfaces.name,listSchema);
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
			InterfaceXMLSchema ixs = objectFactory.createComponent();
			ixs.setName(sAdminType);
			ixs.setType("admin");
			ixs.setVersion(version);
			List<TypeXMLSchema.Schema.Trigger.Event.Eventdetail> lstEvent = null;
			ixs.schema = new ArrayList<InterfaceXMLSchema.Schema>();
			int iCountSchema = 0;
			boolean bSchemaSeperate = false;
			boolean bLogEverything = false;
			File file = null;
			String filePath = "";
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { InterfaceXMLSchema.class });
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty("jaxb.formatted.output", Boolean.valueOf(true));
			int iSize = list.size();
			List<Object> listSchemaList = null;
			for (int iCount = 0; iCount < iSize; iCount++) 
			{	
				listSchemaList = (ArrayList)list.get(iCount);				
				_schema = (InterfaceXMLSchema.Schema)listSchemaList.get(1);
				ixs.schema.add(_schema);
			}
			file = new File(compareResultFolder + "\\" + sAdminType);
			if(!(file.exists() && file.isDirectory())){
				file.mkdir();
			} 
			file = new File(file.getPath() + "\\" + compareFileName + SchemaModelerConstants.XML_EXTENSION);
			jaxbMarshaller.marshal(ixs, file);	
		} catch (Exception e) {
			throw e;
		}
	}
}
