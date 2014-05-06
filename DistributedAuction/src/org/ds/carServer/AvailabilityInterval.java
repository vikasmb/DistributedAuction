package org.ds.carServer;

import java.util.Date;

public class AvailabilityInterval {
	
	@Override
	public String toString() {
		return "AvailabilityInterval [from=" + from + ", till=" + till + "]";
	}

	private Date from;
	private Date till;
	
	public Date getFrom() {
		return from;
	}
	public void setFrom(Date from) {
		this.from = from;
	}
	
	public Date getTill() {
		return till;
	}
	public void setTill(Date till) {
		this.till = till;
	}
	
	public AvailabilityInterval(Date from, Date till) {
		super();
		this.from = from;
		this.till = till;
	}
	
	
}
