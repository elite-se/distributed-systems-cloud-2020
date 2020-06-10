package se.elite.dsc.kafka

import org.apache.kafka.clients.producer.ProducerConfig
import java.util.*

class KafkaConfig {

    private val bootstrap_server = "localhost:9092"
    private val topic: String

    constructor(topic: String) {
        this.topic = topic
    }

    fun getTopic(): String {
        return this.topic
    }

    fun createProducerProperties(): Properties {
        val props = Properties()
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrap_server)
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
        return props
    }
}