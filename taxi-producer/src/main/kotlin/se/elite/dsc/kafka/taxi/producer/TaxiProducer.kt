package se.elite.dsc.kafka.taxi.producer

import se.elite.dsc.kafka.KafkaConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.log4j.LogManager
import java.io.File

class TaxiProducer(config: KafkaConfig, datafile: String) {

    private val topic = config.getTopic()
    private val datafile = datafile
    private val logger = LogManager.getLogger(javaClass)
    private val producer = KafkaProducer<String, String>(config.createProducerProperties())

    fun produce(eventsPerSecond: Int) {
        val waitTimeBetweenIterationsMs = 1000L / eventsPerSecond
        logger.info("Producing $eventsPerSecond records per second (1 every ${waitTimeBetweenIterationsMs}ms)")

        val reader = File(datafile).bufferedReader()

        while (reader.ready()) {
            val data = reader.readLine()
            val result = producer.send(ProducerRecord(topic, data))
            logger.info("Sent record: $data")

            // sleep before sending record
            Thread.sleep(waitTimeBetweenIterationsMs)

            // wait for the write acknowledgment
            result.get()
        }
    }
}