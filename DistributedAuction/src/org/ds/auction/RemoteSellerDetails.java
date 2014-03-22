package org.ds.auction;

public class RemoteSellerDetails extends SellerDetails {

	private Double price;
	private String remoteAddress;
	
	public Double getPrice(){
		return this.price;
	}
	
	public String getRemoteAddress(){
		return this.remoteAddress;
	}
	
	public Boolean hasMadeBid(){
		return getPrice() >= 0;
	}
	
	public void setPrice(Double price){
		this.price = price;
	}

	
	public RemoteSellerDetails(String remoteAddress, String sellerID, String productID){
		this.remoteAddress = remoteAddress;
		setSellerID(sellerID);
		setProductID(productID);
	}
}
