package distributedsystems.hellokafka

import com.github.javafaker.Faker
import distributedsystems.hellokafka.data.Person
import distributedsystems.hellokafka.data.PersonSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.log4j.LogManager
import java.util.*

/*
 * for alternatives to this implementation (+static typing/schema etc), see
 * https://github.com/aseigneurin/kafka-tutorial-simple-client/
 */
class SimpleProducer(brokers: String) {
    private val logger = LogManager.getLogger(javaClass)
    private val producer = createProducer(brokers)

    private fun createProducer(brokers: String): Producer<String, Person> {
        val props = Properties()
        props["bootstrap.servers"] = brokers
        props["key.serializer"] = StringSerializer::class.java
        props["value.serializer"] = PersonSerializer::class.java
        return KafkaProducer<String, Person>(props)
    }

    fun produce(ratePerSecond: Int) {
        val waitTimeMs = 1000L / ratePerSecond;
        logger.info("Producing max $ratePerSecond records per second (one every ${waitTimeMs}ms)")

        val faker = Faker()
        while (true) {
            val fakePerson = Person(
                firstName = faker.name().firstName(),
                lastName = faker.name().lastName(),
                birthDate = faker.date().birthday()
            )
            logger.info("Generated fake person: $fakePerson")

            val futureResult = producer.send(ProducerRecord(Topic.persons.id, fakePerson))
            logger.debug("Sent fake person record")

            Thread.sleep(waitTimeMs)

            // wait for write acknowledgement
            futureResult.get()
        }
    }
}