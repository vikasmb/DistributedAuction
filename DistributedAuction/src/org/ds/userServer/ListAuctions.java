package org.ds.userServer;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class ListAuctions {
	public static void main(String args[]){
		String userID = "123";
		List<String> auctionIDs = getUserAuctions(userID);
		for(String id:auctionIDs){
			System.out.println(id);
		}
	}
	
	public static List<String> getUserAuctions(String userID){
		UserPersistance persistance = new UserPersistance(userID);
		BasicDBList auctions = persistance.getAuctions(userID);
		
		List<String> auctionIDs = new ArrayList<String>();
		for(Object auctionObj:auctions){
			BasicDBObject auction = (BasicDBObject)auctionObj;
			auctionIDs.add(auction.getString(UserPersistance.FIELD_AUCTIONS_CATEGORY) + ":" + auction.getString(UserPersistance.FIELD_AUCTIONS_AUCTION_ID));
		}
		
		return auctionIDs;
	}
	
}
