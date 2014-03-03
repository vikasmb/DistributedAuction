package org.ds.client;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import org.ds.carServer.*;

//Singleton class holding MongoClient instance
public class DBClient {
	//DB Names
	public static String INDEX_DB = "serviceIndex";
	public static String CAR_VENDORS_DB = "carVendorsDB";
	
	//Collection names
	public static String INDEX_COLLECTION = "indexCollection";
	public static String CAR_VENDORS_DETAILS = "carVendorsDetails";
	
	//instance variables
	private static DBClient _instance;
	private MongoClient mongoClient;

	/**
	 * Constructor
	 */
	private DBClient() {
		try {
			mongoClient = new MongoClient(Arrays.asList(new ServerAddress("127.0.0.1", 27017)));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * gets a single instance. Creates one if instance does not exist
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
					clusterAddr= dbObj;
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
	
	/**
	 * persists the seller details in mongo
	 * @param sellerDetails
	 * @return success
	 */
	public Boolean persistSellerDetails(SellerDetails sellerDetails) {
		Boolean success = true;
		DB db = mongoClient.getDB(CAR_VENDORS_DB);
		if(db != null) {
			DBCollection coll = db.getCollection(CAR_VENDORS_DETAILS);
			if(coll != null) {
				coll.insert(sellerDetails.packageToBSON());
			} else {
				System.out.println("Failed to get collection: " + CAR_VENDORS_DETAILS);
				success = false;
			}
		} else {
			System.out.println("Failed to get DB: " + CAR_VENDORS_DB);
			success = false;
		}
		
		return success;
	}
}
