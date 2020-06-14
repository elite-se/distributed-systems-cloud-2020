package se.elite.dsc.kafka.taxi.trip

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsConfig
import se.elite.dsc.kafka.KafkaConfig
import java.util.*

class TripConverterConfig(val groupId: String, val sourcetopic: String, val sinkTopic: String) : KafkaConfig() {

    override fun createProperties(): Properties {
        val props = super.createProperties()
        props[StreamsConfig.APPLICATION_ID_CONFIG] = groupId;
        props[StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String()::class.java;
        props[StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.String()::class.java;
        props[StreamsConfig.PROCESSING_GUARANTEE_CONFIG] = StreamsConfig.EXACTLY_ONCE;

        return props
    }
}