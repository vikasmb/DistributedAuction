package org.ds.auction;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
	public static String LIST_PRICE_FIELD = "listPrice";
	public static String MIN_PRICE_FIELD = "minPrice";
	public static String SELLER_ID_FIELD = "userId";
	public static String PRODUCT_ID_FIELD = "productId";
	
	public static String STATUS_RUNNING = "running";
	public static String STATUS_FINISHED = "finished";
	
	private int minWinners = 5;
	
	private int maxLastCalls = 10;
	
	public static void main(String args[]) {
		
	}
	
	private BidderDetails bidderDetails;
	private BuyerCriteria buyerCriteria;
	
	private AuctionServerPersistance auctionWriter;
	
	
	private void setBidderDetails(BidderDetails bidderDetails){
		this.bidderDetails = bidderDetails;
	}
	
	private void setBuyerCriteria(BuyerCriteria buyerCriteria){
		this.buyerCriteria = buyerCriteria;
	}
	
	private void setAuctionWriter(AuctionServerPersistance auctionWriter) {
		this.auctionWriter = auctionWriter;
	}
	
	private BidderDetails getBidderDetails() {
		return this.bidderDetails;
	}
	
	private BuyerCriteria getBuyerCriteria() {
		return this.buyerCriteria;
	}
	
	private AuctionServerPersistance getAuctionWriter(){
		return this.auctionWriter;
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
		
		AuctionServerPersistance writer = new AuctionServerPersistance();
		setAuctionWriter(writer);
	}
	
	public void run() {
		makeInitAuctionEntry();
		System.out.println("Auction entry made with id");
		runLocalAuction();
		//runRemoteAuction();
		
		finishUpAuction();
	}
	
	private Boolean runLocalAuction(){
		TreeMap<Double, List<LocalSellerDetails>> prices = getSortedListAndMinPrices();
		System.out.println("Sorted list and min prices obtained for local bidders");
		for(Entry<Double,List<LocalSellerDetails>> entry: prices.entrySet()){
			System.out.println("For Price: "+entry.getKey());
			for(LocalSellerDetails localSeller: entry.getValue()){
				System.out.println("Local seller found: "+localSeller.getSellerID());
			}
		}
		TreeMap<Double, List<WinnerDetails>> winners = new TreeMap<Double, List<WinnerDetails>>();
		
		List<WinnerDetails> winnersDetails = null;
		List<LocalSellerDetails> sellersDetails = null;
		Double lastPrice = Double.MAX_VALUE;
		
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
					winners.put(price - 1.0, winnersDetails);
				}
				
				if(winnersNum >= minWinners) {
					break;
				} else {
					sellersDetails = prices.get(price);
					winnersDetails = getWinnersDetails(sellersDetails);
					winnersNum += winnersDetails.size();
					lastPrice = price;
				}
			}
			
			if(winnersDetails != null){
				winners.put(lastPrice, winnersDetails);
			}
		}
		
		System.out.println("Winners found: ");
		for(Entry<Double,List<WinnerDetails>> entry: winners.entrySet()){
			System.out.println("For Price: "+entry.getKey());
			for(WinnerDetails winner: entry.getValue()){
				System.out.println("Winner found: "+winner.getSellerID());
			}
		}
		makeLocalWinnersEntry(winners);
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
		System.out.println("Determing the sorted list and min prices for localBidders of size:"+localBidders.size());
		TreeMap<Double, List<LocalSellerDetails>> prices = new TreeMap<Double, List<LocalSellerDetails>>();
		
		for(int i = 0; i < localBidders.size(); i++) {
			BasicDBObject localBidder = localBidders.get(i);
			enterDetails(localBidder, prices);
		}
		
		return prices;
	}
	//Threadit
	private void enterDetails(BasicDBObject localBidder, TreeMap<Double, List<LocalSellerDetails>> prices){
		BasicDBList pricesByHour = (BasicDBList)localBidder.get(BUYER_PRICE_DETAILS);
		Date startHour = getBuyerCriteria().getNeededFrom();
		Date endHour = getBuyerCriteria().getNeededUntil();
		
		System.out.println("Start hour: " + startHour.getTime());
		System.out.println("End hour: " + endHour.getTime());
		
		Double minPrice = 0.0;
		Double listPrice = 0.0;
		
		for(int i = 0; i < pricesByHour.size(); i++){
			BasicDBObject currentHourPriceDetails = (BasicDBObject)pricesByHour.get(i);
			System.out.println("At " + i + ": " + currentHourPriceDetails);
			Date currentHour = currentHourPriceDetails.getDate(HOUR_FIELD);
			if((currentHour.equals(startHour) || currentHour.after(startHour)) && currentHour.before(endHour)){
				System.out.println("Current hour: " + currentHour.getTime());
				minPrice += currentHourPriceDetails.getDouble(MIN_PRICE_FIELD);
				listPrice += currentHourPriceDetails.getDouble(LIST_PRICE_FIELD);
			} else if(currentHour.equals(endHour) || currentHour.after(endHour)) {
				break;
			}
		}
		
		LocalSellerDetails sellerDetails = new LocalSellerDetails(listPrice, minPrice, 
				localBidder.getString(SELLER_ID_FIELD), localBidder.getString(PRODUCT_ID_FIELD));
		//Synchornized
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
		int roundNum = 1;
		
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
				
				makeRemoteRoundEntry(roundNum, currentResults);
				roundNum++;
			}
			
			//we are ready for a last call because the bids have stabilized
			numLastCalls++;
			lastResults = currentResults;
			currentResults = runRound(remoteBidders, lastResults, true);
			
			makeRemoteRoundEntry(roundNum, currentResults);
			roundNum++;
			
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
			getBid(remoteBidder,  newRemoteBidders, oldBids, newBids); //get his bid Threadit
		}
		
		remoteBidders = newRemoteBidders; //set the remote bidders to the new set
		
		AuctionResults currentResults =  new AuctionResults(); 
		currentResults.setBids(newBids);//new results object //TODO: cut down the number here
		
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
			//Synchoronization
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
	
	private Boolean makeInitAuctionEntry(){
		AuctionServerPersistance writer = getAuctionWriter();
		BuyerCriteria criteria = getBuyerCriteria();
		
		return true;
	}
	
	private Boolean makeLocalWinnersEntry(TreeMap<Double, List<WinnerDetails>> winners){
		AuctionServerPersistance writer = getAuctionWriter();
		return true;
	}
	
	private Boolean makeRemoteRoundEntry(int roundNum, AuctionResults results){
		AuctionServerPersistance writer = getAuctionWriter();
		return true;
	}
	
	private Boolean finishUpAuction(){
		AuctionServerPersistance writer = getAuctionWriter();
		return true;
	}
}
