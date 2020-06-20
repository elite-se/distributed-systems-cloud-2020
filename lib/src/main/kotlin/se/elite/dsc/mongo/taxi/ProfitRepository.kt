package se.elite.dsc.mongo.taxi

import org.litote.kmongo.*
import se.elite.dsc.kafka.taxi.Cell

object ProfitRepository {
    private val database = MongoDB.client.getDatabase("profits")
    val collection = database.getCollection<Profit>()

    fun initialize() {
        collection.createIndex("{ \"dayOfYear\": 1, \"cell\": 1 }")
    }

    fun addTrip(tripDayOfYear: Int, cell: Cell, fareAmount: Double, tipAmount: Double) {
        MongoDB.withTransaction { session ->
            val profitData = collection.findOne(
                    session,
                    and(Profit::dayOfYear eq tripDayOfYear, Profit::cell eq cell)
            ) ?: Profit(tripDayOfYear, cell, 0, 0.0, 0.0)

            profitData.tripCount += 1
            profitData.fareSum += fareAmount
            profitData.tipSum += tipAmount

            collection.save(profitData)
        }
    }
}