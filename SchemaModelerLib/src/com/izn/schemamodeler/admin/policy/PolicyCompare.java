package com.izn.schemamodeler.admin.policy;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.transform.stream.StreamSource;

import com.izn.schemamodeler.SchemaFactoryInfo;
import com.izn.schemamodeler.SchemaInfo;
import com.izn.schemamodeler.SchemaCompare;
import com.izn.schemamodeler.admin.policy.PolicyInfo;
import com.izn.schemamodeler.admin.policy.PolicyXMLSchema;
import com.izn.schemamodeler.admin.attribute.Attribute;
import com.izn.schemamodeler.admin.attribute.AttributeInfo;
import com.izn.schemamodeler.admin.attribute.AttributeXMLSchema;
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
import com.izn.schemamodeler.admin.type.Type;
import com.izn.schemamodeler.admin.type.TypeXMLSchema;
import com.izn.schemamodeler.ui3.webform.WebForm;
import com.izn.schemamodeler.ui3.webform.WebFormXMLSchema;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.SchemaModelerConstants;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MqlUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class PolicyCompare implements SchemaCompare {

	Basic _basicElem = null;
	Field _fieldElem = null;
	Allstate _allstate = null;
	Statebasic _statebasic = null;
	Trigger _trigger = null;
	Access _access = null;
	Signature _signature = null;
	String sAdminType = "";
	//For write xml
	String version = "";
	String compareResultFolder = "";
	public PolicyCompare() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void compareSchema(Context context, String strAdminName, Logger schema_done_log,	SCMConfigProperty scmConfigProperty) throws Exception{
		try {
			sAdminType = strAdminName;
			version = scmConfigProperty.getVersion();
			compareResultFolder = scmConfigProperty.getCompareFolder() + "\\" + SchemaModelerConstants.FOLDER_NAME_SchemaObject;
			Map<String,Object> list1 = readFromXML(context, scmConfigProperty.getFirstFilePath());
			Map<String,Object> list2 = readFromXML(context, scmConfigProperty.getSecondFilePath());
			Map<String, List> compareSchema_1 = processSchemaCompare(context,list1,list2);
			Map<String, List> compareSchema_2 = processSchemaCompare(context,list2,list1);
		    List<Object> matchList = compareSchema_1.get(SchemaModelerConstants.MATCH);
		    List<Object> deltaList_1 = compareSchema_1.get(SchemaModelerConstants.DELTA);
		    List<Object> deltaList_2 = compareSchema_2.get(SchemaModelerConstants.DELTA);
		    List<Object> uniqueList_1 = compareSchema_1.get(SchemaModelerConstants.UNIQUE);
		    List<Object> uniqueList_2 = compareSchema_2.get(SchemaModelerConstants.UNIQUE);
			
			schema_done_log.info(sAdminType +"|"+SchemaModelerConstants.MATCH_COUNT+matchList.size()+","+SchemaModelerConstants.DELTA_COUNT1+deltaList_1.size()+","+SchemaModelerConstants.DELTA_COUNT2+deltaList_2.size()+","+SchemaModelerConstants.UNIQUE_COUNT1+uniqueList_1.size()+","+SchemaModelerConstants.UNIQUE_COUNT2+uniqueList_2.size());
			
			writeToXML(context, SchemaModelerConstants.COMPARE_FILENAME+strAdminName+SchemaModelerConstants.COMPARE_MATCH, matchList);
			writeToXML(context, SchemaModelerConstants.COMPARE_FILENAME+strAdminName+SchemaModelerConstants.COMPARE_DELTA1, deltaList_1);
			writeToXML(context, SchemaModelerConstants.COMPARE_FILENAME+strAdminName+SchemaModelerConstants.COMPARE_DELTA2, deltaList_2);
			writeToXML(context, SchemaModelerConstants.COMPARE_FILENAME+strAdminName+SchemaModelerConstants.COMPARE_UNIQUE1, uniqueList_1);
			writeToXML(context, SchemaModelerConstants.COMPARE_FILENAME+strAdminName+SchemaModelerConstants.COMPARE_UNIQUE2, uniqueList_2);	
			
		} catch (UnmarshalException ume) {
			schema_done_log.error("UnmarshalException on ["+strAdminName+".xml] : "+ume.getCause());
			schema_done_log.warn(UIUtil.sUnmarshalExceptionWarningMessage);
			ume.printStackTrace();
			throw ume;
		} catch (Exception e) {
			schema_done_log.error(""+e.getCause());
			e.printStackTrace();
			throw e;
		}
	}
	
	private Map<String,List> processSchemaCompare(Context context, Map<String,Object> list1, Map<String,Object> list2)  throws Exception{
		Map<String,List> mCompareResultInfo = new HashMap<String,List>();
		try {
			List<Object> matchList = new ArrayList<Object>();
			List<Object> deltaList = new ArrayList<Object>();
			List<Object> uniqueList = new ArrayList<Object>();
			String sSchemaName = "";
			Policy obj1 = null;
			Policy obj2 = null;
			List<Object> listSchemaList1 = null;
			List<Object> listSchemaList2 = null;
			for (Map.Entry<String,Object> entry : list1.entrySet()) 
			{
				sSchemaName = entry.getKey();
				listSchemaList1 = (ArrayList)entry.getValue();
				obj1 = (Policy)listSchemaList1.get(0);
				if(list2.containsKey(sSchemaName))
				{
					listSchemaList2 = (ArrayList)list2.get(sSchemaName);
					obj2 = (Policy)listSchemaList2.get(0);
					if(UIUtil.isEqual(obj1.name,obj2.name) && UIUtil.isEqual(obj1.description,obj2.description) && UIUtil.isEqual(obj1.hidden,obj2.hidden) && UIUtil.isEqual(obj1.registryname,obj2.registryname) && UIUtil.isEqual(obj1.store,obj2.store) && UIUtil.isEqual(obj1.minorsequence,obj2.minorsequence) && UIUtil.isEqual(obj1.getEnforcelocking(),obj2.getEnforcelocking()) && UIUtil.isEqual(obj1.getDefaultformat(),obj2.getDefaultformat()) && UIUtil.isEqual(obj1.getFormat(),obj2.getFormat()) && UIUtil.isEqual(obj1.getType(),obj2.getType()) && UIUtil.isEqual(obj1.getAllstateenabled(),obj2.getAllstateenabled()) && UIUtil.isEqual(obj1.getAllstateenabled(),obj2.getAllstateenabled()) && isEqual(obj1.getLstMapState(), obj2.getLstMapState())) {
						matchList.add(listSchemaList1);
					}
					else {
						deltaList.add(listSchemaList1);
					}
				} else {
					uniqueList.add(listSchemaList1);
				}
			}
			mCompareResultInfo.put(SchemaModelerConstants.MATCH, matchList);
			mCompareResultInfo.put(SchemaModelerConstants.DELTA, deltaList);
			mCompareResultInfo.put(SchemaModelerConstants.UNIQUE, uniqueList);
		}catch (Exception e) {
			throw e;
		}
		return mCompareResultInfo;
	}
	
	private boolean isEqual(Map<String,Policy.State> list1, Map<String,Policy.State> list2) throws Exception{
		if(list1.size() != list2.size()){
			return false;
		}
		Policy.State obj1 = null;
		Policy.State obj2 = null;
		String sStateName = "";
		for (Map.Entry<String,Policy.State> entry : list1.entrySet())  
		{
			sStateName = entry.getKey();
			obj1 = (Policy.State)entry.getValue();
			if(list2.containsKey(sStateName))
			{
				obj2 = (Policy.State)list2.get(sStateName);
				if(!(UIUtil.isEqual(obj1.statename,obj2.statename) && UIUtil.isEqual(obj1.regname,obj2.regname) && UIUtil.isEqual(obj1.getCheckouthistory(),obj2.getCheckouthistory()) && UIUtil.isEqual(obj1.getMinorrevisionable(),obj2.getMinorrevisionable()) && UIUtil.isEqual(obj1.getVersion(),obj2.getVersion()) && UIUtil.isEqual(obj1.getPromote(),obj2.getPromote()) && UIUtil.isEqualStringMap(obj1.getSlAccess(),obj2.getSlAccess()) && UIUtil.isEqualStringMap(obj1.getSlTriggers(),obj2.getSlTriggers()) && isEqualSignature(obj1.lstMapStateSignature, obj2.lstMapStateSignature))) {					
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}
	
	private boolean isEqualSignature(Map<String,Policy.State.Signature> list1, Map<String,Policy.State.Signature> list2) throws Exception{
		if(list1.size() != list2.size()){
			return false;
		}
		Policy.State.Signature obj1 = null;
		Policy.State.Signature obj2 = null;
		String sStateSignatureName = "";
		for (Map.Entry<String,Policy.State.Signature> entry : list1.entrySet())  
		{
			sStateSignatureName = entry.getKey();
			obj1 = (Policy.State.Signature)entry.getValue();
			if(list2.containsKey(sStateSignatureName))
			{
				obj2 = (Policy.State.Signature)list2.get(sStateSignatureName);
				if(!(UIUtil.isEqual(obj1.getApprove(),obj2.getApprove()) && UIUtil.isEqual(obj1.getBranch(),obj2.getBranch()) && UIUtil.isEqual(obj1.getFilter(),obj2.getFilter()) && UIUtil.isEqual(obj1.getIgnore(),obj2.getIgnore()) && UIUtil.isEqual(obj1.getReject(),obj2.getReject()))) {					
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}
	private Map<String,Object> readFromXML(Context context, String fileName) throws Exception{
		Map<String,Object> mSchemaInfo = new HashMap<String,Object>();
		Map<String,Policy.State> mStateInfo = null;
		Map<String,Policy.State.Signature> mStateSignatureInfo = null;
		List<Object> listSchema = null;
		String sStateName = "";
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
				listSchema = new ArrayList<Object>();
				mStateInfo = new HashMap<String,Policy.State>();
				lstSetting = new ArrayList<Map<String, String>>();
				lstAllState = new ArrayList<Map>();
				lstCommandItem = new ArrayList<String>();
				lstPolicyItem = new ArrayList<String>();
				_basicElem = _schema.getBasic();
				policy = new Policy(_basicElem.getName(), _basicElem.getDescription(), _basicElem.getHidden(),_basicElem.getRegistryName());
				policy.hidden = _basicElem.getHidden();
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
					mStateSignatureInfo = new HashMap<String,Policy.State.Signature>();
					slTrigger = new ArrayList<Map<String, String>>();
					lstAccess = new ArrayList<Map<String, String>>();
					lstSignature = new ArrayList<Policy.State.Signature>();
					_statebasic = state.getStatebasic();
					sStateName = _statebasic.getName();
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
							lstAccess.add(mAccess);
						}
						stateOb.setSlAccess(lstAccess);
					}
					// process signature
					_signature = state.getSignature();
					if (_signature != null) {
						List<Signaturebasic> lstSignaBasic = _signature.getSignaturebasic();
						String sSignatureName = "";
						String sSignatureFromState = "";
						String sSignatureToState = "";
						for (Signaturebasic signatureBasic : lstSignaBasic) {
							if(signatureBasic.getName() != null){
								sSignatureName = signatureBasic.getName();
							}else{
								sSignatureName = "";
							}
							if(signatureBasic.getFromState() != null){
								sSignatureFromState = signatureBasic.getFromState();
							}else{
								sSignatureFromState = "";
							}
							if(signatureBasic.getToState() != null){
								sSignatureToState = signatureBasic.getToState();
							}else{
								sSignatureToState = "";
							}
							Policy.State.Signature signature = stateOb.getNewSignatureInstance(sSignatureName,
									sSignatureFromState, sSignatureToState);
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
							mStateSignatureInfo.put(sSignatureName+sSignatureFromState+sSignatureToState, signature);
						}
						stateOb.setLstSignature(lstSignature);
					}
					lstStateObj.add(stateOb);
					stateOb.setLstMapStateSignature(mStateSignatureInfo);
					mStateInfo.put(sStateName, stateOb);
				}
				policy.setLstState(lstStateObj);
				policy.setLstMapState(mStateInfo);
				listSchema.add(policy);
				listSchema.add(_schema);
				mSchemaInfo.put(policy.name,listSchema);
			}
		} catch (UnmarshalException ume) {
			throw ume;
		} catch (Exception e) {
			throw e;
		}
		return mSchemaInfo;
	}	
	
	private void writeToXML(Context context, String compareFileName, List<Object> list) throws Exception{
		PolicyXMLSchema.Schema _schema = null; 
		try {
			ObjectFactory objectFactory = new ObjectFactory();
			PolicyXMLSchema pxs = objectFactory.createComponent();
			pxs.setName("policy");
			pxs.setType("admin");
			pxs.setVersion(version);
			pxs.schema = new ArrayList<PolicyXMLSchema.Schema>();
			List<PolicyXMLSchema.Schema.State.Statebasic.Trigger.Event.Eventdetail> lstEvent = null;
			File file = null;
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { PolicyXMLSchema.class });
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty("jaxb.formatted.output", Boolean.valueOf(true));
			int iSize = list.size();
			List<Object> listSchemaList = null;
			for (int iCount = 0; iCount < iSize; iCount++) 
			{	
				listSchemaList = (ArrayList)list.get(iCount);				
				_schema = (PolicyXMLSchema.Schema)listSchemaList.get(1);
				pxs.schema.add(_schema);
			}
			file = new File(compareResultFolder + "\\" + sAdminType);
			if(!(file.exists() && file.isDirectory())){
				file.mkdir();
			} 
			file = new File(file.getPath() + "\\" + compareFileName + SchemaModelerConstants.XML_EXTENSION);
			jaxbMarshaller.marshal(pxs, file);	
		} catch (Exception e) {
			throw e;
		}
	}
}
