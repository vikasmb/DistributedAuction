package org.ds.auction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.event.ActionEvent;

import org.ds.client.DBClient;
import org.ds.userServer.AuctionDetails;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

@ManagedBean
public class AuctionViewer {
	private List<WinnerDetails> remoteWinners = new ArrayList<WinnerDetails>();
	private List<WinnerDetails> localWinners = new ArrayList<WinnerDetails>();
	@ManagedProperty(value = "#{buyerCriteria}")
	private BuyerCriteria buyerCriteria;

	public AuctionViewer() {

	}

	public void populateWinnerList(String auctionId) {
		System.out.println("Received auctionId" + auctionId);
	}

	public void populateWinnerList1(ActionEvent auctionevt) {
		HtmlCommandLink detail = (HtmlCommandLink) auctionevt.getSource();
		String userId = "123"; // TODO Replace with session's user id
		String auctionId = (String) detail.getValue();
		String auctionKey = userId + "_" + auctionId;
		AuctionResults results = getAuctionResults(auctionKey);
		if (results != null) {
			localWinners.addAll(results.getLocalWinners());
			remoteWinners.addAll(results.getRemoteWinners());
		}
		//System.out.println("Received auctionId: " + detail.getValue());
		//System.out.println("Received category: " + buyerCriteria.getCategory());
	}

	public static void main(String args[]) {
		getAuctionResults("123_1398545916355");
		// getAuctionResults("123_1398025869321");
	}

	public static AuctionResults getAuctionResults(String auctionID) {
		AuctionResults results = null;
		BasicDBObject auctionDetails = getAuctionDetails(auctionID);
		if (auctionDetails != null) {
			BasicDBList localBids = (BasicDBList) ((BasicDBObject) auctionDetails
					.get(AuctionServerPersistance.FIELD_LOCAL_RESULTS))
					.get(AuctionServerPersistance.FIELD_BIDS);
			BasicDBList remoteBids = (BasicDBList) ((BasicDBObject) auctionDetails
					.get(AuctionServerPersistance.FIELD_REMOTE_RESULTS))
					.get(AuctionServerPersistance.FIELD_BIDS);

			List<WinnerDetails> localWinners = new ArrayList<WinnerDetails>();
			List<WinnerDetails> remoteWinners = new ArrayList<WinnerDetails>();

			List<String> productIDs = new ArrayList<String>();
			for (Object bid : localBids) {
				BasicDBObject bidObj = (BasicDBObject) bid;
				productIDs.add(bidObj
						.getString(AuctionServerPersistance.FIELD_PRODUCT_ID));
			}

			for (Object bid : remoteBids) {
				BasicDBObject bidObj = (BasicDBObject) bid;
				productIDs.add(bidObj
						.getString(AuctionServerPersistance.FIELD_PRODUCT_ID));
			}

			DBClient client = DBClient.getInstance();
			Map<String, BasicDBObject> productDetails = client
					.getProductDetails(productIDs);
			for (String s : productDetails.keySet()) {
				System.out.println(productDetails.get(s));
			}

			for (Object bid : localBids) {
				BasicDBObject bidObj = (BasicDBObject) bid;
				BasicDBObject product = productDetails.get(bidObj
						.getString(AuctionServerPersistance.FIELD_PRODUCT_ID));
				WinnerDetails winnerDetails = new WinnerDetails(
						bidObj.getDouble(AuctionServerPersistance.FIELD_BID),
						product.getString(AuctionServer.FIELD_SELLER_ID),
						product.getString(AuctionServerPersistance.FIELD_PRODUCT_ID),
						product.getString(SellerDetails.FIELD_NAME), product
								.getString(SellerDetails.FIELD_MODEL), product
								.getString(SellerDetails.FIELD_ADDRESS),
						product.getString(SellerDetails.FIELD_IMAGE));
				localWinners.add(winnerDetails);
			}

			for (Object bid : remoteBids) {
				BasicDBObject bidObj = (BasicDBObject) bid;
				BasicDBObject product = productDetails.get(bidObj
						.getString(AuctionServerPersistance.FIELD_PRODUCT_ID));
				WinnerDetails winnerDetails = new WinnerDetails(
						bidObj.getDouble(AuctionServerPersistance.FIELD_BID),
						product.getString(AuctionServer.FIELD_SELLER_ID),
						product.getString(AuctionServerPersistance.FIELD_PRODUCT_ID),
						product.getString(SellerDetails.FIELD_NAME), product
								.getString(SellerDetails.FIELD_ADDRESS),
						product.getString(SellerDetails.FIELD_IMAGE));
				remoteWinners.add(winnerDetails);
			}

			System.out
					.println("For auction: "
							+ auctionDetails
									.getString(AuctionServerPersistance.FIELD_AUCTION_ID));
			System.out.println("Local winners");
			for (WinnerDetails winner : localWinners) {
				winner.printDetails();
			}

			System.out.println("Remote winners");
			for (WinnerDetails winner : remoteWinners) {
				winner.printDetails();
			}

			if (!auctionDetails
					.containsField(AuctionServerPersistance.FIELD_VIEWED_AT)) {
				System.out.println("Viewed at recorded");
				BuyerCriteria criteria = new BuyerCriteria(
						(BasicDBObject) auctionDetails
								.get(AuctionServerPersistance.FIELD_BUYER_CRITERIA));
				AuctionServerPersistance writer = new AuctionServerPersistance(
						criteria, auctionID,
						auctionDetails
								.getInt(AuctionServerPersistance.FIELD_VERSION));
				writer.recordViewedAt();
			}

			results = new AuctionResults(remoteWinners, localWinners);
		} else {
			System.out.println("Auction still running!");
		}

		return results;
	}

	private static BasicDBObject getAuctionDetails(String auctionID) {
		BasicDBObject query = new BasicDBObject(
				AuctionServerPersistance.FIELD_AUCTION_ID, auctionID).append(
				AuctionServerPersistance.FIELD_STATUS,
				AuctionServer.STATUS_FINISHED);
		DBCollection coll = getMongoClient().getDB(DBClient.CAR_VENDORS_DB)
				.getCollection(DBClient.AUCTIONS_DETAILS);
		BasicDBObject auctionDetails = (BasicDBObject) coll.findOne(query);
		return auctionDetails;
	}

	private static DBClient getDBClient() {
		DBClient dbClient = DBClient.getInstance();
		return dbClient;
	}

	private static MongoClient getMongoClient() {
		DBClient dbClient = getDBClient();
		MongoClient mongoClient = dbClient.getMongoClient();

		return mongoClient;
	}

	public List<WinnerDetails> getRemoteWinners() {
		return remoteWinners;
	}

	public void setRemoteWinners(List<WinnerDetails> remoteWinners) {
		this.remoteWinners = remoteWinners;
	}

	public List<WinnerDetails> getLocalWinners() {
		return localWinners;
	}

	public void setLocalWinners(List<WinnerDetails> localWinners) {
		this.localWinners = localWinners;
	}

	public BuyerCriteria getBuyerCriteria() {
		return buyerCriteria;
	}

	public void setBuyerCriteria(BuyerCriteria buyerCriteria) {
		this.buyerCriteria = buyerCriteria;
	}
}
