package org.ds.auction;

import java.awt.print.Printable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ds.client.DBClient;
import org.ds.util.DateUtil;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

public class SelectionServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DBClient client = DBClient.getInstance();
		BidderDetails detailsObj = null;
		BuyerCriteria criteria = null;
		AuctionServer auctionServer = null;
		int argsLength = args.length;
		if (argsLength > 0) {
			if (argsLength == 1) { // Only auctionId is present to resume the
									// auction.
				String auctionId = args[0];
				BasicDBObject auctionObj = getAuctionObj(auctionId);
				criteria = getBuyerCriteriaForAuction(auctionObj);

				// Check if local results are already populated in the auction.
				boolean runLocalAuction = checkLocalResultsExist(auctionObj);
				detailsObj = client.getPotentialSellers("cars", criteria.getCity(), DateUtil.getStringFromDate(criteria.getNeededFrom()),
							DateUtil.getStringFromDate(criteria.getNeededUntil()), runLocalAuction);
				auctionServer = new AuctionServer(detailsObj, criteria, auctionId, auctionObj);
			}
			else {
				detailsObj = client.getPotentialSellers("cars", args[1],
						args[2], args[3],true);
				criteria = new BuyerCriteria(args[0],
						DateUtil.getDate(args[2]), DateUtil.getDate(args[3]),
						args[1]);
				auctionServer = new AuctionServer(detailsObj, criteria);
			}
		} else { // For testing purpose
			detailsObj = client.getPotentialSellers("cars", "LA",
					"2014-06-15T10:00:00", "2014-06-15T11:00:00",true);
			criteria = new BuyerCriteria("123",
					DateUtil.getDate("2014-06-15T10:00:00"),
					DateUtil.getDate("2014-06-15T11:00:00"), "LA");
			auctionServer = new AuctionServer(detailsObj, criteria);
			
			/*String auctionId = "123_1397418677436";
			BasicDBObject auctionObj = getAuctionObj(auctionId);
			criteria = getBuyerCriteriaForAuction(auctionObj);

			// Check if local results are already populated in the auction.
			boolean runLocalAuction = !checkLocalResultsExist(auctionObj);
			detailsObj = client.getPotentialSellers("cars", criteria.getCity(), DateUtil.getStringFromDate(criteria.getNeededFrom()),
						DateUtil.getStringFromDate(criteria.getNeededUntil()), runLocalAuction);
			auctionServer = new AuctionServer(detailsObj, criteria, auctionId, auctionObj);*/
		}
		System.out.println("Local Size:" + detailsObj.getLocalBidders().size());
		System.out.println("Remote Size:"
				+ detailsObj.getRemoteBidders().size()); // server.printArgs(args);
		// Pass the local and remote bidders list to auction server.

		auctionServer.run();
	}

	private static boolean checkLocalResultsExist(BasicDBObject auctionObj) {
		if (auctionObj.containsField(AuctionServerPersistance.FIELD_LOCAL_RESULTS)) {
			return true;
		} else {
			return false;
		}
	}

	private static BasicDBObject getAuctionObj(String auctionId) {
		BasicDBObject query = new BasicDBObject(
				AuctionServerPersistance.FIELD_AUCTION_ID, auctionId);
		DBClient client = DBClient.getInstance();
		DBCollection coll = client.getMongoClient()
				.getDB(DBClient.CAR_VENDORS_DB)
				.getCollection(DBClient.AUCTIONS_DETAILS);
		BasicDBObject auctionObj = null;
		DBCursor cursor = coll.find(query);
		try {
			while (cursor.hasNext()) {
				auctionObj = (BasicDBObject) cursor.next();
			}
		} finally {
			cursor.close();
		}
		return auctionObj;
	}

	private static BuyerCriteria getBuyerCriteriaForAuction(
			BasicDBObject auctionObj) {
		BasicDBObject buyerCriteriaDBObj = (BasicDBObject) auctionObj
				.get("buyerCriteria");
		BuyerCriteria criteria = new BuyerCriteria(
				buyerCriteriaDBObj.getString("buyerID"),
				buyerCriteriaDBObj.getDate("neededFrom"),
				buyerCriteriaDBObj.getDate("neededUntil"),
				buyerCriteriaDBObj.getString("city"));
		return criteria;
	}

}
