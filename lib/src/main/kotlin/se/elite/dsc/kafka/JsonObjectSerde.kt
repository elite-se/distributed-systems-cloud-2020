package se.elite.dsc.kafka

import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serializer

class JsonObjectSerde<T>(tClass: Class<T>) : Serde<T> {
    private val SERIALIZER: JsonSerializer<T> = JsonSerializer()
    private val DESERIALIZER: JsonDeserializer<T> = JsonDeserializer(tClass)

    override fun configure(map: MutableMap<String, *>?, b: Boolean) {
        SERIALIZER.configure(map, b)
        DESERIALIZER.configure(map, b)
    }

    override fun close() {
        SERIALIZER.close()
        DESERIALIZER.close()
    }

    override fun serializer(): Serializer<T> {
        return SERIALIZER
    }

    override fun deserializer(): Deserializer<T> {
        return DESERIALIZER
    }

}