package org.ds.faultTolerance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.ds.auction.AuctionServer;
import org.ds.auction.AuctionServerPersistance;
import org.ds.client.DBClient;
import org.ds.resources.RemoteAuctionDetails;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class AuctionFailureDetector implements Watcher {

	private static final int SESSION_TIMEOUT = 2000;
	private static final int EXPECTED_AUCTION_RUNNING_TIME = 2;

	private ZooKeeper zk;
	private CountDownLatch connectedSignal = new CountDownLatch(1);

	public void connect(String hosts) throws IOException, InterruptedException {
		zk = new ZooKeeper(hosts, SESSION_TIMEOUT, this);
		connectedSignal.await();
	}

	@Override
	public void process(WatchedEvent event) { // Watcher interface
		if (event.getState() == KeeperState.SyncConnected) {
			connectedSignal.countDown();
		}
	}

	public void create(String groupName) throws KeeperException,
			InterruptedException {
		String path = "/" + groupName;
		String createdPath = zk.create(path, null/* data */,
				Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		System.out.println("Created " + createdPath);
	}

	public void close() throws InterruptedException {
		zk.close();
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
	

	private static void resumeUnfinishedAuctions(){		
		
		List<String> failedAuctions = getFailedAuctions();
		
    	ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
    	
		for(String auctionID : failedAuctions){
			if(!resumeAuction(client, auctionID)){
				System.out.println("Failed to reschedule auction: " + auctionID);
			}
		}	
	}
	
	
	private static Boolean resumeAuction(Client client, String auctionID){
		//TODO: This has to be a call to the index cluster
		Boolean success = true;
		
		RemoteAuctionDetails auctionObj=new RemoteAuctionDetails();
	    auctionObj.setAuctionId(auctionID);
	  	
	    BasicDBObject jsonAddr = getDBClient().getClusterAddress("cars");
	  	String restAddr = "http://" + 
	  						jsonAddr.getString("ip") + ":" + jsonAddr.getInt("port") + 
	  						jsonAddr.getString("faultTolerancePath");
	  	
	  	System.out.println("Contacting restAddr:" + restAddr + " for resuming auction: " + auctionID);
	  	
	  	WebResource webResource=client.resource(restAddr);
		ClientResponse response=null;
		try{
			 response = webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class,auctionObj);
		}
		catch(Exception e){
			e.printStackTrace();
			success = false;
		}
		
		System.out.println("Fault tolerant process recieved the status  of" + response.getStatus() + 
				" and response as " + response.getEntity(String.class));
		
		return success && response.getStatus() == 200;
	}
	
	private static BasicDBObject getFaultCheckQuery(){
		Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.add(Calendar.MINUTE, -EXPECTED_AUCTION_RUNNING_TIME);
        
        BasicDBObject query = new BasicDBObject(AuctionServerPersistance.FIELD_STATUS, AuctionServer.STATUS_RUNNING);
        query.append(AuctionServerPersistance.FIELD_INITIATED_AT, new BasicDBObject("$lte", cal.getTime()));
        
        return query;
	}
	
	private static List<String> getFailedAuctions(){
		BasicDBObject query = getFaultCheckQuery();
		
		DBCollection coll = getMongoClient().getDB(DBClient.CAR_VENDORS_DB).getCollection(DBClient.AUCTIONS_DETAILS);
        BasicDBObject projectedFields = new BasicDBObject();
        projectedFields.append(AuctionServerPersistance.FIELD_AUCTION_ID, "1");
        
        List<String> failedAuctions = new ArrayList<String>();
        DBCursor cursor = coll.find(query,projectedFields);
        try {
			while (cursor.hasNext()) {
				BasicDBObject dbObj = (BasicDBObject) cursor.next();
			    String auctionID = dbObj.getString(AuctionServerPersistance.FIELD_AUCTION_ID);		
			    failedAuctions.add(auctionID);
			}     
		} finally {
			cursor.close();
		}
        
        return failedAuctions;
	}

	public static void main(String[] args) throws Exception {
		AuctionFailureDetector failureDetector = new AuctionFailureDetector();
		failureDetector.connect("localhost");		
		while (true) {
			BlockingWriteLock locker = new BlockingWriteLock("Fault-tolerant process",
					failureDetector.zk, "/faultToleranceLock");
			System.out.println("Getting lock for fault tolerance process");
			locker.lock();
			long startFaultToleranceTime = System.currentTimeMillis();
			System.out.println("Running fault tolerance process");
			resumeUnfinishedAuctions();
			long endFaultToleranceTime = System.currentTimeMillis();
			long diffTime = endFaultToleranceTime - startFaultToleranceTime;
			if (diffTime < 10000) {
				long offset = 10000 - diffTime;
				Thread.sleep(offset);
			}
			locker.unlock();
		}
		// trialLocker.close();
	}
}
