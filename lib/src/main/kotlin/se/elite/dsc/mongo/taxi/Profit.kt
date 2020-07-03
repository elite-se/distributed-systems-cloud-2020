package se.elite.dsc.mongo.taxi

import org.bson.codecs.pojo.annotations.BsonId
import se.elite.dsc.kafka.taxi.Cell

data class Profit(
        val dayOfYear: Int,
        val cell: Cell,
        var tripCount: Int,
        var fareSum: Double,
        var tipSum: Double
)

data class AggrProfit(
        @BsonId val cell: Cell,
        var tripCount: Int,
        var fareSum: Double,
        var tipSum: Double
)
