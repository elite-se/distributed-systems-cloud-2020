package se.elite.dsc.kafka.taxi

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.io.File

class TaxiProducer(val config: TaxiProducerConfig, val datafile: String, var eventsPerSecond: Int) {

    private val topic = config.topic
    private val producer = KafkaProducer<String, String>(config.createProperties())

    fun produce() {
        val reader = File(datafile).bufferedReader()

        while (reader.ready()) {
            val startTime = System.currentTimeMillis()
            for (x in 0 until eventsPerSecond) {
                if (!reader.ready()) {
                    break
                }
                val data = reader.readLine()
                producer.send(ProducerRecord(topic, data))
            }

            val endTime = System.currentTimeMillis()
            if (startTime + 1000 > endTime) {
                Thread.sleep(1000 - (endTime - startTime))
            }
        }
        println("no events left")
        producer.close()
    }

    fun close() {
        producer.close()
    }
}