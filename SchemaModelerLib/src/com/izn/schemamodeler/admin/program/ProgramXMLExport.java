package com.izn.schemamodeler.admin.program;

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
import com.izn.schemamodeler.admin.program.ProgramXMLSchema;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.admin.program.ObjectFactory;
import com.izn.schemamodeler.util.SchemaModelerConstants;
import matrix.db.Context;
import com.izn.schemamodeler.util.UIUtil;

public class ProgramXMLExport implements SchemaExport {

	String _sDBInfo = "";
	Map _hDBInfo = new HashMap();
	ProgramXMLSchema.Schema _schema = null;
	ProgramXMLSchema.Schema.Basic _basic = null;
	ProgramXMLSchema.Schema.Field _field = null;
	ProgramXMLSchema.Schema.Field.Detail _detail = null;

	public ProgramXMLExport() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void exportSchema(Context context, String strAdminType, List<String> lstNames, String exportPath,
			String strVersion, Logger schema_done_log,SCMConfigProperty scmConfigProperty) throws Exception{

		ObjectFactory objectFactory = new ObjectFactory();
		String[] detailskeys = { "code", "type", "user", "execute", "needsbusinessobject", "downloadable", "pipe",
				"pooled" };
		String[] programtype = { "java", "mql", "external" };
		String[] execute = { "immediate", "deferred" };
		try {
			exportPath = exportPath + "\\" + SchemaModelerConstants.FOLDER_NAME_SchemaObject;
			SchemaInfo schemaInfo = _schemaFactory.getSchemaObject(strAdminType);
			ProgramXMLSchema pxs = objectFactory.createComponent();
			pxs.setName("program");
			pxs.setType("admin");
			pxs.setVersion(strVersion);
			String value = "";
			pxs.schema = new ArrayList<ProgramXMLSchema.Schema>();
			int iCountSchema = 0;
			boolean bSchemaSeperate = false;
			boolean bLogEverything = false;
			File file = null;
			String filePath = "";
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { ProgramXMLSchema.class });
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
				_schema = objectFactory.createComponentSchema();
				_basic = objectFactory.createComponentSchemaBasic();
				_basic.setDescription((String) _hDBInfo.get("description"));
				_basic.setHidden((String) _hDBInfo.get("hidden"));
				_basic.setName(schema);
				_basic.setRegistryName("");
				_schema.setBasic(_basic); // set Basic Tag
				_field = objectFactory.createComponentSchemaField();
				_field.setType("programdetails");
				Map mPtype = (Map) _hDBInfo.get("type");
				for (String key : programtype) {
					value = (String) mPtype.get(key);
					if (value.equalsIgnoreCase("true")) {
						_hDBInfo.put("type", key);
						break;
					}
				}
				Map mExecute = (Map) _hDBInfo.get("execute");
				for (String key : execute) {
					value = (String) mExecute.get(key);
					if (value.equalsIgnoreCase("true")) {
						_hDBInfo.put("execute", key);
						break;
					}
				}
				_field.detail = new ArrayList<ProgramXMLSchema.Schema.Field.Detail>();
				for (String key : detailskeys) {
					_detail = objectFactory.createComponentSchemaFieldDetail();
					_detail.setName(key);
					if (key.equalsIgnoreCase("code")) {
						//_detail.setFilepath((String) _hDBInfo.get("filepath"));
						_detail.setValueAttribute("");
					} else {
						_detail.setValueAttribute((String) _hDBInfo.get(key));
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
			schema_done_log.info(strAdminType+"|"+iCountSchema);
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
