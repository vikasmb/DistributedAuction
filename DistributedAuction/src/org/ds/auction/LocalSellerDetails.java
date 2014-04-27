package org.ds.auction;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LocalSellerDetails extends SellerDetails {
	public LocalSellerDetails() {
		super();
	}

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
}
