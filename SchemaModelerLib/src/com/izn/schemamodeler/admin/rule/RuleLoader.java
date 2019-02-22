package com.izn.schemamodeler.admin.rule;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.izn.schemamodeler.SchemaFactoryInfo;
import com.izn.schemamodeler.SchemaLoader;
import com.izn.schemamodeler.admin.rule.Rule;
import com.izn.schemamodeler.admin.rule.RuleInfo;
import com.izn.schemamodeler.admin.rule.RuleXMLSchema;
import com.izn.schemamodeler.admin.rule.RuleXMLSchema.Schema;
import com.izn.schemamodeler.admin.rule.RuleXMLSchema.Schema.Basic;
import com.izn.schemamodeler.admin.rule.RuleXMLSchema.Schema.Field;
import com.izn.schemamodeler.admin.rule.RuleXMLSchema.Schema.Field.Detail;
import com.izn.schemamodeler.admin.rule.RuleXMLSchema.Schema.ObjectAcess;
import com.izn.schemamodeler.admin.rule.RuleXMLSchema.Schema.ObjectAcess.Accessdetail;
import com.izn.schemamodeler.util.UIUtil;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.matrixone.apps.domain.util.ContextUtil;
import matrix.db.Context;
import matrix.db.MQLCommand;

public class RuleLoader implements SchemaLoader {

	ObjectMapper _gson = new ObjectMapper();
	Basic _basicElem = null;
	Field _fieldElem = null;
	
	ObjectAcess _objeAcess = null;
	Accessdetail _accessDetail = null ;
	public RuleLoader() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void loadSchema(Context context, String strSchemaName, String fileName, Logger schema_done_log,SCMConfigProperty scmConfigProperty)  throws Exception{
		try {
			List<Rule> lstRule = new ArrayList<Rule>();
			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
			RuleInfo ruleInfo = (RuleInfo) schemaFactory.getSchemaObject("rule");
			// String strInterfacesInfo = interfacesInfo.geSchemaInfo(context,
			// "TestInterface", "td");
			// Map mDBInfo = _gson.fromJson(strInterfacesInfo, HashMap.class);
			JAXBContext jConext = JAXBContext.newInstance(RuleXMLSchema.class);
			Unmarshaller unmarshaller = jConext.createUnmarshaller();
			JAXBElement<RuleXMLSchema> ruleElem = unmarshaller.unmarshal(new StreamSource(new File(fileName)),
					RuleXMLSchema.class);
			RuleXMLSchema ruleXMLSchema = ruleElem.getValue();
			// List<Schema> lstSchema = ruleXMLSchema.getSchema();
			List<Schema> lstSchema = (List<Schema>) ruleXMLSchema.getSchema();
			Rule rule = null;
			String fValue = "";
			String fName = "";
			Map<String, String> mSettings = null;
			List<Map<String, String>> lstSetting = null;
			List<String> lstRuleItem = null;
			List<String> lstCommandItem = null;
			for (Schema _schema : lstSchema) {
				lstSetting = new ArrayList<Map<String, String>>();
				lstCommandItem = new ArrayList<String>();
				lstRuleItem = new ArrayList<String>();
				_basicElem = _schema.getBasic();
				rule = new Rule(_basicElem.getName(), _basicElem.getDescription(), _basicElem.getHidden());
				_fieldElem = _schema.getField();
				
				List<Detail> lstDetail = _fieldElem.getDetail();
				List<Detail> lDetail = _accessDetail.getDetail();
				
				for (Detail _detail : lstDetail) {
					fName = _detail.getName();
					fValue = _detail.getValue();
					if(fValue != null) {
						fValue = _detail.getValue().trim();
					}else{
						fValue = "";
					}	
//					if (fName.equalsIgnoreCase("user")) {
//						rule.setUser(fValue);
//					} else 
						
					if (fName.equalsIgnoreCase("governedPrograms")) {
						rule.setGovernedPrograms(fValue);
					} else if (fName.equalsIgnoreCase("governedAttribute")) {
						rule.setGovernedAttribute(fValue);
					} else if (fName.equalsIgnoreCase("governedForms")) {
						rule.setGovernedForms(fValue);
					} else if (fName.equalsIgnoreCase("governedRelationships")) {
						rule.setGovernedRelationships(fValue);
					}
					
				}
				
				// ---access  added for access detail -----ObjectAcess
				_objeAcess = (ObjectAcess) _fieldElem.getDetail();
				for (Detail _detail : lDetail) {
				fName = _detail.getName();
				fValue = _detail.getValue().trim();
				
					if (fName.equalsIgnoreCase("user")) {
					rule.setUser(fValue);
					} 
					lstRule.add(rule);
					}
				


				// --------- end ---------------
			}
			prepareRuleMQL(context, lstRule);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("deprecation")
	private void prepareRuleMQL(Context context, List<Rule> lstRule) throws Exception{
		try {
			StringBuilder sbMQL = new StringBuilder();
			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
			RuleInfo ruleInfo = (RuleInfo) schemaFactory.getSchemaObject("rule");
			for (Rule rule : lstRule) {
				//System.out.println("Ruleloader ......... line 103" + rule.name);
				String strRuleinfo = ruleInfo.geSchemaInfo(context, rule.name, "tbd");
				//System.out.println("Ruleloader ......... line 103" + strRuleinfo);
				Map mDBInfo = _gson.readValue(strRuleinfo, HashMap.class);

				String strRuleName = rule.name;
				//System.out.println("Rule Loader line 106 RuleName is.........." + strRuleName);
				String strResult = MQLCommand.exec(context, "list Rule $1", new String[] { strRuleName });

				if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
					sbMQL.append(" modify rule").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(rule.name)));
				} else {
					sbMQL.append(" add rule").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(rule.name)));
				}

				if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(rule.description)) {
					sbMQL.append(" description ").append(UIUtil.quoteArgument(rule.description))
							.append(UIUtil.padWithSpaces(rule.hidden));
				}
				// sbMQL.append(" abstract ").append(rule.getSabstract());
//--------------------------------------------------------------------------------
				if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
					  sbMQL = clearEarlier(sbMQL,mDBInfo,"settings");
					  sbMQL.append( " remove user all ");
				  }
				  
				  String sUser =  rule.getUser();
				 // String sUser = strRuleName.get
				  for(String user:sUser.split(",")){
					  if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
						  sbMQL.append( " add user ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(user))); 
						  } else {
							  sbMQL.append( " user ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(user))); 
							  }
						  
				  }
				  	 
//	--------------------------		-------------------------------------------
				if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
					sbMQL = clearEarlier(sbMQL, mDBInfo, "settings");
					sbMQL = removeEarlierItems(sbMQL, mDBInfo);
					clearEarlier(sbMQL, mDBInfo, "governedAttribute");
				}
				String sAttributes = rule.getGovernedAttribute();
				String[] sArray = sAttributes.split(",");

				for (String sAttribute : sArray) {
					if (!sAttribute.isEmpty())
						if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
							sbMQL.append(" rule ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sAttribute)));
						} else {
							sbMQL.append(" ; ");
							MQLCommand.exec(context, sbMQL.toString());
							sbMQL = new StringBuilder();
							sbMQL.append("mod attribute ")
									.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sAttribute)) + " add rule "
											+ rule.name + " ");
							sbMQL.append(" ; ");
							//System.out.println("140 sbMQL.toString() :::::::" + sbMQL.toString());
							MQLCommand.exec(context, sbMQL.toString());
							sbMQL = new StringBuilder();
						}
				}
				// added start
				if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
					clearEarlier(sbMQL, mDBInfo, "governedPrograms");
				}
				String sPrograms = rule.getGovernedPrograms();

				String[] sArray1 = sPrograms.split(",");

				for (String sPrograms1 : sArray1) {
					if (!sPrograms1.isEmpty())
						if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
							// sbMQL.append(" add program
							// ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sPrograms1)));+ "
							// add program"+rule.name+" ")
							sbMQL.append(" program ")
									.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sPrograms1)));

						} else {
							sbMQL.append(" ; ");
							MQLCommand.exec(context, sbMQL.toString());
							sbMQL = new StringBuilder();
							sbMQL.append("  mod program ")
									.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sPrograms1)) + " add rule "
											+ rule.name + " ");
							sbMQL.append(" ; ");
							//System.out.println("164 sbMQL.toString() :::::::" + sbMQL.toString());
							MQLCommand.exec(context, sbMQL.toString());
							sbMQL = new StringBuilder();
						}
				}

				if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
					clearEarlier(sbMQL, mDBInfo, "governedForms");
				}
				String sForms = rule.getGovernedForms();
				String[] sArray2 = sForms.split(",");

				for (String sForm : sArray2) {
					if (!sForm.isEmpty())
						if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
							sbMQL.append(" form ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sForm)));
						} else {
							sbMQL.append(" ; ");
							MQLCommand.exec(context, sbMQL.toString());
							sbMQL = new StringBuilder();
							sbMQL.append(" mod form ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sForm))
									+ " add rule " + rule.name + " ");
							sbMQL.append(" ; ");
							//System.out.println("186 sbMQL.toString() :::::::" + sbMQL.toString());
							MQLCommand.exec(context, sbMQL.toString());
							sbMQL = new StringBuilder();
						}
				}

				// added end
				// if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
				// clearEarlier(sbMQL,mDBInfo,"objectAccess"); }
				//
				// String sTypes = rule.getObjectAccess();
				// sArray=sTypes.split(",");
				// for(String sType:sArray){
				// if(!sType.isEmpty())
				// if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
				// sbMQL.append(" add type
				// ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sType)));
				// } else {
				// sbMQL.append(" type
				// ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sType)));
				// }
				// }

				if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
					clearEarlier(sbMQL, mDBInfo, "governedRelationships");
				}
				String sRelationship = rule.getGovernedRelationships();
				sArray = sRelationship.split(",");
				for (String sRel : sArray) {
					if (!sRel.isEmpty())
						if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
							sbMQL.append(" add relationship ")
									.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sRel)));
						} else {
							sbMQL.append(" ; ");
							MQLCommand.exec(context, sbMQL.toString());
							sbMQL = new StringBuilder();
							sbMQL.append(" mod relationship ")
									.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sRel)) + " add rule "
											+ rule.name + " ");
							sbMQL.append(" ; ");
							//System.out.println("221 sbMQL.toString() :::::::" + sbMQL.toString());
							MQLCommand.exec(context, sbMQL.toString());
							sbMQL = new StringBuilder();
							// sbMQL.append(" relationship
							// ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sRel)));
						}
				}
				sbMQL.append(" ; ");
			}

			try {
				ContextUtil.pushContext(context);
				//System.out.println("sbMQL.toString() :::::::" + sbMQL.toString());
				MQLCommand.exec(context, sbMQL.toString());
				ContextUtil.popContext(context);
			} catch (Exception e) {
				ContextUtil.popContext(context);
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private StringBuilder clearEarlier(StringBuilder sbMQL, Map mDBInfo, String key) {
		//System.out.println("Rule Loader line 213 ........" + mDBInfo);
		String sRemoveKey = " remove " + key;
		List<String> lstSettings = (List<String>) mDBInfo.get(key);
		for (String sValue : lstSettings) {
			sbMQL.append(sRemoveKey).append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sValue)));
		}
		return sbMQL;
	}

	private StringBuilder removeEarlierItems(StringBuilder sbMQL, Map mDBInfo) {

		List<Map> lstSettings = (List<Map>) mDBInfo.get("items");
		String strItemType = "";
		String sCommand = " remove command ";
		for (Map m : lstSettings) {
			sCommand = " remove command ";
			strItemType = (String) m.get("type");
			if (strItemType.equalsIgnoreCase("rule")) {
				sCommand = " remove rule ";
			}
			sbMQL.append(sCommand).append(UIUtil.quoteArgument((String) m.get("name")));
		}
		return sbMQL;
	}

}
