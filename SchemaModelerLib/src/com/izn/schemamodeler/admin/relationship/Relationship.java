package com.izn.schemamodeler.admin.relationship;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Relationship {

	String name;
	String description;
	String hidden;
	String registryname;
	String derived;
	String sparse;
	String sAbstract;
	String preventdups;
	ToType toType;
	FromType fromType;
	String attributes;
	List<Map<Object, Object>> slTriggers = new ArrayList<Map<Object, Object>>();
	List<Map> slFilterTrigger = new ArrayList<Map>();

	public Relationship(String name, String description, String hidden, String registryname) {
		super();
		this.name = name;
		this.description = description;
		this.registryname = registryname;
		this.hidden = (hidden.equalsIgnoreCase("true")) ? "hidden" : "!hidden";
	}

	public List<Map<Object, Object>> getSlTriggers() {
		return slTriggers;
	}

	public void setSlTriggers(List<Map<Object, Object>> slTriggers) {
		this.slTriggers = slTriggers;
	}

	public List<Map> getSlFilterTrigger() {
		return slFilterTrigger;
	}

	public void setSlFilterTrigger(List<Map> slFilterTrigger) {
		this.slFilterTrigger = slFilterTrigger;
	}

	public String getAttributes() {
		return attributes;
	}

	public void setAttributes(String attribute) {
		this.attributes = attribute;
	}

	public ToType getNewTotype() {
		return new ToType();
	}

	public ToType getToType() {
		return toType;
	}

	public void setToType(ToType totype) {
		this.toType = totype;
	}

	public FromType getNewFromType() {
		return new FromType();
	}

	public FromType getFromType() {
		return fromType;
	}

	public void setFromType(FromType fromType) {
		this.fromType = fromType;
	}

	public String getDerived() {
		return derived;
	}

	public void setDerived(String derived) {
		this.derived = derived;
	}

	public String getSparse() {
		return sparse;
	}

	public void setSparse(String sparse) {
		this.sparse = sparse;
	}

	public String getsAbstract() {
		return sAbstract;
	}

	public void setsAbstract(String sAbstract) {
		this.sAbstract = sAbstract;
	}

	public String getPreventdups() {
		return preventdups;
	}

	public void setPreventdups(String preventdups) {
		this.preventdups = (preventdups.equalsIgnoreCase("true")) ? "preventduplicates" : "notpreventduplicates";
	}

	public class FromType {

		String fromType;
		String fromRel;
		String fromMeaning;
		String fromCardinality;
		String fromRevision;
		String fromClone;
		String fromProModify;
		String fromProconnection;

		FromType() {

		}

		public String getFromType() {
			return fromType;
		}

		public void setFromType(String fromType) {
			this.fromType = fromType;
		}

		public String getFromRel() {
			return fromRel;
		}

		public void setFromRel(String fromRel) {
			this.fromRel = fromRel;
		}

		public String getFromMeaning() {
			return fromMeaning;
		}

		public void setFromMeaning(String fromMeaning) {
			this.fromMeaning = fromMeaning;
		}

		public String getFromCardinality() {
			return fromCardinality;
		}

		public void setFromCardinality(String fromCardinality) {
			this.fromCardinality = fromCardinality;
		}

		public String getFromRevision() {
			return fromRevision;
		}

		public void setFromRevision(String fromRevision) {
			this.fromRevision = fromRevision;
		}

		public String getFromClone() {
			return fromClone;
		}

		public void setFromClone(String fromClone) {
			this.fromClone = fromClone;
		}

		public String getFromProModify() {
			return fromProModify;
		}

		public void setFromProModify(String fromProModify) {
			this.fromProModify = (fromProModify.equalsIgnoreCase("true")) ? "propagatemodify" : "!propagatemodify";
		}

		public String getFromProconnection() {
			return fromProconnection;
		}

		public void setFromProconnection(String fromProconnection) {
			this.fromProconnection = (fromProconnection.equalsIgnoreCase("true")) ? "propagateconnection"
					: "!propagateconnection";
			;
		}
	}

	public class ToType {

		String toType;
		String toRel;
		String toMeaning;
		String toCardinality;
		String toRevision;
		String toClone;
		String toProModify;
		String toProconnection;

		ToType() {

		}

		public String getToType() {
			return toType;
		}

		public void setToType(String toType) {
			this.toType = toType;
		}

		public String getToRel() {
			return toRel;
		}

		public void setToRel(String toRel) {
			this.toRel = toRel;
		}

		public String getToMeaning() {
			return toMeaning;
		}

		public void setToMeaning(String toMeaning) {
			this.toMeaning = toMeaning;
		}

		public String getToCardinality() {
			return toCardinality;
		}

		public void setToCardinality(String toCardinality) {
			this.toCardinality = toCardinality;
		}

		public String getToRevision() {
			return toRevision;
		}

		public void setToRevision(String toRevision) {
			this.toRevision = toRevision;
		}

		public String getToClone() {
			return toClone;
		}

		public void setToClone(String toClone) {
			this.toClone = toClone;
		}

		public String getToProModify() {
			return toProModify;
		}

		public void setToProModify(String toProModify) {
			this.toProModify = (toProModify.equalsIgnoreCase("true")) ? "propagatemodify" : "!propagatemodify";
			;
		}

		public String getToProconnection() {
			return toProconnection;
		}

		public void setToProconnection(String toProconnection) {
			this.toProconnection = (toProconnection.equalsIgnoreCase("true")) ? "propagateconnection"
					: "!propagateconnection";
			;
		}

	}// end of ToType class

}
