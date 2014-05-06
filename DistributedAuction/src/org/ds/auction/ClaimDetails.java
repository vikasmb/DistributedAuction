package org.ds.auction;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ClaimDetails {
	private String auctionId;
	private String productId;

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getAuctionId() {
		return auctionId;
	}

	public void setAuctionId(String auctionId) {
		this.auctionId = auctionId;
	}

}
