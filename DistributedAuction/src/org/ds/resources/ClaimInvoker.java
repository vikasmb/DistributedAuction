package org.ds.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ds.auction.BuyerCriteria;
import org.ds.auction.ClaimDetails;
import org.ds.claim.ClaimServer;

@Path("/claimAuction")
public class ClaimInvoker {
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response invokeAuction(ClaimDetails claimDetails) {
		ClaimServer server = new ClaimServer(claimDetails.getAuctionId(), claimDetails.getProductId());
		Boolean isSuccess=server.claim();
		String successStatus=null;
		if(isSuccess){
			successStatus="true";
		}
		else{
			successStatus="false";
		}
		return Response.status(200).entity(successStatus).build();
	}
}
