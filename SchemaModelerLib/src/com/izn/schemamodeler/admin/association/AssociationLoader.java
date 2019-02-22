package com.izn.schemamodeler.admin.association;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
//import org.apache.log4j.Logger;
import org.apache.log4j.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.transform.stream.StreamSource;

import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.izn.schemamodeler.SchemaFactoryInfo;
import com.izn.schemamodeler.SchemaLoader;
import com.izn.schemamodeler.admin.association.AssociationXMLSchema;
import com.izn.schemamodeler.admin.association.AssociationXMLSchema.Schema;
import com.izn.schemamodeler.admin.association.AssociationXMLSchema.Schema.Basic;
import com.izn.schemamodeler.admin.association.AssociationXMLSchema.Schema.Field;
import com.izn.schemamodeler.admin.association.AssociationXMLSchema.Schema.Field.Detail;
import com.izn.schemamodeler.admin.attribute.Attribute;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;
import matrix.util.StringList;

public class AssociationLoader implements SchemaLoader {

	ObjectMapper _gson = new ObjectMapper();
	Basic _basicElem = null;
	Field _fieldElem = null;

	public AssociationLoader() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void loadSchema(Context context, String strSchemaName, String fileName, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty)  throws Exception{
		try {
			List<Association> lstAssociation = new ArrayList<Association>();
			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
			AssociationInfo AssociationInfo = (AssociationInfo) schemaFactory.getSchemaObject("association");
			JAXBContext jConext = JAXBContext.newInstance(AssociationXMLSchema.class);
			Unmarshaller unmarshaller = jConext.createUnmarshaller();
			JAXBElement<AssociationXMLSchema> AssociationElem = unmarshaller
					.unmarshal(new StreamSource(new File(fileName)), AssociationXMLSchema.class);
			AssociationXMLSchema AssociationXMLSchema = AssociationElem.getValue();
			List<Schema> lstSchema = AssociationXMLSchema.getSchema();
			Association Association = null;
			String fValue = "";
			String fName = "";
			List<String> lstAssociationItem = null;
			for (Schema _schema : lstSchema) {
				lstAssociationItem = new ArrayList<String>();
				_basicElem = _schema.getBasic();
				Association = new Association(_basicElem.getName(), _basicElem.getDescription(), _basicElem.getHidden(),
						this._basicElem.getRegistryName());
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
					if (fName.equalsIgnoreCase("definition")) {
						Association.setDefinition(fValue.trim());
					}

				}
				lstAssociation.add(Association);
			}
			prepareAssociationMQL(context, lstAssociation, schema_done_log, scmConfigProperty);
		} catch (UnmarshalException ume) {
			schema_done_log.error("UnmarshalException on ["+strSchemaName+".xml] : "+ume.getCause());
			schema_done_log.warn(UIUtil.sUnmarshalExceptionWarningMessage);
			throw ume;
		} catch (Exception e) {
			schema_done_log.error(""+e.getCause());
			throw e;
		}
	}

	private void prepareAssociationMQL(Context context, List<Association> lstAssociation, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty) throws Exception{
		boolean bMQLResult;
		boolean bMQLResult2;
		int iCountTotal = 0;
		int iCountSuccess = 0;
		int iCountFailure = 0;
		int iCountAdd = 0;
		int iCountModify = 0;
		int iCountDelete = 0;
		String sMQL = "";
		String sMQLPropertyQuery = "";
		MQLCommand localMQLCommand = null;
		String strAssociationName = "";
		String sOperation = "";
		List<String> listModified = new ArrayList<String>();
		List<String> listAdded = new ArrayList<String>();
		List<String> listDeleted = new ArrayList<String>();
		try {
			iCountTotal  = lstAssociation.size();
			for (Association association : lstAssociation) {
				
				schema_done_log.info("Association : ["+ association.name.replace(UIUtil.removeCharecter, "") + "]");
				strAssociationName = association.name;
				String strAssoNamewoSpace = strAssociationName.replaceAll(" ", "");
				try {
					String removeSchemaObject = UIUtil.removeSchemaObject(strAssoNamewoSpace, "association");
					if (removeSchemaObject.isEmpty()) {
						if (checkAssociationExists(context, strAssoNamewoSpace)) {
							sMQL = prepareModifyExistingSchemaMQL(association, schema_done_log);
							iCountModify += 1;
							listModified.add(strAssociationName);
							sOperation = "mod";
						} else {
							sMQL = prepareAddNewSchemaMQL(association, schema_done_log);
							iCountAdd += 1;
							listAdded.add(strAssociationName);
							sOperation = "add";
						}
						sMQLPropertyQuery = UIUtil.getMQLPropertyQuery(context, "association", association.name,
								association.registryname, sOperation);
					} else {
						sMQL = removeSchemaObject;
						iCountDelete += 1;
						listDeleted.add(strAssociationName);
					}
					schema_done_log.info("MQL QUERY FOR IMPORT : " + sMQL);
					ContextUtil.pushContext(context);
					localMQLCommand = new MQLCommand();
					bMQLResult = localMQLCommand.executeCommand(context, sMQL, true);
					String sMQLError = (String) localMQLCommand.getError();
					if (bMQLResult) {
						iCountSuccess += 1;
						if (sMQLPropertyQuery != null && !"".equals(sMQLPropertyQuery)) {
							bMQLResult2 = localMQLCommand.executeCommand(context, sMQLPropertyQuery, true);
							String sMQLError1 = (String) localMQLCommand.getError();
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
				} catch (Exception e) {
					ContextUtil.popContext(context);
					schema_done_log.info("TOTAL :" + iCountTotal + ", SUCCESS :" + iCountSuccess + ", ADDED :" + iCountAdd + ", MODIFIED :" + iCountModify + ", DELETED :"	+ iCountDelete + ".");
					throw new Exception("Error occurred while importing schema association : ["
							+ strAssociationName.replace(UIUtil.removeCharecter, "") + "] : " + e.getMessage());
				}
				schema_done_log.info("-----------------------------------------------------------------");
			}
			schema_done_log.info("TOTAL  :" + iCountTotal + ", SUCCESS :" + iCountSuccess + ", FAILURE :"
					+ iCountFailure + ", ADDED  :" + iCountAdd + ", MODIFIED :" + iCountModify + ", DELETED :"
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

	private boolean checkAssociationExists(Context context, String strAssoNamewoSpace) throws Exception {

		String strResult = MQLCommand.exec(context, "list Association $1", new String[] { "*" });
		StringList slAssList = new StringList();
		if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
			slAssList = FrameworkUtil.split(strResult, "\n");

		}
		boolean assoResult = false;
		Iterator<String> itrAss = slAssList.iterator();
		while (itrAss.hasNext()) {
			if (strAssoNamewoSpace.equals(itrAss.next().replaceAll(" ", ""))) {
				assoResult = true;
				break;
			}
		}
		return assoResult;
	}

	private String prepareAddNewSchemaMQL(Object objectSchemaInfo, Logger schema_done_log) throws Exception {
		StringBuilder sbMQL = new StringBuilder();
		try {
			Association objInfo = (Association) objectSchemaInfo;
			sbMQL.append(" add association ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null && !objInfo.description.isEmpty()) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.definition != null && !objInfo.definition.isEmpty()) {
				sbMQL.append(" definition ").append("'").append(UIUtil.quoteArgument(objInfo.definition)).append("'");
			}
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}

	private String prepareModifyExistingSchemaMQL(Object objectSchemaInfo, Logger schema_done_log) throws Exception {
		StringBuilder sbMQL = new StringBuilder();
		try {
			Association objInfo = (Association) objectSchemaInfo;
			sbMQL.append(" mod association ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.definition != null && !objInfo.definition.isEmpty()) {
				sbMQL.append(" definition ").append("'").append(UIUtil.quoteArgument(objInfo.definition)).append("'");
			}
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}
}
