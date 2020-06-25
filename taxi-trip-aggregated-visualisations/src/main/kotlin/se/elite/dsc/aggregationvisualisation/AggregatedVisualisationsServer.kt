package se.elite.dsc.aggregationvisualisation

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import kotlinx.html.*
import kotlinx.html.dom.*
import org.slf4j.LoggerFactory
import se.elite.dsc.kafka.taxi.Cell
import se.elite.dsc.mongo.taxi.Profit
import se.elite.dsc.mongo.taxi.ProfitRepository
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun Double.format(digits: Int) = "%.${digits}f".format(this)

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
        val profits: List<Profit>
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

                val profitsOneMonth = ProfitRepository.retrieveTopTenCells(31)
                println("Retrieved cell from mongo")
                print(profitsOneMonth)

                val viewModel = ViewModel(profitsOneMonth)
                val sdf = SimpleDateFormat("dd/M/yyyy")

                val outputHTML = createHTMLDocument().html {
                    head {
                        style {
                            unsafe {
                                raw("""
                                table {
                                		font-family: verdana, arial, sans-serif;
                                		font-size: 11px;
                                		color: #333333;
                                		border-width: 1px;
                                		border-color: #3A3A3A;
                                		border-collapse: collapse;
                                	}
                                 
                                	table th {
                                		border-width: 1px;
                                		padding: 8px;
                                		border-style: solid;
                                		border-color: #517994;
                                		background-color: #B2CFD8;
                                	}
                                 
                                	table tr:hover td {
                                		background-color: #DFEBF1;
                                	}
                                 
                                	table td {
                                		border-width: 1px;
                                		padding: 8px;
                                		border-style: solid;
                                		border-color: #517994;
                                		background-color: #ffffff;
                                	}
                            """.trimIndent())
                            }
                        }
                    }

                    body {
                        div {
                            h1 {
                                +"Taxi Trips"
                            }
                            table {
                                thead {
                                    tr {
                                        th {
                                            +"Date"
                                        }
                                        th {
                                            +"Cell"
                                        }
                                        th {
                                            +"# Trips"
                                        }
                                        th {
                                            +"Fare Sum"
                                        }
                                        th {
                                            +"Tip Sum"
                                        }
                                    }
                                }
                                tbody {
                                    for (trip in viewModel.profits) {
                                        tr {
                                            td {
                                                +("" + LocalDate.ofYearDay(2013, trip.dayOfYear).format(DateTimeFormatter.ofPattern("dd-MMM-yy")))
                                            }
                                            td {
                                                +("(" + trip.cell.clat + ", " + trip.cell.clong + ")")
                                            }
                                            td {
                                                +("" + trip.tripCount)
                                            }
                                            td {
                                                +moneyString(trip.fareSum)
                                            }
                                            td {
                                                +moneyString(trip.tipSum)
                                            }
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

    private fun moneyString(amount: Double): String {
        return if (amount > 0) (amount.format(2) + "$") else "--"
    }

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
