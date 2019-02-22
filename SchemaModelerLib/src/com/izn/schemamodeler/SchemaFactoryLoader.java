package com.izn.schemamodeler;

import com.izn.schemamodeler.admin.association.AssociationLoader;
import com.izn.schemamodeler.admin.attribute.AttributeLoader;
import com.izn.schemamodeler.admin.dimension.DimensionLoader;
import com.izn.schemamodeler.admin.format.FormatLoader;
import com.izn.schemamodeler.admin.group.GroupLoader;
import com.izn.schemamodeler.admin.inquiry.InquiryLoader;
import com.izn.schemamodeler.admin.interfaces.InterfaceLoader;
import com.izn.schemamodeler.admin.policy.PolicyLoader;
import com.izn.schemamodeler.admin.program.ProgramLoader;
import com.izn.schemamodeler.admin.relationship.RelationshipLoader;
import com.izn.schemamodeler.admin.role.RoleLoader;
import com.izn.schemamodeler.admin.type.TypeLoader;
//import com.izn.schemamodeler.bo.businessobjectgenerator.BusinessObjectLoader;
import com.izn.schemamodeler.bo.BusinessObjectLoader;
import com.izn.schemamodeler.connection.ConnectionLoader;
import com.izn.schemamodeler.system.index.IndexLoader;
import com.izn.schemamodeler.system.store.StoreLoader;
import com.izn.schemamodeler.system.vault.VaultLoader;
import com.izn.schemamodeler.ui3.channel.ChannelLoader;
import com.izn.schemamodeler.ui3.command.CommandLoader;
import com.izn.schemamodeler.ui3.menu.MenuLoader;
import com.izn.schemamodeler.ui3.page.PageLoader;
import com.izn.schemamodeler.ui3.portal.PortalLoader;
import com.izn.schemamodeler.ui3.table.TableLoader;
import com.izn.schemamodeler.ui3.webform.WebFormLoader;



public class SchemaFactoryLoader {
	public SchemaLoader getSchemaLoader(String strSchemaName) {
		if (strSchemaName.equalsIgnoreCase("type")) {
			return new TypeLoader();
		} else if (strSchemaName.equalsIgnoreCase("attribute")) {
			return new AttributeLoader();
		} else if (strSchemaName.equalsIgnoreCase("relationship")) {
			return new RelationshipLoader();
		} else if (strSchemaName.equalsIgnoreCase("role")) {
			return new RoleLoader();
		} else if (strSchemaName.equalsIgnoreCase("policy")) {
			return new PolicyLoader();
		} else if (strSchemaName.equalsIgnoreCase("interface")) {
			return new InterfaceLoader();
		} else if (strSchemaName.equalsIgnoreCase("association")) {
			return new AssociationLoader();
		} else if (strSchemaName.equalsIgnoreCase("dimension")) {
			return new DimensionLoader();
		} else if (strSchemaName.equalsIgnoreCase("program")) {
			return new ProgramLoader();
		} else if (strSchemaName.equalsIgnoreCase("channel")) {
			return new ChannelLoader();
		} else if (strSchemaName.equalsIgnoreCase("command")) {
			return new CommandLoader();
		} else if (strSchemaName.equalsIgnoreCase("menu")) {
			return new MenuLoader();
		} else if (strSchemaName.equalsIgnoreCase("page")) {
			return new PageLoader();
		} else if (strSchemaName.equalsIgnoreCase("portal")) {
			return new PortalLoader();
		} else if (strSchemaName.equalsIgnoreCase("table")) {
			return new TableLoader();
		} else if (strSchemaName.equalsIgnoreCase("form")) {
			return new WebFormLoader();
		} else if (strSchemaName.equalsIgnoreCase("webform")) {
			return new WebFormLoader();
		} else if (strSchemaName.equalsIgnoreCase("businessobject")) {
			return new BusinessObjectLoader();
		} else if (strSchemaName.equalsIgnoreCase("connection")) {
			return new ConnectionLoader();
		} else if (strSchemaName.equalsIgnoreCase("group")) {
			return new GroupLoader();
		} else if (strSchemaName.equalsIgnoreCase("inquiry")) {
			return new InquiryLoader();
		} else if (strSchemaName.equalsIgnoreCase("format")) {
			return new FormatLoader();
		}
		else if (strSchemaName.equalsIgnoreCase("vault")) {
			return new VaultLoader();
		}
		else if (strSchemaName.equalsIgnoreCase("index")) {
			return new IndexLoader();
		}
		else if (strSchemaName.equalsIgnoreCase("store")) {
			return new StoreLoader();
		}
		throw new IllegalArgumentException("No such Schema Exits");
	}

}
