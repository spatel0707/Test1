package com.izn.schemamodeler.admin.format;

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
import com.izn.schemamodeler.admin.relationship.RelationshipXMLSchema;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.SchemaModelerConstants;
import matrix.db.Context;
import com.izn.schemamodeler.util.UIUtil;

public class FormatXMLExport implements SchemaExport {
	String _sDBInfo = "";
	Map _hDBInfo = new HashMap();
	FormatXMLSchema.Schema _schema = null;
	FormatXMLSchema.Schema.Basic _basic = null;
	FormatXMLSchema.Schema.Field _field = null;

	FormatXMLSchema.Schema.Field.Detail _detail = null;

	@Override
	public void exportSchema(Context context, String strAdminType, List<String> lstNames, String exportPath,
			String strVersion, Logger schema_done_log, SCMConfigProperty scmConfigProperty) throws Exception{
		// String[] aFields
		// ={"parent","site","maturity","child","roletype","assignment"};
		String[] aFields = { "version", "filesuffix", "filecreator", "filetype" };
		try {
			exportPath = exportPath + "\\" + SchemaModelerConstants.FOLDER_NAME_SchemaObject;
			SchemaInfo schemaInfo = _schemaFactory.getSchemaObject(strAdminType);
			ObjectFactory objectFactory = new ObjectFactory();
			FormatXMLSchema rxs = objectFactory.createComponent();
			rxs.setName("format");
			rxs.setType("admin");
			rxs.setVersion(strVersion);
			rxs.schema = new ArrayList<FormatXMLSchema.Schema>();
			int iCountSchema = 0;
			boolean bSchemaSeperate = false;
			boolean bLogEverything = false;
			File file = null;
			String filePath = "";
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { FormatXMLSchema.class });
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
				_schema = objectFactory.createComponentSchema();
				_basic = objectFactory.createComponentSchemaBasic();
				_sDBInfo = schemaInfo.geSchemaInfo(context, sName, new String());
				_hDBInfo = _gson.readValue(_sDBInfo, HashMap.class);

				_basic.setDescription((String) _hDBInfo.get("description"));
				_basic.setHidden((String) _hDBInfo.get("hidden"));
				_basic.setName((String) _hDBInfo.get("name"));
				// _basic.setRegistryName("");
				_basic.setRegistryName((String) this._hDBInfo.get("registryname"));
				_schema.setBasic(_basic);

				_field = objectFactory.createComponentSchemaField();
				_field.setType("formatdetails");
				_field.detail = new ArrayList<FormatXMLSchema.Schema.Field.Detail>();
				// processFormatDetail(_hDBInfo,"child");
				// processFormatDetail(_hDBInfo,"assignment");
				for (String sKey : aFields) {
					_detail = objectFactory.createComponentSchemaFieldDetail();
					_detail.setName(sKey);
					_detail.setvalueAttribute((String) _hDBInfo.get(sKey));
					_field.detail.add(_detail);
				}
				_schema.setField(_field);
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

	private void processFormatDetail(Map hdbInfo, String key) {
		try {
			List<Map> lstInfo = (List<Map>) hdbInfo.get(key);
			List<String> slData = new ArrayList<String>();
			for (Map m : lstInfo) {
				slData.add((String) m.get("name"));
			}
			hdbInfo.put(key, String.join(",", slData));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
