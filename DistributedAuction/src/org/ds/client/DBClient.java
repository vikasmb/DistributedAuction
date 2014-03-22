package org.ds.client;

import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import org.ds.auction.BidderDetails;
import org.ds.carServer.*;
import org.ds.util.DateUtil;

//Singleton class holding MongoClient instance
public class DBClient {
	// DB Names
	public static String INDEX_DB = "serviceIndex";
	public static String CAR_VENDORS_DB = "carVendorsDB";

	// Collection names
	public static String INDEX_COLLECTION = "indexCollection";
	public static String CAR_VENDORS_DETAILS = "carVendorsDetails";

	// instance variables
	private static DBClient _instance;
	private MongoClient mongoClient;

	public MongoClient getMongoClient(){
		return this.mongoClient;
	}
	
	/**
	 * Constructor
	 */
	private DBClient() {
		try {
			mongoClient = new MongoClient(Arrays.asList(new ServerAddress(
					"10.0.0.20", 27017)));
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
		if (_instance == null) {
			_instance = new DBClient();
		}
		return _instance;
	}

	/**
	 * Gets cluster address given category
	 * 
	 * @param clusterCategory
	 * @return the cluster address if it exists
	 */
	public BasicDBObject getClusterAddress(String clusterCategory) {
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
			String sellerCity, String fromTime, String tillTime) {
		List<BasicDBObject> localSellersList = null;
		List<BasicDBObject> remoteSellersList = null;

		DB db = mongoClient.getDB(CAR_VENDORS_DB);
		if (db != null) {
			DBCollection coll = db.getCollection(CAR_VENDORS_DETAILS);
			if (coll != null) {
				localSellersList = getLocalBidders(coll, sellerCity, fromTime,
						tillTime);
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

		try {
			while (cursor.hasNext()) {
				// System.out.println( cursor.next().get("address"));
				BasicDBObject dbObj = (BasicDBObject) cursor.next();
				remoteSellersList.add(dbObj);
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
	public Boolean persistSellerDetails(SellerDetails sellerDetails) {
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
}
