package schemamodeler;

public class SchemaErrorHandler {
	public static void errorHandler(String param1, String param2, Exception exception) {
		switch (exception.getClass().getName().toString()) {
		case "schemamodeler.InvalidPathError": 			
			System.out.println("Error : "+exception.getMessage());
			break;
		case "schemamodeler.InvalidSyntaxError": 
			if("quote(s) missing".equalsIgnoreCase(exception.getMessage())) {
				System.out.println("Error : "+exception.getMessage());
			}else {
				System.out.println("Error : "+exception.getMessage());
				if("ALL".equals(param1)) {
					displayUsageForExportSchema();displayUsageForExportBus();displayUsageForExportPnO();displayUsageForImport();
				} else if ("export".equals(param1)) {
					if("schema".equals(param2)) {
						displayUsageForExportSchema();
					} else if ("bus".equals(param2)){
						displayUsageForExportBus();
					} else if ("pno".equals(param2)){
						displayUsageForExportPnO();
					} else if ("ALLExport".equals(param2)) {
						displayUsageForExportSchema();displayUsageForExportBus();displayUsageForExportPnO();
					}
				} else if ("import".equals(param1)) {
					displayUsageForImport();
				}
			}
			;break;
		case "java.lang.IndexOutOfBoundsException": System.out.println("Error : Missmatch in no of command argument. Please cross verify command");break;
		case "java.lang.ArrayIndexOutOfBoundsException": System.out.println("Error : "+exception.getMessage());break;
		case "java.lang.StringIndexOutOfBoundsException": System.out.println("Error : "+exception.getMessage());break;
		default:
			System.out.println("Error : "+exception.getClass().getName().toString());
			break;
		}
	}
	//Code added to display usage for Export Schema: start
	public static void displayUsageForExportSchema() {
		StringBuilder exportSchemaHelp = new StringBuilder("");
		exportSchemaHelp.append("\n******** EXPORT SCHEMA COMMAND USAGE ********");
		exportSchemaHelp.append("\n Warning : To Export the Schema content, execute the below command");
		exportSchemaHelp.append("\n export schema 'SchemaType1|..|SchemaTypeN' 'SchemaName' [moddate [mm/dd/yyyy]] [output EXPORT_DIR]'\n");
		exportSchemaHelp.append("\n Param1 - Schema  constant variable to export Schema content. It is case insensitive.");
		exportSchemaHelp.append("\n Param2 - SchemaType like Attribute,Type,Command,Menu,etc. ");
		exportSchemaHelp.append("\n        - If you want extract multiple SchemaTypes, provide. SchemaType with \"|\" separator like  Attribute|Type|Menu.");
		exportSchemaHelp.append("\n        - To export all schema content, Provide  character.");
		exportSchemaHelp.append("\n Param3 - SchemaName to extract like Part.");
		exportSchemaHelp.append("\n        - If you want to extract Schema name starts with Part, We can provide  value like Part*. To export all schema name content, Provide \"*\" character.");
		exportSchemaHelp.append("\n Param4 (Optional)- Modified date of schema.");
		exportSchemaHelp.append("          - If you pass modified date,it will export all schema before specified modified date.");
		exportSchemaHelp.append("\n Param5 (Optional)- Directory to export  the Schema Content.");
		exportSchemaHelp.append("          - If you pass \"\" or are pass the argument,it will export the Schema Content in \"C:\\temp\" or \"\" folder.");
		exportSchemaHelp.append("\n----------------------------------------------------------");
		System.out.println(exportSchemaHelp.toString());
	}
	
	//Code added to display usage for Export Bus: start
	public static void displayUsageForExportBus() {
		StringBuilder exportBusHelp = new StringBuilder("");
		exportBusHelp.append("\n******** EXPORT BUS COMMAND USAGE ********");
		exportBusHelp.append("\n Warning : To Export the Business Objects, execute the below command");
		exportBusHelp.append("\n export bus Type Name Revision [relationship or rel REL1|..|RELN] [exact] [vault VAULT1|...|VAULTN] [output EXPORT_DIR]\n");
		exportBusHelp.append("\n Param1 - bus  constant variable to export business object");
		exportBusHelp.append("\n Param2 - Param2 - Type name like Part, Document, etc..");
		exportBusHelp.append("\n        - If you want extract multiple types object, pass Type names with \"|\" separator like  Part|Document|Project Space. ");
		exportBusHelp.append("\n        - To export all Types object, Pass \"*\" character.");
		exportBusHelp.append("\n        - If you don't want to export businessobject, pass value as \"\" (empty) char.");
		exportBusHelp.append("\n Param3 - ObjectName to export like A-00001.");
		exportBusHelp.append("\n        - If you want to export object name starts with IZ,pass value as IZ*.");
		exportBusHelp.append("\n        - To export all the objects , pass value as \"*\" character.");
		exportBusHelp.append("\n        - If you don't want to export any businessobject,pass value as \"\" (empty) char.");
		exportBusHelp.append("\n Param4 - Object Revision to export like A.");
		exportBusHelp.append("\n        - If you want to export object revision starts with 0,pass value as 0*.");
		exportBusHelp.append("\n        - To export all the revisions, pass value as \"*\" character.");
		exportBusHelp.append("\n        - If you don't want to export any businessobject,pass value as \"\" (empty) char.");
		exportBusHelp.append("\n Param5(Optional) - Relationship name like Part Specification, Reference Document, etc..");
		exportBusHelp.append("\n        - If you want extract multiple relationships, pass rel names with \"|\" separator like Part Specification|Reference Document.");
		exportBusHelp.append("\n        - To export all Rels list, Pass \"*\" character.");
		exportBusHelp.append("\n        - If you pass \"\" or are pass the argument,it will export the businessobject in C:\temp or  folder..");
		exportBusHelp.append("\n Param6(Optional) - exact constant variable to export exact type objects.");
		exportBusHelp.append("\n        - If you don't want to exact type objects,pass value as \"\" (empty) char.");
		exportBusHelp.append("\n Param7(Optional) - vault name to export objects from particular vault like  eService Production.");
		exportBusHelp.append("\n        - If you want to export objects from multiple  vaults, pass value like vplm|eService Production");
		exportBusHelp.append("\n        - To export objects from all the vault, pass value as \"*\" or \"\".");
		exportBusHelp.append("\n Param8(Optional) - Directory to export  the businessobject content.");
		exportBusHelp.append("\n        - If you pass \"\" or are pass the argument,it will export the Schema Content in \"C:\\temp\" or \"\" folder.");
		exportBusHelp.append("\n ----------------------------------------------------------");
		System.out.println(exportBusHelp.toString());
	}
	
	//Code added to display usage for export pno: start
	public static void displayUsageForExportPnO() {
		StringBuilder exportPnO = new StringBuilder("");
		exportPnO.append("\n******** EXPORT PnO COMMAND USAGE **********");
		exportPnO.append("\n Warning : To export the pno, execute the below command");
		exportPnO.append("\n export pno 'SchemaType1|..|SchemaTypeN' 'SchemaName' [output EXPORT_DIR]\n");
		exportPnO.append("\n Param1 - pnoSchemaType should be either person or disciplines or context or company or role or PROJECT or *");
		exportPnO.append("\n Param2 - pnoSchemaName should be like *, VPLM* etc..");
		exportPnO.append("\n Param3(Optional) - Directory to export the pno content.");
		exportPnO.append("\n        - If you pass \"\" or are pass the argument,it will export the pno content in \"C:\\temp\" or \"\" folder.");
		exportPnO.append("\n ----------------------------------------------------------");
		System.out.println(exportPnO.toString());
	}
	
	//Code added to display usage for Import: start
	public static void displayUsageForImport() {
		StringBuilder importHelp = new StringBuilder("");
		importHelp.append("\n******** IMPORT COMMAND USAGE **********");
		importHelp.append("\n Warning : To import the Schema/businessobjects, execute the below command ");
		importHelp.append("\n Import  '*|SchemaTypes|bus' 'Dir_To_Import'\n");
		importHelp.append("\n Param1 - value should be either * or bus or schematypes(eg. attribute,type,..etc)");
		importHelp.append("\n \t* - import all schema/businessobjects inside 'Dir_To_Import'");
		importHelp.append("\n \tSchemaTypes - import only specified schematype (eg. attribute,type,..etc) inside 'Dir_To_Import'");
		importHelp.append("\n \tbus - import all businessobjects inside 'Dir_To_Import'");
		importHelp.append("\n ----------------------------------------------------------");
		System.out.println(importHelp.toString());
	}
}
