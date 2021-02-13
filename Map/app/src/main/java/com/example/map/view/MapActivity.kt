package com.example.map.view

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.map.BuildConfig
import com.example.map.R
import com.example.map.data.Place
import com.example.map.viewmodel.MapViewModel
import com.google.android.gms.location.*
import com.microsoft.maps.*
import java.util.concurrent.TimeUnit


class MapActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MapApp"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private val mapViewModel by viewModels<MapViewModel>()
    private val mapView by lazy { MapView(this, MapRenderMode.VECTOR) }
    private val pinLayer by lazy { MapElementLayer() }

    // region activity lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        initMapView()
        mapView.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        setLastLocation()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
        mapView.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.size >= 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    setLastLocation()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
    // endregion

    private fun initMapView() {
        mapView.setCredentialsKey(BuildConfig.CREDENTIALS_KEY)
        (findViewById<FrameLayout>(R.id.mapView)).addView(mapView)
        mapView.layers.add(pinLayer)

//        (findViewById<FrameLayout>(R.id.mapView)).addView(geofenceIndicator)
//        (geofenceIndicator.layoutParams as ViewGroup.MarginLayoutParams).topMargin = 20
//        (geofenceIndicator.layoutParams as ViewGroup.MarginLayoutParams).leftMargin = 20
    }

//    private val geofenceIndicator by lazy {
//        val indicator = View(this)
//        indicator.layoutParams = ViewGroup.LayoutParams(100, 100)
//        indicator.background = ColorDrawable(Color.RED)
//        indicator
//    }

    private fun onLocationUpdated(currentLocation: Location) {
        pinLayer.elements.clear()
        val locationGeopoint = Geopoint(currentLocation.latitude, currentLocation.longitude)
        mapView.setScene(MapScene.createFromLocationAndZoomLevel(locationGeopoint, 15.0), MapAnimationKind.DEFAULT)
        addCurrentLocationMapIcon(locationGeopoint)
        val location = "${currentLocation.latitude},${currentLocation.longitude}"
        mapViewModel.getPointsOfInterest(location).observe(this, {
            addPointsOfInterestMapIcons(it)
        })
    }

    private fun addCurrentLocationMapIcon(locationGeopoint: Geopoint) {
        val currentLocationMapIcon = MapIcon()
        currentLocationMapIcon.location = locationGeopoint
        currentLocationMapIcon.title = getString(R.string.yourLocation)
        pinLayer.elements.add(currentLocationMapIcon)
    }

    private fun addPointsOfInterestMapIcons(points: List<Place>) {
        val firstPointsOfInterest = points.take(5) // five points of interest
        for (point in firstPointsOfInterest) {
            val icon = MapIcon()
            val geopoint = Geopoint(point.geometry.location.lat, point.geometry.location.lng)
            icon.location = geopoint
            icon.title = point.name
            mapViewModel.bitmapFromUrl(point.icon).observe(this, {
                if (it != null) {
                    icon.image = MapImage(it)
                }
            })
            pinLayer.elements.add(icon)
        }
    }

    // region location service
    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private val locationRequest = LocationRequest().apply {
        interval = TimeUnit.SECONDS.toMillis(30)
        fastestInterval = TimeUnit.SECONDS.toMillis(30)
        maxWaitTime = TimeUnit.MINUTES.toMillis(2)
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
            //if (locationResult?.locations != null && locationResult.locations.size > 0) {
            if (locationResult?.lastLocation != null) {
                onLocationUpdated(locationResult.lastLocation)
            } else {
                Log.w(TAG, "Location information isn't available.")
            }
        }
    }

    private fun setLastLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener {
            if (it != null) {
                onLocationUpdated(it)
            }
        }
    }

    private fun startLocationUpdates() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun stopLocationUpdates() = fusedLocationClient.removeLocationUpdates(locationCallback)
    // endregion
}