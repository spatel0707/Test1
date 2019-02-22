package com.izn.schemamodeler.admin.attribute;

import com.izn.schemamodeler.SchemaFactoryInfo;
import com.izn.schemamodeler.SchemaCompare;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.SchemaModelerConstants;
import com.izn.schemamodeler.util.UIUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import matrix.db.Context;

import org.apache.commons.collections.CollectionUtils;

public class AttributeCompare implements SchemaCompare {

	AttributeXMLSchema.Schema _schemaEle = null;
	AttributeXMLSchema.Schema.Basic _basicElem = null;
	AttributeXMLSchema.Schema.Basic.Field _fieldElem = null;
	AttributeXMLSchema.Schema.Basic.Field.Detail _detailElem = null;
	AttributeXMLSchema.Schema.Range _rangeElem = null;
	AttributeXMLSchema.Schema.Range.Rangedetail _rangedetail = null;
	AttributeXMLSchema.Schema.Trigger _triggerElem = null;
	AttributeXMLSchema.Schema.Trigger.Event _eventElem = null;
	AttributeXMLSchema.Schema.Trigger.Event.Eventdetail _eventdetailElem = null;
	String MAXLENGTH = "maxlength";
	String VALUETYPE = "valuetype";
	String MULTILINE = "multiline";
	String DIMENSION = "dimension";
	String RESETCLONE = "resetonclone";
	String RESETREVISION = "resetonrevision";
	StringBuilder sbAttributeMQL = new StringBuilder();
	List<Attribute> ltAttributes = new ArrayList();
	Map<String, String> mOpearators = new HashMap();
	String sAdminType = "";
	//For write xml
	String version = "";
	String compareResultFolder = "";
	public AttributeCompare() {
		this.mOpearators.put("equal", "=");
		this.mOpearators.put("notequal", "!=");
		this.mOpearators.put("greaterthan", ">");
		this.mOpearators.put("greaterthanequal", ">=");
		this.mOpearators.put("lessthan", "<");
		this.mOpearators.put("lessthanequal", "<=");
		this.mOpearators.put("notmatch", "!match");
		this.mOpearators.put("match", "match");
		this.mOpearators.put("notstringmatch", "!smatch");
		this.mOpearators.put("stringmatch", "smatch");
		this.mOpearators.put("between", "between");
		this.mOpearators.put("program", "program");
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
			System.out.println("Attribute deltaList_1 :"+deltaList_1);
			System.out.println("Attribute deltaList_2 :"+deltaList_2);
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
			Attribute obj1 = null;
			Attribute obj2 = null;
			List<Object> listSchemaList1 = null;
			List<Object> listSchemaList2 = null;
			for (Map.Entry<String,Object> entry : list1.entrySet())  
			{
				sSchemaName = entry.getKey();
				listSchemaList1 = (ArrayList)entry.getValue();
				obj1 = (Attribute)listSchemaList1.get(0);
				if(list2.containsKey(sSchemaName))
				{
					listSchemaList2 = (ArrayList)list2.get(sSchemaName);
					obj2 = (Attribute)listSchemaList2.get(0);
					if(UIUtil.isEqual(obj1.name,obj2.name) && UIUtil.isEqual(obj1.description,obj2.description) && UIUtil.isEqual(obj1.hidden,obj2.hidden) && UIUtil.isEqual(obj1.registryname,obj2.registryname) && UIUtil.isEqual(obj1.getType(),obj2.getType()) && UIUtil.isEqual(obj1.getDeFault(),obj2.getDeFault()) && UIUtil.isEqual(obj1.getMaxLength(),obj2.getMaxLength()) && UIUtil.isEqual(obj1.getMultiLine(),obj2.getMultiLine()) && UIUtil.isEqual(obj1.getValueType(),obj2.getValueType()) && UIUtil.isEqual(obj1.getResetOnClone(),obj2.getResetOnClone()) && UIUtil.isEqual(obj1.getResetOnRevision(),obj2.getResetOnRevision()) && UIUtil.isEqual(obj1.getResetOnRevision(),obj2.getResetOnRevision()) && UIUtil.isEqual(obj1.getDimension(),obj2.getDimension()) && UIUtil.isEqual(obj1.getSlRanges(),obj2.getSlRanges()) && UIUtil.isEqual(obj1.getSlTriggers(),obj2.getSlTriggers())) {
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
			AttributeInfo attribute = (AttributeInfo) schemaFactory.getSchemaObject("attribute");

			JAXBContext jContext = JAXBContext.newInstance(new Class[] { AttributeXMLSchema.class });
			Unmarshaller unMarsheller = jContext.createUnmarshaller();
			JAXBElement<AttributeXMLSchema> attributeEle = unMarsheller
					.unmarshal(new StreamSource(new FileReader(fileName)), AttributeXMLSchema.class);
			AttributeXMLSchema _axComponent = (AttributeXMLSchema) attributeEle.getValue();
			Map<String, String> mBasic = new HashMap();
			Map<String, Object> mRange = new HashMap();

			List<AttributeXMLSchema.Schema> lsScheama = _axComponent.getSchema();
			Iterator<AttributeXMLSchema.Schema> itrSchema = lsScheama.iterator();
			Attribute attrib = null;
			String attributeName = null;
			int iCountSchema = 0;
			String strFValue = "";
			while (itrSchema.hasNext()) {
				listSchema= new ArrayList<Object>();
				iCountSchema += 1;
				List slTrigger = new ArrayList();
				List slRanges = new ArrayList();
				this._schemaEle = ((AttributeXMLSchema.Schema) itrSchema.next());
				this._basicElem = this._schemaEle.getBasic();

				attrib = new Attribute(this._basicElem.getName(), this._basicElem.getDescription(),
						this._basicElem.getHidden(), this._basicElem.getRegistryName());
				attrib.hidden = this._basicElem.getHidden();
				this._fieldElem = this._basicElem.getField();
				List<AttributeXMLSchema.Schema.Basic.Field.Detail> lsDetails = this._fieldElem.getDetail();
				Iterator<AttributeXMLSchema.Schema.Basic.Field.Detail> itrDetails = lsDetails.iterator();
				while (itrDetails.hasNext()) {
					this._detailElem = ((AttributeXMLSchema.Schema.Basic.Field.Detail) itrDetails.next());
					strFValue = _detailElem.getValueAttribute();
					if (strFValue != null) {
						strFValue = _detailElem.getValueAttribute().trim();
					} else {
						strFValue = "";
					}
					if (this._detailElem.getName().equalsIgnoreCase("DEFAULT")) {
						attrib.setDeFault(strFValue);
					} else if (this._detailElem.getName().equalsIgnoreCase("TYPE")) {
						attrib.setType(strFValue);
					} else if (this._detailElem.getName().equalsIgnoreCase("MULTILINE")) {
						attrib.setMultiLine(strFValue);
					} else if (this._detailElem.getName().equalsIgnoreCase("MAXLENGTH")) {
						attrib.setMaxLength(strFValue);
					} else if (this._detailElem.getName().equalsIgnoreCase("VALUETYPE")) {
						attrib.setValueType(strFValue);
					} else if (this._detailElem.getName().equalsIgnoreCase("RESETONCLONE")) {
						attrib.setResetOnClone(strFValue);
					} else if (this._detailElem.getName().equalsIgnoreCase("RESETONREVISION")) {
						attrib.setResetOnRevision(strFValue);
					} else if (this._detailElem.getName().equalsIgnoreCase("DIMENSION")) {
						attrib.setDimension(strFValue);
					} else if (this._detailElem.getName().equalsIgnoreCase("OWNER")) {
						attrib.setOwner(strFValue);
					} else if (this._detailElem.getName().equalsIgnoreCase("OWNERKIND")) {
						attrib.setOwnerKind(strFValue);
					}
				}
				if (this._schemaEle.getRange() != null) {
					this._rangeElem = this._schemaEle.getRange();
					List<AttributeXMLSchema.Schema.Range.Rangedetail> lstRangeDetails = this._rangeElem
							.getRangedetail();
					Iterator<AttributeXMLSchema.Schema.Range.Rangedetail> itrRangedetail = lstRangeDetails.iterator();
					while (itrRangedetail.hasNext()) {
						mRange = new HashMap();
						this._rangedetail = ((AttributeXMLSchema.Schema.Range.Rangedetail) itrRangedetail.next());
						mRange.put("operator", this._rangedetail.getType());
						System.out.println("F:"+this._rangedetail.getValueAttribute());
						System.out.println("S:"+this._rangedetail.getValueAttribute().split(","));
						mRange.put("value", this._rangedetail.getValueAttribute());
						slRanges.add(mRange);
					}
					System.out.println("slRanges :"+slRanges);
					attrib.setSlRanges(slRanges);
				}
				
				if (this._schemaEle.getTrigger() != null) {
					this._triggerElem = this._schemaEle.getTrigger();

					List<AttributeXMLSchema.Schema.Trigger.Event> lstEvent = this._triggerElem.getEvent();
					Iterator<AttributeXMLSchema.Schema.Trigger.Event> itrEvent = lstEvent.iterator();
					Map<String, String> mTriggerDetails = null;
					String strEvent = "";
					while (itrEvent.hasNext()) {
						this._eventElem = itrEvent.next();
						strEvent = this._eventElem.getName();
						List<AttributeXMLSchema.Schema.Trigger.Event.Eventdetail> lstEventdetails = this._eventElem
								.getEventdetail();
						Iterator<AttributeXMLSchema.Schema.Trigger.Event.Eventdetail> itrEventdetails = lstEventdetails.iterator();
						while (itrEventdetails.hasNext()) {
							this._eventdetailElem = itrEventdetails.next();
							mTriggerDetails = new HashMap<String, String>();
							mTriggerDetails.put(ACTION, strEvent);
							mTriggerDetails.put(TYPE, _eventdetailElem.getType());
							mTriggerDetails.put(PROGRAM, _eventdetailElem.getProgram());
							mTriggerDetails.put(NAME, _eventdetailElem.getInput());
							slTrigger.add(mTriggerDetails);
						}
					}
					attrib.setSlTriggers(slTrigger);
				}
				listSchema.add(attrib);
				listSchema.add(this._schemaEle);
				mSchemaInfo.put(attrib.name,listSchema);
			}
		} catch (UnmarshalException ume) {
			throw ume;
		} catch (Exception e) {
			throw e;
		}
		return mSchemaInfo;
	}	
	
	private void writeToXML(Context context, String compareFileName, List<Object> list) throws Exception{
		AttributeXMLSchema.Schema _schema = null;
		String attribute = "";
		String method = "";
		ObjectFactory objectFactory = new ObjectFactory();
		File file = null;
		try {
			AttributeXMLSchema axc = objectFactory.createAttributeXMLSchema();
			axc.setName("attribute");
			axc.setType("admin");
			axc.setVersion(version);
			axc.schema = new ArrayList();
			List<AttributeXMLSchema.Schema.Trigger.Event.Eventdetail> lstEvent = null;
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { AttributeXMLSchema.class });
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty("jaxb.formatted.output", Boolean.valueOf(true));
			
			int iSize = list.size();
			List<Object> listSchemaList = null;
			for (int iCount = 0; iCount < iSize; iCount++) 
			{	
				listSchemaList = (ArrayList)list.get(iCount);
				_schema = (AttributeXMLSchema.Schema)listSchemaList.get(1);
				axc.schema.add(_schema);
			}
			
			file = new File(compareResultFolder + "\\" + sAdminType);
			if(!(file.exists() && file.isDirectory())){
				file.mkdir();
			} 
			file = new File(file.getPath() + "\\" + compareFileName + SchemaModelerConstants.XML_EXTENSION);
			jaxbMarshaller.marshal(axc, file);	
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	private static List filterRanges(List slNewRanges, List slOldRanges) throws Exception {
		List<Map> slFilteredRanges = new ArrayList();
		try {
			Iterator<Map> itrOldRanges = slOldRanges.iterator();
			Map mFilterRanges = new HashMap();
			String strOldOperator = "";
			String stNewOperator = "";
			boolean bMatch = false;
			while (itrOldRanges.hasNext()) {
				bMatch = false;
				Map mOldRange = (Map) itrOldRanges.next();
				strOldOperator = (String) mOldRange.get("operator");
				Iterator<Map> itrNewRanges = slNewRanges.iterator();
				while (itrNewRanges.hasNext()) {
					mFilterRanges = new HashMap();
					Map mNewRange = (Map) itrNewRanges.next();
					stNewOperator = (String) mNewRange.get("operator");
					if ((stNewOperator.equalsIgnoreCase(strOldOperator))
							&& (!strOldOperator.equalsIgnoreCase("program"))
							&& (!strOldOperator.equalsIgnoreCase("between"))) {
						bMatch = true;
						String[] sArrNewValues = (String[]) mNewRange.get("value");
						List slOldValues = (List) mOldRange.get("value");
						List sNewValues = Arrays.asList(sArrNewValues);
						mFilterRanges.put("operator", strOldOperator);
						mFilterRanges.put(stNewOperator.toLowerCase() + "_Ranges",
								CollectionUtils.subtract(slOldValues, sNewValues));
						slFilteredRanges.add(mFilterRanges);
					}
				}
				if (!bMatch) {
					slFilteredRanges.add(mOldRange);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return slFilteredRanges;
	}
}
