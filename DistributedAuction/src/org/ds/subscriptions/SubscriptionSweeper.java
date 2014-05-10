package org.ds.subscriptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.ws.rs.core.MediaType;

import org.ds.auction.AuctionResults;
import org.ds.auction.AuctionServer;
import org.ds.auction.AuctionServerPersistance;
import org.ds.auction.BuyerCriteria;
import org.ds.auction.ClaimDetails;
import org.ds.auction.SellerDetails;
import org.ds.auction.UserDetails;
import org.ds.auction.WinnerDetails;
import org.ds.client.DBClient;
import org.ds.userServer.ListSubscriptions;
import org.ds.util.DateUtil;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

@ManagedBean
@SessionScoped
public class SubscriptionSweeper {
	public static int ACCEPT_BUFFER = 10;
	public static int RESULTS_LIMIT = 10;
	private List<SubscriptionDeal> deals;
	private String auctionKey;
	private String auctionDisplayHeaderKey = "";

	@ManagedProperty(value = "#{userDetails}")
	private UserDetails userObj;

	public SubscriptionSweeper() {

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		for(int i=0; i < 1000; i++){
			long startTime = System.currentTimeMillis();
			List<String> subscriptions = ListSubscriptions
					.getUserSubscriptions("456");
			Date viewedUntil = null;
			for (String subscription : subscriptions) {
				String auctionID = subscription.split(":")[1];
				//System.out.println(auctionID);
				getSubscriptionResults(auctionID, viewedUntil);
			}
			long difference = System.currentTimeMillis() - startTime;
			System.out.println(i + ", " + difference);
		}
	}

	public static List<SubscriptionDeal> getSubscriptionResults(
			String auctionID, Date viewedUntil) {
		BasicDBObject auctionData = getAuctionData(auctionID);
		BasicDBObject criteriaBSON = (BasicDBObject) auctionData
				.get(AuctionServerPersistance.FIELD_BUYER_CRITERIA);
		BuyerCriteria criteria = new BuyerCriteria(criteriaBSON);
		//criteria.printCriteria();

		return getSubscribedDeals(auctionID, criteria, viewedUntil);
	}

	public static BasicDBObject getAuctionData(String auctionID) {
		BasicDBObject query = new BasicDBObject(
				AuctionServerPersistance.FIELD_AUCTION_ID, auctionID);
		DBCursor cursor;
		BasicDBObject auctionData = null;
		DBCollection coll = getMongoClient().getDB(DBClient.CAR_VENDORS_DB)
				.getCollection(DBClient.AUCTIONS_DETAILS);
		if (coll != null) {
			cursor = coll.find(query);
			try {
				while (cursor.hasNext()) {
					auctionData = (BasicDBObject) cursor.next();
				}
			} finally {
				cursor.close();
			}
		} else {
			System.out.println("Failed to get collection: "
					+ DBClient.AUCTIONS_DETAILS);
		}
		return auctionData;
	}

	private static List<SubscriptionDeal> getSubscribedDeals(String auctionID,
			BuyerCriteria criteria, Date viewedUntil) {
		List<BasicDBObject> qualifyingAuctions = getQualifyingAuctions(
				criteria, viewedUntil);
		List<SubscriptionDeal> deals = new ArrayList<SubscriptionDeal>();

		for (BasicDBObject auction : qualifyingAuctions) {
			if (auctionID.equals(auction
					.getString(AuctionServerPersistance.FIELD_AUCTION_ID))) {
				continue;
			}
			BasicDBList localBids = (BasicDBList) ((BasicDBObject) auction
					.get(AuctionServerPersistance.FIELD_LOCAL_RESULTS))
					.get(AuctionServerPersistance.FIELD_BIDS);
			BasicDBList remoteBids = (BasicDBList) ((BasicDBObject) auction
					.get(AuctionServerPersistance.FIELD_REMOTE_RESULTS))
					.get(AuctionServerPersistance.FIELD_BIDS);

			List<String> productIDs = new ArrayList<String>();
			for (Object bid : localBids) {
				BasicDBObject bidObj = (BasicDBObject) bid;
				if (!bidObj.getBoolean(AuctionServerPersistance.FIELD_CLAIMED)) {
					productIDs
							.add(bidObj
									.getString(AuctionServerPersistance.FIELD_PRODUCT_ID));
				}
			}

			for (Object bid : remoteBids) {
				BasicDBObject bidObj = (BasicDBObject) bid;
				productIDs.add(bidObj
						.getString(AuctionServerPersistance.FIELD_PRODUCT_ID));
			}

			DBClient client = DBClient.getInstance();
			Map<String, BasicDBObject> productDetails = client
					.getProductDetails(productIDs);
			/*
			 * for(String s:productDetails.keySet()){
			 * System.out.println(productDetails.get(s)); }
			 */

			for (Object bid : localBids) {
				BasicDBObject bidObj = (BasicDBObject) bid;
				if (!bidObj.getBoolean(AuctionServerPersistance.FIELD_CLAIMED)) {
					BasicDBObject product = productDetails
							.get(bidObj
									.getString(AuctionServerPersistance.FIELD_PRODUCT_ID));
					WinnerDetails winnerDetails = new WinnerDetails(
							bidObj.getDouble(AuctionServerPersistance.FIELD_BID),
							product.getString(AuctionServer.FIELD_SELLER_ID),
							product.getString(AuctionServerPersistance.FIELD_PRODUCT_ID),
							product.getString(SellerDetails.FIELD_NAME),
							product.getString(SellerDetails.FIELD_MODEL),
							product.getString(SellerDetails.FIELD_ADDRESS),
							product.getString(SellerDetails.FIELD_IMAGE));

					deals.add(new SubscriptionDeal(
							auction.getString(AuctionServerPersistance.FIELD_AUCTION_ID),
							criteria, winnerDetails));
				}
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
				deals.add(new SubscriptionDeal(auction
						.getString(AuctionServerPersistance.FIELD_AUCTION_ID),
						criteria, winnerDetails));
			}

			/*System.out
					.println("For auction: "
							+ auction
									.getString(AuctionServerPersistance.FIELD_AUCTION_ID));
		
			 for(SubscriptionDeal deal:deals){ deal.printDetails(); }*/
			 
		}
		//System.out.println(deals.size());
		//System.out.println("Done");
		return deals;
	}

	private static List<BasicDBObject> getQualifyingAuctions(
			BuyerCriteria criteria, Date viewedUntil) {
		BasicDBObject query = getQualifyingAuctionsQuery(criteria, viewedUntil);

		DBCollection coll = getMongoClient().getDB(DBClient.CAR_VENDORS_DB)
				.getCollection(DBClient.AUCTIONS_DETAILS);

		List<BasicDBObject> subscribedAuctions = new ArrayList<BasicDBObject>();
		BasicDBObject orderBy = new BasicDBObject(
				AuctionServerPersistance.FIELD_VIEWED_AT, -1);
		DBCursor cursor = coll.find(query).sort(orderBy).limit(RESULTS_LIMIT);
		try {
			while (cursor.hasNext()) {
				BasicDBObject dbObj = (BasicDBObject) cursor.next();
				subscribedAuctions.add(dbObj);
			}
		} finally {
			cursor.close();
		}

		return subscribedAuctions;
	}

	private static BasicDBObject getQualifyingAuctionsQuery(
			BuyerCriteria criteria, Date viewedUntil) {
		if (viewedUntil == null) {
			Calendar cal = Calendar.getInstance();
			cal.setTimeZone(TimeZone.getTimeZone("GMT"));
			cal.add(Calendar.MINUTE, -ACCEPT_BUFFER);
			viewedUntil = cal.getTime();
		}
		BasicDBObject query = new BasicDBObject(
				AuctionServerPersistance.FIELD_STATUS,
				AuctionServer.STATUS_FINISHED)
				.append(AuctionServerPersistance.FIELD_VIEWED_AT,
						new BasicDBObject("$lte", viewedUntil))
				.append(AuctionServerPersistance.FIELD_USER_ID,
						new BasicDBObject("$ne", criteria.getBuyerID()))
				.append(AuctionServerPersistance.FIELD_BUYER_CRITERIA + "."
						+ BuyerCriteria.FIELD_CITY, criteria.getCity())
				.append(AuctionServerPersistance.FIELD_BUYER_CRITERIA + "."
						+ BuyerCriteria.FIELD_NEEDED_FROM,
						criteria.getNeededFrom())
				.append(AuctionServerPersistance.FIELD_BUYER_CRITERIA + "."
						+ BuyerCriteria.FIELD_NEEDED_UNTIL,
						criteria.getNeededUntil());

		return query;
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

	public List<SubscriptionDeal> getDeals() {
		if (deals == null) {
			deals = new ArrayList<SubscriptionDeal>();
		}
		return deals;
	}

	public void setDeals(List<SubscriptionDeal> deals) {
		this.deals = deals;
	}

	public void populateSubscriberdeals(ActionEvent auctionevt) {
		getDeals().clear();

		HtmlCommandLink detail = (HtmlCommandLink) auctionevt.getSource();
		Map<String, Object> attributes = auctionevt.getComponent()
				.getAttributes();
		String auctionId = (String) attributes.get("timeStamp");
		String userId = getUserObj().getName();
		auctionKey = userId + "_" + auctionId;
		System.out.println("Current auction id:" + auctionKey);
		List<SubscriptionDeal> subsDeals = getSubscriptionResults(auctionKey,
				null);
		if (subsDeals != null) {
			getDeals().addAll(subsDeals);
		}
	}

	public void claimAuction(SubscriptionDeal auctionObj) {

		ClaimDetails claimDetailsObj = new ClaimDetails();
		System.out.println("Claiming auction id:" + auctionObj.getAuctionID());
		claimDetailsObj.setAuctionId(auctionObj.getAuctionID());
		claimDetailsObj.setProductId(auctionObj.getWinnerDetails()
				.getProductID());
		ClientConfig config = new DefaultClientConfig();
		Client remoteClient = Client.create(config);
		DBClient dbClient = DBClient.getInstance();
		BasicDBObject jsonAddr = dbClient.getClusterAddress("cars");
		String restAddr = "http://" + jsonAddr.getString("ip") + ":"
				+ jsonAddr.getInt("port")
				+ "/DistributedAuction/rest/claimAuction";
		System.out.println("Contacting " + restAddr + " for claiming auction");
		WebResource webResource = remoteClient.resource(restAddr);
		ClientResponse response = null;
		try {
			response = webResource.type(MediaType.APPLICATION_JSON).post(
					ClientResponse.class, claimDetailsObj);
			String isSuccess = response.getEntity(String.class);
			FacesMessage message = null;
			if (isSuccess.equals("true")) {
				message = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Claim successfully completed !!! ", null);
			} else {
				message = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Claim failed !!! ", null);
			}
			FacesContext.getCurrentInstance().addMessage(null, message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public UserDetails getUserObj() {
		return userObj;
	}

	public void setUserObj(UserDetails userObj) {
		this.userObj = userObj;
	}

	public String getAuctionDisplayHeaderKey() {
		auctionDisplayHeaderKey = "";
		if (auctionKey != null && !auctionKey.equals("")) {
			long epoch = Long.parseLong(auctionKey.split("_")[1]);
			Date dateObj = new Date(epoch);
			// System.out.println(DateUtil.getUserDisplayString(expiry));
			DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm");
			formatter.setTimeZone(TimeZone.getTimeZone("GMT-5:00"));
			auctionDisplayHeaderKey = formatter.format(dateObj);
		}
		return auctionDisplayHeaderKey;
	}

	public void setAuctionDisplayHeaderKey(String auctionDisplayHeaderKey) {
		this.auctionDisplayHeaderKey = auctionDisplayHeaderKey;
	}
	
	public void clearResults(){
		deals.clear();		
	}

}
