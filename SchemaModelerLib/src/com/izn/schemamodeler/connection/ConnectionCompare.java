package com.izn.schemamodeler.connection;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import com.izn.schemamodeler.connection.ConnectionXMLSchema.Relationship;
import com.izn.schemamodeler.connection.ConnectionXMLSchema.Relationship.AttributeInfo;
import com.izn.schemamodeler.connection.ConnectionXMLSchema.Relationship.AttributeInfo.AttrDetail;
import com.izn.schemamodeler.connection.ConnectionXMLSchema.Relationship.Basic;
import com.izn.schemamodeler.connection.ConnectionXMLSchema.Relationship.Basic.Detail;
import com.izn.schemamodeler.connection.ConnectionXMLSchema.Relationship.RelationshipInfo;
import com.izn.schemamodeler.connection.ConnectionXMLSchema.Relationship.RelationshipInfo.RelDetail;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.SchemaModelerConstants;
import com.izn.schemamodeler.util.UIUtil;

import matrix.db.Context;

public class ConnectionCompare implements SchemaCompare {
	String sType = "";
	String version = "";
	String compareResultFolder = "";
	ConnectionXMLSchema.Relationship _conEle=null;
		Basic _basicElem=null;
		Detail  _detailElem=null;
		AttributeInfo  _attr =null;
		ConnectionXMLSchema _component = null;
		public ConnectionCompare() {
			// TODO Auto-generated constructor stub
		}

	@Override
	public void compareSchema(Context context, String strAdminName, Logger schema_done_log, SCMConfigProperty scmConfigProperty)  throws Exception{
		try {
			sType = strAdminName;
			version = scmConfigProperty.getVersion();
			compareResultFolder = scmConfigProperty.getCompareFolder() + "\\" + SchemaModelerConstants.FOLDER_NAME_Connection;
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
			ume.printStackTrace();
			throw ume;
		} catch (Exception e) {
			schema_done_log.error(""+e.getCause());
			e.printStackTrace();
			throw e;
		}
	}
	private Map<String,List> processSchemaCompare(Context context, Map<String,List> list1, Map<String,List> list2)  throws Exception{
		Map mCompareResultInfo = new HashMap();
		try {
			List matchList = new ArrayList();
			List deltaList = new ArrayList();
			List uniqueList = new ArrayList();
			String sSchemaName = "";
			Connection obj1 = null;
			Connection obj2 = null;
			List listSchemaList1 = null;
			List listSchemaList2 = null;
			for (Entry<String, List> entry : list1.entrySet())  
			{
				sSchemaName = entry.getKey();
				listSchemaList1 = (ArrayList)entry.getValue();
				obj1 = (Connection)listSchemaList1.get(0);
				if(list2.containsKey(sSchemaName))
				{
					listSchemaList2 = (ArrayList)list2.get(sSchemaName);
					obj2 = (Connection)listSchemaList2.get(0);
					if( UIUtil.isEqual(obj1.getFrommid(),obj2.getFrommid())&& UIUtil.isEqualStringMap(obj1.getLstAttribute(),obj2.getLstAttribute())) {
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
	
	private Map<String,List> readFromXML(Context context, String fileName)  throws Exception{
		List listSchema = null;
		Map mSchemaInfo = new HashMap();
		try {
			JAXBContext jContext = JAXBContext.newInstance(ConnectionXMLSchema.class);
			Unmarshaller unMarsheller = jContext.createUnmarshaller();
			JAXBElement<ConnectionXMLSchema> relJaxElem = unMarsheller.unmarshal((new StreamSource(new FileReader(fileName))), ConnectionXMLSchema.class);
			ConnectionXMLSchema _relComponent = relJaxElem.getValue();
			sType=_relComponent.name;
			version=_relComponent.version;
			List<Relationship> lstRel = _relComponent.getRelationship();
			String fValue="";
		    String fName="";
		    List<Map<String, String>> lstMap=null;
			List<Map<String, String>> lstRelAttrMap=null;
			Connection connection=null;
		    Map m =null;
		    int temp=0;
			for(Relationship _objRel:lstRel){
			temp++;
				listSchema = new ArrayList();
				lstMap=new ArrayList<Map<String, String>>();
				lstRelAttrMap=new ArrayList<Map<String, String>>();
				_basicElem = _objRel.getBasic();
				connection = new Connection(_basicElem.getType());
				List<Detail> lstDetail = _basicElem.getDetail();
				for(Detail detail: lstDetail){
					fName=detail.getName();
					if(detail.getValueAttribute() != null){
						fValue=detail.getValueAttribute().trim();
					}else{
						fValue="";
					}
					if(fName.equalsIgnoreCase("from_type")){
						connection.setFromType(fValue);
			 	   	}else if(fName.equalsIgnoreCase("from_name")){
			 	   		connection.setFromName(fValue);
			 	   	}else if(fName.equalsIgnoreCase("from_revision")){
			 	   		connection.setFromRevision(fValue);
			 	   	}else if(fName.equalsIgnoreCase("to_type")){
				 		connection.setToType(fValue);
			 	   	}else if(fName.equalsIgnoreCase("to_name")){
				 	 	connection.setToName(fValue);
			 	   	}else if(fName.equalsIgnoreCase("to_revision")){
			 	   		connection.setToRevision(fValue);
			 	   	}
				}
				com.izn.schemamodeler.connection.ConnectionXMLSchema.Relationship.AttributeInfo aifo =  _objRel.getAttributes();
			 	List<ConnectionXMLSchema.Relationship.AttributeInfo.AttrDetail> lst = aifo.getAttrdetail();
			 	if(lst != null){
			 		for(AttrDetail attrdetail:lst){
			 			m=new HashMap();
			 			m.put("name", attrdetail.getName());
			 			m.put("value", attrdetail.getValueAttribute() != null ? attrdetail.getValueAttribute().trim() : "");
			 			lstMap.add(m);
			 		}
			 		connection.setLstAttribute(lstMap);
			 	}
			 	List<RelationshipInfo> relListInfo =  _objRel.getRelationships();
			 	if(relListInfo != null){
			 		Connection relConnection = null;
			 		List<Connection> listRelConnection = new ArrayList<Connection>();
			 		for(RelationshipInfo relInfo : relListInfo){
			 			List<RelDetail> relListDetail = relInfo.getAttrdetail();
			 			relConnection = new Connection();
						for(RelDetail relDetail: relListDetail){
							fName=relDetail.getName();
							if(relDetail.getValueAttribute() != null){
								fValue=relDetail.getValueAttribute().trim();
							}else{
								fValue="";
							}
							if(fName.equalsIgnoreCase("tomid")){
								relConnection.setTomid(fValue);
					 	   	}else if(fName.equalsIgnoreCase("frommid")){
								relConnection.setFrommid(fValue);
					 	   	}else if(fName.equalsIgnoreCase("from_type")){
								relConnection.setFromType(fValue);
					 	   	}else if(fName.equalsIgnoreCase("from_name")){
					 	   		relConnection.setFromName(fValue);
					 	   	}else if(fName.equalsIgnoreCase("from_revision")){
					 	   		relConnection.setFromRevision(fValue);
					 	   	}else if(fName.equalsIgnoreCase("to_type")){
					 	   		relConnection.setToType(fValue);
					 	   	}else if(fName.equalsIgnoreCase("to_name")){
					 	   		relConnection.setToName(fValue);
					 	   	}else if(fName.equalsIgnoreCase("to_revision")){
					 	   		relConnection.setToRevision(fValue);
					 	   	}
						}
						List<com.izn.schemamodeler.connection.ConnectionXMLSchema.Relationship.RelationshipInfo.AttrDetail> relAttributeListDetail = relInfo.getRelAttrdetail();
						for(com.izn.schemamodeler.connection.ConnectionXMLSchema.Relationship.RelationshipInfo.AttrDetail relAttrDetail: relAttributeListDetail){
				 			m=new HashMap();
				 			m.put("name", relAttrDetail.getName());
				 			m.put("value", relAttrDetail.getValueAttribute() != null ? relAttrDetail.getValueAttribute().trim() : "");
				 			lstRelAttrMap.add(m);
						}
						relConnection.setLstAttribute(lstRelAttrMap);
						listRelConnection.add(relConnection);
			 		}
			 		connection.setLstRelationship(listRelConnection);
			 	}
			 	listSchema.add(connection);
			 	listSchema.add(_objRel);
				mSchemaInfo.put(connection.getToType()+connection.getToName()+connection.getToRevision()+ connection.getFromName()+connection.getFromRevision()+connection.getFromType(),listSchema);
			}			
		} catch (UnmarshalException ume) {
			throw ume;
		} catch (Exception e) {
			throw e;
		}
		return mSchemaInfo;
	}	
	private void writeToXML(Context context, String compareFileName, List list) throws Exception{
		ObjectFactory objectFactory=new ObjectFactory();
		try {
			File file = null;
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { ConnectionXMLSchema.class });
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty("jaxb.formatted.output", Boolean.valueOf(true));
			ConnectionXMLSchema cxs=objectFactory.createConnectionXMLSchema();
			cxs.setName(sType);
			cxs.setType("connection");
			cxs.setVersion(version);
			cxs.relationship = new ArrayList<ConnectionXMLSchema.Relationship>();
			int iSize = list.size();
			String typeArr[]=new String[iSize];
			Set typeSet=new HashSet();
			List listSchemaList1 = null;
			for (int iCount = 0; iCount < iSize; iCount++) 
			{	
				listSchemaList1 = (ArrayList)list.get(iCount);				
				_conEle = (ConnectionXMLSchema.Relationship)listSchemaList1.get(1);
				cxs.relationship.add(_conEle);
					
			}
			file = new File(compareResultFolder +"\\" + sType);
			if(!(file.exists() && file.isDirectory())){
				file.mkdir();
			} 
			file = new File(file.getPath() + "\\" + compareFileName + SchemaModelerConstants.XML_EXTENSION);
			jaxbMarshaller.marshal(cxs, file);	
				
		} catch (Exception e) {
			throw e;
		}
	}
}
