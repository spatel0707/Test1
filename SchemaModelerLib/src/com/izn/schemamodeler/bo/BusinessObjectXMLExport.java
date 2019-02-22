package com.izn.schemamodeler.bo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.izn.schemamodeler.SchemaBusExport;
import com.izn.schemamodeler.SchemaExport;
import com.izn.schemamodeler.SchemaInfo;
import com.izn.schemamodeler.admin.attribute.AttributeXMLSchema;
import com.izn.schemamodeler.bo.BusinessObjectXMLSchema.Object;
import com.izn.schemamodeler.bo.ObjectFactory;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.matrixone.apps.domain.util.MqlUtil;
import matrix.db.Context;
import matrix.util.StringList;
import com.izn.schemamodeler.util.SchemaModelerConstants;

public class BusinessObjectXMLExport implements SchemaBusExport {

	BusinessObjectXMLSchema.Object _object = null;
	BusinessObjectXMLSchema.Object.Basic _basic = null;
	BusinessObjectXMLSchema.Object.Basic.Field _field = null;
	BusinessObjectXMLSchema.Object.Basic.Field.Detail _detail = null;
	BusinessObjectXMLSchema.Object.AttributeInfo _attrInfo = null;
	BusinessObjectXMLSchema.Object.AttributeInfo.AttrDetail _attrdetail = null;
	String _sDBInfo = "";
	Map _hDBInfo = new HashMap();

	public BusinessObjectXMLExport() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void exportBus(Context context, String strType, List<String> lstNames, String exportPath, String strVersion,
			String sFile, String sExpandRel, Logger schema_done_log, SCMConfigProperty scmConfigProperty) throws Exception{

		// List<String> attributes=new ArrayList<String>();
		List<String> attributes = new ArrayList<String>();

		ObjectFactory objectFactory = new ObjectFactory();
		String[] fieldDetail = { "policy", "vault", "state", "owner", "description", "filePath", "originated",
				"modified", "grantee" };

		try {
			exportPath = exportPath+ "\\" + SchemaModelerConstants.FOLDER_NAME_BusinessObject;
			SchemaInfo schemaInfo = _schemaFactory.getSchemaObject("businessobject");
			BusinessObjectXMLSchema bxs = objectFactory.createBusinessObjectXMLSchema();
			bxs.setName(strType);
			bxs.setType("businessobject");
			bxs.setVersion(strVersion);
			bxs.object = new ArrayList<BusinessObjectXMLSchema.Object>();
			int iCountSchema = 0;
			boolean bLogEverything = false;
			StringList list = new StringList();
			if ("true".equalsIgnoreCase(scmConfigProperty.getLogEverything())) {
				bLogEverything = true;
			}
			for (String bo : lstNames) {
				iCountSchema += 1;
				_sDBInfo = schemaInfo.geSchemaInfo(context, bo, new String());
				//_hDBInfo = _gson.fromJson(_sDBInfo, HashMap.class);
				_hDBInfo = _gson.readValue(_sDBInfo, HashMap.class);
				_object = objectFactory.createBusinessObjectXMLSchemaObject();
				_basic = objectFactory.createBusinessObjectXMLSchemaObjectBasic();
				_basic.setType((String) _hDBInfo.get("type"));
				_basic.setName((String) _hDBInfo.get("name"));
				_basic.setRevision((String) _hDBInfo.get("revision"));
				_object.setBasic(_basic); // set Basic Tag
				_field = objectFactory.createBusinessObjectXMLSchemaObjectBasicField();
				_field.setType("basicInfo");
				_field.detail = new ArrayList<BusinessObjectXMLSchema.Object.Basic.Field.Detail>();
				for (String detailName : fieldDetail) {
					_detail = objectFactory.createBusinessObjectXMLSchemaObjectBasicFieldDetail();
					_detail.setName(detailName);
					_detail.setValueAttribute((String) _hDBInfo.get(detailName));
					_field.detail.add(_detail);
				}
				_basic.setField(_field);
				// process attribute
				_attrInfo = objectFactory.createBusinessObjectXMLSchemaObjectAttributeInfo();
				_attrInfo.attribute = new ArrayList<BusinessObjectXMLSchema.Object.AttributeInfo.AttrDetail>();
				attributes = (List<String>) _hDBInfo.get("attributes");
				for (String mAttribute : attributes) {
					_attrdetail = objectFactory.createBusinessObjectXMLSchemaObjectAttributeInfoAttrdetail();
					_attrdetail.setName(mAttribute);
					_attrdetail.setValueAttribute((String) _hDBInfo.get(mAttribute));
					_attrInfo.attribute.add(_attrdetail);
				}
				_object.setAttributes(_attrInfo);
				bxs.object.add(_object);
				String sMQLResult = MqlUtil.mqlCommand(context, "print bus $1 select $2 $3 $4 dump $5",new String[] { bo, "type", "name", "revision", "|" });
				list.add('\n' + sMQLResult);
			}
			if (bLogEverything) {
				schema_done_log.info(list.toString().replace("|", ",").replace("[", "").replace("]", "."));
			}
			if(strType.contains("*")){
				strType = strType.replace("*", "");
			}
			//File file = new File(exportPath + "\\bo_" + strType.replaceAll(" ", "") + ".xml");
			File file = new File(exportPath + "\\bo_" + strType.toLowerCase() + ".xml");
			JAXBContext jaxbContext = JAXBContext.newInstance(BusinessObjectXMLSchema.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(bxs, file);
			schema_done_log.info("EXPORT TOTAL "+strType+" : "+iCountSchema);
		} catch (Exception e) {
			throw new Exception("Error occurred while exporting schema "+strType+" : " +e.getCause());
		}
	}
	
	@Override
	public Map<String,Set<String>> exportRel(Context context, String strType, List<String> lstNames, String exportPath, String strVersion,String sFile, String sExpandRel, Logger schema_done_log, SCMConfigProperty scmConfigProperty) throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	private void processMap(String key) {
		List<String> lstData = (List<String>) _hDBInfo.get(key);
		String value = "";
		if (!lstData.isEmpty())
			value = String.join(",", lstData);
		_hDBInfo.put(key, value);
	}
}
