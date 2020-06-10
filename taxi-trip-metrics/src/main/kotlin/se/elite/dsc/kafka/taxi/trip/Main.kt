package se.elite.dsc.kafka.taxi.trip

import java.util.concurrent.CountDownLatch
import kotlin.system.exitProcess

fun main() {
    val config = TripMetricsConfig("trip-converter-service", "taxi-trip", "cell-profit")
    val metrics = TripMetrics(config)
    val latch = CountDownLatch(1)

    // attach shutdown handler to catch control-c
    Runtime.getRuntime().addShutdownHook(object : Thread("trip-metrics-shutdown-hook") {
        override fun run() {
            metrics.close()
            latch.countDown()
        }
    })
    try {
        metrics.start()
        latch.await()
    } catch (e: Throwable) {
        exitProcess(1)
    }
    exitProcess(0)
}
