package com.example.map.viewmodel

import android.app.Application
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import com.example.map.data.PlacesData
import kotlinx.coroutines.Dispatchers
import java.io.IOException
import java.net.URL

class MapViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        private const val TAG = "MapApp"
    }

    fun getPointsOfInterest(location: String) = liveData(Dispatchers.IO) {
        val points = PlacesData.getPointsOfInterest(location)
        emit(points)
    }

    fun bitmapFromUrl(urlString: String) = liveData(Dispatchers.IO) {
        val url = URL(urlString)
        try {
            val stream = url.openConnection().getInputStream()
            emit(BitmapFactory.decodeStream(stream))
        } catch (e: IOException) {
            Log.w(TAG, "MapViewModel.bitmapFromUrl - IOException, error=${e.message}")
            emit(ContextCompat.getDrawable(getApplication(), android.R.drawable.star_on)?.toBitmap())
        }
    }
}