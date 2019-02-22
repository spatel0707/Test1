package com.izn.schemamodeler.system.store;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.apache.log4j.Logger;
import com.izn.schemamodeler.SchemaExport;
import com.izn.schemamodeler.SchemaInfo;
import com.izn.schemamodeler.system.store.ObjectFactory;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.UIUtil;
import com.izn.schemamodeler.util.SchemaModelerConstants;
import matrix.db.Context;


public class StoreXMLExport implements  SchemaExport {

	Map _hDBInfo = new HashMap();
	String _sDBInfo = "";
	StoreXMLSchema.Object _object = null;
	StoreXMLSchema.Object.Basic _basic = null;
	StoreXMLSchema.Object.Field _field = null;
	StoreXMLSchema.Object.Field.Detail _detail = null;
	String[] fieldDetail = {"type","permission","protocol","port","host","path","user","password","location","fcs","lock"};
	@Override
	public void exportSchema(Context context, String strAdminType, List<String> lsNames, String exportPath,
			String strVersion, Logger schema_done_log, SCMConfigProperty scmConfigProperty) throws Exception {
		// TODO Auto-generated method stub
		File file = null;
		String filePath = "";
		boolean bLogEverything = false;
		boolean bSchemaSeperate = false;
		ObjectFactory objectFactory = new ObjectFactory();
		try {
			exportPath = exportPath+ "\\" + SchemaModelerConstants.FOLDER_NAME_System;
			SchemaInfo schemaInfo = _schemaFactory.getSchemaObject(strAdminType);
			StoreXMLSchema stxs = objectFactory.createComponent();
			stxs.setName(strAdminType);
			stxs.setType("systemobject");
			stxs.setVersion(strVersion);
			stxs.object = new ArrayList<StoreXMLSchema.Object>();
		    JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { StoreXMLSchema.class });
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty("jaxb.formatted.output", Boolean.valueOf(true));
			int iCountSchema = 0;
			List listSchemaNames = new ArrayList();
			if ("true".equalsIgnoreCase(scmConfigProperty.getLogEverything())) {
				bLogEverything = true;			
	        }
			if ("true".equalsIgnoreCase(scmConfigProperty.getSchemaSeperator())) {
				bSchemaSeperate = true;
				filePath = exportPath + "\\" + strAdminType.trim();
				file = new File(filePath);
				if (!file.exists() && !file.isDirectory()) {
					file.mkdir();
				}
			}
			for (String vo : lsNames) {
				iCountSchema += 1;
				_sDBInfo = schemaInfo.geSchemaInfo(context, vo, new String());
				_hDBInfo = _gson.readValue(_sDBInfo, HashMap.class);
				_object = objectFactory.createComponentObject();
				_basic = objectFactory.createComponentObjectBasic();
				_basic.setName((String) _hDBInfo.get("name"));
				_basic.setDescription((String) _hDBInfo.get("description"));
				_basic.setHidden((String) _hDBInfo.get("hidden"));
				_basic.setRegistryName((String) _hDBInfo.get("registryname"));
				_object.setBasic(_basic); // set Basic Tag
				_field = objectFactory.createComponentObjectField();
				_field.setType("basicInfo");
				_field.detail = new ArrayList<StoreXMLSchema.Object.Field.Detail>();
				for (String detailName : fieldDetail) {
					_detail = objectFactory.createComponentObjectFieldDetail();
					_detail.setName(detailName);
					_detail.setValueAttribute((String) _hDBInfo.get(detailName));
					_field.detail.add(_detail);
				}
				
				_object.setField(_field);
				
				stxs.object.add(_object);
				if (bSchemaSeperate) {
					vo = vo.replaceAll(UIUtil.specialCharecters, "");
					file = new File(filePath + "\\" + vo + ".xml");
					jaxbMarshaller.marshal(stxs, file);
					stxs.object = new ArrayList();
				}
				if(bLogEverything) {					
					listSchemaNames.add(vo);
				}
			}
			schema_done_log.info(strAdminType + "|" + iCountSchema);
			if (bLogEverything) {
				schema_done_log.info(listSchemaNames);				
			}
			if (!bSchemaSeperate) {
				file = new File(exportPath + "\\" + strAdminType.toLowerCase() + ".xml");
				jaxbMarshaller.marshal(stxs, file);
			}									
			}
	
		catch (Exception e) {
			throw new Exception("Error occurred while exporting schema "+strAdminType+" : " +e.getCause());
		}	
	}	
}
