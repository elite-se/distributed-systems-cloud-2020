package se.elite.dsc.kafka.taxi.trip

import org.apache.kafka.clients.consumer.ConsumerConfig
import se.elite.dsc.kafka.JsonDeserializer
import se.elite.dsc.kafka.JsonObjectSerde
import se.elite.dsc.kafka.KafkaConfig
import se.elite.dsc.kafka.taxi.Cell
import se.elite.dsc.kafka.taxi.Trip
import java.util.*

class TripConsumerConfig(val groupId: String, val sourcetopic: String) : KafkaConfig() {

    override fun createProperties(): Properties {
        val props = super.createProperties()
        props[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"

        class CellDeser : JsonDeserializer<Cell>(Cell::class.java)
        class TripDeser : JsonDeserializer<Trip>(Trip::class.java)

        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = CellDeser::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = TripDeser::class.java
        return props
    }
}