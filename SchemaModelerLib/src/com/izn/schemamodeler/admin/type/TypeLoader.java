package com.izn.schemamodeler.admin.type;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.izn.schemamodeler.SchemaFactoryInfo;
import com.izn.schemamodeler.SchemaLoader;
import com.izn.schemamodeler.admin.type.TypeXMLSchema.Schema;
import com.izn.schemamodeler.admin.type.TypeXMLSchema.Schema.Basic;
import com.izn.schemamodeler.admin.type.TypeXMLSchema.Schema.Field;
import com.izn.schemamodeler.admin.type.TypeXMLSchema.Schema.Field.Detail;
import com.izn.schemamodeler.admin.type.TypeXMLSchema.Schema.Trigger;
import com.izn.schemamodeler.admin.type.TypeXMLSchema.Schema.Trigger.Event;
import com.izn.schemamodeler.admin.type.TypeXMLSchema.Schema.Trigger.Event.Eventdetail;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class TypeLoader implements SchemaLoader {
	ObjectMapper _gson = new ObjectMapper();
	Schema _schemaElem = null;
	Basic _basicElem = null;
	Field _fieldElem = null;
	Detail _detailElem = null;
	Trigger _triggerElem = null;
	Event _eventElem = null;
	Eventdetail _eventdetailElem = null;
	List<Type> ltsType = new ArrayList<Type>();

	public TypeLoader() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void loadSchema(Context context, String strSchemaName, String fileName, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty)  throws Exception{
		try {

			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
			TypeInfo typeInfo = (TypeInfo) schemaFactory.getSchemaObject("type");
			JAXBContext jContext = JAXBContext.newInstance(TypeXMLSchema.class);
			Unmarshaller unMarsheller = jContext.createUnmarshaller();
			JAXBElement<TypeXMLSchema> typeJaxbElem = unMarsheller
					.unmarshal((new StreamSource(new FileReader(fileName))), TypeXMLSchema.class);
			TypeXMLSchema typeElem = typeJaxbElem.getValue();
			List<Schema> lstSchema = typeElem.getSchema();
			Iterator<Schema> itrSchema = lstSchema.iterator();
			Type type = null;
			String strFName = "";
			String strFValue = "";
			List slTrigger = null;
			String typeName = null; // Added to check if element exists
			while (itrSchema.hasNext()) {
				slTrigger = new ArrayList();
				_schemaElem = itrSchema.next();
				_basicElem = _schemaElem.getBasic();

				type = new Type(_basicElem.getName(), _basicElem.getDescription(), _basicElem.getHidden(),
						_basicElem.getRegistryName());
				_fieldElem = _schemaElem.getField();
				List<Detail> lstDetail = _fieldElem.getDetail();
				Iterator<Detail> itrDetail = lstDetail.iterator();
				while (itrDetail.hasNext()) {
					_detailElem = itrDetail.next();
					strFName = _detailElem.getName();
					strFValue = _detailElem.getValueAttribute();
					if (strFValue != null) {
						strFValue = _detailElem.getValueAttribute().trim();
					} else {
						strFValue = "";
					}
					if (strFName.equalsIgnoreCase("derived")) {
						type.setDerived(strFValue);
					} else if (strFName.equalsIgnoreCase("abstract")) {
						type.setSabstract(strFValue);
					} else if (strFName.equalsIgnoreCase("sparse")) {
						type.setSparse(strFValue);
					} else if (strFName.equalsIgnoreCase("attribute")) {
						type.setAttributes(strFValue);
					} else if (strFName.equalsIgnoreCase("method")) {
						type.setMethods(strFValue);
					}

				}
				_triggerElem = _schemaElem.getTrigger();
				if (_triggerElem != null) {
					List<Event> lstEvent = _triggerElem.getEvent();
					Iterator<Event> itrEvent = lstEvent.iterator();
					Map<String, String> mTriggerDetails = null;
					String strEvent = "";
					while (itrEvent.hasNext()) {
						_eventElem = itrEvent.next();
						// name of trigger event
						strEvent = _eventElem.getName();
						List<Eventdetail> lstEventdetails = _eventElem.getEventdetail();
						Iterator<Eventdetail> itrEventdetails = lstEventdetails.iterator();
						while (itrEventdetails.hasNext()) {
							_eventdetailElem = itrEventdetails.next();
							mTriggerDetails = new HashMap<String, String>();
							mTriggerDetails.put(ACTION, strEvent);
							mTriggerDetails.put(TYPE, _eventdetailElem.getType());
							mTriggerDetails.put(PROGRAM, _eventdetailElem.getProgram());
							mTriggerDetails.put(NAME, _eventdetailElem.getInput());
							slTrigger.add(mTriggerDetails);
						}
					}
					type.setSlTriggers(slTrigger);
					String sTypeInfo = typeInfo.geSchemaInfo(context, _basicElem.getName(), "tbd");
					if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(sTypeInfo)) {

						Map mTypeDBInfo = _gson.readValue(sTypeInfo, HashMap.class);
						// type.setSlFilterTrigger(UIUtil.filteredListOMap(slTrigger,
						// (List) mTypeDBInfo.get("trigger")));
						type.setSlFilterTrigger((List) mTypeDBInfo.get("trigger"));
					} else {
						type.name = _basicElem.getName();
					}
				}
				ltsType.add(type);
			}
			prepareTypeMQL(context, ltsType, schema_done_log, scmConfigProperty);
		} catch (UnmarshalException ume) {
			schema_done_log.error("UnmarshalException on ["+strSchemaName+".xml] : "+ume.getCause());
			schema_done_log.warn(UIUtil.sUnmarshalExceptionWarningMessage);
			throw ume;
		} catch (Exception e) {
			schema_done_log.error(""+e.getCause());
			throw e;
		}
	}

	private void prepareTypeMQL(Context context, List<Type> ltsType, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty) throws Exception{
		boolean bMQLResult;
		int iCountTotal = 0;
		int iCountSuccess = 0;
		int iCountFailure = 0;
		int iCountAdd = 0;
		int iCountModify = 0;
		int iCountDelete = 0;
		String sMQL = "";
		String sMQLPropertyQuery = "";
		MQLCommand localMQLCommand = null;
		String strTypeName = "";
		String sOperation = "";
		List<String> listModified = new ArrayList<String>();
		List<String> listAdded = new ArrayList<String>();
		List<String> listDeleted = new ArrayList<String>();
		try {
			Iterator<Type> itrType = ltsType.iterator();
			Type type = null;
			iCountTotal = ltsType.size();
			while (itrType.hasNext()) {		
				type = itrType.next();
				schema_done_log.info("Type : ["
						+ type.name.replace(UIUtil.removeCharecter, "") + "]");
				ContextUtil.pushContext(context);
				strTypeName = type.name;
				try {
					String removeSchemaObject = UIUtil.removeSchemaObject(strTypeName, "type");
					if (removeSchemaObject.isEmpty()) {
						String strResult = MQLCommand.exec(context, "list type $1",
								new String[] { strTypeName.replace(UIUtil.removeCharecter, "") });
						if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
							sMQL = prepareModifyExistingSchemaMQL(type, schema_done_log);
							iCountModify += 1;
							listModified.add(strTypeName);
							sOperation = "mod";
						} else {
							sMQL = prepareAddNewSchemaMQL(type, schema_done_log);
							iCountAdd += 1;
							listAdded.add(strTypeName);
							sOperation = "add";
						}
						sMQLPropertyQuery = UIUtil.getMQLPropertyQuery(context, "type", type.name, type.registryname,
								sOperation);
					} else {
						sMQL = removeSchemaObject;
						iCountDelete += 1;
						listDeleted.add(strTypeName);
					}
					schema_done_log.info("MQL QUERY FOR IMPORT : " + sMQL);

					ContextUtil.pushContext(context);
					localMQLCommand = new MQLCommand();
					bMQLResult = localMQLCommand.executeCommand(context, sMQL, true);
					schema_done_log.info("MQL QUERY EXECUTION RESULT : " + bMQLResult);
					String sMQLError = (String) localMQLCommand.getError();
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
					throw new Exception("Error occurred while importing schema type : ["
							+ strTypeName.replace(UIUtil.removeCharecter, "") + "] : " + e.getMessage());
				}
				schema_done_log.info("-----------------------------------------------------------------");
			}
			schema_done_log.info("TOTAL  :" + iCountTotal + ", SUCCESS :" + iCountSuccess 
					 + ", ADDED  :" + iCountAdd + ", MODIFIED :" + iCountModify + ", DELETED :"
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
			Type objInfo = (Type) objectSchemaInfo;
			sbMQL.append(" add type ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null && !objInfo.description.isEmpty()) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.getSabstract() != null && !objInfo.getSabstract().isEmpty()) {
				sbMQL.append("abstract").append(UIUtil.padWithSpaces(objInfo.getSabstract()));
			}
			if (objInfo.getSparse() != null && !objInfo.getSparse().isEmpty()) {
				sbMQL.append("sparse").append(UIUtil.padWithSpaces(objInfo.getSparse()));
			}
			if (objInfo.getDerived() != null && !objInfo.getDerived().isEmpty()) {
				sbMQL.append("derived").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.getDerived())));
			}
			if (objInfo.getAttributes() != null && !objInfo.getAttributes().isEmpty()) {
				String[] sNewAttributes = objInfo.getAttributes().split(",");
				for (String sAttribute : sNewAttributes) {
					sbMQL.append(" attribute ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sAttribute)));
				}
			}
			List<Map<Object, Object>> slNewTriggers = objInfo.getSlTriggers();
			if (slNewTriggers != null && !slNewTriggers.isEmpty()) {
				Iterator<Map<Object, Object>> itrNTriggers = slNewTriggers.iterator();
				while (itrNTriggers.hasNext()) {
					Map m = itrNTriggers.next();
					String strType = (String) m.get("type");
					String strAction = (String) m.get("action");
					String strName = (String) m.get("name");
					String strProgram = (String) m.get("program");
					if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strAction)
							&& strAction.contains("trigger")) {
						strAction = strAction.replace("trigger", "");
					}
					sbMQL.append(" TRIGGER").append(UIUtil.padWithSpaces(strAction)).append(strType)
							.append(UIUtil.padWithSpaces(strProgram)).append("input")
							.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(strName)));
				}
			}
			if (objInfo.getMethods() != null && !objInfo.getMethods().isEmpty()) {
				String[] sArrMethods = objInfo.getMethods().split(",");
				for (String sMethod : sArrMethods) {
					sbMQL.append(" method ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sMethod)));
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
			Type objInfo = (Type) objectSchemaInfo;
			sbMQL.append(" modify type ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.getSabstract() != null && !objInfo.getSabstract().isEmpty()) {
				sbMQL.append("abstract").append(UIUtil.padWithSpaces(objInfo.getSabstract()));
			}
			if (objInfo.getSparse() != null && !objInfo.getSparse().isEmpty()) {
				sbMQL.append("sparse").append(UIUtil.padWithSpaces(objInfo.getSparse()));
			}
			if (objInfo.getDerived() != null) {
				String[] split = objInfo.getDerived().split(",");
				if (split.length > 1) {
					schema_done_log.warn("Type can not have more than one derived type : " + UIUtil.singleQuotes(objInfo.getDerived()));
				}else{
					if(objInfo.getDerived().isEmpty()){
						sbMQL.append(" remove derived ");
					}else{						
						sbMQL.append("derived").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.getDerived())));
					}
				}
			}
			if (objInfo.getAttributes() != null && !objInfo.getAttributes().isEmpty()) {
				String[] sNewAttributes = objInfo.getAttributes().split(",");
				for (String sAttribute : sNewAttributes) {
					sbMQL.append(UIUtil.removeFieldDetail(sAttribute, "attribute"));
				}
			}
			if (objInfo.getMethods() != null && !objInfo.getMethods().isEmpty()) {
				String[] sArrMethods = objInfo.getMethods().split(",");
				for (String sMethod : sArrMethods) {
					sbMQL.append(UIUtil.removeFieldDetail(sMethod, "method"));
				}
			}
			List<Map<Object, Object>> slNewTriggers = objInfo.getSlTriggers();
			if (slNewTriggers != null && !slNewTriggers.isEmpty()) {
				Iterator<Map<Object, Object>> itrNTriggers = slNewTriggers.iterator();
				String[] args;
				String strAction = "";
				while (itrNTriggers.hasNext()) {
					Map m = itrNTriggers.next();
					strAction = (String) m.get("action");
					args = new String[4];
					args[0] = strAction;
					args[1] = (String) m.get("type");
					args[2] = (String) m.get("name");
					args[3] = (String) m.get("program");
					if (strAction != null && !strAction.isEmpty() && "trigger".contains(strAction)) {
						strAction = strAction.replace("trigger", "");
					}
					sbMQL.append(UIUtil.removeTrigger(strAction, args));
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}
}
