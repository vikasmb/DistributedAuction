package org.ds.carServer;

import java.util.ArrayList;
import java.util.List;

import org.ds.auction.AuctionServer;
import org.ds.auction.AuctionServerPersistance;
import org.ds.claim.ClaimServer;
import org.ds.client.DBClient;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

public class ViewProduct {
	
	public static void main(String args[]){
		String productID = "Tracy_Focus_18";
		Product product = getProduct(productID);
		
		System.out.println(product.toString());
	}
	
	public static Product getProduct(String productID){
		BasicDBObject productDetails = getProductDetails(productID);
		String city = productDetails.getString(AuctionServer.FIELD_CITY);
		String userID = productDetails.getString(AuctionServer.FIELD_SELLER_ID);
		int version = productDetails.getInt(AuctionServer.FIELD_VERSION);
		
		List<AvailabilityInterval> availability = getAvailabilityIntervals((BasicDBList)productDetails.get(ClaimServer.FIELD_AVAILABILITY));
		List<HourlyPrice> prices = getHourlyPrices((BasicDBList)productDetails.get(AuctionServer.FIELD_PRICE_DETAILS));
		
		return new Product(city,userID, productID, version,
			availability, prices);
	}
	
	private static List<AvailabilityInterval> getAvailabilityIntervals(BasicDBList availabilityObj){
		List <AvailabilityInterval> availability = new ArrayList<AvailabilityInterval>();
		for(Object intervalObj: availabilityObj){
			BasicDBObject interval = (BasicDBObject)intervalObj;
			availability.add(new AvailabilityInterval(interval.getDate(ClaimServer.FIELD_FROM_DATE), interval.getDate(ClaimServer.FIELD_TO_DATE)));
		}
		
		return availability;
	}
	
	private static List<HourlyPrice> getHourlyPrices(BasicDBList pricesObj){
		List <HourlyPrice> prices = new ArrayList<HourlyPrice>();
		for(Object priceObj: pricesObj){
			BasicDBObject price = (BasicDBObject)priceObj;
			prices.add(new HourlyPrice(
										price.getDate(AuctionServer.FIELD_HOUR),
										price.getDouble(AuctionServer.FIELD_LIST_PRICE),
										price.getDouble(AuctionServer.FIELD_MIN_PRICE)));
		}
		
		return prices;
	}
	
	
	private static BasicDBObject getProductDetails(String productID){
		BasicDBObject query = new BasicDBObject(AuctionServer.FIELD_PRODUCT_ID, productID);
		DBCollection coll = getMongoClient().getDB(DBClient.CAR_VENDORS_DB).getCollection(DBClient.CAR_VENDORS_DETAILS);
		return (BasicDBObject)coll.findOne(query);
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


