package org.ds.auction;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LocalSellerDetails {
	public LocalSellerDetails() {
		super();
	}
	private String sellerID;
	private String productID;
	
	private String name;
	private String model;
	private String address;
	private String image;
	private Double listPrice;
	private Double minPrice;

	public Double getListPrice() {
		return this.listPrice;
	}

	public Double getMinPrice() {
		return this.minPrice;
	}

	public LocalSellerDetails(Double listPrice, Double minPrice,
			String sellerID, String productID) {
		this.listPrice = listPrice;
		this.minPrice = minPrice;
		setSellerID(sellerID);
		setProductID(productID);
	}

	public LocalSellerDetails(String sellerID, String productID, String name,
			String model, String address, String image) {
		setSellerID(sellerID);
		setProductID(productID);

		setName(name);
		setModel(model);
		setAddress(address);
		setImage(image);
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

	public void setListPrice(Double listPrice) {
		this.listPrice = listPrice;
	}

	public void setMinPrice(Double minPrice) {
		this.minPrice = minPrice;
	}
	public void printDetails(){
		System.out.println("ProductID: " + getProductID() + ". SellerID: " + getSellerID());
	}
}
