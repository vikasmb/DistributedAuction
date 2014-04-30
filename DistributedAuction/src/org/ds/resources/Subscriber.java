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
		UserPersistance persistance = new UserPersistance(userID);
		persistance.recordSubscription(auctionID);
	}
}
