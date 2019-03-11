# Analytics project that consumes events from kafka and stores in OLAP family Druid DB for analytics. Superset is visualization tool.

An example project that builds analytics with Kafka messages
###### 1. Druid(Steps to be done using Console 1)
Currently Druid has poor support of Docker images with the latest version. I have build tar from the Druid source code. Modified few settings to make it easy to run Druid on the local RAM and few change in runtime configs.

Download from: https://drive.google.com/open?id=1nvkvs5l6ernV6MLcTSSGHJnDN3fIv7ZX 
1. Unzip apache-druid
2. cd apache-druid
3. run `bin/supervise -c dev/running/conf/demo-cluster.conf`
Once you run, console looks like this.
![](screenshots/1.png)
4. open Druid Console	http://localhost:8081/
5. Druid Indexing	http://localhost:8081/console.html
![](screenshots/2.png)
![](screenshots/3.png)

###### Producing events from  Kafka(Steps to be done using Console 2):

###### 2. Create Network
 `docker network create jemstep`
###### 3. Zookper
```
 docker run -d \
    --net=jemstep \
    --name=zookeeper \
    -p 2190:2181 \
    -e ZOOKEEPER_CLIENT_PORT=2190 \
    confluentinc/cp-zookeeper:4.1.0
```
###### 4. Kafka
```
docker run -d \
    --net=jemstep \
    --name=kafka \
    -p 9092:9092 \
    -p 9999:9999 \
    -p 29092:29092 \
    -e KAFKA_ZOOKEEPER_CONNECT=zookeeper:2190 \
    -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT \
    -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:29092 \
    -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
    -e KAFKA_JMX_HOSTNAME=kafka \
    -e KAFKA_JMX_PORT=9999 \
    confluentinc/cp-kafka:4.1.0
```  
![](screenshots/4.png)



###### 5. Run App(steps to be done using console 3)

1. `git clone https://github.com/jemstep/analytics.git`
2. `cd analytics`
2. `sbt test`
3. `sbt "runMain com.jemstep.producer.PlainSinkProducerMain"`

![](screenshots/5.png)

###### 6. Go to console 1 and 

1. run `./supervisor.sh` which registers kafka-indexing service with the coordinator.
![](screenshots/6.png)

###### 7. Go to console 3 and 

1. run `sbt "runMain com.jemstep.producer.PlainSinkProducerMain"` to genarate more messages
2. run `sbt "runMain com.jemstep.producer.PlainSinkProducerMain"` to genarate more messages

###### 7. Below things can be seen in Druid console and Druid Indexing.

![](screenshots/8.png)
![](screenshots/9.png)
![](screenshots/10.png)
![](screenshots/11.png)
![](screenshots/12.png)
![](screenshots/13.png)
![](screenshots/14.png)
![](screenshots/15.png)
![](screenshots/18.png)










