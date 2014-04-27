package org.ds.carServer;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.xml.bind.annotation.XmlRootElement;

import com.mongodb.BasicDBObject;

//encapsulation and abstraction of seller details
@XmlRootElement
@ManagedBean(name="sellerDetails1")
@SessionScoped
public class CarSellerDetails {
	
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
	
	public void setSellerName(String name) {
		System.out.println("Set seller name to"+name);
		this.sellerName = name;
	}
	
	public void setRemote(Boolean isRemote){
		this.remote = isRemote;
	}
	
	public void setCallback(String callbackIP){
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
	
	public CarSellerDetails(){
		super();
	}
	/**
	 * Constructor
	 * @param inputData
	 */
	public CarSellerDetails(JsonObject inputData) {
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
