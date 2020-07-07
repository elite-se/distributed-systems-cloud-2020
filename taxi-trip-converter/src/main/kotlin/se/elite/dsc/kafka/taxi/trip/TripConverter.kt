package se.elite.dsc.kafka.taxi.trip

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.Produced
import org.slf4j.LoggerFactory
import se.elite.dsc.kafka.JsonObjectSerde
import se.elite.dsc.kafka.taxi.Cell
import se.elite.dsc.kafka.taxi.Location
import se.elite.dsc.kafka.taxi.Trip
import se.elite.dsc.kafka.taxi.TripFields
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos

class TripConverter(val config: TripConverterConfig) {

    private val log = LoggerFactory.getLogger(TripConverter::class.java)
    private val stream = constructStream()
    private val fieldsMap = constructFieldsMap()

    fun start() {
        stream.start()
    }

    fun close() {
        stream.close()
    }

    fun constructStream(): KafkaStreams {
        val props: Properties = config.createProperties()
        val cellSerde = JsonObjectSerde(Cell::class.java)
        val tripSerde = JsonObjectSerde(Trip::class.java)

        val builder = StreamsBuilder()
        builder.stream(config.sourcetopic, Consumed.with(Serdes.String(), Serdes.String()))
                .filter { _: String?, value: String ->
                    try {
                        constructTripFromString(value)
                        return@filter true
                    } catch (e: Throwable) {
                        return@filter false
                    }
                }
                .map { _: String?, value: String ->
                    Thread.sleep(25)
                    val trip = constructTripFromString(value)
                    KeyValue(Cell(START_CELL_ORIGIN, CELL_LENGTH, trip.pickupLoc), trip)
                }
                .filter { cell: Cell, _: Trip? -> cell.inBounds(MAX_CLAT, MAX_CLONG) }
                .to(config.sinkTopic, Produced.with(cellSerde, tripSerde))

        return KafkaStreams(builder.build(), props)
    }

    private fun constructFieldsMap(): Map<TripFields, Int> {
        val fieldMap: MutableMap<TripFields, Int> = EnumMap(TripFields::class.java)
        for (i in TripFields.values().indices) {
            fieldMap[TripFields.values()[i]] = i
        }
        return fieldMap
    }

    private fun constructTripFromString(csv: String): Trip {
        val elements = csv.split(",").toTypedArray()
        val format = SimpleDateFormat("yyyy-MM-dd H:m:s")

        return Trip(
                format.parse(elements[fieldsMap.getValue(TripFields.PICKUP_DATETIME)]),
                format.parse(elements[fieldsMap.getValue(TripFields.DROPOFF_DATETIME)]),
                Location(elements[fieldsMap.getValue(TripFields.PICKUP_LATITUDE)].toDouble(),
                        elements[fieldsMap.getValue(TripFields.PICKUP_LONGITUDE)].toDouble()),
                Location(elements[fieldsMap.getValue(TripFields.DROPOFF_LATITUDE)].toDouble(),
                        elements[fieldsMap.getValue(TripFields.DROPOFF_LONGITUDE)].toDouble()),
                elements[fieldsMap.getValue(TripFields.FARE_AMOUNT)].toDouble(),
                elements[fieldsMap.getValue(TripFields.TIP_AMOUNT)].toDouble())
    }

    companion object {
        private const val MAX_CLAT = 45 // max latitude grid size
        private const val MAX_CLONG = 72 // max longitude grid size
        private const val EARTH_RADIUS_METRES = 6371000.0
        private const val CELL_SIZE_METRES = 500

        private val START_CELL_CENTRE = Location(40.831164, -74.192491)

        private val CELL_LAT_LENGTH = START_CELL_CENTRE.latitude + CELL_SIZE_METRES /
                EARTH_RADIUS_METRES * (180 / Math.PI) - START_CELL_CENTRE.latitude

        private val CELL_LONG_LENGTH = START_CELL_CENTRE.longitude + CELL_SIZE_METRES /
                EARTH_RADIUS_METRES * (180 / Math.PI) / cos(START_CELL_CENTRE.latitude * Math.PI / 180) - START_CELL_CENTRE.longitude

        private val CELL_LENGTH = Location(CELL_LAT_LENGTH, CELL_LONG_LENGTH)

        // Coordinates of top-left corner of cell 1.1
        private val START_CELL_ORIGIN = Location(START_CELL_CENTRE.latitude + CELL_LAT_LENGTH / 2,
                START_CELL_CENTRE.longitude - CELL_LONG_LENGTH / 2)
    }
}