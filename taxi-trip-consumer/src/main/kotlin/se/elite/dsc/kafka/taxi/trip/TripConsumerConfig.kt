package se.elite.dsc.kafka.taxi.trip

import org.apache.kafka.clients.consumer.ConsumerConfig
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
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = JsonObjectSerde(Cell::class.java)
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonObjectSerde(Trip::class.java)

        return props
    }
}