package org.ds.auction;

import java.awt.print.Printable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ds.client.DBClient;

import com.mongodb.BasicDBObject;

public class SelectionServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		SelectionServer server = new SelectionServer();
		DBClient client = DBClient.getInstance();
		
			BidderDetails detailsObj=client.getPotentialSellers("cars", "LA", "2014-03-15T11:00:00",
					"2014-03-15T14:00:00");

			System.out.println("Local Size:" + detailsObj.getLocalBidders().size());
			System.out.println("Remote Size:" + detailsObj.getRemoteBidders().size());
			// server.printArgs(args);
			/*ProcessBuilder builder = new ProcessBuilder("java", "-jar",
					"/tmp/SelectionServer.jar hello");
			try {
				Process p = builder.start();
				// Runtime.getRuntime().exec("java  -jar /tmp/SelectionServer.jar hello");
				Runtime.getRuntime().exec("gedit");
				System.out.println("Continuiung");
			} catch (IOException e) {
				e.printStackTrace();
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
