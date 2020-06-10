package se.elite.dsc.kafka.taxi.producer

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsConfig
import se.elite.dsc.kafka.KafkaConfig
import java.util.*

class TripConverterConfig(val groupId: String, val sourcetopic: String, val sinkTopic: String) : KafkaConfig() {

    override fun createProperties(): Properties {
        val props = super.createProperties()
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String()::class.java);
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String()::class.java);
        return props
    }
}