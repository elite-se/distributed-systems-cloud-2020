package se.elite.dsc.kafka.taxi.metrics

import com.google.common.io.Resources
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.state.WindowStore
import se.elite.dsc.kafka.JsonObjectSerde
import se.elite.dsc.kafka.taxi.Cell
import se.elite.dsc.kafka.taxi.Point
import se.elite.dsc.kafka.taxi.Trip
import java.util.concurrent.TimeUnit

fun main() {
    val config = TripMetricsConfig(sourcetopic = "", sinkTopic = "")
    TripMetricsApp().run(config)
}
