package org.ds.auction;

public class WinnerDetails {
	private String token;
	private String sellerID;
	private String productID;
	
	public String getToken(){
		return this.token;
	}
	
	public String getSellerID(){
		return this.sellerID;
	}
	
	public String getProductID(){
		return this.productID;
	}
	
	public WinnerDetails(String sellerID, String productID){
		this.sellerID = sellerID;
		this.productID = productID;
	}
}
