package com.izn.schemamodeler.ui3.page;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.transform.stream.StreamSource;

import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.izn.schemamodeler.SchemaFactoryInfo;
import com.izn.schemamodeler.SchemaLoader;
import com.izn.schemamodeler.ui3.page.Page;
import com.izn.schemamodeler.ui3.page.PageInfo;
import com.izn.schemamodeler.ui3.page.PageXMLSchema;
import com.izn.schemamodeler.ui3.page.PageXMLSchema.Schema;
import com.izn.schemamodeler.ui3.page.PageXMLSchema.Schema.Basic;
import com.izn.schemamodeler.ui3.page.PageXMLSchema.Schema.Field;
import com.izn.schemamodeler.ui3.page.PageXMLSchema.Schema.Field.Detail;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class PageLoader implements SchemaLoader {
	ObjectMapper _gson = new ObjectMapper();
	Basic _basicElem = null;
	Field _fieldElem = null;

	public PageLoader() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void loadSchema(Context context, String strSchemaName, String fileName, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty)  throws Exception{
		try {
			List<Page> lstPage = new ArrayList<Page>();
			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
			PageInfo pageInfo = (PageInfo) schemaFactory.getSchemaObject("page");
			// String strPageInfo = pageInfo.geSchemaInfo(context, "TestPage");
			// Map mDBInfo = _gson.fromJson(strPageInfo, HashMap.class); this is not
			// required
			JAXBContext jConext = JAXBContext.newInstance(PageXMLSchema.class);
			Unmarshaller unmarshaller = jConext.createUnmarshaller();
			JAXBElement<PageXMLSchema> pageElem = unmarshaller.unmarshal(new StreamSource(new File(fileName)),
					PageXMLSchema.class);
			PageXMLSchema pageXMLSchema = pageElem.getValue();
			List<Schema> lstSchema = pageXMLSchema.getSchema();
			Page page = null;
			String fValue = "";
			String fName = "";

			// To get page folder path
			File file = new File(fileName);
			String sParentPath = file.getParent() + "\\Pages";

			for (Schema _schema : lstSchema) {

				_basicElem = _schema.getBasic();
				page = new Page(_basicElem.getName(), _basicElem.getDescription(), _basicElem.getHidden());
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
					if (fName.equalsIgnoreCase("mime")) {
						page.setMime(fValue);
					} else if (fName.equalsIgnoreCase("content")) {
						// page.setFilepath(_detail.getFilepath());
						if (sParentPath != null && !sParentPath.isEmpty()) {
							page.setFilepath(sParentPath + "\\" + _basicElem.getName());
						}
						page.setContent(fValue);
					}

				}
				lstPage.add(page);
			}

			preparePageMQL(context, lstPage, schema_done_log, scmConfigProperty);
		} catch (UnmarshalException ume) {
			schema_done_log.error("UnmarshalException on ["+strSchemaName+".xml] : "+ume.getCause());
			schema_done_log.warn(UIUtil.sUnmarshalExceptionWarningMessage);
			throw ume;
		} catch (Exception e) {
			schema_done_log.error(""+e.getCause());
			throw e;
		}
		// TODO Auto-generated method stub

	}

	private void preparePageMQL(Context context, List<Page> lstPage, Logger schema_done_log,
			SCMConfigProperty scmConfigProperty) throws Exception{
		String strFileName = "";
		boolean bMQLResult;
		int iCountTotal = 0;
		int iCountSuccess = 0;
		int iCountFailure = 0;
		int iCountAdd = 0;
		int iCountModify = 0;
		int iCountDelete = 0;
		String sMQL = "";
		MQLCommand localMQLCommand = null;
		String strPageName = "";
		String sOperation = "";
		List<String> listModified = new ArrayList<String>();
		List<String> listAdded = new ArrayList<String>();
		List<String> listDeleted = new ArrayList<String>();
		try {
			iCountTotal = lstPage.size(); 
			for (Page page : lstPage) {
				
				StringBuilder sbMQL = new StringBuilder();
				schema_done_log.info("Page : ["
						+ page.name.replace(UIUtil.removeCharecter, "") + "]");
				strPageName = page.name;
				ContextUtil.pushContext(context);
				try {
					String removeSchemaObject = UIUtil.removeSchemaObject(strPageName, "page");
					if (removeSchemaObject.isEmpty()) {
						String strResult = MQLCommand.exec(context, "list page $1",
								new String[] { strPageName.replace(UIUtil.removeCharecter, "") });
						if (com.matrixone.apps.framework.ui.UIUtil.isNotNullAndNotEmpty(strResult)) {
							sMQL = prepareModifyExistingSchemaMQL(page, schema_done_log);
							iCountModify += 1;
							listModified.add(strPageName);
							sOperation = "mod";
						} else {
							sMQL = prepareAddNewSchemaMQL(page, schema_done_log);
							iCountAdd += 1;
							listAdded.add(strPageName);
							sOperation = "add";
						}
					} else {
						sMQL = removeSchemaObject;
						iCountDelete += 1;
						listDeleted.add(strPageName);
					}
					schema_done_log.info("MQL QUERY FOR IMPORT : " + sMQL);
					localMQLCommand = new MQLCommand();
					bMQLResult = localMQLCommand.executeCommand(context, sMQL, true);
					String sMQLError = (String) localMQLCommand.getError();
					schema_done_log.info("MQL QUERY EXECUTION RESULT : " + bMQLResult);
					if (bMQLResult) {
						iCountSuccess += 1;
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
					throw new Exception("Error occurred while importing schema page : ["
							+ strPageName.replace(UIUtil.removeCharecter, "") + "] : " + e.getMessage());
				}
				schema_done_log.info("-----------------------------------------------------------------");
			}
			schema_done_log
					.info("TOTAL  :" + iCountTotal + ", SUCCESS :" + iCountSuccess + ", ADDED  :" + iCountAdd + ", MODIFIED :" + iCountModify + ", DELETED :" + iCountDelete);
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
			Page objInfo = (Page) objectSchemaInfo;
			sbMQL.append(" add page ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null && !objInfo.description.isEmpty()) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.getMime() != null && !objInfo.getMime().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces("mime")).append(UIUtil.quoteArgument(objInfo.getMime()));
			}
			if (objInfo.getFilepath() != null && !objInfo.getFilepath().isEmpty()) {
				sbMQL.append(" file ").append(UIUtil.singleQuoteWithSpace(objInfo.getFilepath()));
			} else {
				sbMQL.append(" content ").append(UIUtil.singleQuoteWithSpace(objInfo.getContent()));
			}
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}

	private String prepareModifyExistingSchemaMQL(Object objectSchemaInfo, Logger schema_done_log) throws Exception {
		StringBuilder sbMQL = new StringBuilder();
		try {
			Page objInfo = (Page) objectSchemaInfo;
			sbMQL.append(" mod page ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(objInfo.name)));
			if (objInfo.description != null) {
				sbMQL.append("description ").append('"').append(objInfo.description).append('"');
			}
			if (objInfo.hidden != null && !objInfo.hidden.isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces(objInfo.hidden));
			}
			if (objInfo.getMime() != null && !objInfo.getMime().isEmpty()) {
				sbMQL.append(UIUtil.padWithSpaces("mime")).append(UIUtil.quoteArgument(objInfo.getMime()));
			}else {
				sbMQL.append(UIUtil.padWithSpaces("mime")).append(UIUtil.singleQuotes(objInfo.getMime()));
			}
			if (objInfo.getFilepath() != null && !objInfo.getFilepath().isEmpty()) {
				sbMQL.append(" file ").append(UIUtil.singleQuoteWithSpace(objInfo.getFilepath()));
			} else {
				sbMQL.append(" content ").append(UIUtil.singleQuoteWithSpace(objInfo.getContent()));
			}
		} catch (Exception e) {
			throw e;
		}
		return sbMQL.toString();
	}
}
