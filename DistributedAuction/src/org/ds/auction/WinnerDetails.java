package org.ds.auction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

public class WinnerDetails {
	private Double price;
	private String token;
	private String sellerID;
	private String productID;
	private String name;
	private String model;
	private String address;
	private String image;
	
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
	
	
	
	public static List<WinnerDetails> getLocalWinnersDetails(Double price,
			List<LocalSellerDetails> sellersDetails) {
		List<WinnerDetails> winnersDetails = new ArrayList<WinnerDetails>();
		for (int i = 0; i < sellersDetails.size(); i++) {
			LocalSellerDetails sellerDetails = sellersDetails.get(i);
			winnersDetails.add(new WinnerDetails(price, sellerDetails.getSellerID(),
					sellerDetails.getProductID()));
		}
		return winnersDetails;
	}
	
	public static List<WinnerDetails> getRemoteWinnersDetails(Double price,
			List<RemoteSellerDetails> sellersDetails) {
		List<WinnerDetails> winnersDetails = new ArrayList<WinnerDetails>();
		for (int i = 0; i < sellersDetails.size(); i++) {
			RemoteSellerDetails sellerDetails = sellersDetails.get(i);
			winnersDetails.add(new WinnerDetails(price, sellerDetails.getSellerID(),
					sellerDetails.getProductID()));
		}
		return winnersDetails;
	}
	
	public void printDetails(){
		System.out.println("ProductID: " + getProductID() + ". SellerID: " + getSellerID());
		System.out.println("Price: " + getPrice() + " Token: " + getToken());
	}

	public String getSellerID() {
		return sellerID;
	}

	public void setSellerID(String sellerID) {
		this.sellerID = sellerID;
	}

	public String getProductID() {
		return productID;
	}

	public void setProductID(String productID) {
		this.productID = productID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
