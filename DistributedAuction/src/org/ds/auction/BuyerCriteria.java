package org.ds.auction;

import java.util.Date;

import com.mongodb.BasicDBObject;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.xml.bind.annotation.XmlRootElement;

import org.ds.util.DateUtil;

@XmlRootElement
@ManagedBean(name="buyerCriteria")
@SessionScoped
public class BuyerCriteria {
	public BuyerCriteria() {
		super();
	}

	public BuyerCriteria(String buyerID, Date neededFrom, Date neededUntil,
			String city) {
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
	
	public static String FIELD_BUYER_ID = "buyerID";
	public static String FIELD_NEEDED_FROM = "neededFrom";
	public static String FIELD_NEEDED_UNTIL = "neededUntil";
	public static String FIELD_CITY = "city";
	
	
	private String buyerID;
	private Date neededFrom;
	private Date neededUntil;
	private String city;
	private String category;

	public String getBuyerID() {
		return this.buyerID;
	}

	public Date getNeededFrom() {
		return this.neededFrom;
	}

	public Date getNeededUntil() {
		return this.neededUntil;
	}

	public String getCity() {
		return this.city;
	}
	
	public void setBuyerID(String buyerID) {
		this.buyerID = buyerID;
	}

	public void setNeededFrom(Date neededFrom) {
		this.neededFrom = neededFrom;
	}

	public void setNeededUntil(Date neededUntil) {
		this.neededUntil = neededUntil;
	}

	public void setCity(String city) {
		System.out.println("Setting city to"+city);
		this.city = city;
	}
	
	public BasicDBObject packageToBSON(){
		return new BasicDBObject(FIELD_BUYER_ID, getBuyerID())
									.append(FIELD_NEEDED_FROM, getNeededFrom())
									.append(FIELD_NEEDED_UNTIL, getNeededUntil())
									.append(FIELD_CITY, getCity());
	}
	
	public void printCriteria(){
		System.out.println("BuyerID: " + getBuyerID() +  " City: " + getCity() 
							+ " Needed from: " + DateUtil.getStringFromDate(getNeededFrom())
							+ " Needed until: " + DateUtil.getStringFromDate(getNeededUntil()));
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		System.out.println("Setting category to"+category);
		this.category = category;
	}
}
