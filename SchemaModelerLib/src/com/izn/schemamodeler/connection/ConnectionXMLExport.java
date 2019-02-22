package com.izn.schemamodeler.connection;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.izn.schemamodeler.SchemaBusExport;
import com.izn.schemamodeler.SchemaExport;
import com.izn.schemamodeler.SchemaInfo;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.connection.ConnectionXMLSchema.Relationship;
import com.matrixone.apps.domain.util.MqlUtil;
import com.izn.schemamodeler.connection.ObjectFactory;
import com.izn.schemamodeler.util.SchemaModelerConstants;
import matrix.db.Context;

public class ConnectionXMLExport implements SchemaBusExport {

	ConnectionXMLSchema.Relationship _relationship = null;
	ConnectionXMLSchema.Relationship.Basic _basic = null;
	ConnectionXMLSchema.Relationship.Basic.Detail _detail = null;
	ConnectionXMLSchema.Relationship.AttributeInfo _attrInfo = null;
	ConnectionXMLSchema.Relationship.AttributeInfo.AttrDetail _attrdetail = null;
	List<ConnectionXMLSchema.Relationship.RelationshipInfo> listRelInfo = null;
	ConnectionXMLSchema.Relationship.RelationshipInfo _relInfo = null;
	ConnectionXMLSchema.Relationship.RelationshipInfo.RelDetail _reldetail = null;
	ConnectionXMLSchema.Relationship.RelationshipInfo.AttrDetail _relAttrDetails = null;
	String strMQLResult = "";
	String _sDBInfo = "";
	boolean flag=false;
	Map _hDBInfo = new HashMap();
	List<Map> _lstDBInfo = new ArrayList();
	ObjectFactory objectFactory = null;
	List<String> attributes = null;
	final String[] fieldDetail = { "from_type", "from_name", "from_revision", "to_type", "to_name", "to_revision" };
	List<String> _listFieldDetail = null;
	public ConnectionXMLExport() {
		// TODO Auto-generated constructor stub
		objectFactory = new ObjectFactory();
		attributes = new ArrayList<String>();
		_listFieldDetail = new ArrayList(Arrays.asList(fieldDetail));
		_listFieldDetail.add("tomid");
		_listFieldDetail.add("frommid");
	}

	@Override
	public Map<String,Set<String>> exportRel(Context context, String strRel, List<String> lstNames, String exportPath, String strVersion,
			String sFile, String sExpandRel, Logger schema_done_log, SCMConfigProperty scmConfigProperty) throws Exception{
		
		List<String> relationships = new ArrayList<String>();
		Map<String, List> mRelToRelInfoMap = null;
		List<Map> listFromMapInfo = null;
		List<Map> listToMapInfo = null;
		Set<String> setBusIdList = null;
		Map<String,Set<String>> mObjectMap = new HashMap<String,Set<String>>();
		String sBusId = "";
		String sBusType = "";
		try 
		{
			exportPath = exportPath+ "\\" + SchemaModelerConstants.FOLDER_NAME_Connection;
			SchemaInfo schemaInfo = _schemaFactory.getSchemaObject("connection");
			ConnectionXMLSchema cxs = objectFactory.createConnectionXMLSchema();
			cxs.setName(strRel);
			cxs.setType("connection");
			cxs.setVersion(strVersion);
			cxs.relationship = new ArrayList<ConnectionXMLSchema.Relationship>();

			
			for (String bo : lstNames) 
			{
				strMQLResult = MqlUtil.mqlCommand(context, "list $1 '$2'",new String[] { "rel", strRel });
				if(!strMQLResult.isEmpty()) 
				{
					strMQLResult = MqlUtil.mqlCommand(context, "expand bus $1 relationship '$2'",new String[] { bo, strRel });
					String strResults = MqlUtil.mqlCommand(context, "print bus $1 select '$2' '$3' '$4' dump '$5'",new String[] { bo, "type", "name", "revision", "" });
					if(!strMQLResult.isEmpty()) 
					{
						flag=true;
						_sDBInfo = schemaInfo.geSchemaInfo(context, bo, strRel);
						if (_sDBInfo != null && !_sDBInfo.isEmpty()) 
						{
							_hDBInfo = _gson.readValue(_sDBInfo, HashMap.class);
							if (_hDBInfo != null && _hDBInfo.size() > 0 && _hDBInfo.containsKey(bo)) 
							{
								_lstDBInfo = (List) _hDBInfo.get(bo);
								for (int i = 0; i < _lstDBInfo.size(); i++) 
								{
									Map _dbInfo = (Map) _lstDBInfo.get(i);
									_relationship = objectFactory.createConnectionXMLSchemaObject();
									_basic = objectFactory.createConnectionXMLSchemaObjectBasic();
									_basic.detail = new ArrayList<ConnectionXMLSchema.Relationship.Basic.Detail>();
									for (String detailName : fieldDetail) 
									{
										_detail = objectFactory.createConnectionXMLSchemaObjectBasicDetail();
										_detail.setName(detailName);
										_detail.setValueAttribute((String) _dbInfo.get(detailName));
										_basic.detail.add(_detail);
									}
									_relationship.setBasic(_basic);
									// process attribute
									_attrInfo = objectFactory.createConnectionXMLSchemaObjectAttributeInfo();
									_attrInfo.attribute = new ArrayList<ConnectionXMLSchema.Relationship.AttributeInfo.AttrDetail>();
									attributes = (List<String>) _dbInfo.get("attributes");
									if (attributes != null)
										for (String mAttribute : attributes) 
										{
										_attrdetail = objectFactory.createConnectionXMLSchemaObjectAttributeInfoAttrdetail();
										_attrdetail.setName(mAttribute);
										_attrdetail.setValueAttribute((String) _dbInfo.get(mAttribute));
										_attrInfo.attribute.add(_attrdetail);
										}
									_relationship.setAttributes(_attrInfo);
							
									//Rel to Rel---------------------STARTS
									mRelToRelInfoMap = (Map<String, List>) _dbInfo.get("relationships");
									if(mRelToRelInfoMap != null && !mRelToRelInfoMap.isEmpty())
									{								
										listToMapInfo = (List<Map>)mRelToRelInfoMap.get("tomid");
										listFromMapInfo = (List<Map>)mRelToRelInfoMap.get("frommid");
										if ((listToMapInfo != null && !listToMapInfo.isEmpty()) || (listFromMapInfo != null && !listFromMapInfo.isEmpty()))
										{								
											listRelInfo = new ArrayList<ConnectionXMLSchema.Relationship.RelationshipInfo>();	
											for (Map<String,Object> mRel : listToMapInfo) {
												listRelInfo.add(getReltoRelInfo(mRel));
											}
											for (Map<String,Object> mRel : listFromMapInfo) {
												listRelInfo.add(getReltoRelInfo(mRel));
											}
											_relationship.setRelationships(listRelInfo);
										}
									}
									else
									{
										listRelInfo = new ArrayList<ConnectionXMLSchema.Relationship.RelationshipInfo>();
										listRelInfo.add(objectFactory.createConnectionXMLSchemaObjectRelationshipInfo());
										_relationship.setRelationships(listRelInfo);
									}
									//Rel to Rel---------------------ENDS
									cxs.relationship.add(_relationship);
									
									//Add from/to object id to list----STARTS
									if(bo.equals((String)_dbInfo.get("to_id")))	{
										sBusId = (String)_dbInfo.get("from_id");
										sBusType = (String)_dbInfo.get("from_type");
									} else if(bo.equals((String)_dbInfo.get("from_id"))) {
										sBusId = (String)_dbInfo.get("to_id");
										sBusType = (String)_dbInfo.get("to_type");
									}
									if(mObjectMap.containsKey(sBusType)) {
										setBusIdList = mObjectMap.get(sBusType);
										setBusIdList.add(sBusId);
										mObjectMap.put(sBusType, setBusIdList);
									} else {
										setBusIdList = new HashSet<String>();
										setBusIdList.add(sBusId);
										mObjectMap.put(sBusType, setBusIdList);
									}
									//Add from/to object id to list----ENDS
								}
							}
						}
					}
					else 
					{
						schema_done_log.info("["+strRel+"] Relationship doesn't exist with busiessobject ["+strResults+"].");
					}
				}
				else 
				{
					schema_done_log.warn("["+strRel+"] Relationship doesn't exist.");
					continue;
				}
			}
			if(flag)
			{
				//File file = new File(exportPath + "\\rel_" + strRel.replaceAll(" ", "") + ".xml");
				File file = new File(exportPath + "\\rel_" + strRel.toLowerCase() + ".xml");
				JAXBContext jaxbContext = JAXBContext.newInstance(ConnectionXMLSchema.class);
				Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
				// output pretty printed
				jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				jaxbMarshaller.marshal(cxs, file);
				//schema_done_log.info("EXPORT TOTAL "+strRel+" : "+iCountSchema);
			}
		} 
		catch (Exception e) 
		{
			throw new Exception("Error occurred while exporting "+strRel+" : " +e.getCause());
		}
		return mObjectMap;
	}
	
	@Override
	public void exportBus(Context context, String strType, List<String> lstNames, String exportPath, String strVersion,String sFile, String sExpandRel, Logger schema_done_log, SCMConfigProperty scmConfigProperty) throws Exception{}

	private ConnectionXMLSchema.Relationship.RelationshipInfo getReltoRelInfo(Map<String,Object> mRel) throws Exception{
		List<String> listAttributeNames = (List)mRel.get("attributes");
		if(listAttributeNames == null){
			listAttributeNames = new ArrayList<String>();
		}
		_relInfo = objectFactory.createConnectionXMLSchemaObjectRelationshipInfo();
		_relInfo.relationship = new ArrayList<ConnectionXMLSchema.Relationship.RelationshipInfo.RelDetail>();
		_relInfo.attribute = new ArrayList<ConnectionXMLSchema.Relationship.RelationshipInfo.AttrDetail>();
		for (Map.Entry<String,Object> entry : mRel.entrySet()) {										
			_reldetail = objectFactory.createConnectionXMLSchemaObjectRelationshipInfoReldetail();
			_relAttrDetails = objectFactory.createConnectionXMLSchemaObjectRelationshipInfoRelAttrDetail();
			if(_listFieldDetail.contains(entry.getKey()))
			{
				_reldetail.setName(entry.getKey());	
				if(entry.getValue() instanceof String){											
					_reldetail.setValueAttribute((String)entry.getValue());
					_relInfo.relationship.add(_reldetail);
				}
			}
			else if(listAttributeNames.contains(entry.getKey()))
			{
				_relAttrDetails.setName(entry.getKey());	
				if(entry.getValue() instanceof String){	
					_relAttrDetails.setValueAttribute((String)entry.getValue());
					_relInfo.attribute.add(_relAttrDetails);
				}
			}
		}											
		return _relInfo;
	}
}
