package org.ds.auction;

import java.io.IOException;

public class SelectionServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	     SelectionServer server=new SelectionServer();
	     server.printArgs(args);
	     ProcessBuilder builder = new ProcessBuilder("java",  "-jar", "/tmp/SelectionServer.jar hello");
			try {
				Process p =builder.start();
				System.out.println("Continuiung");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}

	private void printArgs(String[] args) {
		try {
			//Thread.currentThread();
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Received args");
		
	}

}
