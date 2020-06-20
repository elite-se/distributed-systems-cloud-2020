package se.elite.dsc.kafka

import jsonMapper
import org.apache.kafka.common.serialization.Deserializer

open class JsonDeserializer<T>(val clazz: Class<T>) : Deserializer<T> {

    override fun deserialize(topic: String?, data: ByteArray?): T? {
        if (data == null) return null;
        return jsonMapper.readValue(data, clazz)
    }

    override fun close() {}
    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}
}