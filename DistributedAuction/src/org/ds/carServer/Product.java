package org.ds.carServer;

import java.util.List;

public class Product {

	private String city;
	private String userID;
	private String productID;
	
	private int version;
	
	private List<AvailabilityInterval> availability;
	private List<HourlyPrice> prices;
	
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	
	public String getProductID() {
		return productID;
	}
	public void setProductID(String productID) {
		this.productID = productID;
	}
	
	public int getVersion() {
		return version;
	}
	@Override
	public String toString() {
		return "Product [city=" + city + ", userID=" + userID + ", productID="
				+ productID + ", version=" + version + ", availability="
				+ availability + ", prices=" + prices + "]";
	}
	public void setVersion(int version) {
		this.version = version;
	}
	
	public List<AvailabilityInterval> getAvailability() {
		return availability;
	}
	public void setAvailability(List<AvailabilityInterval> availability) {
		this.availability = availability;
	}
	
	public List<HourlyPrice> getPrices() {
		return prices;
	}
	public void setPrices(List<HourlyPrice> prices) {
		this.prices = prices;
	}
	
	public Product(String city, String userID, String productID, int version,
			List<AvailabilityInterval> availability, List<HourlyPrice> prices) {
		super();
		this.city = city;
		this.userID = userID;
		this.productID = productID;
		this.version = version;
		this.availability = availability;
		this.prices = prices;
	}
}
