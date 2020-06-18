package se.elite.dsc.kafka.taxi.trip

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import java.time.Duration

class TripConsumer(val config: TripConsumerConfig) {

    private val log = LoggerFactory.getLogger(TripConsumer::class.java)
    private val topic = config.sourcetopic
    private val consumer = KafkaConsumer<String, String>(config.createProperties())

    fun consume() {
        consumer.subscribe(listOf(topic))

        var counter = 0

        while (true) {
            val records = consumer.poll(Duration.ofSeconds(1))
            records.iterator().forEach {
                counter++
                log.info("Received message: {}", counter)
                log.info("\tvalue: {}", it.value())
            }
        }
    }

}