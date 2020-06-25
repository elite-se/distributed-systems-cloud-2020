package se.elite.dsc.kafka.taxi

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import java.util.*

data class Trip @JsonCreator constructor(
        @param:JsonProperty("pD") val pickupDatetime: Date,
        @param:JsonProperty("dD") val dropoffDatetime: Date,
        @param:JsonProperty("pL") val pickupLoc: Location,
        @param:JsonProperty("dL") val dropoffLoc: Location,
        @param:JsonProperty("fA") val fareAmount: Double,
        @param:JsonProperty("tA") val tipAmount: Double) : Serializable {

    enum class PaymentType {
        CSH, CRD
    }

    override fun toString(): String {
        return "Trip{" +
                "pickupDatetime=" + pickupDatetime +
                ", dropoffDatetime=" + dropoffDatetime +
                ", pickupLoc=" + pickupLoc +
                ", dropoffLoc=" + dropoffLoc +
                ", fareAmount=" + fareAmount +
                ", tipAmount=" + tipAmount +
                '}'
    }
}