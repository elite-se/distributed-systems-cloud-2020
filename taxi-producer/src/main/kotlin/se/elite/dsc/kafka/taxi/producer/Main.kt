package se.elite.dsc.kafka.taxi.producer

import com.google.common.io.Resources

fun main(args: Array<String>) {
    val config = TaxiProducerConfig("taxi-source")
    val datafile = Resources.getResource("taxi_data_2019_01.csv").path
    TaxiProducer(config, datafile).produce(100)
}
