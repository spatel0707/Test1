package com.izn.schemamodeler.admin.program;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
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
import com.izn.schemamodeler.admin.program.ProgramXMLSchema.Schema;
import com.izn.schemamodeler.admin.program.ProgramXMLSchema.Schema.Basic;
import com.izn.schemamodeler.admin.program.ProgramXMLSchema.Schema.Field;
import com.izn.schemamodeler.admin.program.ProgramXMLSchema.Schema.Field.Detail;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.SchemaModelerConstants;
import com.izn.schemamodeler.util.UIUtil;

import matrix.db.Context;

public class ProgramCompare implements SchemaCompare {

	Basic _basicElem = null;
	Field _fieldElem = null;
	String sAdminType = "";
	String version = "";
	String compareResultFolder = "";
	Schema _schemaElem = null;
	Detail _detailElem = null;
	
	String _sDBInfo = "";
	Map _hDBInfo = new HashMap();
	ProgramXMLSchema.Schema _schema = null;
	ProgramXMLSchema.Schema.Basic _basic = null;
	ProgramXMLSchema.Schema.Field _field = null;
	ProgramXMLSchema.Schema.Field.Detail _detail = null;

	public ProgramCompare() {
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
			Program obj1 = null;
			Program obj2 = null;
			List<Object> listSchemaList1 = null;
			List<Object> listSchemaList2 = null;
			for (Map.Entry<String,Object> entry : list1.entrySet())  
			{
				sSchemaName = entry.getKey();
				listSchemaList1 = (ArrayList)entry.getValue();
				obj1 = (Program)listSchemaList1.get(0);
				if(list2.containsKey(sSchemaName))
				{
					listSchemaList2 = (ArrayList)list2.get(sSchemaName);
					obj2 = (Program)listSchemaList2.get(0);
					if(UIUtil.isEqual(obj1.name,obj2.name) && UIUtil.isEqual(obj1.description,obj2.description) && UIUtil.isEqual(obj1.hidden,obj2.hidden) && UIUtil.isEqual(obj1.registryName,obj2.registryName) && UIUtil.isEqual(obj1.getCode(),obj2.getCode()) && UIUtil.isEqual(obj1.getType(),obj2.getType())  && UIUtil.isEqual(obj1.getUser(),obj2.getUser()) && UIUtil.isEqual(obj1.getExecute(),obj2.getExecute())  && UIUtil.isEqual(obj1.getNeedsbusinessobject(),obj2.getNeedsbusinessobject())  && UIUtil.isEqual(obj1.getDownloadable(),obj2.getDownloadable())  && UIUtil.isEqual(obj1.getPipe(),obj2.getPipe()) && UIUtil.isEqual(obj1.getPool(),obj2.getPool())&&UIUtil.compareFile(obj1.getFilepath(), obj2.getFilepath())) {
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
			JAXBContext jContext = JAXBContext.newInstance(ProgramXMLSchema.class);
			Unmarshaller unMarsheller = jContext.createUnmarshaller();
			JAXBElement<ProgramXMLSchema> programJaxbElem = unMarsheller.unmarshal((new StreamSource(new FileReader(fileName))), ProgramXMLSchema.class);
			ProgramXMLSchema programElem = programJaxbElem.getValue();
			List<Schema> lstSchema = programElem.getSchema();
	
			Iterator<Schema> itrSchema = lstSchema.iterator();
			Program program = null;
			String strFName = "";
			String strFValue = "";
			String sProgramName = "";
			File file = new File(fileName);
			String sParentPath = file.getParent() + "\\Programs";
			while (itrSchema.hasNext()) {
				listSchema = new ArrayList<Object>();
				_schemaElem = itrSchema.next();
				_basicElem = _schemaElem.getBasic();
				program = new Program(_basicElem.getName(),_basicElem.getHidden(),_basicElem.getDescription(), _basicElem.getRegistryName());
				sProgramName = _basicElem.getName();
				program.hidden = this._basicElem.getHidden();
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
					if (strFName.equalsIgnoreCase("code")) {
						program.setCode(strFValue);
					} else if (strFName.equalsIgnoreCase("type")) {
						program.setType(strFValue);
						program.setType(strFValue);
						if("java".equals(strFValue)) {							
							sProgramName = sProgramName.replace(".","\\");
							sProgramName = sProgramName + "_mxJPO.java";
						}
						if (sParentPath != null && !sParentPath.isEmpty()) {
							program.setFilepath(sParentPath + "\\" + sProgramName);
						} 
					} else if (strFName.equalsIgnoreCase("user")) {
						program.setUser(strFValue);
					} else if (strFName.equalsIgnoreCase("execute")) {
						program.setExecute(strFValue);
					} else if (strFName.equalsIgnoreCase("needsbusinessobject")) {
						program.setNeedsbusinessobject(strFValue);
					} else if (strFName.equalsIgnoreCase("downloadable")) {
						program.setDownloadable(strFValue);
					} else if (strFName.equalsIgnoreCase("pipe")) {
						program.setPipe(strFValue);
					} else if (strFName.equalsIgnoreCase("pooled")) {
						program.setPool(strFValue);
					}
				}
				listSchema.add(program);
				listSchema.add(_schemaElem);
				mSchemaInfo.put(program.name,listSchema);
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
			ProgramXMLSchema txc = objectFactory.createComponent();
			txc.setName(sAdminType);
			txc.setType("admin");
			txc.setVersion(version);
			txc.schema = new ArrayList<ProgramXMLSchema.Schema>();
			File file = null;
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { ProgramXMLSchema.class });
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty("jaxb.formatted.output", Boolean.valueOf(true));
			int iSize = list.size();
			List<Object> listSchemaList = null;
			for (int iCount = 0; iCount < iSize; iCount++) 
			{	
				listSchemaList = (ArrayList)list.get(iCount);				
				_schema = (ProgramXMLSchema.Schema)listSchemaList.get(1);
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


