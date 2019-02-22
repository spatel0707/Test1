package com.izn.schemamodeler.admin.role;

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
import com.izn.schemamodeler.admin.role.RoleXMLSchema.Schema;
import com.izn.schemamodeler.admin.role.RoleXMLSchema.Schema.Basic;
import com.izn.schemamodeler.admin.role.RoleXMLSchema.Schema.Field;
import com.izn.schemamodeler.admin.role.RoleXMLSchema.Schema.Field.Childs;
import com.izn.schemamodeler.admin.role.RoleXMLSchema.Schema.Field.Detail;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class RoleLoader implements SchemaLoader {
	ObjectMapper _gson = new ObjectMapper();
	Basic _basicElem = null;
	Field _fieldElem = null;
	Childs _childElem = null;

	public RoleLoader() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void loadSchema(Context context, String strSchemaName, String fileName, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty)  throws Exception{
		try {
			List<Role> lstRole = new ArrayList<Role>();
			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
			RoleInfo roleInfo = (RoleInfo) schemaFactory.getSchemaObject("role");
			JAXBContext jConext = JAXBContext.newInstance(RoleXMLSchema.class);
			Unmarshaller unmarshaller = jConext.createUnmarshaller();
			JAXBElement<RoleXMLSchema> roleElem = unmarshaller.unmarshal(new StreamSource(new File(fileName)),
					RoleXMLSchema.class);
			RoleXMLSchema roleXMLSchema = roleElem.getValue();
			List<Schema> lstSchema = roleXMLSchema.getSchema();
			Role role = null;
			String fValue = "";
			String fName = "";
			String sFilePath = "";
			Map<String, String> mSettings = null;
			List<Map<String, String>> lstSetting = null;
			List<String> lstRoleItem = null;
			List<String> lstAChild = null;
			for (Schema _schema : lstSchema) {
				lstSetting = new ArrayList<Map<String, String>>();
				lstAChild = new ArrayList<String>();
				lstRoleItem = new ArrayList<String>();
				_basicElem = _schema.getBasic();
				String roleName = _basicElem.getName();
				role = new Role(_basicElem.getName(), _basicElem.getDescription(), _basicElem.getHidden(),
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
					if (fName.equalsIgnoreCase("parent")) {
						role.setParent(fValue);
					} else if (fName.equalsIgnoreCase("site")) {
						role.setSite(fValue);
					} else if (fName.equalsIgnoreCase("roletype")) {
						role.setRoletype(fValue);
					} else if (fName.equalsIgnoreCase("maturity")) {
						role.setMaturity(fValue);
					} else if (fName.equalsIgnoreCase("child")) {
						role.setChild(fValue);
					} else if (fName.equalsIgnoreCase("assignment")) {
						role.setAssigment(fValue);
					}

				}
				lstRole.add(role);
			}
			// Gson gson = new Gson();
			// String json = gson.toJson(lstRole);
			prepareRoleMQL(context, lstRole, schema_done_log, scmConfigProperty);
		} catch (UnmarshalException ume) {
			schema_done_log.error("UnmarshalException on ["+strSchemaName+".xml] : "+ume.getCause());
			schema_done_log.warn(UIUtil.sUnmarshalExceptionWarningMessage);
			throw ume;
		} catch (Exception e) {
			schema_done_log.error(""+e.getCause());
			throw e;
		}
		// TODO Auto-generated method stub
	}

	private void prepareRoleMQL(Context context, List<Role> lstRole, Logger schema_done_log,
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
		String strRoleName = "";
		List<String> listModified = new ArrayList<String>();
		List<String> listAdded = new ArrayList<String>();
		List<String> listDeleted = new ArrayList<String>();
		try {
			iCountTotal = lstRole.size();
			for (Role role : lstRole) {
				
				schema_done_log.info("Role : [" + role.name.replace(UIUtil.removeCharecter, "") + "]");
				strRoleName = role.name;
				ContextUtil.pushContext(context);
				try {
					String removeSchemaObject = UIUtil.removeSchemaObject(strRoleName, "role");
					if (removeSchemaObject.isEmpty()) {
						String strResult = MQLCommand.exec(context, "list role $1",
								new String[] { strRoleName.replace(UIUtil.removeCharecter, "") });
						if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
							sMQL = prepareModifyExistingSchemaMQL(role, schema_done_log);
							iCountModify += 1;
							listModified.add(strRoleName);
							sOperation = "mod";
						} else {
							sMQL = prepareAddNewSchemaMQL(role, schema_done_log);
							iCountAdd += 1;
							listAdded.add(strRoleName);
							sOperation = "add";
						}
						sMQLPropertyQuery = UIUtil.getMQLPropertyQuery(context, "role", role.name, role.registryname,sOperation);
					} else {
						sMQL = removeSchemaObject;
						iCountDelete += 1;
						listDeleted.add(strRoleName);
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
					schema_done_log.info("TOTAL :" + iCountTotal + ", SUCCESS :" + iCountSuccess  + ", ADDED :" + iCountAdd + ", MODIFIED :" + iCountModify + ", DELETED :"	+ iCountDelete + ".");
					throw new Exception("Error occured while importing schema role : ["
							+ strRoleName.replace(UIUtil.removeCharecter, "") + "] : " + e.getMessage());
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
			Role objInfo = (Role) objectSchemaInfo;
			sbMQL.append(" add role ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null && !objInfo.description.isEmpty()) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.getParent() != null && !objInfo.getParent().isEmpty()) {
				sbMQL.append(" parent ");
				String[] sParent = objInfo.getParent().split(",");
				for (int i = 0; i < sParent.length; i++) {
					sbMQL.append(UIUtil.quoteArgument(sParent[i]));
					if (i != sParent.length - 1) {
						sbMQL.append(",");
					}
				}
			}
			if (objInfo.getChild() != null && !objInfo.getChild().isEmpty()) {
				String[] arrayofChild = objInfo.getChild().split(",");
				for (String ch : arrayofChild) {
					sbMQL.append(" child ").append('"' + ch + '"');
				}
			}
			if (objInfo.getMaturity() != null && !objInfo.getMaturity().isEmpty()) {
				sbMQL.append(" maturity").append(UIUtil.padWithSpaces(objInfo.getMaturity()));
			}
			if (objInfo.getSite() != null && !objInfo.getSite().isEmpty()) {
				sbMQL.append(" site").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.getSite())));
			}
			if (objInfo.getRoletype() != null && !objInfo.getRoletype().isEmpty()) {
				switch (objInfo.getRoletype()) {
				case "isarole":
					sbMQL.append(UIUtil.padWithSpaces("asarole"));
					break;
				case "asanorg":
					sbMQL.append(UIUtil.padWithSpaces("asanorg"));
					break;
				case "isaproject":
					sbMQL.append(UIUtil.padWithSpaces("asaproject"));
					break;
				}
			}
			if (objInfo.getAssigment() != null && !objInfo.getAssigment().isEmpty()) {
				String[] sArrAssignment = objInfo.getAssigment().split(",");
				for (String sAssignment : sArrAssignment) {
					sbMQL.append(" assign person ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sAssignment)));
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
			Role objInfo = (Role) objectSchemaInfo;
			sbMQL.append(" modify role ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.getParent() != null && !objInfo.getParent().isEmpty()) {				
				sbMQL.append(UIUtil.removeAssignment(objInfo.getParent(),"parent"));
			}
			if (objInfo.getChild() != null && !objInfo.getChild().isEmpty()) {
				String[] arrayofChild = objInfo.getChild().split(",");
				for (String ch : arrayofChild) {
					if(ch.startsWith(UIUtil.removeCharecter) && ch.endsWith(UIUtil.removeCharecter)){
						sbMQL.append(UIUtil.removeAssignment(ch,"child"));
					} else {
						sbMQL.append(" child ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(ch)));
					}
					
				}
			}
			if (objInfo.getMaturity() != null && !objInfo.getMaturity().isEmpty()) {
				sbMQL.append(" maturity").append(UIUtil.padWithSpaces(objInfo.getMaturity()));
			}
			if (objInfo.getSite() != null && !objInfo.getSite().isEmpty()) {
				sbMQL.append(" site ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.getSite())));
			} else {				
				sbMQL.append(" site ").append("\"\"");
			}
			if (objInfo.getRoletype() != null && !objInfo.getRoletype().isEmpty()) {
				switch (objInfo.getRoletype()) {
				case "isarole":
					sbMQL.append(UIUtil.padWithSpaces("asarole"));
					break;
				case "asanorg":
					sbMQL.append(UIUtil.padWithSpaces("asanorg"));
					break;
				case "isaproject":
					sbMQL.append(UIUtil.padWithSpaces("asaproject"));
					break;
				}
			}
			if (objInfo.getAssigment() != null && !objInfo.getAssigment().isEmpty()) {
				String[] sArrAssignment = objInfo.getAssigment().split(",");
				for (String sAssignment : sArrAssignment) {
					if(sAssignment.startsWith(UIUtil.removeCharecter) && sAssignment.endsWith(UIUtil.removeCharecter)){
						sbMQL.append(UIUtil.removeAssignment(sAssignment, "assign person"));
					} else {
						sbMQL.append(" assign person ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sAssignment)));
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}
}
