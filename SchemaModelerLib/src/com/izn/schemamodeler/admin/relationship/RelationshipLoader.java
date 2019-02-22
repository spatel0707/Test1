package com.izn.schemamodeler.admin.relationship;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

import com.izn.schemamodeler.admin.relationship.RelationshipXMLSchema.Schema;
import com.izn.schemamodeler.admin.relationship.RelationshipXMLSchema.Schema.Basic;
import com.izn.schemamodeler.admin.relationship.RelationshipXMLSchema.Schema.Field;
import com.izn.schemamodeler.admin.relationship.RelationshipXMLSchema.Schema.Field.Detail;
import com.izn.schemamodeler.admin.relationship.RelationshipXMLSchema.Schema.Rel;
import com.izn.schemamodeler.admin.relationship.RelationshipXMLSchema.Schema.Rel.Reldetail;
import com.izn.schemamodeler.admin.relationship.RelationshipXMLSchema.Schema.Trigger;
import com.izn.schemamodeler.admin.relationship.RelationshipXMLSchema.Schema.Trigger.Event;
import com.izn.schemamodeler.admin.relationship.RelationshipXMLSchema.Schema.Trigger.Event.Eventdetail;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class RelationshipLoader implements SchemaLoader {
	ObjectMapper _gson = new ObjectMapper();
	Schema _schemaElem = null;
	Basic _basicElem = null;
	Field _fieldElem = null;
	Detail _detailElem = null;
	Trigger _triggerElem = null;
	Eventdetail _eventdetailElem = null;
	Event _eventElem = null;
	Rel _relElem = null;
	Reldetail _reldetailElem = null;

	public RelationshipLoader() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void loadSchema(Context context, String strSchemaName, String fileName, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty)  throws Exception{

		try {
			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
			RelationshipInfo relInfo = (RelationshipInfo) schemaFactory.getSchemaObject("relationship");
			JAXBContext jContext = JAXBContext.newInstance(RelationshipXMLSchema.class);
			Unmarshaller unMarshaller = jContext.createUnmarshaller();
			JAXBElement<RelationshipXMLSchema> relElem = unMarshaller.unmarshal(new StreamSource(new File(fileName)),
					RelationshipXMLSchema.class);
			RelationshipXMLSchema relSchema = relElem.getValue();
			List<Schema> lstSchema = relSchema.getSchema();
			Iterator<Schema> itrSchema = lstSchema.iterator();
			List slTrigger = new ArrayList();
			Relationship relationship = null;
			String fValue = "";
			String fName = "";
			String sRelType = "";
			List<Relationship> lstRelationship = new ArrayList<Relationship>();
			while (itrSchema.hasNext()) {
				_schemaElem = itrSchema.next();
				_basicElem = _schemaElem.getBasic();
				relationship = new Relationship(_basicElem.getName(), _basicElem.getDescription(),
						_basicElem.getHidden(), _basicElem.getRegistryName());
				_fieldElem = _schemaElem.getField();
				List<Detail> lstDetail = _fieldElem.getDetail();
				Iterator<Detail> itrDetail = lstDetail.iterator();
				while (itrDetail.hasNext()) {
					_detailElem = itrDetail.next();
					fName = _detailElem.getName();
					fValue = _detailElem.getValueAttribute();
					if (fValue != null) {
						fValue = _detailElem.getValueAttribute().trim();
					} else {
						fValue = "";
					}
					if (fName.equalsIgnoreCase("derived")) {
						relationship.setDerived(fValue);
					} else if (fName.equalsIgnoreCase("abstract")) {
						relationship.setsAbstract(fValue);
					} else if (fName.equalsIgnoreCase("sparse")) {
						relationship.setSparse(fValue);
					} else if (fName.equalsIgnoreCase("preventduplicates")) {
						relationship.setPreventdups(fValue);
					} else if (fName.equalsIgnoreCase("attribute")) {
						relationship.setAttributes(fValue);
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
					relationship.setSlTriggers(slTrigger);
				}
				String sRelInfo = relInfo.geSchemaInfo(context, _basicElem.getName(), "tbd");

				if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(sRelInfo)) {
					Map mRelInfo = _gson.readValue(sRelInfo, HashMap.class);
					if (mRelInfo.containsKey("trigger")) {
						List slNewTrigger = (List) mRelInfo.get("trigger");
						if (slNewTrigger.size() != 0)
							// relationship.setSlFilterTrigger(UIUtil.filteredListOMap(slTrigger,
							// (List) mRelInfo.get("trigger")));
							relationship.setSlFilterTrigger((List) mRelInfo.get("trigger"));
					}
				}
				List<Rel> lstRel = _schemaElem.getRel();
				Iterator<Rel> itr = lstRel.iterator();
				Relationship.FromType fromType = relationship.getNewFromType();
				Relationship.ToType toType = relationship.getNewTotype();
				while (itr.hasNext()) {
					_relElem = itr.next();
					sRelType = _relElem.getType();
					List<Reldetail> lstRelDetail = _relElem.getReldetail();
					Iterator<Reldetail> itrRelDetails = lstRelDetail.iterator();
					while (itrRelDetails.hasNext()) {
						_reldetailElem = itrRelDetails.next();
						fName = _reldetailElem.getName();
						fValue = _reldetailElem.getValueAttribute();
						if (fName != null && !"".equals(fName) && fValue != null && !"".equals(fValue)) {
							if (sRelType.equalsIgnoreCase("FromType")) {
								if (fName.equalsIgnoreCase("type")) {
									fromType.setFromType(fValue);
								} else if (fName.equalsIgnoreCase("relationship")) {
									fromType.setFromRel(fValue);
								} else if (fName.equalsIgnoreCase("meaning")) {
									fromType.setFromMeaning(fValue);
								} else if (fName.equalsIgnoreCase("cardinality")) {
									fromType.setFromCardinality(fValue);
								} else if (fName.equalsIgnoreCase("revision")) {
									fromType.setFromRevision(fValue);
								} else if (fName.equalsIgnoreCase("clone")) {
									fromType.setFromClone(fValue);
								} else if (fName.equalsIgnoreCase("propagate modify")) {
									fromType.setFromProModify(fValue);
								} else if (fName.equalsIgnoreCase("propagate connection")) {
									fromType.setFromProconnection(fValue);
								}
							} else {
								if (fName.equalsIgnoreCase("type")) {
									toType.setToType(fValue);
								} else if (fName.equalsIgnoreCase("relationship")) {
									toType.setToRel(fValue);
								} else if (fName.equalsIgnoreCase("meaning")) {
									toType.setToMeaning(fValue);
								} else if (fName.equalsIgnoreCase("cardinality")) {
									toType.setToCardinality(fValue);
								} else if (fName.equalsIgnoreCase("revision")) {
									toType.setToRevision(fValue);
								} else if (fName.equalsIgnoreCase("clone")) {
									toType.setToClone(fValue);
								} else if (fName.equalsIgnoreCase("propagate modify")) {
									toType.setToProModify(fValue);
								} else if (fName.equalsIgnoreCase("propagate connection")) {
									toType.setToProconnection(fValue);
								}
							}
						}
					}
				}
				relationship.setFromType(fromType);
				relationship.setToType(toType);
				lstRelationship.add(relationship);
			}

			prepareRelationshipMQL(context, lstRelationship, schema_done_log, scmConfigProperty);

		} catch (UnmarshalException ume) {
			schema_done_log.error("UnmarshalException on ["+strSchemaName+".xml] : "+ume.getCause());
			schema_done_log.warn(UIUtil.sUnmarshalExceptionWarningMessage);
			throw ume;
		} catch (Exception e) {
			schema_done_log.error(""+e.getCause());
			throw e;
		}
	}

	private void prepareRelationshipMQL(Context context, List<Relationship> lstRelationship, Logger schema_done_log,
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
		String strRelationshipName = "";
		List<String> listModified = new ArrayList<String>();
		List<String> listAdded = new ArrayList<String>();
		List<String> listDeleted = new ArrayList<String>();
		try {

			Relationship relationship = null;
			Iterator<Relationship> itrRelationship = lstRelationship.iterator();
			iCountTotal = lstRelationship.size();
			while (itrRelationship.hasNext()) {
				relationship = itrRelationship.next();
				
				schema_done_log.info("Relationship : [" + relationship.name.replace(UIUtil.removeCharecter, "") + "]");
				strRelationshipName = relationship.name;
				ContextUtil.pushContext(context);
				try {
					String removeSchemaObject = UIUtil.removeSchemaObject(strRelationshipName, "relationship");
					if (removeSchemaObject.isEmpty()) {
						String strResult = MQLCommand.exec(context, "list relationship $1",
								new String[] { strRelationshipName.replace(UIUtil.removeCharecter, "") });
						if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
							sMQL = prepareModifyExistingSchemaMQL(relationship, schema_done_log);
							iCountModify += 1;
							listModified.add(strRelationshipName);
							sOperation = "mod";
						} else {
							sMQL = prepareAddNewSchemaMQL(relationship, schema_done_log);
							iCountAdd += 1;
							listAdded.add(strRelationshipName);
							sOperation = "add";
						}
						sMQLPropertyQuery = UIUtil.getMQLPropertyQuery(context, "relationship", relationship.name,
								relationship.registryname,sOperation);
					} else {
						sMQL = removeSchemaObject;
						iCountDelete += 1;
						listDeleted.add(strRelationshipName);
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
					throw new Exception("Error occurred while importing schema relationship : ["
							+ strRelationshipName.replace(UIUtil.removeCharecter, "") + "] : " + e.getMessage());
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

	private StringBuilder formToFromOfRel(StringBuilder sbMQL, String input, String param) {

		String[] sArray = input.split(",");

		for (String type : sArray) {
			sbMQL.append(" ").append(param).append(UIUtil.padWithSpaces(UIUtil.quoteArgument(type)));
		}
		return sbMQL;
	}

	private String prepareAddNewSchemaMQL(Object objectSchemaInfo, Logger schema_done_log) throws Exception {
		StringBuilder sbMQL = new StringBuilder();
		try {
			Relationship objInfo = (Relationship) objectSchemaInfo;
			sbMQL.append(" add relationship ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null && !objInfo.description.isEmpty()) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.getsAbstract() != null && !objInfo.getsAbstract().isEmpty()) {
				sbMQL.append(" abstract ").append(UIUtil.padWithSpaces(objInfo.getsAbstract()));
			}
			if (objInfo.getSparse() != null && !objInfo.getSparse().isEmpty()) {
				sbMQL.append(" sparse ").append(UIUtil.padWithSpaces(objInfo.getSparse()));
			}
			if (objInfo.getDerived() != null && !objInfo.getDerived().isEmpty()) {
				sbMQL.append(" derived ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.getDerived())));
			}
			if (objInfo.getPreventdups() != null && !objInfo.getPreventdups().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.getPreventdups())));
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
			// From
			if (objInfo.getFromType() != null) {
				Relationship.FromType fromType = objInfo.getFromType();
				sbMQL.append(" from ");
				if (fromType.getFromType() != null && !fromType.getFromType().isEmpty()
						&& !fromType.getFromType().equalsIgnoreCase("")) {
					sbMQL = formToFromOfRel(sbMQL, fromType.getFromType(), "type");
				}
				if (fromType.getFromRel() != null && !fromType.getFromRel().isEmpty()
						&& !fromType.getFromRel().equalsIgnoreCase("")) {
					sbMQL = formToFromOfRel(sbMQL, fromType.getFromRel(), "relationship");
				}
				if (fromType.getFromMeaning() != null && !fromType.getFromMeaning().isEmpty())
					sbMQL.append(" meaning ")
							.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(fromType.getFromMeaning())));
				if (fromType.getFromCardinality() != null && !fromType.getFromCardinality().isEmpty())
					sbMQL.append(" cardinality ")
							.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(fromType.getFromCardinality())));
				if (fromType.getFromRevision() != null && !fromType.getFromRevision().isEmpty())
					sbMQL.append(" revision ")
							.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(fromType.getFromRevision())));
				if (fromType.getFromClone() != null && !fromType.getFromClone().isEmpty())
					sbMQL.append(" clone ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(fromType.getFromClone())));
				if (fromType.getFromProModify() != null && !fromType.getFromProModify().isEmpty())
					sbMQL.append(UIUtil.padWithSpaces(fromType.getFromProModify()));
				if (fromType.getFromProconnection() != null && !fromType.getFromProconnection().isEmpty())
					sbMQL.append(UIUtil.padWithSpaces(fromType.getFromProconnection()));
			}
			// To
			if (objInfo.getToType() != null) {
				Relationship.ToType toType = objInfo.getToType();
				sbMQL.append(" to ");
				if (toType.getToType() != null && !toType.getToType().isEmpty()
						&& !toType.getToType().equalsIgnoreCase("")) {
					sbMQL = formToFromOfRel(sbMQL, toType.getToType(), "type");
				}
				if (toType.getToRel() != null && !toType.getToRel().isEmpty()
						&& !toType.getToRel().equalsIgnoreCase("")) {
					sbMQL = formToFromOfRel(sbMQL, toType.getToRel(), "relationship");
				}
				if (toType.getToMeaning() != null && !toType.getToMeaning().isEmpty())
					sbMQL.append(" meaning ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(toType.getToMeaning())));
				if (toType.getToCardinality() != null && !toType.getToCardinality().isEmpty())
					sbMQL.append(" cardinality ")
							.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(toType.getToCardinality())));
				if (toType.getToRevision() != null && !toType.getToRevision().isEmpty())
					sbMQL.append(" revision ")
							.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(toType.getToRevision())));
				if (toType.getToClone() != null && !toType.getToClone().isEmpty())
					sbMQL.append(" clone ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(toType.getToClone())));
				if (toType.getToProModify() != null && !toType.getToProModify().isEmpty())
					sbMQL.append(UIUtil.padWithSpaces(toType.getToProModify()));
				if (toType.getToProconnection() != null && !toType.getToProconnection().isEmpty())
					sbMQL.append(UIUtil.padWithSpaces(toType.getToProconnection()));
			}
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}

	private String prepareModifyExistingSchemaMQL(Object objectSchemaInfo, Logger schema_done_log) throws Exception {
		StringBuilder sbMQL = new StringBuilder();
		try {
			String[] sArray;
			Relationship objInfo = (Relationship) objectSchemaInfo;
			sbMQL.append(" mod relationship ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null && !objInfo.description.isEmpty()) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.getsAbstract() != null && !objInfo.getsAbstract().isEmpty()) {
				sbMQL.append(" abstract ").append(UIUtil.padWithSpaces(objInfo.getsAbstract()));
			}
			if (objInfo.getSparse() != null && !objInfo.getSparse().isEmpty()) {
				sbMQL.append(" sparse ").append(UIUtil.padWithSpaces(objInfo.getSparse()));
			}
			if (objInfo.getDerived() != null && !objInfo.getDerived().isEmpty()) {
				sbMQL.append(" derived ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.getDerived())));
			}
			if (objInfo.getPreventdups() != null && !objInfo.getPreventdups().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.getPreventdups())));
			}
			if (objInfo.getAttributes() != null && !objInfo.getAttributes().isEmpty()) {
				String[] sNewAttributes = objInfo.getAttributes().split(",");
				for (String sAttribute : sNewAttributes) {
					sbMQL.append(UIUtil.removeFieldDetail(sAttribute, "attribute"));
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
			// From
			if (objInfo.getFromType() != null) {
				Relationship.FromType fromType = objInfo.getFromType();
				sbMQL.append(" from ");
				if (fromType.getFromType() != null && !fromType.getFromType().isEmpty()
						&& !fromType.getFromType().equalsIgnoreCase("")) {
					sArray = fromType.getFromType().split(",");
					for (String type : sArray) {
						sbMQL.append(UIUtil.removeFieldDetail(type, "type"));
					}
				}
				if (fromType.getFromRel() != null && !fromType.getFromRel().isEmpty()
						&& !fromType.getFromRel().equalsIgnoreCase("")) {
					sArray = fromType.getFromRel().split(",");
					for (String type : sArray) {
						sbMQL.append(UIUtil.removeFieldDetail(type, "relationship"));
					}
				}
				String fromTypeMeaning = "";
				if(fromType.getFromMeaning() != null){
					fromTypeMeaning = fromType.getFromMeaning().trim();
				}
				if (fromTypeMeaning != null && !fromTypeMeaning.isEmpty()){
					sbMQL.append(" meaning ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(fromType.getFromMeaning())));
				} else {
					sbMQL.append(" meaning ").append("\"\"");
				}
				
				if (fromType.getFromCardinality() != null && !fromType.getFromCardinality().isEmpty())
					sbMQL.append(" cardinality ")
							.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(fromType.getFromCardinality())));
				if (fromType.getFromRevision() != null && !fromType.getFromRevision().isEmpty())
					sbMQL.append(" revision ")
							.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(fromType.getFromRevision())));
				if (fromType.getFromClone() != null && !fromType.getFromClone().isEmpty())
					sbMQL.append(" clone ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(fromType.getFromClone())));
				if (fromType.getFromProModify() != null && !fromType.getFromProModify().isEmpty())
					sbMQL.append(UIUtil.padWithSpaces(fromType.getFromProModify()));
				if (fromType.getFromProconnection() != null && !fromType.getFromProconnection().isEmpty())
					sbMQL.append(UIUtil.padWithSpaces(fromType.getFromProconnection()));
			}
			// To
			if (objInfo.getToType() != null) {
				Relationship.ToType toType = objInfo.getToType();
				sbMQL.append(" to ");
				if (toType.getToType() != null && !toType.getToType().isEmpty()
						&& !toType.getToType().equalsIgnoreCase("")) {
					sArray = toType.getToType().split(",");
					for (String type : sArray) {
						sbMQL.append(UIUtil.removeFieldDetail(type, "type"));
					}
				}
				if (toType.getToRel() != null && !toType.getToRel().isEmpty()
						&& !toType.getToRel().equalsIgnoreCase("")) {
					sArray = toType.getToRel().split(",");
					for (String type : sArray) {
						sbMQL.append(UIUtil.removeFieldDetail(type, "relationship"));
					}
				}
				String toTypeMeaning = "";
				if(toType.getToMeaning() != null){
					toTypeMeaning = toType.getToMeaning().trim();
				}
				if (toTypeMeaning != null && !toTypeMeaning.isEmpty()){
					sbMQL.append(" meaning ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(toType.getToMeaning())));
				} else {
					sbMQL.append(" meaning ").append("\"\"");
				}
				
				if (toType.getToCardinality() != null && !toType.getToCardinality().isEmpty())
					sbMQL.append(" cardinality ")
							.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(toType.getToCardinality())));
				if (toType.getToRevision() != null && !toType.getToRevision().isEmpty())
					sbMQL.append(" revision ")
							.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(toType.getToRevision())));
				if (toType.getToClone() != null && !toType.getToClone().isEmpty())
					sbMQL.append(" clone ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(toType.getToClone())));
				if (toType.getToProModify() != null && !toType.getToProModify().isEmpty())
					sbMQL.append(UIUtil.padWithSpaces(toType.getToProModify()));
				if (toType.getToProconnection() != null && !toType.getToProconnection().isEmpty())
					sbMQL.append(UIUtil.padWithSpaces(toType.getToProconnection()));
			}
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}
}
