package se.elite.dsc.aggregationvisualisation

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import kotlinx.html.*
import kotlinx.html.dom.*
import org.slf4j.LoggerFactory

private fun HttpExchange.sendResponse(code: Int, answer: ByteArray) = run {
    sendResponseHeaders(code, answer.size.toLong())
    responseBody.use { it.write(answer) }
}

private fun HttpExchange.sendResponse(code: Int, answer: String) = sendResponse(code, answer.toByteArray())

data class TaxiTrip(
        val taxiId: String,
        val start: String,
        val destination: String
)

data class ViewModel(
        val startDate: String,
        val endDate: String,
        val trips: List<TaxiTrip>
)

class AggregatedVisualisationsServer(private val port: Int) {
    private val log = LoggerFactory.getLogger(AggregatedVisualisationsServer::class.java)
    private var server: HttpServer = HttpServer.create(InetSocketAddress(port), 0);

    init {
        server.createContext("/") { exchange ->
            exchange.run {
                log.info("Handling request for {}", requestURI)

                if (requestURI.toString().endsWith(".ico")) {
                    sendResponse(404, "404 (Favicon not present)\n")
                    return@run
                }

                /* TODO: populate viewModel from MongoDB */

                val viewModel = ViewModel(
                        "01.01.2013",
                        "31.12.2013",
                        listOf(
                                TaxiTrip("#1234", "1st Street", "5th Avenue"),
                                TaxiTrip("#3415", "1st Street", "5th Avenue"),
                                TaxiTrip("#8261", "1st Street", "5th Avenue"),
                                TaxiTrip("#2134", "1st Street", "5th Avenue"),
                                TaxiTrip("#3018", "1st Street", "5th Avenue")
                        )
                )
                val outputHTML = createHTMLDocument().body {
                    div {
                        h1 {
                            +"Taxi Trips from ${viewModel.startDate} until ${viewModel.endDate}"
                        }
                        table {
                            thead {
                                tr {
                                    th {
                                        +"TaxiId"
                                    }
                                    th {
                                        +"Start"
                                    }
                                    th {
                                        +"Destination"
                                    }
                                }
                            }
                            tbody {
                                for (trip in viewModel.trips) {
                                    tr {
                                        td {
                                            +trip.taxiId
                                        }
                                        td {
                                            +trip.start
                                        }
                                        td {
                                            +trip.destination
                                        }
                                    }
                                }
                            }
                        }

                    }
                }

                sendResponse(200, outputHTML.serialize())
            }
        }
    }

    private var listening: Boolean = false;

    fun listen() {
        if (!listening) {
            log.info("Listening at http://localhost:$port/")
            server.start()
        }
    }

    fun shutdown() {
        if (listening) {
            server.stop(0)
            listening = false
            log.info("Shutdown complete")
        }
    }
}
