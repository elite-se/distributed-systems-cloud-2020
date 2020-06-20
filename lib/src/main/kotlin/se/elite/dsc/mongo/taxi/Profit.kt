package se.elite.dsc.mongo.taxi

import se.elite.dsc.kafka.taxi.Cell
import java.util.*

data class Profit(
    val days: Map<Date, Map<Cell, CellProfit>>
)