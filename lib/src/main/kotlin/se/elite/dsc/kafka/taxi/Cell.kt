package se.elite.dsc.kafka.taxi

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import kotlin.math.abs
import kotlin.math.floor

data class Cell @JsonCreator constructor(@JsonProperty("clat") val clat: Int, @JsonProperty("clong") val clong: Int) : Serializable {

    constructor(origin: Location, sideLength: Location, l: Location)
            : this(floor(((origin.latitude - l.latitude) / sideLength.latitude) + 1).toInt(),
            floor((abs(l.longitude - origin.longitude) / sideLength.longitude) + 1).toInt())

    fun inBounds(minClat: Int, minClong: Int, maxClat: Int, maxClong: Int): Boolean {
        return (this.clat >= minClat && this.clong >= minClong) && (this.clat <= maxClat && this.clong <= maxClong);
    }

    fun inBounds(maxClat: Int, maxClong: Int): Boolean {
        return inBounds(1, 1, maxClat, maxClong);
    }

    override fun toString(): String {
        return String.format("Cell(%d,%d)", clat, clong);
    }

}