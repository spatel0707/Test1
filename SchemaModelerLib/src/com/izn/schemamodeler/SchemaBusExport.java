package com.izn.schemamodeler;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import com.izn.schemamodeler.util.SCMConfigProperty;
import matrix.db.Context;

public interface SchemaBusExport {
	SchemaFactoryInfo _schemaFactory = new SchemaFactoryInfo();
	//Gson _gson=new Gson();
	ObjectMapper _gson = new ObjectMapper();
	public void exportBus(Context context,String strType,List<String> lsNames,String exportPath, String strVersion, String strFile, String strExpandRel, Logger logger,  SCMConfigProperty scmConfigProperty) throws Exception; 	
	public Map<String,Set<String>> exportRel(Context context,String strType,List<String> lsNames,String exportPath, String strVersion, String strFile, String strExpandRel, Logger logger,  SCMConfigProperty scmConfigProperty) throws Exception; 	
}
