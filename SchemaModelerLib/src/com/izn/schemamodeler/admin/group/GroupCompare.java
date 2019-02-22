package com.izn.schemamodeler.admin.group;

import java.io.File;
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
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.transform.stream.StreamSource;

import com.izn.schemamodeler.SchemaFactoryInfo;
import com.izn.schemamodeler.SchemaCompare;
import com.izn.schemamodeler.admin.group.Group;
import com.izn.schemamodeler.admin.group.GroupXMLSchema;
import com.izn.schemamodeler.admin.group.ObjectFactory;
import com.izn.schemamodeler.admin.group.GroupXMLSchema.Schema;
import com.izn.schemamodeler.admin.group.GroupXMLSchema.Schema.Basic;
import com.izn.schemamodeler.admin.group.GroupXMLSchema.Schema.Field;
import com.izn.schemamodeler.admin.group.GroupXMLSchema.Schema.Field.Childs;
import com.izn.schemamodeler.admin.group.GroupXMLSchema.Schema.Field.Childs.Child;
import com.izn.schemamodeler.admin.role.Role;
import com.izn.schemamodeler.admin.group.GroupXMLSchema.Schema.Field.Detail;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.SchemaModelerConstants;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class GroupCompare implements SchemaCompare {
	String sAdminType = "";
	Schema _schemaElem = null;
	Basic _basicElem = null;
	Field _fieldElem = null;
	Detail _detailElem = null;

	
	String version = "";
	String compareResultFolder = "";
	GroupXMLSchema.Schema _schema = null;
	GroupXMLSchema.Schema.Basic _basic = null;
	GroupXMLSchema.Schema.Field _field = null;
	GroupXMLSchema.Schema.Field.Detail _detail = null;

	public GroupCompare() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void compareSchema(Context context, String strAdminName, Logger schema_done_log,			SCMConfigProperty scmConfigProperty)  throws Exception{
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
			ume.printStackTrace();
			throw ume;
		} catch (Exception e) {
			schema_done_log.error(""+e.getCause());
			e.printStackTrace();
			throw e;
		}
	}
	
	private Map<String,Object> readFromXML(Context context, String fileName)  throws Exception{
		List<Object> listSchema = null;
		Map<String,Object> mSchemaInfo = new HashMap<String,Object>();
		try {
			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
			JAXBContext jContext = JAXBContext.newInstance(GroupXMLSchema.class);
			Unmarshaller unMarsheller = jContext.createUnmarshaller();
			JAXBElement<GroupXMLSchema> groupJaxbElem = unMarsheller.unmarshal((new StreamSource(new FileReader(fileName))), GroupXMLSchema.class);
			GroupXMLSchema groupElem = groupJaxbElem.getValue();
			List<Schema> lstSchema = groupElem.getSchema();
			Iterator<Schema> itrSchema = lstSchema.iterator();
			Group group = null;
			String fName = "";
			String fValue = "";
			String associationName = null; // Added to check if element exists
			while (itrSchema.hasNext()) {
				listSchema = new ArrayList<Object>();
				_schemaElem = itrSchema.next();
				_basicElem = _schemaElem.getBasic();
				group = new Group(_basicElem.getName(), _basicElem.getDescription(), _basicElem.getHidden(),_basicElem.getRegistryName());
				group.hidden = this._basicElem.getHidden();
				_fieldElem = _schemaElem.getField();
				List<Detail> lstDetail = _fieldElem.getDetail();
				Iterator<Detail> itrDetail = lstDetail.iterator();
				while (itrDetail.hasNext()) {
					_detailElem = itrDetail.next();
					fName = _detailElem.getName();
					fValue = _detailElem.getValueAttribute();
					if (fValue != null) {
						fValue = _detailElem.getValueAttribute().trim();
					} else {
						fValue = "";
					}
					if (fName.equalsIgnoreCase("parent")) {
						group.setParent(fValue.trim());
					} else if (fName.equalsIgnoreCase("site")) {
						group.setSite(fValue.trim());
					} else if (fName.equalsIgnoreCase("description")) {
						group.setDescription(fValue.trim());
					} else if (fName.equalsIgnoreCase("child")) {
						group.setChild(fValue.trim());
					} else if (fName.equalsIgnoreCase("assignment")) {
						group.setAsssignment(fValue.trim());
					} else if (fName.equalsIgnoreCase("iconFile")) {
						group.setIconFile(fValue.trim());
					}
				}
				listSchema.add(group);
				listSchema.add(_schemaElem);
				mSchemaInfo.put(group.name,listSchema);
			}
		} catch (UnmarshalException ume) {
			throw ume;
		} catch (Exception e) {
			throw e;
		}
		return mSchemaInfo;
	}	
	
	private Map<String,List> processSchemaCompare(Context context, Map<String,Object> list1, Map<String,Object> list2)  throws Exception{
		Map<String,List> mCompareResultInfo = new HashMap<String,List>();
		try {
			List<Object> matchList = new ArrayList<Object>();
			List<Object> deltaList = new ArrayList<Object>();
			List<Object> uniqueList = new ArrayList<Object>();

			String sSchemaName = "";
			Group obj1 = null;
			Group obj2 = null;
			List<Object> listSchemaList1 = null;
			List<Object> listSchemaList2 = null;
			for (Map.Entry<String,Object> entry : list1.entrySet())  
			{
				sSchemaName = entry.getKey();
				listSchemaList1 = (ArrayList)entry.getValue();
				obj1 = (Group)listSchemaList1.get(0);
				if(list2.containsKey(sSchemaName))
				{
					listSchemaList2 = (ArrayList)list2.get(sSchemaName);
					obj2 = (Group)listSchemaList2.get(0);
					
					if(UIUtil.isEqual(obj1.name,obj2.name) && UIUtil.isEqual(obj1.description,obj2.description) && UIUtil.isEqual(obj1.hidden,obj2.hidden) && UIUtil.isEqual(obj1.registryname,obj2.registryname) && UIUtil.isEqual(obj1.getParent(),obj2.getParent()) && UIUtil.isEqual(obj1.getSite(),obj2.getSite()) && UIUtil.isEqual(obj1.getChild(),obj2.getChild()) && UIUtil.isEqual(obj1.getAsssignment(),obj2.getAsssignment()) && UIUtil.isEqual(obj1.getIconFile(),obj2.getIconFile())) {
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
	
	private void writeToXML(Context context, String compareFileName, List<Object> list) throws Exception{
		
		ObjectFactory objectFactory = new ObjectFactory();
		try {
			GroupXMLSchema txc = objectFactory.createComponent();
			txc.setName(sAdminType);
			txc.setType("admin");
			txc.setVersion(version);

			txc.schema = new ArrayList<GroupXMLSchema.Schema>();
			File file = null;
			String filePath = "";
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { GroupXMLSchema.class });
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty("jaxb.formatted.output", Boolean.valueOf(true));
			int iSize = list.size();
			List<Object> listSchemaList = null;
			for (int iCount = 0; iCount < iSize; iCount++) 
			{	
				listSchemaList = (ArrayList)list.get(iCount);				
				_schema = (GroupXMLSchema.Schema)listSchemaList.get(1);
				txc.schema.add(_schema);
			}

			file = new File(compareResultFolder + "\\" + sAdminType);
			if(!(file.exists() && file.isDirectory())){
				file.mkdir();
			} 
			file = new File(file.getPath() + "\\" + compareFileName + SchemaModelerConstants.XML_EXTENSION);
			jaxbMarshaller.marshal(txc, file);	
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
