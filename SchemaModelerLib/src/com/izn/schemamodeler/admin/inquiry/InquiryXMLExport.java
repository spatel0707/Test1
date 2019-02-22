package com.izn.schemamodeler.admin.inquiry;

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
import com.izn.schemamodeler.admin.inquiry.InquiryXMLSchema;
import com.izn.schemamodeler.admin.inquiry.ObjectFactory;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.SchemaModelerConstants;
import matrix.db.Context;
import com.izn.schemamodeler.util.UIUtil;

public class InquiryXMLExport implements SchemaExport {

	String _sDBInfo = "";
	Map _hDBInfo = new HashMap();
	InquiryXMLSchema.Schema _schema = null;
	InquiryXMLSchema.Schema.Basic _basic = null;
	InquiryXMLSchema.Schema.Field _field = null;
	InquiryXMLSchema.Schema.Field.Detail _detail = null;
	InquiryXMLSchema.Schema.Field.Setting _setting = null;
	InquiryXMLSchema.Schema.Field.Setting.Param _param = null;

	public InquiryXMLExport() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void exportSchema(Context context, String strAdminType, List<String> lstNames, String exportPath,
			String strVersion, Logger schema_done_log, SCMConfigProperty scmConfigProperty) throws Exception{
		ObjectFactory objectFactory = new ObjectFactory();
		String[] detailskeys = { "pattern", "format", "code" };
		try {
			exportPath = exportPath + "\\" + SchemaModelerConstants.FOLDER_NAME_SchemaObject;
			SchemaInfo schemaInfo = _schemaFactory.getSchemaObject(strAdminType);
			InquiryXMLSchema cxs = objectFactory.createInquiry();
			cxs.setName("inquiry");
			cxs.setType("admin");
			cxs.setVersion(strVersion);
			String value = "";
			cxs.schema = new ArrayList<InquiryXMLSchema.Schema>();
			int iCountSchema = 0;
			boolean bSchemaSeperate = false;
			boolean bLogEverything = false;
			File file = null;
			String filePath = "";
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { InquiryXMLSchema.class });
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
				_schema = objectFactory.createInquirySchema();
				_basic = objectFactory.createInquirySchemaBasic();
				_basic.setDescription((String) _hDBInfo.get("description"));
				_basic.setHidden((String) _hDBInfo.get("hidden"));
				_basic.setName((String) _hDBInfo.get("name"));
				_basic.setRegistryName((String) _hDBInfo.get("registryname"));
				_schema.setBasic(_basic); // set Basic Tag
				_field = objectFactory.createInquirySchemaField();
				_field.setType("inquirydetails");

				_field.detail = new ArrayList<InquiryXMLSchema.Schema.Field.Detail>();
				// List<Map> lstOAcess=(List<Map>)_hDBInfo.get("objectAccess");
				// List<String> slOAcess=new ArrayList<String>();
				// for(Map m : lstOAcess){
				// slOAcess.add((String)m.get("name"));
				// }
				// _hDBInfo.put("user", String.join(",",slOAcess));
				for (String key : detailskeys) {
					_detail = objectFactory.createInquirySchemaFieldDetail();
					_detail.setName(key);
					_detail.setValueAttribute((String) _hDBInfo.get(key));
					_field.detail.add(_detail);
				}
				List<Map> settigs = (List<Map>) _hDBInfo.get("argument");
				_setting = objectFactory.createInquirySchemaFieldSetting();
				_setting.param = new ArrayList<InquiryXMLSchema.Schema.Field.Setting.Param>();
				for (Map m : settigs) {
					_param = objectFactory.createInquirySchemaFieldSettingParam();
					_param.setName((String) m.get("name"));
					_param.setValueAttribute((String) m.get("value"));
					_setting.param.add(_param);
				}
				_field.setSetting(_setting);
				_schema.setField(_field);
				cxs.schema.add(_schema);
				if (bSchemaSeperate) {
					schema = schema.replaceAll(UIUtil.specialCharecters, "");
					file = new File(filePath + "\\" + schema + ".xml");
					jaxbMarshaller.marshal(cxs, file);
					cxs.schema = new ArrayList();
				}

				// File file = new File(exportPath + "\\inquiry.xml");
				// JAXBContext jaxbContext = JAXBContext.newInstance(InquiryXMLSchema.class);
				// Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
				// output pretty printed
				// jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				// jaxbMarshaller.marshal(cxs, file);
				// jaxbMarshaller.marshal(cxs, System.out);
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
