package se.elite.dsc.kafka.taxi

import com.google.common.io.Resources
import java.util.concurrent.CountDownLatch
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val config = TaxiProducerConfig("taxi-source")
    val datafile = Resources.getResource("taxi_data.csv").path
    var eventsPerSecond = 10

    val producer = TaxiProducer(config, datafile, eventsPerSecond)
    val latch = CountDownLatch(1);

    // attach shutdown handler to catch control-c
    Runtime.getRuntime().addShutdownHook(object : Thread("trip-converter-shutdown-hook") {
        override fun run() {
            producer.close();
            latch.countDown();
        }
    });

    try {
        Thread {
            producer.produce()
        }.start()
        while (latch.count != 0L) {
            println("sending $eventsPerSecond events per second")
            print("enter new rate: ")
            eventsPerSecond = readLine()!!.toInt()
            producer.eventsPerSecond = eventsPerSecond
        }
    } catch (e: Throwable) {
        exitProcess(1);
    }
    latch.await()
}
