package com.izn.schemamodeler.system.store;

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
import com.izn.schemamodeler.SchemaFactoryInfo;
import com.izn.schemamodeler.SchemaLoader;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.izn.schemamodeler.system.store.StoreXMLSchema.Object;
import com.izn.schemamodeler.system.store.StoreXMLSchema.Object.Basic;
import com.izn.schemamodeler.system.store.StoreXMLSchema.Object.Field;
import com.izn.schemamodeler.system.store.StoreXMLSchema.Object.Field.Detail;
import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class StoreLoader implements SchemaLoader {

	Basic _basicElem = null;
	Field _fieldElem = null;
	Object _objectElem = null;

	@Override
	public void loadSchema(Context context, String strSchemaName, String fileName, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty) throws Exception {
		try {
			List<Store> lstStore = new ArrayList<Store>();
			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
			JAXBContext jContext = JAXBContext.newInstance(new Class[] { StoreXMLSchema.class });
			Unmarshaller unmarshaller = jContext.createUnmarshaller();
			JAXBElement<StoreXMLSchema> storeEle = unmarshaller.unmarshal(new StreamSource(new FileReader(fileName)),
					StoreXMLSchema.class);
			StoreXMLSchema _storeComponent = (StoreXMLSchema) storeEle.getValue();
			List<StoreXMLSchema.Object> lsObject = _storeComponent.getObject();
			String fValue = "";
			String fName = "";
			Store store = null;
			List<String> lstStoreItem = null;
			for (Object _object : lsObject) {
				lstStoreItem = new ArrayList<String>();
				_basicElem = _object.getBasic();
				store = new Store(_basicElem.getName(), _basicElem.getDescription(), _basicElem.getHidden(),
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
					if (fName.equalsIgnoreCase("type")) {
						store.setType(fValue.trim());
					}
					if (fName.equalsIgnoreCase("permission")) {
						store.setPermission(fValue.trim());
					}
					if (fName.equalsIgnoreCase("protocol")) {
						store.setProtocol(fValue.trim());
					}
					if (fName.equalsIgnoreCase("port")) {
						store.setPort(fValue.trim());
					}
					if (fName.equalsIgnoreCase("host")) {
						store.setHost(fValue.trim());
					}
					if (fName.equalsIgnoreCase("Path")) {
						store.setPath(fValue.trim());
					}
					if (fName.equalsIgnoreCase("user")) {
						store.setUser(fValue.trim());
					}
					if (fName.equalsIgnoreCase("password")) {
						store.setPassword(fValue.trim());
					}
					if (fName.equalsIgnoreCase("fcs")) {
						store.setFcs(fValue.trim());
					}
					if (fName.equalsIgnoreCase("lock")) {
						store.setLock(fValue.trim());
					}

				}
				lstStore.add(store);
			}
			prepareMQLStore(context, lstStore, schema_done_log, scmConfigProperty);
		} catch (UnmarshalException ume) {
			schema_done_log.error("UnmarshalException on [" + strSchemaName + ".xml] : " + ume.getCause());
			schema_done_log.warn(UIUtil.sUnmarshalExceptionWarningMessage);
			throw ume;
		} catch (Exception e) {
			schema_done_log.error("" + e.getCause());
			throw e;
		}

	}

	private void prepareMQLStore(Context context, List<Store> lstStore, Logger schema_done_log,
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
			String strStoreName = "";
			String sOperation = "";
			List<String> listModified = new ArrayList<String>();
			List<String> listAdded = new ArrayList<String>();
			List<String> listDeleted = new ArrayList<String>();
			try {
				Iterator<Store> itrStore = lstStore.iterator();
				Store store = null;
				iCountTotal = lstStore.size();
				while (itrStore.hasNext()) {
					store = itrStore.next();
					schema_done_log.info("Store : [" + store.name.replace(UIUtil.removeCharecter, "") + "]");
					ContextUtil.pushContext(context);
					strStoreName = store.name;
					try {
						String removeSchemaObject = UIUtil.removeSchemaObject(strStoreName, "store");
						if (removeSchemaObject.isEmpty()) {
							String strResult = MQLCommand.exec(context, "list store $1",
									new String[] { strStoreName.replace(UIUtil.removeCharecter, "") });
							if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
								sMQL = prepareModifyExistingSchemaMQL(store, schema_done_log);
								iCountModify += 1;
								listModified.add(strStoreName);
								sOperation = "mod";
							} else {
								sMQL = prepareAddNewSchemaMQL(store, schema_done_log);
								iCountAdd += 1;
								listAdded.add(strStoreName);
								sOperation = "add";
							}
							sMQLPropertyQuery = UIUtil.getMQLPropertyQuery(context, "store", store.name,
									store.registryname, sOperation);
						} else {
							sMQL = removeSchemaObject;
							iCountDelete += 1;
							listDeleted.add(strStoreName);
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
						throw new Exception("Error occurred while importing schema store : ["
								+ strStoreName.replace(UIUtil.removeCharecter, "") + "] : " + e.getMessage());
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

	private String prepareAddNewSchemaMQL(Store objectSchemaInfo, Logger schema_done_log) throws Exception {
		StringBuilder sbMQL = new StringBuilder();
		try {
			Store objInfo = (Store) objectSchemaInfo;
			sbMQL.append(" add store ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null && !objInfo.description.isEmpty()) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.type != null && !objInfo.description.isEmpty()) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.getType() != null && !objInfo.getType().isEmpty()) {
				sbMQL.append(" type ").append('"').append(objInfo.getType()).append('"');
			}
			if (objInfo.getPermission() != null && !objInfo.getPermission().isEmpty()) {
				sbMQL.append(" permission ").append(objInfo.getPermission());
			}
			if (objInfo.getProtocol() != null && !objInfo.getProtocol().isEmpty()) {
				sbMQL.append(" protocol ").append('"').append(objInfo.getProtocol()).append('"');
			}
			if (objInfo.getPort() != null && !objInfo.getPort().isEmpty()) {
				sbMQL.append(" port ").append('"').append(objInfo.getPort()).append('"');
			}
			if (objInfo.getHost() != null && !objInfo.getHost().isEmpty()) {
				sbMQL.append(" host ").append('"').append(objInfo.getHost()).append('"');
			}
			if (objInfo.getPath() != null && !objInfo.getPath().isEmpty()) {
				sbMQL.append(" Path ").append('"').append(objInfo.getPath()).append('"');
			}
			if (objInfo.getUser() != null && !objInfo.getUser().isEmpty()) {
				sbMQL.append(" user ").append('"').append(objInfo.getUser()).append('"');
			}
			if (objInfo.getPassword() != null && !objInfo.getPassword().isEmpty()) {
				sbMQL.append(" password ").append('"').append(objInfo.getPassword()).append('"');
			}
			if (objInfo.getFcs() != null && !objInfo.getFcs().isEmpty()) {
				sbMQL.append(" fcs ").append('"').append(objInfo.getFcs()).append('"');
			}
			if (objInfo.getLock() != null && !objInfo.getLock().isEmpty()) {
				if(objInfo.getLock().equals("false")){ 
				sbMQL.append(" lock ");
				}
				else {
					sbMQL.append(" unlock ");
				}
			}
			sbMQL.append(";");
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}
	private String prepareModifyExistingSchemaMQL(Store objectSchemaInfo, Logger schema_done_log) throws Exception {
		StringBuilder sbMQL = new StringBuilder();
		try {
			Store objInfo = (Store) objectSchemaInfo;
			sbMQL.append(" mod store ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null && !objInfo.description.isEmpty()) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.type != null && !objInfo.description.isEmpty()) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.getType() != null && objInfo.getType().isEmpty()) {
				sbMQL.append("type ").append('"').append(objInfo.getType()).append('"');
			}
			if (objInfo.getPermission() != null && objInfo.getPermission().isEmpty()) {
				sbMQL.append("permission ").append('"').append(objInfo.getPermission()).append('"');
			}
			if (objInfo.getProtocol() != null && objInfo.getProtocol().isEmpty()) {
				sbMQL.append("protocol ").append('"').append(objInfo.getProtocol()).append('"');
			}
			if (objInfo.getPort() != null && objInfo.getPort().isEmpty()) {
				sbMQL.append("port ").append('"').append(objInfo.getPort()).append('"');
			}
			if (objInfo.getHost() != null && objInfo.getHost().isEmpty()) {
				sbMQL.append("host ").append('"').append(objInfo.getHost()).append('"');
			}
			if (objInfo.getPath() != null && objInfo.getPath().isEmpty()) {
				sbMQL.append("Path ").append('"').append(objInfo.getPath()).append('"');
			}
			if (objInfo.getUser() != null && objInfo.getUser().isEmpty()) {
				sbMQL.append("user ").append('"').append(objInfo.getUser()).append('"');
			}
			if (objInfo.getPassword() != null && objInfo.getPassword().isEmpty()) {
				sbMQL.append("password ").append('"').append(objInfo.getPassword()).append('"');
			}
			if (objInfo.getFcs() != null && objInfo.getFcs().isEmpty()) {
				sbMQL.append("fcs ").append('"').append(objInfo.getFcs()).append('"');
			}
			if (objInfo.getLock() != null && objInfo.getLock().isEmpty()) {
				sbMQL.append("lock ").append('"').append(objInfo.getLock()).append('"');
			}

			sbMQL.append(";");
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}
}
