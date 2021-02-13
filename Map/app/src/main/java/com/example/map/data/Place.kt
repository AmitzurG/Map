package com.example.map.data

import com.google.gson.JsonObject

data class Place(val geometry: Geometry, val name: String, val icon: String)

data class Geometry(val location: Location, val viewport: JsonObject)

data class Location(val lat: Double, val lng: Double)

