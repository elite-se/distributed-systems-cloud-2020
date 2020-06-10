package se.elite.dsc.kafka.taxi.metrics

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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.math.round
import kotlin.system.exitProcess


class TripMetricsApp {
    private val logger = LogManager.getLogger(javaClass)

    fun run(config: TripMetricsConfig) {
        val props = config.createProperties()

        val cellSerde = JsonObjectSerde(Cell::class.java)
        val tripSerde = JsonObjectSerde(Trip::class.java)
        val pointSerde = JsonObjectSerde(Point::class.java)

        val builder = StreamsBuilder()
        val source = builder.stream(config.sourcetopic, Consumed.with(cellSerde, tripSerde))
        val windowed = source
                .groupByKey(Grouped.with(cellSerde, tripSerde))
                .windowedBy(TimeWindows.of(TimeUnit.MINUTES.toMillis(15)))
                .aggregate(
                        { Point(0.0, 0.0) },
                        { _, value, profit ->
                            Point(profit.x + 1, profit.y + (value.fareAmount + value.tipAmount))
                        },
                        Materialized.`as`<Cell, Point, WindowStore<Bytes, ByteArray>>("profit-store")
                                .withValueSerde(pointSerde))
                .toStream()

        windowed.foreach { key: Windowed<Cell>, value: Point? -> logger.info("key: ${key.key()}, val:$value") }
        val average: KStream<Cell?, Double> = windowed
                .map<Cell?, Double> { window: Windowed<Cell>, point: Point -> KeyValue(window.key(), round(point.y * 100) / 100) }
        average.foreach { key: Cell?, value: Double? -> logger.info("key: $key, val: $value") }
        average.to(config.sinkTopic, Produced.with(cellSerde, Serdes.Double()))
        val streams = KafkaStreams(builder.build(), props)
        val latch = CountDownLatch(1)

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(object : Thread("trip-metrics-shutdown-hook") {
            override fun run() {
                streams.close()
                latch.countDown()
            }
        })
        try {
            streams.start()
            latch.await()
        } catch (e: Throwable) {
            exitProcess(1)
        }
        exitProcess(0)
   }

}