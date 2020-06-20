package se.elite.dsc.mongo.taxi

import se.elite.dsc.kafka.taxi.Cell

data class Profit(
    val dayOfYear: Int,
    val cell: Cell,
    var tripCount: Int,
    var fareSum: Double,
    var tipSum: Double
)