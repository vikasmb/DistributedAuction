package org.ds.auction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.ws.rs.core.MediaType;

import org.ds.client.*;
import org.ds.carServer.*;
import org.ds.resources.RemoteAuctionDetails;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class AuctionServer {

	public static String LIST_LOCAL_BIDDERS = "local";
	public static String LIST_REMOTE_BIDDERS = "remote";

	public static String FIELD_BUYER_PRICE_DETAILS = "prices";
	public static String FIELD_HOUR = "hour";
	public static String FIELD_LIST_PRICE = "listPrice";
	public static String FIELD_MIN_PRICE = "minPrice";
	public static String FIELD_SELLER_ID = "userId";
	public static String FIELD_PRODUCT_ID = "productId";
	public static String FIELD_REMOTE_ADDRESS = "remote";

	public static String STATUS_RUNNING = "running";
	public static String STATUS_FINISHED = "finished";

	private int MIN_WINNERS = 5;

	private int maxLastCalls = 10;

	public static void main(String args[]) {

	}

	private BidderDetails bidderDetails;
	private BuyerCriteria buyerCriteria;

	private AuctionServerPersistance auctionWriter;

	private void setBidderDetails(BidderDetails bidderDetails) {
		this.bidderDetails = bidderDetails;
	}

	private void setBuyerCriteria(BuyerCriteria buyerCriteria) {
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

	private AuctionServerPersistance getAuctionWriter() {
		return this.auctionWriter;
	}

	private List<BasicDBObject> getLocalBidders() {
		return getBidderDetails().getLocalBidders();
	}

	private List<BasicDBObject> getRemoteBidders() {
		return getBidderDetails().getRemoteBidders();
	}

	public AuctionServer(BidderDetails bidderDetails,
			BuyerCriteria buyerCriteria) {
		setBidderDetails(bidderDetails);
		setBuyerCriteria(buyerCriteria);

		AuctionServerPersistance writer = new AuctionServerPersistance();
		setAuctionWriter(writer);
	}

	public void run() {
		makeInitAuctionEntry();
		System.out.println("Auction entry made with id");
		runLocalAuction();
		runRemoteAuction();

		finishUpAuction();
	}

	private Boolean runLocalAuction() {
		TreeMap<Double, List<LocalSellerDetails>> prices = getSortedListAndMinPrices();
		System.out
				.println("Sorted list and min prices obtained for local bidders");
		for (Entry<Double, List<LocalSellerDetails>> entry : prices.entrySet()) {
			System.out.println("For Price: " + entry.getKey());
			for (LocalSellerDetails localSeller : entry.getValue()) {
				System.out.println("Local seller found: "
						+ localSeller.getSellerID());
			}
		}
		TreeMap<Double, List<WinnerDetails>> winners = new TreeMap<Double, List<WinnerDetails>>();

		List<WinnerDetails> winnersDetails = null;
		List<LocalSellerDetails> sellersDetails = null;
		Double lastPrice = Double.MAX_VALUE;

		if (prices.size() == 1 && prices.get(prices.firstKey()).size() == 1) {
			// special case when there is only seller for this particular
			// service
			// If this happens, that person can claim the list price
			sellersDetails = prices.get(prices.firstKey());
			Double price = sellersDetails.get(0).getListPrice();
			winnersDetails = WinnerDetails.getWinnersDetails(price, sellersDetails);
			winners.put(price, winnersDetails);
		} else {
			int winnersNum = 0;
			Set<Double> minPrices = prices.keySet();
			for (Double price : minPrices) {
				if (winnersDetails != null) {
					winners.put(price - 1.0, winnersDetails);
				}

				if (winnersNum >= MIN_WINNERS) {
					break;
				} else {
					sellersDetails = prices.get(price);
					winnersDetails = WinnerDetails.getWinnersDetails(price, sellersDetails);
					winnersNum += winnersDetails.size();
					lastPrice = price;
				}
			}

			if (winnersDetails != null) {
				winners.put(lastPrice, winnersDetails);
			}
		}

		System.out.println("Winners found: ");
		for (Entry<Double, List<WinnerDetails>> entry : winners.entrySet()) {
			System.out.println("For Price: " + entry.getKey());
			for (WinnerDetails winner : entry.getValue()) {
				System.out.println("Winner found: " + winner.getSellerID());
			}
		}
		makeLocalWinnersEntry(winners);
		return true;
	}

	private TreeMap<Double, List<LocalSellerDetails>> getSortedListAndMinPrices() {
		List<BasicDBObject> localBidders = getLocalBidders();
		System.out
				.println("Determining the sorted list and min prices for localBidders of size:"
						+ localBidders.size());
		TreeMap<Double, List<LocalSellerDetails>> prices = new TreeMap<Double, List<LocalSellerDetails>>();
		List<Callable<LocalSellerDetails>> localBiddersList = new ArrayList<Callable<LocalSellerDetails>>();
		for (int i = 0; i < localBidders.size(); i++) {
			BasicDBObject localBidder = localBidders.get(i);
			localBiddersList.add(new DetailExtractor(localBidder));
		}
		ExecutorService es = Executors.newFixedThreadPool(5); 
		List<Future<LocalSellerDetails>> resultList = null;
		try {
			resultList = es.invokeAll(localBiddersList);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		List<LocalSellerDetails> valueList;

		if (resultList != null) {
			for (Future<LocalSellerDetails> f : resultList) {
				LocalSellerDetails localBidderDetails = null;
				try {
					localBidderDetails = f.get();
					Double minPrice = localBidderDetails.getMinPrice();
					if (prices.containsKey(minPrice)) {
						valueList = prices.get(minPrice);
					} else {
						valueList = new ArrayList<LocalSellerDetails>();
					}
					valueList.add(localBidderDetails);
					prices.put(minPrice, valueList);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}
		// enterDetails(localBidder, prices);
		return prices;
	}

	// Threadit
	private class DetailExtractor implements Callable<LocalSellerDetails> {
		private BasicDBObject localBidder;

		// private void enterDetails(BasicDBObject localBidder, TreeMap<Double,
		// List<LocalSellerDetails>> prices){
		public DetailExtractor(BasicDBObject localBidderObj) {
			localBidder = localBidderObj;
		}

		@Override
		public LocalSellerDetails call() throws Exception {
			BasicDBList pricesByHour = (BasicDBList) localBidder
					.get(FIELD_BUYER_PRICE_DETAILS);
			Date startHour = getBuyerCriteria().getNeededFrom();
			Date endHour = getBuyerCriteria().getNeededUntil();

			System.out.println("Start hour: " + startHour.getTime());
			System.out.println("End hour: " + endHour.getTime());

			Double minPrice = 0.0;
			Double listPrice = 0.0;

			for (int i = 0; i < pricesByHour.size(); i++) {
				BasicDBObject currentHourPriceDetails = (BasicDBObject) pricesByHour
						.get(i);
				System.out.println("At " + i + ": " + currentHourPriceDetails);
				Date currentHour = currentHourPriceDetails.getDate(FIELD_HOUR);
				if ((currentHour.equals(startHour) || currentHour
						.after(startHour)) && currentHour.before(endHour)) {
					System.out
							.println("Current hour: " + currentHour.getTime());
					minPrice += currentHourPriceDetails
							.getDouble(FIELD_MIN_PRICE);
					listPrice += currentHourPriceDetails
							.getDouble(FIELD_LIST_PRICE);
				} else if (currentHour.equals(endHour)
						|| currentHour.after(endHour)) {
					break;
				}
			}

			LocalSellerDetails sellerDetails = new LocalSellerDetails(
					listPrice, minPrice,
					localBidder.getString(FIELD_SELLER_ID),
					localBidder.getString(FIELD_PRODUCT_ID));
			// Synchornized
			/*
			 * List<LocalSellerDetails> value; if(prices.containsKey(minPrice))
			 * { value = prices.get(minPrice); } else { value = new
			 * ArrayList<LocalSellerDetails>(); }
			 * 
			 * value.add(sellerDetails); prices.put(minPrice, value);
			 */
			return sellerDetails;
		}
	}
	
	private Boolean runRemoteAuction() {
		// get the remote bidders
		List<RemoteSellerDetails> remoteBidders = getPackagedRemoteBidders();

		// run rounds
		int roundNum = 1;

		// last calls tracker
		Boolean lastCallSuccess = false; // did the last call succeed?
		int numLastCalls = 0; // bound the number of last calls to offset
								// byzantine bidders

		// results tracker
		TreeMap<Double, List<WinnerDetails>> lastResults = null;
		TreeMap<Double, List<WinnerDetails>> currentResults = null;
        
		while (!lastCallSuccess && numLastCalls < maxLastCalls) {
			while (currentResults == null
					|| !areSame(currentResults, lastResults)) {
				lastResults = currentResults;
				currentResults = runRound(remoteBidders, lastResults, false,roundNum);

				makeRemoteRoundEntry(roundNum, currentResults);
				roundNum++;
			}

			// we are ready for a last call because the bids have stabilized
			numLastCalls++;
			lastResults = currentResults;
			currentResults = runRound(remoteBidders, lastResults, true,roundNum);

			makeRemoteRoundEntry(roundNum, currentResults);
			roundNum++;

			lastCallSuccess = areSame(currentResults, lastResults);
		}
		
		System.out.println("Winners found: ");
		for (Entry<Double, List<WinnerDetails>> entry : currentResults.entrySet()) {
			System.out.println("For Price: " + entry.getKey());
			for (WinnerDetails winner : entry.getValue()) {
				System.out.println("Winner found: " + winner.getSellerID());
			}
		}
		return true;
	}
	
	private List<RemoteSellerDetails> getPackagedRemoteBidders(){
		List<RemoteSellerDetails> packagedRemoteBidders = new ArrayList<RemoteSellerDetails>();
		
		List<BasicDBObject> remoteBidders = getRemoteBidders();
		for(BasicDBObject bidder:remoteBidders){
			String productID = bidder.getString(FIELD_PRODUCT_ID);
			String sellerID = bidder.getString(FIELD_SELLER_ID);
			String remoteAddress = bidder.getString(FIELD_REMOTE_ADDRESS);
			RemoteSellerDetails remoteSeller = new RemoteSellerDetails(remoteAddress, sellerID, productID);
			packagedRemoteBidders.add(remoteSeller);
		}
		
		return packagedRemoteBidders;
	}
	
	private Boolean areSame(TreeMap<Double, List<WinnerDetails>> currentResults, TreeMap<Double, List<WinnerDetails>> lastResults){
		if(currentResults == null || lastResults == null){
			return false;
		}
		
		if(lastResults.size() != currentResults.size()){
			return false;
		}
		
		for(Entry<Double, List<WinnerDetails>> entry: currentResults.entrySet()){
			Double key = entry.getKey();
			List<WinnerDetails> bidders = entry.getValue();
			if(lastResults.containsKey(key)) {
				if(!equalLists(lastResults.get(key), bidders)) {
					return false;
				}
			} else {
				return false;
			}
		}
		
		return true;
	}
	
	public Boolean equalLists(List<WinnerDetails> sellerList1, List<WinnerDetails> sellerList2){
		
		if(sellerList1.size() != sellerList2.size()) {
			return false;
		}
		
		List<String> list1 = new ArrayList<String>();
		List<String> list2 = new ArrayList<String>();
		
		for(int i = 0; i < sellerList1.size(); i++){
			list1.add(sellerList1.get(i).getProductID());
			list2.add(sellerList2.get(i).getProductID());
		}
		
		Collections.sort(list1);
		Collections.sort(list2);
		
		for(int i = 0; i < list1.size(); i++) {
			if(!list1.get(i).equals(list2.get(i))){
				return false;
			}
		}
		
		return true;
	}

	private TreeMap<Double, List<WinnerDetails>> runRound(List<RemoteSellerDetails> remoteBidders,
			TreeMap<Double, List<WinnerDetails>> lastResults, Boolean lastCall,int roundNum) {
		// contact each remote bidder and ask him if he wants to bid lower than
		// the auction results

		// new result data structures
		TreeMap<Double, List<RemoteSellerDetails>> newBids = new TreeMap<Double, List<RemoteSellerDetails>>();
		List<RemoteSellerDetails> newRemoteBidders = new ArrayList<RemoteSellerDetails>();

		// iterate through each remote bidder and ask if they want to bid
		for (int i = 0; i < remoteBidders.size(); i++) {
			RemoteSellerDetails remoteBidder = remoteBidders.get(i); // get remote
																// bidder at
																// position i
			getBid(remoteBidder, newRemoteBidders, lastResults, newBids,roundNum); // get his
																		// bid
																		// Threadit
		}

		remoteBidders = newRemoteBidders; // set the remote bidders to the new
											// set

		int winnersNum = 0;
		TreeMap<Double, List<WinnerDetails>> winBids = new TreeMap<Double, List<WinnerDetails>>();
		for(Entry<Double, List<RemoteSellerDetails>> entry:newBids.entrySet()){
			Double price = entry.getKey();
			List<RemoteSellerDetails> sellersDetails = entry.getValue();
			
			List<WinnerDetails> winnersDetails = WinnerDetails.getWinnersDetails(price, sellersDetails);
			winBids.put(price, winnersDetails);
			
			winnersNum += winnersDetails.size();
			if(winnersNum >= MIN_WINNERS){
				break;
			}
		}

		return winBids;
	}
	

	private void getBid(RemoteSellerDetails remoteBidder,
			List<RemoteSellerDetails> newRemoteBidders,
			TreeMap<Double, List<WinnerDetails>> lastResults,
			TreeMap<Double, List<RemoteSellerDetails>> newBids,int roundNum) {
		Set<Double> oldBids=new HashSet<Double>();
        if(lastResults!=null){
		    oldBids.addAll(lastResults.keySet());
        }
		String remoteAddress = remoteBidder.getRemoteAddress();
		System.out.println("Remote address: " + remoteAddress);
		
		ClientResponse response=null;
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		WebResource webResource=client.resource(remoteAddress);
		RemoteAuctionDetails remoteDetails=new RemoteAuctionDetails();
		
		remoteDetails.setAuctionId("123");
		remoteDetails.setOldBids(oldBids);
		remoteDetails.setRoundNumber(roundNum);
		BidDetails bidDetails = null;
		try{
			 response = webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class,remoteDetails);
			 //System.out.println("Client recieved the status  of"+response.getStatus());
			 bidDetails=response.getEntity(BidDetails.class);
			 System.out.println("In round:"+roundNum+" got back bid of price "+bidDetails.getBid());
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		// IMPORTANT: oldBids may be null. Handle the case. If it is null, then
		// this is the first round
		// make a URI call to remote address.
		// convert the response to BidDetails Object

		if(bidDetails==null){
			System.out.println("Bid Details are null!!");
			return;
		}			
		if (bidDetails.getMadeBid()) {
			Double bid = bidDetails.getBid();
			// Synchronization
			List<RemoteSellerDetails> sellers;
			if (newBids.containsKey(bid)) {
				sellers = newBids.get(bid);
			} else {
				sellers = new ArrayList<RemoteSellerDetails>();
			}
			
			remoteBidder.setPrice(bid);
			sellers.add(remoteBidder);
			newBids.put(bid, sellers); // put the bid
			newRemoteBidders.add(remoteBidder); // add as possible bidder for
												// next round
		}
	}

	private Boolean makeInitAuctionEntry() {
		AuctionServerPersistance writer = getAuctionWriter();
		BuyerCriteria criteria = getBuyerCriteria();
		writer.makeInitEntry(criteria.getBuyerID());
		return true;
	}

	private Boolean makeLocalWinnersEntry(
			TreeMap<Double, List<WinnerDetails>> winners) {
		AuctionServerPersistance writer = getAuctionWriter();
		return writer.persistLocalBidWinners(winners);
	}

	private Boolean makeRemoteRoundEntry(int roundNum, TreeMap<Double, List<WinnerDetails>> winners) {
		AuctionServerPersistance writer = getAuctionWriter();
		return writer.persistRemoteRoundWinners(roundNum, winners);
	}

	private Boolean finishUpAuction() {
		AuctionServerPersistance writer = getAuctionWriter();
		return true;
	}
}
