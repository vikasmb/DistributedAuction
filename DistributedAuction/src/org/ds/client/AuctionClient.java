package org.ds.client;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
//AuctionClient
public class AuctionClient {
    private String category;
    

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
		System.out.println("Setting category to"+category);
	}
	public void search() {
		System.out.println("category set to"+category);
		DBClient client=DBClient.getInstance();
		BasicDBObject jsonAddr=client.getClusterAddress(category);
		//JSON.parse(jsonAddr);
		 FacesContext context = FacesContext.getCurrentInstance();  
         
	        context.addMessage(null, new FacesMessage("Successful", "Cluster address for category " + category+" is "+ " ip with address "+jsonAddr.getString("ip")+" and port is "+jsonAddr.getInt("port")));  
	}
}
 