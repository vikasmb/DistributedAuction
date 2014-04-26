package org.ds.auction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

public class WinnerDetails extends SellerDetails {
	private Double price;
	private String token;
	
	public Double getPrice(){
		return this.price;
	}
	
	public String getToken(){
		return this.token;
	}
	
	public WinnerDetails(Double price, String sellerID, String productID){
		this.price = price;
		this.token = "";
		setSellerID(sellerID);
		setProductID(productID);
	}
	
	public WinnerDetails(Double price, String sellerID, String productID, String name, String model, String address, String image){
		this.price = price;
		this.token = "";
		setSellerID(sellerID);
		setProductID(productID);
		
		setName(name);
		setModel(model);
		setAddress(address);
		setImage(image);
	}
	
	public WinnerDetails(Double price, String sellerID, String productID, String name, String address, String image){
		this.price = price;
		this.token = "";
		setSellerID(sellerID);
		setProductID(productID);
		
		setName(name);
		setAddress(address);
		setImage(image);
	}
	
	public static List<WinnerDetails> getWinnersDetails(Double price,
			List<? extends SellerDetails> sellersDetails) {
		List<WinnerDetails> winnersDetails = new ArrayList<WinnerDetails>();
		for (int i = 0; i < sellersDetails.size(); i++) {
			SellerDetails sellerDetails = sellersDetails.get(i);
			winnersDetails.add(new WinnerDetails(price, sellerDetails.getSellerID(),
					sellerDetails.getProductID()));
		}
		return winnersDetails;
	}
	
	public void printDetails(){
		super.printDetails();
		System.out.println("Price: " + getPrice() + " Token: " + getToken());
	}
}
