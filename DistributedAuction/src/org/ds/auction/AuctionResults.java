package org.ds.auction;

import java.util.List;


public class AuctionResults {
	private List<WinnerDetails> remoteWinners;
	private List<WinnerDetails> localWinners;
	
	public AuctionResults(){
		super();
	}
	
	public List<WinnerDetails> getRemoteWinners(){
		return this.remoteWinners;
	}
	
	public List<WinnerDetails> getLocalWinners(){
		return this.localWinners;
	}
	
	public AuctionResults(List<WinnerDetails> remoteWinners, List<WinnerDetails> localWinners) {
		this.remoteWinners = remoteWinners;
		this.localWinners = localWinners;
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
	}
}
