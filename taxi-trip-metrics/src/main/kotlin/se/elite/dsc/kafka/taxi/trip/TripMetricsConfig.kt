package se.elite.dsc.kafka.taxi.trip

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.StreamsConfig
import se.elite.dsc.kafka.KafkaConfig
import se.elite.dsc.kafka.taxi.TripTimestampExtractor
import java.util.*

class TripMetricsConfig(val groupId: String, val sourcetopic: String, val sinkTopic: String) : KafkaConfig() {
    override fun createProperties(): Properties {
        val props = super.createProperties()
        props[StreamsConfig.APPLICATION_ID_CONFIG] = groupId
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = "false"
        props[StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG] = 1024 * 1024L // 1 MiByte
        props[StreamsConfig.COMMIT_INTERVAL_MS_CONFIG] = 1000 // 10 sec
        props[StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG] = TripTimestampExtractor::class.java

        return props
    }
}