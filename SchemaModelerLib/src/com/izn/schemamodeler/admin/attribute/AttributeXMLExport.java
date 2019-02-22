package com.izn.schemamodeler.admin.attribute;

import com.izn.schemamodeler.SchemaExport;
import com.izn.schemamodeler.SchemaInfo;
import com.izn.schemamodeler.util.SCMConfigProperty;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import matrix.db.Context;
import com.izn.schemamodeler.util.SchemaModelerConstants;
import org.apache.log4j.Logger;
import com.izn.schemamodeler.util.UIUtil;

public class AttributeXMLExport implements SchemaExport {
	AttributeXMLSchema.Schema _schema = null;
	AttributeXMLSchema.Schema.Basic _basic = null;
	AttributeXMLSchema.Schema.Basic.Field _field = null;
	AttributeXMLSchema.Schema.Basic.Field.Detail _detail = null;
	AttributeXMLSchema.Schema.Trigger _trigger = null;
	AttributeXMLSchema.Schema.Trigger.Event _event = null;
	AttributeXMLSchema.Schema.Trigger.Event.Eventdetail _eventdetail = null;
	AttributeXMLSchema.Schema.Range _range = null;
	AttributeXMLSchema.Schema.Range.Rangedetail _rangedetail = null;
	String _sDBInfo = "";
	Map _hDBInfo = new HashMap();

	public void exportSchema(Context context, String strAdminType, List<String> lstNames, String exportPath,
			String strVersion, Logger schema_done_log, SCMConfigProperty scmConfigProperty) throws Exception{
		
		String name = "";
		String description = "";
		String hidden = "";
		String operator = "";
		List<String> attributes = new ArrayList();
		List<Map> triggers = new ArrayList();
		List<Map> ranges = new ArrayList();
		ObjectFactory objectFactory = new ObjectFactory();
		String[] keysValueType = { "singleValue", "multiValue", "rangeValue" };
		String[] fieldDetail = { "type", "owner", "ownerkind", "multiline", "maxlength", "dimension", "valuetype",
				"default", "resetonclone", "resetonrevision" };
		try {
			exportPath = exportPath + "\\" + SchemaModelerConstants.FOLDER_NAME_SchemaObject;
			SchemaInfo schemaInfo = _schemaFactory.getSchemaObject(strAdminType);
			AttributeXMLSchema axc = objectFactory.createAttributeXMLSchema();
			axc.setName("attribute");
			axc.setType("admin");
			axc.setVersion(strVersion);
			axc.schema = new ArrayList();
			List<AttributeXMLSchema.Schema.Trigger.Event.Eventdetail> lstEvent = null;
			int iCountSchema = 0;
            boolean bLogEverything = false; 
			boolean bSchemaSeperate = false;
			File file = null;
			String filePath = "";
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { AttributeXMLSchema.class });
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
			for (String schema : lstNames) {
				iCountSchema += 1;
				Map mTriggers = new HashMap();
				this._sDBInfo = schemaInfo.geSchemaInfo(context, schema, new String());
				this._hDBInfo = (Map) _gson.readValue(_sDBInfo, HashMap.class);
				this._schema = objectFactory.createAttributeXMLSchemaSchema();
				this._basic = objectFactory.createAttributeXMLSchemaSchemaBasic();
				this._trigger = objectFactory.createAttributeXMLSchemaSchemaTrigger();
				this._basic.setDescription((String) this._hDBInfo.get("description"));
				this._basic.setHidden((String) this._hDBInfo.get("hidden"));
				this._basic.setName((String) this._hDBInfo.get("name"));
				this._basic.setRegistryName((String) this._hDBInfo.get("registryname"));
				this._schema.setBasic(this._basic);
				this._field = objectFactory.createAttributeXMLSchemaSchemaBasicField();
				this._field.setType("attributedetails");

				LinkedHashMap mValueType = (LinkedHashMap) this._hDBInfo.get("valueType");
				String sValue;
				for (String sKey : keysValueType) {
					sValue = (String) mValueType.get(sKey);
					if (sValue.equalsIgnoreCase("true")) {
						this._hDBInfo.put("valuetype", sKey);
						break;
					}
				}
				LinkedHashMap mCloneFlag = (LinkedHashMap) this._hDBInfo.get("resetOn");
				this._hDBInfo.put("resetonclone", (String) mCloneFlag.get("clone"));
				this._hDBInfo.put("resetonrevision", (String) mCloneFlag.get("revision"));
				this._field.detail = new ArrayList();
				for (String detailName : fieldDetail) {
					this._detail = objectFactory.createAttributeXMLSchemaSchemaBasicFieldDetail();
					this._detail.setName(detailName);
					this._detail.setValueAttribute((String) this._hDBInfo.get(detailName));
					//this._field.detail.add(this._detail);
					//Modified for bug 11826 - 
					if(this._hDBInfo.get(detailName) != null && this._hDBInfo.get(detailName).equals("false") && detailName.equals("multiline") && !(this._hDBInfo.get("type").equals("string")) ){
						// Do Nothing
					} else {
						this._field.detail.add(this._detail);
					}
				}
				this._basic.setField(this._field);

				this._range = objectFactory.createAttributeXMLSchemaSchemaRange();
				this._range.rangedetail = new ArrayList();
				ranges = (List) this._hDBInfo.get("ranges");
				for (Map mRange : ranges) {
					List lstValue = (List) mRange.get("value");
					operator = (String) mRange.get("operator");
					this._rangedetail = objectFactory.createAttributeXMLSchemaSchemaRangeRangedetail();
					this._rangedetail.setType(operator);
					this._rangedetail.setValueAttribute(String.join(",", (Iterable) lstValue));
					this._range.rangedetail.add(this._rangedetail);
				}
				this._schema.setRange(this._range);

				triggers = (List) this._hDBInfo.get("trigger");
				String event = "";
				for (Object lstValue = triggers.iterator(); ((Iterator) lstValue).hasNext();) {
					Map mTrigger = (Map) ((Iterator) lstValue).next();
					lstEvent = new ArrayList();
					this._eventdetail = objectFactory.createAttributeXMLSchemaSchemaTriggerEventEventdetail();
					this._eventdetail.setProgram("emxTriggerManager");
					this._eventdetail.setInput((String) mTrigger.get("name"));
					this._eventdetail.setType((String) mTrigger.get("type"));
					event = (String) mTrigger.get("action");
					if (mTriggers.containsKey(event)) {
						lstEvent = (List) mTriggers.get(event);
					}
					lstEvent.add(this._eventdetail);
					mTriggers.put(event, lstEvent);
				}
				Set sTriggers = mTriggers.keySet();
				Object itrsTriggers = sTriggers.iterator();
				String sKey = "";
				this._trigger.event = new ArrayList();
				while (((Iterator) itrsTriggers).hasNext()) {
					this._event = objectFactory.createAttributeXMLSchemaSchemaTriggerEvent();
					sKey = (String) ((Iterator) itrsTriggers).next();
					this._event.setName(sKey);
					this._event.eventdetail = new ArrayList();
					List<AttributeXMLSchema.Schema.Trigger.Event.Eventdetail> lst = (List) mTriggers.get(sKey);
					for (AttributeXMLSchema.Schema.Trigger.Event.Eventdetail eventDetail : lst) {
						this._event.eventdetail.add(eventDetail);
					}
					this._trigger.event.add(this._event);
					this._schema.setTrigger(this._trigger);
				}
				axc.schema.add(this._schema);
				if (bSchemaSeperate) {
					schema = schema.replaceAll(UIUtil.specialCharecters, "");
					file = new File(filePath + "\\" + schema+ ".xml");
					jaxbMarshaller.marshal(axc, file);
					axc.schema = new ArrayList();
				}
				if(bLogEverything) {					
					listSchemaNames.add(schema);
				}
			}
			schema_done_log.info(strAdminType + "|" + iCountSchema);
			if (bLogEverything) {
				schema_done_log.info(listSchemaNames);				
			}
			if (!bSchemaSeperate) {
				file = new File(exportPath + "\\" + strAdminType.toLowerCase() + ".xml");
				jaxbMarshaller.marshal(axc, file);
			}
			
		} catch (Exception e) {
			throw new Exception("Error occurred while exporting schema "+strAdminType+" : " +e.getCause());
		}
	}
}
