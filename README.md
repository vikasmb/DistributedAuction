DistributedAuction
==================

A distributed system application to cnduct auctions in real-time for determining prices of services (like 
car rental, hotel rental etc) or products.

System Architecture 
    Independent clusters for each type of service
    RESTful communication pattern between each component
    MongoDB for persistent storage of nested document data model.
    Zookeeper for load balancing of failure tolerance duties.
