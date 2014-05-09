package org.ds.client;

import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import org.ds.auction.AuctionServer;
import org.ds.auction.BidderDetails;
import org.ds.carServer.*;
import org.ds.util.DateUtil;

//Singleton class holding MongoClient instance
public class DBClient {
	// DB Names
	public static String INDEX_DB = "serviceIndex";
	public static String CAR_VENDORS_DB = "carVendorsDB";
	public static String USERS_DB = "usersDB";

	// Collection names
	public static String INDEX_COLLECTION = "indexCollection";
	public static String CAR_VENDORS_DETAILS = "carVendorsDetails";
	public static String AUCTIONS_DETAILS = "auctionsDetails";
	public static String USERS_DETAILS = "usersDetails";

	// instance variables
	private static DBClient _instance;
	private MongoClient mongoClient;
	
	private String mongoIP = "127.0.0.1";
	private int mongoPort = 27017;

	public MongoClient getMongoClient(){
		return this.mongoClient;
	}
	
	/**
	 * Constructor
	 */
	private DBClient() {
		try {
			mongoClient = new MongoClient(Arrays.asList(new ServerAddress(
					mongoIP, mongoPort)));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * gets a single instance. Creates one if instance does not exist
	 * 
	 * @return instance of DB Client
	 */
	public static DBClient getInstance() {
		long startTime = System.currentTimeMillis();
		if (_instance == null) {
			_instance = new DBClient();
		}
		long difference = System.currentTimeMillis() - startTime;
		//System.out.println("Connection open Latency: " + difference);
		return _instance;
	}

	/**
	 * Gets cluster address given category
	 * 
	 * @param clusterCategory
	 * @return the cluster address if it exists
	 */
	public BasicDBObject getClusterAddress(String clusterCategory) { //TODO Load balancing for the cluster address
		DB db = mongoClient.getDB(INDEX_DB);
		BasicDBObject clusterAddr = null;
		if (db != null) {
			DBCollection coll = db.getCollection(INDEX_COLLECTION);
			BasicDBObject query = new BasicDBObject("category", clusterCategory);

			DBCursor cursor = coll.find(query);

			try {
				while (cursor.hasNext()) {
					// System.out.println( cursor.next().get("address"));
					BasicDBObject dbObj = (BasicDBObject) cursor.next().get(
							"address");
					clusterAddr = dbObj;
					// System.out.println( dbObj==null?"null":dbObj.get("ip"));
					// System.out.println( ( (BasicDBObject)
					// (cursor.next().get("address"))).get("ip"));
					// System.out.println(cursor.next());
				}
			} finally {
				cursor.close();
			}
		}
		return clusterAddr;
	}

	public BidderDetails getPotentialSellers(String clusterCategory,
			String sellerCity, String fromTime, String tillTime,boolean includeLocalSellers) {
		List<BasicDBObject> localSellersList = null;
		List<BasicDBObject> remoteSellersList = null;

		DB db = mongoClient.getDB(CAR_VENDORS_DB);
		if (db != null) {
			DBCollection coll = db.getCollection(CAR_VENDORS_DETAILS);
			if (coll != null) {
				if(includeLocalSellers){
				localSellersList = getLocalBidders(coll, sellerCity, fromTime,
						tillTime);
				}
				else{
					localSellersList = new ArrayList<BasicDBObject>();
				}
				remoteSellersList = getRemoteBidders(coll, sellerCity);
			}
		}
		BidderDetails bidderDetails = new BidderDetails(remoteSellersList,
				localSellersList);
		return bidderDetails;
	}

	private List<BasicDBObject> getLocalBidders(DBCollection coll,
			String sellerCity, String fromTime, String tillTime) {
		BasicDBObject query = new BasicDBObject("city", sellerCity);
		List<BasicDBObject> localSellersList = new ArrayList<BasicDBObject>();

		Date fromDate = DateUtil.getDate(fromTime);

		Date toDate = DateUtil.getDate(tillTime);

		BasicDBObject eleMatch = new BasicDBObject();
		eleMatch.put("from", new BasicDBObject("$lte", fromDate));
		eleMatch.put("till", new BasicDBObject("$gte", toDate));
		BasicDBObject up = new BasicDBObject();
		up.put("$elemMatch", eleMatch);
		query.append("availableTimes", up);
		// query.append("availableTimes.from", new
		// BasicDBObject("$lte",fromDate));
		// query.append("availableTimes.till", new
		// BasicDBObject("$gte",toDate));
		// System.out.println("QUerying"+query.toString());
		DBCursor cursor = coll.find(query);

		try {
			while (cursor.hasNext()) {
				// System.out.println( cursor.next().get("address"));
				BasicDBObject dbObj = (BasicDBObject) cursor.next();
				localSellersList.add(dbObj);
				System.out.println("Found: " + dbObj.getString("userId"));
			}
		} finally {
			cursor.close();
		}
		return localSellersList;

	}

	private List<BasicDBObject> getRemoteBidders(DBCollection coll,
			String sellerCity) {
		List<BasicDBObject> remoteSellersList = new ArrayList<BasicDBObject>();
		BasicDBObject query = new BasicDBObject("city", sellerCity);
		query.append("remote", new BasicDBObject("$ne", ""));
		DBCursor cursor = coll.find(query);
        final int REMOTE_SELLERS_LIMIT=100;
        int count=0;
		try {
			while (cursor.hasNext() && count<REMOTE_SELLERS_LIMIT) {
				// System.out.println( cursor.next().get("address"));
				BasicDBObject dbObj = (BasicDBObject) cursor.next();
				remoteSellersList.add(dbObj);
				count++;
				System.out.println("Found: " + dbObj.getString("userId"));
			}
		} finally {
			cursor.close();
		}
		return remoteSellersList;
	}

	/**
	 * persists the seller details in mongo
	 * 
	 * @param sellerDetails
	 * @return success
	 */
	public Boolean persistSellerDetails(CarSellerDetails sellerDetails) {
		Boolean success = true;
		DB db = mongoClient.getDB(CAR_VENDORS_DB);
		if (db != null) {
			DBCollection coll = db.getCollection(CAR_VENDORS_DETAILS);
			if (coll != null) {
				coll.insert(sellerDetails.packageToBSON());
			} else {
				System.out.println("Failed to get collection: "
						+ CAR_VENDORS_DETAILS);
				success = false;
			}
		} else {
			System.out.println("Failed to get DB: " + CAR_VENDORS_DB);
			success = false;
		}

		return success;
	}
	
	public Map<String, BasicDBObject> getProductDetails(List<String> productIDs){
		BasicDBObject query = constructGetProductDetailsQuery(productIDs);
		Map<String, BasicDBObject> productDetails =  new TreeMap<String, BasicDBObject>();
		DB db = mongoClient.getDB(CAR_VENDORS_DB);
		if (db != null) {
			DBCollection coll = db.getCollection(CAR_VENDORS_DETAILS);
			if (coll != null) {
				DBCursor cursor = coll.find(query);
				try {
					while (cursor.hasNext()) {
						BasicDBObject dbObj = (BasicDBObject) cursor.next();
						productDetails.put(dbObj.getString(AuctionServer.FIELD_PRODUCT_ID), dbObj);
					}
				} finally {
					cursor.close();
				}
			}
		}
		return productDetails;
	}
	
	private BasicDBObject constructGetProductDetailsQuery(List<String> productIDs){
		BasicDBList orParts = new BasicDBList();
		for(String productID: productIDs){
			BasicDBObject orPart = new BasicDBObject(AuctionServer.FIELD_PRODUCT_ID, productID);
			orParts.add(orPart);
		}
		
		BasicDBObject query = new BasicDBObject("$or", orParts);
		return query;
	}
}
