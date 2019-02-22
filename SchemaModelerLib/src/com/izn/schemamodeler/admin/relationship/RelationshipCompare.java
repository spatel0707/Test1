package com.izn.schemamodeler.admin.relationship;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import com.izn.schemamodeler.admin.relationship.Relationship.FromType;
import com.izn.schemamodeler.admin.relationship.Relationship.ToType;
import com.izn.schemamodeler.admin.relationship.RelationshipXMLSchema.Schema;
import com.izn.schemamodeler.admin.relationship.RelationshipXMLSchema.Schema.Basic;
import com.izn.schemamodeler.admin.relationship.RelationshipXMLSchema.Schema.Field;
import com.izn.schemamodeler.admin.relationship.RelationshipXMLSchema.Schema.Field.Detail;
import com.izn.schemamodeler.admin.relationship.RelationshipXMLSchema.Schema.Rel;
import com.izn.schemamodeler.admin.relationship.RelationshipXMLSchema.Schema.Rel.Reldetail;
import com.izn.schemamodeler.admin.relationship.RelationshipXMLSchema.Schema.Trigger;
import com.izn.schemamodeler.admin.relationship.RelationshipXMLSchema.Schema.Trigger.Event;
import com.izn.schemamodeler.admin.relationship.RelationshipXMLSchema.Schema.Trigger.Event.Eventdetail;
import com.izn.schemamodeler.admin.role.Role;
import com.izn.schemamodeler.admin.role.RoleXMLSchema;
import com.izn.schemamodeler.admin.type.ObjectFactory;
import com.izn.schemamodeler.admin.type.Type;
import com.izn.schemamodeler.admin.type.TypeInfo;
import com.izn.schemamodeler.admin.type.TypeXMLSchema;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.SchemaModelerConstants;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class RelationshipCompare implements SchemaCompare 
{
	Schema _schemaElem = null;
	Basic _basicElem = null;
	Field _fieldElem = null;
	Detail _detailElem = null;
	Trigger _triggerElem = null;
	Eventdetail _eventdetailElem = null;
	Event _eventElem = null;
	Rel _relElem = null;
	Reldetail _reldetailElem = null;
	
	String version = "";
	String compareResultFolder = "";
	String sAdminType = "";
	
	RelationshipXMLSchema.Schema _schema = null;
	RelationshipXMLSchema.Schema.Basic _basic = null;
	RelationshipXMLSchema.Schema.Field _field = null;
	RelationshipXMLSchema.Schema.Field.Detail _detail = null;
	RelationshipXMLSchema.Schema.Trigger _trigger = null;
	RelationshipXMLSchema.Schema.Trigger.Event _event = null;
	RelationshipXMLSchema.Schema.Trigger.Event.Eventdetail _eventdetail = null;
	RelationshipXMLSchema.Schema.Rel _rel = null;
	RelationshipXMLSchema.Schema.Rel.Reldetail _reldetail = null;
	String[] relKeys = { "type", "relationship", "meaning", "cardinality", "revision", "clone", "propagate modify",
			"propagate connection" };
	String _sDBInfo = "";
	Map _hDBInfo = new HashMap();

	public RelationshipCompare() 
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public void compareSchema(Context context, String strAdminName, Logger schema_done_log,SCMConfigProperty scmConfigProperty)  throws Exception
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
			Relationship obj1 = null;
			Relationship obj2 = null;
			List<Object> listSchemaList1 = null;
			List<Object> listSchemaList2 = null;
			for (Map.Entry<String,Object> entry : list1.entrySet())  
			{
				sSchemaName = entry.getKey();
				listSchemaList1 = (ArrayList)entry.getValue();
				obj1 = (Relationship)listSchemaList1.get(0);
				if(list2.containsKey(sSchemaName))
				{
					listSchemaList2 = (ArrayList)list2.get(sSchemaName);
					obj2 = (Relationship)listSchemaList2.get(0);
					if( UIUtil.isEqual(obj1.name,obj2.name) && UIUtil.isEqual(obj1.description,obj2.description) && UIUtil.isEqual(obj1.hidden,obj2.hidden) && UIUtil.isEqual(obj1.registryname,obj2.registryname) && UIUtil.isEqual(obj1.getDerived(),obj2.getDerived()) && UIUtil.isEqual(obj1.getSparse(),obj2.getSparse()) && UIUtil.isEqual(obj1.getAttributes(),obj2.getAttributes()) && UIUtil.isEqual(obj1.getSlTriggers(),obj2.getSlTriggers()) && isEqual((Relationship.FromType)obj1.getFromType(),(Relationship.FromType)obj2.getFromType()) && isEqual((Relationship.ToType)obj1.getToType(),(Relationship.ToType)obj2.getToType())&& UIUtil.isEqual(obj1.getPreventdups(),obj2.getPreventdups())) 
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
	
	private boolean isEqual(Relationship.FromType r1, Relationship.FromType r2) throws Exception
	{
		if(UIUtil.isEqual(r1.getFromType(),r2.getFromType()) && UIUtil.isEqual(r1.getFromCardinality(),r2.getFromCardinality()) && UIUtil.isEqual(r1.getFromClone(),r2.getFromClone()) && UIUtil.isEqual(r1.getFromRevision(),r2.getFromRevision()) && UIUtil.isEqual(r1.getFromRel(),r2.getFromRel()) && UIUtil.isEqual(r1.getFromMeaning(),r2.getFromMeaning()) && UIUtil.isEqual(r1.getFromProModify(),r2.getFromProModify()) && UIUtil.isEqual(r1.getFromProconnection(),r2.getFromProconnection()))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	private boolean isEqual(Relationship.ToType r1, Relationship.ToType r2) throws Exception
	{		
		if(UIUtil.isEqual(r1.getToType(),r2.getToType()) && UIUtil.isEqual(r1.getToCardinality(),r2.getToCardinality())&& UIUtil.isEqual(r1.getToRevision(),r2.getToRevision()) && UIUtil.isEqual(r1.getToRel(),r2.getToRel()) && UIUtil.isEqual(r1.getToMeaning(),r2.getToMeaning()) && UIUtil.isEqual(r1.getToClone(),r2.getToClone()) && UIUtil.isEqual(r1.getToProModify(),r2.getToProModify()) && UIUtil.isEqual(r1.getToProconnection(),r2.getToProconnection())){		
			return true;
		}
		else
		{
			return false;
		}
	}
	private Map<String,Object> readFromXML(Context context, String fileName)  throws Exception
	{
		List<Object> listSchema = null;
		Map<String,Object> mSchemaInfo = new HashMap<String,Object>();
		try 
		{
			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
			RelationshipInfo relInfo = (RelationshipInfo) schemaFactory.getSchemaObject("relationship");
			JAXBContext jContext = JAXBContext.newInstance(RelationshipXMLSchema.class);
			Unmarshaller unMarshaller = jContext.createUnmarshaller();
			JAXBElement<RelationshipXMLSchema> relElem = unMarshaller.unmarshal(new StreamSource(new File(fileName)),
					RelationshipXMLSchema.class);
			RelationshipXMLSchema relSchema = relElem.getValue();
			List<Schema> lstSchema = relSchema.getSchema();
			Iterator<Schema> itrSchema = lstSchema.iterator();
			List slTrigger = new ArrayList();
			Relationship relationship = null;
			String fValue = "";
			String fName = "";
			String sRelType = "";
			while (itrSchema.hasNext()) 
			{
				listSchema = new ArrayList<Object>();
				_schemaElem = itrSchema.next();
				_basicElem = _schemaElem.getBasic();
				relationship = new Relationship(_basicElem.getName(), _basicElem.getDescription(),
						_basicElem.getHidden(), _basicElem.getRegistryName());
				relationship.hidden = this._basicElem.getHidden();
				_fieldElem = _schemaElem.getField();
				List<Detail> lstDetail = _fieldElem.getDetail();
				Iterator<Detail> itrDetail = lstDetail.iterator();
				while (itrDetail.hasNext()) 
				{
					_detailElem = itrDetail.next();
					fName = _detailElem.getName();
					fValue = _detailElem.getValueAttribute();
					if (fValue != null) 
					{
						fValue = _detailElem.getValueAttribute().trim();
					} 
					else 
					{
						fValue = "";
					}
					if (fName.equalsIgnoreCase("derived")) 
					{
						relationship.setDerived(fValue);
					} else if (fName.equalsIgnoreCase("abstract")) {
						relationship.setsAbstract(fValue);
					} else if (fName.equalsIgnoreCase("sparse")) {
						relationship.setSparse(fValue);
					} else if (fName.equalsIgnoreCase("preventduplicates")) {
						relationship.setPreventdups(fValue);
					} else if (fName.equalsIgnoreCase("attribute")) {
						relationship.setAttributes(fValue);
					}

				}
				
				_triggerElem = _schemaElem.getTrigger();
				if (_triggerElem != null) 
				{
					List<Event> lstEvent = _triggerElem.getEvent();
					Iterator<Event> itrEvent = lstEvent.iterator();
					Map<String, String> mTriggerDetails = null;
					String strEvent = "";
					while (itrEvent.hasNext()) 
					{
						_eventElem = itrEvent.next();
						// name of trigger event
						strEvent = _eventElem.getName();
						List<Eventdetail> lstEventdetails = _eventElem.getEventdetail();
						Iterator<Eventdetail> itrEventdetails = lstEventdetails.iterator();
						while (itrEventdetails.hasNext()) 
						{
							_eventdetailElem = itrEventdetails.next();
							mTriggerDetails = new HashMap<String, String>();
							mTriggerDetails.put(ACTION, strEvent);
							mTriggerDetails.put(TYPE, _eventdetailElem.getType());
							mTriggerDetails.put(PROGRAM, _eventdetailElem.getProgram());
							mTriggerDetails.put(NAME, _eventdetailElem.getInput());
							slTrigger.add(mTriggerDetails);
						}
					}
					relationship.setSlTriggers(slTrigger);
				}
				//FromType and ToType
				List<Rel> lstRel = _schemaElem.getRel();
				Iterator<Rel> itr = lstRel.iterator();
				Relationship.FromType fromType = relationship.getNewFromType();
				Relationship.ToType toType = relationship.getNewTotype();
				while (itr.hasNext()) 
				{
					_relElem = itr.next();
					sRelType = _relElem.getType();
					List<Reldetail> lstRelDetail = _relElem.getReldetail();
					Iterator<Reldetail> itrRelDetails = lstRelDetail.iterator();
					while (itrRelDetails.hasNext()) 
					{
						_reldetailElem = itrRelDetails.next();
						fName = _reldetailElem.getName();
						fValue = _reldetailElem.getValueAttribute();
						if (fName != null && !"".equals(fName) && fValue != null && !"".equals(fValue)) 
						{
							if (sRelType.equalsIgnoreCase("FromType")) 
							{
								if (fName.equalsIgnoreCase("type")) 
								{
									fromType.setFromType(fValue);
								} else if (fName.equalsIgnoreCase("relationship")) {
									fromType.setFromRel(fValue);
								} else if (fName.equalsIgnoreCase("meaning")) {
									fromType.setFromMeaning(fValue);
								} else if (fName.equalsIgnoreCase("cardinality")) {
									fromType.setFromCardinality(fValue);
								} else if (fName.equalsIgnoreCase("revision")) {
									fromType.setFromRevision(fValue);
								} else if (fName.equalsIgnoreCase("clone")) {
									fromType.setFromClone(fValue);
								} else if (fName.equalsIgnoreCase("propagate modify")) {
									fromType.setFromProModify(fValue);
								} else if (fName.equalsIgnoreCase("propagate connection")) 
								{
									fromType.setFromProconnection(fValue);
								}
							} 
							else 
							{
								if (fName.equalsIgnoreCase("type")) {
									toType.setToType(fValue);
								} else if (fName.equalsIgnoreCase("relationship")) {
									toType.setToRel(fValue);
								} else if (fName.equalsIgnoreCase("meaning")) {
									toType.setToMeaning(fValue);
								} else if (fName.equalsIgnoreCase("cardinality")) {
									toType.setToCardinality(fValue);
								} else if (fName.equalsIgnoreCase("revision")) {
									toType.setToRevision(fValue);
								} else if (fName.equalsIgnoreCase("clone")) {
									toType.setToClone(fValue);
								} else if (fName.equalsIgnoreCase("propagate modify")) {
									toType.setToProModify(fValue);
								} else if (fName.equalsIgnoreCase("propagate connection")) {
									toType.setToProconnection(fValue);
								}
							}
						}
					}
				}
				relationship.setFromType(fromType);
				relationship.setToType(toType);
				listSchema .add(relationship);
				listSchema .add(_schemaElem);
				mSchemaInfo.put(relationship.name,listSchema);
			}
		}
		
		catch (UnmarshalException ume) 
		{
			throw ume;
		} 
		catch (Exception e) 
		{
			throw e;
		}
		return mSchemaInfo;
	}	
	
	private void writeToXML(Context context, String compareFileName, List<Object> list) throws Exception
	{
		List<Map<Object,Object>> triggers = new ArrayList<Map<Object,Object>>();
		try
		{
			com.izn.schemamodeler.admin.relationship.ObjectFactory objectFactory = new com.izn.schemamodeler.admin.relationship.ObjectFactory();
			RelationshipXMLSchema rxs = objectFactory.createComponent();
			rxs.setName("relationship");
			rxs.setType("admin");
			rxs.setVersion(version);
			List<RelationshipXMLSchema.Schema.Trigger.Event.Eventdetail> lstEvent = null;
			rxs.schema = new ArrayList<RelationshipXMLSchema.Schema>();
			int iCountSchema = 0;
			boolean bSchemaSeperate = false;
			boolean bLogEverything = false;
			File file = null;
			String filePath = "";
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { RelationshipXMLSchema.class });
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty("jaxb.formatted.output", Boolean.valueOf(true));
			int iSize = list.size();
			List<Object> listSchemaList = null;
			for (int iCount = 0; iCount < iSize; iCount++) 
			{	
				listSchemaList = (ArrayList)list.get(iCount);				
				_schema = (RelationshipXMLSchema.Schema)listSchemaList.get(1);
				rxs.schema.add(_schema);
			}
			file = new File(compareResultFolder + "\\" + sAdminType);
			if(!(file.exists() && file.isDirectory()))
			{
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
