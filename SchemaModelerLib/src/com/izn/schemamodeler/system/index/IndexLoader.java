package com.izn.schemamodeler.system.index;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import org.apache.log4j.Logger;
import com.izn.schemamodeler.SchemaLoader;
import com.izn.schemamodeler.system.index.IndexXMLSchema.Object;
import com.izn.schemamodeler.system.index.IndexXMLSchema.Object.Basic;
import com.izn.schemamodeler.system.index.IndexXMLSchema.Object.Field;
import com.izn.schemamodeler.system.index.IndexXMLSchema.Object.Field.Detail;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class IndexLoader implements SchemaLoader {

	Basic _basicElem = null;
	Field _fieldElem = null;
	Object _objectElem = null;

	@Override
	public void loadSchema(Context context, String strSchemaName, String fileName, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty) throws Exception {
		// TODO Auto-generated method stub
		try {
			List<Index> lstIndex = new ArrayList<Index>();
			JAXBContext jContext = JAXBContext.newInstance(new Class[] { IndexXMLSchema.class });
			Unmarshaller unmarshaller = jContext.createUnmarshaller();
			JAXBElement<IndexXMLSchema> indEle = unmarshaller.unmarshal(new StreamSource(new FileReader(fileName)),
					IndexXMLSchema.class);
			IndexXMLSchema _indexComponent = (IndexXMLSchema) indEle.getValue();
			List<IndexXMLSchema.Object> lsObject = _indexComponent.getObject();
			String fValue = "";
			String fName = "";
			Index index = null;
			List<String> lstIndexItem = null;
			for (Object _object : lsObject) {
				lstIndexItem = new ArrayList<String>();
				_basicElem = _object.getBasic();
				index = new Index(_basicElem.getName(), _basicElem.getDescription(), _basicElem.getHidden(),
						_basicElem.getRegistryName());
				_fieldElem = _object.getField();
				List<Detail> lstDetail = _fieldElem.getDetail();
				for (Detail _detail : lstDetail) {
					fName = _detail.getName();
					fValue = _detail.getValueAttribute();
					if (fValue != null) {
						fValue = _detail.getValueAttribute().trim();
					} else {
						fValue = "";
					}
					if (fName.equalsIgnoreCase("enable")) {
						index.setEnable(fValue.trim());
					}
					if (fName.equalsIgnoreCase("unique")) {
						index.setUnique(fValue.trim());
					}
					if (fName.equalsIgnoreCase("attribute")) {
						index.setAttribute((fValue.trim()));
					}
					if (fName.equalsIgnoreCase("field")) {
						index.setField((fValue.trim()));
					}
				}
				lstIndex.add(index);
			}
			prepareMQLIndex(context, lstIndex, schema_done_log, scmConfigProperty);
		} catch (UnmarshalException ume) {
			schema_done_log.error("UnmarshalException on [" + strSchemaName + ".xml] : " + ume.getCause());
			schema_done_log.warn(UIUtil.sUnmarshalExceptionWarningMessage);
			throw ume;
		} catch (Exception e) {
			schema_done_log.error("" + e.getCause());
			throw e;
		}

	}

	private void prepareMQLIndex(Context context, List<Index> lstIndex, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty) throws Exception {
		{
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
			String strIndexName = "";
			String sOperation = "";
			List<String> listModified = new ArrayList<String>();
			List<String> listAdded = new ArrayList<String>();
			List<String> listDeleted = new ArrayList<String>();
			try {
				Iterator<Index> itrIndex = lstIndex.iterator();
				Index index = null;
				iCountTotal = lstIndex.size();
				while (itrIndex.hasNext()) {
					index = itrIndex.next();
					schema_done_log.info("Index : [" + index.name.replace(UIUtil.removeCharecter, "") + "]");
					ContextUtil.pushContext(context);
					strIndexName = index.name;
					try {
						String removeSchemaObject = UIUtil.removeSchemaObject(strIndexName, "index");
						if (removeSchemaObject.isEmpty()) {
							String strResult = MQLCommand.exec(context, "list index $1",
									new String[] { strIndexName.replace(UIUtil.removeCharecter, "") });
							if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
								sMQL = prepareModifyExistingSchemaMQL(index, schema_done_log);
								iCountModify += 1;
								listModified.add(strIndexName);
								sOperation = "mod";
							} else {
								sMQL = prepareAddNewSchemaMQL(index, schema_done_log);
								iCountAdd += 1;
								listAdded.add(strIndexName);
								sOperation = "add";
							}
							sMQLPropertyQuery = UIUtil.getMQLPropertyQuery(context, "index", index.name,
									index.registryname, sOperation);
						} else {
							sMQL = removeSchemaObject;
							iCountDelete += 1;
							listDeleted.add(strIndexName);
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
						schema_done_log.info("TOTAL :" + iCountTotal + ", SUCCESS :" + iCountSuccess + ", ADDED :"
								+ iCountAdd + ", MODIFIED :" + iCountModify + ", DELETED :" + iCountDelete + ".");
						throw new Exception("Error occurred while importing schema index : ["
								+ strIndexName.replace(UIUtil.removeCharecter, "") + "] : " + e.getMessage());
					}
					schema_done_log.info("-----------------------------------------------------------------");
				}
				schema_done_log.info("TOTAL  :" + iCountTotal + ", SUCCESS :" + iCountSuccess + ", ADDED  :" + iCountAdd
						+ ", MODIFIED :" + iCountModify + ", DELETED :" + iCountDelete + ".");
				if ("true".equalsIgnoreCase(scmConfigProperty.getLogEverything())) {
					schema_done_log.info("ADDED\t\t:" + listAdded.toString());
					schema_done_log.info("MODIFIED\t:" + listModified.toString());
					schema_done_log.info("DELETED\t:" + listDeleted.toString());
				}
			} catch (Exception e) {
				throw e;
			}
		}
	}

	private String prepareAddNewSchemaMQL(Index objectSchemaInfo, Logger schema_done_log) throws Exception {
		StringBuilder sbMQL = new StringBuilder();
		try {
			Index objInfo = (Index) objectSchemaInfo;
			sbMQL.append(" add index ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null && !objInfo.description.isEmpty()) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.getUnique() != null && !objInfo.getUnique().isEmpty()) {
				if(objInfo.getUnique().equals("true")) {				
				sbMQL.append(" unique ");}
				else {
				sbMQL.append(" notunique ");
				}
			}
			
			if(objInfo.getField() != null && !objInfo.getField().isEmpty())
			{
				String sFieldValues = objInfo.getField();
				String[] split = sFieldValues.split(",");
				if (sFieldValues != null && !sFieldValues.isEmpty()) {
					sbMQL.append(UIUtil.padWithSpaces(" field "));
					for (String strField : split) {		
						sbMQL.append(UIUtil.padWithSpaces(strField));
					}
				}
			}
			sbMQL.append(";");

		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}

	private String prepareModifyExistingSchemaMQL(Index objectSchemaInfo, Logger schema_done_log) throws Exception {
		StringBuilder sbMQL = new StringBuilder();
		try {
			Index objInfo = (Index) objectSchemaInfo;
			sbMQL.append(" modify index ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.getUnique() != null && !objInfo.getUnique().isEmpty()) {
				if(objInfo.getUnique().equals("true")) {

				sbMQL.append(" unique ");
				}
				else {
				sbMQL.append(" notunique ");
				}
			}
			if(objInfo.getField() != null && !objInfo.getField().isEmpty())
			{
				String sFieldValues = objInfo.getField();
				String[] split = sFieldValues.split(",");
				if (sFieldValues != null && !sFieldValues.isEmpty()) {
					for (String strField : split) {		
						sbMQL.append(removeFieldDetail(strField, "field"));
					}
				}
			}		
			sbMQL.append(";");

		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}
	
	public  String removeFieldDetail(String param1, String param2)
	{
		StringBuilder sbReturn = new StringBuilder();
		if(!param1.startsWith(UIUtil.removeCharecter) && !param1.endsWith(UIUtil.removeCharecter)){
			sbReturn.append(" add ").append(param2).append(UIUtil.padWithSpaces(UIUtil.quoteArgument(param1)));
		}else if(param1.startsWith(UIUtil.removeCharecter) && param1.endsWith(UIUtil.removeCharecter)){
			param1 = param1.replace(UIUtil.removeCharecter, "");
			sbReturn.append(" remove ").append(param2).append(UIUtil.padWithSpaces(UIUtil.quoteArgument(param1)));
		}
		return sbReturn.toString();
	}
}
