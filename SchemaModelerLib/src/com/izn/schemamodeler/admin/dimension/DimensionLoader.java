package com.izn.schemamodeler.admin.dimension;

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
import com.izn.schemamodeler.admin.dimension.Dimension;
import com.izn.schemamodeler.admin.dimension.DimensionInfo;
import com.izn.schemamodeler.admin.dimension.DimensionXMLSchema;
import com.izn.schemamodeler.admin.dimension.DimensionXMLSchema.Schema;
import com.izn.schemamodeler.admin.dimension.DimensionXMLSchema.Schema.Basic;
import com.izn.schemamodeler.admin.dimension.DimensionXMLSchema.Schema.Field;
import com.izn.schemamodeler.admin.dimension.DimensionXMLSchema.Schema.Field.Data;
import com.izn.schemamodeler.admin.dimension.DimensionXMLSchema.Schema.Field.Data.Datadetail;
import com.izn.schemamodeler.admin.dimension.DimensionXMLSchema.Schema.Field.Data.Setting;
import com.izn.schemamodeler.admin.dimension.DimensionXMLSchema.Schema.Field.Data.Setting.Param;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MapList;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class DimensionLoader implements SchemaLoader {
	ObjectMapper _gson = new ObjectMapper();
	Basic _basicElem = null;
	Field _fieldElem = null;
	Setting _setting = null;

	public DimensionLoader() {
		// System.out.println("////DimensionLoader////////");
		// TODO Auto-generated constructor stub
	}

	@Override
	public void loadSchema(Context context, String strSchemaName, String fileName, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty)  throws Exception{
		try {

			HashMap map = new HashMap();
			List<Dimension> lstDimension = new ArrayList<Dimension>();
			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
			DimensionInfo dimensionInfo = (DimensionInfo) schemaFactory.getSchemaObject("dimension");
			JAXBContext jConext = JAXBContext.newInstance(DimensionXMLSchema.class);
			Unmarshaller unmarshaller = jConext.createUnmarshaller();
			JAXBElement<DimensionXMLSchema> dimensionElem = unmarshaller.unmarshal(new StreamSource(new File(fileName)),
					DimensionXMLSchema.class);
			DimensionXMLSchema dimensionXMLSchema = dimensionElem.getValue();
			List<Schema> lstSchema = dimensionXMLSchema.getSchema();
			Dimension dimension = null;
			String fValue = "";
			String fName = "";
			Map<String, String> mSettings = null;
			List<Map<String, String>> lstSetting = null;
			List<String> lstDimensionItem = null;
			for (Schema _schema : lstSchema) {
				MapList mapList = new MapList();
				List<Dimension.Data> lstDData = new ArrayList<Dimension.Data>();
				lstDimensionItem = new ArrayList<String>();
				_basicElem = _schema.getBasic();
				dimension = new Dimension(_basicElem.getName(), _basicElem.getHidden(),_basicElem.getDescription(),	_basicElem.getRegistryName());
				_fieldElem = _schema.getField();
				List<Data> lstData = _fieldElem.getData();
				HashMap<String, String> unitValues;

				for (Data _data : lstData) {

					List<Datadetail> lstDataDetail = _data.getDatadetail();
					Dimension.Data dData = dimension.creatNewDataInstance();
					unitValues = new HashMap<String, String>();
					for (Datadetail datadetail : lstDataDetail) {

						lstSetting = new ArrayList<Map<String, String>>();
						fName = datadetail.getName();
						fValue = datadetail.getValueAttribute();
						unitValues.put(fName, fValue);
						if (fValue != null) {
							fValue = datadetail.getValueAttribute().trim();
						} else {
							fValue = "";
						}
						if (fName.equalsIgnoreCase("dbunit")) {
							dData.setDbunit(fValue);
						} else if (fName.equalsIgnoreCase("unit")) {
							dData.setUnit(fValue);
						} else if (fName.equalsIgnoreCase("label")) {
							dData.setLabel(fValue);
						} else if (fName.equalsIgnoreCase("unitdescription")) {
							dData.setUnitdescription(fValue);
						} else if (fName.equalsIgnoreCase("default")) {
							dData.setDefault(fValue);
						} else if (fName.equalsIgnoreCase("multiplier")) {
							dData.setMultiplier(fValue);
						} else if (fName.equalsIgnoreCase("offset")) {
							dData.setOffset(fValue);
						}
					}

					// process settings if any
					_setting = _data.getSetting();
					if (_setting != null) {
						List<Param> lstParam = _setting.getParam();
						// System.out.println("\nlstParam :"+lstParam);
						for (Param param : lstParam) {
							Map m = new HashMap();
							m.put("name", param.getName());
							m.put("value", param.getValueAttribute());
							// System.out.println("param.getName() :"+param.getName());
							// System.out.println("param.getValueAttribute() :"+param.getValueAttribute());
							lstSetting.add(m);
						}
						// System.out.println("lstSetting "+lstSetting.toString());
						unitValues.put("setting", lstSetting.toString());
						mapList.add(unitValues);
						dData.setLstSetting(lstSetting);
						// System.out.println("\ndData :"+dData);
					}
					lstDData.add(dData);
					// System.out.println("\nlstDData :"+lstDData);
				}
				map.put("units", mapList);
				dimension.setLstData(lstDData);
				lstDimension.add(dimension);
				map.clear();
			}
			prepareDimensionMQL(context, lstDimension, schema_done_log, scmConfigProperty);
		} catch (UnmarshalException ume) {
			schema_done_log.error("UnmarshalException on ["+strSchemaName+".xml] : "+ume.getCause());
			schema_done_log.warn(UIUtil.sUnmarshalExceptionWarningMessage);
			throw ume;
		} catch (Exception e) {
			schema_done_log.error(""+e.getCause());
			throw e;
		}
	}

	private void prepareDimensionMQL(Context context, List<Dimension> lstDimension, Logger schema_done_log,
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
		String strDimensionName = "";
		String sOperation = "";
		List<String> listModified = new ArrayList<String>();
		List<String> listAdded = new ArrayList<String>();
		List<String> listDeleted = new ArrayList<String>();
		try {
			iCountTotal = lstDimension.size();
			for (Dimension dimension : lstDimension) {			
				schema_done_log.info("Dimension : ["+ dimension.name.replace(UIUtil.removeCharecter, "") + "]");
				ContextUtil.pushContext(context);
				strDimensionName = dimension.name;
				try {
					String removeSchemaObject = UIUtil.removeSchemaObject(strDimensionName, "dimension");
					if (removeSchemaObject.isEmpty()) {
						String strResult = MQLCommand.exec(context, "list dimension $1",
								new String[] { strDimensionName.replace(UIUtil.removeCharecter, "") });
						if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
							List<String> alUnits = new ArrayList<String>();
							SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
							DimensionInfo formInfo = (DimensionInfo) schemaFactory.getSchemaObject("dimension");
							String strDbDef = formInfo.geSchemaInfo(context, dimension.name, "tbd");
							HashMap<Object, Object> hDBDef = _gson.readValue(strDbDef, HashMap.class);
							if (hDBDef != null && !hDBDef.isEmpty()) {
								List<Map> slDBColumns = (List<Map>) hDBDef.get("units");
								Iterator<Map> itrDBFields = slDBColumns.iterator();
								while (itrDBFields.hasNext()) {
									Map m = itrDBFields.next();
									alUnits.add((String) m.get("dbunit"));
								}
							}
							sMQL = prepareModifyExistingSchemaMQL(context,dimension, alUnits, schema_done_log);
							iCountModify += 1;
							listModified.add(strDimensionName);
							sOperation = "mod";
						} else {
							sMQL = prepareAddNewSchemaMQL(dimension, schema_done_log);
							iCountAdd += 1;
							listAdded.add(strDimensionName);
							sOperation = "add";
						}
						sMQLPropertyQuery = UIUtil.getMQLPropertyQuery(context, "dimension", dimension.name,
								dimension.registryname, sOperation);
					} else {
						sMQL = removeSchemaObject;
						iCountDelete += 1;
						listDeleted.add(strDimensionName);
					}
					schema_done_log.info("MQL QUERY FOR IMPORT : " + sMQL);
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
					throw new Exception("Error occurred while importing schema dimension : ["
							+ strDimensionName.replace(UIUtil.removeCharecter, "") + "] : " + e.getMessage());
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
			throw e;
		}
	}

	private String prepareAddNewSchemaMQL(Object objectSchemaInfo, Logger schema_done_log) throws Exception {
		StringBuilder sbMQL = new StringBuilder();
		try {
			Dimension objInfo = (Dimension) objectSchemaInfo;
			sbMQL.append(" add dimension ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null && !objInfo.description.isEmpty()) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			List<Dimension.Data> lstUnit = (List<Dimension.Data>) objInfo.getLstData();
			if (lstUnit != null && !lstUnit.isEmpty()) {
				for (Dimension.Data dUnit : lstUnit) {
					if (dUnit.getUnit() != null && !dUnit.getUnit().isEmpty()) {
						sbMQL.append(" unit").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(dUnit.getUnit())));
						if (dUnit.getLabel() != null && !dUnit.getLabel().isEmpty()) {
							if(!dUnit.getLabel().startsWith(UIUtil.removeCharecter)){
								sbMQL.append(" label").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(dUnit.getLabel())));
							}
						}
						if (dUnit.getUnitdescription() != null && !dUnit.getUnitdescription().isEmpty()) {
							if(!dUnit.getUnitdescription().startsWith(UIUtil.removeCharecter)){
								sbMQL.append(" unitdescription").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(dUnit.getUnitdescription())));
							}
						}
						if (dUnit.getDefault() != null && !dUnit.getDefault().isEmpty()) {
							if(dUnit.getDefault().equalsIgnoreCase("TRUE")){
								sbMQL.append(" default");
									
							} else {
								sbMQL.append(" !default");
								
							}
						}
						if (dUnit.getMultiplier() != null && !dUnit.getMultiplier().isEmpty()) {
							sbMQL.append(" multiplier")
									.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(dUnit.getMultiplier())));
						}
						if (dUnit.getOffset() != null && !dUnit.getOffset().isEmpty()) {
							sbMQL.append(" offset")
									.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(dUnit.getOffset())));
						}
						List<Map<String, String>> lstSettings = dUnit.getLstSetting();
						if (lstSettings != null && !lstSettings.isEmpty()) {
							for (Map<String, String> m : lstSettings) {
								sbMQL.append(" setting ")
										.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(m.get("name"))))
										.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(m.get("value"))));
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

	private String prepareModifyExistingSchemaMQL(Context context, Object objectSchemaInfo, List<String> alDBUnits,
			Logger schema_done_log) throws Exception {
		StringBuilder sbMQL = new StringBuilder();
		StringBuilder sbRemoveMQL = new StringBuilder();
		
		try {
			Dimension objInfo = (Dimension) objectSchemaInfo;
			sbMQL.append(" mod dimension ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			List<Dimension.Data> lstUnit = (List<Dimension.Data>) objInfo.getLstData();
			if (lstUnit != null && !lstUnit.isEmpty()) {
				boolean bColumnExist = false;
				boolean bCanRemove = false;
				String sUnitName = "";
				for (Dimension.Data dUnit : lstUnit) {
					if (dUnit.getUnit() != null && !dUnit.getUnit().isEmpty()) {
						sUnitName = dUnit.getUnit();
						if (sUnitName.startsWith(UIUtil.removeCharecter)
								&& sUnitName.endsWith(UIUtil.removeCharecter)) {
							sUnitName = sUnitName.replace(UIUtil.removeCharecter, "");
							bCanRemove = true;
						} else if (!sUnitName.startsWith(UIUtil.removeCharecter)
								&& !sUnitName.endsWith(UIUtil.removeCharecter)) {
							bCanRemove = false;
						}
						if (bCanRemove && alDBUnits.contains(sUnitName)) {
							MQLCommand localMQLCommand = new MQLCommand();
							sbRemoveMQL.append(" mod dimension ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
							sbRemoveMQL.append(" remove unit").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(sUnitName)));
							boolean bMQLResult = localMQLCommand.executeCommand(context, sbRemoveMQL.toString(), true);
							continue;
						} else if (bCanRemove && !alDBUnits.contains(sUnitName)) {
							continue;// throw exception : not properly removed.
						}
						if (alDBUnits.contains(sUnitName)) {
							sbMQL.append(" mod unit");
							bColumnExist = true;
						} else {
							sbMQL.append(" add unit");
							bColumnExist = false;
						}
						sbMQL.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(dUnit.getUnit())));
						if (dUnit.getLabel() != null && !dUnit.getLabel().isEmpty()) {
							if(!dUnit.getLabel().startsWith(UIUtil.removeCharecter)){	
								sbMQL.append(" label").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(dUnit.getLabel())));
							}  else {
								sbMQL.append(" label ").append("\" \"");
							}
						}
						if (dUnit.getUnitdescription() != null && !dUnit.getUnitdescription().isEmpty()) {
							if(!dUnit.getUnitdescription().startsWith(UIUtil.removeCharecter)){
								sbMQL.append(" unitdescription").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(dUnit.getUnitdescription())));
							} else {
								sbMQL.append(" unitdescription ").append("\" \"");
							}
						}
						if (dUnit.getDefault() != null && !dUnit.getDefault().isEmpty()) {
							if(dUnit.getDefault().equalsIgnoreCase("TRUE")){
								sbMQL.append(" default");
									
							} else {
								sbMQL.append(" !default");
								
							}
						}
						if (dUnit.getMultiplier() != null && !dUnit.getMultiplier().isEmpty()) {
							sbMQL.append(" multiplier")
									.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(dUnit.getMultiplier())));
						}
						if (dUnit.getOffset() != null && !dUnit.getOffset().isEmpty()) {
							sbMQL.append(" offset")
									.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(dUnit.getOffset())));
						}
						List<Map<String, String>> lstSettings = dUnit.getLstSetting();
						if (lstSettings != null && !lstSettings.isEmpty()) {
							for (Map<String, String> m : lstSettings) {
								/*if (!m.get("value").isEmpty() && bColumnExist) {
									sbMQL.append(" setting ")
											.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(m.get("name"))))
											.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(m.get("value"))));
								} else if (!m.get("value").isEmpty() && !bColumnExist) {
									sbMQL.append(" setting " + " \"" + m.get("name") + "\" \"" + m.get("value") + "\"");

								}*/
								if(m.get("name").startsWith(UIUtil.removeCharecter) && m.get("name").endsWith(UIUtil.removeCharecter)){
									String param1 = m.get("name").replace(UIUtil.removeCharecter, "");
									sbMQL.append(" remove ").append(" setting ").append(UIUtil.singleQuoteWithSpace(param1));
								}else if(!m.get("name").startsWith(UIUtil.removeCharecter) && !m.get("name").endsWith(UIUtil.removeCharecter) && !m.get("value").isEmpty()){
									sbMQL.append(" setting ").append(UIUtil.quoteArgument(m.get("name"))).append(" ").append(UIUtil.quoteArgument(m.get("value")));
								}
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
}
