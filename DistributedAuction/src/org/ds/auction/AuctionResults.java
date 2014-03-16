package org.ds.auction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class AuctionResults {
	
	private TreeMap<Double, List<String>> bids;
	
	public TreeMap<Double, List<String>> getBids() {
		return this.bids;
	}
	
	public void setBids(TreeMap<Double, List<String>> bids) {
		this.bids = bids;
	}
	
	public AuctionResults() {
		setBids(new TreeMap<Double, List<String>>());
	}
	
	public Boolean isSameAs(AuctionResults other){
		if(other == null) {
			return false;
		}
		
		TreeMap<Double, List<String>> bids  = getBids();
		TreeMap<Double, List<String>> otherBids = other.getBids();
		
		if(bids.size() != otherBids.size()){
			return false;
		}
		
		Set<Entry<Double, List<String>>> bidsEntrySet = bids.entrySet();
		for(Entry<Double, List<String>> entry: bidsEntrySet){
			Double key = entry.getKey();
			List<String> bidders = entry.getValue();
			if(otherBids.containsKey(key)) {
				if(!equalLists(otherBids.get(key), bidders)) {
					return false;
				}
			} else {
				return false;
			}
		}
		
		return true;
	}
	
	public Boolean equalLists(List<String> list1, List<String> list2){
		
		if(list1.size() != list2.size()) {
			return false;
		}
		
		Collections.sort(list1);
		Collections.sort(list2);
		
		for(int i = 0; i < list1.size(); i++) {
			if(!list1.get(i).equals(list2.get(i))){
				return false;
			}
		}
		
		return true;
	}
}
