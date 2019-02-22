package com.izn.schemamodeler.admin.relationship;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import com.izn.schemamodeler.util.SchemaModelerConstants;
import com.izn.schemamodeler.SchemaExport;
import com.izn.schemamodeler.SchemaInfo;
import com.izn.schemamodeler.util.SCMConfigProperty;
import matrix.db.Context;
import com.izn.schemamodeler.util.UIUtil;

public class RelationshipXMLExport implements SchemaExport {
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

	public RelationshipXMLExport() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void exportSchema(Context context, String strAdminType, List<String> lstNames, String exportPath,
			String strVersion, Logger schema_done_log, SCMConfigProperty scmConfigProperty) throws Exception{
		String name = "";
		String description = "";
		String hidden = "";
		String operator = "";
		String attribute = "";
		List<String> attributes = new ArrayList<String>();
		List<Map> triggers = new ArrayList<Map>();
		List<Map> ranges = new ArrayList<Map>();

		String[] fieldDetail = { "derived", "abstract", "sparse", "preventduplicates", "attribute" };
		try {
			exportPath = exportPath + "\\" + SchemaModelerConstants.FOLDER_NAME_SchemaObject;
			ObjectFactory objectFactory = new ObjectFactory();
			RelationshipXMLSchema rxs = objectFactory.createComponent();
			rxs.setName("relationship");
			rxs.setType("admin");
			rxs.setVersion(strVersion);
			SchemaInfo schemaInfo = _schemaFactory.getSchemaObject(strAdminType);
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
			if ("true".equalsIgnoreCase(scmConfigProperty.getSchemaSeperator())) {
				bSchemaSeperate = true;
				filePath = exportPath + "\\" + strAdminType.trim();
				file = new File(filePath);
				if (!file.exists() && !file.isDirectory()) {
					file.mkdir();
				}
			}
			List listSchemaNames = new ArrayList();
			if ("true".equalsIgnoreCase(scmConfigProperty.getLogEverything())) {
						bLogEverything = true;			
			}	
			for (String sName : lstNames) {
				iCountSchema += 1;
				attribute = "";
				Map mTriggers = new HashMap();
				_schema = objectFactory.createComponentSchema();
				_sDBInfo = schemaInfo.geSchemaInfo(context, sName, new String());
				_hDBInfo = _gson.readValue(_sDBInfo, HashMap.class);
				objectFactory.createComponentSchemaBasic();
				_basic = objectFactory.createComponentSchemaBasic();
				_basic.setDescription((String) _hDBInfo.get("description"));
				_basic.setHidden((String) _hDBInfo.get("hidden"));
				_basic.setName((String) _hDBInfo.get("name"));
				_basic.setRegistryName((String) _hDBInfo.get("registryname"));
				_schema.setBasic(_basic); // set basic
				_field = objectFactory.createComponentSchemaField();
				_trigger = objectFactory.createComponentSchemaTrigger();
				_field.setType("reldetails");
				_field.detail = new ArrayList<RelationshipXMLSchema.Schema.Field.Detail>();

				attributes = (List<String>) _hDBInfo.get("attribute");
				_hDBInfo.put("attribute", attribute);
				if (attributes != null && !attributes.isEmpty()) {
					attribute = String.join(",", attributes);
					_hDBInfo.put("attribute", attribute);
				}
				for (String fDtail : fieldDetail) {
					_detail = objectFactory.createComponentSchemaFieldDetail();
					_detail.setName(fDtail);
					_detail.setValueAttribute((String) _hDBInfo.get(fDtail));
					_field.detail.add(_detail);
				}
				_schema.setField(_field);
				triggers = (List<Map>) _hDBInfo.get("trigger");
				String event = "";
				for (Map mTrigger : triggers) {
					lstEvent = new ArrayList<RelationshipXMLSchema.Schema.Trigger.Event.Eventdetail>();
					_eventdetail = objectFactory.createComponentSchemaTriggerEventEventdetail();
					_eventdetail.setProgram("emxTriggerManager");
					_eventdetail.setInput((String) mTrigger.get("name"));
					_eventdetail.setType((String) mTrigger.get("type"));
					event = (String) mTrigger.get("action");
					if (mTriggers.containsKey(event)) {
						lstEvent = (List<RelationshipXMLSchema.Schema.Trigger.Event.Eventdetail>) mTriggers.get(event);
					}
					lstEvent.add(_eventdetail);
					mTriggers.put(event, lstEvent);
				}
				Set sTriggers = mTriggers.keySet();
				Iterator<String> itrsTriggers = sTriggers.iterator();
				String sKey = "";
				_trigger.event = new ArrayList<RelationshipXMLSchema.Schema.Trigger.Event>();
				while (itrsTriggers.hasNext()) {
					_event = objectFactory.createComponentSchemaTriggerEvent();
					sKey = itrsTriggers.next();
					_event.setName(sKey);
					_event.eventdetail = new ArrayList<RelationshipXMLSchema.Schema.Trigger.Event.Eventdetail>();
					List<RelationshipXMLSchema.Schema.Trigger.Event.Eventdetail> lst = (List<RelationshipXMLSchema.Schema.Trigger.Event.Eventdetail>) mTriggers
							.get(sKey);
					for (RelationshipXMLSchema.Schema.Trigger.Event.Eventdetail eventDetail : lst) {
						_event.eventdetail.add(eventDetail);
					}
					_trigger.event.add(_event);
				}

				_schema.setTrigger(_trigger);
				// process from side
				_schema.rel = new ArrayList<RelationshipXMLSchema.Schema.Rel>();
				LinkedHashMap fromSide = (LinkedHashMap) _hDBInfo.get("fromSide");
				processFromToSide(fromSide, "FromType", objectFactory);
				LinkedHashMap toSide = (LinkedHashMap) _hDBInfo.get("toSide");
				processFromToSide(toSide, "ToType", objectFactory);
				rxs.schema.add(_schema);

				if (bSchemaSeperate) {
					sName = sName.replaceAll(UIUtil.specialCharecters, "");
					file = new File(filePath + "\\" + sName + ".xml");
					jaxbMarshaller.marshal(rxs, file);
					rxs.schema = new ArrayList();
				}
				if(bLogEverything) {					
					listSchemaNames.add(sName);
				}
			}
			schema_done_log.info(strAdminType + "|" + iCountSchema);
			if (bLogEverything) {
				schema_done_log.info(listSchemaNames);				
			}
			if (!bSchemaSeperate) {
				file = new File(exportPath + "\\" + strAdminType.toLowerCase() + ".xml");
				jaxbMarshaller.marshal(rxs, file);
			}
			
		} catch (Exception e) {
			throw new Exception("Error occurred while exporting schema "+strAdminType+" : " +e.getCause());
		}

	}

	public void processFromToSide(LinkedHashMap _hDBInfo, String mode, ObjectFactory objectFactory) throws Exception {
		try {
			List<String> lstType = (List<String>) _hDBInfo.get("type");
			_hDBInfo.put("type", String.join(",", lstType));
			List<String> lstRel = (List<String>) _hDBInfo.get("relationship");
			_hDBInfo.put("relationship", String.join(",", lstRel));
			String meaning = (String) _hDBInfo.get("meaning");
			_hDBInfo.put("meaning", meaning);
			LinkedHashMap mCardinality = (LinkedHashMap) _hDBInfo.get("cardinality");
			String sOne = (String) mCardinality.get("one");
			_hDBInfo.put("cardinality", "many");
			if (sOne.equalsIgnoreCase("true")) {
				_hDBInfo.put("cardinality", "one");
			}
			getCloneRevisionFlag(_hDBInfo, "revision");
			getCloneRevisionFlag(_hDBInfo, "clone");
			String propogateConnect = (String) _hDBInfo.get("propogateConnect");
			_hDBInfo.put("propagate connection", propogateConnect);
			String propogateModify = (String) _hDBInfo.get("propogateModify");
			_hDBInfo.put("propagate modify", propogateModify);

			_rel = objectFactory.createComponentSchemaRel();
			_rel.setType(mode);
			_rel.reldetail = new ArrayList<RelationshipXMLSchema.Schema.Rel.Reldetail>();
			for (String key : relKeys) {
				_reldetail = objectFactory.createComponentSchemaRelReldetail();
				_reldetail.setName(key);
				_reldetail.setValueAttribute((String) _hDBInfo.get(key));
				_rel.reldetail.add(_reldetail);
			}
			_schema.rel.add(_rel);
		} catch (Exception e) {
			throw e;
		}
	}

	private void getCloneRevisionFlag(LinkedHashMap _hDBInfo, String mode) throws Exception {
		String[] keys = { "replicate", "float", "none" };
		try {
			LinkedHashMap mClone = (LinkedHashMap) _hDBInfo.get(mode);
			for (String key : keys) {
				String sValue = (String) mClone.get(key);
				if (sValue.equalsIgnoreCase("true")) {
					_hDBInfo.put(mode, key);
					break;
				}
			}
		} catch (Exception e) {
			throw e;
		}
	}
}
