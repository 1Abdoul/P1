# P1 New Analyser

    A brief data analysis on the top 1500 data breach around the world.​
    
    Using:
    Hadoop MapReduce​

    YARN​

    HDFS​

    HIVE​

    Apache Spark​

    Scala​

    MySQL database​

Description:

Using the combination of the technologies above mentioned​

I will run a quick analysis on a CSV file containing data about the top 1500 cyber breaches.​

The CSV file in imported to the local HDFS, where tables are also created to accommodate the data file. ​

A first table will be populated from a temporary table.​

And using MapReduce, a second table will be created and partitioned for faster query.​

Finally, a user table will also be created on the local SQL database to store​

users' credentials and their encrypted password.
