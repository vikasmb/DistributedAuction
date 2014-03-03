package org.ds.carServer;

import javax.json.Json;
import javax.json.JsonObject;

import org.ds.client.*;

//Class to persist seller data
public class SellerStore {

	public static void main(String[] args) {
		JsonObject inputData = createDummyData();
		SellerStore sellerStore = new SellerStore();
		if(sellerStore.handleSellerRegistration(inputData)) {
			System.out.println("Seller details saved!");
		} else {
			System.out.println("Could not save seller details!");
		}
	}
	
	public static JsonObject createDummyData(){
		return Json.createObjectBuilder()
				.add(SellerDetails.FIELD_NAME, "Test Service")
				.add(SellerDetails.FIELD_REMOTE, true)
				.add(SellerDetails.FIELD_CALLBACK, "127.0.0.1")
				.build();			
	}
	
	/**
	 * provides a public interface for saving the seller
	 * @param inputData
	 * @return success
	 */
	public Boolean handleSellerRegistration(JsonObject inputData){
		SellerDetails sellerDetails = extractSellerData(inputData);
		return registerSeller(sellerDetails);
	}
	
	/**
	 * Extracts the fields expected in the seller data and returns it as an object
	 * @param inputData
	 * @return the seller details object
	 */
	private SellerDetails extractSellerData(JsonObject inputData){
		return new SellerDetails(inputData);
	}
	
	/**
	 * calls the mongo client to persist the seller data
	 * @param sellerDetails
	 * @return success
	 */
	private Boolean registerSeller(SellerDetails sellerDetails) {
		DBClient mongoClient = DBClient.getInstance();
		return mongoClient.persistSellerDetails(sellerDetails);
	}
	
	/**
	 * debug printer
	 * @param sellerDetails
	 */
	private void printSellerData(SellerDetails sellerDetails){
		System.out.println(sellerDetails.getSellerName());
		System.out.println(sellerDetails.getRemote());
		System.out.println(sellerDetails.getCallback());
	}
	

}
