### Demo Links
- p1: https://www.youtube.com/watch?v=IEztD_Pa1SI
- p2: https://youtu.be/YycSJR589V8
- p3: https://youtu.be/2BsrhG8gSts
- p4: https://youtu.be/VST8KezGy4I
- p5: https://youtu.be/MwGkIWP9q8w



Throughput numbers:
1 Control Plane + 3 Worker nodes + 1 master MySQL pod + 1 slave MySQL pod + 2 Fabflix pods: 917.204/minute

1 Control Plane + 4 Worker nodes + 1 master MySQL pod + 1 slave MySQL pod + 3 Fabflix pods : 1,002.194/minute
Explanation and other data:
I found these numbers to be very odd, especially if i just changed the amount of worker nodes/pods there would be
high performance increase initially that would get bogged down overtime. I took multiple reads.
1 Control Plane + 3 Worker nodes + 1 master MySQL pod + 1 slave MySQL pod + 2 Fabflix pods
917.204/minute
Upon start up:
5,931.36/minute
Overtime dropping to:
1,790.63/minute
1 Control Plane + 4 Worker nodes + 1 master MySQL pod + 1 slave MySQL pod + 3 Fabflix pods
1,303.08/minute
1,002.194/minute

After adding another worker node and a fabflix pod (not on a fresh startup) it was around 1000.


#### Hunter:
- AWS setup
- single movie and movie list page with servlets
- login filtering
- search
- browse by genre
- encryption
- https
- employee log in
- recaptcha
- jdbc pooling
- master and slave
- load balancing
- dockerized
- kubernetes deployment
- jmeterad
- MySQL DB setup
- hyperlinking star/movie single pages + jump to movie list
- browse by genre
- caching
- shopping cart + checkout
- prepare statements
- xml parser
- employee dashboard
- autocomplete and full text search
- dockerized
- kubernetes deployment
- j meter


- # Connection Pooling
    - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
      WebContent/META-INF/context.xml
    - define the connection pool for the master and slave using internal aws ip address
    - WebContent/WEB-INF/web.xml declaring master and slave for lookup
    - creating a servlet for getting the master and slave connections
    - i then use my databaseutil java file to get these connections in each servlet depending on read of write.
    - #### Explain how Connection Pooling is utilized in the Fabflix code.
  Connection pooling is used throughout the code via DatabaseUtil whenever fabflix needs to do a read or write it uses this
- util to decide whether to use the master or the slave connection. This seperates our load.
- it calls either DatabaseUtil.getMasterConnection() for writes or DatabaseUtil.getSlaveConnection() for reads. 
- These methods retrieve connections from the pools defined in context.xml
    - #### Explain how Connection Pooling works with two backend SQL.
When there are two backend sql, each sql server/database has its own connection pool.
One is for the read only handled by the slave and the master does both reading and writing.
The slave has a read only mysql credentials.
Every operation uses one or the other depending on read and write, however if read fails we can fallback on the master for reading

- # Master/Slave
    - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.
WebContent/META-INF/context.xml
WebContent/WEB-INF/web.xml
    - #### How read/write requests were routed to Master/Slave SQL?
Based on the request it is explicitly routed to the master for writes and reads and the slave for read only falling back to the master if it failed.
Each servlet depending on its functionality uses the database utility class to decide which database to use, the slave or the master.
For write request we used our database utility file in each servlet to get the master connection. 
for read request we used the same database utility file in those servlets to get the slave read only connection.
Explain in the README.md how to use Connection Pooling using two backend servers. Mention your configuration file name and path in your README.md.
WebContent/META-INF/context.xml, WebContent/WEB-INF/web.xml,
When there are two backend sql, each sql server/database has its own connection pool.
One is for the read only handled by the slave and the master does both reading and writing.


project 3
#### Inconsistency Report note: in the demo you can see the report at 8:06
refer to txt files that are populated in xml-parser folder after execution
- Total duplicate movies found: 28
- Total inconsistent movies found: 3343
- Total duplicate stars: 27
- Total stars not found: 11841
- Total movies no stars: 0
- Total unknown movies found: 4426


#### Optimization Strategy
- Used SAX Parsing for more efficient memory usage
- Only looked at tags that contained info we needed
- Batch Processed the sql inserts and wrapped it as a transaction
- Omitted inconsistent and duplicate movies from being added prior to inserts
- Calculated values like unknown movies, movies without actors, unidentified stars, and duplicate stars during parsing rather than during inserts

