package se.elite.dsc.kafka.taxi

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.streams.processor.TimestampExtractor;

class TripTimestampExtractor: TimestampExtractor {
    override fun extract(record: ConsumerRecord<Any, Any>?, previousTimestamp: Long): Long {
        val (_, _, pickupDatetime) = record?.value() as Trip
        return pickupDatetime.time
    }
}