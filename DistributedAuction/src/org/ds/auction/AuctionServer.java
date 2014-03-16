package org.ds.auction;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;


import org.ds.client.*;
import org.ds.carServer.*;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class AuctionServer {
	
	public static String LOCAL_BIDDERS = "local";
	public static String REMOTE_BIDDERS = "remote";
	
	public static String BUYER_PRICE_DETAILS = "prices";
	public static String HOUR_FIELD = "hour";
	public static String LIST_PRICE_FIELD = "list_price";
	public static String MIN_PRICE_FIELD = "min_price";
	public static String SELLER_ID_FIELD = "userId";
	public static String PRODUCT_ID_FIELD = "productId";
	
	private int minWinners = 5;
	
	private int maxLastCalls = 10;
	
	public static void main(String args[]) {
		Map<Integer, String> checkMap = new TreeMap<Integer, String>();
		checkMap.put(1, "One");
		checkMap.put(4, "Four");
		checkMap.put(3, "Three");
		checkMap.put(2, "Two");
		
		for(Integer key : checkMap.keySet()) {
		    System.out.println(checkMap.get(key));
		}
	}
	
	private BidderDetails bidderDetails;
	private BuyerCriteria buyerCriteria;
	
	private BidderDetails getBidderDetails() {
		return this.bidderDetails;
	}
	
	private void setBidderDetails(BidderDetails bidderDetails){
		this.bidderDetails = bidderDetails;
	}
	
	private void setBuyerCriteria(BuyerCriteria buyerCriteria){
		this.buyerCriteria = buyerCriteria;
	}
	
	private BuyerCriteria getBuyerCriteria() {
		return this.buyerCriteria;
	}
	
	private List<BasicDBObject> getLocalBidders() {
		return getBidderDetails().getLocalBidders();
	}
	
	private List<BasicDBObject> getRemoteBidders() {
		return getBidderDetails().getRemoteBidders();
	}
	
	public AuctionServer(BidderDetails bidderDetails, BuyerCriteria buyerCriteria) {
		setBidderDetails(bidderDetails);
		setBuyerCriteria(buyerCriteria);
	}
	
	public void run() {
		runLocalAuction();
		runRemoteAuction();
		finishUpAuction();
	}
	
	private Boolean runLocalAuction(){
		TreeMap<Double, List<LocalSellerDetails>> prices = getSortedListAndMinPrices();
		TreeMap<Double, List<WinnerDetails>> winners = new TreeMap<Double, List<WinnerDetails>>();
		
		List<WinnerDetails> winnersDetails = null;
		List<LocalSellerDetails> sellersDetails = null;
		
		
		if(prices.size() == 1 && prices.get(prices.firstKey()).size() == 1){
			//special case when there is only seller for this particular service
			//If this happens, that person can claim the list price
			sellersDetails = prices.get(prices.firstKey()); 
			Double price = sellersDetails.get(0).getListPrice();
			winnersDetails = getWinnersDetails(sellersDetails);
			winners.put(price, winnersDetails);
		} else {
			int winnersNum = 0;
			Set<Double> minPrices = prices.keySet();
			for(Double price:minPrices){
				if(winnersDetails != null){
					winners.put(price, winnersDetails);
				}
				
				if(winnersNum >= minWinners) {
					break;
				} else {
					sellersDetails = prices.get(price);
					winnersDetails = getWinnersDetails(sellersDetails);
					winnersNum += winnersDetails.size();
				}
			}
		}
		
		return true;
	}
	
	private List<WinnerDetails> getWinnersDetails(List<LocalSellerDetails> sellersDetails) {
		 List<WinnerDetails> winnersDetails = new ArrayList<WinnerDetails>();
		 for(int i = 0; i < sellersDetails.size(); i++) {
			 LocalSellerDetails sellerDetails = sellersDetails.get(i);
			 winnersDetails.add(new WinnerDetails(sellerDetails.getSellerID() ,sellerDetails.getProductID()));
		 }
		 return winnersDetails;
	}
	
	private TreeMap<Double, List<LocalSellerDetails>> getSortedListAndMinPrices(){
		List<BasicDBObject> localBidders = getLocalBidders();
		TreeMap<Double, List<LocalSellerDetails>> prices = new TreeMap<Double, List<LocalSellerDetails>>();
		
		for(int i = 0; i < localBidders.size(); i++) {
			BasicDBObject localBidder = localBidders.get(i);
			enterDetails(localBidder, prices);
		}
		
		return prices;
	}
	
	private void enterDetails(BasicDBObject localBidder, TreeMap<Double, List<LocalSellerDetails>> prices){
		BasicDBList pricesByHour = (BasicDBList)localBidder.get(BUYER_PRICE_DETAILS);
		Date startHour = getBuyerCriteria().getNeededFrom();
		Date endHour = getBuyerCriteria().getNeededUntil();
		
		Double minPrice = 0.0;
		Double listPrice = 0.0;
		
		for(int i = 0; i < pricesByHour.size(); i++){
			BasicDBObject currentHourPriceDetails = (BasicDBObject)pricesByHour.get(i);
			Date currentHour = currentHourPriceDetails.getDate(HOUR_FIELD);
			if(currentHour.after(startHour) && currentHour.before(endHour)){
				minPrice += currentHourPriceDetails.getDouble(MIN_PRICE_FIELD);
				listPrice += currentHourPriceDetails.getDouble(LIST_PRICE_FIELD);
			}
		}
		
		LocalSellerDetails sellerDetails = new LocalSellerDetails(listPrice, minPrice, 
				localBidder.getString(SELLER_ID_FIELD), localBidder.getString(PRODUCT_ID_FIELD));
		
		List<LocalSellerDetails> value;
		if(prices.containsKey(minPrice)) {
			value = prices.get(minPrice);
		} else {
			value = new ArrayList<LocalSellerDetails>();
		}
		
		value.add(sellerDetails);
		prices.put(minPrice, value);
	}
	
	private Boolean runRemoteAuction(){
		//get the remote bidders
		List<BasicDBObject> remoteBidders = getRemoteBidders();
		
		//run rounds
		//last calls tracker
		Boolean lastCallSuccess = false;			//did the last call succeed?
		int numLastCalls = 0;					//bound the number of last calls to offset byzantine bidders
		
		//results tracker
		AuctionResults lastResults = null;
		AuctionResults currentResults = null;
		
		while(!lastCallSuccess && numLastCalls < maxLastCalls) {
			while(currentResults == null || !currentResults.isSameAs(lastResults)) {
				lastResults = currentResults;
				currentResults = runRound(remoteBidders, lastResults, false);
			}
			
			//we are ready for a last call because the bids have stabilized
			numLastCalls++;
			lastResults = currentResults;
			currentResults = runRound(remoteBidders, lastResults, true);
			
			lastCallSuccess = currentResults.isSameAs(lastResults);
		}
		
		return true;
	}
	
	private AuctionResults runRound(List<BasicDBObject> remoteBidders, AuctionResults lastResults, Boolean lastCall){
		//contact each remote bidder and ask him if he wants to bid lower than the auction results
		TreeMap<Double, List<String>> oldBids;
		
		//get the old bids
		if(lastResults != null) {
			oldBids = lastResults.getBids();
		} else {
			oldBids = null;
		}
		
		//new result data structures
		TreeMap<Double, List<String>> newBids = new TreeMap<Double, List<String>>();
		List<BasicDBObject> newRemoteBidders = new ArrayList<BasicDBObject>();
		
		//iterate through each remote bidder and ask if they want to bid
		for(int i = 0; i < remoteBidders.size(); i++){
			BasicDBObject remoteBidder = remoteBidders.get(i); //get remote bidder at position i
			getBid(remoteBidder,  newRemoteBidders, oldBids, newBids); //get his bid
		}
		
		remoteBidders = newRemoteBidders; //set the remote bidders to the new set
		
		AuctionResults currentResults =  new AuctionResults(); 
		currentResults.setBids(newBids);//new results object
		
		return currentResults;
	}
	
	private void getBid(BasicDBObject remoteBidder, List<BasicDBObject> newRemoteBidders,
			TreeMap<Double, List<String>> oldBids, TreeMap<Double, List<String>> newBids){
		
		String remoteAddress = remoteBidder.getString(SellerDetails.FIELD_REMOTE);//get remote address
		String sellerName = remoteBidder.getString(SellerDetails.FIELD_NAME);//get the seller's name
		
		//IMPORTANT: oldBids may be null. Handle the case. If it is null, then this is the first round
		//make a URI call to remote address.
		//convert the response to BidDetails Object
		
		BidDetails bidDetails = new BidDetails();
		if(bidDetails.getMadeBid()){
			Double bid = bidDetails.getBid();
			List<String> sellers;
			if(newBids.containsKey(bid)) {
				sellers = newBids.get(bid);
			} else {
				sellers = new ArrayList<String>();
			}
			
			sellers.add(sellerName);
			newBids.put(bid, sellers); //put the bid
			newRemoteBidders.add(remoteBidder); //add as possible bidder for next round
		}
	}
	
	private Boolean finishUpAuction(){
		return true;
	}
}
