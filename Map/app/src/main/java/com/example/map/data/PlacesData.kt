package com.example.map.data

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object PlacesData {
    const val key = "AIzaSyBg6OKWRluHwMgZkjMJKLeDYr1L-mz9s4Q"
    private const val baseUrl = "https://maps.googleapis.com/maps/api/place/"
    private val retrofit = Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create()).build()
    private val placeService = retrofit.create(PlaceService::class.java)

    suspend fun getPointsOfInterest(location: String): List<Place> {
        val placesJsonObject = placeService.getNearPlaces(location)
        val placesArray = placesJsonObject.get("results")?.asJsonArray
        return if (placesArray == null) emptyList() else Gson().fromJson(placesArray, object : TypeToken<List<Place>>() {}.type)
    }
}

private interface PlaceService {

    @GET("nearbysearch/json?key=${PlacesData.key}")
    suspend fun getNearPlaces(
            @Query("location") location: String,
            @Query("radius") radius: String = "1500",
            @Query("type") type: String = "point_of_interest"
    ) : JsonObject
}
