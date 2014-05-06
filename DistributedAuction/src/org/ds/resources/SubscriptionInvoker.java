package org.ds.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ds.auction.ClaimDetails;
import org.ds.auction.SubscriptionAuctionDetails;
import org.ds.claim.ClaimServer;
import org.ds.userServer.UserPersistance;

@Path("/claimSubscriptionAuction")
public class SubscriptionInvoker {
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response invokeAuction(SubscriptionAuctionDetails obj) {
		UserPersistance persistance = new UserPersistance(obj.getUserId());
		persistance.recordSubscription(obj.getAuctionId());
		return Response.status(200).entity("success").build();
	}
}
