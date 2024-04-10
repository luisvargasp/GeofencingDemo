package com.geofencing.demo.data.service_models

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

interface Geofence:Parcelable {
    fun isInsideGeofence(target:LatLng):Boolean
}