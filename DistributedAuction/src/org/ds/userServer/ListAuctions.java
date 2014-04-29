package org.ds.userServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import org.ds.auction.BuyerCriteria;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

@ManagedBean
public class ListAuctions {
	private List<AuctionDetails> auctionDetailsList;
	@ManagedProperty(value = "#{buyerCriteria}")
	private BuyerCriteria buyerCriteria;

	public static void main(String args[]) {
		String userID = "123";
		List<String> auctionIDs = getUserAuctions(userID);
		for (String id : auctionIDs) {
			System.out.println(id);
		}
	}

	public ListAuctions() {
		super();
	}

	public static List<String> getUserAuctions(String userID) {
		userID="123";
		UserPersistance persistance = new UserPersistance(userID);
		BasicDBList auctions = persistance.getAuctions(userID);

		List<String> auctionIDs = new ArrayList<String>();
		for (Object auctionObj : auctions) {
			BasicDBObject auction = (BasicDBObject) auctionObj;
			auctionIDs
					.add(auction
							.getString(UserPersistance.FIELD_AUCTIONS_CATEGORY)
							+ ":"
							+ auction
									.getString(UserPersistance.FIELD_AUCTIONS_AUCTION_ID));
		}

		Collections.sort(auctionIDs, Collections.reverseOrder());
		return auctionIDs;
	}

	

	public List<AuctionDetails> getAuctionDetailsList() {
		if (auctionDetailsList == null) {
			auctionDetailsList = new ArrayList<AuctionDetails>();
			List<String> auctions = getUserAuctions(buyerCriteria.getBuyerID());
			for (String auction : auctions) {
				String[] splitParts = auction.split(":");
				AuctionDetails obj = new AuctionDetails(splitParts[0],
						splitParts[1].split("_")[1]);
				auctionDetailsList.add(obj);

			}
		}
		return auctionDetailsList;
	}

	public void setAuctionDetailsList(List<AuctionDetails> auctionDetailsList) {
		this.auctionDetailsList = auctionDetailsList;
	}

	public BuyerCriteria getBuyerCriteria() {
		return buyerCriteria;
	}

	public void setBuyerCriteria(BuyerCriteria buyerCriteria) {
		this.buyerCriteria = buyerCriteria;
	}

}
