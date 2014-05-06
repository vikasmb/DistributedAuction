package org.ds.resources;

import org.ds.userServer.UserPersistance;

public class Subscriber {
	
	public static void main(String args[]){
		Subscriber subscriber = new Subscriber();
		subscriber.subscribe("123", "123_1398545770925");
	}
	
	public void subscribe(String userID, String auctionID){
		UserPersistance persistance = new UserPersistance(userID);
		persistance.recordSubscription(auctionID);
	}
}
