package org.ds.resources;

import org.ds.auction.AuctionServerPersistance;
import org.ds.client.DBClient;
import org.ds.userServer.UserPersistance;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

public class Subscriber {
	
	public static void main(String args[]){
		Subscriber subscriber = new Subscriber();
		subscriber.subscribe("123", "123_1398545770927");
	}
	
	public void subscribe(String userID, String auctionID){
		BasicDBObject auctionData = getAuctionData(auctionID);
		BasicDBObject criteraBSON = (BasicDBObject)auctionData.get(AuctionServerPersistance.FIELD_BUYER_CRITERIA);
		
		UserPersistance persistance = new UserPersistance(userID);
		persistance.recordSubscription(auctionID, criteraBSON);
	}
	
	public BasicDBObject getAuctionData(String auctionID){
		BasicDBObject query = new BasicDBObject(AuctionServerPersistance.FIELD_AUCTION_ID, auctionID);
		BasicDBObject auctionData = readMongo(query, DBClient.AUCTIONS_DETAILS);
		//System.out.println("Read result : " + auctionData);
		
		return auctionData;
		
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
	
	private MongoClient getMongoClient(){
		DBClient dbClient = DBClient.getInstance();
		MongoClient mongoClient = dbClient.getMongoClient();
		
		return mongoClient;
	}
	
	public BasicDBObject readMongo(BasicDBObject query, String collectionName){
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

}
