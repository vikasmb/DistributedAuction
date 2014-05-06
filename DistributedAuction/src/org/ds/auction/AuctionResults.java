package org.ds.auction;

import java.util.List;


public class AuctionResults {
	private List<WinnerDetails> remoteWinners;
	private List<WinnerDetails> localWinners;
	private Boolean claimed;
	
	public AuctionResults(){
		super();
	}
	
	public List<WinnerDetails> getRemoteWinners(){
		return this.remoteWinners;
	}
	
	public List<WinnerDetails> getLocalWinners(){
		return this.localWinners;
	}
	
	public Boolean getClaimed() {
		return claimed;
	}

	
	public AuctionResults(List<WinnerDetails> remoteWinners, List<WinnerDetails> localWinners, Boolean atLeastOneDealClaimed) {
		this.remoteWinners = remoteWinners;
		this.localWinners = localWinners;
		this.claimed = atLeastOneDealClaimed;
	}
	
	public void printDetails(){
		System.out.println("Local bidders: ");
		for(WinnerDetails localWinner:this.localWinners){
			localWinner.printDetails();
		}
		
		System.out.println("Remote bidders: ");
		for(WinnerDetails remoteWinner:this.remoteWinners){
			remoteWinner.printDetails();
		}
		
		System.out.println("Claimed = " + this.claimed);
	}
}
