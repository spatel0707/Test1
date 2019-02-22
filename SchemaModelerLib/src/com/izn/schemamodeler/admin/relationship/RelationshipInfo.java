package com.izn.schemamodeler.admin.relationship;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.izn.schemamodeler.SchemaInfo;
import com.matrixone.apps.cache.UITypeUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;

import matrix.db.Context;

public class RelationshipInfo implements SchemaInfo {

	public RelationshipInfo() {
		// TODO Auto-generated constructor stub
	}

	public String geSchemaInfo(Context context, String strRelationship, String tbd) throws Exception {
		String strInfo = EMPTY_STRING;
		String strRegistryName = EMPTY_STRING;
		String strResult = null;
		String strFromToTypes = EMPTY_STRING;
		HashMap hmFromAndToTypes = new HashMap();
		try {
			strResult = MqlUtil.mqlCommand(context, "list relationship $1", new String[] { strRelationship });
			if (UIUtil.isNotNullAndNotEmpty(strRelationship))
				try {
					strResult = MqlUtil.mqlCommand(context, "print relationship $1 nothistory",
							new String[] { strRelationship });
					strRegistryName = MqlUtil.mqlCommand(context, "list property to relationship $1",
							new String[] { strRelationship });
					if (strRegistryName != null && !strRegistryName.equals("")) {
						String[] split = strRegistryName.trim().split(" ");
						strRegistryName = split[0].trim();
						strRegistryName = strRegistryName.replaceFirst("relationship_", "");
					}
					strFromToTypes = MqlUtil.mqlCommand(context, "print relationship $1 select fromtype dump $2",
							new String[] { strRelationship, "," });
					hmFromAndToTypes.put("fromtypes", strFromToTypes.split(","));
					strFromToTypes = MqlUtil.mqlCommand(context, "print relationship $1 select totype dump $2",
							new String[] { strRelationship, "," });
					hmFromAndToTypes.put("totypes", strFromToTypes.split(","));
					strFromToTypes = MqlUtil.mqlCommand(context, "print relationship $1 select fromrel dump $2",
							new String[] { strRelationship, "," });
					hmFromAndToTypes.put("fromrel", strFromToTypes.split(","));
					strFromToTypes = MqlUtil.mqlCommand(context, "print relationship $1 select torel dump $2",
							new String[] { strRelationship, "," });
					hmFromAndToTypes.put("torel", strFromToTypes.split(","));
				} catch (Exception ex) {

				}
			if (UIUtil.isNotNullAndNotEmpty(strResult)) {
				strInfo = parseRelationship(context, strResult, strRegistryName, hmFromAndToTypes);
			}

		} catch (Exception e) {

			e.printStackTrace();
		}
		return strInfo;
	}

	private String parseRelationship(Context context, String strMQLResult, String strRegistryName,
			HashMap hmFromAndToTypes) throws Exception {

		ObjectMapper _objectMapper = new ObjectMapper();
		Map relationshipInfoMap = new LinkedHashMap();
		try {
			Map<Object, Object> mToMap = new HashMap<Object, Object>();
			Map<Object, Object> mFromMap = new HashMap<Object, Object>();
			Map<String, String> mProperties = new HashMap<String, String>();
			relationshipInfoMap.put("adminType", "relationship");
			relationshipInfoMap.put("action", "modify");
			relationshipInfoMap.put("preventduplicates", "false");
			relationshipInfoMap.put("sparse", "false");
			relationshipInfoMap.put("derived", "");
			relationshipInfoMap.put("attribute", new ArrayList()); // to stick
																	// to format
			relationshipInfoMap.put("trigger", new ArrayList());// to stick to
																// format
			relationshipInfoMap.put("registryname", strRegistryName);
			Map<Object, Object> mRevisionMap = new HashMap<Object, Object>();
			mRevisionMap.put("none", "false");
			mRevisionMap.put("float", "false");
			mRevisionMap.put("replicate", "false");
			Map<Object, Object> mCardanilityMap = new HashMap<Object, Object>();
			mCardanilityMap.put("many", "false");
			mCardanilityMap.put("one", "false");
			Map<Object, Object> mCloneMap = new HashMap<Object, Object>();
			mCloneMap.put("none", "false");
			mCloneMap.put("float", "false");
			mCloneMap.put("replicate", "false");

			List slTriggers = new ArrayList();
			List slInherited = new ArrayList();
			boolean bToFrom = false;
			int i = 0;
			BufferedReader localBufferedReader = new BufferedReader(new StringReader(strMQLResult));
			boolean bchildren = false;
			String str1;
			while ((str1 = localBufferedReader.readLine()) != null) {
				str1 = str1.trim();
				int j = str1.indexOf(' ');
				String str2 = "";
				if ((j != -1) && (str1.length() > j + 1)) {
					str2 = UITypeUtil.unquote(str1.substring(j + 1));
				}
				if (str1.startsWith("relationship") && !bchildren) {
					relationshipInfoMap.put("name", str2);
					bchildren = true;
				} else if (str1.startsWith("description")) {
					relationshipInfoMap.put("description", str2);
				} else if (str1.startsWith("derived")) {
					relationshipInfoMap.put("derived", str2);
				} else if (str1.startsWith("abstract")) {
					relationshipInfoMap.put("abstract", str2);
				} else if (str1.startsWith("attribute")) {
					relationshipInfoMap.put("attribute", str2.split(","));
				} else if (str1.startsWith("inherited attribute")) {
					relationshipInfoMap.put("inheriedAttribute", str2);
				} else if (str1.startsWith("nothidden")) {
					relationshipInfoMap.put("hidden", "false");
				} else if (str1.startsWith("hidden")) {
					relationshipInfoMap.put("hidden", "true");
				} else if (str1.startsWith("property")) {

					// reset b4 to side fill
				} else if (str1.contains("preventduplicates")) {
					str1 = (str1.equalsIgnoreCase("preventduplicates")) ? "true" : "false";
					relationshipInfoMap.put("preventduplicates", str1);
					bToFrom = false;
					mFromMap.put("revision", mRevisionMap);
					mFromMap.put("clone", mCloneMap);
					mFromMap.put("cardinality", mCardanilityMap);
					relationshipInfoMap.put("toSide", mFromMap);
					relationshipInfoMap.put("trigger", slTriggers);
					relationshipInfoMap.put("inheritedTrigger", slInherited);
				} else if (str1.startsWith("trigger")) {
					slTriggers = com.izn.schemamodeler.util.UIUtil.getTriggerDetails(context, str2);

				} else if (str1.startsWith("inherited trigger")) {
					slInherited = com.izn.schemamodeler.util.UIUtil.getTriggerDetails(context, str2);

				} else if (str1.startsWith("from")) {
					bToFrom = true;
				} else if (str1.startsWith("to")) {
					bToFrom = true;
					mFromMap.put("revision", mRevisionMap);
					mFromMap.put("clone", mCloneMap);
					mFromMap.put("cardinality", mCardanilityMap);
					mRevisionMap = new HashMap<Object, Object>();
					mRevisionMap.put("none", "false");
					mRevisionMap.put("float", "false");
					mRevisionMap.put("replicate", "false");
					mCardanilityMap = new HashMap<Object, Object>();
					mCardanilityMap.put("many", "false");
					mCardanilityMap.put("one", "false");
					mCloneMap = new HashMap<Object, Object>();
					mCloneMap.put("none", "false");
					mCloneMap.put("float", "false");
					mCloneMap.put("replicate", "false");
					relationshipInfoMap.put("fromSide", mFromMap);
					mFromMap = new HashMap();
				} else if (bToFrom) {
					/*if (str1.startsWith("type")) {
						mFromMap.put("type", "none".equalsIgnoreCase(str2) ? "":str2.split(","));
					}
					if (str1.startsWith("relationship")) {
						mFromMap.put("relationship", "none".equalsIgnoreCase(str2) ? "":str2.split(","));
					} */
					if (str1.startsWith("revision")) {
						mRevisionMap.replace(str2, "true");
					} else if (str1.startsWith("clone")) {
						mCloneMap.replace(str2, "true");
					} else if (str1.startsWith("cardinality")) {
						mCardanilityMap.replace(str2, "true");
					} else if (str1.startsWith("propagate modify")) {
						mFromMap.put("propogateModify", str2.substring(7));
					} else if (str1.startsWith("propagate connection")) {
						mFromMap.put("propogateConnect", str2.substring(11));
					} else if (str1.startsWith("meaning")) {
						mFromMap.put("meaning", str2);
					}
				}
			}
			
			HashMap hmFromToSide = (HashMap) relationshipInfoMap.get("fromSide");
			hmFromToSide.put("type", hmFromAndToTypes.get("fromtypes"));
			hmFromToSide.put("relationship", hmFromAndToTypes.get("fromrel"));
			relationshipInfoMap.put("fromSide", hmFromToSide);

			hmFromToSide = (HashMap) relationshipInfoMap.get("toSide");
			hmFromToSide.put("type", hmFromAndToTypes.get("totypes"));
			hmFromToSide.put("relationship", hmFromAndToTypes.get("torel"));
			relationshipInfoMap.put("toSide", hmFromToSide);
			
		} catch (Exception e) {
			// System.out.println(" Error in getting relationship Info");
			e.printStackTrace();
		}

		return _objectMapper.writeValueAsString(relationshipInfoMap);
	}

	@Override
	public String geSchemaInfoWithPath(Context context, String strSchemaName, String strExportPath) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}