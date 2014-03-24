package org.ds.auction;

import java.awt.print.Printable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ds.client.DBClient;
import org.ds.util.DateUtil;

import com.mongodb.BasicDBObject;

public class SelectionServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		SelectionServer server = new SelectionServer();
		DBClient client = DBClient.getInstance();
		BidderDetails detailsObj = null;
		BuyerCriteria criteria=null;
		if (args.length > 0) {
			detailsObj = client.getPotentialSellers("cars", args[1], args[2],
					args[3]);
			criteria = new BuyerCriteria(args[0],
					DateUtil.getDate(args[2]),
					DateUtil.getDate(args[3]),  args[1]);
		} else {
			detailsObj = client.getPotentialSellers("cars", "LA",
					"2014-03-15T10:00:00", "2014-03-15T11:00:00");
			criteria = new BuyerCriteria("123",
					DateUtil.getDate("2014-03-15T10:00:00"),
					DateUtil.getDate("2014-03-15T11:00:00"), "LA");
		}
		System.out.println("Local Size:" + detailsObj.getLocalBidders().size());
		System.out.println("Remote Size:"
				+ detailsObj.getRemoteBidders().size()); // server.printArgs(args);
		// Pass the local and remote bidders list to auction server.
	

		AuctionServer auctionServer = new AuctionServer(detailsObj, criteria);
		auctionServer.run();
	}

}
