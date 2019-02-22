package com.izn.schemamodeler.admin.type;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.SchemaExport;
import com.izn.schemamodeler.SchemaInfo;
import com.izn.schemamodeler.util.SchemaModelerConstants;
import matrix.db.Context;
import com.izn.schemamodeler.util.UIUtil;

public class TypeXMLExport implements SchemaExport {
	Map _hDBInfo = new HashMap();
	String _sDBInfo = "";

	TypeXMLSchema.Schema _schema = null;
	TypeXMLSchema.Schema.Basic _basic = null;
	TypeXMLSchema.Schema.Field _field = null;
	TypeXMLSchema.Schema.Field.Detail _detail = null;
	TypeXMLSchema.Schema.Trigger _trigger = null;
	TypeXMLSchema.Schema.Trigger.Event _event = null;
	TypeXMLSchema.Schema.Trigger.Event.Eventdetail _eventdetail = null;
	String[] fieldDetail = { "derived", "abstract", "sparse", "attribute", "method" };

	public TypeXMLExport() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void exportSchema(Context context, String strAdminType, List<String> lstNames, String exportPath,
			String strVersion, Logger schema_done_log, SCMConfigProperty scmConfigProperty) throws Exception{
		String name = "";
		String description = "";
		String hidden = "";
		String isAbstract = "";
		String derived = "";
		String registryName = "";
		List<String> attributes = new ArrayList<String>();
		List<String> methods = new ArrayList<String>();
		List<Map> triggers = new ArrayList<Map>();
		String attribute = "";
		String method = "";
		ObjectFactory objectFactory = new ObjectFactory();
		try {
			exportPath = exportPath + "\\" + SchemaModelerConstants.FOLDER_NAME_SchemaObject;
			SchemaInfo schemaInfo = _schemaFactory.getSchemaObject(strAdminType);
			TypeXMLSchema txc = objectFactory.createComponent();
			txc.setName(strAdminType);
			txc.setType("admin");
			txc.setVersion(strVersion);
			List<TypeXMLSchema.Schema.Trigger.Event.Eventdetail> lstEvent = null;
			txc.schema = new ArrayList<TypeXMLSchema.Schema>();
			int iCountSchema = 0;
			boolean bSchemaSeperate = false;
			boolean bLogEverything = false;
			File file = null;
			String filePath = "";
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { TypeXMLSchema.class });
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
				Map mTriggers = new HashMap();
				attribute = "";
				method = "";
				_sDBInfo = schemaInfo.geSchemaInfo(context, sName, new String());
				_hDBInfo = _gson.readValue(_sDBInfo, HashMap.class);
				_schema = objectFactory.createComponentSchema();
				_basic = objectFactory.createComponentSchemaBasic();
				_field = objectFactory.createComponentSchemaField();
				_trigger = objectFactory.createComponentSchemaTrigger();

				name = (String) _hDBInfo.get("name");

				description = (String) _hDBInfo.get("description");

				hidden = (String) _hDBInfo.get("hidden");
				isAbstract = (String) _hDBInfo.get("abstract");
				derived = (String) _hDBInfo.get("derived");
				attributes = (List<String>) _hDBInfo.get("attribute");

				_basic.setRegistryName((String) _hDBInfo.get("registryname"));
				_hDBInfo.put("attribute", attribute);
				if (!attributes.isEmpty()) {
					attribute = String.join(",", attributes);
					_hDBInfo.put("attribute", attribute);
				}
				if (_hDBInfo != null && _hDBInfo.containsKey("method")) {
					methods = (List<String>) _hDBInfo.get("method");
				}
				if (methods != null && methods.size() > 0) {
					// if (!(methods.isEmpty()) || (methods.size()==0) ) ){
					_hDBInfo.put("method", method);
					if (!methods.isEmpty()) {
						method = String.join(",", methods);
						_hDBInfo.put("method", method);
					}
				}
				triggers = (List<Map>) _hDBInfo.get("trigger");
				// process basic tag
				_basic.setName(name);

				_basic.setDescription(description);

				_basic.setHidden(hidden);
				// _basic.setRegistryName("");

				_schema.setBasic(_basic); // setBaic
				// process field tag
				_field.setType("typedetails");
				_field.detail = new ArrayList<TypeXMLSchema.Schema.Field.Detail>();
				for (String detailName : fieldDetail) {
					_detail = objectFactory.createComponentSchemaFieldDetail();
					_detail.setName(detailName);
					_detail.setValueAttribute((String) _hDBInfo.get(detailName));
					_field.detail.add(_detail);
				}
				_schema.setField(_field); // set fieldTag
				// process trigger
				String event = "";
				for (Map mTrigger : triggers) {
					lstEvent = new ArrayList<TypeXMLSchema.Schema.Trigger.Event.Eventdetail>();
					_eventdetail = objectFactory.createComponentSchemaTriggerEventEventdetail();
					_eventdetail.setProgram("emxTriggerManager");
					_eventdetail.setInput((String) mTrigger.get("name"));
					_eventdetail.setType((String) mTrigger.get("type"));
					event = (String) mTrigger.get("action");
					if (mTriggers.containsKey(event)) {
						lstEvent = (List<TypeXMLSchema.Schema.Trigger.Event.Eventdetail>) mTriggers.get(event);
					}
					lstEvent.add(_eventdetail);
					mTriggers.put(event, lstEvent);
				}

				Set sTriggers = mTriggers.keySet();
				Iterator<String> itrsTriggers = sTriggers.iterator();
				String sKey = "";
				_trigger.event = new ArrayList<TypeXMLSchema.Schema.Trigger.Event>();
				while (itrsTriggers.hasNext()) {
					_event = objectFactory.createComponentSchemaTriggerEvent();
					sKey = itrsTriggers.next();
					_event.setName(sKey);
					_event.eventdetail = new ArrayList<TypeXMLSchema.Schema.Trigger.Event.Eventdetail>();
					List<TypeXMLSchema.Schema.Trigger.Event.Eventdetail> lst = (List<TypeXMLSchema.Schema.Trigger.Event.Eventdetail>) mTriggers
							.get(sKey);
					for (TypeXMLSchema.Schema.Trigger.Event.Eventdetail eventDetail : lst) {
						_event.eventdetail.add(eventDetail);
					}
					_trigger.event.add(_event);
					_schema.setTrigger(_trigger);
				}
				txc.schema.add(_schema);
				if (bSchemaSeperate) {
					sName = sName.replaceAll(UIUtil.specialCharecters, "");
					file = new File(filePath + "\\" + sName+ ".xml");
					jaxbMarshaller.marshal(txc, file);
					txc.schema = new ArrayList();
				}
				if(bLogEverything) {					
					listSchemaNames.add(sName);
				}
				methods.removeAll(methods);
			}
			schema_done_log.info(strAdminType + "|" + iCountSchema);
			if (bLogEverything) {
				schema_done_log.info(listSchemaNames);				
			}
			if (!bSchemaSeperate) {
				file = new File(exportPath + "\\" + strAdminType.toLowerCase() + ".xml");
				jaxbMarshaller.marshal(txc, file);
			}
			
		} catch (Exception e) {
			throw new Exception("Error occurred while exporting schema "+strAdminType+" : " +e.getCause());
		}
	}

}
