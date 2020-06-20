package se.elite.dsc.mongo.taxi

import org.litote.kmongo.*

object ProfitRepository {
    private val database = MongoDB.client.getDatabase("profits")
    val collection = database.getCollection<Profit>()
}