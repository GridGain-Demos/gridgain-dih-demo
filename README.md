# GridGain Data Integration Hub Demo

This demo emulates a fraud detection system for credit card transactions. The system works with 3 data sets that originate from 3 different sources.

## Accounts Data

Credit card account data is loaded from CSV files located in HDFS. [GridGain Spark Loader](https://www.gridgain.com/docs/latest/integrations/datalake-accelerator/load-data-spark) is used for this.

Every credit card has a country code associated with it to indicate where the card was issued.

## Transactions Data

Transactions are continuously streamed from Kafka via [GridGain Kafka Connector](https://www.gridgain.com/docs/latest/integrations/kafka/cert-kafka-connect).

Every transaction record holds a country code to indicate where this transaction occurred.

## Travel Data

In addition, credit card users can use Web interface to notify the system about anticipated travels providing a list of country codes.

This data is stored only in GridGain.

## Fraud Detection

An incoming transaction is considered fraudulent if one of these conditions is true:

- The transaction is associated with a non-existent account (e.g., provided credit card number is invalid).
- The transaction occurred outside of the home country, as well as not in one of the countries specified for travel.

# Installation

## GridGain

1. Download GridGain 8.7.12 Enterprise Edition ZIP from here: https://www.gridgain.com/resources/download
2. Unzip the downloaded file to a preferred location (`$GRIDGAIN_HOME`).
3. Prepare Kafka Connector package:
```bash
cd $GRIDGAIN_HOME/integration/gridgain-kafka-connect
./copy-dependencies.sh
```

## Kafka

1. Download Kafka 2.4.1 from here: https://kafka.apache.org/downloads
2. Unzip the downloaded file to a preferred location (`$KAFKA_HOME`).
3. Open the `$KAFKA_HOME/config/connect-standalone.properties` file for editing and add the following line (replace `$GRIDGAIN_HOME` with the actual path to GridGain installation):
```properties
plugin.path=$GRIDGAIN_HOME/integration/gridgain-kafka-connect
```
4. Create `$KAFKA_HOME/config/gridgain-kafka-connect-sink.properties` file with the following content:
```properties
name=gridgain-kafka-connect-sink
topics=ignite.TRANSACTIONS
topicPrefix=ignite.
connector.class=org.gridgain.kafka.sink.IgniteSinkConnector
igniteCfg=META-INF/ignite-client-config.xml
shallProcessUpdates=true
key.converter=org.apache.kafka.connect.json.JsonConverter
value.converter=org.apache.kafka.connect.json.JsonConverter
```
5. Start ZooKeeper server:
```bash
$KAFKA_HOME/bin/zookeeper-server-start.sh $KAFKA_HOME/config/zookeeper.properties
```
6. Start Kafka server:
```bash
$KAFKA_HOME/bin/kafka-server-start.sh $KAFKA_HOME/config/server.properties
```
7. Create Kafka topic:
```bash
$KAFKA_HOME/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic ignite.TRANSACTIONS
```

## This Project

1. Build the project (replace `$GRIDGAIN_HOME` with the actual path to GridGain installation):
```bash
mvn clean package -Dgridgain.home=$GRIDGAIN_HOME
```
2. Open the project in your favorite IDE.

# Running the Demo

1. Start GridGain Kafka connector:
```bash
$KAFKA_HOME/bin/connect-standalone.sh $KAFKA_HOME/config/connect-standalone.properties $KAFKA_HOME/config/gridgain-kafka-connect-sink.properties
```
2. Run `IgniteServer` class in your IDE to start an Ignite server node.
3. Run `AccountsLoader` class in your IDE to load accounts data from HDFS.
4. Run `TransactionsProducer` class in your IDE to start streaming transactions from Kafka.
5. Run `AccountsWebApp` class in your IDE. You can now edit travel countries for all the accounts via the Web interface: http://localhost:4567/accounts
6. Run `FraudChecker` class in your IDE. It will connect to the Ignite cluster and periodically execute SQL queries to check if there are any fraudulent transactions. In case there are any, they will be printed out. Here are the queries that are used for this:
```SQL
-- Transactions associated with a non-existent account
SELECT id, ccNumber, amount
FROM Transaction
WHERE status = 'NO_ACCOUNT'

-- Transactions occurred in an unexpected country
SELECT a.ccNumber, CONCAT(a.firstName, ' ', a.lastName) as name, a.issueCountry, t.country, t.status
FROM Account a, Transaction t
WHERE a.ccNumber = t.ccNumber
AND t.status = 'WRONG_COUNTRY'
```
