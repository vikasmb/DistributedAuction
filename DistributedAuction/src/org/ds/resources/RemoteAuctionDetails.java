package org.ds.resources;

import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import org.ds.auction.BuyerCriteria;

@XmlRootElement
public class RemoteAuctionDetails {
	private String auctionId;
	private Set<Double> oldBids;
	private BuyerCriteria buyerCriteria;
    private int roundNumber;
	public String getAuctionId() {
		return auctionId;
	}

	public void setAuctionId(String auctionId) {
		this.auctionId = auctionId;
	}

	public Set<Double> getOldBids() {
		return oldBids;
	}

	public void setOldBids(Set<Double> oldBids) {
		this.oldBids = oldBids;
	}

	public BuyerCriteria getBuyerCriteria() {
		return buyerCriteria;
	}

	public void setBuyerCriteria(BuyerCriteria buyerCriteria) {
		this.buyerCriteria = buyerCriteria;
	}

	public int getRoundNumber() {
		return roundNumber;
	}

	public void setRoundNumber(int roundNumber) {
		this.roundNumber = roundNumber;
	}
}
