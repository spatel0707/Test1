package com.izn.schemamodeler.ui3.webform;

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
import com.izn.schemamodeler.ui3.webform.ObjectFactory;
import com.izn.schemamodeler.ui3.webform.WebFormXMLSchema;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.SchemaModelerConstants;

import matrix.db.Context;
import com.izn.schemamodeler.util.UIUtil;

public class WebFormXMLExport implements SchemaExport {

	String _sDBInfo = "";
	Map _hDBInfo = new HashMap();
	WebFormXMLSchema.Schema _schema = null;
	WebFormXMLSchema.Schema.Basic _basic = null;
	WebFormXMLSchema.Schema.Column _column = null;
	WebFormXMLSchema.Schema.Column.Data _data = null;
	WebFormXMLSchema.Schema.Column.Data.Datadetail _datadetail = null;
	WebFormXMLSchema.Schema.Column.Data.Setting _setting = null;
	WebFormXMLSchema.Schema.Column.Data.Setting.Param _param = null;
	WebFormXMLSchema.Schema.Column.Data.Accessdetail _accessdetail = null;
	WebFormXMLSchema.Schema.Column.Data.Accessdetail.Access _access = null;

	public WebFormXMLExport() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void exportSchema(Context context, String strAdminType, List<String> lstNames, String exportPath,
			String strVersion, Logger schema_done_log, SCMConfigProperty scmConfigProperty) throws Exception{
		ObjectFactory objectFactory = new ObjectFactory();
		String[] detailskeys = { "column", "label", "description", "columnType", "expression", "href", "alt", "range",
				"update", "order" };
		try {
			exportPath = exportPath + "\\" + SchemaModelerConstants.FOLDER_NAME_SchemaObject;
			SchemaInfo schemaInfo = _schemaFactory.getSchemaObject(strAdminType);
			WebFormXMLSchema pxs = objectFactory.createComponent();
			pxs.setName("form");
			pxs.setType("ui3");
			pxs.setVersion(strVersion);
			pxs.schema = new ArrayList<WebFormXMLSchema.Schema>();
			int iCountSchema = 0;
			boolean bSchemaSeperate = false;
			boolean bLogEverything = false;
			File file = null;
			String filePath = "";
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { WebFormXMLSchema.class });
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty("jaxb.formatted.output", Boolean.valueOf(true));
			if ("true".equalsIgnoreCase(scmConfigProperty.getSchemaSeperator())) {
				bSchemaSeperate = true;
				filePath = exportPath + "\\" + "webform";
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
				_schema = objectFactory.createComponentSchema();
				_basic = objectFactory.createComponentSchemaBasic();
				_basic.setDescription((String) _hDBInfo.get("description"));
				_basic.setType((String) _hDBInfo.get("type"));
				_basic.setHidden((String) _hDBInfo.get("hidden"));
				_basic.setName((String) _hDBInfo.get("name"));
				_basic.setRegistryName((String) _hDBInfo.get("registryname"));
				_schema.setBasic(_basic); // set Basic Tag
				_schema.column = new ArrayList<WebFormXMLSchema.Schema.Column>();
				List<Map> lstColumns = (List<Map>) _hDBInfo.get("fields");
				for (Map mColumn : lstColumns) {
					_column = objectFactory.createComponentSchemaColumn();
					_data = objectFactory.createComponentSchemaColumnData();
					_setting = objectFactory.createComponentSchemaColumnDataSetting();
					_accessdetail = objectFactory.createComponentSchemaColumnDataAccessdetail();
					_data.datadetail = new ArrayList<WebFormXMLSchema.Schema.Column.Data.Datadetail>();
					for (String key : detailskeys) {
						_datadetail = objectFactory.createComponentSchemaColumnDataDatadetail();
						_datadetail.setName(key);
						_datadetail.setValueAttribute((String) mColumn.get(key));
						_data.datadetail.add(_datadetail);
					}
					// Object Access
					_accessdetail.access = new ArrayList<WebFormXMLSchema.Schema.Column.Data.Accessdetail.Access>();
					List<LinkedHashMap> lstAccess = (List<LinkedHashMap>) mColumn.get("objectAccess");

					_access = objectFactory.createComponentSchemaColumnDataAccessdetailAccess();
					_access.setName("user");
					List<String> sAcess = new ArrayList<String>();
					if (lstAccess.isEmpty()) {
						sAcess.add("all");
					}
					for (LinkedHashMap m : lstAccess) {
						sAcess.add((String) m.get("name"));
					}
					Map acessMap = new HashMap();
					acessMap.put("name", String.join(",", sAcess));
					_access.setValue((String) acessMap.get("name"));
					_accessdetail.access.add(_access);

					_setting.param = new ArrayList<WebFormXMLSchema.Schema.Column.Data.Setting.Param>();
					List<LinkedHashMap> lstSetting = (List<LinkedHashMap>) mColumn.get("settings");
					for (LinkedHashMap m : lstSetting) {
						_param = objectFactory.createComponentSchemaColumnDataSettingParam();
						_param.setName((String) m.get("name"));
						_param.setValueAttribute((String) m.get("value"));
						_setting.param.add(_param);
					}
					_data.setSetting(_setting);
					_data.setAccessdetail(_accessdetail);
					_column.setType("fielddetails");
					_column.setData(_data);
					_schema.column.add(_column);
				}
				pxs.schema.add(_schema);
				if (bSchemaSeperate) {
					schema = schema.replaceAll(UIUtil.specialCharecters, "");
					file = new File(filePath + "\\" + schema + ".xml");
					jaxbMarshaller.marshal(pxs, file);
					pxs.schema = new ArrayList();
				}
				if(bLogEverything) {					
					listSchemaNames.add(schema);
				}
			}
			schema_done_log.info("webform|" + iCountSchema);
			if (bLogEverything) {
				schema_done_log.info(listSchemaNames);				
			}
			if (!bSchemaSeperate) {
				file = new File(exportPath + "\\webform.xml");
				jaxbMarshaller.marshal(pxs, file);
			}
			
		} catch (Exception e) {
			throw new Exception("Error occurred while exporting schema "+strAdminType+" : " +e.getCause());
		}
	}
}
