package com.izn.schemamodeler.ui3.page;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.transform.stream.StreamSource;

import com.izn.schemamodeler.SchemaFactoryInfo;
import com.izn.schemamodeler.SchemaCompare;
import com.izn.schemamodeler.ui3.menu.Menu;
import com.izn.schemamodeler.ui3.menu.MenuXMLSchema;
import com.izn.schemamodeler.ui3.page.Page;
import com.izn.schemamodeler.ui3.page.PageInfo;
import com.izn.schemamodeler.ui3.page.PageXMLSchema;
import com.izn.schemamodeler.ui3.page.PageXMLSchema.Schema;
import com.izn.schemamodeler.ui3.page.PageXMLSchema.Schema.Basic;
import com.izn.schemamodeler.ui3.page.PageXMLSchema.Schema.Field;
import com.izn.schemamodeler.ui3.page.PageXMLSchema.Schema.Field.Detail;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.SchemaModelerConstants;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class PageCompare implements SchemaCompare {

	Basic _basicElem = null;
	Field _fieldElem = null;
	
	PageXMLSchema.Schema _schema = null;
	String version = "";
	String compareResultFolder = "";
	String sAdminType = "";
	PageXMLSchema.Schema.Field.Detail _detail = null;


	public PageCompare() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void compareSchema(Context context, String strAdminName, Logger schema_done_log,	SCMConfigProperty scmConfigProperty)  throws Exception{
		try {
			sAdminType = strAdminName;
			version = scmConfigProperty.getVersion();
			compareResultFolder = scmConfigProperty.getCompareFolder() + "\\" + SchemaModelerConstants.FOLDER_NAME_SchemaObject;
			Map<String,Object> list1 = readFromXML(context, scmConfigProperty.getFirstFilePath());
			Map<String,Object> list2 = readFromXML(context, scmConfigProperty.getSecondFilePath());
			Map<String, List> compareSchema_1 = processSchemaCompare(context,list1,list2);
			Map<String, List> compareSchema_2 = processSchemaCompare(context,list2,list1);
		    List<Object> matchList = compareSchema_1.get(SchemaModelerConstants.MATCH);
		    List<Object> deltaList_1 = compareSchema_1.get(SchemaModelerConstants.DELTA);
		    List<Object> deltaList_2 = compareSchema_2.get(SchemaModelerConstants.DELTA);
		    List<Object> uniqueList_1= compareSchema_1.get(SchemaModelerConstants.UNIQUE);
			List<Object> uniqueList_2 = compareSchema_2.get(SchemaModelerConstants.UNIQUE);
			
			schema_done_log.info(sAdminType +"|"+SchemaModelerConstants.MATCH_COUNT+matchList.size()+","+SchemaModelerConstants.DELTA_COUNT1+deltaList_1.size()+","+SchemaModelerConstants.DELTA_COUNT2+deltaList_2.size()+","+SchemaModelerConstants.UNIQUE_COUNT1+uniqueList_1.size()+","+SchemaModelerConstants.UNIQUE_COUNT2+uniqueList_2.size());
			
			writeToXML(context, SchemaModelerConstants.COMPARE_FILENAME+strAdminName+SchemaModelerConstants.COMPARE_MATCH, matchList);
			writeToXML(context, SchemaModelerConstants.COMPARE_FILENAME+strAdminName+SchemaModelerConstants.COMPARE_DELTA1, deltaList_1);
			writeToXML(context, SchemaModelerConstants.COMPARE_FILENAME+strAdminName+SchemaModelerConstants.COMPARE_DELTA2, deltaList_2);
			writeToXML(context, SchemaModelerConstants.COMPARE_FILENAME+strAdminName+SchemaModelerConstants.COMPARE_UNIQUE1, uniqueList_1);
			writeToXML(context, SchemaModelerConstants.COMPARE_FILENAME+strAdminName+SchemaModelerConstants.COMPARE_UNIQUE2, uniqueList_2);
			
		} catch (UnmarshalException ume) {
			schema_done_log.error("UnmarshalException on ["+strAdminName+".xml] : "+ume.getCause());
			schema_done_log.warn(UIUtil.sUnmarshalExceptionWarningMessage);
			throw ume;
		} catch (Exception e) {
			throw e;
		}
	}
	
	private Map<String,List> processSchemaCompare(Context context, Map<String,Object> list1, Map<String,Object> list2)  throws Exception{
		Map<String,List> mCompareResultInfo = new HashMap<String,List>();
		try {
			List<Object> matchList = new ArrayList<Object>();
			List<Object> deltaList = new ArrayList<Object>();
			List<Object> uniqueList = new ArrayList<Object>();
			String sSchemaName = "";
			Page obj1 = null;
			Page obj2 = null;
			List<Object> listSchemaList1 = null;
			List<Object> listSchemaList2 = null;
			for (Map.Entry<String,Object> entry : list1.entrySet())  
			{
				sSchemaName = entry.getKey();
				listSchemaList1 = (ArrayList)entry.getValue();
				obj1 = (Page)listSchemaList1.get(0);
				if(list2.containsKey(sSchemaName))
				{
					listSchemaList2 = (ArrayList)list2.get(sSchemaName);
					obj2 = (Page)listSchemaList2.get(0);
				
					if(UIUtil.isEqual(obj1.name,obj2.name) && UIUtil.isEqual(obj1.description,obj2.description) && UIUtil.isEqual(obj1.hidden,obj2.hidden) && UIUtil.isEqual(obj1.getContent(),obj2.getContent()) && UIUtil.isEqual(obj1.getMime(),obj2.getMime()) && UIUtil.compareFile(obj1.getFilepath(),obj2.getFilepath())) {
						matchList.add(listSchemaList1);		
					}
					else {
						deltaList.add(listSchemaList1);	
					}
				} else {
					uniqueList.add(listSchemaList1);
				}
			}
			mCompareResultInfo.put(SchemaModelerConstants.MATCH, matchList);
			mCompareResultInfo.put(SchemaModelerConstants.DELTA, deltaList);
			mCompareResultInfo.put(SchemaModelerConstants.UNIQUE, uniqueList);
		}catch (Exception e) {
			throw e;
		}
		return mCompareResultInfo;
	}
	
	private Map<String,Object> readFromXML(Context context, String fileName)  throws Exception{
		List<Object> listSchema = null;
		Map<String,Object> mSchemaInfo = new HashMap<String,Object>();
		try {
			JAXBContext jConext = JAXBContext.newInstance(PageXMLSchema.class);
			Unmarshaller unmarshaller = jConext.createUnmarshaller();
			JAXBElement<PageXMLSchema> pageElem = unmarshaller.unmarshal(new StreamSource(new File(fileName)),PageXMLSchema.class);
			PageXMLSchema pageXMLSchema = pageElem.getValue();
			List<Schema> lstSchema = pageXMLSchema.getSchema();
			Page page = null;
			String fValue = "";
			String fName = "";

			// To get page folder path
			File file = new File(fileName);
			String sParentPath = file.getParent() + "\\Pages";

			for (Schema _schema : lstSchema) {
				listSchema = new ArrayList<Object>();

				_basicElem = _schema.getBasic();
				String pageName=_basicElem.getName();
				page = new Page(_basicElem.getName(), _basicElem.getDescription(), _basicElem.getHidden());
				page.hidden = this._basicElem.getHidden();
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
				listSchema.add(page);
				listSchema.add(_schema);
				mSchemaInfo.put(page.name, listSchema);
			}
		} catch (UnmarshalException ume) {
			throw ume;
		} catch (Exception e) {
			throw e;
		}
		return mSchemaInfo;
	}	
	
	private void writeToXML(Context context, String compareFileName, List<Object> list) throws Exception{
		ObjectFactory objectFactory=new ObjectFactory();
		try {	
			PageXMLSchema txc = objectFactory.createPageXMLSchema();
			txc.setName("page");
			txc.setType("admin");
			txc.setVersion(version);
			txc.schema = new ArrayList<PageXMLSchema.Schema>();
			File file = null;
			JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { PageXMLSchema.class });
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty("jaxb.formatted.output", Boolean.valueOf(true));
			int iSize = list.size();
			List<Object> listSchemaList = null;
			for (int iCount = 0; iCount < iSize; iCount++) 
			{	
				listSchemaList = (ArrayList)list.get(iCount);				
				_schema = (PageXMLSchema.Schema)listSchemaList.get(1);
				txc.schema.add(_schema);
			}
			file = new File(compareResultFolder + "\\" + sAdminType);
			if(!(file.exists() && file.isDirectory())){
				file.mkdir();
			} 
			file = new File(file.getPath() + "\\" + compareFileName + SchemaModelerConstants.XML_EXTENSION);
			jaxbMarshaller.marshal(txc, file);		
		} catch (Exception e) {
			throw e;
		}
	}
}

