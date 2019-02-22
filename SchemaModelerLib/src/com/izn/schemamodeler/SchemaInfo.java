package com.izn.schemamodeler;
 

import matrix.db.Context;
 

public interface SchemaInfo {
	
	String EMPTY_STRING="";
	String geSchemaInfo(Context context,String strSchemaName, String strSchemaFileName) throws Exception;
	
	
	String geSchemaInfoWithPath(Context context, String strSchemaName, String strExportPath) throws Exception;
	
 
	
}
