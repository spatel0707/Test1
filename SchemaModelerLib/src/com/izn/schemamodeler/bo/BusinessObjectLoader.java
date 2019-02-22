package com.izn.schemamodeler.bo;

import java.io.FileReader;
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
import com.izn.schemamodeler.SchemaFactoryInfo;
import com.izn.schemamodeler.SchemaLoader;
import com.izn.schemamodeler.bo.BusinessObjectXMLSchema.Object.AttributeInfo.AttrDetail;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.izn.schemamodeler.util.UIUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MqlUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class BusinessObjectLoader implements SchemaLoader {

	BusinessObjectXMLSchema.Object.Basic _basicElem = null;
	BusinessObjectXMLSchema.Object _ObjectEle = null;
	BusinessObjectXMLSchema.Object.Basic.Field _fieldElem = null;
	BusinessObjectXMLSchema.Object.Basic.Field.Detail _detailElem = null;
	BusinessObjectXMLSchema.Object.AttributeInfo _attrInfo = null;
	BusinessObjectXMLSchema.Object.AttributeInfo.AttrDetail _attrDetails = null;

	public BusinessObjectLoader() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void loadSchema(Context context, String strSchemaName, String fileName, Logger schema_done_log,SCMConfigProperty scmConfigProperty)  throws Exception{

		try {
			List<BusinessObject> lstNg = new ArrayList<BusinessObject>();
			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
			BusinessobjectInfo businessInfo = (BusinessobjectInfo) schemaFactory.getSchemaObject("businessobject");
			JAXBContext jContext = JAXBContext.newInstance(new Class[] { BusinessObjectXMLSchema.class });
			Unmarshaller unMarsheller = jContext.createUnmarshaller();
			JAXBElement<BusinessObjectXMLSchema> busEle = unMarsheller
					.unmarshal(new StreamSource(new FileReader(fileName)), BusinessObjectXMLSchema.class);
			BusinessObjectXMLSchema _busComponent = (BusinessObjectXMLSchema) busEle.getValue();
			List<BusinessObjectXMLSchema.Object> lsObject = _busComponent.getObject();
			Iterator<BusinessObjectXMLSchema.Object> itsObject = lsObject.iterator();
			String fValue = "";
			String fName = "";
			BusinessObject bus = null;
			Map mAttributes = null;
			List<Map<String, String>> lstMap = new ArrayList();
			while (itsObject.hasNext()) 
			{
				this._ObjectEle = ((BusinessObjectXMLSchema.Object) itsObject.next());
				this._basicElem = this._ObjectEle.getBasic();
				bus = new BusinessObject(_basicElem.getType(), _basicElem.getName(), _basicElem.getRevision());
				_fieldElem = this._basicElem.getField();
				List<BusinessObjectXMLSchema.Object.Basic.Field.Detail> lsDetails = this._fieldElem.getDetail();
				Iterator<BusinessObjectXMLSchema.Object.Basic.Field.Detail> itrDetails = lsDetails.iterator();
				while (itrDetails.hasNext()) {
					this._detailElem = ((BusinessObjectXMLSchema.Object.Basic.Field.Detail) itrDetails.next());
					fName = this._detailElem.getName();
					if(this._detailElem.getValueAttribute() != null){
						fValue = this._detailElem.getValueAttribute().trim();
					}else{
						fValue = "";
					}
					if (fName.equalsIgnoreCase("POLICY")) {
						bus.setPolicy(fValue);
					} else if (fName.equalsIgnoreCase("VAULT")) {
						bus.setVault(fValue);
					}
					else if (fName.equalsIgnoreCase("DESCRIPTION")) {
						bus.setDescription(fValue);
					} else if (fName.equalsIgnoreCase("OWNER")) {
						bus.setOwner(fValue);
					}
				}
				this._attrInfo = this._ObjectEle.getAttributes();
				List<BusinessObjectXMLSchema.Object.AttributeInfo.AttrDetail> lstAttribDetails = this._attrInfo.getAttrdetail();
				if(lstAttribDetails != null)
				{
					for (AttrDetail attrdetail : lstAttribDetails) {
						mAttributes = new HashMap();
						mAttributes.put("name", attrdetail.getName());
						mAttributes.put("value", attrdetail.getValueAttribute() != null ? attrdetail.getValueAttribute().trim() : "");
						lstMap.add(mAttributes);
					}					
				}
				bus.setLstAttribute(lstMap);
				lstNg.add(bus);
			}
			prepareNGMQL(context, lstNg, schema_done_log);
		} catch (UnmarshalException ume) {
			schema_done_log.error("UnmarshalException on [BusinessObject] : "+ume.getCause());
			schema_done_log.warn(UIUtil.sUnmarshalExceptionWarningMessage);
			throw ume;
		} catch (Exception e) {
			schema_done_log.error(""+e.getCause());
			throw e;
		}
	}

	private void prepareNGMQL(Context context, List<BusinessObject> lstNg, Logger schema_done_log) throws Exception{
		StringBuilder sbMQL = null;
		int iCountTotal = 0;
		int iCountSuccess = 0;
		int iCountFailure = 0;
		int iCountAdd = 0;
		int iCountModify = 0;
		int iCountDelete = 0;
		MQLCommand localMQLCommand = null;
		boolean bMQLResult = false;
		try {
			iCountTotal = lstNg.size();
			for (BusinessObject ng : lstNg)
			{
				
				sbMQL = new StringBuilder();
				String strMQL = "print bus '" + ng.type + "' '" + ng.name + "' '" + ng.revision	+ "' select exists dump";
				String strResult = MqlUtil.mqlCommand(context, strMQL);
				if (strResult.trim().equalsIgnoreCase("TRUE")) {
					iCountModify += 1;
					sbMQL.append(" modify bus ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(ng.type)));
				} else {
					iCountAdd += 1;
					sbMQL.append(" add bus ").append(UIUtil.padWithSpaces(UIUtil.quoteArgument(ng.type)));
				}
				sbMQL.append(UIUtil.padWithSpaces(UIUtil.quoteArgument(ng.name)));
				sbMQL.append(UIUtil.singleQuoteWithSpace(ng.revision));
				sbMQL.append(" description ").append(UIUtil.singleQuoteWithSpace(ng.getDescription()));
				sbMQL.append(" policy ").append(UIUtil.singleQuoteWithSpace(ng.getPolicy()));
				sbMQL.append(" vault ").append(UIUtil.singleQuoteWithSpace(ng.getVault()));
				sbMQL.append(" owner ").append(UIUtil.singleQuoteWithSpace(ng.getOwner()));

				List<Map<String, String>> lstAttribute = ng.getLstAttribute();
				if(lstAttribute != null && !lstAttribute.isEmpty())
				{					
					for (Map m : lstAttribute) {
						if (!(((String) m.get("value")).isEmpty()) && ((String) m.get("value") != null))
							sbMQL.append(UIUtil.padWithSpaces(UIUtil.quoteArgument((String) m.get("name"))))
							.append(UIUtil.quoteArgument((String) m.get("value")));
					}
				}
				sbMQL.append(";");
				schema_done_log.info("MQL QUERY FOR IMPORT : "+sbMQL.toString());
				try {
					ContextUtil.pushContext(context);
					localMQLCommand = new MQLCommand();
					bMQLResult = localMQLCommand.executeCommand(context, sbMQL.toString(), true);
					schema_done_log.info("MQL QUERY EXECUTION RESULT : "+bMQLResult);
					if(bMQLResult){
						iCountSuccess += 1;	
					}else{
						iCountFailure += 1;
						if(sbMQL.toString().trim().toLowerCase().startsWith("add"))
							iCountAdd -= 1; 
						else if(sbMQL.toString().trim().toLowerCase().startsWith("mod"))
							iCountModify -= 1;
						String sMQLError = (String)localMQLCommand.getError();
						throw new MatrixException(sMQLError);
					}
					ContextUtil.popContext(context);
				} catch (Exception e) {
					schema_done_log.info("TOTAL :" + iCountTotal + ", SUCCESS :" + iCountSuccess + ", ADDED :" + iCountAdd + ", MODIFIED :" + iCountModify + ", DELETED :"	+ iCountDelete + ".");
					throw new Exception("Error occurred while import businessobject : ["+ng.type+"] ["+ng.name+"] ["+ng.revision+"] : " +e.getMessage());
				}
			}
			schema_done_log.info("TOTAL  :" + iCountTotal + ", SUCCESS :" + iCountSuccess  + ", ADDED  :" + iCountAdd + ", MODIFIED :" + iCountModify);
		} catch (Exception e) {
			throw e;
			
		}
	}

}
