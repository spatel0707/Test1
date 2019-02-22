package com.izn.schemamodeler.connection;

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
import com.izn.schemamodeler.connection.Connection;
import com.izn.schemamodeler.connection.ConnectionInfo;
import com.izn.schemamodeler.connection.ConnectionXMLSchema;
import com.izn.schemamodeler.connection.ConnectionXMLSchema.Relationship;
import com.izn.schemamodeler.connection.ConnectionXMLSchema.Relationship.AttributeInfo;
import com.izn.schemamodeler.connection.ConnectionXMLSchema.Relationship.RelationshipInfo;
import com.izn.schemamodeler.connection.ConnectionXMLSchema.Relationship.AttributeInfo.AttrDetail;
import com.izn.schemamodeler.connection.ConnectionXMLSchema.Relationship.RelationshipInfo.RelDetail;
import com.izn.schemamodeler.connection.ConnectionXMLSchema.Relationship.Basic;
import com.izn.schemamodeler.connection.ConnectionXMLSchema.Relationship.Basic.Detail;
import com.izn.schemamodeler.util.UIUtil;
import com.izn.schemamodeler.util.SCMConfigProperty;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.StringList;

public class ConnectionLoader implements SchemaLoader {

	ObjectMapper _gson = new ObjectMapper();
		Basic _basicElem=null;
		Detail  _detailElem=null;
		AttributeInfo  _attr =null;
		ConnectionXMLSchema _component = null;
		public ConnectionLoader() {
			// TODO Auto-generated constructor stub
		}

		@Override
		public void loadSchema(Context context, String strSchemaName, String fileName, Logger schema_done_log,SCMConfigProperty scmConfigProperty)  throws Exception{
		try{
			List<Connection> lstConn= new ArrayList<Connection>();
			SchemaFactoryInfo schemaFactory = new SchemaFactoryInfo();
			ConnectionInfo connectionInfo = (ConnectionInfo) schemaFactory.getSchemaObject(strSchemaName);
			//String strProgramInfo = connectionInfo.geSchemaInfo(context, "eService Object Generator|type_PMSEPCG|");
			//Map mDBInfo = _gson.fromJson(strProgramInfo, HashMap.class);
	 	    JAXBContext jConext = JAXBContext.newInstance(ConnectionXMLSchema.class);
		    Unmarshaller  unmarshaller =  jConext.createUnmarshaller();
		    //fileName = UIUtil.quoteArgument(fileName);
		    JAXBElement<ConnectionXMLSchema> ngElem = unmarshaller.unmarshal((new StreamSource((new File(fileName)))),ConnectionXMLSchema.class);
			ConnectionXMLSchema  ngXMLSchema = ngElem.getValue();
			List<Relationship> lstRel = ngXMLSchema.getRelationship();
		    String fValue="";
		    String fName="";
		    List<Map<String, String>> lstMap=null;
			List<Map<String, String>> lstRelAttrMap=null;
		    Connection connection=null;
		    Map m =null;
				for(Relationship _objRel:lstRel){
					lstMap=new ArrayList<Map<String, String>>();
				 	lstRelAttrMap=new ArrayList<Map<String, String>>();
					_basicElem = _objRel.getBasic();
					connection = new Connection(_basicElem.getType());
					List<Detail> lstDetail = _basicElem.getDetail();
					for(Detail detail: lstDetail){
						fName=detail.getName();
						if(detail.getValueAttribute() != null){
							fValue=detail.getValueAttribute().trim();
						}else{
							fValue="";
						}
						if(fName.equalsIgnoreCase("from_type")){
							connection.setFromType(fValue);
				 	   	}else if(fName.equalsIgnoreCase("from_name")){
				 	   		connection.setFromName(fValue);
				 	   	}else if(fName.equalsIgnoreCase("from_revision")){
				 	   		connection.setFromRevision(fValue);
				 	   	}else if(fName.equalsIgnoreCase("to_type")){
					 		connection.setToType(fValue);
				 	   	}else if(fName.equalsIgnoreCase("to_name")){
					 	 	connection.setToName(fValue);
				 	   	}else if(fName.equalsIgnoreCase("to_revision")){
				 	   		connection.setToRevision(fValue);
				 	   	}
					}
					//Attribute info
					com.izn.schemamodeler.connection.ConnectionXMLSchema.Relationship.AttributeInfo aifo =  _objRel.getAttributes();
				 	List<ConnectionXMLSchema.Relationship.AttributeInfo.AttrDetail> lst = aifo.getAttrdetail();
				 	if(lst != null){
				 		for(AttrDetail attrdetail:lst){
				 			m=new HashMap();
				 			m.put("name", attrdetail.getName());
				 			m.put("value", attrdetail.getValueAttribute() != null ? attrdetail.getValueAttribute().trim() : "");
				 			lstMap.add(m);
				 		}
				 		connection.setLstAttribute(lstMap);
				 	}

				 	//Re to Rel info-----------STARTS
					List<RelationshipInfo> relListInfo =  _objRel.getRelationships();
				 	if(relListInfo != null){
				 		Connection relConnection = null;
				 		List<Connection> listRelConnection = new ArrayList<Connection>();
				 		for(RelationshipInfo relInfo : relListInfo){
				 			List<RelDetail> relListDetail = relInfo.getAttrdetail();
				 			relConnection = new Connection();
							for(RelDetail relDetail: relListDetail){
								fName=relDetail.getName();
								if(relDetail.getValueAttribute() != null){
									fValue=relDetail.getValueAttribute().trim();
								}else{
									fValue="";
								}
								if(fName.equalsIgnoreCase("tomid")){
									relConnection.setTomid(fValue);
						 	   	}else if(fName.equalsIgnoreCase("frommid")){
									relConnection.setFrommid(fValue);
						 	   	}else if(fName.equalsIgnoreCase("from_type")){
									relConnection.setFromType(fValue);
						 	   	}else if(fName.equalsIgnoreCase("from_name")){
						 	   		relConnection.setFromName(fValue);
						 	   	}else if(fName.equalsIgnoreCase("from_revision")){
						 	   		relConnection.setFromRevision(fValue);
						 	   	}else if(fName.equalsIgnoreCase("to_type")){
						 	   		relConnection.setToType(fValue);
						 	   	}else if(fName.equalsIgnoreCase("to_name")){
						 	   		relConnection.setToName(fValue);
						 	   	}else if(fName.equalsIgnoreCase("to_revision")){
						 	   		relConnection.setToRevision(fValue);
						 	   	}
							}
							List<com.izn.schemamodeler.connection.ConnectionXMLSchema.Relationship.RelationshipInfo.AttrDetail> relAttributeListDetail = relInfo.getRelAttrdetail();
							for(com.izn.schemamodeler.connection.ConnectionXMLSchema.Relationship.RelationshipInfo.AttrDetail relAttrDetail: relAttributeListDetail){
					 			m=new HashMap();
					 			m.put("name", relAttrDetail.getName());
					 			m.put("value", relAttrDetail.getValueAttribute() != null ? relAttrDetail.getValueAttribute().trim() : "");
					 			lstRelAttrMap.add(m);
							}
							relConnection.setLstAttribute(lstRelAttrMap);
							listRelConnection.add(relConnection);
				 		}
				 		connection.setLstRelationship(listRelConnection);
				 	}
				 	//Re to Rel info-----------ENDS
					lstConn.add(connection);
				}
		 		prepareProgramMQL(context,lstConn,ngXMLSchema,schema_done_log);
			}catch (UnmarshalException ume) {
				schema_done_log.error("UnmarshalException on [Connection] : "+ume.getCause());
				schema_done_log.warn(UIUtil.sUnmarshalExceptionWarningMessage);
				throw ume;
			} catch (Exception e) {
				schema_done_log.error(""+e.getCause());
				throw e;
			}
		}

		private void prepareProgramMQL(Context context,List<Connection> lstConn, ConnectionXMLSchema ngXMLSchema, Logger schema_done_log) throws Exception{
			try{
				StringBuilder sbMQL = null;
				int iCountTotal = 0;
				int iCountSuccess = 0;
				int iCountFailure = 0;
				String sRelationshipName = "";
				String sFromType = "";
				String sFromName = "";
				String sFromRevision = "";
				String sToType = "";
				String sToName = "";
				String sToRevision = "";
				String sFromObjectId = "";
				String sToObjectId = "";
				boolean bMQLResult = false;
				MQLCommand localMQLCommand = null;
				String strMQLResult = "";
				String sRelId = "";
				ContextUtil.pushContext(context);
				sRelationshipName = ngXMLSchema.getName() != null ? ngXMLSchema.getName() : "";
				strMQLResult = MqlUtil.mqlCommand(context, "list relationship $1", new String[] { sRelationshipName });
				if(strMQLResult != null && !strMQLResult.isEmpty())
				{
					Map<String,String> mapConnectionInfo = null;
					iCountTotal = lstConn.size();
				 	for(Connection connection:lstConn)
				 	{
				 		
				 		sbMQL = new StringBuilder();
			 			sFromType = connection.getFromType() != null ? connection.getFromType() : "";
			 			sFromName = connection.getFromName() != null ? connection.getFromName() : "";
			 			sFromRevision = connection.getFromRevision() != null ? connection.getFromRevision() : "";
			 			sToType = connection.getToType() != null ? connection.getToType() : "";
			 			sToName = connection.getToName() != null ? connection.getToName() : "";
			 			sToRevision = connection.getToRevision() != null ? connection.getToRevision() : "";
			 			//Check From type exist or not
			 			strMQLResult = MqlUtil.mqlCommand(context, "print bus '$1' '$2' '$3' select $4 dump", new String[] { sFromType,sFromName,sFromRevision,"id" });
			 			if(strMQLResult.isEmpty()) {
			 				iCountFailure += 1;
			 				schema_done_log.info("From Businessobject '"+sFromType+"' '"+sFromName+"' '"+sFromRevision+"' does not exist. Please specify valid from object.");
			 			}else {
			 				sFromObjectId = strMQLResult.trim();
			 				//Check To type exist or not
			 				strMQLResult = MqlUtil.mqlCommand(context, "print bus '$1' '$2' '$3' select $4 dump", new String[] { sToType,sToName,sToRevision,"id" });
			 				if(strMQLResult.isEmpty()) {
			 					iCountFailure += 1;
			 					schema_done_log.info("To Businessobject '"+sToType+"' '"+sToName+"' '"+sToRevision+"' does not exist. Please specify valid to object.");
			 				}else{
			 					sToObjectId = strMQLResult.trim();
					 			try {
					 				sbMQL.append("connect bus");
					 				sbMQL.append(" ").append(UIUtil.singleQuoteWithSpace(sFromType));
					 				sbMQL.append(" ").append(UIUtil.singleQuoteWithSpace(sFromName));
					 				sbMQL.append(" ").append(UIUtil.singleQuoteWithSpace(sFromRevision));
					 				sbMQL.append(" ").append("relationship");
					 				sbMQL.append(" ").append(UIUtil.singleQuotes(sRelationshipName));
					 				sbMQL.append(" ").append("to");
					 				sbMQL.append(" ").append(UIUtil.singleQuotes(sToType));
					 				sbMQL.append(" ").append(UIUtil.singleQuotes(sToName));
					 				sbMQL.append(" ").append(UIUtil.singleQuotes(sToRevision));
					 				List<Map<String, String>> listAttributes = connection.getLstAttribute();
					 				if(listAttributes != null && !listAttributes.isEmpty()){
					 					Iterator itr = listAttributes.iterator();
					 					while (itr.hasNext()) {
					 						Map m = (Map)itr.next();
					 						sbMQL.append(" ").append(UIUtil.singleQuotes((String)m.get("name")));
					 						sbMQL.append(" ").append(UIUtil.singleQuotes((String)m.get("value")));
					 					}
					 				}
					 				schema_done_log.info("MQL QUERY FOR CONNECTION : " + sbMQL.toString());
									localMQLCommand = new MQLCommand();
									bMQLResult = localMQLCommand.executeCommand(context, sbMQL.toString(), true);
									schema_done_log.info("MQL QUERY EXECUTION RESULT : " + bMQLResult);
									if(bMQLResult){
										iCountSuccess += 1;
									}else{
										iCountFailure += 1;
										String sMQLError = (String)localMQLCommand.getError();
										schema_done_log.error("Error on connection for ["+sRelationshipName+"] : " +sMQLError);
									}
							//Rel to Rel Connection--------------STARTS
									mapConnectionInfo = new HashMap<String,String>();
									mapConnectionInfo.put("relationshipname", sRelationshipName);
									mapConnectionInfo.put("fromtype", sFromType);
									mapConnectionInfo.put("fromname", sFromName);
									mapConnectionInfo.put("fromrevision", sFromRevision);
									mapConnectionInfo.put("totype", sToType);
									mapConnectionInfo.put("toname", sToName);
									mapConnectionInfo.put("torevision", sToRevision);
									mapConnectionInfo.put("fromobjectid", sFromObjectId);
									mapConnectionInfo.put("toobjectid", sToObjectId);
									strMQLResult = MqlUtil.mqlCommand(context, "expand bus '$1' '$2' '$3' rel '$4' select rel $5 where '$6' limit $7 dump $8", new String[] { sFromType, sFromName,sFromRevision,sRelationshipName,"id", "to.id=='"+sToObjectId+"'", "1", "|"});
									//String strMQLResult = MqlUtil.mqlCommand(context, "expand bus '$1' '$2' '$3' rel '$4' select rel $5 where '$6' dump $7", new String[] { mapConnectionInfo.get("fromtype"), mapConnectionInfo.get("fromname"),mapConnectionInfo.get("fromrevision"),mapConnectionInfo.get("relationshipname"),"id", "to.id=='"+mapConnectionInfo.get("toobjectid")+"'", "|"});
									if(strMQLResult != null && !strMQLResult.isEmpty()){
										StringList slSplit = FrameworkUtil.split(strMQLResult, "|");
										if(slSplit.size()>=6){
											sRelId = slSplit.get(6);
											mapConnectionInfo.put("relid", sRelId);
											if(connection.getLstRelationship() != null && !connection.getLstRelationship().isEmpty()){
												schema_done_log.info("\t\t\t ---------: REL to REL CONNECTION for ["+sRelationshipName+"]-----STARTS");
												processReltoRelConnection(context,connection.getLstRelationship(),mapConnectionInfo,schema_done_log);
												schema_done_log.info("\t\t\t ---------: REL to REL CONNECTION for ["+sRelationshipName+"]-----ENDS");
											}
										}
									}
							//Rel to Rel Connection--------------ENDS
					 			} catch (Exception e) {
									
					 				schema_done_log.error("Error : ["+sRelationshipName+"] : " +e.getMessage());
					 			}
			 				}
			 			}
				 	}
				 	schema_done_log.info("TOTAL  :" + iCountTotal + ", CONNECTSUCCESS :" + iCountSuccess + ", CONNECTFAILED :"+ iCountFailure);
				}
				else
				{
					schema_done_log.info("Relationship ["+sRelationshipName+"] does not exist. Please specify valid relationsip name");
				}
			 	ContextUtil.popContext(context);
	 		}catch(Exception e){
	 			schema_done_log.error("Error : " +e.getMessage());
			}
		}
		private void processReltoRelConnection(Context context,List<Connection> listConnection, Map<String,String> mapConnectionInfo, Logger schema_done_log) throws Exception
		{
			StringBuilder sbMQL = new StringBuilder();
			String mmId = "";
			String sRelId = "";
			String sFromType = "";
			String sFromName = "";
			String sFromRevision = "";
			String sToType = "";
			String sToName = "";
			String sToRevision = "";
			int iCountTotal = 0;
			int iCountSuccess = 0;
			int iCountFailure = 0;
			boolean bMQLResult = false;
			MQLCommand localMQLCommand = null;
			sRelId = mapConnectionInfo.get("relid");
			for(Connection connection : listConnection)
			{
				iCountTotal += 1;
				sFromType = connection.getFromType() != null ? connection.getFromType() : "";
				sFromName = connection.getFromName() != null ? connection.getFromName() : "";
				sFromRevision = connection.getFromRevision() != null ? connection.getFromRevision() : "";
				sToType = connection.getToType() != null ? connection.getToType() : "";
				sToName = connection.getToName() != null ? connection.getToName() : "";
				sToRevision = connection.getToRevision() != null ? connection.getToRevision() : "";
				sbMQL.append("add connection");
				if(connection.getFrommid() != null && !connection.getFrommid().isEmpty()){
					mmId = connection.getFrommid();
					sbMQL.append(" ").append(UIUtil.singleQuoteWithSpace(mmId));
					sbMQL.append(" ").append("to");
					sbMQL.append(" ").append(UIUtil.singleQuoteWithSpace(sToType));
					sbMQL.append(" ").append(UIUtil.singleQuoteWithSpace(sToName));
					sbMQL.append(" ").append(UIUtil.singleQuoteWithSpace(sToRevision));
					sbMQL.append(" ").append("fromrel");
					sbMQL.append(" ").append(UIUtil.singleQuoteWithSpace(sRelId));
				}else if(connection.getTomid() != null && !connection.getTomid().isEmpty()){
					mmId = connection.getTomid();
					sbMQL.append(" ").append(UIUtil.singleQuoteWithSpace(mmId));
					sbMQL.append(" ").append("from");
					sbMQL.append(" ").append(UIUtil.singleQuoteWithSpace(sFromType));
					sbMQL.append(" ").append(UIUtil.singleQuoteWithSpace(sFromName));
					sbMQL.append(" ").append(UIUtil.singleQuoteWithSpace(sFromRevision));
					sbMQL.append(" ").append("torel");
					sbMQL.append(" ").append(UIUtil.singleQuoteWithSpace(sRelId));
				}
				List<Map<String, String>> listAttributes = connection.getLstAttribute();
 				if(listAttributes != null && !listAttributes.isEmpty()){
 					Iterator itr = listAttributes.iterator();
 					while (itr.hasNext()) {
 						Map m = (Map)itr.next();
 						sbMQL.append(" ").append(UIUtil.singleQuotes((String)m.get("name")));
 						sbMQL.append(" ").append(UIUtil.singleQuotes((String)m.get("value")));
 					}
 				}
			 	schema_done_log.info("MQL QUERY FOR {REL TO REL} CONNECTION : " + sbMQL.toString());

				localMQLCommand = new MQLCommand();
				bMQLResult = localMQLCommand.executeCommand(context, sbMQL.toString(), true);
				schema_done_log.info("{REL TO REL} CONNECTION MQL QUERY EXECUTION RESULT : " + bMQLResult);
				if(bMQLResult){ 
					iCountSuccess += 1;
				}else{
					iCountFailure += 1;
					String sMQLError = (String)localMQLCommand.getError();
					schema_done_log.error("Error {REL to REL}: " + sMQLError);
				}
			}
			schema_done_log.info("RELTOREL_TOTAL  :" + iCountTotal + ", RELTOREL_SUCCESS :" + iCountSuccess + ", RELTOREL_FAILED :"+ iCountFailure);
		}
}
