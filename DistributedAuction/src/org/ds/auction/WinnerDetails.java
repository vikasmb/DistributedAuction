package org.ds.auction;

public class WinnerDetails {
	private Double winPrice;
	private String token;
	private String sellerID;
	private String productID;
	
	public Double getWinPrice(){
		return this.winPrice;
	}
	
	public String getToken(){
		return this.token;
	}
	
	public String getSellerID(){
		return this.sellerID;
	}
	
	public String getProductID(){
		return this.productID;
	}
	
	public WinnerDetails(Double winPrice, String sellerID, String productID){
		this.winPrice = winPrice;
		this.sellerID = sellerID;
		this.productID = productID;
	}
}
