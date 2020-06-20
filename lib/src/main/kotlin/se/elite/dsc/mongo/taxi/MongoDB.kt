package se.elite.dsc.mongo.taxi

import org.litote.kmongo.KMongo

object MongoDB {
    val client = KMongo.createClient("mongodb://localhost:4040")
}
