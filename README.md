# Analytics project that consumes events from kafka and stores in OLAP family Druid DB for analytics. Superset is visualization tool.

An example project that produces some example Kafka messages

## Running the example:

###### 1. Create Network
 `docker network create jemstep`
###### 2. Zookper
```
 docker run -d \
    --net=jemstep \
    --name=zookeeper \
    -p 2190:2181 \
    -e ZOOKEEPER_CLIENT_PORT=2190 \
    confluentinc/cp-zookeeper:4.1.0
```
###### 3. Kafka
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

###### 4. Run App

1. `git clone https://github.com/jemstep/analytics.git`
2. `cd analytics`
2. `sbt test`
3. `sbt "runMain com.jemstep.producer.PlainSinkProducerMain"`



