package com.izn.schemamodeler.admin.group;

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
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.SchemaModelerConstants;
import matrix.db.Context;
import com.izn.schemamodeler.util.UIUtil;

public class GroupXMLExport implements SchemaExport {
	String _sDBInfo = "";
	Map _hDBInfo = new HashMap();
	GroupXMLSchema.Schema _schema = null;
	GroupXMLSchema.Schema.Basic _basic = null;
	GroupXMLSchema.Schema.Field _field = null;

	GroupXMLSchema.Schema.Field.Detail _detail = null;

	@Override
	public void exportSchema(Context context, String strAdminType, List<String> lstNames, String exportPath,
			String strVersion, Logger schema_done_log, SCMConfigProperty scmConfigProperty) throws Exception{
		String[] aFields = { "parent", "site", "child", "assignment", "iconFile" };
		try {
			exportPath = exportPath + "\\" + SchemaModelerConstants.FOLDER_NAME_SchemaObject;
			SchemaInfo schemaInfo = _schemaFactory.getSchemaObject(strAdminType);
			// System.out.println("schemaInfo***********"+schemaInfo);
			ObjectFactory objectFactory = new ObjectFactory();
			GroupXMLSchema rxs = objectFactory.createComponent();
			rxs.setName("group");
			rxs.setType("admin");
			rxs.setVersion(strVersion);
			rxs.schema = new ArrayList<GroupXMLSchema.Schema>();
			// System.out.println("lstNames is ............."+lstNames);
			int iCountSchema = 0;
			boolean bSchemaSeperate = false;
			boolean bLogEverything = false;
			File file = null;
			String filePath = "";
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { GroupXMLSchema.class });
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
				// System.out.println("SNAme in GroupXMLExport is "+sName);
				iCountSchema += 1;
				_schema = objectFactory.createComponentSchema();
				_basic = objectFactory.createComponentSchemaBasic();
				_sDBInfo = schemaInfo.geSchemaInfo(context, sName, new String());
				_hDBInfo = _gson.readValue(_sDBInfo, HashMap.class);
				// System.out.println("_hDBInfo............."+_hDBInfo);
				_basic.setDescription((String) _hDBInfo.get("description"));
				_basic.setHidden((String) _hDBInfo.get("hidden"));
				_basic.setName((String) _hDBInfo.get("name"));
				_basic.setRegistryName((String) _hDBInfo.get("registryname"));
				_schema.setBasic(_basic);
				_field = objectFactory.createComponentSchemaField();
				_field.setType("groupdetails");
				_field.detail = new ArrayList<GroupXMLSchema.Schema.Field.Detail>();
				processGroupDetail(_hDBInfo, "child");
				processGroupDetail(_hDBInfo, "assignment");
				for (String sKey : aFields) {
					_detail = objectFactory.createComponentSchemaFieldDetail();
					_detail.setName(sKey);
					_detail.setValueAttribute((String) _hDBInfo.get(sKey));
					_field.detail.add(_detail);
				}
				_schema.setField(_field);
				rxs.schema.add(_schema);

				String name = (String) _hDBInfo.get("name");
				String description = (String) _hDBInfo.get("description");
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

	private void processGroupDetail(Map hdbInfo, String key) {
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
