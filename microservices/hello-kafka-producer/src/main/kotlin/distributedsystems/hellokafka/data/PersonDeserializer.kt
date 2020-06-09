package distributedsystems.hellokafka.data

import distributedsystems.hellokafka.jsonMapper
import org.apache.kafka.common.serialization.Deserializer

class PersonDeserializer: Deserializer<Person> {
    override fun deserialize(topic: String?, data: ByteArray?): Person? {
        if (data == null) return null;
        return jsonMapper.readValue(data, Person::class.java)
    }

    override fun close() {}
    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}
}