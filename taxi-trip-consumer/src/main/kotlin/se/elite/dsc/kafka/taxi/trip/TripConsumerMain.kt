package se.elite.dsc.kafka.taxi.trip

fun main() {
    val config = TripConsumerConfig("trip-consumer-service", "taxi-trip")
    TripConsumer(config).consume()
}
