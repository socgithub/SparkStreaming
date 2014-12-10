SparkStreaming
==============

##Configuration

1. Download spark-1.1.0-bin-hadoop2.4 from Apache
2. Setup the master node and workers
3. Start Spark by running sbin/startall.sh
4. Please modify Properties.java to setup the file path and the frequency of Spark Streaming 

##How to run?

Before launching SparkStreaming, you can use to following command to open a writing socket for testing
```bash
nc -lk 9999
```

And then run SparkStreaming
```bash
cd SparkStreaming
mvn clean
mvn package
spark-1.1.0-bin-hadoop2.4/bin/spark-submit --class soc.SparkStreaming --master spark://{USERNAME}.local:7077 --num-executors 3 --driver-memory 4g --executor-memory 2g --executor-cores 1 ./target/SparkStreaming-1.jar localhost 9999
 ```


