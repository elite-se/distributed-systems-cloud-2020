package se.elite.dsc.aggregationvisualisation

import jsonMapper
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import kotlinx.html.*
import kotlinx.html.dom.*
import org.slf4j.LoggerFactory
import se.elite.dsc.kafka.taxi.Cell
import se.elite.dsc.mongo.taxi.Profit
import se.elite.dsc.mongo.taxi.ProfitRepository
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun Double.format(digits: Int) = "%.${digits}f".format(this)

private fun HttpExchange.sendResponse(code: Int, answer: ByteArray) = run {
    sendResponseHeaders(code, answer.size.toLong())
    responseBody.use { it.write(answer) }
}

private fun HttpExchange.sendResponse(code: Int, answer: String) = sendResponse(code, answer.toByteArray())

private fun HttpExchange.sendStaticFile(path: String) {
    try {
        sendResponse(200, AggregatedVisualisationsServer::class.java.getResourceAsStream(path).readAllBytes())
    } catch (e: IOException) {
        println("Exception loading Static File " + path)
    }
}

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

                val viewModel = ViewModel(profitsOneMonth)
                print(jsonMapper.writeValueAsString(viewModel.profits))

                val outputHTML = createHTMLDocument().html {
                    head {
                        link {
                            rel = "stylesheet"
                            href = "dist/css/patternfly_edited.css"
                        }
                        link {
                            rel = "stylesheet"
                            href = "dist/css/patternfly-additions_edited.min.css"
                        }

                        style {
                            unsafe {
                                raw("""
                                    .side-bar-custom {
                                        height: 100vh;
                                    }
                                    
                                    .heading-custom {
                                        padding-left: 40px;
                                    }
                                """.trimIndent())
                            }
                        }

                        unsafe {
                            raw("""
                                <!-- Leaflet -->
                                    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.4.0/dist/leaflet.css"
                                          integrity="sha512-puBpdR0798OZvTTbP4A8Ix/l+A4dHDD0DGqYW6RQ+9jxkRFclaxxQb/SJAWZfWAkuyeQUytO7+7N4QKrDh+drA=="
                                          crossorigin=""/>
                                    <script src="https://unpkg.com/leaflet@1.4.0/dist/leaflet.js"
                                            integrity="sha512-QVftwZFqvtRNi0ZyCtsznlKSWOStnDORoefr1enyq5mVL4tmKB3S/EnC3rRJcxCPavG10IcrVGSmPh6Qw5lwrg=="
                                            crossorigin=""></script>

                                    <!-- jQuery CDN -->
                                    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>

                                    <!-- Bootstrap JS CDN -->
                                    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js"></script>

                                    <!-- Patternfly JS -->
                                    <script src="https://cdnjs.cloudflare.com/ajax/libs/patternfly/3.59.1/js/patternfly.min.js"></script>
                                    
                                    <script src="/dist/js/map.js"></script>

                                    <style media="screen">
                                        .log {
                                            background: #f1f1f1;
                                            height: calc(100vh - 280px);
                                            overflow-y: scroll;
                                            padding: 20px;
                                            margin-top: 20px;
                                        }

                                        .item {
                                            margin: 1rem;
                                            padding: 1rem;
                                            border: 1px solid #f1f1f1;
                                            flex: 0 0 350px;
                                        }

                                        #status {
                                            font-weight: bold;
                                        }

                                        #map {
                                            height: calc(100% - 150px);
                                        }
                                    </style>
                            """)
                        }
                    }

                    body {
                        div {
                            classes = setOf("containter-fluid")

                            div {
                                classes = setOf("row")

                                unsafe {
                                    raw("""
                                                <div class="col-sm-8 col-md-9">
                                                    <div class="page-header page-header-bleed-right">
                                                        <h1 class="heading-custom">Taxi Data Dashboard</h1>
                                                    </div>
                                                    <div>
                                                        <p id="status"></p>
                                                    </div>
                                                    <div id="map"></div>
                                                </div>
                                            """)
                                }

                                div {
                                    classes = setOf("col-sm-4", "col-md-3", "sidebar-pf", "sidebar-pf-right", "side-bar-custom")
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


                        script {
                            unsafe {
                                raw("""
                                    document.cellData = ${jsonMapper.writeValueAsString(viewModel.profits)};
                                """.trimIndent())
                            }
                        }


                    }
                }

                sendResponse(200, outputHTML.serialize())
            }
        }

        server.createContext("/dist/css/patternfly_edited.css") {
            it.sendStaticFile("/patternfly_edited.css")
        }

        server.createContext("/dist/css/patternfly-additions_edited.min.css") {
            it.sendStaticFile("/patternfly-additions_edited.min.css")
        }

        server.createContext("/dist/img/brand.svg") {
            it.sendStaticFile("/brand.svg")
        }

        server.createContext("/dist/js/map.js") {
            it.sendStaticFile("/map.js")
        }
    }

    private var listening: Boolean = false

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
