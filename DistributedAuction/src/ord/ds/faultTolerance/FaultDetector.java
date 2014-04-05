package ord.ds.faultTolerance;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.ws.rs.core.MediaType;

import org.ds.auction.AuctionServer;
import org.ds.auction.AuctionServerPersistance;
import org.ds.client.DBClient;
import org.ds.resources.RemoteAuctionDetails;
import org.ds.util.DateUtil;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class FaultDetector {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		while(true){
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			resumeUnFinishedAuctions();
		}
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
	
	private static void resumeUnFinishedAuctions(){
		BasicDBObject query = new BasicDBObject(AuctionServerPersistance.FIELD_STATUS, AuctionServer.STATUS_RUNNING);		
		Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.add(Calendar.MINUTE, -2);
        query.append(AuctionServerPersistance.FIELD_INITIATED_AT, new BasicDBObject("$lte", cal.getTime()));
        
        DBCollection coll = getMongoClient().getDB(DBClient.CAR_VENDORS_DB).getCollection(DBClient.AUCTIONS_DETAILS);

        BasicDBObject projectedFields = new BasicDBObject();
        projectedFields.append(AuctionServerPersistance.FIELD_AUCTION_ID, "1");
    	
        DBCursor cursor = coll.find(query,projectedFields);
    	ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
    	try {
			while (cursor.hasNext()) {
				BasicDBObject dbObj = (BasicDBObject) cursor.next();
			    String auctionId=dbObj.getString(AuctionServerPersistance.FIELD_AUCTION_ID);		
			    RemoteAuctionDetails auctionObj=new RemoteAuctionDetails();
			    auctionObj.setAuctionId(auctionId);
			  	BasicDBObject jsonAddr = getDBClient().getClusterAddress("cars");
			  	String restAddr="http://"+jsonAddr.getString("ip")+":"+jsonAddr.getInt("port")+jsonAddr.getString("faultTolerancePath");
			  	System.out.println("Contacting restAddr:"+restAddr+" for resuming auction: "+auctionId);
			  	WebResource webResource=client.resource(restAddr);
				ClientResponse response=null;
				try{
					 response = webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class,auctionObj);
				}
				catch(Exception e){
					e.printStackTrace();
				}
				System.out.println("Fault tolerant process recieved the status  of"+response.getStatus()+" and response as "+response.getEntity(String.class));
				System.out.println("Fault tolerant process exiting the rest call");	
			}     
		} finally {
			cursor.close();
		}
	}

}
