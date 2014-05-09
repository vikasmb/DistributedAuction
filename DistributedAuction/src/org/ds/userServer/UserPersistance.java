package org.ds.userServer;

import java.util.Date;

import org.ds.client.DBClient;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;

public class UserPersistance {
	
	//main keys
	public static String FIELD_USER_ID = "userID";
	public static String FIELD_AUCTIONS = "auctions";
	public static String FIELD_SUBSCRIPTIONS = "subscriptions";
	public static String FIELD_PRODUCTS = "products";
	public static String FIELD_VERSION = "version";
	
	//auctions keys
	public static String FIELD_AUCTIONS_CATEGORY = "category";
	public static String FIELD_AUCTIONS_AUCTION_ID = "auctionID";
	public static String FIELD_AUCTIONS_INITIATED_AT = "initiatedAt";
	public static String FIELD_AUCTIONS_FINISHED_AT = "finishedAt";
	
	//subscription keys
	public static String FIELD_SUBSCRIPTIONS_CATEGORY = "category";
	public static String FIELD_SUBSCRIPTION_AUCTION_ID = "auctionID";
	
	//product keys
	public static String FIELD_PRODUCTS_CATEGORY = "category";
	public static String FIELD_PRODUCTS_PRODUCT_ID = "productID";
	
	
	//category name
	public static String CATEGORY_CARS = "Cars";
	
	
	private String userID;
	private int version = 0;
	
	public String getUserID(){
		return this.userID;
	}
	
	public int getVersion(){
		return this.version;
	}
	
	private void setUserID(String userID){
		this.userID = userID;
	}
	
	private int incrementVersion(){
		this.version += 1;
		return getVersion();
	}
	
	private int initVersion(){
		BasicDBObject query = new BasicDBObject(FIELD_USER_ID, getUserID());
		BasicDBObject projectedFields = new BasicDBObject(FIELD_VERSION, 1);
		
		DBCollection coll = getCollection();
		BasicDBObject userDetails = (BasicDBObject)coll.findOne(query, projectedFields);
		
		this.version = userDetails != null ? userDetails.getInt(FIELD_VERSION) : 0;
		return this.version;
	}
	
	public UserPersistance(String userID){
		setUserID(userID);
	}
	
	public Boolean registerUser(){
		if(initVersion() == 0) {
			BasicDBObject userDetails = initUser();
			return insertIntoMongo(userDetails);
		} 
		
		System.out.println("User " + getUserID() + " already exists!");
		return false;
		
	}
	
	private BasicDBObject initUser(){
		return new BasicDBObject(FIELD_USER_ID, getUserID())
							.append(FIELD_AUCTIONS, new BasicDBList())
							.append(FIELD_SUBSCRIPTIONS, new BasicDBList())
							.append(FIELD_PRODUCTS, new BasicDBList())
							.append(FIELD_VERSION, incrementVersion());
	}
	
	public void recordSubscription(String auctionID){
		BasicDBObject subscriptionEntry = new BasicDBObject(FIELD_SUBSCRIPTION_AUCTION_ID, auctionID)
												.append(FIELD_SUBSCRIPTIONS_CATEGORY, CATEGORY_CARS);
		
		BasicDBObject query;
		BasicDBObject update;
		do{
			BasicDBList subscriptions = getSubscriptions();
			subscriptions.add(subscriptionEntry);
			
			query = new BasicDBObject(FIELD_USER_ID, getUserID())
											.append(FIELD_VERSION, getVersion());
			BasicDBObject updateFields = new BasicDBObject(FIELD_SUBSCRIPTIONS, subscriptions)
													.append(FIELD_VERSION, incrementVersion());
			update = new BasicDBObject("$set", updateFields);
		}while(!updateMongo(query, update));
	}
	
	public void recordProduct(String productID){
		BasicDBObject productEntry = new BasicDBObject(FIELD_PRODUCTS_PRODUCT_ID, productID)
												.append(FIELD_PRODUCTS_CATEGORY, CATEGORY_CARS);
		
		BasicDBObject query;
		BasicDBObject update;
		do{
			BasicDBList products = getProducts();
			products.add(productEntry);
			
			query = new BasicDBObject(FIELD_USER_ID, getUserID())
											.append(FIELD_VERSION, getVersion());
			BasicDBObject updateFields = new BasicDBObject(FIELD_PRODUCTS, products)
													.append(FIELD_VERSION, incrementVersion());
			update = new BasicDBObject("$set", updateFields);
		}while(!updateMongo(query, update));
	}
	
	public void recordAuctionInit(String auctionID, Date initiatedAt){
		BasicDBObject auctionEntry = new BasicDBObject(FIELD_AUCTIONS_CATEGORY, CATEGORY_CARS)
												.append(FIELD_AUCTIONS_AUCTION_ID, auctionID)
												.append(FIELD_AUCTIONS_INITIATED_AT, initiatedAt);
		BasicDBObject query;
		BasicDBObject update;
		do{
			BasicDBList auctions = getAuctions();
			auctions.add(auctionEntry);
			
			query = new BasicDBObject(FIELD_USER_ID, getUserID())
											.append(FIELD_VERSION, getVersion());
			BasicDBObject updateFields = new BasicDBObject(FIELD_AUCTIONS, auctions)
													.append(FIELD_VERSION, incrementVersion());
			update = new BasicDBObject("$set", updateFields);
		}while(!updateMongo(query, update));
	}
	
	public void recordAuctionEnd(String auctionID, Date finishedAt){
		BasicDBObject query;
		BasicDBObject update;
		do{
			BasicDBList auctions = getAuctions();
			for(Object auctionObj:auctions){
				BasicDBObject auction = (BasicDBObject)auctionObj;
				if(auction.get(FIELD_AUCTIONS_AUCTION_ID).equals(auctionID)){
					auction.append(FIELD_AUCTIONS_FINISHED_AT, finishedAt);
					break;
				}
			}
			
			query = new BasicDBObject(FIELD_USER_ID, getUserID())
											.append(FIELD_VERSION, getVersion());
			BasicDBObject updateFields = new BasicDBObject(FIELD_AUCTIONS, auctions)
													.append(FIELD_VERSION, incrementVersion());
			update = new BasicDBObject("$set", updateFields);
		}while(!updateMongo(query, update));
	}
		
	public BasicDBList getAuctions(){
		BasicDBObject query = new BasicDBObject(FIELD_USER_ID, getUserID());
		BasicDBObject projectedFields = new BasicDBObject(FIELD_AUCTIONS, 1)
												.append(FIELD_VERSION, 1);
		
		DBCollection coll = getCollection();
		BasicDBObject userDetails = (BasicDBObject)coll.findOne(query, projectedFields);
		
		this.version = userDetails.getInt(FIELD_VERSION);
		return (BasicDBList)userDetails.get(FIELD_AUCTIONS);
	}
	
	public BasicDBList getSubscriptions(){
		BasicDBObject query = new BasicDBObject(FIELD_USER_ID, getUserID());
		BasicDBObject projectedFields = new BasicDBObject(FIELD_SUBSCRIPTIONS, 1)
												.append(FIELD_VERSION, 1);
		
		DBCollection coll = getCollection();
		BasicDBObject userDetails = (BasicDBObject)coll.findOne(query, projectedFields);
		
		this.version = userDetails.getInt(FIELD_VERSION);
		return (BasicDBList)userDetails.get(FIELD_SUBSCRIPTIONS);
	}
	
	public BasicDBList getProducts(){
		BasicDBObject query = new BasicDBObject(FIELD_USER_ID, getUserID());
		BasicDBObject projectedFields = new BasicDBObject(FIELD_PRODUCTS, 1)
												.append(FIELD_VERSION, 1);
		
		DBCollection coll = getCollection();
		BasicDBObject userDetails = (BasicDBObject)coll.findOne(query, projectedFields);
		
		this.version = userDetails.getInt(FIELD_VERSION);
		return (BasicDBList)userDetails.get(FIELD_PRODUCTS);
	}
	
	private static MongoClient getMongoClient(){
		DBClient dbClient = DBClient.getInstance();
		MongoClient mongoClient = dbClient.getMongoClient();
		
		return mongoClient;
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
			success = coll.update(query, update).isUpdateOfExisting();
		} else {
			System.out.println("Failed to get collection: "
					+ DBClient.USERS_DETAILS);
			success = false;
		}
		return success;
	}
	
	private DB getDB(){
		MongoClient mongoClient = getMongoClient();
		return mongoClient.getDB(DBClient.USERS_DB);
	}
	
	private DBCollection getCollection(){
		DB db = getDB();
		DBCollection coll = null;
		if(db != null){
			coll = db.getCollection(DBClient.USERS_DETAILS);
		} else {
			System.out.println("Failed to get DB: " + DBClient.USERS_DB);
		}
		
		return coll;
	}
	
	private Boolean verifyNoError(WriteResult result){
		System.out.println("Result of operation: " + result);
		return true;
	}
	
	public static void main(String args[]){
		UserPersistance userWriter = new UserPersistance("456");
		userWriter.registerUser();
		//userWriter.recordAuctionInit("123", "123_987654", new Date());
		//userWriter.recordAuctionEnd("123", "123_987654", new Date());
	}
}
