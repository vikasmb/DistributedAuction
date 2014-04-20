package org.ds.subscriptions;

import org.ds.auction.BuyerCriteria;
import org.ds.auction.WinnerDetails;

public class SubscriptionDetails {
	
	private BuyerCriteria criteria;
	private WinnerDetails winnerDetails;
	
	public BuyerCriteria getBuyerCriteria(){
		return this.criteria;
	}
	
	public WinnerDetails getWinnerDetails(){
		return this.winnerDetails;
	}
	
	public SubscriptionDetails(BuyerCriteria criteria, WinnerDetails winnerDetails){
		this.criteria = criteria;
		this.winnerDetails = winnerDetails;
	}
	
	public void printDetails(){
		getBuyerCriteria().printCriteria();
		getWinnerDetails().printDetails();
	}
}
