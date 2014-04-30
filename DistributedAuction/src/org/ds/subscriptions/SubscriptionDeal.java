package org.ds.subscriptions;

import org.ds.auction.BuyerCriteria;
import org.ds.auction.WinnerDetails;

public class SubscriptionDeal {
	
	private BuyerCriteria criteria;
	private WinnerDetails winnerDetails;
	private String auctionID;
	
	public BuyerCriteria getBuyerCriteria(){
		return this.criteria;
	}
	
	public String getAuctionID(){
		return this.auctionID;
	}
	
	public WinnerDetails getWinnerDetails(){
		return this.winnerDetails;
	}
	
	public SubscriptionDeal(String auctionID, BuyerCriteria criteria, WinnerDetails winnerDetails){
		this.auctionID = auctionID;
		this.criteria = criteria;
		this.winnerDetails = winnerDetails;
	}
	
	public void printDetails(){
		System.out.println(getAuctionID());
		getBuyerCriteria().printCriteria();
		getWinnerDetails().printDetails();
	}
}
