package org.ds.userServer;

import org.ds.util.DateUtil;

public class SubscriptionDetails {
	public SubscriptionDetails(String category, String auctionId) {
		super();
		this.category = category;
		this.auctionId = auctionId;
		this.displayString=DateUtil.getUserDisplayString(auctionId);
	}
	
	private String category;
	private String auctionId;
	private String displayString;
	
	public String getCategory() {
		return category;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	
	public String getAuctionId() {
		return auctionId;
	}
	
	public void setAuctionId(String auctionId) {
		this.auctionId = auctionId;
	}

	public String getDisplayString() {
		return displayString;
	}

	public void setDisplayString(String displayString) {
		this.displayString = displayString;
	}
}
