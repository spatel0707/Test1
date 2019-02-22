package com.izn.schemamodeler.admin.role;

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
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.transform.stream.StreamSource;

import com.izn.schemamodeler.SchemaFactoryInfo;
import com.izn.schemamodeler.SchemaInfo;
import com.izn.schemamodeler.SchemaCompare;
import com.izn.schemamodeler.admin.role.RoleXMLSchema.Schema;
import com.izn.schemamodeler.admin.role.RoleXMLSchema.Schema.Basic;
import com.izn.schemamodeler.admin.role.RoleXMLSchema.Schema.Field;
import com.izn.schemamodeler.admin.role.RoleXMLSchema.Schema.Field.Childs;
import com.izn.schemamodeler.admin.role.RoleXMLSchema.Schema.Field.Detail;

import com.izn.schemamodeler.admin.type.Type;
import com.izn.schemamodeler.admin.type.TypeInfo;
import com.izn.schemamodeler.admin.type.TypeXMLSchema;
import com.izn.schemamodeler.admin.type.TypeXMLSchema.Schema.Trigger.Event;
import com.izn.schemamodeler.admin.type.TypeXMLSchema.Schema.Trigger.Event.Eventdetail;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.SchemaModelerConstants;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class RoleCompare implements SchemaCompare 
{
	Basic _basicElem = null;
	Field _fieldElem = null;
	Childs _childElem = null;

	String version = "";
	String compareResultFolder = "";
	String sAdminType = "";
	
	String _sDBInfo = "";
	Map _hDBInfo = new HashMap();
	RoleXMLSchema.Schema _schema = null;
	RoleXMLSchema.Schema.Basic _basic = null;
	RoleXMLSchema.Schema.Field _field = null;

	RoleXMLSchema.Schema.Field.Detail _detail = null;
	
	public RoleCompare() 
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public void compareSchema(Context context, String strAdminName, Logger schema_done_log,	SCMConfigProperty scmConfigProperty)  throws Exception
	{
		try 
		{
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
		    //uniqueList_1.addAll(compareSchema_2.get(SchemaModelerConstants.UNIQUE));
			
			schema_done_log.info(sAdminType +"|"+SchemaModelerConstants.MATCH_COUNT+matchList.size()+","+SchemaModelerConstants.DELTA_COUNT1+deltaList_1.size()+","+SchemaModelerConstants.DELTA_COUNT2+deltaList_2.size()+","+SchemaModelerConstants.UNIQUE_COUNT1+uniqueList_1.size()+","+SchemaModelerConstants.UNIQUE_COUNT2+uniqueList_2.size());
			
			writeToXML(context, SchemaModelerConstants.COMPARE_FILENAME+strAdminName+SchemaModelerConstants.COMPARE_MATCH, matchList);
			writeToXML(context, SchemaModelerConstants.COMPARE_FILENAME+strAdminName+SchemaModelerConstants.COMPARE_DELTA1, deltaList_1);
			writeToXML(context, SchemaModelerConstants.COMPARE_FILENAME+strAdminName+SchemaModelerConstants.COMPARE_DELTA2, deltaList_2);
			writeToXML(context, SchemaModelerConstants.COMPARE_FILENAME+strAdminName+SchemaModelerConstants.COMPARE_UNIQUE1, uniqueList_1);
			writeToXML(context, SchemaModelerConstants.COMPARE_FILENAME+strAdminName+SchemaModelerConstants.COMPARE_UNIQUE2, uniqueList_2);			
		} 
		catch (UnmarshalException ume) 
		{
			schema_done_log.error("UnmarshalException on ["+strAdminName+".xml] : "+ume.getCause());
			schema_done_log.warn(UIUtil.sUnmarshalExceptionWarningMessage);
			throw ume;
		} 
		catch (Exception e) 
		{
			throw e;
		}
	}
	
	private Map<String,List> processSchemaCompare(Context context, Map<String,Object> list1, Map<String,Object> list2)  throws Exception
	{
		Map<String,List> mCompareResultInfo = new HashMap<String,List>();
		try 
		{
			List<Object> matchList = new ArrayList<Object>();
			List<Object> deltaList = new ArrayList<Object>();
			List<Object> uniqueList = new ArrayList<Object>();
			boolean bMatch = true;
			String sSchemaName = "";
			Role obj1 = null;
			Role obj2 = null;
			List<Object> listSchemaList1 = null;
			List<Object> listSchemaList2 = null;
			for (Map.Entry<String,Object> entry : list1.entrySet())  
			{
				sSchemaName = entry.getKey();
				listSchemaList1 = (ArrayList)entry.getValue();
				obj1 = (Role)listSchemaList1.get(0);
				if(list2.containsKey(sSchemaName))
				{
					listSchemaList2 = (ArrayList)list2.get(sSchemaName);
					obj2 = (Role)listSchemaList2.get(0);
					if(UIUtil.isEqual(obj1.name,obj2.name) && UIUtil.isEqual(obj1.description,obj2.description) && UIUtil.isEqual(obj1.getParent(),obj2.getParent()) && UIUtil.isEqual(obj1.getSite(),obj2.getSite()) && UIUtil.isEqual(obj1.getRoletype(),obj2.getRoletype()) && UIUtil.isEqual(obj1.hidden,obj2.hidden) && UIUtil.isEqual(obj1.registryname,obj2.registryname) && UIUtil.isEqual(obj1.getChild(),obj2.getChild()) && UIUtil.isEqual(obj1.getAssigment(),obj2.getAssigment())&& UIUtil.isEqual(obj1.getMaturity(),obj2.getMaturity()))
					{
						matchList.add(listSchemaList1);
					}
					else
					{
						deltaList.add(listSchemaList1);
					}
				} 
				else 
				{
					uniqueList.add(listSchemaList1);
				}
			}
			mCompareResultInfo.put(SchemaModelerConstants.MATCH, matchList);
			mCompareResultInfo.put(SchemaModelerConstants.DELTA, deltaList);
			mCompareResultInfo.put(SchemaModelerConstants.UNIQUE, uniqueList);
		}
		catch (Exception e) 
		{
			throw e;
		}
		return mCompareResultInfo;
	}
	
	private Map<String,Object> readFromXML(Context context, String fileName)  throws Exception
	{
		List<Object> listSchema = null;
		Map<String,Object> mSchemaInfo = new HashMap<String,Object>();
		try
		{
			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
			RoleInfo roleInfo = (RoleInfo) schemaFactory.getSchemaObject("role");
			JAXBContext jConext = JAXBContext.newInstance(RoleXMLSchema.class);
			Unmarshaller unmarshaller = jConext.createUnmarshaller();
			JAXBElement<RoleXMLSchema> roleElem = unmarshaller.unmarshal(new StreamSource(new File(fileName)),
					RoleXMLSchema.class);
			RoleXMLSchema roleXMLSchema = roleElem.getValue();
			List<Schema> lstSchema = roleXMLSchema.getSchema();
			Role role = null;
			String fValue = "";
			String fName = "";
			String sFilePath = "";
			Map<String, String> mSettings = null;
			List<Map<String, String>> lstSetting = null;
			List<String> lstRoleItem = null;
			List<String> lstAChild = null;
			for (Schema _schema : lstSchema) 
			{
				listSchema = new ArrayList<Object>();
				lstSetting = new ArrayList<Map<String, String>>();
				lstAChild = new ArrayList<String>();
				lstRoleItem = new ArrayList<String>();
				_basicElem = _schema.getBasic();
				String roleName = _basicElem.getName();
				role = new Role(_basicElem.getName(), _basicElem.getDescription(), _basicElem.getHidden(),
						_basicElem.getRegistryName());
				role.hidden = this._basicElem.getHidden();
				_fieldElem = _schema.getField();
				List<Detail> lstDetail = _fieldElem.getDetail();
				for (Detail _detail : lstDetail) 
				{
					fName = _detail.getName();
					fValue = _detail.getValueAttribute();
					if (fValue != null) 
					{
						fValue = _detail.getValueAttribute().trim();
					} 
					else 
					{
						fValue = "";
					}
					if (fName.equalsIgnoreCase("parent")) 
					{
						role.setParent(fValue);
					} else if (fName.equalsIgnoreCase("site")) {
						role.setSite(fValue);
					} else if (fName.equalsIgnoreCase("roletype")) {
						role.setRoletype(fValue);
					} else if (fName.equalsIgnoreCase("maturity")) {
						role.setMaturity(fValue);
					} else if (fName.equalsIgnoreCase("child")) {
						role.setChild(fValue);
					} else if (fName.equalsIgnoreCase("assignment")) {
						role.setAssigment(fValue);
					}

				}
				listSchema.add(role);
				listSchema.add(_schema);
				mSchemaInfo.put(role.name,listSchema);
			
			}
		} 
		catch (UnmarshalException ume) 
		{
			throw ume;
		} catch (Exception e) 
		{
			throw e;
		}
		return mSchemaInfo;
	}	
	
	private void writeToXML(Context context, String compareFileName, List<Object> list) throws Exception
	{
		try 
		{
			ObjectFactory objectFactory = new ObjectFactory();
			RoleXMLSchema rxs = objectFactory.createComponent();
			rxs.setName("role");
			rxs.setType("admin");
			rxs.setVersion(version);
		
			rxs.schema = new ArrayList<RoleXMLSchema.Schema>();
			File file = null;
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { RoleXMLSchema.class });
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty("jaxb.formatted.output", Boolean.valueOf(true));
			
			int iSize = list.size();
			List<Object> listSchemaList = null;
			for (int iCount = 0; iCount < iSize; iCount++) 
			{	
				listSchemaList = (ArrayList)list.get(iCount);				
				_schema = (RoleXMLSchema.Schema)listSchemaList.get(1);
				rxs.schema.add(_schema);
			}
			file = new File(compareResultFolder + "\\" + sAdminType);
			if(!(file.exists() && file.isDirectory())){
				file.mkdir();
			} 
			file = new File(file.getPath() + "\\" + compareFileName + SchemaModelerConstants.XML_EXTENSION);
			jaxbMarshaller.marshal(rxs, file);
		}
		catch (Exception e) 
		{
			throw e;
		}
	}
}
