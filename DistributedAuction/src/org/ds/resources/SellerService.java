package org.ds.resources;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ds.auction.BidDetails;
import org.ds.carServer.SellerDetails;
import org.ds.carServer.SellerStore;

@Path("SellerService")
public class SellerService {
	
	int DEFAULT_BID_PROBABILITY = 100;
	int DEFAULT_BID_LOWER_PROBABILITY = 0;
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/register")
	public Response register(SellerDetails sellerObj) {

		System.out.println("Received Seller with name"
				+ sellerObj.getSellerName());
		String result = "Error saving seller details!";
		SellerStore sellerStore = new SellerStore();
		Boolean success = sellerStore.handleSellerRegistration(sellerObj);
		if (success) {
			result = "Seller details saved!";
		}
		return Response.status(200).entity(result).build();
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/respondToBid")
	public Response respondToBid(RemoteAuctionDetails auctionDetails) { // BuyerCriteria,OldBids
																		// and
																		// auctionId

		System.out.println("Processing auction of id"
				+ auctionDetails.getAuctionId()+" in round"+auctionDetails.getRoundNumber());
		// For testing timeout of remote peers
		/*if( (Math.random()*100.0f -50.0)>0){
			try {
				System.out.println("Sleeping for 16 seconds");
				Thread.sleep(16000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/	
		Set<Double> oldBids  = auctionDetails.getOldBids();
		int roundNumber = auctionDetails.getRoundNumber();
		Double lowestBid = (Math.random() * 1000);
		Double highestBid = lowestBid;
		if(oldBids != null && oldBids.size() > 0 ){
			lowestBid = oldBids.iterator().next();
			for(Double price:oldBids){
				highestBid = price;
			}
		}	
		
		HashMap<Integer, Integer> bidProbabilityMap = getMakeBidProbabilityMap();
		HashMap<Integer, Integer> bidLowerProbabilityMap = getBidLowerProbabilityMap();
		
		int bidProbability = DEFAULT_BID_PROBABILITY;
		int bidLowerProbability = DEFAULT_BID_LOWER_PROBABILITY;
		
		if(bidProbabilityMap.containsKey(roundNumber)){
			bidProbability = bidProbabilityMap.get(roundNumber);
		}
		
		if(bidLowerProbabilityMap.containsKey(roundNumber)){
			bidLowerProbability = bidLowerProbabilityMap.get(roundNumber);
		}
		
		int makeBidRand = (int) (Math.random() * 100);
		int bidLowerRand = (int) (Math.random() * 100);
		
		BidDetails responseBid = new BidDetails();
		
		if(makeBidRand < bidProbability){
			if(bidLowerRand < bidLowerProbability){
				double bid = lowestBid + (Math.random() * 100) * (highestBid - lowestBid);
				responseBid.setBid(bid);
			} else {
				responseBid.setBid(lowestBid);	
			}
			responseBid.setMadeBid(true);
		} else {
			responseBid.setBid(-1.0);
			responseBid.setMadeBid(false);
		}
		
		return Response.status(200).entity(responseBid).build();
	}
	
	private HashMap<Integer, Integer> getMakeBidProbabilityMap(){
		HashMap<Integer, Integer> probabilityMap = new HashMap<Integer, Integer>();
		probabilityMap.put(1, 50);
		probabilityMap.put(2, 60);
		probabilityMap.put(3, 70);
		probabilityMap.put(4, 80);
		probabilityMap.put(5, 90);
		return probabilityMap;
	}
	
	private HashMap<Integer, Integer> getBidLowerProbabilityMap(){
		HashMap<Integer, Integer> probabilityMap = new HashMap<Integer, Integer>();
		probabilityMap.put(1, 100);
		probabilityMap.put(2, 80);
		probabilityMap.put(3, 60);
		probabilityMap.put(4, 40);
		probabilityMap.put(5, 20);
		return probabilityMap;
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/respondToBid1")
	public Response respondToBid1(RemoteAuctionDetails auctionDetails) { // BuyerCriteria,OldBids
																		// and
																		// auctionId

		System.out.println("Processing auction of id"
				+ auctionDetails.getAuctionId()+" in round"+auctionDetails.getRoundNumber());
		Random rand = new Random();
		Double minPriceToRespondTo = rand.nextDouble() * 60.0; // Randomize to
																// obtain
																// different
																// remote
																// services
		Set<Double> oldRoundPrices = auctionDetails.getOldBids();
		// Process oldBids to see if this remote seller wants to continue
		// bidding.
		Double minPriceInOlderRound = 25.0;
		BidDetails responseBid = new BidDetails();
		if (auctionDetails.getRoundNumber() >= 3) {
			responseBid.setBid(25.0);
			responseBid.setMadeBid(true);
		} else {
			responseBid.setBid(minPriceToRespondTo);
			responseBid.setMadeBid(false);
			if (minPriceInOlderRound > minPriceToRespondTo) {
				responseBid.setMadeBid(true);
			}
		}
		return Response.status(200).entity(responseBid).build();
	}
}
