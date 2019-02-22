package com.izn.schemamodeler.admin.policy;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.izn.schemamodeler.SchemaInfo;
import com.matrixone.apps.cache.UITypeUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;

import matrix.db.Context;

public class PolicyInfo implements SchemaInfo {

	String _PolicyAccess = "read,modify,delete,checkout,checkin,schedule,lock,execute,unlock,freeze,thaw,create,revise,promote,"
			+ "demote,grant,enable,disable,override,changename,changetype,changeowner,changepolicy,"
			+ "revoke,changevault,fromconnect,toconnect,fromdisconnect,todisconnect,"
			+ "viewform,modifyform,show,approve,reject,ignore,reserve,unreserve,,majorrevise";
	List<String> _lstPolicyAccess = new ArrayList<String>();

	public PolicyInfo() {

		String[] strArr = _PolicyAccess.split(",");
		_lstPolicyAccess = Arrays.asList(strArr);
	}

	@Override
	public String geSchemaInfo(Context context, String strPolicyName, String tbd) throws Exception {
		String strInfo = EMPTY_STRING;
		String strRegistryName = EMPTY_STRING;
		try {
			String strResult = MqlUtil.mqlCommand(context, "list policy $1", new String[] { strPolicyName });
			String strUserName = MqlUtil.mqlCommand(context, "print policy $1 select $2 dump", new String[] { strPolicyName,"state.user" });
			String strAllStateUser = MqlUtil.mqlCommand(context, "print policy $1 select $2 dump", new String[] { strPolicyName,"allstate.user" });
			
			// System.out.println("Line 32 PolicyInfo strResult is ::::::::::::
			// "+strResult);

			if (UIUtil.isNotNullAndNotEmpty(strPolicyName))
				try {
					strResult = MqlUtil.mqlCommand(context, "print policy $1 nothistory",
							new String[] { strPolicyName });
					strRegistryName = MqlUtil.mqlCommand(context, "list property to policy $1",
							new String[] { strPolicyName });
					if (strRegistryName != null && !strRegistryName.equals("")) {
						String[] split = strRegistryName.trim().split(" ");
						strRegistryName = split[0].trim();
						strRegistryName = strRegistryName.replaceFirst("policy_", "");
					}
				} catch (Exception ex) {

				}
			if (UIUtil.isNotNullAndNotEmpty(strResult)) {
				strInfo = parsePolicy(context, strResult, strRegistryName,strPolicyName,strUserName,strAllStateUser);
			}

		} catch (Exception e) {

			e.printStackTrace();
		}
		return strInfo;
	}

	private String parsePolicy(Context context, String strMQLResult, String strRegistryName, String strPolicyName, String strUserName, String strAllStateUser) throws Exception {
		ObjectMapper _objectMapper = new ObjectMapper();
		Map<Object, Object> policyInfoMap = new LinkedHashMap<Object, Object>();// To
																				// hold
																				// policy
																				// basic
																				// info
																				// Parent
																				// Map
																				// ??

		try {
			Map<Object, Object> pFormatInfoMap = new HashMap<Object, Object>(); // stores
																				// format
																				// info
			Map<Object, Object> pAllStateInfoMap = new HashMap<Object, Object>(); // stores
																					// format
																					// info
			Map<Object, Object> pStatesInfoMap = new HashMap<Object, Object>(); // states
																				// info
																				// holder
																				// //

			Map<Object, Object> pIndiStateInfoMap = new HashMap<Object, Object>(); // single
																					// states
																					// info
																					// holder
																					// //
			Map<String, String> pInfoMap = new HashMap<String, String>();
			Map<String, String> mSettings = new HashMap<String, String>();
			Map<String, String> mProperties = new HashMap<String, String>();
			Map<Object, Object> mStateAccessInfo = new HashMap<Object, Object>();
			Map<String, List> mStateTriggerInfo = new HashMap<String, List>();
			Map<Object, Object> mStateSignatureInfo = new HashMap<Object, Object>();

			List slUsers = new ArrayList();
			List slAllStateUsers = new ArrayList();
			List slStateTriggers = new ArrayList();
			List slStateInheritedTriggers = new ArrayList();
			List lStates = new ArrayList(); // will be holding states info
			List lstEmpty = new ArrayList();
			List slStateSignature = new ArrayList();
			policyInfoMap.put("adminType", "policy");
			policyInfoMap.put("action", "modify");
			policyInfoMap.put("enforcelocking", "true");
			policyInfoMap.put("defaultformat", "");
			policyInfoMap.put("store", "");
			policyInfoMap.put("minorsequence", "");
			//policyInfoMap.put("types", lstEmpty); // to avoid NPE
			policyInfoMap.put("format", lstEmpty);// to avoid NPE
			policyInfoMap.put("registryname", strRegistryName);

			Map<String, String> mAccessAllState = new HashMap<String, String>();
			boolean bAllState = false;
			boolean bState = false;
			boolean bSignature = false;
			String strFilter = "";
			String strStateName = "";
			String strStateRegistryName = "";
			boolean bstate = false;
			BufferedReader localBufferedReader = new BufferedReader(new StringReader(strMQLResult));
			boolean bchildren = false;
			String str1;
			boolean bAllStateEnabled = false;
			
			//To get policy state property value---STARTS
			String strMQLStateResult = MqlUtil.mqlCommand(context, "print policy $1 select $2 dump",new String[] { strPolicyName,"property.name" });
			Map mapStateProperty = new HashMap();
			if(strMQLStateResult != null && !strMQLStateResult.isEmpty()){
				List<String> listStateProperty = com.izn.schemamodeler.util.UIUtil.spiltSingleStringToken(context, ",", strMQLStateResult);
				int iSize = listStateProperty.size();
				for(int iCount = 0; iCount<iSize; iCount++){
					if(listStateProperty.get(iCount).startsWith("state_")){
						strMQLStateResult = MqlUtil.mqlCommand(context, "print policy $1 select $2 dump",new String[] { strPolicyName,"property["+listStateProperty.get(iCount)+"].value" });
						if(strMQLStateResult != null && !strMQLStateResult.isEmpty()){							
							mapStateProperty.put(strMQLStateResult.trim(),listStateProperty.get(iCount).trim());
						}
					}
				}
			}
			//To get policy state property value---ENDS
			
			while ((str1 = localBufferedReader.readLine()) != null) {
				str1 = str1.trim();

				if (str1.startsWith("state") && str1.indexOf("allstate") > -1) {
					bAllStateEnabled = true;
				}
				int j = str1.indexOf(' ');
				String str2 = "";
				if ((j != -1) && (str1.length() > j + 1)) {
					str2 = UITypeUtil.unquote(str1.substring(j + 1));
				}
				if (str1.startsWith("policy") && !bchildren) {
					policyInfoMap.put("name", str2);
					bchildren = true;
				} else if (str1.startsWith("description")) {
					policyInfoMap.put("description", str2);
				} else if (str1.startsWith("derived")) {
					policyInfoMap.put("derived", str2);
				} else if (str1.startsWith("abstract")) {
					policyInfoMap.put("abstract", str2);
				} /*else if (str1.startsWith("type")) {
					policyInfoMap.put("types", str2.split(","));*/
				else if (str1.startsWith("store")) {
					policyInfoMap.put("store", str2);
				} else if (str1.startsWith("inherited attribute")) {
					policyInfoMap.put("inheriedAttribute", str2);
				} else if (str1.startsWith("nothidden")) {
					policyInfoMap.put("hidden", "false");
				} else if (str1.startsWith("hidden")) {
					policyInfoMap.put("hidden", "true");
				} else if (str1.startsWith("format")) {
					policyInfoMap.put("format", str2.split(","));
				} else if (str1.startsWith("defaultformat")) {
					policyInfoMap.put("defaultformat", str2);
				} else if (str1.startsWith("locking not")) {
					policyInfoMap.put("enforcelocking", "false");

				} else if (str1.startsWith("minor sequence")) {
					if(str2.indexOf(" ")>-1)
						policyInfoMap.put("minorsequence", (str2.substring(str2.indexOf(" "), str2.length())).trim());
					else
						policyInfoMap.put("minorsequence","");

				} else if (str1.startsWith("property")) {
					bState = false;
					// pStatesInfoMap.put("access", slUsers);
					// pStatesInfoMap.put("trigger", slStateTriggers); // for
					// last state .as it moves to next line ...so s
					// lStates.add(pStatesInfoMap);
					break;
				} else if (str1.startsWith("signature")) {
					if (!mStateSignatureInfo.isEmpty())
						slStateSignature.add(mStateSignatureInfo);
					mStateSignatureInfo = new HashMap();
					
					mStateSignatureInfo.put("name", str2);
					mStateSignatureInfo.put("ignore", new ArrayList());
					mStateSignatureInfo.put("reject", new ArrayList());
					mStateSignatureInfo.put("approve", new ArrayList());
					mStateSignatureInfo.put("filter", "");
					bSignature = true;
					bState = false;

				} 
				else if (str1.startsWith("state") && str1.indexOf("allstate") == -1) {
					bAllState = false;
					bstate = true;
					if (!strStateName.isEmpty() && !strStateName.equalsIgnoreCase(str2)) {
						//pStatesInfoMap.put("access", slUsers);
						//pStatesInfoMap.put("trigger", slStateTriggers);
						//pStatesInfoMap.put("inheritedTrigger", slStateInheritedTriggers);
						if (!mStateSignatureInfo.isEmpty())
							slStateSignature.add(mStateSignatureInfo);
						pStatesInfoMap.put("signature", slStateSignature);

						//System.out.println(">>>>>>>>>>>>>>>pStatesInfoMap : "+pStatesInfoMap);
						//slUsers = new ArrayList();
						//slStateTriggers = new ArrayList();
						slStateSignature = new ArrayList();
						//slStateInheritedTriggers = new ArrayList();
						lStates.add(pStatesInfoMap);
						//System.out.println(">>>>>>>>>>>>>>>lStates : "+lStates);
						pStatesInfoMap = new HashMap();
/*						pStatesInfoMap.put("access", new ArrayList()); 
						pStatesInfoMap.put("trigger", new ArrayList());
						pStatesInfoMap.put("signature", new ArrayList()); */
						mStateSignatureInfo = new HashMap();

						bSignature = false;
					}
					bState = true;
					strStateName = str2;					
					pStatesInfoMap.put("name", str2);
					strStateRegistryName = (String)mapStateProperty.get(strStateName);
					if(strStateRegistryName != null) {
						pStatesInfoMap.put(str2, strStateRegistryName);
					} else {
						pStatesInfoMap.put(str2, "");
					}
				} else if (bState) {
					if (str1.startsWith("versionable")) {
						pStatesInfoMap.put("version", str2);
					} else if (str1.startsWith("minorrevisionable")) {
						pStatesInfoMap.put("minorrevisionable", str2);
					} else if (str1.startsWith("majorrevisionable")) {
						pStatesInfoMap.put("revisionable", str2);
					} else if (str1.startsWith("promote")) {
						pStatesInfoMap.put("promote", str2);
					} else if (str1.startsWith("checkout history")) {
						pStatesInfoMap.put("checkouthistory", str2.substring(7, str2.length()));
					} else if (str1.startsWith("trigger")) {

						slStateTriggers = com.izn.schemamodeler.util.UIUtil.getTriggerDetails(context, str2);
//System.out.println("\nslStateTriggers>>>>>>>>>>>>>>>>>> : "+slStateTriggers);
						if(pStatesInfoMap.containsKey("trigger")){							
							List listStateTriggers = (List)pStatesInfoMap.get("trigger");
							listStateTriggers.add(slStateTriggers);
							pStatesInfoMap.put("trigger", listStateTriggers);
						}else{
							pStatesInfoMap.put("trigger", slStateTriggers);
						}
					} else if (str1.startsWith("inherited trigger")) {
						slStateInheritedTriggers = com.izn.schemamodeler.util.UIUtil.getTriggerDetails(context, str2);
					} else {

						mAccessAllState = new HashMap();
						//if (str2.indexOf("organization") != -1 || str2.indexOf("project") != -1
						//		|| str2.indexOf("key") != -1)
							//continue;
						mAccessAllState = getUserAndAccesses(context, str1, str2, mAccessAllState);
						pStatesInfoMap.put("filter", "");
						if (mAccessAllState.containsKey("filter")) {
							strFilter = mAccessAllState.get("filter");
							// pStatesInfoMap.put("filter", strFilter);
						}
						Set<String> stAccess = mAccessAllState.keySet();
						Iterator<String> itr = stAccess.iterator();
						String strKey = "";
						String strValue = "";
						while (itr.hasNext()) {
							mStateAccessInfo = new HashMap<Object, Object>();
							strKey = itr.next();
							if (!strKey.isEmpty() && !strKey.equalsIgnoreCase("filter")) {
								mStateAccessInfo.put("name", strKey);
								if (mAccessAllState.containsKey("filter"))
									mStateAccessInfo.put("filter", mAccessAllState.get("filter"));
								strValue = mAccessAllState.get(strKey);
								if (!strValue.isEmpty()) {
									String[] accss = strValue.split("\\|");
									// mStateAccessInfo.put("access", accss);
									mStateAccessInfo.put("access", Arrays.asList(accss));
									//slUsers.add(mStateAccessInfo);
									List listUserAccess =  new ArrayList();
									if(pStatesInfoMap.containsKey("access")){
										listUserAccess = (List)pStatesInfoMap.get("access");
										listUserAccess.add(mStateAccessInfo);
										pStatesInfoMap.put("access", listUserAccess);
									}else{
										listUserAccess.add(mStateAccessInfo);
										pStatesInfoMap.put("access", listUserAccess);
									}
								}
							}
						}
					}

				} else if (bSignature) {

					if (str1.startsWith("approve")) {
						mStateSignatureInfo.put("approve", str2.split(","));
					} else if (str1.startsWith("reject")) {
						mStateSignatureInfo.put("reject", str2.split(","));
					} else if (str1.startsWith("ignore")) {
						mStateSignatureInfo.put("ignore", str2.split(","));
					} else if (str1.startsWith("branch")) {
						mStateSignatureInfo.put("branch", str2);
					} else if (str1.startsWith("filter")) {
						mStateSignatureInfo.put("filter", str2);
					}

				} else if (str1.indexOf("allstate") != -1 || bAllState) {
					if (str1.startsWith("minorrevisionable")) {
						bAllState = false;
						pStatesInfoMap.put("name", "allstate");
						pStatesInfoMap.put("minorrevisionable", str2);
						continue;
					}
					bAllState = true;
					//if (str2.indexOf("organization") != -1 || str2.indexOf("project") != -1
					//		|| str2.indexOf("allstate") != -1 || str2.indexOf("key") != -1)
						//continue;
					mAccessAllState = new HashMap();
					mAccessAllState = getUserAndAccesses(context, str1, str2, mAccessAllState);
					if (mAccessAllState.containsKey("filter")) {
						strFilter = mAccessAllState.get("filter");
						pStatesInfoMap.put("filter", strFilter);
					}
					Set<String> stAccess = mAccessAllState.keySet();
					Iterator<String> itr = stAccess.iterator();
					String strKey = "";
					String strValue = "";
					while (itr.hasNext()) {
						pAllStateInfoMap = new HashMap<Object, Object>();
						strKey = itr.next();
						if (!strKey.isEmpty() && !strKey.equalsIgnoreCase("filter")) {
							pAllStateInfoMap.put("name", strKey);
							if (mAccessAllState.containsKey("filter"))
								pAllStateInfoMap.put("filter", mAccessAllState.get("filter"));
							strValue = mAccessAllState.get(strKey);
							if (!strValue.isEmpty()) {
								String[] accss = strValue.split("\\|");
								pAllStateInfoMap.put("access", accss);
								// pAllStateInfoMap.put("access",
								// Arrays.asList(accss));
								slAllStateUsers.add(pAllStateInfoMap);
							}
						}
					}

				}
			}
			if (bstate) {
				if (!pStatesInfoMap.containsKey("signature"))
					pStatesInfoMap.put("signature", new ArrayList());
				if (!pStatesInfoMap.containsKey("trigger"))
					pStatesInfoMap.put("trigger", new ArrayList());
				if (!pStatesInfoMap.containsKey("access"))
					pStatesInfoMap.put("access", new ArrayList());
				lStates.add(pStatesInfoMap); // last state?
			}
			String stype = MqlUtil.mqlCommand(context, "print policy $1 select type dump",new String[] { strPolicyName });
			List listtypes = new ArrayList();
			if (stype != null && !"".equals(stype)) {
				listtypes.add(stype);
				policyInfoMap.put("types", listtypes);
			} 

			policyInfoMap.put("states", lStates);
			policyInfoMap.put("allStateAccess", slAllStateUsers);
			policyInfoMap.put("allStateAccessEnabled", String.valueOf(bAllStateEnabled));
			policyInfoMap.put("User", strUserName);
			policyInfoMap.put("allStateUser", strAllStateUser);
		} catch (Exception e) {
			// System.out.println(" Error in getting Policy Info");
			e.printStackTrace();
		}

		return _objectMapper.writeValueAsString(policyInfoMap);
	}

	private Map getUserAndAccesses(Context context, String str1, String str2, Map mAccessAllState) throws Exception {

		try {

			String strUser = "";
			String strAccess = "";
			String strFilter = "";
			
			if (str1.startsWith("user") || str1.startsWith("public") || str1.startsWith("owner")) {
				if (str1.startsWith("public"))
					strUser = "public";
				if (str1.startsWith("owner"))
					strUser = "owner";
			}
			
				if(str1.contains("none")){
					String[] splitString = str1.split(" ");
					for(int i=0; i<splitString.length; i++){
						if(splitString[i].equals("none")){
							strUser = splitString[i-1];
						}

						mAccessAllState.put(strUser.trim(), "none");
					}
				} else {
			
				int iFilterIndex = str2.indexOf("filter");
				if (iFilterIndex != -1) {
					strFilter = str2.substring(iFilterIndex + 7, str2.length());
					str2 = str2.substring(0, iFilterIndex - 1); 
					mAccessAllState.put("filter", strFilter);
				}
				
				
				
				String[] strTokens = str2.split("\\s*(=>|,|\\s)\\s*");
				List<String> lstTokens = Arrays.asList(strTokens);
				Collection<String> lstAccess = CollectionUtils.intersection(_lstPolicyAccess, lstTokens);
				List<String> lstModTokens = new ArrayList<String>(lstTokens); 
				lstModTokens.removeAll(_lstPolicyAccess);
				if (strUser.isEmpty())
					strUser = lstModTokens.stream().map(Object::toString).collect(Collectors.joining(" "));
				strAccess = lstAccess.stream().map(Object::toString).collect(Collectors.joining("|"));
				
				if(strUser.trim().startsWith("user ")){												
					strUser  = strUser.trim().substring(5);									
				} else {
					strUser  = strUser;
				}
				if(strUser.trim().startsWith("login user ")){												
					strUser  = strUser.trim().substring(11);									
				} else {
					strUser  = strUser;
				}
				if(strUser.trim().startsWith("login ")){												
					strUser  = strUser.trim().substring(6);									
				} else {
					strUser  = strUser;
				}
				 
				if ((str1.contains("public") || str1.contains("owner")) && (!str1.contains("none")) && str1.length() > 6) {
					mAccessAllState.put(str1, strAccess);
				} else {
					mAccessAllState.put(strUser.trim(), strAccess);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mAccessAllState;
	}

	@Override
	public String geSchemaInfoWithPath(Context context, String strSchemaName, String strExportPath) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
