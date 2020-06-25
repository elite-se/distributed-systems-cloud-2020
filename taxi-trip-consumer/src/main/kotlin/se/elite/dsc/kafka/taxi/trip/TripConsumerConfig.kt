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
        props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "false"
        props[ConsumerConfig.ISOLATION_LEVEL_CONFIG] = "read_committed"


        class CellDeser: JsonDeserializer<Cell>(Cell::class.java)
        class TripDeser: JsonDeserializer<Trip>(Trip::class.java)

        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = CellDeser::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = TripDeser::class.java
        return props
    }
}