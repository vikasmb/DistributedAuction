package org.ds.resources;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import org.ds.carServer.SellerDetails;

@Path("SellerService")
public class SellerService {
	    @POST	   
	    @Produces(MediaType.APPLICATION_JSON)
	    @Consumes(MediaType.APPLICATION_JSON)
	    public Response register(SellerDetails sellerObj) {
	    	
	    	System.out.println("Received Seller with name"+sellerObj.getSellerName());
	    	try {
				//Runtime.getRuntime().exec("gedit");
	    		Process p = new ProcessBuilder("gedit").start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	String result="Seller id 100 created";
	    	System.out.println("Returning after creating seller with id 100");
	        return Response.status(200).entity(result).build();
	    }
}
