package org.ds.auction;

import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.ds.client.DBClient;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;

public class AuctionServerPersistance {
	
	private String auctionID;
	private int version;
	
	private void setAuctionID(String id){
		this.auctionID = id;
	}
	
	public String getAuctionID(){
		return this.auctionID;
	}
	
	public int getVersion(){
		return this.version;
	}
	
	public int incrementVersion(){
		this.version += 1;
		return getVersion();
	}
	
	public AuctionServerPersistance(){
		this.version = 1;
	}
	
	private MongoClient getMongoClient(){
		DBClient dbClient = DBClient.getInstance();
		MongoClient mongoClient = dbClient.getMongoClient();
		
		return mongoClient;
	}
	
	public Boolean makeInitEntry(String buyerID){
		MongoClient mongoClient = getMongoClient();
		Date date = new Date();
		String auctionID = buyerID + String.valueOf(date.getTime());
		String status = AuctionServer.STATUS_RUNNING;
		
		BasicDBObject entry;
		return true;
	}
	
	public Boolean persistLocalBidWinners(TreeMap<Double, List<WinnerDetails>> winners){
		MongoClient mongoClient = getMongoClient();
		return true;
	}
	
	public Boolean persistRemoteRoundWinners(int roundNum, AuctionResults results){
		MongoClient mongoClient = getMongoClient();
		return true;
	}
	
	public Boolean finishUpAuction(){
		MongoClient mongoClient = getMongoClient();
		return true;
	}
}
