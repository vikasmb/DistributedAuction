package org.ds.userServer;

public class AuctionDetails {
public AuctionDetails(String category, String auctionId) {
		super();
		this.category = category;
		this.auctionId = auctionId;
	}
private String category;
private String auctionId;
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
}
