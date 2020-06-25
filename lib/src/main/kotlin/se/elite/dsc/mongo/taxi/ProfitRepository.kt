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
        // Transactions requires deploying a special mongo cluster which I'm not eager to do
        // beyond localhost
//        MongoDB.withTransaction { session ->
        val filter = and(Profit::dayOfYear eq tripDayOfYear, Profit::cell eq cell)
        val mongoResult = collection.findOne(
//                    session,
                filter
        )
        val profitData = mongoResult ?: Profit(tripDayOfYear, cell, 0, 0.0, 0.0)

        profitData.tripCount += 1
        profitData.fareSum += fareAmount
        profitData.tipSum += tipAmount

        if (mongoResult != null) {
            println("Cell ${profitData.cell} already has a value on day ${profitData.dayOfYear} => Updating")
            collection.updateOne(filter, profitData, UpdateOptions())
        } else {
            println("Inserting new value for cell ${profitData.cell} and day ${profitData.dayOfYear}")
            collection.insertOne(profitData)
        }
//        }
    }
}