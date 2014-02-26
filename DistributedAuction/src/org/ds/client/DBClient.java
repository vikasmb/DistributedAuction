package org.ds.client;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

//Singleton class holding MongoClient instance
public class DBClient {
	private static DBClient _instance;
	private MongoClient mongoClient;

	private DBClient() {
		try {
			mongoClient = new MongoClient(Arrays.asList(new ServerAddress("localhost", 27017)));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static DBClient getInstance() {
		if (_instance == null) {
			_instance = new DBClient();
		}
		return _instance;
	}

	public BasicDBObject getClusterAddress(String dbName, String collectionName,String clusterCategory) {
		DB db = mongoClient.getDB(dbName);
		BasicDBObject clusterAddr=null;
		if (db != null) {

			DBCollection coll = db.getCollection(collectionName);
			BasicDBObject query = new BasicDBObject("category", clusterCategory);

			DBCursor cursor = coll.find(query);
			
			try {
				while (cursor.hasNext()) {
					// System.out.println( cursor.next().get("address"));
					BasicDBObject dbObj = (BasicDBObject) cursor.next().get(
							"address");
					clusterAddr= dbObj;
					// System.out.println( dbObj==null?"null":dbObj.get("ip"));
					// System.out.println( ( (BasicDBObject)
					// (cursor.next().get("address"))).get("ip"));
					// System.out.println(cursor.next());
				}
			} finally {
				cursor.close();
			}			
		}
		return clusterAddr;					
	}
}
