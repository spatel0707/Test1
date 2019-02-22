package com.ws.sh;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.dassault_systemes.platform.restServices.RestService;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.json.JSONObject;

import matrix.db.Context;
import matrix.util.Pattern;
import matrix.util.StringList;

public class Service extends RestService {

	@POST
	@Path("/getDMSDashboardDetails")
	public Response getDMSDashboardDetails(@javax.ws.rs.core.Context HttpServletRequest request,
			@QueryParam("objectId") String objectId) {
		System.out.println("JPO Called::::::::::");
		JSONObject output = null;
		Context context = null;
		Map mp=new HashMap();
		try {
			// context = getAuthenticatedContext(request, false);
			context = getContext("https://cpms.apcrda.com/internal", "admin_platform", "Intelizign123");
			mp = getDMSTypeStates(context, objectId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(mp).build();
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	public Map getDMSTypeStates(Context context, String objectId) throws Exception 
	{
		LinkedHashMap dmsDashboardMap=new LinkedHashMap<String,Map>();
		System.out.println("objectId:::::::::::::::" + objectId);
		String count = DomainObject.EMPTY_STRING;
		String strFirstPolicy = DomainConstants.EMPTY_STRING;
		String keyType = DomainConstants.EMPTY_STRING;
		String keyPolicy = DomainConstants.EMPTY_STRING;
		String strStateFromPolicy = DomainConstants.EMPTY_STRING;
		JSONObject jsonStateCount = null;
		LinkedHashMap<String, String> mpFinal = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> m1 = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> m2 = new LinkedHashMap<String, String>();
		DomainObject domProjectSpace = DomainObject.newInstance(context, objectId);
		String strCreatedDate = domProjectSpace.getCreated(context);
		String strTypes=EnoviaResourceBundle.getProperty(context,"DMS.Dashboard.Report.Types");
		//String strTypes = "DMSDrawing~Drawing|DMSSampleAndDatasheets~Sample and Datasheets|DMSPlansAndProcedures~Plans and Procedures|DMSClosureDocuments~Closure Documents|DMSRFI~RFI|DMSTransmittal~Transmittal|DMSSIR~SIR|DMSLetter~Letter";
		String[] strDMSTypes = strTypes.split("\\|");
		Map<String, String> m = new HashMap<String, String>();
		String[] strSplitedDMSTypes;
		for (String strToken : strDMSTypes) {
			strSplitedDMSTypes = strToken.split("~");
			for (int i = 0; i < strSplitedDMSTypes.length - 1; i += 2) {
				m.put(strSplitedDMSTypes[i], strSplitedDMSTypes[i + 1]);
			}
		}
		Iterator itr = m.keySet().iterator();
		while (itr.hasNext()) {
			keyType = (String) itr.next();
			String strPolicyFromType = MqlUtil.mqlCommand(context,
					"print type " + keyType + " " + "select policy dump");
			String[] strArrPolicyFromType = strPolicyFromType.split(",");
			strFirstPolicy = strArrPolicyFromType[0];
			m1.put(keyType, strFirstPolicy);
			Iterator itr1 = m1.values().iterator();
			while (itr1.hasNext()) {
				keyPolicy = (String) itr1.next();
				strStateFromPolicy = MqlUtil.mqlCommand(context,
						"print type " + keyType + " " + "select policy[" + keyPolicy + "].state dump");

			}

			m2.put(keyPolicy + ":" + keyType, strStateFromPolicy);
		}
		
	     final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
	     Calendar cal = Calendar.getInstance();
	     String strTodaysDate=sdf.format(cal.getTime());
	     LinkedHashMap lhmDayObjectCount=new LinkedHashMap<String,Integer>();
	     LinkedHashMap lhmWeekObjectCount=new LinkedHashMap<String,Integer>();
	     LinkedHashMap lhmMonthObjectCount=new LinkedHashMap<String,Integer>();
	 
	     
	     
	     
	     for(String i:m.keySet())
	     {
	    	 LinkedHashMap mpTypeObjects=new LinkedHashMap<String,Map>();
	    	 LinkedHashMap ObjectsCount = new LinkedHashMap<String, String>();
	    	 int countTodayObjects=0;
	         int countWeekObjects=0;
	         int countMonthObjects=0;
	    	 ObjectsCount.put("Raised Today", Integer.toString(countTodayObjects));
	    	 ObjectsCount.put("Raised This Week", Integer.toString(countWeekObjects));
	    	 ObjectsCount.put("Raised This Month", Integer.toString(countMonthObjects));
    		 mpTypeObjects.put("NumberOfObjects", ObjectsCount);
    		 dmsDashboardMap.put(m.get(i), mpTypeObjects);
	    	 //System.out.println("Types are::::::::::"+i);
	    	 
	    	 mpTypeObjects.put("NumberOfObjects", ObjectsCount);
	    	 
	    	 StringList objectSelects = new StringList();
	          objectSelects.add("originated");
	          String objectWhere = null;
	          MapList mlObjects = DomainObject.findObjects(context,
	        		  i,                                 // type filter
	                  "*",         // vault filter
	                  objectWhere,                            // where clause
	                  objectSelects);                         // object selects
	          //System.out.println("mlObjects::::::::::::"+mlObjects);
	          Map mMap=new LinkedHashMap();
	          
	          Calendar cal1=Calendar.getInstance();
	          cal1.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
	          String strWeekStartDay=sdf.format(cal1.getTime());
	          for (int k = 0; k <6; k++) {
	              cal1.add(Calendar.DATE, 1);
	                }
	          String strWeekLastDay=sdf.format(cal1.getTime());
	          
	          for(int j=0;j<mlObjects.size();j++)
	          {
	        	  
	        	  mMap =  (Map) mlObjects.get(j);
	        	  
			    	 String strOriginated =  (String) mMap.get("originated");
			    	 strOriginated=strOriginated.split(" ")[0];
			    	 SimpleDateFormat sdfEnovia = new SimpleDateFormat("MM/dd/yyyy");
			    	 Date strOriginatedDate = (Date)sdfEnovia.parse(strOriginated);
			    	 String strOriginatedDate1 = sdf.format(strOriginatedDate);
			    	 if(strTodaysDate.equals(strOriginatedDate1))
			    	 {
			    		 countTodayObjects++;
			    		 ObjectsCount.put("Raised Today", Integer.toString(countTodayObjects));
			    		 mpTypeObjects.put("NumberOfObjects", ObjectsCount);
			    		 
			    	 }
			    	 if(strOriginatedDate1.compareTo(strWeekStartDay)>0 && strOriginatedDate1.compareTo(strWeekLastDay)<0)
			    	 {
			    		 countWeekObjects++;
			    		 ObjectsCount.put("Raised This Week", Integer.toString(countWeekObjects));
			    		 mpTypeObjects.put("NumberOfObjects", ObjectsCount);
			    	 }
			    	 Calendar cal2 = Calendar.getInstance();
			    	 Date d1=sdf.parse(strOriginatedDate1);
			    	 cal2.setTime(d1);
			    	 if(cal1.get(Calendar.MONTH)==cal2.get(Calendar.MONTH))
			    	 {
			    		 countMonthObjects++;
			    		 ObjectsCount.put("Raised This Month", Integer.toString(countMonthObjects));
			    		 mpTypeObjects.put("NumberOfObjects", ObjectsCount);
			    	 }
	          }
	          
	     }
	     LinkedHashMap mapTypeStates = new LinkedHashMap<String, LinkedHashMap<String, String>>();
		
		
		for (String i : m.keySet()) {
	    	 
	    	 for (String policyName:m2.keySet()){
	    		 String strType = i;
	    		 LinkedHashMap mapStateCount = new LinkedHashMap<String, String>(); 
	    		 if ( policyName.split(":")[0].equals(m1.get(i))){
	    			 String strPolicyName1 = m1.get(i);
	    			 String[] strArrStates=m2.get(policyName).split(",");
	    			 for(String strState:strArrStates) 
	    			 {
	    				 count=MqlUtil.mqlCommand(context, "eval expression COUNT=true on temp query bus "+m1.get(i)+" * * where 'current==\""+strState+"\"'");
	    				 mpFinal.put(i+":"+strState, count);
	    				 mapStateCount.put(strState, count);
	    			 }
	    			 LinkedHashMap tempMap = new LinkedHashMap<String, Map>();
	    			 tempMap = (LinkedHashMap) dmsDashboardMap.get(m.get(strType));
   				 tempMap.put("statescount", mapStateCount);
	    			 if( dmsDashboardMap.containsKey(m.get(strType)))
	    			 {
	    				 dmsDashboardMap.put(m.get(strType), tempMap);
	    			 }
	    			 else 
	    			 {
	    				 dmsDashboardMap.put(m.get(strType), tempMap);
	    			 }
	    			 mapTypeStates.put(m.get(strType), mapStateCount);
	    		 }
	    		 
	    	 }
	     }
		return dmsDashboardMap;
	}

	public matrix.db.Context getContext(String serverURL, String user, String password) throws Exception {
		matrix.db.Context eMatrixContext = null;
		try {
			eMatrixContext = new matrix.db.Context(serverURL);

			eMatrixContext.setUser(user);
			eMatrixContext.setPassword(password);
			eMatrixContext.connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return eMatrixContext;
	}

	// Web Service to get WMSDashboard Details
	@POST
	@Path("/getWMSDashboardDetails")
	public Response getWMSDashboardDetails(@javax.ws.rs.core.Context HttpServletRequest request,
			@QueryParam("objectId") String objectId) {
		System.out.println("getWMSDashboardDetails JPO Called::::::::::");
		JSONObject output = null;
		Context context = null;
		try {
			// context = getAuthenticatedContext(request, false);
			context = getContext("https://cpms.apcrda.com/internal", "admin_platform", "Intelizign123");
			Map mp = getWMSDashboardDetails(context, objectId);
			System.out.println("objectId::::::::" + objectId);
			output = new JSONObject(mp);
			System.out.println("ouptput:::::::::::" + output);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.status(200).entity(output.toString()).build();
	}

	public Map getWMSDashboardDetails(Context context, String objectId) throws Exception {
		String strProjectId = objectId;
		DomainObject domProjectSpace = DomainObject.newInstance(context, strProjectId);
		String strWorkOrderID = (String) domProjectSpace.getInfo(context, "relationship[WMSProjectWorkOrder].to.id");
		DomainObject domWorkOrder = DomainObject.newInstance(context, strWorkOrderID);
		// Calculate Physical Progress

		StringList strListBusSelects = new StringList();
		strListBusSelects.add(DomainConstants.SELECT_ID);
		strListBusSelects.add("attribute[WMSTotalQuantity]");
		strListBusSelects.add("attribute[WMSMBEQuantity]");
		strListBusSelects.add("attribute[WMSReducedSORRate]");
		strListBusSelects.add("attribute[WMSQtyPaidTillDate]");
		StringList strListRelSelects = new StringList();
		strListRelSelects.add(DomainRelationship.SELECT_ID);
		Pattern patternType = new Pattern("WMSMeasurementTask");
		patternType.addPattern("WMSSegment");
		patternType.addPattern("WMSMeasurementBook");
		patternType.addPattern("WMSWorkOrder");

		Pattern patternRel = new Pattern("WMSMeasurementBookItems");

		MapList mlBOQ = domWorkOrder.getRelatedObjects(context, patternRel.getPattern(), patternType.getPattern(), true,
				true, (short) 0, strListBusSelects, strListRelSelects, DomainConstants.EMPTY_STRING,
				DomainConstants.EMPTY_STRING, (short) 0, DomainConstants.EMPTY_STRING, "WMSMeasurementTask", null);
		int sumQuantity = 0;
		int sumQtySubmittedTillDate = 0;
		int physicalProgress = 0;
		int sumQuantityRate = 0;
		int sumQtyPaidTillDateRate = 0;
		for (int i = 0; i < mlBOQ.size(); i++) {
			Map mMap = (Map) mlBOQ.get(i);
			sumQuantity = sumQuantity + (int) (Double.parseDouble((String) mMap.get("attribute[WMSTotalQuantity]")));
			sumQuantityRate = sumQuantityRate
					+ (int) (sumQuantity * (Double.parseDouble((String) mMap.get("attribute[WMSReducedSORRate]"))));
			sumQtyPaidTillDateRate = sumQtyPaidTillDateRate
					+ (int) (sumQuantity * (Double.parseDouble((String) mMap.get("attribute[WMSQtyPaidTillDate]"))));
			sumQtySubmittedTillDate = sumQtySubmittedTillDate
					+ (int) (Double.parseDouble((String) mMap.get("attribute[WMSMBEQuantity]")));
		}
		
		if (sumQuantity != 0) {
			physicalProgress = sumQtySubmittedTillDate / sumQuantity * 100;
		}
		
		// Get Contract Value
		String strContractValue = (String) domWorkOrder.getInfo(context, "attribute[WMSValueOfContract].value");
		
		// Calculate Financial Progress
		
		int financialProgress = 0;
		String strBudgetActualCost = (String) domProjectSpace.getInfo(context,
				"relationship[Project Financial Item].to.attribute[Actual Cost].value");
		System.out.println("strBudgetActualCost:::::::::::::" + strBudgetActualCost);
		financialProgress = (int) ((int) Double.parseDouble(strBudgetActualCost) / Double.parseDouble(strContractValue)
				* 100);
		System.out.println("financialProgress:::::::::::::" + financialProgress);

		// Get Advancies and Recoveries
		String strAdvance = (String) domWorkOrder.getInfo(context,
				"relationship[WMSWorkOrderAdvances].to.attribute[WMSAdvanceAmount].value");
		String strRecovery = (String) domWorkOrder.getInfo(context,
				"relationship[WMSWorkOrderAdvances].to.attribute[WMSRecoveryAmount].value");
		
		// Get Retentions and Payments
		String strRetention = (String) domWorkOrder.getInfo(context,
				"relationship[WMSWorkOrderRetentionRecovery].to.attribute[WMSRetentionAmount].value");
		String strPayment = (String) domWorkOrder.getInfo(context,
				"relationship[WMSWorkOrderRetentionRecovery].to.attribute[WMSRetensionAmountPaidTillDate].value");

		// Check LD or No
		String strCheckLD = (String) domWorkOrder.getInfo(context,
				"relationship[WMSWorkOrderReduction].to.attribute[WMSIsLDItem].value");
		
		String strLD = DomainConstants.EMPTY_STRING;
		String strLDRelease = DomainConstants.EMPTY_STRING;
		String strWitheld = DomainConstants.EMPTY_STRING;
		String strWitheldRelease = DomainConstants.EMPTY_STRING;
		if (strCheckLD.equals("Yes")) {
			// Get Liquidated Damages
			strLD = domWorkOrder.getInfo(context,
					"relationship[WMSWorkOrderReduction].to.attribute[WMSBillReductionAmount].value");
			// Get Liquidated Damages Release
			strLDRelease = domWorkOrder.getInfo(context,
					"relationship[WMSWorkOrderReduction].to.attribute[WMSBillReductionReleaseAmountTillDate].value");
			} 
		else {
			// Get Withelds/Releases
			strWitheld = (String) domWorkOrder.getInfo(context,
					"relationship[WMSWOAbstractMBE].to.attribute[WMSAbstractMBEWithHeldAmount].value");
			strWitheldRelease = (String) domWorkOrder.getInfo(context,
					"relationship[WMSWOAbstractMBE].to.attribute[WMSAbstractMBEWithHeldReleasedAmount].value");
			}

		// Get Royalties
		int strRoyalty = 0;
		StringList strRoyaltyBaseRate = domWorkOrder.getInfoList(context,
				"relationship[WMSWorkOrderRoyaltyCharges].to.attribute[WMSBaseRate].value");
		StringList strRoyaltyQty = domWorkOrder.getInfoList(context,
				"relationship[WMSWorkOrderRoyaltyCharges].to.attribute[WMSAbstractMBEItemPayableQuantity].value");
		for (int i = 0; i < strRoyaltyBaseRate.size(); i++) {
			strRoyalty = (int) (Double.parseDouble(strRoyaltyBaseRate.get(i))
					* Double.parseDouble(strRoyaltyQty.get(i)));
		}
		
		// Get Rate Escalations
		int strRateEscalation = 0;
		StringList strRateEscalationBaseRate = domWorkOrder.getInfoList(context,
				"relationship[WMSWorkOrderRateEscalation].to.attribute[WMSBaseRate].value");
		StringList strRateEscalationQty = domWorkOrder.getInfoList(context,
				"relationship[WMSWorkOrderRateEscalation].to.attribute[WMSAbstractMBEItemPayableQuantity].value");
		for (int i = 0; i < strRateEscalationBaseRate.size(); i++) {
			strRateEscalation = (int) (Double.parseDouble(strRateEscalationBaseRate.get(i))
					* Double.parseDouble(strRateEscalationQty.get(i)));
		}
		String strBudgetStartDate = domProjectSpace.getInfo(context,
				"relationship[Project Financial Item].to.attribute[Cost Interval Start Date].value");
		String strBudgetEndDate = domProjectSpace.getInfo(context,
				"relationship[Project Financial Item].to.attribute[Cost Interval End Date].value");
		
		StringList strListBusSelects1 = new StringList();
		StringList strListRelSelects1 = new StringList();
		strListRelSelects1.add("attribute[Estimated Cost]");
		strListRelSelects1.add("attribute[Actual Cost]");
		strListRelSelects1.add("attribute[Interval Date]");
		Pattern patternType1 = new Pattern("Budget");
		patternType1.addPattern("Cost Item");
		patternType1.addPattern("Interval Item Data");
		
		Pattern patternRel1 = new Pattern("Project Financial Item");
		patternRel1.addPattern("Financial Items");
		patternRel1.addPattern("Cost Item Interval");

		MapList mlRelAttributeActualCost = domProjectSpace.getRelatedObjects(context, patternRel1.getPattern(),
				patternType1.getPattern(), true, true, (short) 0, strListBusSelects1, strListRelSelects1,
				DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING, (short) 0, "Cost Item Interval", null,
				null);

		Map mMap = new LinkedHashMap();
		String strEstimatedCost = DomainConstants.EMPTY_STRING;
		String strActualCost = DomainConstants.EMPTY_STRING;
		String strIntervalDate = DomainConstants.EMPTY_STRING;
		String strMonth = DomainConstants.EMPTY_STRING;
		String strMonthwiseCost = DomainConstants.EMPTY_STRING;
		String strPlannedMonthwiseCost = DomainConstants.EMPTY_STRING;
		String strActualMonthwiseCost = DomainConstants.EMPTY_STRING;
		ArrayList<Date> dtYearMonth = new ArrayList<Date>();
		ArrayList<String> dtYearMonth2 = new ArrayList<>();
		int Year = 0;
		String[] monthName = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
		for (int i = 0; i < mlRelAttributeActualCost.size(); i++) {
			mMap = (Map) mlRelAttributeActualCost.get(i);
			String strLevel = (String) mMap.get("level");
			if (strLevel.equals("3")) {
				strEstimatedCost = (String) mMap.get("attribute[Estimated Cost]");
				strIntervalDate = (String) mMap.get("attribute[Interval Date]");
				strActualCost = (String) mMap.get("attribute[Actual Cost]");
				Date date = new Date(strIntervalDate);
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				strMonth = monthName[cal.get(Calendar.MONTH)];
				Year = cal.get(Calendar.YEAR);
				String strMonthYear = strMonth + "-" + Integer.toString(Year);
				SimpleDateFormat objSDF = new SimpleDateFormat("MMM-yyyy");
				Date dt_1 = objSDF.parse(strMonthYear);
				dtYearMonth.add(dt_1);
				strPlannedMonthwiseCost = strPlannedMonthwiseCost + ";" + strMonthYear + ":" + strEstimatedCost;
				strActualMonthwiseCost = strActualMonthwiseCost + ";" + strMonthYear + ":" + strActualCost;
			}
		}
		Collections.sort(dtYearMonth);

		for (int i = 0; i < dtYearMonth.size(); i++) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(dtYearMonth.get(i));
			strMonth = monthName[cal.get(Calendar.MONTH)];
			Year = cal.get(Calendar.YEAR);
			dtYearMonth2.add(strMonth + "-" + Integer.toString(Year));
		}
		String sortedDatesString = String.join(", ", dtYearMonth2);
		// Get Cost Curve -
		LinkedHashMap<String, String> mp = new LinkedHashMap<String, String>();
		mp.put("physicalProgress", Integer.toString(physicalProgress));
		mp.put("financialProgress", Integer.toString(financialProgress));
		mp.put("strContractValue", strContractValue);
		mp.put("strAdvance", strAdvance);
		mp.put("strRecovery", strRecovery);
		mp.put("strRetention", strRetention);
		mp.put("strPayments", strPayment);
		mp.put("strLD", strLD);
		mp.put("strLDRelease", strLDRelease);
		mp.put("strWitheld", strWitheld);
		mp.put("strWitheldRelease", strWitheldRelease);
		mp.put("strRoyalty", Integer.toString(strRoyalty));
		mp.put("strRateEscalation", Integer.toString(strRateEscalation));
		mp.put("strBudgetStartDate", strBudgetStartDate);
		mp.put("strBudgetEndDate", strBudgetEndDate);
		mp.put("scurvePlanned", strPlannedMonthwiseCost);
		mp.put("sortedDates", sortedDatesString);
		mp.put("scurveActual", strActualMonthwiseCost);
		return mp;
	}
}
