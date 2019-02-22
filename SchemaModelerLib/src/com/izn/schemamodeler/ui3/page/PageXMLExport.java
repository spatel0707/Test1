package com.izn.schemamodeler.ui3.page;

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
import com.izn.schemamodeler.ui3.page.PageXMLSchema;
import com.izn.schemamodeler.ui3.page.ObjectFactory;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.SchemaModelerConstants;

import matrix.db.Context;
import com.izn.schemamodeler.util.UIUtil;

public class PageXMLExport implements SchemaExport {

	String _sDBInfo = "";
	Map _hDBInfo = new HashMap();
	PageXMLSchema.Schema _schema = null;
	PageXMLSchema.Schema.Basic _basic = null;
	PageXMLSchema.Schema.Field _field = null;
	PageXMLSchema.Schema.Field.Detail _detail = null;

	@Override
	public void exportSchema(Context context, String strAdminType, List<String> lstNames, String exportPath,
			String strVersion, Logger schema_done_log,SCMConfigProperty scmConfigProperty) throws Exception{
		ObjectFactory objectFactory = new ObjectFactory();
		String[] detailskeys = { "mime", "content"};
		try {
			exportPath = exportPath + "\\" + SchemaModelerConstants.FOLDER_NAME_SchemaObject;
			SchemaInfo schemaInfo = _schemaFactory.getSchemaObject(strAdminType);
			PageXMLSchema pxs = objectFactory.createPageXMLSchema();
			pxs.setName("page");
			pxs.setType("ui3");
			pxs.setVersion(strVersion);
			pxs.schema = new ArrayList<PageXMLSchema.Schema>();
			int iCountSchema = 0;
			boolean bSchemaSeperate = false;
			boolean bLogEverything = false;
			File file = null;
			String filePath = "";
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { PageXMLSchema.class });
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty("jaxb.formatted.output", Boolean.valueOf(true));
			if ("true".equalsIgnoreCase(scmConfigProperty.getSchemaSeperator())) {
				bSchemaSeperate = true;
				filePath = exportPath + "\\" + strAdminType.trim();
				file = new File(filePath);
				if (!file.exists() && !file.isDirectory()) {
					file.mkdir();
				}
				exportPath = filePath;
			}
			List listSchemaNames = new ArrayList();
			if ("true".equalsIgnoreCase(scmConfigProperty.getLogEverything())) {
						bLogEverything = true;			
			}
			for (String schema : lstNames) {
				iCountSchema += 1;
				_sDBInfo = schemaInfo.geSchemaInfoWithPath(context, schema, exportPath);
				_hDBInfo = _gson.readValue(_sDBInfo, HashMap.class);
				_schema = objectFactory.createPageXMLSchemaSchema();
				_basic = objectFactory.createPageXMLSchemaSchemaBasic();
				_basic.setDescription((String) _hDBInfo.get("description"));
				_basic.setHidden((String) _hDBInfo.get("hidden"));
				_basic.setName(((String) _hDBInfo.get("name")).trim());
				;
				_schema.setBasic(_basic); // set Basic Tag
				_field = objectFactory.createPageXMLSchemaSchemaField();
				_field.setType("pagedetails");
				_field.detail = new ArrayList<PageXMLSchema.Schema.Field.Detail>();
				for (String key : detailskeys) {
					_detail = objectFactory.createPageXMLSchemaSchemaFieldDetail();
					_detail.setName(key);
					_detail.setValueAttribute((String) _hDBInfo.get(key));
					if (key.equalsIgnoreCase("content")) {
						_detail.setFilepath(((String) _hDBInfo.get("filepath")).trim());
					}
					_field.detail.add(_detail);
				}

				_schema.setField(_field);
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
			schema_done_log.info(strAdminType + "|" + iCountSchema);
			if (bLogEverything) {
				schema_done_log.info(listSchemaNames);				
			}
			if (!bSchemaSeperate) {
				file = new File(exportPath + "\\" + strAdminType.toLowerCase() + ".xml");
				jaxbMarshaller.marshal(pxs, file);
			}
			
		} catch (Exception e) {
			throw new Exception("Error occurred while exporting schema "+strAdminType+" : " +e.getCause());
		}
	}
}
