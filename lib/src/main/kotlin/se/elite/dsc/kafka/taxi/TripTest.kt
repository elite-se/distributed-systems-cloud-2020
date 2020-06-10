package se.elite.dsc.kafka.taxi

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import java.util.*

data class Trip @JsonCreator constructor(
        @param:JsonProperty("medallion") val medallion: String,
        @param:JsonProperty("hackLicense") val hackLicense: String,
        @param:JsonProperty("pickupDatetime") val pickupDatetime: Date,
        @param:JsonProperty("dropoffDatetime") val dropoffDatetime: Date,
        @param:JsonProperty("tripTime") val tripTime: Double,
        @param:JsonProperty("tripDistance") val tripDistance: Double,
        @param:JsonProperty("pickupLoc") val pickupLoc: Location,
        @param:JsonProperty("dropoffLoc") val dropoffLoc: Location,
        @param:JsonProperty("paymentType") val paymentType: PaymentType,
        @param:JsonProperty("fareAmount") val fareAmount: Double,
        @param:JsonProperty("surcharge") val surcharge: Double,
        @param:JsonProperty("mtaTax") val mtaTax: Double,
        @param:JsonProperty("tipAmount") val tipAmount: Double,
        @param:JsonProperty("tollsAmount") val tollsAmount: Double,
        @param:JsonProperty("totalAmount") val totalAmount: Double) : Serializable {

    enum class PaymentType {
        CSH, CRD
    }

    override fun toString(): String {
        return "Trip{" +
                "medallion='" + medallion + '\'' +
                ", hackLicense='" + hackLicense + '\'' +
                ", pickupDatetime=" + pickupDatetime +
                ", dropoffDatetime=" + dropoffDatetime +
                ", tripTime=" + tripTime +
                ", tripDistance=" + tripDistance +
                ", pickupLoc=" + pickupLoc +
                ", dropoffLoc=" + dropoffLoc +
                ", paymentType=" + paymentType +
                ", fareAmount=" + fareAmount +
                ", surcharge=" + surcharge +
                ", mtaTax=" + mtaTax +
                ", tipAmount=" + tipAmount +
                ", tollsAmount=" + tollsAmount +
                ", totalAmount=" + totalAmount +
                '}'
    }
}