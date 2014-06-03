DistributedAuction
==================

A distributed system application to conduct auctions in real-time for determining prices of services (like 
car rental, hotel rental etc) or products.

System Architecture: <br/>
    1.Independent clusters for each type of service<br/>
    2.RESTful communication pattern between each component<br/>
    3.MongoDB for persistent storage of nested document data model.<br/>
    4.Zookeeper for load balancing of failure tolerance duties.<br/>
    5.Web interface for both buyer and seller functionality using Primefaces/JSF 2.0 

Awarded the first prize in Demo Session for best entrepreneurial project in advanced distributed systems course (CS 525 Spring 2014 UIUC CS)

Reference : http://courses.engr.illinois.edu/cs525/sp2014/ (Section titled 'Demo Session Award Winners')
