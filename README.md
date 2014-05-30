DistributedAuction
==================

A distributed system application to cnduct auctions in real-time for determining prices of services (like 
car rental, hotel rental etc) or products.

System Architecture 
    Independent clusters for each type of service<br/>
    RESTful communication pattern between each component<br/>
    MongoDB for persistent storage of nested document data model.<br/>
    Zookeeper for load balancing of failure tolerance duties.<br/>
