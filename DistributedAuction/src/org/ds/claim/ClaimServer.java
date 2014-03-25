package org.ds.claim;

import org.ds.auction.AuctionServerPersistance;
import org.ds.auction.BuyerCriteria;
import org.ds.util.DateUtil;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class ClaimServer {
	
	public static String FIELD_AVAILABILITY = "availableTimes" ;
	public static String FIELD_VERSION = "version";
	
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
	
	public static class AvailibiltyData{
		private BasicDBList availabilityData;
		private int version;
		
		public BasicDBList getAvailabilityData(){
			return this.availabilityData;
		}
		
		public int getVersion(){
			return this.version;
		}
		
		public Boolean readSucceeded(){
			return this.availabilityData != null;
		}
		
		public AvailibiltyData(BasicDBList availabilityData, int version){
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
			this.productDetails = persistance.getProductData(getProductID());
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
	
	public static void main(String[] args){
		ClaimServer server = new ClaimServer("123_1395703797188", "car3");
		server.claim();
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
		
		return result.isSuccess();
	}

	private ClaimResult attemptClaim(){
		Boolean success = true;
		String reason = "";
		
		AvailibiltyData readResult = getAvailibilityData();
		success = readResult.readSucceeded();
		if(success){
			BasicDBObject newAvailibility = sunderAvailability(readResult);
			System.out.println("New availability: " + newAvailibility);
			return commitClaim(newAvailibility, readResult);
		} else {
			reason = ClaimResult.REASON_PERMANENT;
		}
		
		return new ClaimResult(success, reason);
	}
	
	private AvailibiltyData getAvailibilityData(){
		BasicDBObject productDetails = getProductDetails();
		
		BasicDBList availabilityData = null;
		int version = 0;
		if(productDetails.containsField(FIELD_AVAILABILITY) && productDetails.containsField(FIELD_VERSION)){
			availabilityData = (BasicDBList) productDetails.get(FIELD_AVAILABILITY);
			version = productDetails.getInt(FIELD_VERSION);
		}
		
		return new AvailibiltyData(availabilityData, version);
	}
	
	private BasicDBObject sunderAvailability(AvailibiltyData availabilityData){
		BasicDBList availability = availabilityData.getAvailabilityData();
		System.out.println("Availability: " + availability);
		BuyerCriteria criteria = getBuyerCriteria();
		System.out.println("Needed from: " + DateUtil.getStringFromDate(criteria.getNeededFrom()) +
				". Needed until: " + DateUtil.getStringFromDate(criteria.getNeededUntil()));
		return new BasicDBObject();
	}
	
	private ClaimResult commitClaim(BasicDBObject availibility, AvailibiltyData result){
		Boolean success = true;
		String reason = "";
		return new ClaimResult(success, reason);
	}
}
