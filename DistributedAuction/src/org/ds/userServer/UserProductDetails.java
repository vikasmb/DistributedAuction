package org.ds.userServer;

public class UserProductDetails {
	public UserProductDetails(String category, String productID) {
		super();
		this.category = category;
		this.productID = productID;
	}
	
	private String category;
	private String productID;
	
	public String getCategory() {
		return category;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	
	public String getProductID() {
		return productID;
	}
	
	public void setProductID(String productID) {
		this.productID = productID;
	}
}
