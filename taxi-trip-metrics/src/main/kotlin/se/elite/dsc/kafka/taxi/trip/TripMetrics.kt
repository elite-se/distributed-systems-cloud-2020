package se.elite.dsc.kafka.taxi.trip

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.state.WindowStore
import org.apache.log4j.LogManager
import se.elite.dsc.kafka.JsonObjectSerde
import se.elite.dsc.kafka.taxi.Cell
import se.elite.dsc.kafka.taxi.Point
import se.elite.dsc.kafka.taxi.Trip
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.round
import kotlin.system.exitProcess


class TripMetrics(val config: TripMetricsConfig) {
    private val log = LogManager.getLogger(javaClass)
    private val stream = constructStream()

    fun start() {
        stream.start()
    }

    fun close() {
        stream.close()
    }

    fun constructStream(): KafkaStreams {
        val props = config.createProperties()
        val cellSerde = JsonObjectSerde(Cell::class.java)
        val tripSerde = JsonObjectSerde(Trip::class.java)
        val pointSerde = JsonObjectSerde(Point::class.java)

        val builder = StreamsBuilder()
        val source = builder.stream(config.sourcetopic, Consumed.with(cellSerde, tripSerde))
        val windowed = source
                .groupByKey(Grouped.with(cellSerde, tripSerde))
                .windowedBy(TimeWindows.of(Duration.ofMillis(TimeUnit.MINUTES.toMillis(15))))
                .aggregate(
                        { Point(0.0, 0.0) },
                        { _, value, profit ->
                            Point(profit.x + 1, profit.y + (value.fareAmount + value.tipAmount))
                        },
                        Materialized.`as`<Cell, Point, WindowStore<Bytes, ByteArray>>("profit-store")
                                .withValueSerde(pointSerde))
                .toStream()

        windowed.map<Cell?, Double> { window: Windowed<Cell>, point: Point -> KeyValue(window.key(), round(point.y * 100) / 100) }
                .foreach { key: Cell?, value: Double? -> log.info("key: $key, val: $value") }
                .to(config.sinkTopic, Produced.with(cellSerde, Serdes.Double()))

        return KafkaStreams(builder.build(), props)
    }
}
