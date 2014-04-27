package org.ds.carServer;

import javax.json.Json;
import javax.json.JsonObject;

import org.ds.client.*;

//Class to persist seller data
public class SellerStore {
	/**
	 * provides a public interface for saving the seller
	 * @param inputData
	 * @return success
	 */
	public Boolean handleSellerRegistration(CarSellerDetails sellerDetails){
		return registerSeller(sellerDetails);
	}
	
	/**
	 * Extracts the fields expected in the seller data and returns it as an object
	 * @param inputData
	 * @return the seller details object
	 */
	private CarSellerDetails extractSellerData(JsonObject inputData){
		return new CarSellerDetails(inputData);
	}
	
	/**
	 * calls the mongo client to persist the seller data
	 * @param sellerDetails
	 * @return success
	 */
	private Boolean registerSeller(CarSellerDetails sellerDetails) {
		DBClient mongoClient = DBClient.getInstance();
		return mongoClient.persistSellerDetails(sellerDetails);
	}
	
	/**
	 * debug printer
	 * @param sellerDetails
	 */
	private void printSellerData(CarSellerDetails sellerDetails){
		System.out.println(sellerDetails.getSellerName());
		System.out.println(sellerDetails.getRemote());
		System.out.println(sellerDetails.getCallback());
	}
	

}
