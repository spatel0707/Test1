package com.izn.schemamodeler.ui3.webform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebForm {
	String name;
	String oldName;
	String description;
	String registryname;
	String hidden;
	String type;
	WebForm.Column[] wbColumn;

	public WebForm(String name, String description, String hidden, String registryname,String type) {
		super();
		this.name = name;
		this.description = description;
		this.registryname = registryname;
		this.hidden = (hidden.equalsIgnoreCase("true")) ? "hidden" : "!hidden";
		this.type = type;
	}

	public String getOldName() {
		return oldName;
	}

	public void setOldName(String oldName) {
		this.oldName = oldName;
	}

	public WebForm.Column[] getWbColumn() {
		return wbColumn;
	}

	public void setWbColumn(WebForm.Column[] wbColumn) {
		this.wbColumn = wbColumn;
	}

	List<Column> lstColumn = new ArrayList<Column>();
	Map<String,Column> lstMapColumn = new HashMap<String,Column>();

	public Map<String, Column> getLstMapColumn() {
		return lstMapColumn;
	}

	public void setLstMapColumn(Map<String, Column> lstMapColumn) {
		this.lstMapColumn = lstMapColumn;
	}

	public WebForm() {
		// TODO Auto-generated constructor stub
	}

	public Column getColumnObejct() {

		return new Column();

	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Column> getLstColumn() {
		return lstColumn;
	}

	public void setLstColumn(List<Column> lstColumn) {
		this.lstColumn = lstColumn;
	}

	public class Column implements Comparable<Column> {

		String columnName;
		String label;
		String description;
		String columnType;
		String expression;
		String href;
		String alt;
		String range;
		String update;
		String sortType;
		String user;
		int order;
		List<Map<String, String>> lstSetting = new ArrayList<Map<String, String>>();
		List<Map<String, String>> lstAccessDetail = new ArrayList<Map<String, String>>();

		public Column() {
			// TODO Auto-generated constructor stub
		}

		public String getColumnName() {
			return columnName;
		}

		public void setColumnName(String column) {
			this.columnName = column;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getColumnType() {
			return columnType;
		}

		public void setColumnType(String columnType) {
			this.columnType = columnType;
		}

		public String getExpression() {
			return expression;
		}

		public void setExpression(String expression) {
			this.expression = expression;
		}

		public String getHref() {
			return href;
		}

		public void setHref(String href) {
			this.href = href;
		}

		public String getAlt() {
			return alt;
		}

		public void setAlt(String alt) {
			this.alt = alt;
		}

		public String getRange() {
			return range;
		}

		public void setRange(String range) {
			this.range = range;
		}

		public String getUpdate() {
			return update;
		}

		public void setUpdate(String update) {
			this.update = update;
		}

		public String getSortType() {
			return sortType;
		}

		public void setSortType(String sortType) {
			this.sortType = sortType;
		}

		public String getUser() {
			return user;
		}

		public void setUser(String user) {
			this.user = user;
		}

		public int getOrder() {
			return order;
		}

		public void setOrder(int order) {
			this.order = order;
		}

		public List<Map<String, String>> getLstSetting() {
			return lstSetting;
		}

		public void setLstSetting(List<Map<String, String>> lstSetting) {
			this.lstSetting = lstSetting;
		}

		public List<Map<String, String>> getLstAccessDetail() {
			return lstAccessDetail;
		}

		public void setLstAccessDetail(List<Map<String, String>> lstAccessDetail) {
			this.lstAccessDetail = lstAccessDetail;
		}

		@Override
		public int compareTo(Column column) {
			int iColumnOrder = ((Column) column).getOrder();

			return this.order - iColumnOrder;
		}

	}
}
