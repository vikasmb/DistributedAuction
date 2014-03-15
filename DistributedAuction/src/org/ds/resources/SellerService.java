package org.ds.resources;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import org.ds.carServer.SellerDetails;
import org.ds.carServer.SellerStore;

@Path("SellerService")
public class SellerService {
	    @POST	   
	    @Produces(MediaType.APPLICATION_JSON)
	    @Consumes(MediaType.APPLICATION_JSON)
	    public Response register(SellerDetails sellerObj) {
	    	
	    	System.out.println("Received Seller with name"+sellerObj.getSellerName());
			String result = "Error saving seller details!";
	    	SellerStore sellerStore = new SellerStore();
			Boolean success = sellerStore.handleSellerRegistration(sellerObj);	
			if(success) {
				result = "Seller details saved!";
			}
	        return Response.status(200).entity(result).build();
	    }
}
