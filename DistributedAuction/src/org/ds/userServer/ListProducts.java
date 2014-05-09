package org.ds.userServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ds.auction.BuyerCriteria;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class ListProducts {
	private List<UserProductDetails> productsDetailsList;
	private BuyerCriteria buyerCriteria;

	public static void main(String args[]) {
		String userID = "123";
		List<String> products = getUserProducts(userID);
		for (String id : products) {
			System.out.println(id);
		}
	}

	public static List<String> getUserProducts(String userID) {
		UserPersistance persistance = new UserPersistance(userID);
		BasicDBList products = persistance.getProducts();

		List<String> productIDs = new ArrayList<String>();
		for (Object productObj : products) {
			BasicDBObject product = (BasicDBObject) productObj;
			productIDs
					.add(product
							.getString(UserPersistance.FIELD_PRODUCTS_CATEGORY)
							+ ":"
							+ product
									.getString(UserPersistance.FIELD_PRODUCTS_PRODUCT_ID));
		}

		Collections.sort(productIDs, Collections.reverseOrder());
		return productIDs;
	}

	

	public List<UserProductDetails> getProductsDetailsList() {
		if (productsDetailsList == null) {
			productsDetailsList = new ArrayList<UserProductDetails>();
			List<String> products = getUserProducts(buyerCriteria.getBuyerID());
			for (String product : products) {
				String[] splitParts = product.split(":");
				UserProductDetails obj = new UserProductDetails(splitParts[0],
						splitParts[1].split("_")[1]);
				productsDetailsList.add(obj);

			}
		}
		return productsDetailsList;
	}

	public void setProductDetailsList(List<UserProductDetails> productsDetailsList) {
		this.productsDetailsList = productsDetailsList;
	}

	public BuyerCriteria getBuyerCriteria() {
		return buyerCriteria;
	}

	public void setBuyerCriteria(BuyerCriteria buyerCriteria) {
		this.buyerCriteria = buyerCriteria;
	}
}
