package se.elite.dsc.aggregationvisualisation

import kotlin.system.exitProcess

fun main() {
    val visualisationsServer = AggregatedVisualisationsServer(8080)

    // attach shutdown handler to catch control-c
    Runtime.getRuntime().addShutdownHook(object : Thread("trip-aggregated-visualisations-shutdown-hook") {
        override fun run() {
            visualisationsServer.shutdown()
        }
    })
    try {
        visualisationsServer.listen()
    } catch (e: Throwable) {
        exitProcess(1)
    }
}
