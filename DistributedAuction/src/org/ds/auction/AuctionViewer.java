package org.ds.auction;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import org.ds.client.DBClient;
import org.ds.userServer.AuctionDetails;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

@ManagedBean
@SessionScoped
public class AuctionViewer {
	private List<WinnerDetails> remoteWinners = new ArrayList<WinnerDetails>();
	private List<WinnerDetails> localWinners = new ArrayList<WinnerDetails>();
	private Boolean claimed = false;
	private String auctionDisplayHeaderKey = "";
	@ManagedProperty(value = "#{userDetails}")
	private UserDetails userObj;
	@ManagedProperty(value = "#{buyerCriteria}")
	private BuyerCriteria buyerCriteria;
	private String currentAuctionKey;

	public AuctionViewer() {

	}

	public void populateWinnerList(String auctionId) {
		System.out.println("Received auctionId" + auctionId);
	}

	public void populateWinnerList1(ActionEvent auctionevt) {
		localWinners.clear();
		remoteWinners.clear();
		claimed = false;

		HtmlCommandLink detail = (HtmlCommandLink) auctionevt.getSource();
		Map<String, Object> attributes = auctionevt.getComponent()
				.getAttributes();
		String auctionId = (String) attributes.get("timeStamp");
		System.out.println("Attribute found:" + auctionId);
		String userId = getUserObj().getName();
		// String auctionId = (String) detail.getValue();
		currentAuctionKey = userId + "_" + auctionId;
		AuctionResults results = getAuctionResults(currentAuctionKey);
		if (results != null) {
			localWinners.addAll(results.getLocalWinners());
			remoteWinners.addAll(results.getRemoteWinners());
			claimed = results.getClaimed();
		}
		// System.out.println("Received auctionId: " + detail.getValue());
		// System.out.println("Received category: " +
		// buyerCriteria.getCategory());
	}

	public static void main(String args[]) {
		AuctionResults results = getAuctionResults("123_1398545916352");
		results.printDetails();
		// getAuctionResults("123_1398025869321");
	}

	public static AuctionResults getAuctionResults(String auctionID) {
		AuctionResults results = null;
		BasicDBObject auctionDetails = getAuctionDetails(auctionID);
		Boolean atLeastOneDealClaimed = false;
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
				if (bidObj
						.containsField(AuctionServerPersistance.FIELD_CLAIMED)
						&& bidObj
								.getBoolean(AuctionServerPersistance.FIELD_CLAIMED)) {
					atLeastOneDealClaimed = true;
				}
			}

			for (Object bid : remoteBids) {
				BasicDBObject bidObj = (BasicDBObject) bid;
				productIDs.add(bidObj
						.getString(AuctionServerPersistance.FIELD_PRODUCT_ID));
				if (bidObj
						.containsField(AuctionServerPersistance.FIELD_CLAIMED)
						&& bidObj
								.getBoolean(AuctionServerPersistance.FIELD_CLAIMED)) {
					atLeastOneDealClaimed = true;
				}
			}

			DBClient client = DBClient.getInstance();
			Map<String, BasicDBObject> productDetails = client
					.getProductDetails(productIDs);
			/*
			 * for (String s : productDetails.keySet()) {
			 * System.out.println(productDetails.get(s)); }
			 */

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

			/*
			 * System.out .println("For auction: " + auctionDetails
			 * .getString(AuctionServerPersistance.FIELD_AUCTION_ID));
			 * System.out.println("Local winners"); for (WinnerDetails winner :
			 * localWinners) { winner.printDetails(); }
			 * 
			 * System.out.println("Remote winners"); for (WinnerDetails winner :
			 * remoteWinners) { winner.printDetails(); }
			 */

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

			results = new AuctionResults(remoteWinners, localWinners,
					atLeastOneDealClaimed);
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

	public Boolean getClaimed() {
		return claimed;
	}

	public void setClaimed(Boolean claimed) {
		this.claimed = claimed;

	}

	public void claimAuction(WinnerDetails auctionObj) {
		System.out.println("In claim auction with currentAuctionkey as "
				+ currentAuctionKey + "and product as "
				+ auctionObj.getProductID());
		System.out.println("In claim auction");
		ClaimDetails claimDetailsObj = new ClaimDetails();
		claimDetailsObj.setAuctionId(currentAuctionKey);
		claimDetailsObj.setProductId(auctionObj.getProductID());
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

	public void claimSubscriptionAuction() {
		System.out.println("In claim subscription");
		SubscriptionAuctionDetails obj = new SubscriptionAuctionDetails();
		System.out.println("Current auction key:" + currentAuctionKey);
		obj.setAuctionId(currentAuctionKey);
		obj.setUserId(getUserObj().getName());
		if (currentAuctionKey == null) {
			return;
		}
		ClientConfig config = new DefaultClientConfig();
		Client remoteClient = Client.create(config);
		DBClient dbClient = DBClient.getInstance();
		BasicDBObject jsonAddr = dbClient.getClusterAddress("cars");
		String restAddr = "http://" + jsonAddr.getString("ip") + ":"
				+ jsonAddr.getInt("port")
				+ "/DistributedAuction/rest/claimSubscriptionAuction";
		System.out.println("Contacting " + restAddr
				+ " for claiming subscription");
		WebResource webResource = remoteClient.resource(restAddr);
		ClientResponse response = null;
		try {
			response = webResource.type(MediaType.APPLICATION_JSON).post(
					ClientResponse.class, obj);
			String isSuccess = response.getEntity(String.class);
			FacesMessage message = null;
			if (isSuccess.equals("success")) {
				message = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Successfully subscribed !!! ", null);
			} else {
				message = new FacesMessage(FacesMessage.SEVERITY_INFO,
						"Subscription failed !!! ", null);
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

	public String getCurrentAuctionKey() {
		return currentAuctionKey;
	}

	public void setCurrentAuctionKey(String currentAuctionKey) {
		this.currentAuctionKey = currentAuctionKey;
	}

	public String getAuctionDisplayHeaderKey() {

		auctionDisplayHeaderKey = "";
		if (currentAuctionKey != null && !currentAuctionKey.equals("")) {
			long epoch = Long.parseLong(currentAuctionKey.split("_")[1]);
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
		localWinners.clear();
		remoteWinners.clear();		
	}
}
