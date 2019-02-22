package com.izn.schemamodeler.admin.policy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Policy {

	String name;
	String description;
	String hidden;
	String store;
 	String minorsequence;
 	String type;
 	String format;
 	String defaultformat;
 	String enforcelocking;
 	String allstateenabled; 
 	String registryname;
 	List<Map> lstAllState=new ArrayList<Map>();
 	List<State> lstState=new ArrayList<State>();
 	Map<String,State> lstMapState = new HashMap<String,State>();
	public Policy(String name, String description, String hidden, String registryname) {
		super();
		this.name = name;
		this.description = description;
		this.registryname = registryname;
		this.hidden = (hidden.endsWith("true")) ? "hidden" : "!hidden";
	}
		

	public Map<String, State> getLstMapState() {
		return lstMapState;
	}

	public void setLstMapState(Map<String, State> lstMapState) {
		this.lstMapState = lstMapState;
	}
	
	public String getStore() {
		return store;
	}

	public void setStore(String store) {
		this.store = store;
	}

	public String getMinorsequence() {
		return minorsequence;
	}

	public void setMinorsequence(String minorsequence) {
		this.minorsequence = minorsequence;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getDefaultformat() {
		return defaultformat;
	}

	public void setDefaultformat(String defaultformat) {
		this.defaultformat = defaultformat;
	}

	public String getEnforcelocking() {
		return   enforcelocking;
	}

	public void setEnforcelocking(String enforcelocking) {
		this.enforcelocking =  (enforcelocking.equalsIgnoreCase("true")) ? "enforce" : "notenforce";
	}

	public String getAllstateenabled() {
		return allstateenabled;
	}

	public void setAllstateenabled(String allstateenabled) {
		this.allstateenabled = allstateenabled;
	}

		
	public List<Map> getLstAllState() {
		return lstAllState;
	}

	public void setLstAllState(List<Map> lstAllState) {
		this.lstAllState = lstAllState;
	}

     public State getNewStateIntance(String strName,String strRegName){
    	 return new State(strName,strRegName);
     }

     
	public List<State> getLstState() {
		return lstState;
	}

	public void setLstState(List<State> lstState) {
		this.lstState = lstState;
	}


	public class State{
		
		String statename;
		String regname;
		String promote;
		String version;
		String checkouthistory;
		String minorrevisionable;
		List<Map<String, String>> slTriggers= new ArrayList<Map<String, String>>();
		List<Map<String, String>> slAccess= new ArrayList<Map<String, String>>();
		List<Map> slFilterTrigger= new ArrayList<Map>();
		List<Signature> lstSignature=new ArrayList<Signature>();
	 	Map<String,Signature> lstMapStateSignature = new HashMap<String,Signature>();
		int order;
		
		public State(String statename, String regname) {
			super();
			this.statename = statename;
			this.regname = regname;
		}
		
		
		public Map<String, Signature> getLstMapStateSignature() {
			return lstMapStateSignature;
		}


		public void setLstMapStateSignature(Map<String, Signature> lstMapStateSignature) {
			this.lstMapStateSignature = lstMapStateSignature;
		}


		public String getPromote() {
			return promote;
		}
		public void setPromote(String promote) {
			this.promote = promote;
		}
		public String getVersion() {
			return version;
		}
		public void setVersion(String version) {
			this.version = version;
		}
		public String getCheckouthistory() {
			return checkouthistory;
		}
		public void setCheckouthistory(String checkouthistory) {
			this.checkouthistory = checkouthistory;
		}
		public String getMinorrevisionable() {
			return minorrevisionable;
		}
		public void setMinorrevisionable(String minorrevisionable) {
			this.minorrevisionable = minorrevisionable;
		}


		public List<Map<String, String>> getSlTriggers() {
			return slTriggers;
		}


		public void setSlTriggers(List<Map<String, String>> slTriggers) {
			this.slTriggers = slTriggers;
		}


		public List<Map> getSlFilterTrigger() {
			return slFilterTrigger;
		}


		public void setSlFilterTrigger(List<Map> slFilterTrigger) {
			this.slFilterTrigger = slFilterTrigger;
		}
		
		
		public List<Map<String, String>> getSlAccess() {
			return slAccess;
		}


		public void setSlAccess(List<Map<String, String>> slAccess) {
			this.slAccess = slAccess;
		}

         public Signature getNewSignatureInstance(String name, String fromstate, String tostate ){
        	 
        	return new Signature(name,fromstate,tostate); 
         }

         
         
		public List<Signature> getLstSignature() {
			return lstSignature;
		}


		public void setLstSignature(List<Signature> lstSignature) {
			this.lstSignature = lstSignature;
		}



		class Signature {
			
			String name="";
			String fromstate="";
			String tostate="";
			String branch="";
			String filter="";
			String approve="";
			String reject="";
			String ignore="";
			
			List<Map> lstSignDetails = new ArrayList<Map>();
	 
			public Signature(String name, String fromstate, String tostate) {
				super();
				this.name = name;
				this.fromstate = fromstate;
				this.tostate = tostate;
			}
	 		public List<Map> getLstSignDetails() {
				return lstSignDetails;
			}
	 		
      	   public void setLstSignDetails(List<Map> lstSignDetails) {
				this.lstSignDetails = lstSignDetails;
			}
		public String getBranch() {
			return branch;
		}
		public void setBranch(String branch) {
			this.branch = branch;
		}
		public String getFilter() {
			return filter;
		}
		public void setFilter(String filter) {
			this.filter = filter;
		}
		public String getApprove() {
			return approve;
		}
		public void setApprove(String approve) {
			this.approve = approve;
		}
		public String getReject() {
			return reject;
		}
		public void setReject(String reject) {
			this.reject = reject;
		}
		public String getIgnore() {
			return ignore;
		}
		public void setIgnore(String ignore) {
			this.ignore = ignore;
		}

				
		} 
		
		
		
		
	}
	
}
