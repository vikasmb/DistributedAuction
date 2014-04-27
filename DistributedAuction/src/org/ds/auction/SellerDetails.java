package org.ds.auction;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SellerDetails {
	
	private String sellerID;
	private String productID;
	
	private String name;
	private String model;
	private String address;
	private String image;
	private String displayName;
	
	public static String FIELD_NAME = "name";
	public static String FIELD_MODEL = "model";
	public static String FIELD_ADDRESS = "address";
	public static String FIELD_IMAGE = "image";
	
	public String getSellerID(){
		return this.sellerID;
	}
	
	public String getProductID(){
		return this.productID;
	}
	
	public String getName(){
		return this.name;
	}
	
	public String getModel(){
		return this.model;
	}
	
	public String getAddress(){
		return this.address;
	}
	
	public String getImage(){
		return this.image;
	}
	
	protected void setSellerID(String sellerID){
		this.sellerID = sellerID;
	}
	
	protected void setProductID(String productID){
		this.productID = productID;
	}
	
	protected void setName(String name){
		this.name = name;
	}
	
	protected void setModel(String model){
		this.model = model;
	}
	
	protected void setAddress(String address){
		this.address = address;
	}
	
	protected void setImage(String image){
		this.image = image;
	}
	
	public SellerDetails(){
		
	}
	
	public void printDetails(){
		System.out.println("ProductID: " + getProductID() + ". SellerID: " + getSellerID());
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
}
