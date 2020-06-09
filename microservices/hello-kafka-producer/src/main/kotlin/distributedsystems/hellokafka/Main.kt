package distributedsystems.hellokafka

import org.apache.log4j.BasicConfigurator

// $ kafka-topics --zookeeper localhost:2181 --create --topic persons --replication-factor 1 --partitions 4

fun main() {
    // Configure logging
    BasicConfigurator.configure()

    // Start producing values
    val producer = SimpleProducer("localhost:9092")
    producer.produce(2)
}