package org.ds.carServer;

import java.util.Date;

public class HourlyPrice {

	@Override
	public String toString() {
		return "HourlyPrice [hour=" + hour + ", listPrice=" + listPrice
				+ ", minPrice=" + minPrice + "]";
	}

	private Date hour;
	private Double listPrice;
	private Double minPrice;
	
	public Date getHour() {
		return hour;
	}
	public void setHour(Date hour) {
		this.hour = hour;
	}
	
	public Double getListPrice() {
		return listPrice;
	}
	public void setListPrice(Double listPrice) {
		this.listPrice = listPrice;
	}
	
	public Double getMinPrice() {
		return minPrice;
	}
	public void setMinPrice(Double minPrice) {
		this.minPrice = minPrice;
	}
	
	public HourlyPrice(Date hour, Double listPrice, Double minPrice){
		setHour(hour);
		setListPrice(listPrice);
		setMinPrice(minPrice);
	}
	
	
}
