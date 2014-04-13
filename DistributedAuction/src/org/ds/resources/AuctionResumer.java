package org.ds.resources;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/resumeAuction")
public class AuctionResumer {

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response resumeAuction(RemoteAuctionDetails auctionToBeResumed) {
		System.out.println("****Resuming auction id:"
				+ auctionToBeResumed.getAuctionId());
		ProcessBuilder builder = new ProcessBuilder("java", "-jar",
				"SelectionServer.jar",auctionToBeResumed.getAuctionId());
		//ProcessBuilder builder = new ProcessBuilder("java", "-version");
		String homeDir=System.getProperty("user.home");
		builder.directory(new File(homeDir));
		File dirOut = new File(homeDir+"/out.txt");
		File dirErr = new File(homeDir+"/err.txt");

		builder.redirectOutput(dirOut);
		builder.redirectError(dirErr);
		System.out.println("Process Builder's command"+builder.command());
		String status=null;
		try {
			Process p = builder.start();
			
			System.out.println("Continuiung after scheduling auction");
			status="Auction resumed";
		} catch (IOException e) {
			e.printStackTrace();
			status="Failure to resume auction";
		}
		return Response.status(200).entity(status).build();
	}
}
