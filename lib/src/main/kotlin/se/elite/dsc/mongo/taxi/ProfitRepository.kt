package se.elite.dsc.mongo.taxi

import com.mongodb.client.model.UpdateOptions
import org.litote.kmongo.*
import se.elite.dsc.kafka.taxi.Cell

object ProfitRepository {
    private val database = MongoDB.client.getDatabase("profits")
    val collection = database.getCollection<Profit>()

    fun initialize() {
        collection.createIndex("{ \"dayOfYear\": 1, \"cell\": 1 }")
    }

    fun addTrip(tripDayOfYear: Int, cell: Cell, fareAmount: Double, tipAmount: Double) {
        val filter = and(Profit::dayOfYear eq tripDayOfYear, Profit::cell eq cell)
        val mongoResult = collection.findOne(filter)
        val profitData = mongoResult ?: Profit(tripDayOfYear, cell, 0, 0.0, 0.0)

        profitData.tripCount += 1
        profitData.fareSum += fareAmount
        profitData.tipSum += tipAmount

        collection.updateOne(filter, profitData, UpdateOptions().upsert(true))
    }

    fun retrieveCellInfoForOneMonth(tripDayOfYear: Int): List<AggrProfit> {
        val found = collection.aggregate<AggrProfit>(
                match(and(Profit::dayOfYear lt tripDayOfYear, Profit::dayOfYear gte tripDayOfYear - 31)),
                project(
                        Profit::cell from Profit::cell,
                        Profit::tripCount from Profit::tripCount,
                        Profit::fareSum from Profit::fareSum,
                        Profit::tipSum from Profit::tipSum
                ),
                group(
                        Profit::cell,
                        AggrProfit::fareSum sum Profit::fareSum,
                        AggrProfit::tipSum sum Profit::tipSum,
                        AggrProfit::tripCount sum Profit::tripCount
                ),
                sort(descending(AggrProfit::fareSum)))
        return found.toList()
    }

    fun retrieveTopTenCells(tripDayOfYear: Int): List<AggrProfit> {
        val found = collection.aggregate<AggrProfit>(
                match(and(Profit::dayOfYear lt tripDayOfYear, Profit::dayOfYear gte tripDayOfYear - 31)),
                project(
                        Profit::cell from Profit::cell,
                        Profit::tripCount from Profit::tripCount,
                        Profit::fareSum from Profit::fareSum,
                        Profit::tipSum from Profit::tipSum
                ),
                group(
                        Profit::cell,
                        AggrProfit::fareSum sum Profit::fareSum,
                        AggrProfit::tipSum sum Profit::tipSum,
                        AggrProfit::tripCount sum Profit::tripCount
                ),
                sort(descending(AggrProfit::fareSum)),
                limit(10))
        return found.toList()
    }

    fun retrieveTopTenCellsYear(): List<AggrProfit> {
        val found = collection.aggregate<AggrProfit>(
                match(Profit::dayOfYear lt 365),
                project(
                        Profit::cell from Profit::cell,
                        Profit::tripCount from Profit::tripCount,
                        Profit::fareSum from Profit::fareSum,
                        Profit::tipSum from Profit::tipSum
                ),
                group(
                        Profit::cell,
                        AggrProfit::fareSum sum Profit::fareSum,
                        AggrProfit::tipSum sum Profit::tipSum,
                        AggrProfit::tripCount sum Profit::tripCount
                ),
                sort(descending(AggrProfit::fareSum)),
                limit(10))
        return found.toList()
    }
}
