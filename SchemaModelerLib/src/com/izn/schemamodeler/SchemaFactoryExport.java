package com.izn.schemamodeler;

import com.izn.schemamodeler.admin.association.AssociationXMLExport;
import com.izn.schemamodeler.admin.attribute.AttributeXMLExport;
import com.izn.schemamodeler.admin.policy.PolicyXMLExport;
import com.izn.schemamodeler.admin.relationship.RelationshipXMLExport;
import com.izn.schemamodeler.admin.role.RoleXMLExport;
import com.izn.schemamodeler.admin.rule.RuleXMLExport;
import com.izn.schemamodeler.admin.type.TypeXMLExport;
import com.izn.schemamodeler.bo.BusinessObjectXMLExport;
import com.izn.schemamodeler.connection.ConnectionXMLExport;
import com.izn.schemamodeler.system.index.IndexInfo;
import com.izn.schemamodeler.system.index.IndexXMLExport;
import com.izn.schemamodeler.system.store.StoreXMLExport;
import com.izn.schemamodeler.system.vault.VaultInfo;
import com.izn.schemamodeler.system.vault.VaultXMLExport;
import com.izn.schemamodeler.system.vault.VaultXMLSchema;
import com.izn.schemamodeler.admin.interfaces.InterfaceXMLExport;
import com.izn.schemamodeler.admin.dimension.DimensionXMLExport;
import com.izn.schemamodeler.admin.format.FormatXMLExport;
import com.izn.schemamodeler.admin.group.GroupXMLExport;
import com.izn.schemamodeler.admin.inquiry.InquiryXMLExport;
import com.izn.schemamodeler.admin.program.ProgramXMLExport;
import com.izn.schemamodeler.ui3.channel.ChannelXMLExport;
import com.izn.schemamodeler.ui3.command.CommandXMLExport;
import com.izn.schemamodeler.ui3.menu.MenuXMLExport;
import com.izn.schemamodeler.ui3.page.PageXMLExport;
import com.izn.schemamodeler.ui3.portal.PortalXMLExport;
import com.izn.schemamodeler.ui3.table.TableXMLExport;
import com.izn.schemamodeler.ui3.webform.WebFormXMLExport;

import matrix.db.Context;

public class SchemaFactoryExport {
	public SchemaExport getSchemaXML(Context context, String strSchemaName) {
		if (strSchemaName.equalsIgnoreCase("type")) {
			return new TypeXMLExport();
		} else if (strSchemaName.equalsIgnoreCase("attribute")) {
			return new AttributeXMLExport();
		} else if (strSchemaName.equalsIgnoreCase("relationship")) {
			return new RelationshipXMLExport();
		} else if (strSchemaName.equalsIgnoreCase("role")) {
			return new RoleXMLExport();
		} else if (strSchemaName.equalsIgnoreCase("policy")) {
			return new PolicyXMLExport();
		} else if (strSchemaName.equalsIgnoreCase("interface")) {
			return new InterfaceXMLExport();
		} else if (strSchemaName.equalsIgnoreCase("association")) {
			return new AssociationXMLExport();
		} else if (strSchemaName.equalsIgnoreCase("dimension")) {
			return new DimensionXMLExport();
		} else if (strSchemaName.equalsIgnoreCase("program")) {
			return new ProgramXMLExport();
		} else if (strSchemaName.equalsIgnoreCase("channel")) {
			return new ChannelXMLExport();
		} else if (strSchemaName.equalsIgnoreCase("command")) {
			return new CommandXMLExport();
		} else if (strSchemaName.equalsIgnoreCase("menu")) {
			return new MenuXMLExport();
		} else if (strSchemaName.equalsIgnoreCase("page")) {
			return new PageXMLExport();
		} else if (strSchemaName.equalsIgnoreCase("portal")) {
			return new PortalXMLExport();
		} else if (strSchemaName.equalsIgnoreCase("table")) {
			return new TableXMLExport();
		} else if (strSchemaName.equalsIgnoreCase("form")) {
			return new WebFormXMLExport();
		} else if (strSchemaName.equalsIgnoreCase("format")) {
			return new FormatXMLExport();
		} else if (strSchemaName.equalsIgnoreCase("group")) {
			return new GroupXMLExport();
		} else if (strSchemaName.equalsIgnoreCase("rule")) {
			return new RuleXMLExport();
		} else if (strSchemaName.equalsIgnoreCase("inquiry")) {
			return new InquiryXMLExport();
		}
		else if (strSchemaName.equalsIgnoreCase("vault")) {
			return new VaultXMLExport();
		}
		else if (strSchemaName.equalsIgnoreCase("store")) {
			return new StoreXMLExport();
		}
		else if(strSchemaName.equalsIgnoreCase("index")) {
			  return new IndexXMLExport();
		}

		throw new IllegalArgumentException("No such Schema Exits");
	}

	public SchemaBusExport getBusSchemaXML(Context context, String strSchemaName) {
		
		if (strSchemaName.equalsIgnoreCase("businessobject")) {
			return new BusinessObjectXMLExport();
		} else if (strSchemaName.equalsIgnoreCase("connection")) {
			return new ConnectionXMLExport();
		}
		throw new IllegalArgumentException("No such Schema Exits");
	}

}
