package se.elite.dsc.mongo.taxi

import com.mongodb.client.FindIterable
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

    fun retrieveCellInfoForOneMonth(tripDayOfYear: Int): List<Profit> {
        val filter = and(Profit::dayOfYear lt tripDayOfYear)
        val found = collection.find(filter)
        return found.toList()
    }

    fun retrieveTopTenCells(tripDayOfYear: Int): List<Profit> {
        val found = collection.aggregate<Profit>(match(Profit::dayOfYear lt tripDayOfYear), sort(descending(Profit::fareSum)), limit(10))
        return found.toList()
    }
}
