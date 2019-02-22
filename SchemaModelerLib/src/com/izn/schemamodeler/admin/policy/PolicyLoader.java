package com.izn.schemamodeler.admin.policy;

import java.io.File;
import java.util.ArrayList;
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
import com.izn.schemamodeler.admin.policy.PolicyInfo;
import com.izn.schemamodeler.admin.policy.PolicyXMLSchema;
import com.izn.schemamodeler.admin.policy.Policy;
import com.izn.schemamodeler.admin.policy.PolicyXMLSchema.Schema;
import com.izn.schemamodeler.admin.policy.PolicyXMLSchema.Schema.Allstate;
import com.izn.schemamodeler.admin.policy.PolicyXMLSchema.Schema.Allstate.Allstateaccess;
import com.izn.schemamodeler.admin.policy.PolicyXMLSchema.Schema.Basic;
import com.izn.schemamodeler.admin.policy.PolicyXMLSchema.Schema.Field;
import com.izn.schemamodeler.admin.policy.PolicyXMLSchema.Schema.Field.Detail;
import com.izn.schemamodeler.admin.policy.PolicyXMLSchema.Schema.State;
import com.izn.schemamodeler.admin.policy.PolicyXMLSchema.Schema.State.Signature;
import com.izn.schemamodeler.admin.policy.PolicyXMLSchema.Schema.State.Signature.Signaturebasic;
import com.izn.schemamodeler.admin.policy.PolicyXMLSchema.Schema.State.Signature.Signaturebasic.Signaturedetail;
import com.izn.schemamodeler.admin.policy.PolicyXMLSchema.Schema.State.Statebasic;
import com.izn.schemamodeler.admin.policy.PolicyXMLSchema.Schema.State.Statebasic.Access;
import com.izn.schemamodeler.admin.policy.PolicyXMLSchema.Schema.State.Statebasic.Access.Accessdetails;
import com.izn.schemamodeler.admin.policy.PolicyXMLSchema.Schema.State.Statebasic.Statedetail;
import com.izn.schemamodeler.admin.policy.PolicyXMLSchema.Schema.State.Statebasic.Trigger;
import com.izn.schemamodeler.admin.policy.PolicyXMLSchema.Schema.State.Statebasic.Trigger.Event;
import com.izn.schemamodeler.admin.policy.PolicyXMLSchema.Schema.State.Statebasic.Trigger.Event.Eventdetail;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MqlUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class PolicyLoader implements SchemaLoader {
	ObjectMapper _gson = new ObjectMapper();
	Basic _basicElem = null;
	Field _fieldElem = null;
	Allstate _allstate = null;
	Statebasic _statebasic = null;
	Trigger _trigger = null;
	Access _access = null;
	Signature _signature = null;

	public PolicyLoader() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void loadSchema(Context context, String strSchemaName, String fileName, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty)  throws Exception{
		try {
			List<Policy> lstPolicy = new ArrayList<Policy>();
			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
			PolicyInfo policyInfo = (PolicyInfo) schemaFactory.getSchemaObject("policy");

			JAXBContext jConext = JAXBContext.newInstance(PolicyXMLSchema.class);
			Unmarshaller unmarshaller = jConext.createUnmarshaller();
			JAXBElement<PolicyXMLSchema> policyElem = unmarshaller.unmarshal(new StreamSource(new File(fileName)),
					PolicyXMLSchema.class);
			PolicyXMLSchema policyXMLSchema = policyElem.getValue();
			List<Schema> lstSchema = policyXMLSchema.getSchema();
			Policy policy = null;
			String fValue = "";
			String fName = "";
			Map<String, String> mSettings = null;
			List<Map<String, String>> lstSetting = null;
			List<String> lstPolicyItem = null;
			List<String> lstCommandItem = null;
			List<Map> lstAllState = null;
			for (Schema _schema : lstSchema) {
				lstSetting = new ArrayList<Map<String, String>>();
				lstAllState = new ArrayList<Map>();
				lstCommandItem = new ArrayList<String>();
				lstPolicyItem = new ArrayList<String>();
				_basicElem = _schema.getBasic();
				policy = new Policy(_basicElem.getName(), _basicElem.getDescription(), _basicElem.getHidden(),
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
					if (fName.equalsIgnoreCase("store")) {
						policy.setStore(fValue);
					} else if (fName.equalsIgnoreCase("minorsequence")) {
						policy.setMinorsequence(fValue);
					} else if (fName.equalsIgnoreCase("format")) {
						policy.setFormat(fValue);
					} else if (fName.equalsIgnoreCase("type")) {
						policy.setType(fValue);
					} else if (fName.equalsIgnoreCase("defaultformat")) {
						policy.setDefaultformat(fValue);
					} else if (fName.equalsIgnoreCase("enforcelocking")) {
						policy.setEnforcelocking(fValue);
					}
				}
				_allstate = _schema.getAllstate();
				if (_allstate != null) {
					String isEnable = _allstate.getEnabled();
					policy.setAllstateenabled(isEnable);
					List<Allstateaccess> lstAllStateAccess = _allstate.getAllstateaccess();
					Map mAsa = null;
					for (Allstateaccess asa : lstAllStateAccess) {
						mAsa = new HashMap();
						mAsa.put("user", asa.getUser());
						mAsa.put("access", asa.getAccess());
						mAsa.put("filter", asa.getFilter());
						
						mAsa.put("key", asa.getKey());
						mAsa.put("organization", asa.getOrganization());
						mAsa.put("project", asa.getProject());
						mAsa.put("maturity", asa.getMaturity());
						mAsa.put("Owner", asa.getOwner());
						mAsa.put("category", asa.getCategory());
						mAsa.put("reserve", asa.getReserve());
						
						lstAllState.add(mAsa);
					}
					policy.setLstAllState(lstAllState);
				}
				// process the state
				List<Statedetail> lstStateDetails = null;
				List<State> lstState = _schema.getState();
				List<Map<String, String>> slTrigger = null;
				List<Map<String, String>> lstAccess = null;
				List<Policy.State.Signature> lstSignature = null;
				List<Policy.State> lstStateObj = new ArrayList<Policy.State>();
				for (State state : lstState) {
					slTrigger = new ArrayList<Map<String, String>>();
					lstAccess = new ArrayList<Map<String, String>>();
					lstSignature = new ArrayList<Policy.State.Signature>();
					_statebasic = state.getStatebasic();
					Policy.State stateOb = policy.getNewStateIntance(_statebasic.getName(),
							_statebasic.getRegistryName());
					lstStateDetails = _statebasic.getStatedetail();
					for (Statedetail stateDetails : lstStateDetails) {
						fName = stateDetails.getName();
						fValue = stateDetails.getValueAttribute();
						if (fValue != null) {
							fValue = stateDetails.getValueAttribute().trim();
						} else {
							fValue = "";
						}
						if (fName.equalsIgnoreCase("promote")) {
							stateOb.setPromote(fValue);
						} else if (fName.equalsIgnoreCase("version")) {
							stateOb.setVersion(fValue);
						} else if (fName.equalsIgnoreCase("checkouthistory")) {
							stateOb.setCheckouthistory(fValue);
						} else if (fName.equalsIgnoreCase("minorrevisionable")) {
							stateOb.setMinorrevisionable(fValue);
						}
					}
					// tag trigger
					_trigger = _statebasic.getTrigger();
					if (_trigger != null) {
						List<Event> lstEvent = _trigger.getEvent();
						Map<String, String> mTriggerDetails = null;
						String strEvent = "";
						for (Event event : lstEvent) {
							// name of trigger event
							strEvent = event.getName();
							List<Eventdetail> lstEventdetails = event.getEventdetail();
							for (Eventdetail eventDetail : lstEventdetails) {
								mTriggerDetails = new HashMap<String, String>();
								mTriggerDetails.put(ACTION, strEvent);
								mTriggerDetails.put(TYPE, eventDetail.getType());
								mTriggerDetails.put(PROGRAM, eventDetail.getProgram());
								mTriggerDetails.put(NAME, eventDetail.getInput());
								slTrigger.add(mTriggerDetails);
							}
						}
						stateOb.setSlTriggers(slTrigger);
					}
					// Access tag
					_access = _statebasic.getAccess();
					if (_access != null) {
						List<Accessdetails> lstAccessDetails = _access.getAccessdetails();
						Map mAccess = null;
						for (Accessdetails accessDetails : lstAccessDetails) {
							mAccess = new HashMap<String, String>();
							mAccess.put("user", accessDetails.getUser());
							mAccess.put("access", accessDetails.getAccess());
							mAccess.put("filter", accessDetails.getFilter());
							mAccess.put("key", accessDetails.getKey());
							mAccess.put("organization", accessDetails.getOrganization());
							mAccess.put("project", accessDetails.getProject());
							mAccess.put("maturity", accessDetails.getMaturity());
							mAccess.put("Owner", accessDetails.getOwner());
							mAccess.put("category", accessDetails.getCategory());
							mAccess.put("reserve", accessDetails.getReserve());
							lstAccess.add(mAccess);
						}
						stateOb.setSlAccess(lstAccess);
					}
					// process signature
					_signature = state.getSignature();
					if (_signature != null) {
						List<Signaturebasic> lstSignaBasic = _signature.getSignaturebasic();
						for (Signaturebasic signatureBasic : lstSignaBasic) {

							Policy.State.Signature signature = stateOb.getNewSignatureInstance(signatureBasic.getName(),
									signatureBasic.getFromState(), signatureBasic.getToState());
							List<Signaturedetail> signaturedetail = signatureBasic.getSignaturedetail();
							for (Signaturedetail signaturDetail : signaturedetail) {
								fValue = signaturDetail.getValueAttribute();
								if (signaturDetail.getApprove() != null && !signaturDetail.getApprove().isEmpty()) {
									signature.setApprove(signaturDetail.getApprove().trim());
								}
								if (signaturDetail.getBranch() != null) {
									signature.setBranch(signaturDetail.getBranch().trim());
								}
								if (signaturDetail.getIgnore() != null && !signaturDetail.getIgnore().isEmpty()) {
									signature.setIgnore(signaturDetail.getIgnore().trim());
								}
								if (signaturDetail.getReject() != null && !signaturDetail.getReject().isEmpty()) {
									signature.setReject(signaturDetail.getReject().trim());
								}
								if (signaturDetail.getFilter() != null) {
									signature.setFilter(signaturDetail.getFilter().trim());
								}
							}
							lstSignature.add(signature);
						}
						stateOb.setLstSignature(lstSignature);
					}
					lstStateObj.add(stateOb);
				}
				policy.setLstState(lstStateObj);
				lstPolicy.add(policy);
			}
			preparePolicyMQL(context, lstPolicy, schema_done_log, scmConfigProperty);
		} catch (UnmarshalException ume) {
			schema_done_log.error("UnmarshalException on ["+strSchemaName+".xml] : "+ume.getCause());
			schema_done_log.warn(UIUtil.sUnmarshalExceptionWarningMessage);
			throw ume;
		} catch (Exception e) {
			schema_done_log.error(""+e.getCause());
			throw e;
		}
	}

	private void preparePolicyMQL(Context context, List<Policy> lstPolicy, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty) throws Exception {
		PolicyInfo policyInfo = null;
		SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
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
		String strpolicyName = "";
		List<String> listModified = new ArrayList<String>();
		List<String> listAdded = new ArrayList<String>();
		List<String> listDeleted = new ArrayList<String>();
		try {
			policyInfo = (PolicyInfo) schemaFactory.getSchemaObject("policy");
			iCountTotal = lstPolicy.size();
			for (Policy policy : lstPolicy) {			
				schema_done_log.info("Policy : [" + policy.name.replace(UIUtil.removeCharecter, "") + "]");
				strpolicyName = policy.name;
				ContextUtil.pushContext(context);
				try {
					String removeSchemaObject = UIUtil.removeSchemaObject(strpolicyName, "policy");
					if (removeSchemaObject.isEmpty()) {
						String strResult = MQLCommand.exec(context, "list policy $1",
								new String[] { strpolicyName.replace(UIUtil.removeCharecter, "") });
						if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
							sMQL = prepareModifyExistingSchemaMQL(context, policy, schema_done_log);
							iCountModify += 1;
							listModified.add(strpolicyName);
							sOperation = "mod";
						} else {
							sMQL = prepareAddNewSchemaMQL(policy, schema_done_log);
							iCountAdd += 1;
							listAdded.add(strpolicyName);
							sOperation = "add";
						}
						sMQLPropertyQuery = UIUtil.getMQLPropertyQuery(context, "policy", policy.name,
								policy.registryname,sOperation);
					} else {
						sMQL = removeSchemaObject;
						iCountDelete += 1;
						listDeleted.add(strpolicyName);
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
					throw new Exception("Error occurred while importing schema policy : ["
							+ strpolicyName.replace(UIUtil.removeCharecter, "") + "] : " + e.getMessage());
				}
				schema_done_log.info("-----------------------------------------------------------------");
			}
			schema_done_log.info("TOTAL  :" + iCountTotal + ", SUCCESS :" + iCountSuccess  + ", ADDED  :" + iCountAdd + ", MODIFIED :" + iCountModify + ", DELETED :"
					+ iCountDelete + ".");
			if ("true".equalsIgnoreCase(scmConfigProperty.getLogEverything())) {
				schema_done_log.info("ADDED\t\t:"+listAdded.toString());
				schema_done_log.info("MODIFIED\t:"+listModified.toString());
				schema_done_log.info("DELETED\t:"+listDeleted.toString());
			}
		} catch (Exception e) {
			throw e; // TODO: handle exception
		}
	}

	private String prepareAddNewSchemaMQL(Object objectSchemaInfo, Logger schema_done_log) throws Exception {
		StringBuilder sbMQL = new StringBuilder();
		try {
			String sUser = "";
			String sFilter = "";
			String skey="";
			String sOrganization = "";
			String sProject ="";
			String sMaturity = "";
			String sOwner ="";
			String sCategory = "";
			String sReserve = "";
			String sAccess = "";
			
			Policy objInfo = (Policy) objectSchemaInfo;
			sbMQL.append(" add policy ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null && !objInfo.description.isEmpty()) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.store != null && !objInfo.store.isEmpty()) {
				sbMQL.append(" store").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.store)));
			}
			if (objInfo.getEnforcelocking() != null && !objInfo.getEnforcelocking().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.getEnforcelocking()));
			}
			if (UIUtil.quoteArgument(objInfo.minorsequence) != null
					&& !"".equals(UIUtil.quoteArgument(objInfo.minorsequence))) {
				sbMQL.append(" sequence '")
						.append(UIUtil.quoteArgument((objInfo.minorsequence) != null ? (objInfo.minorsequence) : ""))
						.append("'");
			}
			if (objInfo.getDefaultformat() != null && !"".equals(objInfo.getDefaultformat())) {
				sbMQL.append(" defaultformat ").append(UIUtil.singleQuotes(objInfo.getDefaultformat()));
			}
			if (objInfo.getFormat() != null && !objInfo.getFormat().isEmpty()) {
				String[] arrFormats = objInfo.getFormat().split(",");
				for (int i = 0; i < arrFormats.length; i++) {
					if (UIUtil.singleQuotes(arrFormats[i]) != null && !"".equals(UIUtil.singleQuotes(arrFormats[i]))
							&& !"''".equals(UIUtil.singleQuotes(arrFormats[i]))) {
						sbMQL.append(" format ").append(UIUtil.singleQuotes(arrFormats[i]));
					}
				}
			}
			if (objInfo.getType() != null && !"".equals(objInfo.getType())) {
				String[] stypes = objInfo.getType().split(",");
				for (int i = 0; i < stypes.length; i++) {
					if (UIUtil.singleQuotes(stypes[i]) != null && !"".equals(UIUtil.singleQuotes(stypes[i]))
							&& !"''".equals(UIUtil.singleQuotes(stypes[i]))) {
						sbMQL.append(" type ").append(UIUtil.singleQuotes(stypes[i]));
					}
				}
			}
			// All State
			Boolean bAllStatetEnabled = Boolean.valueOf(objInfo.getAllstateenabled());
			if (bAllStatetEnabled.booleanValue()) {
				sbMQL.append(" allstate");
				List<Map> lstAllStateNew = objInfo.getLstAllState();
				for (Map m : lstAllStateNew) {
					sUser = (String) m.get("user");
					if (sUser.equalsIgnoreCase("owner") || sUser.equalsIgnoreCase("public")) {
						sbMQL.append("").append(UIUtil.singleQuoteWithSpace((String) m.get("user")));
								//.append(UIUtil.padWithSpaces((String) m.get("access")));
					} else {
						sbMQL.append(" user ").append(UIUtil.singleQuoteWithSpace((String) m.get("user")));
								//.append(UIUtil.padWithSpaces((String) m.get("access")));
					}
					skey = (String) m.get("key");
					if (skey != null && !skey.isEmpty()) {
						sbMQL.append(" key ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(skey)));
					}
					sAccess = (String) m.get("access");
					if (sAccess != null && !sAccess.isEmpty()) {
						sbMQL.append(sAccess).append(" ");
					}
					
					sOrganization = (String) m.get("organization");
					if (sOrganization != null && !sOrganization.isEmpty()) {
						sbMQL.append(sOrganization).append(" organization ");
					}
					sProject = (String) m.get("project");
					if (sProject != null && !sProject.isEmpty()) {
						sbMQL.append(sProject).append(" project ");
					}
					
					sMaturity = (String) m.get("maturity");
					if (sMaturity != null && !sMaturity.isEmpty()) {
						sbMQL.append(sMaturity).append(" maturity ");
					}
					
					sOwner = (String) m.get("Owner");
					if (sOwner != null && !sOwner.isEmpty()) {
						sbMQL.append(sOwner).append(" owner ");
					}
					
					sCategory = (String) m.get("category");
					if (sCategory != null && !sCategory.isEmpty()) {
						sbMQL.append(sCategory).append(" category ");
					}
					
					sReserve = (String) m.get("reserve");
					if (sReserve != null && !sReserve.isEmpty()) {
						sbMQL.append(sReserve).append(" reserve ");
					}
				
					sFilter = (String) m.get("filter");
					if (sFilter != null && !sFilter.isEmpty()) {
						sbMQL.append("filter ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sFilter)));
					}
				}
			}
			// State
			List<Policy.State> lstState = (List<Policy.State>) objInfo.getLstState();
			if (lstState != null && !lstState.isEmpty()) {
				String sApprove = "";
				String sIgnore = "";
				String sReject = "";
				String strType = "";
				String strAction = "";
				String strName = "";
				String strProgram = "";
				for (Policy.State pState : lstState) {
					sbMQL.append(" state").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(pState.statename)));
					if (pState.regname != null && !"".equals(pState.regname)) {
						sbMQL.append(" property ").append(pState.regname).append(" value ").append(UIUtil.singleQuoteWithSpace(pState.statename));
					}
					if (pState.getCheckouthistory() != null && !"".equals(pState.getCheckouthistory())) {
						sbMQL.append(" checkouthistory ").append(pState.getCheckouthistory());
					}
					if (pState.getMinorrevisionable() != null && !"".equals(pState.getMinorrevisionable())) {
						sbMQL.append(" revision ").append(pState.getMinorrevisionable());
					}
					if (pState.getVersion() != null && !"".equals(pState.getVersion())) {
						sbMQL.append(" version ").append(pState.getVersion());
					}
					if (pState.getPromote() != null && !"".equals(pState.getPromote())) {
						sbMQL.append(" promote ").append(pState.getPromote());
					}
					// User Access
					List<Map<String, String>> lstAccess = (List<Map<String, String>>) pState.getSlAccess();
					if (lstAccess != null && !lstAccess.isEmpty()) {
						for (Map<String, String> m : lstAccess) {
							sUser = (String) m.get("user");
							if (sUser.equalsIgnoreCase("owner") || sUser.equalsIgnoreCase("public")) {
								sbMQL.append("").append(UIUtil.singleQuoteWithSpace(sUser));
										//.append(UIUtil.padWithSpaces((String) m.get("access")));
							} else {
								sbMQL.append(" user ").append(UIUtil.singleQuoteWithSpace(sUser));
										//.append(UIUtil.padWithSpaces((String) m.get("access")));
							}
							skey = (String) m.get("key");
							if (skey != null && !skey.isEmpty()) {
								sbMQL.append(" key ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(skey)));
							}
							sAccess = (String) m.get("access");
							if (sAccess != null && !sAccess.isEmpty()) {
								sbMQL.append(sAccess).append(" ");
							}
							sOrganization = (String) m.get("organization");
							if (sOrganization != null && !sOrganization.isEmpty()) {
								sbMQL.append(sOrganization).append(" organization ");
							}
							sProject = (String) m.get("project");
							if (sProject != null && !sProject.isEmpty()) {
								sbMQL.append(sProject).append(" project ");
							}
							
							sMaturity = (String) m.get("maturity");
							if (sMaturity != null && !sMaturity.isEmpty()) {
								sbMQL.append(sMaturity).append(" maturity ");
							}
							
							sOwner = (String) m.get("Owner");
							if (sOwner != null && !sOwner.isEmpty()) {
								sbMQL.append(sOwner).append(" owner ");
							}
							
							sCategory = (String) m.get("category");
							if (sCategory != null && !sCategory.isEmpty()) {
								sbMQL.append(sCategory).append(" category ");
							}
							
							sReserve = (String) m.get("reserve");
							if (sReserve != null && !sReserve.isEmpty()) {
								sbMQL.append(sReserve).append(" reserve ");
							}
							sFilter = (String) m.get("filter");
							if (sFilter != null && !sFilter.isEmpty()) {
								sbMQL.append(" filter ")
										.append(UIUtil.padWithSpaces(UIUtil.quoteArgument((String) m.get("filter"))));
							}
						}
					}
					// Process triggers if any
					List<Map<String, String>> lstTrigegrs = pState.getSlTriggers();
					if (lstTrigegrs != null && !lstTrigegrs.isEmpty()) {
						for (Map<String, String> m : lstTrigegrs) {
							strType = (String) m.get("type");
							strAction = (String) m.get("action");
							strName = (String) m.get("name");
							strProgram = (String) m.get("program");
							sbMQL.append(" TRIGGER").append(UIUtil.padWithSpaces(strAction)).append(strType)
									.append(UIUtil.padWithSpaces(strProgram)).append("input")
									.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(strName)));
						}
					}
					// process signature if any
					List<Policy.State.Signature> lstPSS = pState.getLstSignature();
					if (lstPSS != null && !lstPSS.isEmpty()) {
						for (Policy.State.Signature pss : lstPSS) {
							sbMQL.append(" signature ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(pss.name)));
							sApprove = pss.getApprove();
							sIgnore = pss.getIgnore();
							sReject = pss.getReject();
							sFilter = pss.getFilter();
							if (sApprove != null && !sApprove.isEmpty())
								sbMQL.append(" approve ")
										.append(UIUtil.padWithSpaces(UIUtil.removeArgumentWithQuote(sApprove)));
							if (sIgnore != null && !sIgnore.isEmpty())
								sbMQL.append(" ignore ")
										.append(UIUtil.padWithSpaces(UIUtil.removeArgumentWithQuote(sIgnore)));
							if (sReject != null && !sReject.isEmpty())
								sbMQL.append(" reject ")
										.append(UIUtil.padWithSpaces(UIUtil.removeArgumentWithQuote(sReject)));
							if (pss.getBranch() != null && !pss.getBranch().isEmpty())
								sbMQL.append(" branch ")
										.append(UIUtil.padWithSpaces(UIUtil.removeArgumentWithQuote(pss.getBranch())));
							if (sFilter != null && !sFilter.isEmpty())
								sbMQL.append(" filter ")
										.append(UIUtil.padWithSpaces(UIUtil.removeArgumentWithQuote(sFilter)));
						}
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}

	private String prepareModifyExistingSchemaMQL(Context context, Object objectSchemaInfo, Logger schema_done_log)
			throws Exception {
		StringBuilder sbMQL = new StringBuilder();
		try {
			String sUser = "";
			String sFilter = "";
			String skey="";
			String sOrganization = "";
			String sProject ="";
			String sMaturity = "";
			String sOwner ="";
			String sCategory = "";
			String sReserve = "";
			String sAccess = "";
			Policy objInfo = (Policy) objectSchemaInfo;
			sbMQL.append(" mod policy ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.store != null && !objInfo.store.isEmpty()) {
				sbMQL.append(" store ").append('"').append(objInfo.store).append('"');
			}
			if (objInfo.getEnforcelocking() != null && !objInfo.getEnforcelocking().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.getEnforcelocking()));
			}
			if (UIUtil.quoteArgument(objInfo.minorsequence) != null) {
				sbMQL.append(" sequence '")
						.append(UIUtil.quoteArgument((objInfo.minorsequence) != null ? (objInfo.minorsequence) : ""))
						.append("'");
			}
			if (objInfo.getDefaultformat() != null) {
				sbMQL.append(" defaultformat ").append(UIUtil.singleQuotes(objInfo.getDefaultformat()));
			}
			if (objInfo.getFormat() != null && !objInfo.getFormat().isEmpty()) {
				String[] arrFormats = objInfo.getFormat().split(",");
				for (int i = 0; i < arrFormats.length; i++) {
					if (UIUtil.singleQuotes(arrFormats[i]) != null && !"".equals(UIUtil.singleQuotes(arrFormats[i]))
							&& !"''".equals(UIUtil.singleQuotes(arrFormats[i]))) {
						sbMQL.append(UIUtil.removeFieldDetail(arrFormats[i], "format"));
						// sbMQL.append(" add format ").append(UIUtil.singleQuotes(arrFormats[i]));
					}
				}
			}
			if (objInfo.getType() != null && !"".equals(objInfo.getType())) {
				String[] stypes = objInfo.getType().split(",");
				for (int i = 0; i < stypes.length; i++) {
					if (UIUtil.singleQuotes(stypes[i]) != null && !"".equals(UIUtil.singleQuotes(stypes[i]))
							&& !"''".equals(UIUtil.singleQuotes(stypes[i]))) {
						// sbMQL.append(" add type ").append(UIUtil.singleQuotes(stypes[i]));
						sbMQL.append(UIUtil.removeFieldDetail(stypes[i], "type"));
					}
				}
			}
			// All State
			String sIsAllState = MqlUtil.mqlCommand(context, "print policy $1 select allstate dump",
					new String[] { objInfo.name });
			Boolean bAllStatetEnabled = Boolean.valueOf(objInfo.getAllstateenabled());
			if (bAllStatetEnabled.booleanValue()) {
				if ("TRUE".equalsIgnoreCase(sIsAllState))
					sbMQL.append(" allstate ");
				else
					sbMQL.append(" add allstate ");
				List<Map> lstAllStateNew = objInfo.getLstAllState();
				for (Map m : lstAllStateNew) {
					sUser = (String) m.get("user");
					skey = (String) m.get("key");
					if (sUser.equalsIgnoreCase("owner") || sUser.equalsIgnoreCase("public")) {
						sbMQL.append("").append(UIUtil.singleQuoteWithSpace((String) m.get("user")));
						if (skey != null && !skey.isEmpty()) {
							sbMQL.append(" key ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(skey)));
						}
						sbMQL.append(UIUtil.padWithSpaces((String) m.get("access")));
					} else {
						sbMQL.append(UIUtil.removeFieldDetail(sUser, (String) m.get("access"), "user",skey));
					}
					sOrganization = (String) m.get("organization");
					if (sOrganization != null && !sOrganization.isEmpty()) {
						sbMQL.append(sOrganization).append(" organization ");
					}
					sProject = (String) m.get("project");
					if (sProject != null && !sProject.isEmpty()) {
						sbMQL.append(sProject).append(" project ");
					}
					
					sMaturity = (String) m.get("maturity");
					if (sMaturity != null && !sMaturity.isEmpty()) {
						sbMQL.append(sMaturity).append(" maturity ");
					}
					
					sOwner = (String) m.get("Owner");
					if (sOwner != null && !sOwner.isEmpty()) {
						sbMQL.append(sOwner).append(" owner ");
					}
					
					sCategory = (String) m.get("category");
					if (sCategory != null && !sCategory.isEmpty()) {
						sbMQL.append(sCategory).append(" category ");
					}
					
					sReserve = (String) m.get("reserve");
					if (sReserve != null && !sReserve.isEmpty()) {
						sbMQL.append(sReserve).append(" reserve ");
					}
					sFilter = (String) m.get("filter");
					if (sFilter != null && !sFilter.isEmpty()) {
						sbMQL.append("filter ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sFilter)));
					}
				}
			} else {
				if ("TRUE".equalsIgnoreCase(sIsAllState))
					sbMQL.append(" remove allstate ");
			}
			// State
			List<Policy.State> lstState = (List<Policy.State>) objInfo.getLstState();
			if (lstState != null && !lstState.isEmpty()) {
				String strAction = "";
				String[] args;
				List listExistingState = getExistingStates(context, objInfo.name);
				for (Policy.State pState : lstState) {
					if (pState.statename.startsWith(UIUtil.removeCharecter)
							&& pState.statename.endsWith(UIUtil.removeCharecter)) {
						String strStateName = pState.statename.replace(UIUtil.removeCharecter, "");
						if (listExistingState.contains(strStateName)) {
							sbMQL.append(" remove state").append(UIUtil.padWithSpaces(
									UIUtil.singleQuoteWithSpace(pState.statename.replace(UIUtil.removeCharecter, ""))));
						}
					} else if (!pState.statename.startsWith(UIUtil.removeCharecter)
							&& !pState.statename.endsWith(UIUtil.removeCharecter)) {
						if (listExistingState.contains(pState.statename)) {
							sbMQL.append(" state")
									.append(UIUtil.padWithSpaces(UIUtil.singleQuoteWithSpace(pState.statename)));
						} else {
							sbMQL.append(" add state")
									.append(UIUtil.padWithSpaces(UIUtil.singleQuoteWithSpace(pState.statename)));
						}

						if (pState.regname != null && !"".equals(pState.regname)) {
							sbMQL.append(" property ").append(pState.regname).append(" value ")
									.append(pState.statename);
						}
						if (pState.getCheckouthistory() != null && !"".equals(pState.getCheckouthistory())) {
							sbMQL.append(" checkouthistory ").append(pState.getCheckouthistory());
						}
						if (pState.getMinorrevisionable() != null && !"".equals(pState.getMinorrevisionable())) {
							sbMQL.append(" revision ").append(pState.getMinorrevisionable());
						}
						if (pState.getVersion() != null && !"".equals(pState.getVersion())) {
							sbMQL.append(" version ").append(pState.getVersion());
						}
						if (pState.getPromote() != null && !"".equals(pState.getPromote())) {
							sbMQL.append(" promote ").append(pState.getPromote());
						}
						// User Access
						List<Map<String, String>> lstAccess = (List<Map<String, String>>) pState.getSlAccess();
						StringBuilder sbOwnerPublicAccess = new StringBuilder();
						if (lstAccess != null && !lstAccess.isEmpty()) {
							for (Map<String, String> m : lstAccess) {
								sUser = (String) m.get("user");
								sFilter = (String) m.get("filter");
								skey = (String) m.get("key");
								
								if (sUser.equalsIgnoreCase("owner") || sUser.equalsIgnoreCase("public")) {
									String[] splitOwnerPublicAccess = ((String) m.get("access")).split(",");
									sbOwnerPublicAccess.setLength(0);
									for(int i=0; i<splitOwnerPublicAccess.length; i++){
										if(!splitOwnerPublicAccess[i].startsWith(UIUtil.removeCharecter) && !splitOwnerPublicAccess[i].endsWith(UIUtil.removeCharecter)){
											sbOwnerPublicAccess.append(splitOwnerPublicAccess[i]);
											if(i != splitOwnerPublicAccess.length-1)
												sbOwnerPublicAccess.append(",");
										}
									}
									if(!sbOwnerPublicAccess.toString().isEmpty())	{										
										sbMQL.append("").append(UIUtil.singleQuoteWithSpace(sUser));
										if (skey != null && !skey.isEmpty()) {
											sbMQL.append(" key ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(skey)));
										}
										sbMQL.append(UIUtil.padWithSpaces(sbOwnerPublicAccess.toString()));
									} else{										
										sbMQL.append("").append(UIUtil.singleQuoteWithSpace(sUser));
										if (skey != null && !skey.isEmpty()) {
											sbMQL.append(" key ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(skey)));
										}
										sbMQL.append(UIUtil.padWithSpaces("none")); 
									}
								} else {

									sbMQL.append(UIUtil.removeFieldDetail(sUser, (String) m.get("access"), "user",skey));
								}
								
								sOrganization = (String) m.get("organization");
								if (sOrganization != null && !sOrganization.isEmpty()) {
									sbMQL.append(sOrganization).append(" organization ");
								}
								sProject = (String) m.get("project");
								if (sProject != null && !sProject.isEmpty()) {
									sbMQL.append(sProject).append(" project ");
								}
								
								sMaturity = (String) m.get("maturity");
								if (sMaturity != null && !sMaturity.isEmpty()) {
									sbMQL.append(sMaturity).append(" maturity ");
								}
								
								sOwner = (String) m.get("Owner");
								if (sOwner != null && !sOwner.isEmpty()) {
									sbMQL.append(sOwner).append(" owner ");
								}
								
								sCategory = (String) m.get("category");
								if (sCategory != null && !sCategory.isEmpty()) {
									sbMQL.append(sCategory).append(" category ");
								}
								
								sReserve = (String) m.get("reserve");
								if (sReserve != null && !sReserve.isEmpty()) {
									sbMQL.append(sReserve).append(" reserve ");
								}
								if (sFilter != null && !sFilter.isEmpty()) {
									sbMQL.append(" filter ").append(UIUtil
											.padWithSpaces(UIUtil.singleQuoteWithSpace((String) m.get("filter"))));
								}
							}
						}
						// Process triggers if any
						List<Map<String, String>> lstTrigegrs = pState.getSlTriggers();
						if (lstTrigegrs != null && !lstTrigegrs.isEmpty()) {
							for (Map<String, String> m : lstTrigegrs) {
								strAction = (String) m.get("action");
								args = new String[4];
								args[0] = strAction;
								args[1] = (String) m.get("type");
								args[2] = (String) m.get("name");
								args[3] = (String) m.get("program");
								sbMQL.append(UIUtil.removeTrigger(strAction, args));
								// sbMQL.append(" ADD
								// TRIGGER").append(UIUtil.padWithSpaces(strAction)).append(strType).append(UIUtil.padWithSpaces(strProgram)).append("input").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(strName)));
							}
						}
						// process signature if any
						List<Policy.State.Signature> lstPSS = pState.getLstSignature();
						if (lstPSS != null && !lstPSS.isEmpty()) {
							for (Policy.State.Signature pss : lstPSS) {
								args = new String[5];
								args[0] = pss.getApprove();
								args[1] = pss.getIgnore();
								args[2] = pss.getReject();
								args[3] = pss.getBranch();
								args[4] = pss.getFilter();
								sbMQL.append(removeSignature(pss.name, args));
							}
						}
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}

	private String removeSignature(String param1, String[] param2) throws Exception {
		StringBuilder sbReturn = new StringBuilder();
		if (param1.startsWith(UIUtil.removeCharecter) && param1.endsWith(UIUtil.removeCharecter)) {
			param1 = param1.replace(UIUtil.removeCharecter, "");
			sbReturn.append(" remove signature").append(UIUtil.padWithSpaces(UIUtil.singleQuoteWithSpace(param1)));
		} else if (!param1.startsWith(UIUtil.removeCharecter) && !param1.endsWith(UIUtil.removeCharecter)) {
			sbReturn.append(" signature ").append(UIUtil.padWithSpaces(UIUtil.singleQuoteWithSpace(param1)));
			String sApprove = param2[0];
			String sIgnore = param2[1];
			String sReject = param2[2];
			String sBranch = param2[3];
			String sFilter = param2[4];
			String sSplit[];
			if (sApprove != null && !sApprove.isEmpty()) {
				sSplit = sApprove.split(",");
				for (String sValue : sSplit)
					sbReturn.append(UIUtil.removeFieldDetail(sValue, "approve"));
			}
			if (sIgnore != null && !sIgnore.isEmpty()) {
				sSplit = sIgnore.split(",");
				for (String sValue : sSplit)
					sbReturn.append(UIUtil.removeFieldDetail(sValue, "ignore"));
			}
			if (sReject != null && !sReject.isEmpty()) {
				sSplit = sReject.split(",");
				for (String sValue : sSplit)
					sbReturn.append(UIUtil.removeFieldDetail(sValue, "reject"));
			}
			if (sBranch != null) {
				if (sBranch.isEmpty())
					sbReturn.append("remove branch ");
				else
					sbReturn.append("add branch").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(sBranch)));
			}
			if (sFilter != null) {
				sbReturn.append(" add filter").append(UIUtil.padWithSpaces(UIUtil.singleQuotes(sFilter)));
			}
		}
		return sbReturn.toString();
	}

	private List getExistingStates(Context context, String policyname) throws Exception {
		List<String> listOldStates = new ArrayList<String>();
		String strResult = MqlUtil.mqlCommand(context, "print policy $1 select state dump",
				new String[] { policyname });
		if (strResult != null && !strResult.isEmpty())
			listOldStates = UIUtil.spiltSingleStringToken(context, ",", strResult);
		return listOldStates;
	}
}
