package com.izn.schemamodeler;

import com.izn.schemamodeler.admin.association.AssociationInfo;
import com.izn.schemamodeler.admin.attribute.AttributeInfo;
import com.izn.schemamodeler.admin.dimension.DimensionInfo;
import com.izn.schemamodeler.admin.format.FormatInfo;
import com.izn.schemamodeler.admin.group.GroupInfo;
import com.izn.schemamodeler.admin.inquiry.InquiryInfo;
import com.izn.schemamodeler.admin.interfaces.InterfaceInfo;
import com.izn.schemamodeler.admin.policy.PolicyInfo;
import com.izn.schemamodeler.admin.program.ProgramInfo;
import com.izn.schemamodeler.admin.relationship.RelationshipInfo;
import com.izn.schemamodeler.admin.role.RoleInfo;
import com.izn.schemamodeler.admin.rule.RuleInfo;
import com.izn.schemamodeler.admin.type.TypeInfo;
import com.izn.schemamodeler.bo.BusinessobjectInfo;
import com.izn.schemamodeler.connection.ConnectionInfo;
import com.izn.schemamodeler.system.index.IndexInfo;
import com.izn.schemamodeler.system.store.StoreInfo;
import com.izn.schemamodeler.system.vault.VaultInfo;
import com.izn.schemamodeler.ui3.channel.ChannelInfo;
import com.izn.schemamodeler.ui3.command.CommandInfo;
import com.izn.schemamodeler.ui3.menu.MenuInfo;
import com.izn.schemamodeler.ui3.page.PageInfo;
import com.izn.schemamodeler.ui3.portal.PortalInfo;
import com.izn.schemamodeler.ui3.table.TableInfo;
import com.izn.schemamodeler.ui3.webform.WebFormInfo;

 

public class SchemaFactoryInfo {

  public SchemaInfo getSchemaObject(String strSchemaName) {
	  
	  if(strSchemaName.equalsIgnoreCase("type")){
		  return new TypeInfo();
	  }else if(strSchemaName.equalsIgnoreCase("attribute")){
		  return new AttributeInfo();
	  }else if(strSchemaName.equalsIgnoreCase("relationship")){
		  return new RelationshipInfo();
	  }else if(strSchemaName.equalsIgnoreCase("role")){
		  return new RoleInfo();
	  }else if(strSchemaName.equalsIgnoreCase("policy")){
		  return new PolicyInfo();
	  }else if(strSchemaName.equalsIgnoreCase("interface")){
		  return new InterfaceInfo();
	  }else if(strSchemaName.equalsIgnoreCase("association")){
		  return new AssociationInfo();
	  }else if(strSchemaName.equalsIgnoreCase("dimension")){
		  return new DimensionInfo();
	  }else if(strSchemaName.equalsIgnoreCase("program")){
		  return new ProgramInfo();
	  }else if(strSchemaName.equalsIgnoreCase("channel")){
		  return new ChannelInfo();
	  }else if(strSchemaName.equalsIgnoreCase("command")){
		  return new CommandInfo();
	  }else if(strSchemaName.equalsIgnoreCase("menu")){
		  return new MenuInfo();
	  }else if(strSchemaName.equalsIgnoreCase("page")){
		  return new PageInfo();
	  }else if(strSchemaName.equalsIgnoreCase("portal")){
		  return new PortalInfo();
	  }else if(strSchemaName.equalsIgnoreCase("table")){
		  return new TableInfo();
	  }else if(strSchemaName.equalsIgnoreCase("form")){
		  return new WebFormInfo();
	  }else if(strSchemaName.equalsIgnoreCase("businessobject")){
		  return new BusinessobjectInfo();
	  }else if(strSchemaName.equalsIgnoreCase("connection")){
		  return new ConnectionInfo();
	  }else if(strSchemaName.equalsIgnoreCase("format")){
		  return new FormatInfo();
	  }else if(strSchemaName.equalsIgnoreCase("group")){
		  return new GroupInfo();
	  }else if(strSchemaName.equalsIgnoreCase("rule")){
		  return new RuleInfo();
	  }else if(strSchemaName.equalsIgnoreCase("inquiry")){
		  return new InquiryInfo();
	  }
	  else if(strSchemaName.equalsIgnoreCase("vault")) {
		  return new VaultInfo();
	  }
	  else if(strSchemaName.equalsIgnoreCase("index")) {
		  return new IndexInfo();
	  }
	  else if(strSchemaName.equalsIgnoreCase("store")) {
		  return new StoreInfo();
	  }
	  
 	  
	  throw new IllegalArgumentException("No such Schema Exits");
  }
	 

}
