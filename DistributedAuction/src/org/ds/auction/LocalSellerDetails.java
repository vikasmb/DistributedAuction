package org.ds.auction;

public class LocalSellerDetails {
	
	private Double listPrice;
	private Double minPrice;
	private String sellerID;
	private String productID;
	
	public Double getListPrice(){
		return this.listPrice;
	}
	
	public Double getMinPrice(){
		return this.minPrice;
	}
	
	public String getSellerID(){
		return this.sellerID;
	}
	
	public String getProductID(){
		return this.productID;
	}
	
	public LocalSellerDetails(Double listPrice, Double minPrice, String sellerID, String productID){
		this.listPrice = listPrice;
		this.minPrice = minPrice;
		this.sellerID = sellerID;
		this.productID = productID;
	}
}
