/*Association.java

Copyright (c) 2018-2019 Intelizign Engineering Services Pvt Ltd.
All Rights Reserved.

This program contains proprietary and trade secret information of
Intelizign Engineering Services, Inc. Copyright notice is precautionary only and does
not evidence any actual or intended publication of such program.

*/

package com.izn.schemamodeler.admin.association;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Class Asociation.
 */
public class Association {
    /** The association name. */
	String name;
	
	/** The association hidden. */
	String hidden;
	
	/** The association description. */
	String description;
	
	/** The association definition. */
	String definition;
	
	/** The association registryname. */
	String registryname;
	
	/**
	 * Instantiates a new association.
	 *
	 * @param name the association name
	 * @param description the association description
	 * @param hidden the association hidden
	 * @param registryname the association registryname
	 */
	public Association(String name, String description, String hidden, String registryname) {
		super();
		this.name = name;
		this.hidden = (hidden.equalsIgnoreCase("true")) ? "hidden" : "!hidden" ;
		this.description = description;
		this.registryname = registryname;
	}
	
	/**
	 * Gets the association definition.
	 *
	 * @return the association definition
	 */
	public String getDefinition() {
		return definition;
	}
	
	/**
	 * Sets the association definition.
	 *
	 * @param definition the new definition
	 */
	public void setDefinition(String definition) {
		this.definition = definition;
	}	
 	
}
