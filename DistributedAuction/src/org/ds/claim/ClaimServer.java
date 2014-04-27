package org.ds.claim;

import java.util.Date;

import org.ds.auction.AuctionServer;
import org.ds.auction.AuctionServerPersistance;
import org.ds.auction.BuyerCriteria;
import org.ds.client.DBClient;
import org.ds.util.DateUtil;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.WriteResult;

public class ClaimServer {
	
	public static void main(String[] args){
		long startTime = System.currentTimeMillis();
		ClaimServer server = new ClaimServer("123_1398545770927", "Steve_Focus_8");
		server.claim();
		long difference = System.currentTimeMillis() - startTime;
		System.out.println("Latency: " + difference);
		return;
	}
	
	public static String FIELD_AVAILABILITY = "availableTimes" ;
	public static String FIELD_VERSION = "version";
	
	public static String FIELD_FROM_DATE = "from";
	public static String FIELD_TO_DATE = "till";
	
	public static class ClaimResult{
		
		public static final String REASON_TEMPORARY = "temporary";
		public static final String REASON_PERMANENT = "permanenant";
		
		private Boolean success;
		private String reason;
		
		public Boolean isSuccess() {
			return this.success;
		}
		
		public String getReason(){
			return this.reason;
		}
		
		public Boolean shouldTryAgain(){
			return !isSuccess() && getReason().equals(REASON_TEMPORARY);
		}
		
		public void setSuccess(Boolean success){
			this.success = success;
		}
		
		public void setReason(String reason){
			this.reason = reason;
		}
		
		public ClaimResult(Boolean success, String reason){
			setSuccess(success);
			setReason(reason);
		}
	}
	
	public static class AvailabiltyData{
		private BasicDBList availabilityData;
		private int version;
		
		public BasicDBList getAvailabilityData(){
			return this.availabilityData;
		}
		
		public int getVersion(){
			return this.version;
		}
		
		public Boolean isAvailable(){
			return this.availabilityData != null;
		}
		
		public void printAvailability(){
			for(Object interval:getAvailabilityData()){
				System.out.println(interval);
			}
		}
		
		public AvailabiltyData(BasicDBList availabilityData, int version){
			this.availabilityData = availabilityData;
			this.version = version;
		}
	}
	
	private String auctionID;
	private String productID;
	
	private BasicDBObject auctionDetails;
	private BasicDBObject productDetails;
	private BuyerCriteria buyerCriteria;
	
	private ClaimServerPersistance claimServerPersistance;
	
	public String getAuctionID(){
		return this.auctionID;
	}
	
	public String getProductID(){
		return this.productID;
	}
	
	public ClaimServerPersistance getClaimServerPersistance(){
		if(this.claimServerPersistance == null){
			this.claimServerPersistance = new ClaimServerPersistance();
		}
		
		return this.claimServerPersistance;
	}
	
	public BasicDBObject getAuctionDetails(){
		if(this.auctionDetails == null){
			ClaimServerPersistance persistance = getClaimServerPersistance();
			this.auctionDetails = persistance.getAuctionData(getAuctionID());
		}
		return this.auctionDetails;
	}
	
	public BasicDBObject getProductDetails(){
		if(this.productDetails == null){
			ClaimServerPersistance persistance = getClaimServerPersistance();
			BuyerCriteria criteria = getBuyerCriteria();
			this.productDetails = persistance.getProductData(getProductID(), criteria);
		}
		return this.productDetails;
	}
	
	private BuyerCriteria getBuyerCriteria(){
		if(this.buyerCriteria == null) {
			BasicDBObject auctionDetails = getAuctionDetails();
			BasicDBObject criteriaBSON = (BasicDBObject) auctionDetails.get(AuctionServerPersistance.FIELD_BUYER_CRITERIA);
			this.buyerCriteria = new BuyerCriteria(criteriaBSON);
		}
		
		return this.buyerCriteria; 
	}
	
	public ClaimServer(String auctionID, String productID){
		this.auctionID = auctionID;
		this.productID = productID;	
	}
	
	public Boolean claim(){
		ClaimResult result;
		do{
			System.out.println("Attempting to claim: " + getProductID());
			result = attemptClaim();
		} while(!result.isSuccess() && result.shouldTryAgain());
		
		if(result.isSuccess()) {
			while(!markDealAsClaimedInAuction()){
				//keep doing this till it succeeds
			}
		}
		
		return result.isSuccess();
	}

	private Boolean markDealAsClaimedInAuction(){
		BasicDBObject auctionDetails = getAuctionDetails();
		AuctionServerPersistance persistance = new AuctionServerPersistance(getBuyerCriteria(), 
				getAuctionID(), auctionDetails.getInt(AuctionServerPersistance.FIELD_VERSION));
		BasicDBObject localResults = (BasicDBObject)auctionDetails.get(AuctionServerPersistance.FIELD_LOCAL_RESULTS);
		BasicDBList bids = (BasicDBList)localResults.get(AuctionServerPersistance.FIELD_BIDS);
		BasicDBList modifiedBids = new BasicDBList();
		for(Object bidObj:bids){
			BasicDBObject bid = (BasicDBObject)bidObj;
			if(bid.get(AuctionServerPersistance.FIELD_PRODUCT_ID).equals(getProductID())){
				bid.append(AuctionServerPersistance.FIELD_CLAIMED, true);
			}
			modifiedBids.add(bid);
		}
		
		localResults.remove(AuctionServerPersistance.FIELD_BIDS);
		localResults.append(AuctionServerPersistance.FIELD_BIDS, modifiedBids);
		
		return persistance.updateOnClaim(localResults);
		
	}
	
	private ClaimResult attemptClaim(){
		Boolean success = true;
		String reason = "";
		
		AvailabiltyData availability = getAvailibilityData();
		success = availability.isAvailable();
		if(success){
			AvailabiltyData newAvailibility = sunderAvailability(availability);
			//System.out.println("New availability: ");
			newAvailibility.printAvailability();
			return commitClaim(newAvailibility, availability);
		} else {
			System.out.println("Item no longer available");
			reason = ClaimResult.REASON_PERMANENT;
		}
		
		return new ClaimResult(success, reason);
	}
	
	private AvailabiltyData getAvailibilityData(){
		BasicDBObject productDetails = getProductDetails();
		
		BasicDBList availabilityData = null;
		int version = 0;
		if(productDetails != null && productDetails.containsField(FIELD_AVAILABILITY) && productDetails.containsField(FIELD_VERSION)){
			availabilityData = (BasicDBList) productDetails.get(FIELD_AVAILABILITY);
			version = productDetails.getInt(FIELD_VERSION);
		}
		
		return new AvailabiltyData(availabilityData, version);
	}
	
	private AvailabiltyData sunderAvailability(AvailabiltyData availabilityData){
		BasicDBList availability = availabilityData.getAvailabilityData();
		BuyerCriteria criteria = getBuyerCriteria();
		
		Date neededFrom = criteria.getNeededFrom();
		Date neededUntil = criteria.getNeededUntil();
		
		//System.out.println("Availability: " + availability);
		//System.out.println("Needed from: " + DateUtil.getStringFromDate(criteria.getNeededFrom()) +
		//		". Needed until: " + DateUtil.getStringFromDate(criteria.getNeededUntil()));
		
		BasicDBList newAvailability = new BasicDBList();
		for(Object period:availability){
			BasicDBObject interval = (BasicDBObject)period;
			Date fromField = interval.getDate(FIELD_FROM_DATE);
			Date toField = interval.getDate(FIELD_TO_DATE);
			if((neededFrom.equals(fromField) || neededFrom.after(fromField)) 
					&& (neededUntil.before(toField) || neededUntil.equals(toField))){
				//System.out.println("Found matching interval: " + interval);
				BasicDBList sunderedIntervals = getNewIntervals(interval, neededFrom, neededUntil);
				newAvailability.addAll(sunderedIntervals);
			} else {
				newAvailability.add(interval);
				//System.out.println("Skipping interval: " + interval);
			}
		}
		
		return new AvailabiltyData(newAvailability, availabilityData.getVersion() + 1);
	}
	
	private BasicDBList getNewIntervals(BasicDBObject interval, Date neededFrom, Date neededUntil){
		Date fromField = interval.getDate(FIELD_FROM_DATE);
		Date toField = interval.getDate(FIELD_TO_DATE);
		
		
		BasicDBList sunderedList = new BasicDBList();
		if(!(fromField.equals(neededFrom) && toField.equals(neededUntil))){
			if(!fromField.equals(neededFrom)) {
				BasicDBObject newInterval = new BasicDBObject(FIELD_FROM_DATE, fromField)
																.append(FIELD_TO_DATE, neededFrom);
				sunderedList.add(newInterval);
			}
			
			if(!toField.equals(neededUntil)){
				BasicDBObject newInterval = new BasicDBObject(FIELD_FROM_DATE, neededUntil)
																.append(FIELD_TO_DATE, toField);
				sunderedList.add(newInterval);
			}
		} else {
			//System.out.println("Exact match of interval!");
		}
		
		return sunderedList;
	}
	
	private ClaimResult commitClaim(AvailabiltyData newAvailibility, AvailabiltyData oldAvailability){
		Boolean success = true;
		String reason = ClaimResult.REASON_TEMPORARY;
		
		ClaimServerPersistance persistance = getClaimServerPersistance();
		BuyerCriteria criteria = getBuyerCriteria();
		
		Date neededFrom = criteria.getNeededFrom();
		Date neededUntil = criteria.getNeededUntil();
		
		BasicDBObject eleMatch = new BasicDBObject();
		eleMatch.put("from", new BasicDBObject("$lte", neededFrom));
		eleMatch.put("till", new BasicDBObject("$gte", neededUntil));
		
		BasicDBObject wrapper = new BasicDBObject();
		wrapper.put("$elemMatch", eleMatch);
		
		BasicDBObject query = new BasicDBObject(AuctionServer.FIELD_PRODUCT_ID, productID)
													.append(FIELD_AVAILABILITY, wrapper)
													.append(FIELD_VERSION, oldAvailability.getVersion());
		//System.out.println("Query: " + query);
		
		BasicDBObject entry = new BasicDBObject(FIELD_AVAILABILITY, newAvailibility.getAvailabilityData())
													.append(FIELD_VERSION, newAvailibility.getVersion());
		BasicDBObject update = new BasicDBObject("$set", entry);
		//System.out.println("Update: " + update);
		
		WriteResult updateResult = persistance.updateMongo(query, update, DBClient.CAR_VENDORS_DETAILS);
		//System.out.println("Update result: " + updateResult);
		
		success = updateResult.isUpdateOfExisting();
		
		return new ClaimResult(success, reason);
	}
}
