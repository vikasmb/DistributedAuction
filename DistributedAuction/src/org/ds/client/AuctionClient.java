package org.ds.client;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.ds.carServer.SellerDetails;

import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
//AuctionClient
@ManagedBean
@SessionScoped
public class AuctionClient {
    private String category;
    
    @ManagedProperty(value="#{sellerDetails}")
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
	
	public void register() {
		       if(activeUser!=null)
		    	   System.out.println("User registered"+activeUser.getSellerName());
		       else
		    	   System.out.println("null");
	}
}
 