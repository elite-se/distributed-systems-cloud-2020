package se.elite.dsc.kafka.taxi

import com.google.common.io.Resources

fun main(args: Array<String>) {
    val config = TaxiProducerConfig("taxi-source")
    val datafile = Resources.getResource("taxi_data.csv").path
    TaxiProducer(config, datafile).produce(1)
}
