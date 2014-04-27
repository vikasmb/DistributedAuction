package org.ds.auction;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RemoteSellerDetails {

	private Double price;
	private String remoteAddress;
	private String name;
	private String model;
	private String address;
	private String image;
	private String displayName;
	private String sellerID;
	private String productID;

	public RemoteSellerDetails() {
		super();
	}

	public Double getPrice() {
		return this.price;
	}

	public String getRemoteAddress() {
		return this.remoteAddress;
	}

	public Boolean hasMadeBid() {
		return getPrice() >= 0;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public RemoteSellerDetails(String remoteAddress, String sellerID,
			String productID) {
		this.remoteAddress = remoteAddress;
		setSellerID(sellerID);
		setProductID(productID);
	}

	public RemoteSellerDetails(String sellerID, String productID, String name,
			String address, String image) {
		setSellerID(sellerID);
		setProductID(productID);

		setName(name);
		setAddress(address);
		setImage(image);
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

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
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
	
	public void printDetails(){
		System.out.println("ProductID: " + getProductID() + ". SellerID: " + getSellerID());
	}
}
