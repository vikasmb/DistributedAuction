package org.ds.client;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.ws.rs.core.MediaType;

import org.ds.carServer.SellerDetails;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

//AuctionClient
@ManagedBean
@SessionScoped
public class AuctionClient {
	private String category;
	private Client client;
	private ClientResponse response;
	@ManagedProperty(value = "#{sellerDetails}")
	private SellerDetails activeUser;

	public void setActiveUser(SellerDetails activeUser) {
		System.out.println("Setting activeUser bean");
		this.activeUser = activeUser;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;

	}

	public void search() {
		System.out.println("category set to" + category);
		DBClient client = DBClient.getInstance();
		BasicDBObject jsonAddr = client.getClusterAddress(category);
		// JSON.parse(jsonAddr);
		FacesContext context = FacesContext.getCurrentInstance();

		context.addMessage(null, new FacesMessage("Successful",
				"Cluster address for category " + category + " is "
						+ " ip with address " + jsonAddr.getString("ip")
						+ " and port is " + jsonAddr.getInt("port")));
	}

	public void register() {
		if (activeUser != null) {
			ClientConfig config = new DefaultClientConfig();
			client = Client.create(config);
			DBClient dbClient = DBClient.getInstance();
			BasicDBObject jsonAddr = dbClient.getClusterAddress(category);
			String restAddr="http://"+jsonAddr.getString("ip")+":"+jsonAddr.getInt("port")+"/DistributedAuction/rest/";
			//String restAddr="http://"+"localhost"+":"+"8080"+"/DistributedAuction/rest/";
			WebResource webResource=client.resource(restAddr).path("SellerService");
			try{
				 response = webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class,activeUser);
			}
			catch(Exception e){
				e.printStackTrace();
			}
			System.out.println("Client recieved the status  of"+response.getStatus()+" and response as "+response.getEntity(String.class));
			System.out.println("Client exiting the rest call");
			//System.out.println("User registered" + activeUser.getSellerName());
		} else
			System.out.println("null");
	}
}
