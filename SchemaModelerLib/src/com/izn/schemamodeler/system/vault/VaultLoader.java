package com.izn.schemamodeler.system.vault;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import org.apache.log4j.Logger;
import com.izn.schemamodeler.SchemaFactoryInfo;
import com.izn.schemamodeler.SchemaLoader;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

import com.izn.schemamodeler.system.vault.VaultXMLSchema;
import com.izn.schemamodeler.system.vault.VaultXMLSchema.Object;
import com.izn.schemamodeler.system.vault.VaultXMLSchema.Object.Basic;
import com.izn.schemamodeler.system.vault.VaultXMLSchema.Object.Field;
import com.izn.schemamodeler.system.vault.VaultXMLSchema.Object.Field.Detail;

public class VaultLoader implements SchemaLoader {

	Basic _basicElem = null;
	Field _fieldElem = null;	
	Object _objectElem = null;

	public VaultLoader() {

	}

	@Override
	public void loadSchema(Context context, String strSchemaName, String fileName, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty) throws Exception {
		// TODO Auto-generated method stub
		try {
			
			List<Vault> lstVault = new ArrayList<Vault>();
			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
			JAXBContext jContext = JAXBContext.newInstance(new Class[] { VaultXMLSchema.class });
			Unmarshaller unmarshaller = jContext.createUnmarshaller();
			JAXBElement<VaultXMLSchema> valEle = unmarshaller.unmarshal(new StreamSource(new FileReader(fileName)),
					VaultXMLSchema.class);
			VaultXMLSchema _vaultComponent = (VaultXMLSchema) valEle.getValue();
			List<Object> lsObject = _vaultComponent.getObject();
			String fValue = "";
			String fName = "";
			Vault vault = null;	
			List<String> lstVaultItem = null;
			for (Object _object : lsObject) {
				lstVaultItem =  new ArrayList<String>();
				_basicElem = _object.getBasic();
				 vault = new Vault(_basicElem.getName(), _basicElem.getDescription(), _basicElem.getHidden(),
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
					if (fName.equalsIgnoreCase("tablespace")) {
						vault.setTablespace(fValue.trim());
					}
					if (fName.equalsIgnoreCase("indexspace")) {
						vault.setIndexspace(fValue.trim());
					}
					if (fName.equalsIgnoreCase("status")) {
						vault.setStatus(fValue.trim());
					}
				}
				lstVault.add(vault);
			}
			prepareMQLVault(context, lstVault, schema_done_log,scmConfigProperty);
		} catch (UnmarshalException ume) {
			schema_done_log.error("UnmarshalException on ["+strSchemaName+".xml] : "+ume.getCause());
			schema_done_log.warn(UIUtil.sUnmarshalExceptionWarningMessage);
			throw ume;
		} catch (Exception e) {
			schema_done_log.error(""+e.getCause());
			throw e;
		}
	}

	private void prepareMQLVault(Context context, List<Vault> lstVault, Logger schema_done_log,SCMConfigProperty scmConfigProperty) throws Exception {
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
		String strVaultName = "";
		String sOperation = "";
		List<String> listModified = new ArrayList<String>();
		List<String> listAdded = new ArrayList<String>();
		List<String> listDeleted = new ArrayList<String>();
		try {
			Iterator<Vault> itrVault = lstVault.iterator();
			Vault vault = null;
			iCountTotal = lstVault.size();
			while (itrVault.hasNext()) {		
				vault = itrVault.next();
				schema_done_log.info("Vault : ["
						+ vault.name.replace(UIUtil.removeCharecter, "") + "]");
				ContextUtil.pushContext(context);
				strVaultName = vault.name;
				try {
					String removeSchemaObject = UIUtil.removeSchemaObject(strVaultName, "vault");
					if (removeSchemaObject.isEmpty()) {
						String strResult = MQLCommand.exec(context, "list vault $1",
								new String[] { strVaultName.replace(UIUtil.removeCharecter, "") });
						if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
							sMQL = prepareModifyExistingSchemaMQL(vault, schema_done_log);
							iCountModify += 1;
							listModified.add(strVaultName);
							sOperation = "mod";
						} else {
							sMQL = prepareAddNewSchemaMQL(vault, schema_done_log);
							iCountAdd += 1;
							listAdded.add(strVaultName);
							sOperation = "add";
						}
						sMQLPropertyQuery = UIUtil.getMQLPropertyQuery(context, "vault", vault.name, vault.registryname,
								sOperation);
					} else {
						sMQL = removeSchemaObject;
						iCountDelete += 1;
						listDeleted.add(strVaultName);
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
					throw new Exception("Error occurred while importing schema vault : ["
							+ strVaultName.replace(UIUtil.removeCharecter, "") + "] : " + e.getMessage());
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

	private String prepareAddNewSchemaMQL(Vault objectSchemaInfo, Logger schema_done_log) throws Exception {
		StringBuilder sbMQL = new StringBuilder();
		try {
			Vault objInfo = (Vault)objectSchemaInfo;
			sbMQL.append(" add vault ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null && !objInfo.description.isEmpty()) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.getIndexspace() != null && !objInfo.getIndexspace().isEmpty()) {
				sbMQL.append("indexspace ").append('"').append(objInfo.getIndexspace()).append('"');
			}
			if (objInfo.getTablespace() != null && !objInfo.getTablespace().isEmpty()) {
				sbMQL.append(" tablespace ").append('"').append(objInfo.getTablespace()).append('"');
			}			
			sbMQL.append(";");
			
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}

	private String prepareModifyExistingSchemaMQL(Vault objectSchemaInfo, Logger schema_done_log) throws Exception {
		StringBuilder sbMQL = new StringBuilder();
		try {
			Vault objInfo = (Vault) objectSchemaInfo;
			sbMQL.append(" modify vault ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.getIndexspace() != null && !objInfo.getIndexspace().isEmpty()) {
				sbMQL.append("indexspace ").append('"').append(objInfo.getIndexspace()).append('"');
			}
			if (objInfo.getTablespace() != null && !objInfo.getTablespace().isEmpty()) {
				sbMQL.append(" tablespace ").append('"').append(objInfo.getTablespace()).append('"');
			}
			sbMQL.append(";");
			
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}

	

}

