package com.izn.schemamodeler;

import java.util.List;
import org.apache.log4j.Logger;

import com.dassault_systemes.platform.ven.jackson.databind.ObjectMapper;
import matrix.db.Context;

import com.izn.schemamodeler.util.SCMConfigProperty;

public interface SchemaExport {
 SchemaFactoryInfo _schemaFactory = new SchemaFactoryInfo();
 ObjectMapper _gson = new ObjectMapper();

 public void exportSchema(Context context,String strAdminType,List<String> lsNames,String exportPath, String strVersion, Logger logger, SCMConfigProperty scmConfigProperty) throws Exception;

}
