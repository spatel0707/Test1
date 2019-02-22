package com.izn.schemamodeler.admin.rule;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.izn.schemamodeler.SchemaExport;
import com.izn.schemamodeler.SchemaInfo;

import com.izn.schemamodeler.admin.rule.RuleXMLSchema.Schema;
import com.izn.schemamodeler.admin.rule.RuleXMLSchema.Schema.Field;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.admin.rule.ObjectFactory;

import matrix.db.Context;
import com.izn.schemamodeler.util.UIUtil;

public class RuleXMLExport implements SchemaExport {

	String _sDBInfo = "";
	Map _hDBInfo = new HashMap();
	RuleXMLSchema.Schema _schema = null;
	RuleXMLSchema.Schema.Basic _basic = null;
	RuleXMLSchema.Schema.Field _field = null;
	RuleXMLSchema.Schema.Field.Detail _detail = null;
	Field _accessdetail = null;

	public RuleXMLExport() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void exportSchema(Context context, String strAdminType, List<String> lstNames, String exportPath,
			String strVersion, Logger schema_done_log, SCMConfigProperty scmConfigProperty) throws Exception{
		ObjectFactory objectFactory = new ObjectFactory();
		// String[] detailskeys =
		// {"derived","abstract","attribute","type","relationship"};
		String[] detailskeys = { "governedAttribute", "governedForms", "governedPrograms", "governedRelationships",
				"user" };
		try {
			SchemaInfo schemaInfo = _schemaFactory.getSchemaObject(strAdminType);
			RuleXMLSchema ixs = objectFactory.createRuleXMLSchema();
			ixs.setName("rule");
			ixs.setType("admin");
			ixs.setVersion(strVersion);
			ixs.schema = new ArrayList<RuleXMLSchema.Schema>();
			int iCountSchema = 0;
			boolean bSchemaSeperate = false;
			boolean bLogEverything = false;
			File file = null;
			String filePath = "";
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { RuleXMLSchema.class });
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
				_sDBInfo = schemaInfo.geSchemaInfo(context, schema, new String());
				_hDBInfo = _gson.readValue(_sDBInfo, HashMap.class);
				_schema = objectFactory.createRuleXMLSchemaSchema();
				_basic = objectFactory.createRuleXMLSchemaSchemaBasic();
				_basic.setDescription((String) _hDBInfo.get("description"));
				_basic.setHidden((String) _hDBInfo.get("hidden"));
				_basic.setName((String) _hDBInfo.get("name"));
				_basic.setRegistryName("");
				_schema.setBasic(_basic); // set Basic Tag
				_field = objectFactory.createRuleXMLSchemaSchemaField();
				_field.setType("ruledetails");
				processMap("governedAttribute");
				processMap("governedForms");
				processMap("governedPrograms");
				processMap("governedRelationships");

				_field.detail = new ArrayList<RuleXMLSchema.Schema.Field.Detail>();
				// for(String skey: detailskeys){
				// _detail = objectFactory.createRuleXMLSchemaSchemaFieldDetail();
				// _detail.setName(skey); _detail.setValueAttribute((String)_hDBInfo.get(skey));
				// _field.detail.add(_detail);
				// }
				List<Map> lstOAcess = (List<Map>) _hDBInfo.get("objectAccess");

				List<String> slOAcess = new ArrayList<String>();
				for (Map m : lstOAcess) {
					slOAcess.add((String) m.get("flag"));
					// System.out.println("RuleXML Export line 67..........."+slOAcess);
				}
				_hDBInfo.put("name", String.join(",", slOAcess));
				for (String key : detailskeys) {
					_detail = objectFactory.createRuleXMLSchemaSchemaFieldDetail();
					_detail.setName(key);
					_detail.setValue((String) _hDBInfo.get(key));
					_field.detail.add(_detail);
				}
				_schema.setField(_field);
				_schema.setField(_accessdetail);
				ixs.schema.add(_schema);
				// System.out.println("RuleXML Export line 80..........."+_hDBInfo);

				if (bSchemaSeperate) {
					schema = schema.replaceAll(UIUtil.specialCharecters, "");
					file = new File(filePath + "\\" + schema + ".xml");
					jaxbMarshaller.marshal(ixs, file);
					ixs.schema = new ArrayList();
				}
				if(bLogEverything) {					
					listSchemaNames.add(schema);
				}
				// File file = new File(exportPath+"\\rule.xml");
				// JAXBContext jaxbContext = JAXBContext.newInstance(RuleXMLSchema.class);
				// Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
				// output pretty printed
				// jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				// jaxbMarshaller.marshal(ixs, file);
			}
			schema_done_log.info(strAdminType + "|" + iCountSchema);
			if (bLogEverything) {
				schema_done_log.info(listSchemaNames);				
			}
			if (!bSchemaSeperate) {
				file = new File(exportPath + "\\" + strAdminType.toLowerCase() + ".xml");
				jaxbMarshaller.marshal(ixs, file);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void processMap(String key) {
		List<String> lstData = (List<String>) _hDBInfo.get(key);
		String value = "";
		if (!lstData.isEmpty())
			value = String.join(",", lstData);
		_hDBInfo.put(key, value);
	}
}
