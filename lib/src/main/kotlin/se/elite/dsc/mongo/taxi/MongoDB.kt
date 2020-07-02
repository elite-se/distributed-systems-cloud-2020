package se.elite.dsc.mongo.taxi

import com.mongodb.ReadConcern
import com.mongodb.ReadPreference
import com.mongodb.TransactionOptions
import com.mongodb.WriteConcern
import com.mongodb.client.ClientSession
import com.mongodb.client.TransactionBody
import org.litote.kmongo.KMongo

object MongoDB {
    private val connection_uri = System.getenv("MONGODB_ATLAS_CONNECTION_STRING")
    val client = KMongo.createClient(connection_uri)

    fun <T> withTransaction(
            options: TransactionOptions
            = TransactionOptions.builder()
                    .readPreference(ReadPreference.primary())
                    .readConcern(ReadConcern.LOCAL)
                    .writeConcern(WriteConcern.MAJORITY)
                    .build(),
            exec: (session: ClientSession) -> T
    ) {
        client.startSession().use { session ->
            val txnBody = TransactionBody { exec(session) }
            session.withTransaction(txnBody, options)
        }
    }
}
