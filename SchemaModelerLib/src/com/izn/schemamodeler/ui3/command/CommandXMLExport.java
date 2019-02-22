package com.izn.schemamodeler.ui3.command;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.izn.schemamodeler.SchemaExport;
import com.izn.schemamodeler.SchemaInfo;
import com.izn.schemamodeler.ui3.command.CommandXMLSchema;
import com.izn.schemamodeler.ui3.command.ObjectFactory;
import com.izn.schemamodeler.ui3.webform.WebFormXMLSchema;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.SchemaModelerConstants;

import matrix.db.Context;
import com.izn.schemamodeler.util.UIUtil;

public class CommandXMLExport implements SchemaExport {

	String _sDBInfo = "";
	Map _hDBInfo = new HashMap();
	CommandXMLSchema.Schema _schema = null;
	CommandXMLSchema.Schema.Basic _basic = null;
	CommandXMLSchema.Schema.Field _field = null;
	CommandXMLSchema.Schema.Field.Detail _detail = null;
	CommandXMLSchema.Schema.Field.Setting _setting = null;
	CommandXMLSchema.Schema.Field.Setting.Param _param = null;
	CommandXMLSchema.Schema.Field.Accessdetail _acessdetail = null;
	CommandXMLSchema.Schema.Field.Accessdetail.Access _access = null;

	public CommandXMLExport() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void exportSchema(Context context, String strAdminType, List<String> lstNames, String exportPath,
			String strVersion, Logger schema_done_log, SCMConfigProperty scmConfigProperty) throws Exception{

		ObjectFactory objectFactory = new ObjectFactory();
		String[] detailskeys = { "label", "href", "alt", "code" };
		try {
			exportPath = exportPath + "\\" + SchemaModelerConstants.FOLDER_NAME_SchemaObject;
			SchemaInfo schemaInfo = _schemaFactory.getSchemaObject(strAdminType);
			CommandXMLSchema cxs = objectFactory.createComponent();
			cxs.setName("comamnd");
			cxs.setType("ui3");
			cxs.setVersion(strVersion);
			String value = "";
			cxs.schema = new ArrayList<CommandXMLSchema.Schema>();
			int iCountSchema = 0;
			boolean bSchemaSeperate = false;
			boolean bLogEverything = false;
			File file = null;
			String filePath = "";
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { CommandXMLSchema.class });
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
				_sDBInfo = schemaInfo.geSchemaInfoWithPath(context, schema, exportPath);
				_hDBInfo = _gson.readValue(_sDBInfo, HashMap.class);
				_schema = objectFactory.createComponentSchema();
				_basic = objectFactory.createComponentSchemaBasic();
				_acessdetail = objectFactory.createComponentSchemaFieldAccessdetail();
				_basic.setDescription((String) _hDBInfo.get("description"));
				_basic.setHidden((String) _hDBInfo.get("hidden"));
				_basic.setName((String) _hDBInfo.get("name"));
				_basic.setRegistryName((String) _hDBInfo.get("registryname"));
				_schema.setBasic(_basic); // set Basic Tag
				_field = objectFactory.createComponentSchemaField();
				_field.setType("commanddetails");
				_field.detail = new ArrayList<CommandXMLSchema.Schema.Field.Detail>();

				// for label,href,alt,code
				for (String key : detailskeys) {
					_detail = objectFactory.createComponentSchemaFieldDetail();
					_detail.setName(key);
					_detail.setValueAttribute((String) _hDBInfo.get(key));
					_field.detail.add(_detail);
				}

				List<Map> settigs = (List<Map>) _hDBInfo.get("settings");
				_setting = objectFactory.createComponentSchemaFieldSetting();
				_setting.param = new ArrayList<CommandXMLSchema.Schema.Field.Setting.Param>();
				for (Map m : settigs) {
					_param = objectFactory.createComponentSchemaFieldSettingParam();
					_param.setName((String) m.get("name"));
					_param.setValueAttribute((String) m.get("value"));
					_setting.param.add(_param);
				}
				// ObjectAcess
				_acessdetail.access = new ArrayList<CommandXMLSchema.Schema.Field.Accessdetail.Access>();
				List<LinkedHashMap> lstAccess = (List<LinkedHashMap>) _hDBInfo.get("objectAccess");
				_access = objectFactory.createComponentSchemaFieldAccessdetailAccess();
				_access.setName("user");
				List<String> sAccess = new ArrayList<String>();
				for (LinkedHashMap m : lstAccess) {
					sAccess.add((String) m.get("name"));
				}
				Map acessMap = new HashMap();
				if (sAccess.isEmpty()) {
					sAccess.add("all");
				}
				acessMap.put("name", String.join(",", sAccess));
				_access.setValue((String) acessMap.get("name"));
				_acessdetail.access.add(_access);

				_field.setAccessdetail(_acessdetail);
				_field.setSetting(_setting);
				_schema.setField(_field);
				cxs.schema.add(_schema);
				if (bSchemaSeperate) {
					schema = schema.replaceAll(UIUtil.specialCharecters, "");
					file = new File(filePath + "\\" + schema+ ".xml");
					jaxbMarshaller.marshal(cxs, file);
					cxs.schema = new ArrayList();
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
				jaxbMarshaller.marshal(cxs, file);
			}
			
		} catch (Exception e) {
			throw new Exception("Error occurred while exporting schema "+strAdminType+" : " +e.getCause());
		}
	}
}
