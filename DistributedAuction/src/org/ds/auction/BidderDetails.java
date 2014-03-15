package org.ds.auction;

import java.util.List;

import com.mongodb.BasicDBObject;

public class BidderDetails {
	private List<BasicDBObject> remoteBidders;
	private List<BasicDBObject> localBidders;
	
	public List<BasicDBObject> getRemoteBidders(){
		return this.remoteBidders;
	}
	
	public List<BasicDBObject> getLocalBidders(){
		return this.localBidders;
	}
	
	public BidderDetails(List<BasicDBObject> remoteBidders, List<BasicDBObject> localBidders) {
		this.remoteBidders = remoteBidders;
		this.localBidders = localBidders;
	}
}
