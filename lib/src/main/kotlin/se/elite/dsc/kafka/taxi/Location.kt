package se.elite.dsc.kafka.taxi

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class Location @JsonCreator constructor(@JsonProperty("latitude") val latitude: Double, @JsonProperty("longitude") val longitude: Double) : Serializable {

    override fun toString(): String {
        return String.format("Location(%f,%f)", latitude, longitude);
    }
}