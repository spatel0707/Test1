package com.izn.schemamodeler;

import com.izn.schemamodeler.admin.association.AssociationCompare;
import com.izn.schemamodeler.admin.attribute.AttributeCompare;
import com.izn.schemamodeler.admin.dimension.DimensionCompare;
import com.izn.schemamodeler.admin.format.FormatCompare;
import com.izn.schemamodeler.admin.group.GroupCompare;
import com.izn.schemamodeler.admin.inquiry.InquiryCompare;
import com.izn.schemamodeler.admin.interfaces.InterfaceCompare;
import com.izn.schemamodeler.admin.policy.PolicyCompare;
import com.izn.schemamodeler.admin.program.ProgramCompare;
import com.izn.schemamodeler.admin.relationship.RelationshipCompare;
import com.izn.schemamodeler.admin.role.RoleCompare;
import com.izn.schemamodeler.admin.type.TypeCompare;
import com.izn.schemamodeler.bo.BusinessObjectCompare;
import com.izn.schemamodeler.connection.ConnectionCompare;
import com.izn.schemamodeler.ui3.channel.ChannelCompare;
import com.izn.schemamodeler.ui3.command.CommandCompare;
import com.izn.schemamodeler.ui3.menu.MenuCompare;
import com.izn.schemamodeler.ui3.page.PageCompare;
import com.izn.schemamodeler.ui3.portal.PortalCompare;
import com.izn.schemamodeler.ui3.table.TableCompare;
import com.izn.schemamodeler.ui3.webform.WebFormCompare;



public class SchemaFactoryCompare {
	public SchemaCompare getSchemaCompare(String strSchemaName) {
		if (strSchemaName.equalsIgnoreCase("type")) {
			return new TypeCompare();
		} else if (strSchemaName.equalsIgnoreCase("attribute")) {
			return new AttributeCompare();
		} else if (strSchemaName.equalsIgnoreCase("relationship")) {
			return new RelationshipCompare();
		} else if (strSchemaName.equalsIgnoreCase("role")) {
			return new RoleCompare();
		} else if (strSchemaName.equalsIgnoreCase("policy")) {
			return new PolicyCompare();
		} else if (strSchemaName.equalsIgnoreCase("interface")) {
			return new InterfaceCompare();
		} else if (strSchemaName.equalsIgnoreCase("association")) {
			return new AssociationCompare();
		} else if (strSchemaName.equalsIgnoreCase("dimension")) {
			return new DimensionCompare();
		} else if (strSchemaName.equalsIgnoreCase("program")) {
			return new ProgramCompare();
		} else if (strSchemaName.equalsIgnoreCase("channel")) {
			return new ChannelCompare();
		} else if (strSchemaName.equalsIgnoreCase("command")) {
			return new CommandCompare();
		} else if (strSchemaName.equalsIgnoreCase("menu")) {
			return new MenuCompare();
		} else if (strSchemaName.equalsIgnoreCase("page")) {
			return new PageCompare();
		} else if (strSchemaName.equalsIgnoreCase("portal")) {
			return new PortalCompare();
		} else if (strSchemaName.equalsIgnoreCase("table")) {
			return new TableCompare();
		} else if (strSchemaName.equalsIgnoreCase("form")) {
			return new WebFormCompare();
		} else if (strSchemaName.equalsIgnoreCase("webform")) {
			return new WebFormCompare();
		} else if (strSchemaName.equalsIgnoreCase("businessobject")) {
			return new BusinessObjectCompare();
		} else if (strSchemaName.equalsIgnoreCase("connection")) {
			return new ConnectionCompare();
		} else if (strSchemaName.equalsIgnoreCase("group")) {
			return new GroupCompare();
		} else if (strSchemaName.equalsIgnoreCase("inquiry")) {
			return new InquiryCompare();
		} else if (strSchemaName.equalsIgnoreCase("format")) {
			return new FormatCompare();
		}

		throw new IllegalArgumentException("No such Schema Exits");
	}

}
