package com.izn.schemamodeler.admin.policy;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.izn.schemamodeler.SchemaExport;
import com.izn.schemamodeler.SchemaInfo;
import com.izn.schemamodeler.admin.policy.ObjectFactory;
import com.izn.schemamodeler.util.SCMConfigProperty;

import matrix.db.Context;
import matrix.util.StringList;
import com.izn.schemamodeler.util.SchemaModelerConstants;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;

public class PolicyXMLExport implements SchemaExport {
	String _sDBInfo = "";
	Map _hDBInfo = new HashMap();
	PolicyXMLSchema.Schema _schema = null;
	PolicyXMLSchema.Schema.Basic _basic = null;
	PolicyXMLSchema.Schema.Allstate _allstate = null;
	PolicyXMLSchema.Schema.Allstate.Allstateaccess _allstateaccess = null;
	PolicyXMLSchema.Schema.Field _field = null;
	PolicyXMLSchema.Schema.Field.Detail _detail = null;
	PolicyXMLSchema.Schema.State _state = null;
	PolicyXMLSchema.Schema.State.Statebasic _statebasic = null;
	PolicyXMLSchema.Schema.State.Statebasic.Access _access = null;
	PolicyXMLSchema.Schema.State.Statebasic.Access.Accessdetails _accessdetails = null;
	PolicyXMLSchema.Schema.State.Statebasic.Statedetail _statedetail = null;
	PolicyXMLSchema.Schema.State.Statebasic.Trigger _trigger = null;
	PolicyXMLSchema.Schema.State.Statebasic.Trigger.Event _event = null;
	PolicyXMLSchema.Schema.State.Statebasic.Trigger.Event.Eventdetail _eventdetail = null;
	PolicyXMLSchema.Schema.State.Signature _signature = null;
	PolicyXMLSchema.Schema.State.Signature.Signaturebasic _signaturebasic = null;
	PolicyXMLSchema.Schema.State.Signature.Signaturebasic.Signaturedetail _signaturedetail = null;

	public PolicyXMLExport() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void exportSchema(Context context, String strAdminType, List<String> lstNames, String exportPath,
			String strVersion, Logger schema_done_log, SCMConfigProperty scmConfigProperty) throws Exception{
		String[] fDetails = { "store", "minorsequence", "type", "format", "defaultformat", "enforcelocking" };
		String name = "";
		String description = "";
		String hidden = "";
		String operator = "";
		String type = "";
		List<String> types = new ArrayList<String>();
		List<Map> triggers = new ArrayList<Map>();
		try {
			exportPath = exportPath + "\\" + SchemaModelerConstants.FOLDER_NAME_SchemaObject;
			ObjectFactory objectFactory = new ObjectFactory();
			PolicyXMLSchema pxs = objectFactory.createComponent();
			pxs.setName("policy");
			pxs.setType("admin");
			pxs.setVersion(strVersion);
			pxs.schema = new ArrayList<PolicyXMLSchema.Schema>();
			String[] stateDetails = { "promote", "version", "checkouthistory", "minorrevisionable" };
			String[] signkey = { "branch", "filter", "approve", "reject", "ignore" };
			SchemaInfo schemaInfo = _schemaFactory.getSchemaObject(strAdminType);
			List<PolicyXMLSchema.Schema.State.Statebasic.Trigger.Event.Eventdetail> lstEvent = null;
			int iCountSchema = 0;
			boolean bSchemaSeperate = false;
			boolean bLogEverything = false;
			File file = null;
			String filePath = "";
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { PolicyXMLSchema.class });
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty("jaxb.formatted.output", Boolean.valueOf(true));
			if ("true".equalsIgnoreCase(scmConfigProperty.getSchemaSeperator())) {
				bSchemaSeperate = true;
				filePath = exportPath + "\\" + strAdminType.trim();
				file = new File(filePath);
				if (!file.exists() && !file.isDirectory()) {
					file.mkdir();
				}
			}
			List listSchemaNames = new ArrayList();
			if ("true".equalsIgnoreCase(scmConfigProperty.getLogEverything())) {
						bLogEverything = true;			
			}
			for (String sName : lstNames) {

				iCountSchema += 1;
				_sDBInfo = schemaInfo.geSchemaInfo(context, sName, new String());
				_hDBInfo = _gson.readValue(_sDBInfo, HashMap.class);
				_schema = objectFactory.createComponentSchema();
				_basic = objectFactory.createComponentSchemaBasic();
				_allstate = objectFactory.createComponentSchemaAllstate();

				_schema.state = new ArrayList<PolicyXMLSchema.Schema.State>();
				String strUsers = (String) _hDBInfo.get("User");
				String strAllStateUser = (String) _hDBInfo.get("allStateUser");
				_basic.setDescription((String) _hDBInfo.get("description"));
				_basic.setHidden((String) _hDBInfo.get("hidden"));
				_basic.setName((String) _hDBInfo.get("name"));
				_basic.setRegistryName((String) _hDBInfo.get("registryname"));
				_schema.setBasic(_basic); // set basic
				_field = objectFactory.createComponentSchemaField();
				_field.setType("policydetails");
				types = (List<String>) _hDBInfo.get("types");
				_hDBInfo.put("type", type);
				if (types != null && !types.isEmpty()) {
					type = String.join(",", types); // jdk 8 only
					_hDBInfo.put("type", type);
				} else {
					_hDBInfo.put("type", "");
				}
				StringList userList = new StringList();
				StringList sList = FrameworkUtil.split(strUsers, ",");				
				for(int k=0; k < sList.size();k++ ){
					if(((String)sList.get(k)).contains("|")){
						StringList sPipeSepList = FrameworkUtil.split(((String)sList.get(k)), "|");
						userList.add(sPipeSepList.get(0));
					} else {
						userList.add((String)sList.get(k));
					}
				}
				StringList allStateUser = new StringList();
				StringList sList1 = FrameworkUtil.split(strAllStateUser, ",");	

				for(int m=0; m < sList1.size();m++ ){
					if(((String)sList1.get(m)).contains("|")){
						StringList sPipeSepList = FrameworkUtil.split(((String)sList1.get(m)), "|");
						allStateUser.add(sPipeSepList.get(0));
					} else {
						allStateUser.add((String)sList1.get(m));
					}
				}
				
				List<String> formats = (List<String>) _hDBInfo.get("format");
				if (formats != null && !formats.isEmpty()) {
					type = String.join(",", formats); // jdk 8 only
					_hDBInfo.put("format", type);
				} else {
					_hDBInfo.put("format", "");
				}
				_field.detail = new ArrayList<PolicyXMLSchema.Schema.Field.Detail>();
				for (String key : fDetails) {
					_detail = objectFactory.createComponentSchemaFieldDetail();
					_detail.setName(key);
					Object object = (Object) _hDBInfo.get(key);
					if (object instanceof String) {
						_detail.setValueAttribute((String) object);
					}
					_field.detail.add(_detail);
				}
				_allstate.setEnabled((String) _hDBInfo.get("allStateAccessEnabled"));
				List<Map> asa = (List<Map>) _hDBInfo.get("allStateAccess");
				
				
				if (asa != null && !asa.isEmpty()) {
					_allstate.allstateaccess = new ArrayList<PolicyXMLSchema.Schema.Allstate.Allstateaccess>();
					
					for (Map m : asa) {
						
						String allStateUsers = getUser((String) m.get("name"),allStateUser);
						
						String sOrg = "";
						String sProject = "";
						String sKey = "";
						String sMaturity="";
						String sOwner="";
						String sCategory="";
						String sReverse="";
						
						String[] splitString = ((String) m.get("name")).split(" ");
						for(int i=0; i<splitString.length; i++){
							if(splitString[i].equals("organization")){
								sOrg = splitString[i-1];
							}else if(splitString[i].equals("project")){
								sProject = splitString[i-1];
							}else if(splitString[i].equals("key")){
								sKey = splitString[i+1];
							}else if(splitString[i].equals("maturity")){
								sMaturity = splitString[i-1];
							}else if(splitString[i].equals("context")){
								sOwner = splitString[i];
							}else if(splitString[i].equals("category")){
								sCategory = splitString[i-1];
							}else if(splitString[i].equals("reserve")){
								sReverse = splitString[i-1];
							}
							
						}			
						_allstateaccess = objectFactory.createComponentSchemaAllstateAllstateaccess();
						_allstateaccess.setAccess(String.join(",", (List<String>) m.get("access")));
						_allstateaccess.setUser(allStateUsers);
						_allstateaccess.setFilter((String) m.get("filter"));
						_allstateaccess.setKey(sKey);
						_allstateaccess.setOrganization(sOrg);
						_allstateaccess.setProject(sProject);
						_allstateaccess.setMaturity(sMaturity);
						_allstateaccess.setOwner(sOwner);
						_allstateaccess.setCategory(sCategory);
						_allstateaccess.setReserve(sReverse);
						_allstate.allstateaccess.add(_allstateaccess);
					}
				}
				List<Map> lstState = (List<Map>) _hDBInfo.get("states");
				if (lstState != null && !lstState.isEmpty()) {
					for (Map m : lstState) {
						_state = objectFactory.createComponentSchemaState();
						_statebasic = objectFactory.createComponentSchemaStateStatebasic();
						_trigger = objectFactory.createComponentSchemaStateStatebasicTrigger();
						String strStateName = (String) m.get("name");
						String strStateRegistryName = (String) m.get(strStateName);
						_statebasic.setName((String) m.get("name"));
						_statebasic.setRegistryName(strStateRegistryName);
						_statebasic.statedetail = new ArrayList<PolicyXMLSchema.Schema.State.Statebasic.Statedetail>();
						for (String key : stateDetails) {
							_statedetail = objectFactory.createComponentSchemaStateStatebasicStatedetail();
							_statedetail.setName(key);
							_statedetail.setValueAttribute((String) m.get(key));
							_statebasic.statedetail.add(_statedetail);
						}
						triggers = (List<Map>) m.get("trigger");
						if (triggers != null && !triggers.isEmpty()) {

							Map mTriggers = new HashMap();
							String event = "";
							for (Map mTrigger : triggers) {
								lstEvent = new ArrayList<PolicyXMLSchema.Schema.State.Statebasic.Trigger.Event.Eventdetail>();
								_eventdetail = objectFactory
										.createComponentSchemaStateStatebasicTriggerEventEventdetail();
								_eventdetail.setProgram("emxTriggerManager");
								_eventdetail.setInput((String) mTrigger.get("name"));
								_eventdetail.setType((String) mTrigger.get("type"));
								event = (String) mTrigger.get("action");
								if (mTriggers.containsKey(event)) {
									lstEvent = (List<PolicyXMLSchema.Schema.State.Statebasic.Trigger.Event.Eventdetail>) mTriggers
											.get(event);
								}
								lstEvent.add(_eventdetail);
								mTriggers.put(event, lstEvent);
							}
							Set sTriggers = mTriggers.keySet();
							Iterator<String> itrsTriggers = sTriggers.iterator();
							String sKey = "";
							_trigger.event = new ArrayList<PolicyXMLSchema.Schema.State.Statebasic.Trigger.Event>();
							while (itrsTriggers.hasNext()) {
								_event = objectFactory.createComponentSchemaStateStatebasicTriggerEvent();
								sKey = itrsTriggers.next();
								_event.setName(sKey);
								_event.eventdetail = new ArrayList<PolicyXMLSchema.Schema.State.Statebasic.Trigger.Event.Eventdetail>();
								List<PolicyXMLSchema.Schema.State.Statebasic.Trigger.Event.Eventdetail> lst = (List<PolicyXMLSchema.Schema.State.Statebasic.Trigger.Event.Eventdetail>) mTriggers
										.get(sKey);
								for (PolicyXMLSchema.Schema.State.Statebasic.Trigger.Event.Eventdetail eventDetail : lst) {
									_event.eventdetail.add(eventDetail);
								}
								_trigger.event.add(_event);
							}
						}

						// process access
						List<Map> sa = (List<Map>) m.get("access");
						_access = objectFactory.createComponentSchemaStateStatebasicAccess();
						String strValue;
						if (sa != null && !sa.isEmpty()) {
							_access.accessdetails = new ArrayList<PolicyXMLSchema.Schema.State.Statebasic.Access.Accessdetails>();
							for (Map m0 : sa) {
								String strName = (String)m0.get("name");	
								String userName = getUser(strName,userList);

								String sOrg = "";
								String sProject = "";
								String sKey = "";
								String sUserName="";
								String sMaturity="";
								String sOwner="";
								String sCategory="";
								String sReverse="";
							
							
								String[] splitString = ((String) m0.get("name")).split(" ");
								for(int i=0; i<splitString.length; i++){
									if(splitString[i].equals("organization")){
										sOrg = splitString[i-1];
									}else if(splitString[i].equals("project")){
										sProject = splitString[i-1];
									}else if(splitString[i].equals("key")){
										sKey = splitString[i+1];
									}else if(splitString[i].equals("maturity")){
										sMaturity = splitString[i-1];
									}else if(splitString[i].equals("context")){
										sOwner = splitString[i];
									}else if(splitString[i].equals("category")){
										sCategory = splitString[i-1];
									}else if(splitString[i].equals("reserve")){
										sReverse = splitString[i-1];
									}
								}	
								_accessdetails = objectFactory.createComponentSchemaStateStatebasicAccessAccessdetails();
								_accessdetails.setAccess(String.join(",", (List<String>) m0.get("access")));
								_accessdetails.setUser(userName);
								_accessdetails.setFilter((String) m0.get("filter"));
								_accessdetails.setKey(sKey);
								_accessdetails.setOrganization(sOrg);
								_accessdetails.setProject(sProject);
								_accessdetails.setMaturity(sMaturity);
								_accessdetails.setOwner(sOwner);
								_accessdetails.setCategory(sCategory);
								_accessdetails.setReserve(sReverse);
								_access.accessdetails.add(_accessdetails);
							
							}
						}
						// process signature
						_signature = objectFactory.createComponentSchemaStateSignature();
						_signature.signaturebasic = new ArrayList<PolicyXMLSchema.Schema.State.Signature.Signaturebasic>();
						List<Map> ss = (List<Map>) m.get("signature");
						for (Map m1 : ss) {
							_signaturebasic = objectFactory.createComponentSchemaStateSignatureSignaturebasic();
							_signaturebasic.setName((String) m1.get("name"));
							_signaturebasic.setFromState((String) m.get("name"));
							_signaturebasic.signaturedetail = new ArrayList<PolicyXMLSchema.Schema.State.Signature.Signaturebasic.Signaturedetail>();
							for (String k : signkey) {
								_signaturedetail = objectFactory
										.createComponentSchemaStateSignatureSignaturebasicSignaturedetail();
								if (k.equalsIgnoreCase("approve")) {
									_signaturedetail.setApprove(String.join(",", (List<String>) m1.get(k)));
								} else if (k.equalsIgnoreCase("branch")) {
									_signaturedetail.setBranch((String) m1.get(k));
									_signaturebasic.setToState((String) m1.get(k));
								} else if (k.equalsIgnoreCase("ignore")) {
									_signaturedetail.setIgnore((String.join(",", (List<String>) m1.get(k))));
								} else if (k.equalsIgnoreCase("filter")) {
									_signaturedetail.setFilter((String) m1.get(k));
								} else if (k.equalsIgnoreCase("reject")) {
									_signaturedetail.setReject((String.join(",", (List<String>) m1.get(k))));
								}
								_signaturebasic.signaturedetail.add(_signaturedetail);

							}
							_signature.signaturebasic.add(_signaturebasic);
						}
						_statebasic.setAccess(_access);
						_statebasic.setTrigger(_trigger);
						_state.setStatebasic(_statebasic);
						_state.setSignature(_signature);
						_schema.state.add(_state);
					}
				}
				_schema.setAllstate(_allstate);
				_schema.setField(_field);
				pxs.schema.add(_schema);
				if (bSchemaSeperate) {
					sName = sName.replaceAll(UIUtil.specialCharecters, "");
					file = new File(filePath + "\\" + sName + ".xml");
					jaxbMarshaller.marshal(pxs, file);
					pxs.schema = new ArrayList();
				}
				if(bLogEverything) {					
					listSchemaNames.add(sName);
				}
			}
			schema_done_log.info(strAdminType + "|" + iCountSchema);
			if (bLogEverything) {
				schema_done_log.info(listSchemaNames);				
			}
			if (!bSchemaSeperate) {
				file = new File(exportPath + "\\" + strAdminType.toLowerCase() + ".xml");
				jaxbMarshaller.marshal(pxs, file);
			}			
		} catch (Exception e) {
			throw new Exception("Error occurred while exporting schema "+strAdminType+" : " +e.getCause());
		}
	}
	
	private String getUser(String userAccess, StringList userList){
		userList.add("public");
		userList.add("owner");
		for (int i = 0; i < userList.size(); i++) {
			
			if(userAccess.indexOf((String)userList.get(i)) > -1){
				return (String)userList.get(i);
			}									
		}
		return "";
	}
	
}
