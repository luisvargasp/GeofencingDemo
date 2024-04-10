package com.geofencing.demo.broadcast

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.geofencing.demo.R
import com.geofencing.demo.data.GeofenceDao
import com.geofencing.demo.util.Constants.NOTIFICATION_CHANNEL_ID
import com.geofencing.demo.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GeofencingBroadcastReceiver: BroadcastReceiver() {
    @Inject
    lateinit var geofenceDao: GeofenceDao


    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if(geofencingEvent?.hasError()!!){
            val errorMessage = GeofenceStatusCodes
                .getStatusCodeString(geofencingEvent.errorCode)
            Log.e("BroadcastReceiver", errorMessage)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            geofencingEvent.triggeringGeofences?.forEach {

                var geofence= geofenceDao.getGeofenceById((it.requestId.toInt()))
                //  Log.d("GeofenceReceiver",geofence.toString())

                if(geofence==null){
                    //unregister

                    return@forEach
                }

                when(geofencingEvent.geofenceTransition){
                    Geofence.GEOFENCE_TRANSITION_ENTER -> {
                        Log.d("GeofenceReceiver", "Geofence ENTER")

                        displayNotification(geofence.id, context,"ENTERING TO $geofence" )
                    }
                    Geofence.GEOFENCE_TRANSITION_EXIT -> {
                        Log.d("GeofenceReceiver", "Geofence EXIT")
                        displayNotification(geofence.id,context, "EXITING FROM $geofence")
                    }
                    Geofence.GEOFENCE_TRANSITION_DWELL -> {
                        Log.d("GeofenceReceiver", "Geofence DWELL")
                        displayNotification(geofence.id ,context, "DWELLING IN  $geofence ")
                    }
                    else -> {
                        Log.d("GeofenceReceiver", "Invalid Type")
                        displayNotification(geofence.id ,context, "Geofence INVALID TYPE $geofence")
                    }
                }
            }

        }


    }

    private fun displayNotification(notificationId:Int, context: Context, geofenceTransition: String){
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Geofence Event")
            .setDefaults(Notification.DEFAULT_VIBRATE)
            .setContentText(geofenceTransition)
        notificationManager.notify(notificationId, notification.build())
    }

    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }

}