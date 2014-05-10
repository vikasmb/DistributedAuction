package evaluations;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ds.auction.AuctionServerPersistance;
import org.ds.auction.BuyerCriteria;
import org.ds.client.DBClient;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

public class AuctionRunTimeEvaluator {

	public static void main(String args[]){
		List<Long> times = getAuctionRunTimes();
		for(Long time:times){
			System.out.println(time);
		}
	}
	
	public static List<Long> getAuctionRunTimes(){
		List<Long> times = new ArrayList<Long>();
		List<BasicDBObject> auctions= getAuctions();
		for(Object auctionObj:auctions){
			BasicDBObject auction = (BasicDBObject)auctionObj;
			Date startedAt = auction.getDate(AuctionServerPersistance.FIELD_INITIATED_AT);
			Date finishedAt = auction.getDate(AuctionServerPersistance.FIELD_FINISHED_AT);
			Long seconds = (finishedAt.getTime() - startedAt.getTime())/1000;
			times.add(seconds);
		}
		
		return times;
	}
	
	private static List<BasicDBObject> getAuctions() {
		BasicDBObject query = new BasicDBObject();

		DBCollection coll = getMongoClient().getDB(DBClient.CAR_VENDORS_DB)
				.getCollection(DBClient.AUCTIONS_DETAILS);

		List<BasicDBObject> auctions = new ArrayList<BasicDBObject>();
		DBCursor cursor = coll.find(query);
		try {
			while (cursor.hasNext()) {
				BasicDBObject dbObj = (BasicDBObject) cursor.next();
				auctions.add(dbObj);
			}
		} finally {
			cursor.close();
		}

		return auctions;
	}
	
	private static DBClient getDBClient() {
		DBClient dbClient = DBClient.getInstance();
		return dbClient;
	}

	private static MongoClient getMongoClient() {
		DBClient dbClient = getDBClient();
		MongoClient mongoClient = dbClient.getMongoClient();

		return mongoClient;
	}
}
