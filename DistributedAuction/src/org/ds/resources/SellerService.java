package org.ds.resources;

import java.io.IOException;
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
		Random rand = new Random();
		Double minPriceToRespondTo = rand.nextDouble() * 60.0; // Randomize to
																// obtain
																// different
																// remote
																// services
		Set<Double> oldRoundPrices = auctionDetails.getOldBids();
		// Process oldBids to see if this remote seller wants to continue
		// bidding.
		Double minPriceInOlderRound = 30.0;
		BidDetails responseBid = new BidDetails();
		if (auctionDetails.getRoundNumber() >= 3) {
			responseBid.setBid(30.0);
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
