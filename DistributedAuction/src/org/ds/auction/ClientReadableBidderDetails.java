package org.ds.auction;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.mongodb.BasicDBObject;

@XmlRootElement
public class ClientReadableBidderDetails {
	@XmlElement(name = "remoteBidders")
	private List<RemoteSellerDetails> remoteBidders;
	@XmlElement(name = "localBidders")
	private List<LocalSellerDetails> localBidders;
	
	public ClientReadableBidderDetails(){
		super();
	}
	
	public List<RemoteSellerDetails> getRemoteBidders(){
		return this.remoteBidders;
	}
	
	public List<LocalSellerDetails> getLocalBidders(){
		return this.localBidders;
	}
	
	public ClientReadableBidderDetails(List<RemoteSellerDetails> remoteBidders, List<LocalSellerDetails> localBidders) {
		this.remoteBidders = remoteBidders;
		this.localBidders = localBidders;
	}
	
	public void printDetails(){
		System.out.println("Local bidders: ");
		for(LocalSellerDetails localBidder:this.localBidders){
			localBidder.printDetails();
		}
		
		System.out.println("Remote bidders: ");
		for(RemoteSellerDetails remoteBidder:this.remoteBidders){
			remoteBidder.printDetails();
		}
	}
	
	public static ClientReadableBidderDetails convertToClientReadable(BidderDetails bidderDetails){
		List<BasicDBObject> remoteBidders = bidderDetails.getRemoteBidders();
		List<BasicDBObject> localBidders = bidderDetails.getLocalBidders();
		
		List<RemoteSellerDetails> convertedRemoteBidders = new ArrayList<RemoteSellerDetails>();
		List<LocalSellerDetails> convertedLocalBidders = new ArrayList<LocalSellerDetails>();
		
		for (int i = 0; i < remoteBidders.size(); i++) {
			BasicDBObject remoteBidder = remoteBidders.get(i);
			convertedRemoteBidders.add(new RemoteSellerDetails(remoteBidder.getString(AuctionServer.FIELD_SELLER_ID), 
					remoteBidder.getString(AuctionServer.FIELD_PRODUCT_ID),
					remoteBidder.getString(SellerDetails.FIELD_NAME),
					remoteBidder.getString(SellerDetails.FIELD_ADDRESS),
					remoteBidder.getString(SellerDetails.FIELD_IMAGE)));
		}
		
		for (int i = 0; i < localBidders.size(); i++) {
			BasicDBObject localBidder = localBidders.get(i);
			convertedLocalBidders.add(new LocalSellerDetails(localBidder.getString(AuctionServer.FIELD_SELLER_ID), 
					localBidder.getString(AuctionServer.FIELD_PRODUCT_ID),
					localBidder.getString(SellerDetails.FIELD_NAME),
					localBidder.getString(SellerDetails.FIELD_MODEL),
					localBidder.getString(SellerDetails.FIELD_ADDRESS),
					localBidder.getString(SellerDetails.FIELD_IMAGE)));
		}
		
		return new ClientReadableBidderDetails(convertedRemoteBidders, convertedLocalBidders);
	}
}
