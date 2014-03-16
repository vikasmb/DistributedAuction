package org.ds.auction;

import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class AuctionResults {
	
	private TreeMap<Double, String> bids;
	
	public TreeMap<Double, String> getBids() {
		return this.bids;
	}
	
	public void setBids(TreeMap<Double, String> bids) {
		this.bids = bids;
	}
	
	public AuctionResults() {
		setBids(new TreeMap<Double, String>());
	}
	
	public Boolean isSameAs(AuctionResults other){
		if(other == null) {
			return false;
		}
		
		TreeMap<Double, String> bids  = getBids();
		TreeMap<Double, String> otherBids = other.getBids();
		
		if(bids.size() != otherBids.size()){
			return false;
		}
		
		Set<Entry<Double, String>> bidsEntrySet = bids.entrySet();
		for(Entry<Double, String> entry: bidsEntrySet){
			Double key = entry.getKey();
			String bidder = entry.getValue();
			if(otherBids.containsKey(key)) {
				if(!otherBids.get(key).equals(bidder)) {
					return false;
				}
			} else {
				return false;
			}
		}
		
		return true;
	}
}
