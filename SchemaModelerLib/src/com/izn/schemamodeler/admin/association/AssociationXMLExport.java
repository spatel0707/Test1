package com.izn.schemamodeler.admin.association;

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
import com.izn.schemamodeler.admin.association.AssociationXMLSchema;
import com.izn.schemamodeler.admin.association.ObjectFactory;
import com.izn.schemamodeler.util.SCMConfigProperty;
import matrix.db.Context;
import com.izn.schemamodeler.util.UIUtil;
import com.izn.schemamodeler.util.SchemaModelerConstants;

public class AssociationXMLExport  implements SchemaExport{ 

	 String _sDBInfo="";
	 Map _hDBInfo= new HashMap();
	 AssociationXMLSchema.Schema _schema=null;
	 AssociationXMLSchema.Schema.Basic _basic=null;
	 AssociationXMLSchema.Schema.Field _field=null;
	 AssociationXMLSchema.Schema.Field.Detail _detail=null;
     public AssociationXMLExport() {
		// TODO Auto-generated constructor stub
	 }
		
	 @Override
	 public void exportSchema(Context context,String strAdminType,List<String> lstNames,String exportPath, String strVersion, Logger schema_done_log, SCMConfigProperty scmConfigProperty) throws Exception{
		 	ObjectFactory  objectFactory= new ObjectFactory();
		 	String[] detailskeys = {"definition"};		 	
		 	try {
				 exportPath = exportPath + "\\" + SchemaModelerConstants.FOLDER_NAME_SchemaObject;
				 SchemaInfo  schemaInfo  =_schemaFactory.getSchemaObject(strAdminType);
				 AssociationXMLSchema axs = objectFactory.createComponent();
				 axs.setName("association"); axs.setType("admin"); axs.setVersion(strVersion);
				 axs.schema =new ArrayList<AssociationXMLSchema.Schema>();
				 int iCountSchema = 0;
				 boolean bSchemaSeperate = false;
				 boolean bLogEverything = false;
				 File file = null;
				 String filePath = "";
				 JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { AssociationXMLSchema.class });
				 Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
				 jaxbMarshaller.setProperty("jaxb.formatted.output", Boolean.valueOf(true));
				 if("true".equalsIgnoreCase(scmConfigProperty.getSchemaSeperator())){
					 bSchemaSeperate = true;
					 filePath = exportPath + "\\"+ strAdminType.trim();
					 file = new File(filePath);
					 if(!file.exists() && !file.isDirectory()){
						 file.mkdir();
					 }
				 }
				 List listSchemaNames = new ArrayList();
				 if ("true".equalsIgnoreCase(scmConfigProperty.getLogEverything())) {
					bLogEverything = true;			
				}
				 for(String schema : lstNames){
					 iCountSchema += 1;
			 		 _sDBInfo = schemaInfo.geSchemaInfo(context, schema,new String());
			 		_hDBInfo = _gson.readValue(_sDBInfo, HashMap.class);
			 	 	 _schema = objectFactory.createComponentSchema();
			 		 _basic = objectFactory.createComponentSchemaBasic();
			 	 	 _basic.setDescription((String)_hDBInfo.get("description"));_basic.setHidden((String)_hDBInfo.get("hidden"));_basic.setName((String)_hDBInfo.get("name"));_basic.setRegistryName((String)_hDBInfo.get("registryname"));
					 _schema.setBasic(_basic); // set Basic Tag 
					 _field = objectFactory.createComponentSchemaField();
					 _field.setType("associationdetails");
					 _field.detail = new ArrayList<AssociationXMLSchema.Schema.Field.Detail>();
					  for(String skey: detailskeys){
						  _detail =   objectFactory.createComponentSchemaFieldDetail();
						  _detail.setName(skey); _detail.setValueAttribute((String)_hDBInfo.get(skey));
						  _field.detail.add(_detail);
					  }
					  _schema.setField(_field);
					  axs.schema.add(_schema);
					  if(bSchemaSeperate){
						  schema = schema.replaceAll(UIUtil.specialCharecters, "");
						  file = new File(filePath + "\\"+schema+".xml");
						  jaxbMarshaller.marshal(axs, file);
						  axs.schema = new ArrayList();
					  }
					  if(bLogEverything) {					
						listSchemaNames.add(schema);
					  }
				 }
					schema_done_log.info(strAdminType+"|"+iCountSchema); 
				 if (bLogEverything) {
					schema_done_log.info(listSchemaNames);				
				 }
				 if(!bSchemaSeperate){
					 file = new File(exportPath + "\\" +strAdminType.toLowerCase()+".xml");
					 jaxbMarshaller.marshal(axs, file);
				 }
			     
		     } catch(Exception e){
				throw new Exception("Error occurred while exporting schema "+strAdminType+" : " +e.getCause());
		     }
        }
	}
