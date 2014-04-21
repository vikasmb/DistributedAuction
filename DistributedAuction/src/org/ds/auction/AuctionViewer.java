package org.ds.auction;

import java.util.ArrayList;
import java.util.List;

import org.ds.client.DBClient;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class AuctionViewer {
	
	public static void main(String args[]){
		getAuctionResults("123_1398025840804");
		//getAuctionResults("123_1398025869321");
	}
	
	public static void getAuctionResults(String auctionID){
		BasicDBObject auctionDetails = getAuctionDetails(auctionID);
		if(auctionDetails != null){
			BasicDBList localBids = (BasicDBList)((BasicDBObject)auctionDetails
					.get(AuctionServerPersistance.FIELD_LOCAL_RESULTS))
					.get(AuctionServerPersistance.FIELD_BIDS);
			BasicDBList remoteBids = (BasicDBList)((BasicDBObject)auctionDetails
					.get(AuctionServerPersistance.FIELD_REMOTE_RESULTS))
					.get(AuctionServerPersistance.FIELD_BIDS);
			
			List <WinnerDetails> localWinners = new ArrayList<WinnerDetails>();
			List <WinnerDetails> remoteWinners = new ArrayList<WinnerDetails>();
			
			for(Object bid:localBids){
				BasicDBObject bidObj = (BasicDBObject)bid;
				WinnerDetails winnerDetails = new WinnerDetails(bidObj.getDouble(AuctionServerPersistance.FIELD_BID),
													bidObj.getString(AuctionServerPersistance.FIELD_SELLER_ID),
													bidObj.getString(AuctionServerPersistance.FIELD_PRODUCT_ID));
				localWinners.add(winnerDetails);
			}
			
			for(Object bid:remoteBids){
				BasicDBObject bidObj = (BasicDBObject)bid;
				WinnerDetails winnerDetails = new WinnerDetails(bidObj.getDouble(AuctionServerPersistance.FIELD_BID),
													bidObj.getString(AuctionServerPersistance.FIELD_SELLER_ID),
													bidObj.getString(AuctionServerPersistance.FIELD_PRODUCT_ID));
				remoteWinners.add(winnerDetails);
			}	
			
			System.out.println("For auction: " + auctionDetails.getString(AuctionServerPersistance.FIELD_AUCTION_ID));
			System.out.println("Local winners");
			for(WinnerDetails winner:localWinners){
				winner.printDetails();
			}
			
			System.out.println("Remote winners");
			for(WinnerDetails winner:remoteWinners){
				winner.printDetails();
			}
			
			if(!auctionDetails.containsField(AuctionServerPersistance.FIELD_VIEWED_AT)){
				System.out.println("Viewed at recorded");
				BuyerCriteria criteria = new BuyerCriteria((BasicDBObject)auctionDetails.get(AuctionServerPersistance.FIELD_BUYER_CRITERIA));
				AuctionServerPersistance writer = new AuctionServerPersistance(criteria, auctionID, auctionDetails.getInt(AuctionServerPersistance.FIELD_VERSION));
				writer.recordViewedAt();
			}
		} else {
			System.out.println("Auction still running!");
		}
	}
	
	private static BasicDBObject getAuctionDetails(String auctionID){
		BasicDBObject query = new BasicDBObject(AuctionServerPersistance.FIELD_AUCTION_ID, auctionID)
												.append(AuctionServerPersistance.FIELD_STATUS, AuctionServer.STATUS_FINISHED);
		DBCollection coll = getMongoClient().getDB(DBClient.CAR_VENDORS_DB).getCollection(DBClient.AUCTIONS_DETAILS);
        BasicDBObject auctionDetails = (BasicDBObject)coll.findOne(query);
        return auctionDetails;
	}
	
	private static DBClient getDBClient(){
		DBClient dbClient = DBClient.getInstance();
		return dbClient;
	}
	private static MongoClient getMongoClient(){
		DBClient dbClient =getDBClient();
		MongoClient mongoClient = dbClient.getMongoClient();
		
		return mongoClient;
	}
}
