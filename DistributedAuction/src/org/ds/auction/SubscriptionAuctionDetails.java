package org.ds.auction;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SubscriptionAuctionDetails {
	private String auctionId;
	private String userId;

	public String getAuctionId() {
		return auctionId;
	}

	public void setAuctionId(String auctionId) {
		this.auctionId = auctionId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}
