package com.izn.schemamodeler.admin.group;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import com.izn.schemamodeler.admin.group.GroupXMLSchema.Schema;
import com.izn.schemamodeler.admin.group.GroupXMLSchema.Schema.Basic;
import com.izn.schemamodeler.admin.group.GroupXMLSchema.Schema.Field;
import com.izn.schemamodeler.admin.group.GroupXMLSchema.Schema.Field.Childs;
import com.izn.schemamodeler.admin.group.GroupXMLSchema.Schema.Field.Childs.Child;
import com.izn.schemamodeler.admin.role.Role;
import com.izn.schemamodeler.admin.group.GroupXMLSchema.Schema.Field.Detail;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class GroupLoader implements SchemaLoader {
	ObjectMapper _gson = new ObjectMapper();
	Basic _basicElem = null;
	Field _fieldElem = null;
	Childs _childElem = null;

	public GroupLoader() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void loadSchema(Context context, String strSchemaName, String fileName, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty)  throws Exception{
		try {
			List<Group> lstGroup = new ArrayList<Group>();
			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
			GroupInfo groupInfo = (GroupInfo) schemaFactory.getSchemaObject("group");
			JAXBContext jConext = JAXBContext.newInstance(GroupXMLSchema.class);
			Unmarshaller unmarshaller = jConext.createUnmarshaller();
			JAXBElement<GroupXMLSchema> groupElem = unmarshaller.unmarshal(new StreamSource(new File(fileName)),
					GroupXMLSchema.class);
			GroupXMLSchema groupXMLSchema = groupElem.getValue();
			List<Schema> lstSchema = groupXMLSchema.getSchema();
			Group group = null;
			String fValue = "";
			String fName = "";
			String sFilePath = "";
			Map<String, String> mSettings = null;
			List<Map<String, String>> lstSetting = null;
			List<String> lstGroupItem = null;
			List<String> lstAChild = null;
			for (Schema _schema : lstSchema) {
				lstSetting = new ArrayList<Map<String, String>>();
				lstAChild = new ArrayList<String>();
				lstGroupItem = new ArrayList<String>();
				_basicElem = _schema.getBasic();
				String groupName = _basicElem.getName();
				group = new Group(_basicElem.getName(), _basicElem.getDescription(), _basicElem.getHidden(),
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
						group.setParent(fValue.trim());
					} else if (fName.equalsIgnoreCase("site")) {
						group.setSite(fValue.trim());
					} else if (fName.equalsIgnoreCase("description")) {
						group.setDescription(fValue.trim());
					} else if (fName.equalsIgnoreCase("child")) {
						group.setChild(fValue.trim());
					} else if (fName.equalsIgnoreCase("assignment")) {
						group.setAsssignment(fValue.trim());
					}

				}
				lstGroup.add(group);
			}
			prepareGroupMQL(context, lstGroup, schema_done_log, scmConfigProperty);
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

	private void prepareGroupMQL(Context context, List<Group> lstGroup, Logger schema_done_log,
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
		String strGroupName = "";
		List<String> listModified = new ArrayList<String>();
		List<String> listAdded = new ArrayList<String>();
		List<String> listDeleted = new ArrayList<String>();

		try {
			iCountTotal = lstGroup.size();
			for (Group group : lstGroup) {
				schema_done_log.info("Group : [" + group.name.replace(UIUtil.removeCharecter, "") + "]");
				strGroupName = group.name;
				ContextUtil.pushContext(context);
				try {
					String removeSchemaObject = UIUtil.removeSchemaObject(strGroupName, "group");
					if (removeSchemaObject.isEmpty()) {
						String strResult = MQLCommand.exec(context, "list group $1",
								new String[] { strGroupName.replace(UIUtil.removeCharecter, "") });
						if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
							sMQL = prepareModifyExistingSchemaMQL(group, schema_done_log);
							iCountModify += 1;
							listModified.add(strGroupName);
							sOperation = "mod";
						} else {
							sMQL = prepareAddNewSchemaMQL(group, schema_done_log);
							iCountAdd += 1;
							listAdded.add(strGroupName);
							sOperation = "add";
						}
						sMQLPropertyQuery = UIUtil.getMQLPropertyQuery(context, "group", group.name,
								group.registryname,sOperation);
					} else {
						sMQL = removeSchemaObject;
						iCountDelete += 1;
						listDeleted.add(strGroupName);
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
					schema_done_log.info("TOTAL :" + iCountTotal + ", SUCCESS :" + iCountSuccess + ", ADDED :" + iCountAdd + ", MODIFIED :" + iCountModify + ", DELETED :"	+ iCountDelete + ".");
					throw new Exception("Error occurred while importing schema attribute : ["
							+ strGroupName.replace(UIUtil.removeCharecter, "") + "] : " + e.getMessage());
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
			Group objInfo = (Group) objectSchemaInfo;
			sbMQL.append(" add group ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null && !objInfo.description.isEmpty()) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.getParent() != null) {
				if(objInfo.getParent().isEmpty()){
					sbMQL.append(" remove parent ");
				}else{
					sbMQL.append(" parent ");
					String[] sParent = objInfo.getParent().split(",");
					for (int i = 0; i < sParent.length; i++) {
						sbMQL.append(UIUtil.quoteArgument(sParent[i]));
						if (i != sParent.length - 1) {
							sbMQL.append(",");
						}
					}					
				}
			}
			if (objInfo.getChild() != null && !objInfo.getChild().isEmpty()) {
				String[] arrayofChild = objInfo.getChild().split(",");
				for (String ch : arrayofChild) {
					sbMQL.append(" child ").append('"' + ch + '"');
				}
			}
			if (objInfo.getSite() != null && !objInfo.getSite().isEmpty()) {
				sbMQL.append(" site").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.getSite())));
			}
			if (objInfo.getAsssignment() != null && !objInfo.getAsssignment().isEmpty()) {
				String[] sArrAssignment = objInfo.getAsssignment().split(",");
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
			Group objInfo = (Group) objectSchemaInfo;
			sbMQL.append(" modify group ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			/*if (objInfo.getParent() != null && !objInfo.getParent().isEmpty()) {
				sbMQL.append(" parent ");
				String[] sParent = objInfo.getParent().split(",");
				for (int i = 0; i < sParent.length; i++) {
					sbMQL.append(UIUtil.quoteArgument(sParent[i]));
					if (i != sParent.length - 1) {
						sbMQL.append(",");
					}
				}
			}*/
			if (objInfo.getParent() != null && !objInfo.getParent().isEmpty()) {				
				sbMQL.append(UIUtil.removeAssignment(objInfo.getParent(),"parent"));
			}
			if (objInfo.getChild() != null && !objInfo.getChild().isEmpty()) {
				String[] arrayofChild = objInfo.getChild().split(",");
				for (String ch : arrayofChild) {
					//sbMQL.append(" child ").append('"' + ch + '"');
					if(ch.startsWith(UIUtil.removeCharecter) && ch.endsWith(UIUtil.removeCharecter)){
						sbMQL.append(UIUtil.removeAssignment(ch,"child"));
					} else {
						sbMQL.append(" child ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(ch)));
					}
				}
			}
			if (objInfo.getSite() != null && !objInfo.getSite().isEmpty()) {
				sbMQL.append(" site").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.getSite())));
			} else {				
				sbMQL.append(" site ").append("\"\"");
			}
			if (objInfo.getAsssignment() != null && !objInfo.getAsssignment().isEmpty()) {
				String[] sArrAssignment = objInfo.getAsssignment().split(",");
				for (String sAssignment : sArrAssignment) {
					//UIUtil.removeAssignment(sAssignment, "assign person");
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
