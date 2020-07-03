package se.elite.dsc.aggregationvisualisation

import jsonMapper
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import kotlinx.html.*
import kotlinx.html.dom.*
import org.slf4j.LoggerFactory
import org.w3c.dom.Document
import se.elite.dsc.mongo.taxi.AggrProfit
import se.elite.dsc.mongo.taxi.Profit
import se.elite.dsc.mongo.taxi.ProfitRepository
import java.io.IOException
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
        val profits: List<AggrProfit>
)

class AggregatedVisualisationsServer(private val port: Int) {
    private val log = LoggerFactory.getLogger(AggregatedVisualisationsServer::class.java)
    private var server: HttpServer = HttpServer.create(InetSocketAddress(port), 0);
    private val podName: String = System.getenv("POD_NAME") ?: "defaultPod"

    init {
        server.createContext("/") { exchange ->
            exchange.run {
                log.info("Handling request for {}", requestURI)

                if (requestURI.toString().endsWith(".ico")) {
                    sendResponse(404, "404 (Favicon not present)\n")
                    return@run
                }

                val profitsOneMonth = ProfitRepository.retrieveCellInfoForOneMonth(31)
                val outputHTML = getHTML(ViewModel(profitsOneMonth), "Aggregated Cells January")

                sendResponse(200, outputHTML.serialize())
            }
        }

        server.createContext("/january") { exchange ->
            exchange.run {
                log.info("Handling request for suburi: {}", requestURI)

                val profitsOneMonth = ProfitRepository.retrieveTopTenCells(31)
                val outputHTML = getHTML(ViewModel(profitsOneMonth), "Top 10 cells January")

                sendResponse(200, outputHTML.serialize())
            }
        }

        server.createContext("/february") { exchange ->
            exchange.run {
                log.info("Handling request for suburi: {}", requestURI)

                val profitsOneMonth = ProfitRepository.retrieveTopTenCells(62)
                val outputHTML = getHTML(ViewModel(profitsOneMonth), "Top 10 cells February")

                sendResponse(200, outputHTML.serialize())
            }
        }

        server.createContext("/year") { exchange ->
            exchange.run {
                log.info("Handling request for suburi: {}", requestURI)

                val profitsOneMonth = ProfitRepository.retrieveTopTenCellsYear()
                val outputHTML = getHTML(ViewModel(profitsOneMonth), "Top 10 cells 2013")

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

    private fun getHTML(viewModel: ViewModel, title: String): Document {
        return createHTMLDocument().html {
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
                                                <h1 class="heading-custom">Taxi Data ${title} (Server: ${podName})</h1>
                                                <ul style="display: flex; list-style: none;">
                                                    <li><a href="/january">Top 10 January</a></li>
                                                    <li style="margin-left: 12px"><a href="/february">Top 10 February</a></li>
                                                    <li style="margin-left: 12px"><a href="/year">Top 10 Whole Year</a></li>
                                                </ul>
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
                                +"Aggregated Cells"
                            }
                            table {
                                classes = setOf("table")
                                role = "grid"
                                thead {
                                    tr {
                                        role = "row"
                                        th {
                                            role = "columnheader"
                                            +"Cell"
                                        }
                                        th {
                                            role = "columnheader"
                                            +"# Trips"
                                        }
                                        th {
                                            role = "columnheader"
                                            +"Fare Sum"
                                        }
                                        th {
                                            role = "columnheader"
                                            +"Tip Sum"
                                        }
                                    }
                                }
                                tbody {
                                    role = "rowgroup"
                                    for (trip in viewModel.profits) {
                                        tr {
                                            role = "row"
                                            td {
                                                role = "cell"
                                                +("(" + trip.cell.clat + ", " + trip.cell.clong + ")")
                                            }
                                            td {
                                                role = "cell"
                                                +("" + trip.tripCount)
                                            }
                                            td {
                                                role = "cell"
                                                +moneyString(trip.fareSum)
                                            }
                                            td {
                                                role = "cell"
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
    }

    private var listening: Boolean = false

    private fun moneyString(amount: Double): String {
        return if (amount > 0) (amount.format(2) + "$") else "--"
    }


    fun listen() {
        if (!listening) {
            log.info("$podName is listening at http://localhost:$port/")
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
