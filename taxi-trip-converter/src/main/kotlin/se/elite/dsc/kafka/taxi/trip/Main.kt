package se.elite.dsc.kafka.taxi.trip

import java.util.concurrent.CountDownLatch
import kotlin.system.exitProcess

fun main() {
    val config = TripConverterConfig("trip-converter-service", "taxi-source", "taxi-trip")
    val converter = TripConverter(config)
    val latch = CountDownLatch(1);

    // attach shutdown handler to catch control-c
    Runtime.getRuntime().addShutdownHook(object : Thread("trip-converter-shutdown-hook") {
        override fun run() {
            converter.close();
            latch.countDown();
        }
    });

    try {
        converter.start();
        latch.await();
    } catch (e: Throwable) {
        exitProcess(1);
    }

    exitProcess(0);
}
