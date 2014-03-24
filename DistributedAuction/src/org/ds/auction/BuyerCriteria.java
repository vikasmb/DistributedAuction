package org.ds.auction;

import java.util.Date;

import com.mongodb.BasicDBObject;

public class BuyerCriteria {

	public static String FIELD_BUYER_ID = "buyerID";
	public static String FIELD_NEEDED_FROM = "neededFrom";
	public static String FIELD_NEEDED_UNTIL = "neededUntil";
	public static String FIELD_CITY = "city";
	
	
	private String buyerID;
	private Date neededFrom;
	private Date neededUntil;
	private String city;
	
	public String getBuyerID(){
		return this.buyerID;
	}
	
	public Date getNeededFrom() {
		return this.neededFrom;
	}
	
	public Date getNeededUntil(){
		return this.neededUntil;
	}
	
	public String getCity(){
		return this.city;
	}
	
	public BuyerCriteria(String buyerID, Date neededFrom, Date neededUntil, String city){
		this.buyerID = buyerID;
		this.neededFrom = neededFrom;
		this.neededUntil = neededUntil;
		this.city = city;
	}
	
	public BuyerCriteria(BasicDBObject criteriaBSON){
		this.buyerID = criteriaBSON.getString(FIELD_BUYER_ID);
		this.neededFrom = criteriaBSON.getDate(FIELD_NEEDED_FROM);
		this.neededUntil = criteriaBSON.getDate(FIELD_NEEDED_UNTIL);
		this.city = criteriaBSON.getString(FIELD_CITY);
	}
	
	public BasicDBObject packageToBSON(){
		return new BasicDBObject(FIELD_BUYER_ID, getBuyerID())
									.append(FIELD_NEEDED_FROM, getNeededFrom())
									.append(FIELD_NEEDED_UNTIL, getNeededUntil())
									.append(FIELD_CITY, getCity());
	}
}
