package org.ds.auction;

import java.awt.print.Printable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ds.client.DBClient;
import org.ds.util.DateUtil;

import com.mongodb.BasicDBObject;

public class SelectionServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		
		  SelectionServer server = new SelectionServer(); DBClient client =
		  DBClient.getInstance();
		  
		  BidderDetails detailsObj=client.getPotentialSellers("cars", "LA",
		  "2014-03-15T10:00:00", "2014-03-15T11:00:00");
		  
		  System.out.println("Local Size:" +
		  detailsObj.getLocalBidders().size());
		  System.out.println("Remote Size:" +
		  detailsObj.getRemoteBidders().size()); // server.printArgs(args);
		 //Pass the local and remote bidders list to auction server.
		  BuyerCriteria criteria=new BuyerCriteria("123", DateUtil.getDate("2014-03-15T10:00:00"), DateUtil.getDate("2014-03-15T11:00:00"), "LA");
		  AuctionServer auctionServer=new AuctionServer(detailsObj, criteria);
		  auctionServer.run();
		  
		/*if (args.length > 0) {
			try {
				// Thread.currentThread();
				System.out.println("Starting sleep");
				Thread.sleep(3000);
				System.out.println("Ending sleep");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Received args");
		} else {
			System.out.println("Spawning new process");
			ProcessBuilder builder = new ProcessBuilder("java", "-jar",
					"SelectionServer.jar hello");
			builder.directory(new File("/home/vikas"));
			File dirOut = new File("/home/vikas/out.txt");
			File dirErr = new File("/home/vikas/err.txt");

			builder.redirectOutput(dirOut);
			builder.redirectError(dirErr);
			try {
				Process p = builder.start();
				
				// Runtime.getRuntime().exec("java  -jar /tmp/SelectionServer.jar hello");
				//Runtime.getRuntime().exec("gedit");
				System.out.println("Continuiung");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}*/

	}

	private void printArgs(String[] args) {
		try {
			// Thread.currentThread();
			System.out.println("Starting sleep");
			Thread.sleep(3000);
			System.out.println("Ending sleep");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Received args");

	}

}
