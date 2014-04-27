package org.ds.resources;

import java.util.TimeZone;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ds.auction.BidderDetails;
import org.ds.auction.BuyerCriteria;
import org.ds.auction.ClientReadableBidderDetails;
import org.ds.auction.RemoteSellerDetails;
import org.ds.auction.SellerDetails;
import org.ds.client.DBClient;
import org.ds.util.DateUtil;

@Path("viewBidders")
public class ViewBidders {
	public static void main(String args[]){
		BuyerCriteria criteria = new BuyerCriteria("123",
				DateUtil.getDate("2014-06-15T10:00:00"),
				DateUtil.getDate("2014-06-15T11:00:00"), "LA");
		//ClientReadableBidderDetails bidderDetails = getBidderDetails(criteria);
		DBClient client = DBClient.getInstance();
		BidderDetails detailsObj = client.getPotentialSellers(criteria.getCategory(), criteria.getCity(), DateUtil.getStringFromDate(criteria.getNeededFrom()),
				DateUtil.getStringFromDate(criteria.getNeededUntil()), true);
		ClientReadableBidderDetails bidderDetails=ClientReadableBidderDetails.convertToClientReadable(detailsObj);

		bidderDetails.printDetails();
//		String[] tzList=TimeZone.getAvailableIDs();
//		for(String s:tzList){
//			System.out.println("Timezone:"+s);
//		}
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getBidderDetails(BuyerCriteria criteria){
		DBClient client = DBClient.getInstance();
		System.out.println("Needed from:"+DateUtil.getStringFromDate(criteria.getNeededFrom()));
		System.out.println("Needed till:"+DateUtil.getStringFromDate(criteria.getNeededUntil()));
		BidderDetails detailsObj = client.getPotentialSellers(criteria.getCategory(), criteria.getCity(), DateUtil.getStringFromDate(criteria.getNeededFrom()),
				DateUtil.getStringFromDate(criteria.getNeededUntil()), true);
		ClientReadableBidderDetails obj=ClientReadableBidderDetails.convertToClientReadable(detailsObj);
		//obj.printDetails();
		System.out.println("REST returning local size"+obj.getLocalBidders().size());
		System.out.println("REST returning remote size"+obj.getRemoteBidders().size());
		
		//RemoteSellerDetails testObj = new RemoteSellerDetails("dummySellerID1","dummySellerID2","dummySellerID3","dummySellerID4","dummySellerID5" );
        //SellerDetails testObj=new SellerDetails();
		//testObj.printDetails();
        //testObj.setDisplayName("abc");
		return Response.status(200).entity(obj).build();
	}
}
