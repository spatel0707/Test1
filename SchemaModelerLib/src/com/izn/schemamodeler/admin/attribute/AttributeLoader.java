package com.izn.schemamodeler.admin.attribute;

import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.izn.schemamodeler.SchemaFactoryInfo;
import com.izn.schemamodeler.SchemaLoader;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
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
import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

import org.apache.commons.collections.CollectionUtils;

public class AttributeLoader implements SchemaLoader {
	ObjectMapper _gson = new ObjectMapper();
	AttributeXMLSchema.Schema _schemaEle = null;
	AttributeXMLSchema.Schema.Basic _basicElem = null;
	AttributeXMLSchema.Schema.Basic.Field _fieldElem = null;
	AttributeXMLSchema.Schema.Basic.Field.Detail _detailElem = null;
	AttributeXMLSchema.Schema.Range _rangeElem = null;
	AttributeXMLSchema.Schema.Range.Rangedetail _rangedetail = null;
	AttributeXMLSchema.Schema.Trigger _triggerElem = null;
	AttributeXMLSchema.Schema.Trigger.Event _eventElem = null;
	AttributeXMLSchema.Schema.Trigger.Event.Eventdetail _eventdetailElem = null;
	String MAXLENGTH = "maxlength";
	String VALUETYPE = "valuetype";
	String MULTILINE = "multiline";
	String DIMENSION = "dimension";
	String RESETCLONE = "resetonclone";
	String RESETREVISION = "resetonrevision";
	StringBuilder sbAttributeMQL = new StringBuilder();
	List<Attribute> ltAttributes = new ArrayList();
	Map<String, String> mOpearators = new HashMap();

	public AttributeLoader() {
		this.mOpearators.put("equal", "=");
		this.mOpearators.put("notequal", "!=");
		this.mOpearators.put("greaterthan", ">");
		this.mOpearators.put("greaterthanequal", ">=");
		this.mOpearators.put("lessthan", "<");
		this.mOpearators.put("lessthanequal", "<=");
		this.mOpearators.put("notmatch", "!match");
		this.mOpearators.put("match", "match");
		this.mOpearators.put("notstringmatch", "!smatch");
		this.mOpearators.put("stringmatch", "smatch");
		this.mOpearators.put("between", "between");
		this.mOpearators.put("program", "program");
	}

	public void loadSchema(Context context, String strSchemaName, String fileName, Logger schema_done_log,SCMConfigProperty scmConfigProperty)  throws Exception{
		try {
			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
			AttributeInfo attribute = (AttributeInfo) schemaFactory.getSchemaObject("attribute");

			JAXBContext jContext = JAXBContext.newInstance(new Class[] { AttributeXMLSchema.class });
			Unmarshaller unMarsheller = jContext.createUnmarshaller();
			JAXBElement<AttributeXMLSchema> attributeEle = unMarsheller
					.unmarshal(new StreamSource(new FileReader(fileName)), AttributeXMLSchema.class);
			AttributeXMLSchema _axComponent = (AttributeXMLSchema) attributeEle.getValue();
			Map<String, String> mBasic = new HashMap();
			Map<String, Object> mRange = new HashMap();

			List<AttributeXMLSchema.Schema> lsScheama = _axComponent.getSchema();
			Iterator<AttributeXMLSchema.Schema> itrSchema = lsScheama.iterator();
			Attribute attrib = null;
			String attributeName = null;
			int iCountSchema = 0;
			String strFValue = "";
			while (itrSchema.hasNext()) {
				iCountSchema += 1;
				List slTrigger = new ArrayList();
				List slRanges = new ArrayList();
				this._schemaEle = ((AttributeXMLSchema.Schema) itrSchema.next());
				this._basicElem = this._schemaEle.getBasic();

				attrib = new Attribute(this._basicElem.getName(), this._basicElem.getDescription(),
						this._basicElem.getHidden(), this._basicElem.getRegistryName());
				this._fieldElem = this._basicElem.getField();
				List<AttributeXMLSchema.Schema.Basic.Field.Detail> lsDetails = this._fieldElem.getDetail();
				Iterator<AttributeXMLSchema.Schema.Basic.Field.Detail> itrDetails = lsDetails.iterator();
				while (itrDetails.hasNext()) {
					this._detailElem = ((AttributeXMLSchema.Schema.Basic.Field.Detail) itrDetails.next());
					strFValue = _detailElem.getValueAttribute();
					if (strFValue != null) {
						strFValue = _detailElem.getValueAttribute().trim();
					} else {
						strFValue = "";
					}
					if (this._detailElem.getName().equalsIgnoreCase("DEFAULT")) {
						attrib.setDeFault(strFValue);
					} else if (this._detailElem.getName().equalsIgnoreCase("TYPE")) {
						attrib.setType(strFValue);
					} else if (this._detailElem.getName().equalsIgnoreCase("MULTILINE")) {
						attrib.setMultiLine(strFValue);
					} else if (this._detailElem.getName().equalsIgnoreCase("MAXLENGTH")) {
						attrib.setMaxLength(strFValue);
					} else if (this._detailElem.getName().equalsIgnoreCase("VALUETYPE")) {
						attrib.setValueType(strFValue);
					} else if (this._detailElem.getName().equalsIgnoreCase("RESETONCLONE")) {
						attrib.setResetOnClone(strFValue);
					} else if (this._detailElem.getName().equalsIgnoreCase("RESETONREVISION")) {
						attrib.setResetOnRevision(strFValue);
					} else if (this._detailElem.getName().equalsIgnoreCase("DIMENSION")) {
						attrib.setDimension(strFValue);
					} else if (this._detailElem.getName().equalsIgnoreCase("OWNER")) {
						attrib.setOwner(strFValue);
					} else if (this._detailElem.getName().equalsIgnoreCase("OWNERKIND")) {
						attrib.setOwnerKind(strFValue);
					}
				}
				if (this._schemaEle.getRange() != null) {
					this._rangeElem = this._schemaEle.getRange();
					List<AttributeXMLSchema.Schema.Range.Rangedetail> lstRangeDetails = this._rangeElem
							.getRangedetail();
					Iterator<AttributeXMLSchema.Schema.Range.Rangedetail> itrRangedetail = lstRangeDetails.iterator();
					while (itrRangedetail.hasNext()) {
						mRange = new HashMap();
						this._rangedetail = ((AttributeXMLSchema.Schema.Range.Rangedetail) itrRangedetail.next());
						mRange.put("operator", this._rangedetail.getType());
						mRange.put("value", this._rangedetail.getValueAttribute().split(","));
						slRanges.add(mRange);
					}
					attrib.setSlRanges(slRanges);
				}
				String strDbDef = attribute.geSchemaInfo(context, this._basicElem.getName(), "tbd");
				if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strDbDef)) {
					attributeName = this._basicElem.getName();
					HashMap<Object, Object> hDBDef = (HashMap) this._gson.readValue(strDbDef, HashMap.class);
					attrib.setSlFilterRanges(filterRanges(slRanges, (List) hDBDef.get("ranges")));
				}
				if (this._schemaEle.getTrigger() != null) {
					this._triggerElem = this._schemaEle.getTrigger();

					List<AttributeXMLSchema.Schema.Trigger.Event> lstEvent = this._triggerElem.getEvent();
					Iterator<AttributeXMLSchema.Schema.Trigger.Event> itrEvent = lstEvent.iterator();
					Map<String, String> mTriggerDetails = null;
					String strEvent = "";

					while (itrEvent.hasNext()) {

						this._eventElem = itrEvent.next();

						strEvent = this._eventElem.getName();
						List<AttributeXMLSchema.Schema.Trigger.Event.Eventdetail> lstEventdetails = this._eventElem
								.getEventdetail();
						Iterator<AttributeXMLSchema.Schema.Trigger.Event.Eventdetail> itrEventdetails = lstEventdetails
								.iterator();
						while (itrEventdetails.hasNext()) {
							this._eventdetailElem = itrEventdetails.next();
							mTriggerDetails = new HashMap<String, String>();
							mTriggerDetails.put(ACTION, strEvent);
							mTriggerDetails.put(TYPE, _eventdetailElem.getType());
							mTriggerDetails.put(PROGRAM, _eventdetailElem.getProgram());
							mTriggerDetails.put(NAME, _eventdetailElem.getInput());
							slTrigger.add(mTriggerDetails);
						}
					}
					attrib.setSlTriggers(slTrigger);
				}

				if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strDbDef)) {
					HashMap<Object, Object> hDBDef = (HashMap) this._gson.readValue(strDbDef, HashMap.class);
					attrib.setSlFilterTrigger(com.izn.schemamodeler.util.UIUtil.filteredListOMap(slTrigger,
							(List) hDBDef.get("trigger")));
				}
				this.ltAttributes.add(attrib);
			}
			// Gson gson = new Gson();
			// String json = gson.toJson(this.ltAttributes);
			prepareAttributeMQL(context, this.ltAttributes, attributeName, schema_done_log, scmConfigProperty);
		} catch (UnmarshalException ume) {
			schema_done_log.error("UnmarshalException on ["+strSchemaName+".xml] : "+ume.getCause());
			schema_done_log.warn(UIUtil.sUnmarshalExceptionWarningMessage);
			throw ume;
		} catch (Exception e) {
			schema_done_log.error(""+e.getCause());
			throw e;
		}
	}

	public static List filterRanges(List slNewRanges, List slOldRanges) throws Exception {
		List<Map> slFilteredRanges = new ArrayList();
		try {
			Iterator<Map> itrOldRanges = slOldRanges.iterator();
			Map mFilterRanges = new HashMap();
			String strOldOperator = "";
			String stNewOperator = "";
			boolean bMatch = false;
			while (itrOldRanges.hasNext()) {
				bMatch = false;
				Map mOldRange = (Map) itrOldRanges.next();
				strOldOperator = (String) mOldRange.get("operator");
				Iterator<Map> itrNewRanges = slNewRanges.iterator();
				while (itrNewRanges.hasNext()) {
					mFilterRanges = new HashMap();
					Map mNewRange = (Map) itrNewRanges.next();
					stNewOperator = (String) mNewRange.get("operator");
					if ((stNewOperator.equalsIgnoreCase(strOldOperator))
							&& (!strOldOperator.equalsIgnoreCase("program"))
							&& (!strOldOperator.equalsIgnoreCase("between"))) {
						bMatch = true;
						String[] sArrNewValues = (String[]) mNewRange.get("value");
						List slOldValues = (List) mOldRange.get("value");
						List sNewValues = Arrays.asList(sArrNewValues);
						mFilterRanges.put("operator", strOldOperator);
						mFilterRanges.put(stNewOperator.toLowerCase() + "_Ranges",
								CollectionUtils.subtract(slOldValues, sNewValues));
						slFilteredRanges.add(mFilterRanges);
					}
				}
				if (!bMatch) {
					slFilteredRanges.add(mOldRange);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return slFilteredRanges;
	}

	private void prepareAttributeMQL(Context context, List<Attribute> lstAttribute, String attributeName, Logger schema_done_log,SCMConfigProperty scmConfigProperty) throws Exception{
		String strAttributeName = "";
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
		String sOperation = "";
		List<String> listModified = new ArrayList<String>();
		List<String> listAdded = new ArrayList<String>();
		List<String> listDeleted = new ArrayList<String>();
		try {
			Iterator<Attribute> itrAttibute = lstAttribute.iterator();
			Attribute attribute = null;
			iCountTotal = lstAttribute.size();
			while (itrAttibute.hasNext()) {
				
				attribute = (Attribute) itrAttibute.next();
				this.sbAttributeMQL.setLength(0);
				schema_done_log.info("Attribute : ["+attribute.name.replace(UIUtil.removeCharecter, "")+"]");
				ContextUtil.pushContext(context);
				strAttributeName = attribute.name;
				try {
					localMQLCommand = new MQLCommand();
					String removeSchemaObject = UIUtil.removeSchemaObject(strAttributeName,"attribute");
					if(removeSchemaObject.isEmpty())
					{						
						bMQLResult = localMQLCommand.executeCommand(context, "print attribute $1", new String[] { strAttributeName.replace(UIUtil.removeCharecter, "") });
						System.out.println("bMQLResult :"+bMQLResult);
						if (bMQLResult) {
							sMQL = prepareModifyExistingSchemaMQL(attribute,schema_done_log);
							iCountModify += 1;
							listModified.add(strAttributeName);
							sOperation = "mod";
						}else{
							sMQL = prepareAddNewSchemaMQL(attribute,schema_done_log);
							iCountAdd += 1;
							listAdded.add(strAttributeName);
							sOperation = "add";
						}
						sMQLPropertyQuery = UIUtil.getMQLPropertyQuery(context,"attribute",attribute.name,attribute.registryname,sOperation);
					}
					else 
					{
						sMQL = removeSchemaObject;
						iCountDelete += 1;
						listDeleted.add(strAttributeName);
					}
					schema_done_log.info("MQL QUERY FOR IMPORT : "+sMQL);
					//localMQLCommand = new MQLCommand();
					bMQLResult = localMQLCommand.executeCommand(context, sMQL, true);
					String sMQLError = (String)localMQLCommand.getError();
					schema_done_log.info("MQL QUERY EXECUTION RESULT : " + bMQLResult);
					if (bMQLResult) {
						iCountSuccess += 1;						
						if (sMQLPropertyQuery != null && !"".equals(sMQLPropertyQuery)) {
							schema_done_log.info("MQL QUERY {PROPERTY} : " + sMQLPropertyQuery);
							localMQLCommand.executeCommand(context, sMQLPropertyQuery, true);							
						}
					} else {
						iCountFailure += 1;
						if(sMQL.trim().toLowerCase().startsWith("add"))
							iCountAdd -= 1; 
						else if(sMQL.trim().toLowerCase().startsWith("mod"))
							iCountModify -= 1;
						else
							iCountDelete -= 1;
						throw new MatrixException(sMQLError);						
					}
					ContextUtil.popContext(context);
				} catch (Exception e) {
					ContextUtil.popContext(context);
					schema_done_log.info("TOTAL :" + iCountTotal + ", SUCCESS :" + iCountSuccess +  ", ADDED :" + iCountAdd + ", MODIFIED :" + iCountModify + ", DELETED :"	+ iCountDelete + ".");
					throw new Exception("Error occurred while importing schema attribute : ["+strAttributeName.replace(UIUtil.removeCharecter, "")+"] : " +e.getMessage());
				} 
				schema_done_log.info("-----------------------------------------------------------------");
			} 
			
			schema_done_log.info("TOTAL  :" + iCountTotal + ", SUCCESS :" + iCountSuccess + ", ADDED  :" + iCountAdd + ", MODIFIED :" + iCountModify + ", DELETED :" + iCountDelete +".");
			if("true".equalsIgnoreCase(scmConfigProperty.getLogEverything())) {					
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
			Attribute objInfo = (Attribute) objectSchemaInfo;
			sbMQL.append(" add attribute ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null && !objInfo.description.isEmpty()) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.getDeFault() != null && !objInfo.getDeFault().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces("default")).append(UIUtil.quoteArgument(objInfo.getDeFault()));
			}
			if (objInfo.getType() != null && !objInfo.getType().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(" type"))
						.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.getType())));
			}
			if (objInfo.getOwner() != null && !objInfo.getOwner().isEmpty()) {

				sbMQL.append(UIUtil.padWithSpaces(" owner"))
						.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.getOwnerKind())));
				sbMQL.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.getOwner())));
				
			}
			if (objInfo.getMaxLength() != null && !objInfo.getMaxLength().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(this.MAXLENGTH)).append(UIUtil.quoteArgument(objInfo.getMaxLength()));
			}
			if (objInfo.getMultiLine() != null && !objInfo.getMultiLine().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.getMultiLine()));
			}
			if (objInfo.getValueType() != null && !objInfo.getValueType().isEmpty() && !objInfo.getType().equals("binary")) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.getValueType()));
			}
			if (objInfo.getResetOnClone() != null && !objInfo.getResetOnClone().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.getResetOnClone()));
			}
			if (objInfo.getResetOnRevision() != null && !objInfo.getResetOnRevision().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.getResetOnRevision()));
			}
			if (objInfo.getResetOnRevision() != null && !objInfo.getResetOnRevision().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.getResetOnRevision()));
			}
			if (objInfo.getDimension() != null && !objInfo.getDimension().isEmpty()) {
				sbMQL.append(" DIMENSION").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.getDimension())));
			}
			if (objInfo.getSlRanges() != null && !objInfo.getSlRanges().isEmpty()) {
				List slNewRanges = objInfo.getSlRanges();
				Iterator<Map> itrNewRanges = slNewRanges.iterator();
				while (itrNewRanges.hasNext()) {
					Map m = (Map) itrNewRanges.next();
					String strOperator = (String) m.get("operator");
					String[] sValues = (String[]) m.get("value");
					if (strOperator.equalsIgnoreCase("programRange")) {
						sbMQL.append(" RANGE").append(UIUtil.padWithSpaces("program"))
								.append(UIUtil.quoteArgument(sValues[0])).append(" input ")
								.append(UIUtil.quoteArgument(sValues[1]));
					} else if (strOperator.equalsIgnoreCase("between")) {
						sbMQL.append(" RANGE").append(UIUtil.padWithSpaces(strOperator)).append(sValues[0])
								.append(sValues[1].equalsIgnoreCase("true") ? " inclusive " : " exclusive ");
						sbMQL.append(sValues[2])
								.append(sValues[3].equalsIgnoreCase("true") ? " inclusive " : " exclusive");
					} else {
						for (String v : sValues) {
							sbMQL.append(" RANGE")
									.append(UIUtil.padWithSpaces((String) mOpearators.get(strOperator.toLowerCase())))
									.append(" ").append('"').append(((v))).append(" ").append('"');
						}
					}
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
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}

	private String prepareModifyExistingSchemaMQL(Object objectSchemaInfo, Logger schema_done_log) throws Exception {
		StringBuilder sbMQL = new StringBuilder();
		try {
			Attribute objInfo = (Attribute) objectSchemaInfo;
			sbMQL.append(" mod attribute ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument("'"+objInfo.name+"'")));
			System.out.println("------------------------------objInfo.name------"+objInfo.name);
			if (objInfo.description != null) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.getDeFault() != null && !objInfo.getDeFault().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces("default")).append(UIUtil.quoteArgument(objInfo.getDeFault()));
			}
			if (objInfo.getType() != null && !objInfo.getType().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(" type"))
						.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.getType())));
			}
			if (objInfo.getMaxLength() != null && !objInfo.getMaxLength().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(this.MAXLENGTH)).append(UIUtil.quoteArgument(objInfo.getMaxLength()));
			}else  
				sbMQL.append(UIUtil.padWithSpaces(this.MAXLENGTH)).append("0");
			
			if (objInfo.getMultiLine() != null && !objInfo.getMultiLine().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.getMultiLine()));
			}
			if (objInfo.getValueType() != null && !objInfo.getValueType().isEmpty() && !objInfo.getType().equals("binary")) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.getValueType()));
			}
			if (objInfo.getResetOnClone() != null && !objInfo.getResetOnClone().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.getResetOnClone()));
			}
			if (objInfo.getResetOnRevision() != null && !objInfo.getResetOnRevision().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.getResetOnRevision()));
			}
			if (objInfo.getResetOnRevision() != null && !objInfo.getResetOnRevision().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.getResetOnRevision()));
			}
			if (objInfo.getDimension() != null && !objInfo.getDimension().isEmpty()) {
				sbMQL.append(UIUtil.removeFieldDetail(objInfo.getDimension(), "DIMENSION"));
			}
			if (objInfo.getSlRanges() != null && !objInfo.getSlRanges().isEmpty()) {
				List slNewRanges = objInfo.getSlRanges();
				sbMQL.append(removeRange(slNewRanges));
			}
			List<Map<Object, Object>> slNewTriggers = objInfo.getSlTriggers();
			if (slNewTriggers != null && !slNewTriggers.isEmpty()) {
				sbMQL.append(UIUtil.removeTrigger(slNewTriggers));
			}
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}

	private String removeRange(List slNewRanges) throws Exception {
		Iterator<Map> itrNewRanges = slNewRanges.iterator();
		StringBuilder sbReturn = new StringBuilder();
		String strRangeMod = "";
		while (itrNewRanges.hasNext()) {
			Map m = (Map) itrNewRanges.next();
			String strOperator = (String) m.get("operator");
			String[] sValues = (String[]) m.get("value");
			if (strOperator.startsWith(UIUtil.removeCharecter) && strOperator.endsWith(UIUtil.removeCharecter)) {
				strOperator = strOperator.replace(UIUtil.removeCharecter, "");
				strRangeMod = " REMOVE RANGE";
			} else if (!strOperator.startsWith(UIUtil.removeCharecter)
					&& !strOperator.endsWith(UIUtil.removeCharecter)) {
				strRangeMod = " ADD RANGE";
			}
			if (strOperator.equalsIgnoreCase("programRange")) {
				sbReturn.append(strRangeMod).append(UIUtil.padWithSpaces("program"))
						.append(UIUtil.quoteArgument(sValues[0])).append(" input ")
						.append(UIUtil.quoteArgument(sValues[1]));
			} else if (strOperator.equalsIgnoreCase("between")) {
				sbReturn.append(strRangeMod).append(UIUtil.padWithSpaces(strOperator)).append(sValues[0])
						.append(sValues[1].equalsIgnoreCase("true") ? " inclusive " : " exclusive ");
				sbReturn.append(sValues[2]).append(sValues[3].equalsIgnoreCase("true") ? " inclusive " : " exclusive");
			} else {
				for (String v : sValues) {
					if (" ADD RANGE".equalsIgnoreCase(strRangeMod)) {
						if (v.startsWith(UIUtil.removeCharecter) && v.endsWith(UIUtil.removeCharecter)) {
							v = v.replace(UIUtil.removeCharecter, "");
							sbReturn.append(" REMOVE RANGE")
									.append(UIUtil.padWithSpaces((String) mOpearators.get(strOperator.toLowerCase())))
									.append('"').append(((v))).append('"');
						} else if (!v.startsWith(UIUtil.removeCharecter) && !v.endsWith(UIUtil.removeCharecter)) {
							sbReturn.append(" ADD RANGE")
									.append(UIUtil.padWithSpaces((String) mOpearators.get(strOperator.toLowerCase())))
									.append('"').append(((v))).append('"');
						}
					} else {
						sbReturn.append(strRangeMod)
								.append(UIUtil.padWithSpaces((String) mOpearators.get(strOperator.toLowerCase())))
								.append('"').append(((v))).append('"');
					}
				}
			}
		}
		return sbReturn.toString();
	}
}
