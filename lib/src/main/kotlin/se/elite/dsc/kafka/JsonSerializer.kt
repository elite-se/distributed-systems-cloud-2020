package se.elite.dsc.kafka

import jsonMapper
import org.apache.kafka.common.serialization.Serializer

class JsonSerializer<T> : Serializer<T> {
    override fun serialize(topic: String, data: T?): ByteArray? {
        if (data == null) return null
        return jsonMapper.writeValueAsBytes(data)
    }

    override fun close() {}
    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}
}