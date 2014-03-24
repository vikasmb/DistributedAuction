package org.ds.claim;

import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.ds.auction.AuctionServer;
import org.ds.auction.AuctionServerPersistance;
import org.ds.auction.BuyerCriteria;
import org.ds.claim.ClaimServer.AvailibiltyData;
import org.ds.client.DBClient;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;

public class ClaimServerPersistance {
	
	private MongoClient getMongoClient(){
		DBClient dbClient = DBClient.getInstance();
		MongoClient mongoClient = dbClient.getMongoClient();
		
		return mongoClient;
	}
	
	public BasicDBObject getProductData(String productID){
		BasicDBObject query = new BasicDBObject(AuctionServer.FIELD_PRODUCT_ID, productID);
		BasicDBObject productData = readMongo(query, DBClient.CAR_VENDORS_DETAILS);
		System.out.println("Read result : " + productData);
		
		return productData;
	}
	
	public BasicDBObject getAuctionData(String auctionID){
		BasicDBObject query = new BasicDBObject(AuctionServerPersistance.FIELD_AUCTION_ID, auctionID);
		BasicDBObject auctionData = readMongo(query, DBClient.AUCTIONS_DETAILS);
		System.out.println("Read result : " + auctionData);
		
		return auctionData;
		
	}
	
	private Boolean updateMongo(BasicDBObject query, BasicDBObject update, String collectionName){
		Boolean success = true;
		DBCollection coll = getCollection(collectionName);
		if (coll != null) {
			success = verifyNoError(coll.update(query, update));
		} else {
			System.out.println("Failed to get collection: "
					+ collectionName);
			success = false;
		}
		return success;
	}
	
	
	private BasicDBObject readMongo(BasicDBObject query, String collectionName){
		DBCursor cursor;
		BasicDBObject queryResult = null;
		DBCollection coll = getCollection(collectionName);
		if (coll != null) {
			cursor = coll.find(query);
			try {
				while (cursor.hasNext()) {
					queryResult = (BasicDBObject) cursor.next();
				}
			} finally {
				cursor.close();
			}
		} else {
			System.out.println("Failed to get collection: "
					+ collectionName);
		}
		return queryResult;
	}
	
	private DB getDB(){
		MongoClient mongoClient = getMongoClient();
		return mongoClient.getDB(DBClient.CAR_VENDORS_DB);
	}
	
	private DBCollection getCollection(String collectionName){
		DB db = getDB();
		DBCollection coll = null;
		if(db != null){
			coll = db.getCollection(collectionName);
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
