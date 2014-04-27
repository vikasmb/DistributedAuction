package org.ds.client;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.ws.rs.core.MediaType;

import org.ds.auction.BuyerCriteria;
import org.ds.auction.ClientReadableBidderDetails;
import org.ds.auction.LocalSellerDetails;
import org.ds.auction.RemoteSellerDetails;
import org.ds.auction.SellerDetails;
import org.ds.resources.SellerService;
import org.ds.util.DateUtil;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

//AuctionClient
@ManagedBean(name = "auctionClient")
@SessionScoped
public class AuctionClient {
	private String category;
	private Client client;
	private ClientResponse response;
	@ManagedProperty(value = "#{sellerDetails}")
	private SellerDetails activeUser;
	@ManagedProperty(value = "#{buyerCriteria}")
	private BuyerCriteria buyerCriteria;

	public void setActiveUser(SellerDetails activeUser) {
		System.out.println("Setting activeUser bean");
		this.activeUser = activeUser;
	}

	public void setBuyerCriteria(BuyerCriteria buyerCriteria) {
		System.out.println("Setting buyerCriteria bean");
		this.buyerCriteria = buyerCriteria;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;

	}

	public void search() {
		category = buyerCriteria.getCategory();
		System.out.println("category set to" + category);
		DBClient client = DBClient.getInstance();
		buyerCriteria.setBuyerID("buyer123");
		// JSON.parse(jsonAddr);
		// FacesContext context = FacesContext.getCurrentInstance();
		//
		// context.addMessage(null, new FacesMessage("Successful",
		// "Cluster address for category " + category + " is "
		// + " ip with address " + jsonAddr.getString("ip")
		// + " and port is " + jsonAddr.getInt("port")));
		ClientConfig config = new DefaultClientConfig();
		Client remoteClient = Client.create(config);
		DBClient dbClient = DBClient.getInstance();
		BasicDBObject jsonAddr = dbClient.getClusterAddress(category);
		String restAddr = "http://" + jsonAddr.getString("ip") + ":"
				+ jsonAddr.getInt("port")
				+ "/DistributedAuction/rest/viewBidders";
		System.out.println("Contacting " + restAddr + " for viewBidders");
		BuyerCriteria criteria1 = new BuyerCriteria("123",
				DateUtil.getDate("2014-06-15T10:00:00"),
				DateUtil.getDate("2014-06-15T11:00:00"), "LA");
		WebResource webResource = remoteClient.resource(restAddr);
		ClientResponse response = null;
		try {
			response = webResource.type(MediaType.APPLICATION_JSON).post(
					ClientResponse.class, criteria1);
			/*ClientReadableBidderDetails detailsObj = response
					.getEntity(ClientReadableBidderDetails.class);
			List<LocalSellerDetails> localBidders = detailsObj
					.getLocalBidders();
			List<RemoteSellerDetails> remoteBidders = detailsObj
					.getRemoteBidders();
			if (localBidders == null) {
				System.out.println("Auction client found local bidders null");
			}
			if (remoteBidders == null) {
				System.out.println("Auction client found remote bidders null");
			}
			if (localBidders != null && remoteBidders != null) {
				System.out.println("Auction client found "
						+ localBidders.size() + " local sellers" + "and "
						+ remoteBidders.size() + " remote sellers");
				//LocalSellerDetails ls=localBidders.get(0);
				//System.out.println("#######First local:"+ls.getName());
			}*/
			RemoteSellerDetails remoteSellerDetails = response
					.getEntity(RemoteSellerDetails.class);
			System.out.println("###########IN CLIENT########");
			remoteSellerDetails.printDetails();
			//System.out.println("Class received:"+response.getClass());
			//System.out.println("Class received:"+response.get());
			/*SellerDetails sellerDetails = response
					.getEntity(SellerDetails.class);
			System.out.println("Display name"+sellerDetails.getDisplayName());*/
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void register() {
		if (activeUser != null) {
			ClientConfig config = new DefaultClientConfig();
			client = Client.create(config);
			DBClient dbClient = DBClient.getInstance();
			BasicDBObject jsonAddr = dbClient.getClusterAddress(category);
			String restAddr = "http://" + jsonAddr.getString("ip") + ":"
					+ jsonAddr.getInt("port") + "/DistributedAuction/rest/";
			// String
			// restAddr="http://"+"localhost"+":"+"8080"+"/DistributedAuction/rest/";
			WebResource webResource = client.resource(restAddr).path(
					"SellerService");
			try {
				response = webResource.type(MediaType.APPLICATION_JSON).post(
						ClientResponse.class, activeUser);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("Client recieved the status  of"
					+ response.getStatus() + " and response as "
					+ response.getEntity(String.class));
			System.out.println("Client exiting the rest call");
			// System.out.println("User registered" +
			// activeUser.getSellerName());
		} else
			System.out.println("null");
	}

	public static void main(String[] args) {
		BuyerCriteria buyerCriteria = new BuyerCriteria();
		buyerCriteria.setBuyerID("buyer123");
		buyerCriteria.setCity("LA");
		buyerCriteria.setNeededFrom(DateUtil.getDate("2014-03-15T10:00:00"));
		buyerCriteria.setNeededUntil(DateUtil.getDate("2014-03-15T11:00:00"));

		for (int i = 1; i <= 1; i++) {
			long startTime = System.currentTimeMillis();
			ClientConfig config = new DefaultClientConfig();
			Client client = Client.create(config);
			DBClient dbClient = DBClient.getInstance();
			BasicDBObject jsonAddr = dbClient.getClusterAddress("cars"); // TODO
																			// Replace
																			// with
																			// the
																			// actual
																			// service
																			// selection
																			// by
																			// the
																			// user
			String restAddr = "http://" + jsonAddr.getString("ip") + ":"
					+ jsonAddr.getInt("port") + jsonAddr.getString("path");
			System.out.println("Contacting " + restAddr + " for invokeAuction");

			WebResource webResource = client.resource(restAddr);
			ClientResponse response = null;
			try {
				response = webResource.type(MediaType.APPLICATION_JSON).post(
						ClientResponse.class, buyerCriteria);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("Client recieved the status  of"
					+ response.getStatus() + " and response as "
					+ response.getEntity(String.class));
			System.out.println("Client exiting the rest call");

			long difference = System.currentTimeMillis() - startTime;
			System.out.println("Latency: " + i + ", " + difference);
		}
	}

	public SellerDetails getActiveUser() {
		return activeUser;
	}

	public BuyerCriteria getBuyerCriteria() {
		return buyerCriteria;
	}
}
