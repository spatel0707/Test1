package com.izn.schemamodeler.bo;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;

import com.izn.schemamodeler.SchemaCompare;
import com.izn.schemamodeler.bo.BusinessObjectXMLSchema.Object;
import com.izn.schemamodeler.bo.BusinessObjectXMLSchema.Object.AttributeInfo.AttrDetail;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.SchemaModelerConstants;
import com.izn.schemamodeler.util.UIUtil;

import matrix.db.Context;

public class BusinessObjectCompare implements SchemaCompare {

	BusinessObjectXMLSchema.Object.Basic _basicElem = null;
	BusinessObjectXMLSchema.Object _ObjectEle = null;
	BusinessObjectXMLSchema.Object.Basic.Field _fieldElem = null;
	BusinessObjectXMLSchema.Object.Basic.Field.Detail _detailElem = null;
	BusinessObjectXMLSchema.Object.AttributeInfo _attrInfo = null;
	BusinessObjectXMLSchema.Object.AttributeInfo.AttrDetail _attrDetails = null;
	
	String sAdminType = "";
    String sType = "";
	String version = "";
	String compareResultFolder = "";
	//Detail _detailElem = null;
	
	String _sDBInfo = "";
	Map _hDBInfo = new HashMap();

	public BusinessObjectCompare() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void compareSchema(Context context, String strAdminName, Logger schema_done_log,	SCMConfigProperty scmConfigProperty)  throws Exception{
		try {
			sAdminType = strAdminName;
			version = scmConfigProperty.getVersion();
			compareResultFolder = scmConfigProperty.getCompareFolder() + "\\" + SchemaModelerConstants.FOLDER_NAME_BusinessObject;
			Map<String,List> list1 = readFromXML(context, scmConfigProperty.getFirstFilePath());
			Map<String,List> list2 = readFromXML(context, scmConfigProperty.getSecondFilePath());
			
			Map compareSchema_1 = processSchemaCompare(context,list1,list2);
			Map compareSchema_2 = processSchemaCompare(context,list2,list1);
		    List matchList = (ArrayList)compareSchema_1.get(SchemaModelerConstants.MATCH);
		    List deltaList_1 = (ArrayList)compareSchema_1.get(SchemaModelerConstants.DELTA);
		    List deltaList_2 = (ArrayList)compareSchema_2.get(SchemaModelerConstants.DELTA);
		    List uniqueList_1 = (ArrayList)compareSchema_1.get(SchemaModelerConstants.UNIQUE);
		    List uniqueList_2 = (ArrayList)compareSchema_2.get(SchemaModelerConstants.UNIQUE);
			schema_done_log.info(sType +"|"+SchemaModelerConstants.MATCH_COUNT+matchList.size()+","+SchemaModelerConstants.DELTA_COUNT1+deltaList_1.size()+","+SchemaModelerConstants.DELTA_COUNT2+deltaList_2.size()+","+SchemaModelerConstants.UNIQUE_COUNT1+uniqueList_1.size()+","+SchemaModelerConstants.UNIQUE_COUNT2+uniqueList_2.size());
			
						
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
	
	
	private Map processSchemaCompare(Context context, Map<String, List> list1, Map<String, List> list2)  throws Exception{
		Map mCompareResultInfo = new HashMap();
		try {
			List matchList = new ArrayList();
			List deltaList = new ArrayList();
			List uniqueList = new ArrayList();
			String sSchemaName = "";
			BusinessObject obj1 = null;
			BusinessObject obj2 = null;
			List listSchemaList1 = null;
			List listSchemaList2 = null;
			for (Entry<String, List> entry : list1.entrySet())  
			{
				sSchemaName = entry.getKey();
				listSchemaList1 = (ArrayList)entry.getValue();
				obj1 = (BusinessObject)listSchemaList1.get(0);
				if(list2.containsKey(sSchemaName))
				{
					listSchemaList2 = (ArrayList)list2.get(sSchemaName);
					obj2 = (BusinessObject)listSchemaList2.get(0);
					if(UIUtil.isEqual(obj1.type,obj2.type) && UIUtil.isEqual(obj1.name,obj2.name) && UIUtil.isEqual(obj1.revision,obj2.revision) && UIUtil.isEqual(obj1.getDescription(),obj2.getDescription()) && UIUtil.isEqual(obj1.getPolicy(),obj2.getPolicy()) && UIUtil.isEqual(obj1.getVault(),obj2.getVault())   && UIUtil.isEqual(obj1.getState(),obj2.getState()) && UIUtil.isEqual(obj1.getOwner(),obj2.getOwner()) && UIUtil.isEqual(obj1.getFilePath(),obj2.getFilePath()) && UIUtil.isEqual(obj1.getGrantee(),obj2.getGrantee()) && UIUtil.isEqualStringMap(obj1.getLstAttribute(),obj2.getLstAttribute())) {
						matchList.add(listSchemaList1);
					} else {
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
	
	private Map readFromXML(Context context, String fileName)  throws Exception{
		List listSchema = null;
		List<BusinessObject> lstNg = new ArrayList<BusinessObject>();
		Map mSchemaInfo = new HashMap();
		try {
			JAXBContext jContext = JAXBContext.newInstance(BusinessObjectXMLSchema.class);
			Unmarshaller unMarsheller = jContext.createUnmarshaller();
			JAXBElement<BusinessObjectXMLSchema> busJaxbElem = unMarsheller.unmarshal((new StreamSource(new FileReader(fileName))), BusinessObjectXMLSchema.class);
			BusinessObjectXMLSchema _busComponent = busJaxbElem.getValue();
			List<Object> lsObject = _busComponent.getObject();
			Iterator<Object> itsObject = lsObject.iterator();
			String fValue = "";
			String fName = "";
			BusinessObject bus = null;
			Map mAttributes = null;
			List<Map<String, String>> lstMap =null;
			while (itsObject.hasNext()) 
			{
				lstMap = new ArrayList();
				listSchema = new ArrayList();
				this._ObjectEle = itsObject.next();
				this._basicElem = this._ObjectEle.getBasic();
				bus = new BusinessObject(_basicElem.getType(), _basicElem.getName(), _basicElem.getRevision());
				_fieldElem = this._basicElem.getField();
				sType = _basicElem.getType();
				List<BusinessObjectXMLSchema.Object.Basic.Field.Detail> lsDetails = this._fieldElem.getDetail();
				Iterator<BusinessObjectXMLSchema.Object.Basic.Field.Detail> itrDetails = lsDetails.iterator();
				while (itrDetails.hasNext()) {
					
					this._detailElem = ((BusinessObjectXMLSchema.Object.Basic.Field.Detail) itrDetails.next());
					fName = this._detailElem.getName();
					if(this._detailElem.getValueAttribute() != null){
						fValue = this._detailElem.getValueAttribute().trim();
					}else{
						fValue = "";
					}
					if (fName.equalsIgnoreCase("POLICY")) {
						bus.setPolicy(fValue);
					} else if (fName.equalsIgnoreCase("VAULT")) {
						bus.setVault(fValue);
					}
					else if (fName.equalsIgnoreCase("DESCRIPTION")) {
						bus.setDescription(fValue);
					} else if (fName.equalsIgnoreCase("OWNER")) {
						bus.setOwner(fValue);
					} else if (fName.equalsIgnoreCase("STATE")) {
						bus.setState(fValue);
					} else if (fName.equalsIgnoreCase("FILEPATH")) {
						bus.setFilePath(fValue);
					}
				}
				this._attrInfo = this._ObjectEle.getAttributes();
				List<BusinessObjectXMLSchema.Object.AttributeInfo.AttrDetail> lstAttribDetails = this._attrInfo.getAttrdetail();
				if(lstAttribDetails != null)
				{
					for (AttrDetail attrdetail : lstAttribDetails) {
						mAttributes = new HashMap();
						mAttributes.put("name", attrdetail.getName());
						mAttributes.put("value", attrdetail.getValueAttribute() != null ? attrdetail.getValueAttribute().trim() : "");
						lstMap.add(mAttributes);
					}					
				}
				bus.setLstAttribute(lstMap);
				listSchema.add(bus);
				listSchema.add(this._ObjectEle);
				listSchema.add(_basicElem.getType());
				mSchemaInfo.put(bus.type+bus.name+bus.revision,listSchema);			
			}				
		} catch (UnmarshalException ume) {
			throw ume;
		} catch (Exception e) {
			throw e;
		}
		return mSchemaInfo;
	}	
	
	private void writeToXML(Context context, String compareFileName, List list) throws Exception
	{
		ObjectFactory objectFactory = new ObjectFactory();
		try {
			File file = null;
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { BusinessObjectXMLSchema.class });
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty("jaxb.formatted.output", Boolean.valueOf(true));
			int iSize = list.size();
			String typeArr[]=new String[iSize];
			Set typeSet=new HashSet();
			List listSchemaList1 = null;
			for (int iCount = 0; iCount < iSize; iCount++) 
			{	
				listSchemaList1 = (ArrayList)list.get(iCount);				
				String type = (String) listSchemaList1.get(2);
				typeSet.add(type);
				typeArr[iCount]=type;
				
			}
			List<Object> listSchemaList = null;
			Iterator it2=typeSet.iterator();
			while(it2.hasNext()) 
			{
				String str=(String)it2.next();
				BusinessObjectXMLSchema bxs = objectFactory.createBusinessObjectXMLSchema();
				bxs.setName(str);
				bxs.setType("businessobject");
				bxs.setVersion(version); 
				bxs.object = new ArrayList<BusinessObjectXMLSchema.Object>();
				
				for (int iCount = 0; iCount < iSize; iCount++) 
				{	
					if(typeArr[iCount].equals(str))
					{
					listSchemaList = (ArrayList)list.get(iCount);				
					_ObjectEle = (BusinessObjectXMLSchema.Object)listSchemaList.get(1);
					bxs.object.add(_ObjectEle);
					}
					
				}
				file = new File(compareResultFolder + "\\" + sAdminType);
				if(!(file.exists() && file.isDirectory())){
					file.mkdir();
				} 
				
				file = new File(compareResultFolder + "\\" +sAdminType + "\\" + str);
				if(!(file.exists() && file.isDirectory())){
					file.mkdir();
				} 
				str="_"+str+"_";
				 StringBuffer buf = new StringBuffer(compareFileName);
				    buf.insert(14, str);
				file = new File(file.getPath() +"\\"+ buf+ SchemaModelerConstants.XML_EXTENSION);
				jaxbMarshaller.marshal(bxs, file);			
			}
		} catch (Exception e) {
			throw e;
		}
	}
}
