package se.elite.dsc.kafka.taxi.trip

import se.elite.dsc.mongo.taxi.ProfitRepository

fun main() {
    ProfitRepository.initialize()

    val config = TripConsumerConfig("trip-consumer-service", "taxi-trip")
    TripConsumer(config).consume()
}
