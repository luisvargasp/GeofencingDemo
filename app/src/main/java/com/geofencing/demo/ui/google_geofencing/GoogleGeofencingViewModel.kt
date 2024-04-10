package com.geofencing.demo.ui.google_geofencing

import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.geofencing.demo.broadcast.GeofencingBroadcastReceiver
import com.geofencing.demo.data.GeofenceEntity
import com.geofencing.demo.data.GeofenceRepository
import com.geofencing.demo.util.Permissions
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil

@HiltViewModel
class GoogleGeofencingViewModel @Inject constructor(
    application: Application,
    private val geofenceRepository: GeofenceRepository
) : AndroidViewModel(application) {
    val app = application
    private var geofencingClient = LocationServices.getGeofencingClient(app.applicationContext)



    // Database
    val geofences =    geofenceRepository.geofences.asLiveData()


    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(app, GeofencingBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            app,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                (PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
            else PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun addGeofenceToDatabase(geofenceEntity: GeofenceEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            geofenceRepository.addGeofence(geofenceEntity)
        }

    fun removeGeofenceFromDatabase(geofenceEntity: GeofenceEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            geofenceRepository.removeGeofence(geofenceEntity)
        }

    private fun removeGeofenceFromDatabaseById(id: Int) =
        viewModelScope.launch(Dispatchers.IO) {
            geofenceRepository.removeGeofenceByGeoId(id)
        }


    @SuppressLint("MissingPermission")
    suspend fun addGeofenceToSystem(//Geofence API
        latitude: Double,
        longitude: Double, radius: Float
    ) {
        if (Permissions.hasBackgroundLocationPermission(app)) {
            var geoId = geofenceRepository.getNextGeoIdToCreate()
            val geofence = Geofence.Builder()
                .setRequestId(geoId.toString())
                .setCircularRegion(
                    latitude,
                    longitude,
                    radius
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(
                    Geofence.GEOFENCE_TRANSITION_ENTER
                            or Geofence.GEOFENCE_TRANSITION_EXIT
                            or Geofence.GEOFENCE_TRANSITION_DWELL
                )
                .setLoiteringDelay(7000)
                .build()

            val geofencingRequest = GeofencingRequest.Builder()
                .setInitialTrigger(
                    GeofencingRequest.INITIAL_TRIGGER_ENTER
                            // or GeofencingRequest.INITIAL_TRIGGER_EXIT
                            or GeofencingRequest.INITIAL_TRIGGER_DWELL
                )
                .addGeofence(geofence)
                .build()
            Log.d("Geofence adding", geoId.toString())

            geofencingClient.addGeofences(geofencingRequest, getPendingIntent()).run {
                addOnSuccessListener {
                    Log.d("Geofence adding", "Successfully added.")
                    Toast.makeText(
                        app.applicationContext,
                        "Geofence  $geoId successfully added",
                        Toast.LENGTH_SHORT
                    ).show()
                    addGeofenceToDatabase(
                        GeofenceEntity(
                            geoId,
                            latitude = latitude,
                            longitude = longitude,
                            radius = radius
                        )
                    )
                }
                addOnFailureListener {
                    Toast.makeText(
                        app.applicationContext,
                        "Error  adding geofence",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(
                        "Geofence  adding Error ",
                        it.message.toString() + " " + it.localizedMessage + " " + it.stackTrace
                    )
                }
            }
        } else {
            Log.d("Geofence", "Permission not granted.")
        }
    }

    suspend fun removeGeofenceFromSystem(id: Int) {
        geofencingClient.removeGeofences(mutableListOf(id.toString()))
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(
                        app.applicationContext,
                        "Geofence $id successfully removed from system",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        app.applicationContext,
                        "Failed to  remove geofence  from system",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                removeGeofenceFromDatabaseById(id)
            }
    }

    fun getBounds(center: LatLng, radius: Float): LatLngBounds {
        val distanceFromCenterToCorner = radius * kotlin.math.sqrt(2.0)
        val southWestCorner = SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0)
        val northEastCorner = SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0)
        return LatLngBounds(southWestCorner, northEastCorner)
    }

    @SuppressLint("MissingPermission")
     fun registerLocalGeofences(locals: MutableList<GeofenceEntity>) {

        if(locals.isEmpty()){
            return
        }


        val localGeofences =locals.map {

            Geofence.Builder()
                .setRequestId(it.id.toString())
                .setCircularRegion(
                    it.latitude,
                    it.longitude,
                    it.radius
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(
                    Geofence.GEOFENCE_TRANSITION_ENTER
                            or Geofence.GEOFENCE_TRANSITION_EXIT
                            or Geofence.GEOFENCE_TRANSITION_DWELL
                )
                .setLoiteringDelay(7000)
                .build()
        }


        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(
                GeofencingRequest.INITIAL_TRIGGER_ENTER
                        // or GeofencingRequest.INITIAL_TRIGGER_EXIT
                        or GeofencingRequest.INITIAL_TRIGGER_DWELL
            )
            .addGeofences(localGeofences)
            .build()


        geofencingClient.addGeofences(geofencingRequest, getPendingIntent()).run {
            addOnSuccessListener {
                Toast.makeText(
                    app.applicationContext,
                    " ${localGeofences.size} geofences successfully added to system",
                    Toast.LENGTH_SHORT
                ).show()

            }
            addOnFailureListener {
                Toast.makeText(
                    app.applicationContext,
                    "Error  adding geofences",
                    Toast.LENGTH_SHORT
                ).show()

            }

        }
    }


}