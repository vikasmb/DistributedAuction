package org.ds.auction;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class BuyerCriteria {
	public BuyerCriteria() {
		super();
	}

	private String buyerID;
	private Date neededFrom;
	private Date neededUntil;
	private String city;

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

	public BuyerCriteria(String buyerID, Date neededFrom, Date neededUntil,
			String city) {
		this.buyerID = buyerID;
		this.neededFrom = neededFrom;
		this.neededUntil = neededUntil;
		this.city = city;
	}
}
