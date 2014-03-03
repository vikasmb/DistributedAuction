package org.ds.carServer;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import com.mongodb.BasicDBObject;

//encapsulation and abstraction of seller details
public class SellerDetails {
	
	/**
	 * Field names
	 */
	public static String FIELD_NAME = "name";
	public static String FIELD_REMOTE = "remote";
	public static String FIELD_CALLBACK = "callback";
	
	/**
	 * The seller details variables
	 */
	private String sellerName;
	private Boolean remote;
	private String callback;
	
	/**
	 * setters
	 */
	
	private void setSellerName(String name) {
		this.sellerName = name;
	}
	
	private void setRemote(Boolean isRemote){
		this.remote = isRemote;
	}
	
	private void setCallback(String callbackIP){
		this.callback = callbackIP;
	}
	
	/**
	 * getters
	 */
	
	public String getSellerName() {
		return sellerName;
	}
	
	public Boolean getRemote() {
		return remote;
	}
	
	public String getCallback() {
		return callback;
	}
	
	/**
	 * Constructor
	 * @param inputData
	 */
	public SellerDetails(JsonObject inputData) {
		if(inputData.containsKey(FIELD_NAME)){
			setSellerName(inputData.getString(FIELD_NAME));
		} else {
			setSellerName("");
		}
		
		if(inputData.containsKey(FIELD_REMOTE)){
			setRemote(inputData.getBoolean(FIELD_REMOTE));
		} else {
			setRemote(false);
		}
		
		if(inputData.containsKey(FIELD_CALLBACK)){
			setCallback(inputData.getString(FIELD_CALLBACK));
		} else {
			setCallback("");
		}
	}
	
	/**
	 * Packages to BSON for mongo insertion
	 * @return BSONObject containing all fields
	 */
	public BasicDBObject packageToBSON(){
		return new BasicDBObject(FIELD_NAME, getSellerName())
						.append(FIELD_REMOTE, remote)
						.append(FIELD_CALLBACK, callback);
	}
}
