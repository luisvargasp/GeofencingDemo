package com.geofencing.demo.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.geofencing.demo.R
import com.geofencing.demo.data.service_models.CircularGeofence
import com.geofencing.demo.data.service_models.Geofence
import com.geofencing.demo.util.Constants.ACTION_SERVICE_START
import com.geofencing.demo.util.Constants.ACTION_SERVICE_STOP
import com.geofencing.demo.util.Constants.LOCATION_UPDATE_INTERVAL
import com.geofencing.demo.util.Constants.SERVICE_NOTIFICATION_CHANNEL_ID
import com.geofencing.demo.util.Constants.SERVICE_NOTIFICATION_CHANNEL_NAME
import com.geofencing.demo.util.Constants.SERVICE_NOTIFICATION_ID
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
@AndroidEntryPoint
class GeofencingService: LifecycleService() {
    companion object{
        const val DWELLING_TIME=10000
        val started= MutableLiveData(false)
        val geofences = MutableLiveData<MutableList<Geofence>>()
    }
    private lateinit var states: MutableList<Boolean?>
    private lateinit var enteringTime: MutableList<Long?>


    @Inject
    lateinit var notification: NotificationCompat.Builder


    @Inject
    lateinit var notificationManager: NotificationManager





    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    private fun setInitialValues(){
        started.value=false
        geofences.value= mutableListOf()
    }
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            result.locations.let { locations ->

                for (location in locations) {
                    Log.d("GeofencingService", LatLng(location.latitude,location.longitude).toString())
                    updateNotificationPeriodically(LatLng(location.latitude,location.longitude))
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        setInitialValues()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {

            when(it.action){
                ACTION_SERVICE_START->{
                    started.value=true

                    val geofences= if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableArrayListExtra("geofences",Geofence::class.java)
                    } else {
                        TODO("VERSION.SDK_INT < TIRAMISU")
                    }
                    geofences?.forEach {geofence->
                        //Log.d("TestLog",geofence.radius.toString())
                    }
                    states= MutableList(geofences!!.size){null}
                    enteringTime= MutableList(geofences!!.size){null}
                    Companion.geofences.value=geofences
                    startForegroundService()
                    startLocationUpdates()
                }
                ACTION_SERVICE_STOP->{
                    setInitialValues()
                    stopForegroundService()
                }
                else->{

                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
    private fun startForegroundService(){
        createNotificationChannel()
        startForeground(SERVICE_NOTIFICATION_ID,notification.build())
    }
    @SuppressLint("MissingPermission")
    private  fun startLocationUpdates(){
        val locationRequest =  LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL
        ).build()

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            SERVICE_NOTIFICATION_CHANNEL_ID,
            SERVICE_NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
    private fun stopForegroundService() {
        removeLocationUpdates()
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(
            SERVICE_NOTIFICATION_ID
        )
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun removeLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
    private fun updateNotificationPeriodically(myLocation:LatLng) {

        var message=""
        geofences.value?.forEachIndexed { index, circularGeofence ->
            val isInside=circularGeofence.isInsideGeofence(myLocation)
            if(states.get(index)==false&&isInside){//show entering geofence
                showGeofenceEvent(index+1,"Entering Geofence ${index+1}")
                enteringTime[index]=System.currentTimeMillis()

            }else if(states.get(index)==true&&!isInside){//show exiting geofence
                showGeofenceEvent(index+1,"Exiting Geofence ${index+1}")
                enteringTime[index]=null

            }
            if(enteringTime[index]!=null&&isInside&&(System.currentTimeMillis()- enteringTime[index]!! >= DWELLING_TIME)){
                showGeofenceEvent(index+1,"Dwelling in  Geofence ${index+1}")
                enteringTime[index]=null


            }

            states[index]=isInside

            message += "${if (isInside) {
                "Inside of"
            } else "Outside of"}  Geofence ${index + 1} \n"



        }

        notification.apply {
            setContentTitle("Geofence States")
            setContentText(message)
            setOngoing(true)
            setStyle(NotificationCompat.BigTextStyle())
        }
        notificationManager.notify(SERVICE_NOTIFICATION_ID, notification.build())


    }

    private fun showGeofenceEvent(id:Int,msg:String){
        notification.apply {
            setContentTitle("Geofence Event")
            setContentText(msg)
            setOngoing(false)
        }
        notificationManager.notify(id, notification.build())


    }
}