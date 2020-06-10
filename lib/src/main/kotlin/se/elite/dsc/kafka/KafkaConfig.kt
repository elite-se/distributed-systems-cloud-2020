package se.elite.dsc.kafka

import org.apache.kafka.clients.producer.ProducerConfig
import java.util.*

open class KafkaConfig {

    private val bootstrap_server = "localhost:9092"

    open fun createProperties(): Properties {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = this.bootstrap_server
        return props
    }
}