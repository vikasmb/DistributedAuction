package org.ds.auction;

public class LocalSellerDetails extends SellerDetails{
	
	private Double listPrice;
	private Double minPrice;
	
	public Double getListPrice(){
		return this.listPrice;
	}
	
	public Double getMinPrice(){
		return this.minPrice;
	}
	
	public LocalSellerDetails(Double listPrice, Double minPrice, String sellerID, String productID){
		this.listPrice = listPrice;
		this.minPrice = minPrice;
		setSellerID(sellerID);
		setProductID(productID);
	}
}
