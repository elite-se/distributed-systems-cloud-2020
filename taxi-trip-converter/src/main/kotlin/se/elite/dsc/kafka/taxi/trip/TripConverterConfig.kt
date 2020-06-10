package se.elite.dsc.kafka.taxi.trip

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsConfig
import se.elite.dsc.kafka.KafkaConfig
import java.util.*

class TripConverterConfig(val groupId: String, val sourcetopic: String, val sinkTopic: String) : KafkaConfig() {

    override fun createProperties(): Properties {
        val props = super.createProperties()
        props[StreamsConfig.APPLICATION_ID_CONFIG] = groupId;
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest";
        props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "false";
        props[StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG] = 0;
        props[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String()::class.java;
        props[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.String()::class.java;
        return props
    }
}