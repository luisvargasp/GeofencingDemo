package com.geofencing.demo.util

import android.graphics.PointF
import com.google.android.gms.maps.model.LatLng

object GeofencingUtils {

    fun isInsideCircle(target:LatLng,center:LatLng,radius:Double):Boolean{
        val distance= distanceBetweenPoints(target,center)
        return distance<=radius
    }

    private fun distanceBetweenPoints(point1:LatLng,point2:LatLng):Double{
        val earthRadius=6371000
        val lat1=Math.toRadians(point1.latitude)
        val lon1=Math.toRadians(point1.longitude)
        val lat2=Math.toRadians(point2.latitude)
        val lon2=Math.toRadians(point2.longitude)

        val dLat=lat2-lat1
        val dLon=lon2-lon1

        val a=Math.sin(dLat/2)*Math.sin(dLat/2)+
                Math.cos(lat1)*Math.cos(lat2)*
                Math.sin(dLon/2)*Math.sin(dLon/2)

        val c=2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a))
        return earthRadius*c
    }
    fun isPointInsidePolygon(point: LatLng, polygon: List<LatLng>): Boolean {
        val p = PointF(point.latitude.toFloat(), point.longitude.toFloat())
        val poly = polygon.map { PointF(it.latitude.toFloat(), it.longitude.toFloat()) }

        var c = false
        var i = -1
        var j = poly.size - 1
        while (++i < poly.size) {
            if ((poly[i].y <= p.y && p.y < poly[j].y || poly[j].y <= p.y && p.y < poly[i].y) &&
                p.x < (poly[j].x - poly[i].x) * (p.y - poly[i].y) / (poly[j].y - poly[i].y) + poly[i].x
            ) {
                c = !c
            }
            j = i
        }
        return c
    }




}