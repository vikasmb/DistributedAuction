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
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

	private class RoundResults {
		private TreeMap<Double, List<WinnerDetails>> newBids;
		private List<RemoteSellerDetails> newRemoteBidders;

		public RoundResults(TreeMap<Double, List<WinnerDetails>> newBids,
				List<RemoteSellerDetails> newRemoteBidders) {
			this.newBids = newBids;
			this.newRemoteBidders = newRemoteBidders;
		}

		public TreeMap<Double, List<WinnerDetails>> getBids() {
			return this.newBids;
		}

		public List<RemoteSellerDetails> getRemoteBidders() {
			return this.newRemoteBidders;
		}
	}

	public static String LIST_LOCAL_BIDDERS = "local";
	public static String LIST_REMOTE_BIDDERS = "remote";

	public static String FIELD_PRICE_DETAILS = "prices";
	public static String FIELD_HOUR = "hour";
	public static String FIELD_LIST_PRICE = "listPrice";
	public static String FIELD_MIN_PRICE = "minPrice";
	public static String FIELD_SELLER_ID = "userId";
	public static String FIELD_PRODUCT_ID = "productId";
	public static String FIELD_REMOTE_ADDRESS = "remote";
	public static String FIELD_CITY = "city";
	public static String FIELD_VERSION = "version";

	public static String STATUS_RUNNING = "running";
	public static String STATUS_FINISHED = "finished";

	private int MIN_WINNERS = 5;

	private int maxLastCalls = 10;

	public static void main(String args[]) {

	}

	private BidderDetails bidderDetails;
	private BuyerCriteria buyerCriteria;
	private Boolean resumingAuction;
	private BasicDBObject failedAuctionDetails;

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

	private Boolean isResumingAuction() {
		return this.resumingAuction;
	}

	private BasicDBObject getFailedAuctionDetails() {
		return this.failedAuctionDetails;
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

		AuctionServerPersistance writer = new AuctionServerPersistance(
				getBuyerCriteria());
		setAuctionWriter(writer);
		this.resumingAuction = false;
	}

	public AuctionServer(BidderDetails bidderDetails,
			BuyerCriteria buyerCriteria, String auctionID,
			BasicDBObject failedAuctionDetails) {
		setBidderDetails(bidderDetails);
		setBuyerCriteria(buyerCriteria);

		AuctionServerPersistance writer = new AuctionServerPersistance(
				getBuyerCriteria(), auctionID,
				failedAuctionDetails
						.getInt(AuctionServerPersistance.FIELD_VERSION));
		setAuctionWriter(writer);
		this.resumingAuction = true;
		this.failedAuctionDetails = failedAuctionDetails;
	}

	public void run() {
		if (!isResumingAuction()) {
			makeInitAuctionEntry();
		}

		runLocalAuction();
		runRemoteAuction();

		finishUpAuction();
	}

	private Boolean runLocalAuction() {
		if (getLocalBidders().size() == 0) {
			return true;
		}

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
			winnersDetails = WinnerDetails.getLocalWinnersDetails(price,
					sellersDetails);
			winners.put(price, winnersDetails);
		} else {
			int winnersNum = 0;	
			Set<Double> minPrices = prices.keySet();
			for (Double price : minPrices) {
				if (winnersDetails != null) {
					winners.put(price - 1.0, winnersDetails);
					winnersNum += winnersDetails.size();
				}

				if (winnersNum >= MIN_WINNERS) {
					break;
				} else {
					sellersDetails = prices.get(price);
					winnersDetails = WinnerDetails.getLocalWinnersDetails(
							price, sellersDetails);
					lastPrice = price;
				}
			}

			if (winnersDetails != null && winnersNum < MIN_WINNERS) {
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
		makeLocalWinnersEntry(winners); // changeit for simulating local round
										// failure
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
		es.shutdown();
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
					.get(FIELD_PRICE_DETAILS);
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

	private int getInitRoundNumber() {
		int roundNum = 1;
		if (getFailedAuctionDetails() != null) {
			BasicDBObject remoteResults = (BasicDBObject) getFailedAuctionDetails()
					.get(AuctionServerPersistance.FIELD_REMOTE_RESULTS);
			roundNum = remoteResults
					.getInt(AuctionServerPersistance.FIELD_ROUND_NUM) + 1;
		}

		return roundNum;
	}

	private TreeMap<Double, List<WinnerDetails>> getInitLastResults(int roundNum) {
		TreeMap<Double, List<WinnerDetails>> lastResults = null;
		if (roundNum >= 1) {
			lastResults = new TreeMap<Double, List<WinnerDetails>>();
			if (getFailedAuctionDetails() != null) {
				BasicDBObject remoteResults = (BasicDBObject) getFailedAuctionDetails()
						.get(AuctionServerPersistance.FIELD_REMOTE_RESULTS);
				BasicDBList bids = (BasicDBList) remoteResults
						.get(AuctionServerPersistance.FIELD_BIDS);
				for (Object bid : bids) {
					BasicDBObject bidDetails = (BasicDBObject) bid;
					Double price = bidDetails
							.getDouble(AuctionServerPersistance.FIELD_BID);
					WinnerDetails details = new WinnerDetails(
							price,
							bidDetails
									.getString(AuctionServerPersistance.FIELD_SELLER_ID),
							bidDetails
									.getString(AuctionServerPersistance.FIELD_PRODUCT_ID));

					List<WinnerDetails> winnersDetails = null;
					if (lastResults.containsKey(price)) {
						winnersDetails = lastResults.get(price);
					} else {
						winnersDetails = new ArrayList<WinnerDetails>();
					}
					winnersDetails.add(details);
					lastResults.put(price, winnersDetails);
				}
			}
		}

		return lastResults;
	}

	private Boolean runRemoteAuction() {
		// get the remote bidders
		List<RemoteSellerDetails> remoteBidders = getPackagedRemoteBidders();

		// run rounds
		int roundNum = getInitRoundNumber();

		// last calls tracker
		Boolean lastCallSuccess = false; // did the last call succeed?
		int numLastCalls = 0; // bound the number of last calls to offset
								// byzantine bidders

		// results tracker
		TreeMap<Double, List<WinnerDetails>> lastResults = getInitLastResults(roundNum);
		TreeMap<Double, List<WinnerDetails>> currentResults = null;

		while (!lastCallSuccess && numLastCalls < maxLastCalls) {
			while (currentResults == null
					|| !areSame(currentResults, lastResults)) {
				lastResults = currentResults;
				System.out.println(remoteBidders);
				if(remoteBidders.size() > 0) {
					RoundResults results = runRound(remoteBidders, lastResults,
							false, roundNum);
					currentResults = results.getBids();
					remoteBidders = results.getRemoteBidders();
	
					makeRemoteRoundEntry(roundNum, currentResults);
					roundNum++;
				} else {
					break;
				}
			}

			// we are ready for a last call because the bids have stabilized
			numLastCalls++;
			lastResults = currentResults;
			if(remoteBidders.size() > 0) {
				RoundResults results = runRound(remoteBidders, lastResults, true,
						roundNum);
				currentResults = results.getBids();
				remoteBidders = results.getRemoteBidders();
	
				makeRemoteRoundEntry(roundNum, currentResults);
				roundNum++;
	
				lastCallSuccess = areSame(currentResults, lastResults);
			} else {
				break;
			}
		}

		System.out.println("Winners found: ");
		for (Entry<Double, List<WinnerDetails>> entry : currentResults
				.entrySet()) {
			System.out.println("For Price: " + entry.getKey());
			for (WinnerDetails winner : entry.getValue()) {
				System.out.println("Winner found: " + winner.getSellerID());
			}
		}
		return true;
	}

	private List<RemoteSellerDetails> getPackagedRemoteBidders() {
		List<RemoteSellerDetails> packagedRemoteBidders = new ArrayList<RemoteSellerDetails>();

		List<BasicDBObject> remoteBidders = getRemoteBidders();
		for (BasicDBObject bidder : remoteBidders) {
			String productID = bidder.getString(FIELD_PRODUCT_ID);
			String sellerID = bidder.getString(FIELD_SELLER_ID);
			String remoteAddress = bidder.getString(FIELD_REMOTE_ADDRESS);
			RemoteSellerDetails remoteSeller = new RemoteSellerDetails(
					remoteAddress, sellerID, productID);
			packagedRemoteBidders.add(remoteSeller);
		}

		return packagedRemoteBidders;
	}

	private Boolean areSame(
			TreeMap<Double, List<WinnerDetails>> currentResults,
			TreeMap<Double, List<WinnerDetails>> lastResults) {
		if (currentResults == null || lastResults == null) {
			return false;
		}

		if (lastResults.size() != currentResults.size()) {
			return false;
		}

		for (Entry<Double, List<WinnerDetails>> entry : currentResults
				.entrySet()) {
			Double key = entry.getKey();
			List<WinnerDetails> bidders = entry.getValue();
			if (lastResults.containsKey(key)) {
				if (!equalLists(lastResults.get(key), bidders)) {
					return false;
				}
			} else {
				return false;
			}
		}

		return true;
	}

	public Boolean equalLists(List<WinnerDetails> sellerList1,
			List<WinnerDetails> sellerList2) {

		if (sellerList1.size() != sellerList2.size()) {
			return false;
		}

		List<String> list1 = new ArrayList<String>();
		List<String> list2 = new ArrayList<String>();

		for (int i = 0; i < sellerList1.size(); i++) {
			list1.add(sellerList1.get(i).getProductID());
			list2.add(sellerList2.get(i).getProductID());
		}

		Collections.sort(list1);
		Collections.sort(list2);

		for (int i = 0; i < list1.size(); i++) {
			if (!list1.get(i).equals(list2.get(i))) {
				return false;
			}
		}

		return true;
	}

	private RoundResults runRound(List<RemoteSellerDetails> remoteBidders,
			TreeMap<Double, List<WinnerDetails>> lastResults, Boolean lastCall,
			int roundNum) {
		// contact each remote bidder and ask him if he wants to bid lower than
		// the auction results

		// new result data structures
		TreeMap<Double, List<RemoteSellerDetails>> newBids = new TreeMap<Double, List<RemoteSellerDetails>>();
		List<RemoteSellerDetails> newRemoteBidders = new ArrayList<RemoteSellerDetails>();

		// iterate through each remote bidder and ask if they want to bid
		List<Callable<RemoteSellerDetails>> remoteBiddersList = new ArrayList<Callable<RemoteSellerDetails>>();
		for (int i = 0; i < remoteBidders.size(); i++) {
			RemoteSellerDetails remoteBidderObj = remoteBidders.get(i); // get
																		// remote
			// bidder at
			// position i
			RemoteBidder remoteBidder = new RemoteBidder(remoteBidderObj,
					lastResults, roundNum);
			remoteBiddersList.add(remoteBidder);
			// Threadit
		}

		ExecutorService es = Executors.newFixedThreadPool(100);
		System.out.println("********##### Contacting "
				+ remoteBiddersList.size() + " number of remoteBidders");
		List<Future<RemoteSellerDetails>> resultList = null;
		try {
			resultList = es.invokeAll(remoteBiddersList, 5, TimeUnit.SECONDS);
			System.out.println("Size of returned resultList is:"
					+ resultList.size());
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

		List<RemoteSellerDetails> valueList;

		if (resultList != null) {
			for (Future<RemoteSellerDetails> f : resultList) {
				RemoteSellerDetails remoteBidderDetails = null;
				try {
					try {
						remoteBidderDetails = f.get();
					} catch (CancellationException ce) {
						System.out
								.println("########Ignoring timed out futures");
						continue; // Ignore as they would not be added to
									// newBids or newRemoteBidders
					}
					if(remoteBidderDetails == null){
						continue;
					}
					Double bidPrice = remoteBidderDetails.getPrice();
					System.out.println("*******Bid price is: " + bidPrice);
					if (bidPrice > 0) {
						System.out.println("Trying to find if key exists for "
								+ bidPrice);
						if (newBids.containsKey(bidPrice)) {
							valueList = newBids.get(bidPrice);
						} else {
							valueList = new ArrayList<RemoteSellerDetails>();
						}
						valueList.add(remoteBidderDetails);
						newBids.put(remoteBidderDetails.getPrice(), valueList);
						newRemoteBidders.add(remoteBidderDetails);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}
		es.shutdown();
		/*
		 * sellers.add(remoteBidder); newBids.put(bid, sellers); // put the bid
		 * newRemoteBidders.add(remoteBidder); // add as possible bidder for //
		 * next round
		 */
		// getBid(remoteBidder, newRemoteBidders, lastResults,
		// newBids,roundNum); // get his
		// bid

		int winnersNum = 0;
		TreeMap<Double, List<WinnerDetails>> winBids = new TreeMap<Double, List<WinnerDetails>>();
		for (Entry<Double, List<RemoteSellerDetails>> entry : newBids
				.entrySet()) {
			Double price = entry.getKey();
			List<RemoteSellerDetails> sellersDetails = entry.getValue();

			List<WinnerDetails> winnersDetails = WinnerDetails
					.getRemoteWinnersDetails(price, sellersDetails);

			winBids.put(price, winnersDetails);

			winnersNum += winnersDetails.size();
			if (winnersNum >= MIN_WINNERS) {
				break;
			}
		}

		RoundResults results = new RoundResults(winBids, newRemoteBidders);
		return results;
	}

	private class RemoteBidder implements Callable<RemoteSellerDetails> {

		private RemoteSellerDetails remoteBidder;
		private TreeMap<Double, List<WinnerDetails>> lastResults;
		private int roundNum;

		public RemoteBidder(RemoteSellerDetails remoteBidder,
				TreeMap<Double, List<WinnerDetails>> lastResults, int roundNum) {
			super();
			this.remoteBidder = remoteBidder;
			this.lastResults = lastResults;
			this.roundNum = roundNum;
		}

		@Override
		public RemoteSellerDetails call() throws Exception {
			Set<Double> oldBids = new HashSet<Double>();

			if (lastResults != null) {
				oldBids.addAll(lastResults.keySet());
			}
			String remoteAddress = remoteBidder.getRemoteAddress();
			System.out.println("Remote address: " + remoteAddress);

			ClientResponse response = null;
			ClientConfig config = new DefaultClientConfig();
			Client client = Client.create(config);
			WebResource webResource = client.resource(remoteAddress);
			RemoteAuctionDetails remoteDetails = new RemoteAuctionDetails();

			remoteDetails.setAuctionId(getAuctionWriter().getAuctionID());
			remoteDetails.setOldBids(oldBids);
			remoteDetails.setRoundNumber(roundNum);
			BidDetails bidDetails = null;
			try {
				response = webResource.type(MediaType.APPLICATION_JSON).post(
						ClientResponse.class, remoteDetails);
				// System.out.println("Client recieved the status  of"+response.getStatus());
				if(response != null && response.getStatus() == 200){
					bidDetails = response.getEntity(BidDetails.class);
					System.out.println("In round: " + roundNum
							+ " with madeBid as " + bidDetails.getMadeBid()
							+ " got back bid of price " + bidDetails.getBid());
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}

			// IMPORTANT: oldBids may be null. Handle the case. If it is null,
			// then
			// this is the first round
			// make a URI call to remote address.
			// convert the response to BidDetails Object

			if (bidDetails == null) {
				System.out.println("Bid Details are null!!");
				return null;
			} else {
				Double bid = -1d;
				if (bidDetails.getMadeBid()) {
					bid = bidDetails.getBid();
				}

				remoteBidder.setPrice(bid);
				System.out
						.println("In call method remoteBidder price is set to "
								+ remoteBidder.getPrice());
				return remoteBidder;
				/*
				 * sellers.add(remoteBidder); newBids.put(bid, sellers); // put
				 * the bid newRemoteBidders.add(remoteBidder); // add as
				 * possible bidder for // next round
				 */
			}
		}

	}

	private void getBid(RemoteSellerDetails remoteBidder,
			List<RemoteSellerDetails> newRemoteBidders,
			TreeMap<Double, List<WinnerDetails>> lastResults,
			TreeMap<Double, List<RemoteSellerDetails>> newBids, int roundNum) {
		Set<Double> oldBids = new HashSet<Double>();
		if (lastResults != null) {
			oldBids.addAll(lastResults.keySet());
		}
		String remoteAddress = remoteBidder.getRemoteAddress();
		System.out.println("Remote address: " + remoteAddress);

		ClientResponse response = null;
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		WebResource webResource = client.resource(remoteAddress);
		RemoteAuctionDetails remoteDetails = new RemoteAuctionDetails();

		remoteDetails.setAuctionId("123");
		remoteDetails.setOldBids(oldBids);
		remoteDetails.setRoundNumber(roundNum);
		BidDetails bidDetails = null;
		try {
			response = webResource.type(MediaType.APPLICATION_JSON).post(
					ClientResponse.class, remoteDetails);
			// System.out.println("Client recieved the status  of"+response.getStatus());
			bidDetails = response.getEntity(BidDetails.class);
			System.out.println("In round:" + roundNum
					+ " got back bid of price " + bidDetails.getBid());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// IMPORTANT: oldBids may be null. Handle the case. If it is null, then
		// this is the first round
		// make a URI call to remote address.
		// convert the response to BidDetails Object

		if (bidDetails == null) {
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
		return writer.makeInitEntry();
	}

	private Boolean makeLocalWinnersEntry(
			TreeMap<Double, List<WinnerDetails>> winners) {
		AuctionServerPersistance writer = getAuctionWriter();
		return writer.persistLocalBidWinners(winners);
		// return true;
	}

	private Boolean makeRemoteRoundEntry(int roundNum,
			TreeMap<Double, List<WinnerDetails>> winners) {
		AuctionServerPersistance writer = getAuctionWriter();
		return writer.persistRemoteRoundWinners(roundNum, winners);
		// return true;
	}

	private Boolean finishUpAuction() {
		AuctionServerPersistance writer = getAuctionWriter();
		return writer.finishUpAuction();
		// return true;
	}
}
