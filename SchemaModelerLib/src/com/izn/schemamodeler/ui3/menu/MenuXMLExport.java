package com.izn.schemamodeler.ui3.menu;

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
import com.izn.schemamodeler.ui3.menu.MenuXMLSchema;
import com.izn.schemamodeler.ui3.menu.ObjectFactory;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.SchemaModelerConstants;

import matrix.db.Context;
import com.izn.schemamodeler.util.UIUtil;

public class MenuXMLExport implements SchemaExport {

	String _sDBInfo = "";
	Map _hDBInfo = new HashMap();
	MenuXMLSchema.Schema _schema = null;
	MenuXMLSchema.Schema.Basic _basic = null;
	MenuXMLSchema.Schema.Field _field = null;
	MenuXMLSchema.Schema.Field.Detail _detail = null;
	MenuXMLSchema.Schema.Field.Setting _setting = null;
	MenuXMLSchema.Schema.Field.Setting.Param _param = null;
	MenuXMLSchema.Schema.Field.Items _items = null;
	MenuXMLSchema.Schema.Field.Items.Item _item = null;

	public MenuXMLExport() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void exportSchema(Context context, String strAdminType, List<String> lstNames, String exportPath,
			String strVersion, Logger schema_done_log, SCMConfigProperty scmConfigProperty) throws Exception{
		ObjectFactory objectFactory = new ObjectFactory();
		String[] detailskeys = { "label", "href", "alt" };
		try {
			exportPath = exportPath + "\\" + SchemaModelerConstants.FOLDER_NAME_SchemaObject;
			SchemaInfo schemaInfo = _schemaFactory.getSchemaObject(strAdminType);
			MenuXMLSchema mxs = objectFactory.createComponent();
			mxs.setName("menu");
			mxs.setType("ui3");
			mxs.setVersion(strVersion);
			String value = "";
			mxs.schema = new ArrayList<MenuXMLSchema.Schema>();
			int iCountSchema = 0;
			boolean bSchemaSeperate = false;
			boolean bLogEverything = false;
			File file = null;
			String filePath = "";
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { MenuXMLSchema.class });
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
				_field.setType("menudetails");
				_field.detail = new ArrayList<MenuXMLSchema.Schema.Field.Detail>();
				List<Map> lstItems = (List<Map>) _hDBInfo.get("items");
				_items = objectFactory.createComponentSchemaFieldItems();
				_items.item = new ArrayList<MenuXMLSchema.Schema.Field.Items.Item>();
				for (Map m : lstItems) {
					_item = objectFactory.createComponentSchemaFieldItemsItem();
					_item.setType((String) m.get("type"));
					_item.setValue((String) m.get("name"));
					_items.item.add(_item);
				}
				_field.setItems(_items);
				for (String key : detailskeys) {
					_detail = objectFactory.createComponentSchemaFieldDetail();
					_detail.setName(key);
					_detail.setValueAttribute((String) _hDBInfo.get(key));
					_field.detail.add(_detail);
				}

				List<Map> settigs = (List<Map>) _hDBInfo.get("settings");
				_setting = objectFactory.createComponentSchemaFieldSetting();
				_setting.param = new ArrayList<MenuXMLSchema.Schema.Field.Setting.Param>();
				for (Map m : settigs) {
					_param = objectFactory.createComponentSchemaFieldSettingParam();
					_param.setName((String) m.get("name"));
					_param.setValueAttribute((String) m.get("value"));
					_setting.param.add(_param);
				}
				_field.setSetting(_setting);
				_schema.setField(_field);
				mxs.schema.add(_schema);
				if (bSchemaSeperate) {
					schema = schema.replaceAll(UIUtil.specialCharecters, "");
					file = new File(filePath + "\\" + schema + ".xml");
					jaxbMarshaller.marshal(mxs, file);
					mxs.schema = new ArrayList();
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
				jaxbMarshaller.marshal(mxs, file);
			}
			
		} catch (Exception e) {
			throw new Exception("Error occurred while exporting schema "+strAdminType+" : " +e.getCause());
		}
	}

}
