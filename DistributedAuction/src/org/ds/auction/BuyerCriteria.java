package org.ds.auction;

import java.util.Date;

public class BuyerCriteria {

	private Date neededFrom;
	private Date neededUntil;
	private String city;
	
	public Date getNeededFrom() {
		return this.neededFrom;
	}
	
	public Date getNeededUntil(){
		return this.neededUntil;
	}
	
	public String getCity(){
		return this.city;
	}
	
	public BuyerCriteria(Date neededFrom, Date neededUntil, String city){
		this.neededFrom = neededFrom;
		this.neededUntil = neededUntil;
		this.city = city;
	}
}
