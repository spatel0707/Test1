package com.izn.schemamodeler.admin.dimension;

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
import com.izn.schemamodeler.admin.attribute.AttributeXMLSchema;
import com.izn.schemamodeler.admin.dimension.DimensionXMLSchema;
import com.izn.schemamodeler.admin.dimension.ObjectFactory;
import com.izn.schemamodeler.admin.interfaces.InterfaceXMLSchema;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.SchemaModelerConstants;
import matrix.db.Context;
import com.izn.schemamodeler.util.UIUtil;

public class DimensionXMLExport implements SchemaExport {

	String _sDBInfo = "";
	Map _hDBInfo = new HashMap();
	DimensionXMLSchema.Schema _schema = null;
	DimensionXMLSchema.Schema.Basic _basic = null;
	DimensionXMLSchema.Schema.Field _field = null;
	DimensionXMLSchema.Schema.Field.Data _data = null;
	DimensionXMLSchema.Schema.Field.Data.Datadetail _datadetail = null;
	DimensionXMLSchema.Schema.Field.Data.Setting _setting = null;
	DimensionXMLSchema.Schema.Field.Data.Setting.Param _param = null;

	public DimensionXMLExport() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void exportSchema(Context context, String strAdminType, List<String> lstNames, String exportPath,
			String strVersion, Logger schema_done_log, SCMConfigProperty scmConfigProperty) throws Exception{
				
		ObjectFactory objectFactory = new ObjectFactory();
		String[] detailskeys = { "dbunit", "unit", "label", "unitdescription", "default", "multiplier", "offset" };
		try {
			exportPath = exportPath + "\\" + SchemaModelerConstants.FOLDER_NAME_SchemaObject;
			SchemaInfo schemaInfo = _schemaFactory.getSchemaObject(strAdminType);
			DimensionXMLSchema dxs = objectFactory.createComponent();
			dxs.setName("dimension");
			dxs.setType("admin");
			dxs.setVersion(strVersion);
			dxs.schema = new ArrayList<DimensionXMLSchema.Schema>();
			int iCountSchema = 0;
			boolean bSchemaSeperate = false;
			boolean bLogEverything = false;
			File file = null;
			String filePath = "";
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { DimensionXMLSchema.class });
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
				_schema = objectFactory.createComponentSchema();
				_basic = objectFactory.createComponentSchemaBasic();
				_basic.setDescription((String) _hDBInfo.get("description"));
				_basic.setHidden((String) _hDBInfo.get("hidden"));
				_basic.setName((String) _hDBInfo.get("name"));
				_basic.setRegistryName((String) _hDBInfo.get("registryname"));
				_schema.setBasic(_basic); // set Basic Tag
				_field = objectFactory.createComponentSchemaField();
				_field.setType("dimensionedetails");
				_field.data = new ArrayList<DimensionXMLSchema.Schema.Field.Data>();
				List<Map> lstUnits = (List<Map>) _hDBInfo.get("units");
				for (Map m : lstUnits) {
					_data = objectFactory.createComponentSchemaFieldData();
					_data.datadetail = new ArrayList<DimensionXMLSchema.Schema.Field.Data.Datadetail>();
					for (String key : detailskeys) {
						_datadetail = objectFactory.createComponentSchemaFieldDataDatadetail();
						_datadetail.setName(key);
						_datadetail.setValueAttribute((String) m.get(key));
						_data.datadetail.add(_datadetail);
					}
					_setting = objectFactory.createComponentSchemaFieldSetting();
					List<Map> lstSetting = (List<Map>) m.get("setting");
					_setting.param = new ArrayList<DimensionXMLSchema.Schema.Field.Data.Setting.Param>();
					for (Map setting : lstSetting) {
						_param = objectFactory.createComponentSchemaFieldSettingParam();
						_param.setName((String) setting.get("name"));
						_param.setValueAttribute((String) setting.get("value"));
						_setting.param.add(_param);
					}
					_data.setSetting(_setting);
					_field.data.add(_data);
				}
				_schema.setField(_field);
				dxs.schema.add(_schema);
				if (bSchemaSeperate) {
					schema = schema.replaceAll(UIUtil.specialCharecters, "");
					file = new File(filePath + "\\" + schema + ".xml");
					jaxbMarshaller.marshal(dxs, file);
					dxs.schema = new ArrayList();
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
				file = new File(exportPath + "\\" + strAdminType.toLowerCase()+ ".xml");
				jaxbMarshaller.marshal(dxs, file);
			}
			
		} catch (Exception e) {
			throw new Exception("Error occurred while exporting schema "+strAdminType+" : " +e.getCause());
		}
	}
}
