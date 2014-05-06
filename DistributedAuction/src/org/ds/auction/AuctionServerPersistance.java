package org.ds.auction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.ds.client.DBClient;
import org.ds.userServer.UserPersistance;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;

public class AuctionServerPersistance {
	
	private class RoundResults{
		private BasicDBObject roundResults;
		
		public BasicDBObject getRoundResults() {
			return this.roundResults;
		}
		
		public RoundResults(int roundNum, TreeMap<Double, List<WinnerDetails>> winners){
			this.roundResults = new BasicDBObject();
			this.roundResults.append(FIELD_ROUND_NUM, roundNum);
			this.roundResults.append(FIELD_BIDS, getBids(winners));
		}
		
		private BasicDBList getBids(TreeMap<Double, List<WinnerDetails>> winners){
			BasicDBList bids = new BasicDBList();
			for(Entry<Double,List<WinnerDetails>> entry: winners.entrySet()){
				Double price = entry.getKey();
				List<WinnerDetails> winnersDetails = entry.getValue();
				for(WinnerDetails details:winnersDetails){
					BasicDBObject bidDetails = new BasicDBObject(FIELD_PRODUCT_ID, details.getProductID())
																	.append(FIELD_SELLER_ID, details.getSellerID())
																	.append(FIELD_BID, price)
																	.append(FIELD_TOKEN, details.getToken());
					bids.add(bidDetails);
				}
			}
			
			return bids;
		}
		
	}
	
	private String auctionID;
	private BuyerCriteria criteria;
	private int version;
	private UserPersistance userWriter;
	
	public static String FIELD_AUCTION_ID = "auctionID";
	public static String FIELD_STATUS = "status";
	public static String FIELD_USER_ID = "userID";
	public static String FIELD_INITIATED_AT = "initiatedAt";
	public static String FIELD_VIEWED_AT = "viewedAt";
	public static String FIELD_FINISHED_AT = "finishedAt";
	public static String FIELD_BUYER_CRITERIA = "buyerCriteria";
	public static String FIELD_REMOTE_RESULTS = "remoteResults";
	public static String FIELD_LOCAL_RESULTS = "localResults";
	public static String FIELD_VERSION = "version";
	public static String FIELD_ROUND_NUM = "roundNum";
	public static String FIELD_BIDS = "bids";
	public static String FIELD_PRODUCT_ID ="productId";
	public static String FIELD_SELLER_ID ="sellerID";
	public static String FIELD_BID = "bid";
	public static String FIELD_TOKEN = "token";
	public static String FIELD_CLAIMED = "claimed";
	
	
	public static String MONGO_RESPONSE_FIELD_OK = "ok";
	public static String MONGO_RESPONSE_FIELD_ERR = "err";
	
	private void setAuctionID(String id){
		this.auctionID = id;
	}
	
	public String getAuctionID(){
		return this.auctionID;
	}
	
	private void setBuyerCriteria(BuyerCriteria criteria){
		this.criteria = criteria;
	}
	
	public BuyerCriteria getBuyerCriteria(){
		return this.criteria;
	}
	
	public int getVersion(){
		return this.version;
	}
	
	public int incrementVersion(){
		this.version += 1;
		return getVersion();
	}
	
	public UserPersistance getUserWriter(){
		return this.userWriter;
	}
	
	public void initUserWriter(){
		this.userWriter = new UserPersistance(getBuyerCriteria().getBuyerID());;
	}
	
	public AuctionServerPersistance(BuyerCriteria criteria){
		this.version = 1;
		setBuyerCriteria(criteria);
		initUserWriter();
	}
	
	public AuctionServerPersistance(BuyerCriteria criteria, String auctionID, int version){
		this.version = version;
		setAuctionID(auctionID);
		setBuyerCriteria(criteria);
		initUserWriter();
	}
	
	private MongoClient getMongoClient(){
		DBClient dbClient = DBClient.getInstance();
		MongoClient mongoClient = dbClient.getMongoClient();
		
		return mongoClient;
	}
	
	public Boolean makeInitEntry(){
		BuyerCriteria criteria = getBuyerCriteria();
		String buyerID = criteria.getBuyerID();
		BasicDBObject criteriaBSON = criteria.packageToBSON();
		Date initiatedAt = new Date();
		
		setAuctionID(buyerID + "_" + String.valueOf(initiatedAt.getTime()));
		String status = AuctionServer.STATUS_RUNNING;
		RoundResults remoteResults = new RoundResults(0, new TreeMap<Double, List<WinnerDetails>>());
		
		getUserWriter().recordAuctionInit(getAuctionID(), initiatedAt);
		
		BasicDBObject entry = new BasicDBObject(FIELD_AUCTION_ID, getAuctionID())
												.append(FIELD_STATUS, status)
												.append(FIELD_USER_ID, buyerID)
												.append(FIELD_INITIATED_AT, initiatedAt)
												.append(FIELD_BUYER_CRITERIA, criteriaBSON)
												.append(FIELD_REMOTE_RESULTS, remoteResults.getRoundResults())
												.append(FIELD_VERSION, getVersion());
		
		System.out.println("Making entry: " + entry);
		
		return insertIntoMongo(entry);
	}
	
	public Boolean persistLocalBidWinners(TreeMap<Double, List<WinnerDetails>> winners){
		String auctionID = getAuctionID();
		
		int oldVersion = getVersion();
		int newVersion = incrementVersion();
		RoundResults localResults = new RoundResults(1, winners);
		
		BasicDBObject entry = new BasicDBObject(FIELD_LOCAL_RESULTS, localResults.getRoundResults())
													.append(FIELD_VERSION, newVersion);
		BasicDBObject query = new BasicDBObject(FIELD_AUCTION_ID, auctionID)
												.append(FIELD_VERSION, oldVersion);
		BasicDBObject update = new BasicDBObject("$set", entry);
		
		System.out.println("Making query: " + query);
		System.out.println("Making update: " + update);
		
		return updateMongo(query, update);
	}
	
	public Boolean updateOnClaim(BasicDBObject localResults){
		String auctionID = getAuctionID();
		
		int oldVersion = getVersion();
		int newVersion = incrementVersion();
		
		BasicDBObject entry = new BasicDBObject(FIELD_LOCAL_RESULTS, localResults)
													.append(FIELD_VERSION, newVersion);
		BasicDBObject query = new BasicDBObject(FIELD_AUCTION_ID, auctionID)
												.append(FIELD_VERSION, oldVersion);
		BasicDBObject update = new BasicDBObject("$set", entry);
		
		System.out.println("Making query: " + query);
		System.out.println("Making update: " + update);
		
		return updateMongo(query, update);
	}
	
	public Boolean persistRemoteRoundWinners(int roundNum, TreeMap<Double, List<WinnerDetails>> winners){
		String auctionID = getAuctionID();
		
		int oldVersion = getVersion();
		int newVersion = incrementVersion();
		String status = AuctionServer.STATUS_RUNNING;
		RoundResults remoteResults = new RoundResults(roundNum, winners);
		
		BasicDBObject entry = new BasicDBObject(FIELD_REMOTE_RESULTS, remoteResults.getRoundResults())
													.append(FIELD_VERSION, newVersion);
		BasicDBObject query = new BasicDBObject(FIELD_AUCTION_ID, auctionID)
													.append(FIELD_VERSION, oldVersion);
		BasicDBObject update = new BasicDBObject("$set", entry);
		
		//System.out.println("Making query: " + query);
		//System.out.println("Making update: " + update);
		
		return updateMongo(query, update);
	}
	
	public Boolean finishUpAuction(){
		String auctionID = getAuctionID();
		
		int oldVersion = getVersion();
		int newVersion = incrementVersion();
		
		Date finishedAt = new Date();
		
		String status = AuctionServer.STATUS_FINISHED;
		
		getUserWriter().recordAuctionEnd(getAuctionID(), finishedAt);
		
		BasicDBObject entry = new BasicDBObject(FIELD_FINISHED_AT, finishedAt)
													.append(FIELD_STATUS, status)
													.append(FIELD_VERSION, newVersion);
		BasicDBObject query = new BasicDBObject(FIELD_AUCTION_ID, auctionID)
												.append(FIELD_VERSION, oldVersion);
		BasicDBObject update = new BasicDBObject("$set", entry);
		
		//System.out.println("Making query: " + query);
		//System.out.println("Making update: " + update);
		
		return updateMongo(query, update); // changeit to simulate auction failure
		
	}
	
	public Boolean recordViewedAt(){
		String auctionID = getAuctionID();
		
		int oldVersion = getVersion();
		int newVersion = incrementVersion();
		
		Date date = new Date();
		BasicDBObject entry = new BasicDBObject(FIELD_VIEWED_AT, date)
												.append(FIELD_VERSION, newVersion);
		BasicDBObject query = new BasicDBObject(FIELD_AUCTION_ID, auctionID)
												.append(FIELD_VERSION, oldVersion);
		BasicDBObject update = new BasicDBObject("$set", entry);
		return updateMongo(query, update);
	}
	
	private Boolean insertIntoMongo(BasicDBObject entry){
		Boolean success = true;
		DBCollection coll = getCollection();
		if (coll != null) {
			success = verifyNoError(coll.insert(entry));
		} else {
			System.out.println("Failed to get collection: "
					+ DBClient.AUCTIONS_DETAILS);
			success = false;
		}
		return success;
	}
	
	private Boolean updateMongo(BasicDBObject query, BasicDBObject update){
		Boolean success = true;
		DBCollection coll = getCollection();
		if (coll != null) {
			success = verifyNoError(coll.update(query, update));
		} else {
			System.out.println("Failed to get collection: "
					+ DBClient.AUCTIONS_DETAILS);
			success = false;
		}
		return success;
	}
	
	private DB getDB(){
		MongoClient mongoClient = getMongoClient();
		return mongoClient.getDB(DBClient.CAR_VENDORS_DB);
	}
	
	private DBCollection getCollection(){
		DB db = getDB();
		DBCollection coll = null;
		if(db != null){
			coll = db.getCollection(DBClient.AUCTIONS_DETAILS);
		} else {
			System.out.println("Failed to get DB: " + DBClient.CAR_VENDORS_DB);
		}
		
		return coll;
	}
	
	private Boolean verifyNoError(WriteResult result){
		System.out.println("Result of operation: " + result);
		return true;
	}
}
