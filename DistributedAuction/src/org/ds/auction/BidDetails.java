package org.ds.auction;

public class BidDetails {

	Boolean madeBid;
	Double bid;
	
	public Boolean getMadeBid() {
		return this.madeBid;
	}
	
	public Double getBid() {
		return this.bid;
	}
	
	public BidDetails(){
		//TODO: this constructor has to be modified to use the details sent back by the remote URI
		this.madeBid = true;
		this.bid = 10.0;
	}
}
