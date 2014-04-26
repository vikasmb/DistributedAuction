package org.ds.subscriptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.ds.auction.AuctionServer;
import org.ds.auction.AuctionServerPersistance;
import org.ds.auction.BuyerCriteria;
import org.ds.auction.SellerDetails;
import org.ds.auction.WinnerDetails;
import org.ds.client.DBClient;
import org.ds.util.DateUtil;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

public class SubscriptionSweeper {
	public static int ACCEPT_BUFFER = 10;
	public static int RESULTS_LIMIT = 10;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BuyerCriteria criteria = new BuyerCriteria("123",
				DateUtil.getDate("2014-06-15T10:00:00"),
				DateUtil.getDate("2014-06-15T11:00:00"), "LA");
		
		Date viewedUntil = null;
		getSubscribedDeals(criteria, viewedUntil);
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
	
	private static void getSubscribedDeals(BuyerCriteria criteria, Date viewedUntil){		
		List<BasicDBObject> subscribedAuctions = getSubscribedAuctions(criteria, viewedUntil);
		for(BasicDBObject auction:subscribedAuctions){
			BasicDBList localBids = (BasicDBList)((BasicDBObject)auction
					.get(AuctionServerPersistance.FIELD_LOCAL_RESULTS))
					.get(AuctionServerPersistance.FIELD_BIDS);
			BasicDBList remoteBids = (BasicDBList)((BasicDBObject)auction
					.get(AuctionServerPersistance.FIELD_REMOTE_RESULTS))
					.get(AuctionServerPersistance.FIELD_BIDS);
			List <SubscriptionDetails> deals = new ArrayList<SubscriptionDetails>();
			
			List<String> productIDs = new ArrayList<String>();
			for(Object bid:localBids){
				BasicDBObject bidObj = (BasicDBObject)bid;
				if(!bidObj.getBoolean(AuctionServerPersistance.FIELD_CLAIMED)){
					productIDs.add(bidObj.getString(AuctionServerPersistance.FIELD_PRODUCT_ID));
				}
			}
			
			for(Object bid: remoteBids){
				BasicDBObject bidObj = (BasicDBObject)bid;
				productIDs.add(bidObj.getString(AuctionServerPersistance.FIELD_PRODUCT_ID));
			}
			
			DBClient client = DBClient.getInstance();
			Map<String, BasicDBObject> productDetails = client.getProductDetails(productIDs);
			for(String s:productDetails.keySet()){
				System.out.println(productDetails.get(s));
			}
			
			for(Object bid:localBids){
				BasicDBObject bidObj = (BasicDBObject)bid;
				if(!bidObj.getBoolean(AuctionServerPersistance.FIELD_CLAIMED)){
					BasicDBObject product = productDetails.get(bidObj.getString(AuctionServerPersistance.FIELD_PRODUCT_ID));
					WinnerDetails winnerDetails = new WinnerDetails(bidObj.getDouble(AuctionServerPersistance.FIELD_BID),
														product.getString(AuctionServer.FIELD_SELLER_ID),
														product.getString(AuctionServerPersistance.FIELD_PRODUCT_ID),
														product.getString(SellerDetails.FIELD_NAME),
														product.getString(SellerDetails.FIELD_MODEL),
														product.getString(SellerDetails.FIELD_ADDRESS),
														product.getString(SellerDetails.FIELD_IMAGE));
				
					deals.add(new SubscriptionDetails(criteria, winnerDetails));
				}
			}
			for(Object bid:remoteBids){
				BasicDBObject bidObj = (BasicDBObject)bid;
				BasicDBObject product = productDetails.get(bidObj.getString(AuctionServerPersistance.FIELD_PRODUCT_ID));
				WinnerDetails winnerDetails = new WinnerDetails(bidObj.getDouble(AuctionServerPersistance.FIELD_BID),
													product.getString(AuctionServer.FIELD_SELLER_ID),
													product.getString(AuctionServerPersistance.FIELD_PRODUCT_ID),
													product.getString(SellerDetails.FIELD_NAME),
													product.getString(SellerDetails.FIELD_ADDRESS),
													product.getString(SellerDetails.FIELD_IMAGE));
				deals.add(new SubscriptionDetails(criteria, winnerDetails));
			}	
			
			System.out.println("For auction: " + auction.getString(AuctionServerPersistance.FIELD_AUCTION_ID));
			for(SubscriptionDetails deal:deals){
				deal.printDetails();
			}
		}
		System.out.println("Done");
	}
	
	private static BasicDBObject getSubscribedAuctionsQuery(BuyerCriteria criteria, Date viewedUntil){
		if(viewedUntil == null) {
			Calendar cal = Calendar.getInstance();
	        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
	        cal.add(Calendar.MINUTE, -ACCEPT_BUFFER);
	        viewedUntil = cal.getTime();
		}
        BasicDBObject query = new BasicDBObject(AuctionServerPersistance.FIELD_STATUS, AuctionServer.STATUS_FINISHED)
        							.append(AuctionServerPersistance.FIELD_VIEWED_AT, new BasicDBObject("$lte", viewedUntil))
        							.append(AuctionServerPersistance.FIELD_BUYER_CRITERIA + "." + BuyerCriteria.FIELD_CITY, criteria.getCity())
        							.append(AuctionServerPersistance.FIELD_BUYER_CRITERIA + "." + BuyerCriteria.FIELD_NEEDED_FROM, criteria.getNeededFrom())
        							.append(AuctionServerPersistance.FIELD_BUYER_CRITERIA + "." + BuyerCriteria.FIELD_NEEDED_UNTIL, criteria.getNeededUntil());
        
        return query;
	}
	
	private static List<BasicDBObject> getSubscribedAuctions(BuyerCriteria criteria, Date viewedUntil){
		BasicDBObject query = getSubscribedAuctionsQuery(criteria, viewedUntil);
		
		DBCollection coll = getMongoClient().getDB(DBClient.CAR_VENDORS_DB).getCollection(DBClient.AUCTIONS_DETAILS);
       
        List<BasicDBObject> subscribedAuctions = new ArrayList<BasicDBObject>();
        BasicDBObject orderBy = new BasicDBObject(AuctionServerPersistance.FIELD_VIEWED_AT, -1);
        DBCursor cursor = coll.find(query).sort(orderBy).limit(RESULTS_LIMIT);
        try {
			while (cursor.hasNext()) {
				BasicDBObject dbObj = (BasicDBObject) cursor.next();
				subscribedAuctions.add(dbObj);
			}     
		} finally {
			cursor.close();
		}
        
        return subscribedAuctions;
	}
}
