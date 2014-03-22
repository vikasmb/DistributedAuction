package org.ds.auction;

public class SellerDetails {
	
	private String sellerID;
	private String productID;
	
	public String getSellerID(){
		return this.sellerID;
	}
	
	public String getProductID(){
		return this.productID;
	}
	
	protected void setSellerID(String sellerID){
		this.sellerID = sellerID;
	}
	
	protected void setProductID(String productID){
		this.productID = productID;
	}
	
	public SellerDetails(){
		
	}
	
}
