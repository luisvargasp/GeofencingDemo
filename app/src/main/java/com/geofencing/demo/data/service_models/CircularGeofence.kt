package com.geofencing.demo.data.service_models

import android.os.Parcelable
import com.geofencing.demo.util.GeofencingUtils
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class CircularGeofence(val center:LatLng, val radius:Float):Parcelable,Geofence {
    override fun isInsideGeofence(point: LatLng):Boolean {
        return GeofencingUtils.isInsideCircle(point,center,radius.toDouble())
    }
}