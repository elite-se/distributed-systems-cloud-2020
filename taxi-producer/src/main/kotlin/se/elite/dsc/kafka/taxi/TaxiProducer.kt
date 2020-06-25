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
            for (i in 0 until 10) {
                val startTime = System.currentTimeMillis()
                for (j in 0 until eventsPerSecond / 10 - 1) {
                    if (!reader.ready()) {
                        break
                    }
                    val data = reader.readLine()
                    producer.send(ProducerRecord(topic, data))
                }

                if (!reader.ready()) {
                    break
                }
                val data = reader.readLine()
                producer.send(ProducerRecord(topic, data)).get()

                val endTime = System.currentTimeMillis()
                if (startTime + 100 > endTime) {
                    Thread.sleep(100 - (endTime - startTime))
                }
            }
        }
        println("no events left")
        producer.close()
    }

    fun close() {
        producer.close()
    }
}