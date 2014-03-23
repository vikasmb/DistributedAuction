package org.ds.auction;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class BidDetails {

	Boolean madeBid;
	Double bid;
	
	public void setMadeBid(Boolean madeBid) {
		this.madeBid = madeBid;
	}

	public void setBid(Double bid) {
		this.bid = bid;
	}

	public Boolean getMadeBid() {
		return this.madeBid;
	}
	
	public Double getBid() {
		return this.bid;
	}
	
	public BidDetails(){
	}
}
