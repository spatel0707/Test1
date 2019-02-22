package com.izn.schemamodeler.util;

public class SchemaModelerConstants {
	public final static String TRUE = "true";
	public final static String FALSE = "false";
	public final static String APPLICATION_JSON = "application/json";
	public final static String EMPTY = "";
	public final static String WARNING = "Warning";
	public final static String EXPORT_DONE_MESSAGE = "Export Done";
	public final static String IMPORT_DONE_MESSAGE = "Import Done";
	public final static String CORRECT_VERSION_MESSAGE = "Please correct 3DExperience Platform version in config.properties.";
	public final static String RESTSERVICEMODELER_APPLICATIONPATH = "/resources/IZNSchemaModeler";
	public final static String RESTFULCLI_PATH = "/RestfulCLI";
	
	final static String[] _aArryExportALLAdmin = { "association", "format", "group", "rule", "inquiry", "policy", "form", "relationship",
			"attribute", "type", "role", "interface", "dimension", "program", "channel", "command", "menu",
			"page", "table", "portal"};
	final static String[] _aArryImportALLAdmin = { "association", "format", "group", "rule", "inquiry", "policy", "form", "relationship",
			"attribute", "type", "role", "interface", "dimension", "program", "channel", "command", "menu","page", "table", "portal", "businessobject", "connection", "webform"};
	
	//Logger
	public final static String FAILED_INITIALISE_FILE_APPENDER_MESSAGE = "Failed to initialise file appender. Error:";
	public final static String PROBLEM_DEBUG_LOG_FILE_MESSAGE = "Problem with debug log file location. Error: ";
	public final static String MAXFILESIZE = "25MB";
	public final static String LOGGER_NAME = "FileLogger";
	public final static String ENCODING = "UTF-8";
	
	//Symbols
	public final static String SPECIALCHARECTERS = "[^a-zA-Z0-9_. ]";
	public final static String SYMBOL_WILDCARD = "*";
	public final static String SYMBOL_PIPELINE = "|";
	public final static String SYMBOL_COMMA = ",";
	public final static String SYMBOL_AT = "@";
	public final static String SYMBOL_BACKSLASH = "\\";
	public final static String SYMBOL_FORWARDSLASH = "//";
	public final static String SYMBOL_SPACE = " ";
	public final static String SYMBOL_REPLACE_SPACE = "$SPACE$";
	public final static String SYMBOL_EQUAL = "=";
	public final static String SYMBOL_HASH = "#";
	
	//Common
	public final static String ID = "id";
	public final static String USER = "user";
	public final static String PASS = "pass";
	public final static String ACTION = "action";
	public final static String VERSION = "version";
	public final static String SCHEMA = "schema";
	public final static String BUS = "bus";
	public final static String PNO = "pno";
	public final static String ADMINTYPE = "admintype";
	public final static String ADMINUSER = "adminUser";
	public final static String ADMINCONTEXT = "adminContext";
	public final static String ADMINPASSWORD = "adminPassword";
	public final static String SERVERPATH = "serverPath";
	public final static String SURL = "sURL";
	public final static String EXPORT = "export";
	public final static String IMPORT = "import";
	public final static String EXPORT_PATH = "exportPath";
	public final static String EXPORT_TYPE = "exportType";
	public final static String FILENAMES = "fileNames";
	public final static String SCHEMANAMES = "schemaNames";
	public final static String LOGS_FOLDERNAME = "Logs";
	public final static String LOGS_SCHEMAEXPORTDONE_FILENAME = "SchemaExportDone";
	public final static String LOGS_SCHEMAIMPORTDONE_FILENAME = "SchemaImportDone";
	public final static String SCHEMA_SEPERATOR = "schemaSeperator";
	public final static String LOG_EVERYTHING = "logEverything";
	public final static String RAWTYPES = "rawtypes";
	public final static String UNCHECKED = "unchecked";
	public final static String PATTERN = "pattern";
	public final static String REVISION = "revision";
	public final static String CLONE = "clone";
	public final static String REL = "rel";
	public final static String VAULT = "vault";
	public final static String MODIFIEDDATE = "modifiedDate";

	//Schema Types
	public final static String ASSOCIATION = "association";
	public final static String ATTRIBUTE = "attribute";
	public final static String DIMENSION = "dimension";
	public final static String FORMAT = "format";
	public final static String GROUP = "group";
	public final static String RULE = "rule";
	public final static String INQUIRY = "inquiry";
	public final static String POLICY = "policy";
	public final static String FORM = "form";
	public final static String RELATIONSHIP = "relationship";
	public final static String TYPE = "type";
	public final static String ROLE = "role";
	public final static String INTERFACE = "interface";
	public final static String PROGRAM = "program";
	public final static String CHANNEL = "channel";
	public final static String COMMAND = "command";
	public final static String MENU = "menu";
	public final static String PAGE = "page";
	public final static String TABLE = "table";
	public final static String PORTAL = "portal";
	public final static String BUSINESSOBJECT = "businessobject";
	public final static String CONNECTION = "connection";
	public final static String WEBFORM = "webform";
	
	public final static String _ASSOCIATION = "association_";
	public final static String _ATTRIBUTE = "attribute_";
	public final static String _DIMENSION = "dimension_";
	public final static String _FORMAT = "format_";
	public final static String _GROUP = "group_";
	public final static String _RULE = "rule_";
	public final static String _INQUIRY = "inquiry_";
	public final static String _POLICY = "policy_";
	public final static String _WEBFORM = "form_";
	public final static String _RELATIONSHIP = "relationship_";
	public final static String _TYPE = "type_";
	public final static String _ROLE = "role_";
	public final static String _INTERFACE = "interface_";
	public final static String _PROGRAM = "program_";
	public final static String _CHANNEL = "channel_";
	public final static String _COMMAND = "command_";
	public final static String _MENU = "menu_";
	public final static String _PAGE = "page_";
	public final static String _TABLE = "table_";
	public final static String _PORTAL = "portal_";
	public final static String _STATE = "state_";

	//SchemaModelerLib
	public final static String UNMARSHAL_EXCPTION_WARNING_MESSAGE = "Please remove special characters which are not supported for XML and proceed.";
	public final static String ADD_OPERATION = "add";
	public final static String MODIFY_OPERATION = "mod";
	public final static String DELETE_OPERATION = "delete";
		
	public final static String TOTAL = "TOTAL :";
	public final static String SUCCESS = "SUCCESS :";
	public final static String FAILURE = "FAILURE :";
	public final static String ADDED = "ADDED :";
	public final static String MODIFIED = "MODIFIED :";
	public final static String DELETED = "DELETED :";
	public final static String CONNECTSUCCESS = "CONNECTSUCCESS :";
	public final static String CONNECTFAILED = "CONNECTFAILED :";
	public final static String RELTOREL_TOTAL = "RELTOREL_TOTAL  :";
	public final static String RELTOREL_SUCCESS = "RELTOREL_SUCCESS :";
	public final static String RELTOREL_FAILED = "RELTOREL_FAILED :";
	
	//Common for all schema
	public final static String ADMIN_TYPE = "adminType";
	public final static String REGISTRY_NAME = "registryname";
	public final static String NAME = "name";
	public final static String VALUE = "value";
	public final static String DESCRIPTION = "description";
	public final static String HIDDEN = "hidden";
	public final static String N_HIDDEN = "!hidden";
	public final static String NOT_HIDDEN = "nothidden";
	public final static String SETTING = "setting";
	public final static String DEFAULT = "default";
	public final static String MODIFY = "modify";
	public final static String ADMIN = "admin";
	public final static String UI3 = "ui3";
	public final static String TRIGGER = "trigger";
	public final static String INPUT = "input";
	public final static String GLOBAL = "global";
	public final static String XML_EXTENSION = ".xml";
	public final static String NONE = "none";
	public final static String FLAG = "flag";
	public final static String CODE = "code";
	public final static String PROPERTY = "property";
	public final static String EMXTRIGGERMANAGER = "emxTriggerManager";
	
	//Log informations
	public final static String ERROR = "Error : ";
	public final static String ERROR_REL = "Error {REL to REL}: ";
	public final static String ERROR_OCCURED_EXPORT_SCHEMA = "Error occurred while exporting schema";
	public final static String ERROR_OCCURED_EXPORT_BO = "Error occurred while exporting businessobject";
	public final static String ERROR_OCCURED_IMPORT_SCHEMA = "Error occurred while importing schema";
	public final static String ERROR_OCCURED_IMPORT_BO = "Error occurred while importing businessobject";
	public final static String ERROR_OCCURED_IMPORT_CONNECTION = "MQL QUERY FOR {REL TO REL} CONNECTION : ";
	public final static String ERROR_OCCURED_IMPORT_EXECUTION_RESULT_CONNECTION = "{REL TO REL} CONNECTION MQL QUERY EXECUTION RESULT :";
	public final static String MQL_QUERY_IMPORT = "MQL QUERY FOR IMPORT : ";
	public final static String MQL_QUERY_EXECUTION_RESULT  = "MQL QUERY EXECUTION RESULT : ";
	public final static String MQL_QUERY_PROPERTY  = "MQL QUERY {PROPERTY} : ";
	public final static String EXPORT_TOTAL  = "EXPORT TOTAL";
	public final static String MQL_QUERY_CONNECTION  = "MQL QUERY FOR CONNECTION : ";

	//
	public final static String ASSOCIATION_DETAILS = "associationdetails";
	public final static String ATTRIBUTE_DETAILS = "attributedetails";
	public final static String DIMENSION_DETAILS = "dimensionedetails";
	public final static String FORMAT_DETAILS = "formatdetails";
	public final static String GROUP_DETAILS = "groupdetails";
	public final static String INQUIRY_DETAILS = "inquirydetails";
	public final static String POLICY_DETAILS = "policydetails";
	public final static String RELATIONSHIP_DETAILS = "reldetails";
	public final static String TYPE_DETAILS = "typedetails";
	public final static String ROLE_DETAILS = "roledetails";
	public final static String INTERFACE_DETAILS = "interfacedetails";
	public final static String PROGRAM_DETAILS = "programdetails";
	public final static String CHANNEL_DETAILS = "channeldetails";
	public final static String COMMAND_DETAILS = "commanddetails";
	public final static String MENU_DETAILS = "menudetails";
	public final static String PAGE_DETAILS = "pagedetails";
	public final static String FIELD_DETAILS = "fielddetails";
	public final static String PORTAL_DETAILS = "portaldetails";
	public final static String BUSINESSOBJECT_DETAILS = "businessobject";
	public final static String CONNECTION_DETAILS = "connection";
	
	//Field Details
	public final static String[] ASSOCIATION_FIELDDETAILS = {"definition"};
	public final static String[] ATTRIBUTE_FIELDDETAILS = {"type", "owner", "ownerkind", "multiline", "maxlength", "dimension", "valuetype","default", "resetonclone", "resetonrevision"};
	public final static String[] DIMENSION_FIELDDETAILS = {"dbunit", "unit", "label", "unitdescription", "default", "multiplier", "offset"};
	public final static String[] FORMAT_FIELDDETAILS = {"version", "filesuffix", "filecreator", "filetype"};
	public final static String[] GROUP_FIELDDETAILS = {"parent", "site", "child", "assignment", "iconFile"};
	public final static String[] INQUIRY_FIELDDETAILS = {"pattern", "format", "code"};
	public final static String[] POLICY_FIELDDETAILS = { "store", "minorsequence", "type", "format", "defaultformat", "enforcelocking" };
	public final static String[] SATE_FIELDDETAILS = {"promote", "version", "checkouthistory", "minorrevisionable"};
	public final static String[] SIGNATURE_FIELDDETAILS = {"branch", "filter", "approve", "reject", "ignore"};
	public final static String[] RELATIONSHIP_FIELDDETAILS = { "derived", "abstract", "sparse", "preventduplicates", "attribute" };
	public final static String[] RELATIONSHIP_KEYS= { "type", "relationship", "meaning", "cardinality", "revision", "clone", "propagate modify", "propagate connection" };
	public final static String[] CLONEREVISIONFLAG_KEYS= { "replicate", "float", "none" };
	public final static String[] TYPE_FIELDDETAILS = { "derived", "abstract", "sparse", "attribute", "method" };
	public final static String[] ROLE_FIELDDETAILS = { "parent", "site", "maturity", "child", "roletype", "assignment" };
	public final static String[] INTERFACE_FIELDDETAILS = {"derived", "abstract", "attribute", "type", "relationship"};
	public final static String[] PROGRAM_FIELDDETAILS = {"code", "type", "user", "execute", "needsbusinessobject", "downloadable", "pipe",	"pooled"};
	public final static String[] PROGRAMTYPES = { "java", "mql", "external" };
	public final static String[] EXECUTES = { "immediate", "deferred" };
	public final static String[] CHANNEL_FIELDDETAILS = { "label", "href", "alt", "command", "height" };
	public final static String[] COMMAND_FIELDDETAILS = { "label", "href", "alt", "code" };
	public final static String[] MENU_FIELDDETAILS = { "label", "href", "alt" };
	public final static String[] PAGE_FIELDDETAILS = { "mime", "content"};
	public final static String[] TABLE_FIELDDETAILS = { "column", "label", "description", "columnType", "expression", "href", "alt", "range","update", "order", "sorttype" };
	public final static String[] PORTAL_FIELDDETAILS = { "label", "href", "alt" };
	public final static String[] BUSINESSOBJECT_FIELDDETAILS = { "policy", "vault", "state", "owner", "description", "filePath", "originated","modified", "grantee" };
	public final static String[] CONNECTION_FIELDDETAILS = { "from_type", "from_name", "from_revision", "to_type", "to_name", "to_revision" };
	public final static String[] WEBFORM_FIELDDETAILS = { "column", "label", "description", "columnType", "expression", "href", "alt", "range","update", "order" };
	
	//Association
	public final static String DEFINITION = "definition";
	
	//Attribute
	public final static String RESETONCLONE = "resetonclone";
	public final static String NOT_RESETONCLONE = "notresetonclone";
	public final static String RESETONREVISION = "resetonrevision";
	public final static String NOT_RESETONREVISION = "notresetonrevision";
	public final static String INTEGER = "integer";
	public final static String REAL = "real";
	public final static String TIMESTAMP = "timestamp";
	public final static String SINGLEVALUE = "singleValue";
	public final static String MULTIVALUE = "multiValue";
	public final static String NOT_MULTIVALUE = "notmultivalue";
	public final static String RANGEVALUE = "rangeValue";
	public final static String MULTILINE = "multiLine";
	public final static String N_MULTILINE = "!multiLine";
	public final static String NOTMULTIVALUE_NOTRANGEVALUE = "notmultivalue notrangevalue";
	public final static String MULTIVALUE_NOTRANGEVALUE = "multivalue notrangevalue";
	public final static String NOTMULTIVALUE_RANGEVALUE = "notmultivalue rangevalue";
	public final static String OWNERKIND = "ownerkind";
	public final static String MAXLENGTH = "maxlength";
	public final static String ATTRIBUTE_RANGE = "range";
	public final static String RANGES = "ranges";
	public final static String OPERATOR = "operator";
	public final static String RESETON = "resetOn";
	public final static String VALUETYPE = "valueType";
	public final static String SCOPE = "scope";
	public final static String BETWEEN = "between";
	public final static String[] ATTRIBUTE_KEYVALUETYPE = {"singleValue", "multiValue", "rangeValue"};
	
	//Dimension
	public final static String UNIT = "unit";
	public final static String UNITS = "units";
	public final static String DBUNIT = "dbunit";
	public final static String UNITDESCRIPTION = "unitdescription";
	public final static String MULTIPLIER = "multiplier";
	public final static String OFFSET = "offset";

	//Format
	public final static String FILESUFFIX = "filesuffix";
	public final static String SUFFIX = "suffix";
	public final static String FILECREATOR = "filecreator";
	public final static String FILETYPE = "filetype";
	public final static String CREATOR = "creator";
	
	//Group
	public final static String PARENT = "parent";
	public final static String SITE = "site";
	public final static String CHILD = "child";
	public final static String ICONFILE = "iconFile";
	public final static String ASSIGN_PERSON = "assign person";
	public final static String ASSIGNMENT = "assignment";
	
	//Inquiry
	public final static String ARGUMENT = "argument";	
	
	//Interface
	public final static String DERIVED = "derived";	
	public final static String ABSTRACT = "abstract";	
	public final static String INHERITED_ATTRIBUTE = "inherited attribute";	
	public final static String INHERITEDATTRIBUTE = "inheriedAttribute";
	
	//Policy
	public final static String ENFORCE = "enforce";
	public final static String NOTENFORCE = "notenforce";
	public final static String POLICYACCESS = "read,modify,delete,checkout,checkin,schedule,lock,execute,unlock,freeze,thaw,create,revise,promote,demote,grant,enable,disable,override,changename,changetype,changeowner,changepolicy,revoke,changevault,fromconnect,toconnect,fromdisconnect,todisconnect,viewform,modifyform,show,approve,reject,ignore,reserve,unreserve,,majorrevise";
	public final static String ENFORCELOCKING = "enforcelocking";
	public final static String DEFAULTFORMAT = "defaultformat";
	public final static String STORE = "store";
	public final static String MINORSEQUENCE = "minorsequence";
	public final static String STATE = "state";
	public final static String STATES = "states";
	public final static String ALLSTATE = "allstate";
	public final static String PROPERTY_NAME = "property.name";
	public final static String LOCKINGNOT = "locking not";
	public final static String MINOR_SEQUENCE = "minor sequence";
	public final static String SIGNATURE = "signature";
	public final static String IGNORE = "ignore";
	public final static String REJECT = "reject";
	public final static String APPROVE = "approve";
	public final static String FILTER = "filter";
	public final static String VERSIONABLE = "versionable";
	public final static String MINORREVISIONABLE = "minorrevisionable";
	public final static String MAJORREVISIONABLE = "majorrevisionable";
	public final static String REVISIONABLE = "revisionable";
	public final static String PROMOTE = "promote";
	public final static String CHECKOUT_HISTORY = "checkout history";
	public final static String CHECKOUTHISTORY = "checkouthistory";
	public final static String INHERITED_TRIGGER = "inherited trigger";
	public final static String ORGANIZATION = "organization";
	public final static String PROJECT = "project";
	public final static String ACCESS = "access";
	public final static String ALLSTATEACCESS = "allStateAccess";
	public final static String ALLSTATEACCESSENABLED = "allStateAccessEnabled";
	public final static String PUBLIC = "public";
	public final static String OWNER = "owner";
	public final static String ALL = "all";
	public final static String TYPES = "types";
	
	//Program
	public final static String NEEDSBUSINESSOBJECT = "needsbusinessobject";
	public final static String N_NEEDSBUSINESSOBJECT = "!needsbusinessobject";
	public final static String DOWNLOADABLE = "downloadable";
	public final static String N_DOWNLOADABLE = "!downloadable";
	public final static String PIPE = "pipe";
	public final static String N_PIPE = "!pipe";
	public final static String POOLED = "pooled";
	public final static String N_POOLED = "!pooled";
	public final static String MQL = "mql";
	public final static String JAVA = "java";
	public final static String EXTERNAL = "external";
	public final static String IMMEDIATE = "immediate";
	public final static String DEFERRED = "deferred";
	public final static String EXECUTE = "execute";
	public final static String FILEPATH = "filepath";
	public final static String PROGRAM_FOLDER_NAME = "Programs";
	
	//Relationship
	public final static String PREVENTDUPLICATES = "preventduplicates";
	public final static String NOTPREVENTDUPLICATES = "notpreventduplicates";
	public final static String PROPAGATEMODIFY = "PROPAGATEMODIFY";
	public final static String N_PROPAGATEMODIFY = "!propagatemodify";
	public final static String PROPAGATECONNECTION = "propagateconnection";
	public final static String N_PROPAGATECONNECTION = "!propagateconnection";
	public final static String FROMTYPES = "fromtypes";
	public final static String FROMTYPE = "FromType";
	public final static String TOTYPES = "totypes";
	public final static String FROMREL = "fromrel";
	public final static String TOREL = "torel";
	public final static String SPARSE = "sparse";
	public final static String FLOAT = "float";
	public final static String REPLICATE = "replicate";
	public final static String MANY = "many";
	public final static String CARDINALITY = "cardinality";
	public final static String TOSIDE = "toSide";
	public final static String INHERITEDTRIGGER = "inheritedTrigger";
	public final static String FROM = "from";
	public final static String TO = "to";
	public final static String FROMSIDE = "fromSide";
	public final static String MEANING = "meaning";
	public final static String PROPAGATE_MODIFY = "propagate modify";
	public final static String PROPAGATE_CONNECTION = "propagate connection";
	
	//Role
	public final static String MATURITY = "maturity";
	public final static String ISAPROJECT = "isaproject";
	public final static String ISANORG = "isanorg";
	public final static String ISAROLE = "isarole";
	public final static String ROLETYPE = "roletype";
	public final static String ASAROLE = "asarole";
	public final static String ASANORG = "asanorg";
	public final static String ASAPROJECT = "asaproject";
	
	//Type
	public final static String METHOD = "method";
	public final static String WARNING_MORETHANDERIVEDTYPE = "Type can not have more than one derived type : ";
	
	//Channel
	public final static String ICON = "icon";
	public final static String HEIGHT = "height";
	public final static String ORDER = "order";
	public final static String COMMANDS = "commands";
	public final static String HREF = "href";
	public final static String ALT = "alt";
	public final static String SETTINGS = "settings";
	
	//Portal
	public final static String CHANNELS = "channels";
	
	//Command
	public final static String OBJECTACCESS = "objectAccess";
	
	//Page
	public final static String MIME = "mime";
	public final static String PAGES_FOLDER_NAME = "Pages";
	public final static String ORIGINATED_PAGE = "originated";
	public final static String MODIFIED_PAGE = "modified";
	
	//Table
	public final static String COLUMN = "column";
	public final static String ORDERDB = "orderDB";
	public final static String RANGE_TABLE = "range";
	public final static String UPDATE_TABLE = "update";
	public final static String SORTTYPE_TABLE = "sorttype";
	public final static String BUSINESSOBJECT_TABLE = "businessobject";
	public final static String COLUMNTYPE_TABLE = "columnType";
	public final static String EXPRESSION_TABLE = "expression";
	public final static String SET_TABLE = "set";
	public final static String RELATIONSHIP_TABLE = "relationship";
	public final static String USER_TABLE = "user";
	public final static String SYSTEM_TABLE = "system";
	
	//Webform
	public final static String WEBFORM_FORM = "webForm";
	public final static String FIELD_HASH_FORM = "field#";
	public final static String SELECT_FORM = "select";
	public final static String FIELDS = "fields";
	public final static String RANGE_FORM = "range";
	public final static String UPDATE_FORM = "update";
	public final static String SORTTYPE_FORM = "sorttype";
	public final static String BUSINESSOBJECT_FORM = "businessobject";
	public final static String COLUMNTYPE_FORM = "columnType";
	public final static String EXPRESSION_FORM = "expression";
	public final static String SET_FORM = "set";
	
	//Businessobject
	public final static String ORIGINATED_BO = "originated";
	public final static String MODIFIED_BO = "modified";
	public final static String GRANTEE_BO = "grantee";
	public final static String ATTRIBUTES_BO = "attributes";
	public final static String POLICY_BO = "POLICY";
	public final static String VAULT_BO = "VAULT";
	public final static String DESCRIPTION_BO = "DESCRIPTION";
	public final static String OWNER_BO = "OWNER";
	
	//Connection
	public final static String FROMTYPE_CONNECTION = "from.type";
	public final static String FROMNAME_CONNECTION = "from.name";
	public final static String FROMREVISION_CONNECTION = "from.revision";
	public final static String TOTYPE_CONNECTION = "to.type";
	public final static String TONAME_CONNECTION = "to.name";
	public final static String TOREVISION_CONNECTION = "to.revision";
	public final static String ATTRIBUTEVALUE_CONNECTION = "attribute.value";
	public final static String TOMID_CONNECTION = "tomid.id";
	public final static String FROMMID_CONNECTION = "frommid.id";
	public final static String FROM_TYPE_CONNECTION = "from_type";
	public final static String FROM_NAME_CONNECTION = "from_name";
	public final static String FROMR_EVISION_CONNECTION = "from_revision";
	public final static String TO_TYPE_CONNECTION = "to_type";
	public final static String TO_NAME_CONNECTION = "to_name";
	public final static String TO_REVISION_CONNECTION = "to_revision";
	public final static String ATTRIBUTES_CONNECTION = "attributes";
	public final static String RELATIONSHIPS_CONNECTION = "relationships";
	public final static String FROMMID = "frommid";
	public final static String TOMID = "tomid";
	public final static String RELID = "relid";
	public final static String LOG_FROM_BUSINESSOBJECT = "From Businessobject";
	public final static String LOG_TO_BUSINESSOBJECT = "To Businessobject";
	public final static String LOG_NOT_EXIST = "does not exist. Please specify valid from object.";
	
	//Schema Modeler Folder Structure
	public final static String FOLDER_NAME_SchemaObject = "SchemaObject";
	public final static String FOLDER_NAME_BusinessObject = "BusinessObject";
	public final static String FOLDER_NAME_Connection = "Connection";
	public final static String FOLDER_NAME_System = "System";
	public final static String FOLDER_NAME_Pno = "PnO";
	public final static String[] _folders = {FOLDER_NAME_SchemaObject, FOLDER_NAME_BusinessObject, FOLDER_NAME_Connection, FOLDER_NAME_System};
	
	//Compare Schema
	public final static String MATCH = "Match";
	public final static String DELTA = "Delta";
	public final static String UNIQUE = "Unique";
	public final static String COMPARE_MATCH = "Compare_Match";
	public final static String COMPARE_DELTA1 = "Compare_Delta1";
	public final static String COMPARE_DELTA2 = "Compare_Delta2";
	public final static String COMPARE_UNIQUE1 = "Compare_Unique1";
	public final static String COMPARE_UNIQUE2 = "Compare_Unique2";
	public final static String COMPARE_FILENAME = "admin_";
	public final static String MATCH_COUNT = "Match=";
	public final static String DELTA_COUNT1 = "Delta1=";
	public final static String DELTA_COUNT2 = "Delta2=";
	public final static String UNIQUE_COUNT1 = "Unique1=";
	public final static String UNIQUE_COUNT2 = "Unique2=";
}
