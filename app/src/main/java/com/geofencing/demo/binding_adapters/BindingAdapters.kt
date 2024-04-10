package com.geofencing.demo.binding_adapters

import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("latitude", "longitude", requireAll = true)
fun TextView.parseCoordinates(lat: Double,long:Double) {
    val lat = String.format("%.4f", lat)
    val long = String.format("%.4f", long)
    "$lat  ,  $long".also { this.text = it }
}