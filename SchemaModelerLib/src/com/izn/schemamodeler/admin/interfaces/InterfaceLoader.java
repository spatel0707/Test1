package com.izn.schemamodeler.admin.interfaces;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.transform.stream.StreamSource;

import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.izn.schemamodeler.SchemaFactoryInfo;
import com.izn.schemamodeler.SchemaLoader;
import com.izn.schemamodeler.admin.interfaces.Interfaces;
import com.izn.schemamodeler.admin.interfaces.InterfaceInfo;
import com.izn.schemamodeler.admin.interfaces.InterfaceXMLSchema;
import com.izn.schemamodeler.admin.interfaces.InterfaceXMLSchema.Schema;
import com.izn.schemamodeler.admin.interfaces.InterfaceXMLSchema.Schema.Basic;
import com.izn.schemamodeler.admin.interfaces.InterfaceXMLSchema.Schema.Field;
import com.izn.schemamodeler.admin.interfaces.InterfaceXMLSchema.Schema.Field.Detail;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class InterfaceLoader implements SchemaLoader {

	ObjectMapper _gson = new ObjectMapper();
	Basic _basicElem = null;
	Field _fieldElem = null;

	public InterfaceLoader() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void loadSchema(Context context, String strSchemaName, String fileName, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty)  throws Exception{
		try {
			List<Interfaces> lstInterfaces = new ArrayList<Interfaces>();
			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
			InterfaceInfo interfacesInfo = (InterfaceInfo) schemaFactory.getSchemaObject("interface");
			JAXBContext jConext = JAXBContext.newInstance(InterfaceXMLSchema.class);
			Unmarshaller unmarshaller = jConext.createUnmarshaller();
			JAXBElement<InterfaceXMLSchema> interfacesElem = unmarshaller
					.unmarshal(new StreamSource(new File(fileName)), InterfaceXMLSchema.class);
			InterfaceXMLSchema interfacesXMLSchema = interfacesElem.getValue();
			List<Schema> lstSchema = interfacesXMLSchema.getSchema();
			Interfaces interfaces = null;
			String fValue = "";
			String fName = "";
			Map<String, String> mSettings = null;
			List<Map<String, String>> lstSetting = null;
			List<String> lstInterfacesItem = null;
			List<String> lstCommandItem = null;
			for (Schema _schema : lstSchema) {
				lstSetting = new ArrayList<Map<String, String>>();
				lstCommandItem = new ArrayList<String>();
				lstInterfacesItem = new ArrayList<String>();
				_basicElem = _schema.getBasic();
				interfaces = new Interfaces(_basicElem.getName(), _basicElem.getDescription(), _basicElem.getHidden(),
						_basicElem.getRegistryName());
				_fieldElem = _schema.getField();
				List<Detail> lstDetail = _fieldElem.getDetail();
				for (Detail _detail : lstDetail) {
					fName = _detail.getName();
					fValue = _detail.getValueAttribute();
					if (fValue != null) {
						fValue = _detail.getValueAttribute().trim();
					} else {
						fValue = "";
					}
					if (fName.equalsIgnoreCase("derived")) {
						interfaces.setDerived(fValue.trim());
					} else if (fName.equalsIgnoreCase("abstract")) {
						interfaces.setSabstract(fValue.trim());
					} else if (fName.equalsIgnoreCase("attribute")) {
						interfaces.setAttribute(fValue.trim());
					} else if (fName.equalsIgnoreCase("type")) {
						interfaces.setType(fValue.trim());
					} else if (fName.equalsIgnoreCase("relationship")) {
						interfaces.setRelationship(fValue.trim());
					}

				}
				lstInterfaces.add(interfaces);
			}
			prepareInterfacesMQL(context, lstInterfaces, schema_done_log, scmConfigProperty);
		} catch (UnmarshalException ume) {
			schema_done_log.error("UnmarshalException on ["+strSchemaName+".xml] : "+ume.getCause());
			schema_done_log.warn(UIUtil.sUnmarshalExceptionWarningMessage);
			throw ume;
		} catch (Exception e) {
			schema_done_log.error(""+e.getCause());
			throw e;
		}
	}

	private void prepareInterfacesMQL(Context context, List<Interfaces> lstInterfaces, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty) throws Exception{
		boolean bMQLResult;
		int iCountTotal = 0;
		int iCountSuccess = 0;
		int iCountFailure = 0;
		int iCountAdd = 0;
		int iCountModify = 0;
		int iCountDelete = 0;
		String sMQL = "";
		String sOperation = "";
		String sMQLPropertyQuery = "";
		MQLCommand localMQLCommand = null;
		String strInterfaceName = "";
		List<String> listModified = new ArrayList<String>();
		List<String> listAdded = new ArrayList<String>();
		List<String> listDeleted = new ArrayList<String>();
		try {
			iCountTotal = lstInterfaces.size();
			for (Interfaces interfaces : lstInterfaces) {		
				schema_done_log.info("Interface : [" + interfaces.name.replace(UIUtil.removeCharecter, "") + "]");
				strInterfaceName = interfaces.name;
				ContextUtil.pushContext(context);
				try {
					String removeSchemaObject = UIUtil.removeSchemaObject(strInterfaceName, "interface");
					if (removeSchemaObject.isEmpty()) {
						String strResult = MQLCommand.exec(context, "list interface $1",
								new String[] { strInterfaceName.replace(UIUtil.removeCharecter, "") });
						if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
							sMQL = prepareModifyExistingSchemaMQL(interfaces, schema_done_log);
							iCountModify += 1;
							listModified.add(strInterfaceName);
							sOperation = "mod";
						} else {
							sMQL = prepareAddNewSchemaMQL(interfaces, schema_done_log);
							iCountAdd += 1;
							listAdded.add(strInterfaceName);
							sOperation = "add";
						}
						sMQLPropertyQuery = UIUtil.getMQLPropertyQuery(context, "interface", interfaces.name,
								interfaces.registryname,sOperation);
					} else {
						sMQL = removeSchemaObject;
						iCountDelete += 1;
						listDeleted.add(strInterfaceName);
					}
					schema_done_log.info("MQL QUERY FOR IMPORT : " + sMQL);

					ContextUtil.pushContext(context);
					localMQLCommand = new MQLCommand();
					bMQLResult = localMQLCommand.executeCommand(context, sMQL, true);
					String sMQLError = (String) localMQLCommand.getError();
					schema_done_log.info("MQL QUERY EXECUTION RESULT : " + bMQLResult);
					if (bMQLResult) {
						iCountSuccess += 1;
						if (sMQLPropertyQuery != null && !"".equals(sMQLPropertyQuery)) {
							schema_done_log.info("MQL QUERY {PROPERTY} : " + sMQLPropertyQuery);
							localMQLCommand.executeCommand(context, sMQLPropertyQuery, true);
						}
					} else {
						iCountFailure += 1;
						if (sMQL.trim().toLowerCase().startsWith("add"))
							iCountAdd -= 1;
						else if (sMQL.trim().toLowerCase().startsWith("mod"))
							iCountModify -= 1;
						else
							iCountDelete -= 1;
						throw new MatrixException(sMQLError);
					}
					ContextUtil.popContext(context);
				} catch (Exception e) {
					ContextUtil.popContext(context);
					schema_done_log.info("TOTAL :" + iCountTotal + ", SUCCESS :" + iCountSuccess +  ", ADDED :" + iCountAdd + ", MODIFIED :" + iCountModify + ", DELETED :"	+ iCountDelete + ".");
					throw new Exception("Error occured while importing schema interfaces : ["
							+ strInterfaceName.replace(UIUtil.removeCharecter, "") + "] : " + e.getMessage());
				}
				schema_done_log.info("-----------------------------------------------------------------");
			}
			schema_done_log.info("TOTAL  :" + iCountTotal + ", SUCCESS :" + iCountSuccess + ", ADDED  :" + iCountAdd + ", MODIFIED :" + iCountModify + ", DELETED :"
					+ iCountDelete + ".");
			if ("true".equalsIgnoreCase(scmConfigProperty.getLogEverything())) {
				schema_done_log.info("ADDED\t\t:"+listAdded.toString());
				schema_done_log.info("MODIFIED\t:"+listModified.toString());
				schema_done_log.info("DELETED\t:"+listDeleted.toString());
			}
		} catch (Exception e) {
			throw e;
		}
	}

	private String prepareAddNewSchemaMQL(Object objectSchemaInfo, Logger schema_done_log) throws Exception {
		StringBuilder sbMQL = new StringBuilder();
		try {
			Interfaces objInfo = (Interfaces) objectSchemaInfo;
			sbMQL.append(" add interface ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null && !objInfo.description.isEmpty()) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.getDerived() != null && !objInfo.getDerived().isEmpty()) {
				sbMQL.append("derived").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.getDerived())));
			}
			if (objInfo.getAttribute() != null && !objInfo.getAttribute().isEmpty()) {
				String[] sNewAttributes = objInfo.getAttribute().split(",");
				for (String sAttribute : sNewAttributes) {
					sbMQL.append(" attribute ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sAttribute)));
				}
			}
			if (objInfo.getType() != null && !objInfo.getType().isEmpty()) {
				String[] sArrayTypes = objInfo.getType().split(",");
				for (String sType : sArrayTypes) {
					sbMQL.append(" type ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sType)));
				}
			}
			if (objInfo.getRelationship() != null && !objInfo.getRelationship().isEmpty()) {
				String[] sArrayRels = objInfo.getRelationship().split(",");
				for (String sRelationship : sArrayRels) {
					sbMQL.append(" relationship ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sRelationship)));
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}

	private String prepareModifyExistingSchemaMQL(Object objectSchemaInfo, Logger schema_done_log) throws Exception {
		StringBuilder sbMQL = new StringBuilder();
		try {
			Interfaces objInfo = (Interfaces) objectSchemaInfo;
			sbMQL.append(" modify interface ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.getSabstract() != null && !objInfo.getSabstract().isEmpty()) {
				sbMQL.append("abstract").append(UIUtil.padWithSpaces(objInfo.getSabstract()));
			}
			if (objInfo.getDerived() != null) {
				String[] split = objInfo.getDerived().split(",");
				if (split.length > 1) {
					schema_done_log.warn("Interface can not have more than one derived type : " + UIUtil.quoteArgument(objInfo.getDerived()));
				}else{
					if(objInfo.getDerived().isEmpty()){
						sbMQL.append(" remove derived ");
					}else{
						sbMQL.append("derived").append(UIUtil.padWithSpaces(UIUtil.singleQuoteWithSpace(objInfo.getDerived())));						
					}
				}
			}
			if (objInfo.getAttribute() != null && !objInfo.getAttribute().isEmpty()) {
				String[] sNewAttributes = objInfo.getAttribute().split(",");
				for (String sAttribute : sNewAttributes) {
					sbMQL.append(UIUtil.removeFieldDetail(sAttribute, "attribute"));
				}
			}
			if (objInfo.getType() != null && !objInfo.getType().isEmpty()) {
				String[] sArrayTypes = objInfo.getType().split(",");
				for (String sType : sArrayTypes) {
					sbMQL.append(UIUtil.removeFieldDetail(sType, "type"));
				}
			}
			if (objInfo.getRelationship() != null && !objInfo.getRelationship().isEmpty()) {
				String[] sArrayRels = objInfo.getRelationship().split(",");
				for (String sRelationship : sArrayRels) {
					sbMQL.append(UIUtil.removeFieldDetail(sRelationship, "relationship"));
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}
}
