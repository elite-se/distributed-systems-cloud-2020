package se.elite.dsc.kafka.taxi.trip

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import se.elite.dsc.kafka.taxi.Cell
import se.elite.dsc.kafka.taxi.Trip
import se.elite.dsc.mongo.taxi.ProfitRepository
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class TripConsumer(val config: TripConsumerConfig) {

    private val log = LoggerFactory.getLogger(TripConsumer::class.java)
    private val topic = config.sourcetopic
    private val consumer = KafkaConsumer<Cell, Trip>(config.createProperties())

    fun consume() {
        consumer.subscribe(listOf(topic))

        var counter = 0

        while (true) {
            val records = consumer.poll(Duration.ofSeconds(1))
            records.iterator().forEach {
                val cell = it.key()
                val trip = it.value()
                val tripDayOfYear = convertDate(trip.pickupDatetime).dayOfYear

                ProfitRepository.addTrip(
                        tripDayOfYear,
                        cell,
                        trip.fareAmount,
                        trip.tipAmount
                )

                counter++
                log.info("Received message: {}", counter)
                log.info("\tvalue: {}", it.value())
            }
        }
    }

    fun convertDate(date: Date): LocalDate {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
    }
}