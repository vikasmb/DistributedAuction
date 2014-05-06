package org.ds.userServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.ds.auction.BuyerCriteria;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

@ManagedBean
@SessionScoped
public class ListSubscriptions {
	private List<SubscriptionDetails> subscriptionsDetailsList;
	private BuyerCriteria buyerCriteria;

	public static void main(String args[]) {
		String userID = "123";
		List<String> subscriptions = getUserSubscriptions(userID);
		for (String id : subscriptions) {
			System.out.println(id);
		}
	}

	public static List<String> getUserSubscriptions(String userID) {
		userID = "123";
		UserPersistance persistance = new UserPersistance(userID);
		BasicDBList subscriptions = persistance.getSubscriptions();

		List<String> subscriptionIDs = new ArrayList<String>();
		for (Object subscriptionObj : subscriptions) {
			BasicDBObject subscription = (BasicDBObject) subscriptionObj;
			subscriptionIDs
					.add(subscription
							.getString(UserPersistance.FIELD_SUBSCRIPTIONS_CATEGORY)
							+ ":"
							+ subscription
									.getString(UserPersistance.FIELD_SUBSCRIPTION_AUCTION_ID));
		}

		Collections.sort(subscriptionIDs, Collections.reverseOrder());
		return subscriptionIDs;
	}

	public List<SubscriptionDetails> getSubscriptionsDetailsList() {

		subscriptionsDetailsList = new ArrayList<SubscriptionDetails>();
		List<String> subscriptions = getUserSubscriptions("123");// TODO Replace
																	// with user
																	// id
		for (String subscription : subscriptions) {
			String[] splitParts = subscription.split(":");
			SubscriptionDetails obj = new SubscriptionDetails(splitParts[0],
					splitParts[1].split("_")[1]);
			subscriptionsDetailsList.add(obj);

		}

		return subscriptionsDetailsList;
	}

	public void setAuctionDetailsList(
			List<SubscriptionDetails> subscriptionDetailsList) {
		this.subscriptionsDetailsList = subscriptionDetailsList;
	}

	public BuyerCriteria getBuyerCriteria() {
		return buyerCriteria;
	}

	public void setBuyerCriteria(BuyerCriteria buyerCriteria) {
		this.buyerCriteria = buyerCriteria;
	}

}
