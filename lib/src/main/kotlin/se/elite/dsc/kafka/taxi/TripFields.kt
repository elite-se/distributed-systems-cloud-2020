package se.elite.dsc.kafka.taxi

enum class TripFields {
    MEDALLION, // md5sum identifier of the taxi (vehicle bound)
    HACK_LICENSE, // md5sum of the tax license
    PICKUP_DATETIME, // time when passenger(s) were picked up
    DROPOFF_DATETIME, // time when passenger(s) were dropped off
    TRIP_TIME,  // duration in seconds
    TRIP_DISTANCE,  // distance in miles
    PICKUP_LONGITUDE, // longitude of pickup
    PICKUP_LATITUDE, // latitude of pickup
    DROPOFF_LONGITUDE, // longitude of drop-off
    DROPOFF_LATITUDE, // latitude of drop-off
    PAYMENT_TYPE,  // cash (CSH) or credit card (CRD) payment
    FARE_AMOUNT,  // fare amount in dollars
    SURCHARGE,  // surcharge in dollars
    MTA_TAX,  // tax in dollars
    TIP_AMOUNT,  // tip in dollars
    TOLLS_AMOUNT,  // bridge and tunnel tolls in dollars
    TOTAL_AMOUNT // total paid amount in dollars
}