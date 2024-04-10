package com.geofencing.demo.data.service_models

import android.os.Parcelable
import com.geofencing.demo.util.GeofencingUtils
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
data class PolygonalGeofence(val points:List<LatLng>,val holes:List<List<LatLng>> = emptyList()):Geofence,Parcelable {
    override fun isInsideGeofence(target: LatLng): Boolean {
        return  if(holes.isEmpty())  GeofencingUtils.isPointInsidePolygon(target,points)  else
            GeofencingUtils.isPointInsidePolygon(target,points) &&  holes.none { GeofencingUtils.isPointInsidePolygon(target, it) }
    }
}