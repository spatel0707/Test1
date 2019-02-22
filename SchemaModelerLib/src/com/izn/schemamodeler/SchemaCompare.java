package com.izn.schemamodeler;

import org.apache.log4j.Logger;
import com.izn.schemamodeler.util.SCMConfigProperty;

import matrix.db.Context;

public interface SchemaCompare {
	
	String NAME="name";
	String DESCRIPTION="description";
	String TYPE="type";
	String HIDDEN="hidden";
	String REVISION="revision";
	String DEFAULT="default";
	String INPUT="input";
	String PROGRAM="program";
	String ACTION="action";
	public void compareSchema(Context context,String strSchemaName,Logger logger,SCMConfigProperty scmConfigProperty) throws Exception;

}
