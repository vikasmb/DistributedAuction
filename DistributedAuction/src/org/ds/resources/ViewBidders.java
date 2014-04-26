package org.ds.resources;

import org.ds.auction.BidderDetails;
import org.ds.auction.BuyerCriteria;
import org.ds.auction.ClientReadableBidderDetails;
import org.ds.client.DBClient;
import org.ds.util.DateUtil;

public class ViewBidders {
	public static void main(String args[]){
		BuyerCriteria criteria = new BuyerCriteria("123",
				DateUtil.getDate("2014-03-15T10:00:00"),
				DateUtil.getDate("2014-03-15T11:00:00"), "LA");
		ClientReadableBidderDetails bidderDetails = getBidderDetails("cars", criteria);
		bidderDetails.printDetails();
	}
	
	public static ClientReadableBidderDetails getBidderDetails(String category, BuyerCriteria criteria){
		DBClient client = DBClient.getInstance();
		BidderDetails detailsObj = client.getPotentialSellers(category, criteria.getCity(), DateUtil.getStringFromDate(criteria.getNeededFrom()),
				DateUtil.getStringFromDate(criteria.getNeededUntil()), true);
		
		return ClientReadableBidderDetails.convertToClientReadable(detailsObj);
	}
}
