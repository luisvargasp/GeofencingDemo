package com.geofencing.demo.ui.service_geofence

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.geofencing.demo.R
import com.geofencing.demo.data.service_models.CircularGeofence
import com.geofencing.demo.data.service_models.Geofence
import com.geofencing.demo.data.service_models.PolygonalGeofence
import com.geofencing.demo.databinding.ActivityServiceGeofencingBinding
import com.geofencing.demo.databinding.CreateGeofenceLayoutBinding
import com.geofencing.demo.service.GeofencingService
import com.geofencing.demo.util.Constants.ACTION_SERVICE_START
import com.geofencing.demo.util.Constants.ACTION_SERVICE_STOP
import com.geofencing.demo.util.ViewExtensions.hide
import com.geofencing.demo.util.ViewExtensions.show
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions

class ServiceGeofencingActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener  {
    private lateinit var polygonMap: Polygon
    private lateinit var binding: ActivityServiceGeofencingBinding
    private lateinit var map: GoogleMap


    private  var geofences= arrayListOf<Geofence>()

    private var polygon = arrayListOf<LatLng>()// used to set a polygon
    private var hole = arrayListOf<LatLng>()// used to set a hole


    private var holes= arrayListOf<List<LatLng>>()
    private var isDrawingOuterPolygon=true





    private var markers= arrayListOf<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServiceGeofencingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.btnServiceAction.setOnClickListener {


            sendActionCommandToService(if(GeofencingService.started.value!!) ACTION_SERVICE_STOP else ACTION_SERVICE_START)


        }
        binding.rgShape.setOnCheckedChangeListener { _, id ->
            if (findViewById<RadioButton>(id)==binding.rbCircle){
                binding.ll.hide()

            }else{
                binding.ll.show()
            }
        }
        binding.btnSetPolygon.setOnClickListener {
            addPolygon(polygon)
            markers.forEach{
               // it.remove()
            }
            hole.clear()
            isDrawingOuterPolygon=false

        }
        binding.btnSetHole.setOnClickListener {
            holes.add(hole.toList())
            polygonMap.remove()
            drawPolygonalGeofence(PolygonalGeofence(polygon,holes))
            markers.forEach{
                //it.remove()
            }
            hole.clear()

        }
        binding.btnSetGeofence.setOnClickListener{

            geofences.add(PolygonalGeofence(polygon.toList(),holes.toList()))//save polygon
            polygon.clear()
            hole.clear()
            holes.clear()
            markers.forEach{
                it.remove()
            }
            Toast.makeText(this,"Geofence ${geofences.size} saved ",Toast.LENGTH_SHORT).show()
            isDrawingOuterPolygon=true

        }
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        this.map=map
        map.isMyLocationEnabled = true
        map.setOnMapLongClickListener(this)
        map.uiSettings.apply {
            isMyLocationButtonEnabled = true
            isZoomControlsEnabled = true

            isMapToolbarEnabled = false
        }
        GeofencingService.geofences.observe(this) {
            this.map.clear()
           // circularGeofences.clear()
            //polygonalGeofences.clear()
            geofences.clear()

            if (it.isNullOrEmpty().not()) {
                it.forEach {geofence ->

                    if(geofence is CircularGeofence){
                        drawCircularGeofence(geofence)
                    }else if(geofence is PolygonalGeofence){
                        drawPolygonalGeofence(geofence)
                    }


                }
            }
        }
        GeofencingService.started.observe(this) {
            binding.btnServiceAction.show()
           if(it){
               binding.btnServiceAction.text= getString(R.string.stop_service)

               binding.rgShape.hide()
                binding.ll.hide()
           }else{
               binding.btnServiceAction.text= getString(R.string.start_service)
               binding.rgShape.show()
               if(binding.rbPolygon.isChecked){
                   binding.ll.show()
               }

           }
        }
    }

    override fun onMapLongClick(point: LatLng) {

        if(GeofencingService.started.value!!){
            return
        }

        if(binding.rbCircle.isChecked){
            val inflater = layoutInflater
            val binding = CreateGeofenceLayoutBinding.inflate(inflater)
            val dialog = AlertDialog.Builder(this).create()
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            dialog.setView(binding.root)
            binding.btnAccept.setOnClickListener {

                val geofence=CircularGeofence(point,binding.etRadius.text.toString().toFloat())

                geofences.add(geofence)

                drawCircularGeofence(geofence)
                Toast.makeText(this,"Geofence ${geofences.size} saved ",Toast.LENGTH_SHORT).show()


                dialog.dismiss()
            }
            dialog.show()
        }else{
            if(isDrawingOuterPolygon){
                polygon.add(point)
            }else{
                hole.add(point)
            }



           val marker= map.addMarker(
                MarkerOptions().position(point).title(polygon.size.toString())
                .alpha(1f)// opacity
                .draggable(false)
                .flat(false)
            )
            marker?.let {
                markers.add(it)
            }



        }


    }

    private fun drawCircularGeofence(geofence: CircularGeofence) {
        map.addCircle(
            CircleOptions().center(geofence.center).radius(geofence.radius.toDouble())
                .strokeColor(ContextCompat.getColor(this, R.color.blue_700))
                .fillColor(ContextCompat.getColor(this, R.color.blue_transparent))
        )

    }
    private fun drawPolygonalGeofence(geofence: PolygonalGeofence) {
       polygonMap= map.addPolygon(
            PolygonOptions().apply {
                addAll(geofence.points)
                if(geofence.holes.isNotEmpty()){
                   geofence.holes.forEach {
                       addHole(it)
                   }
                }
                strokeWidth(10f)
                strokeColor(ContextCompat.getColor(this@ServiceGeofencingActivity, R.color.blue_700))
                fillColor(ContextCompat.getColor(this@ServiceGeofencingActivity, R.color.blue_transparent))
                geodesic(true)
            }
        )

    }
    private fun addPolygon(points:List<LatLng>){

        polygonMap =map.addPolygon(
            PolygonOptions().apply {
                addAll(points)
                strokeWidth(10f)
                strokeColor(ContextCompat.getColor(this@ServiceGeofencingActivity, R.color.blue_700))
                fillColor(ContextCompat.getColor(this@ServiceGeofencingActivity, R.color.blue_transparent))
                geodesic(true)
            }
        )

    }
    private fun sendActionCommandToService(action: String) {
        map.clear()

       // val geofences= arrayListOf<Geofence>()
       // geofences.addAll(this.geofences)
        //geofences.addAll(polygonalGeofences)
        Intent(this, GeofencingService::class.java).apply {
            this.action = action
            putParcelableArrayListExtra("geofences", geofences)
            startService(this)
        }


    }
}