package com.geofencing.demo.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope

import com.geofencing.demo.databinding.ActivityInitBinding
import com.geofencing.demo.ui.google_geofencing.GoogleGeofencingActivity
import com.geofencing.demo.ui.service_geofence.ServiceGeofencingActivity
import com.geofencing.demo.util.Permissions.hasBackgroundLocationPermission
import com.geofencing.demo.util.Permissions.hasLocationPermission
import com.geofencing.demo.util.Permissions.hasPostNotificationsPermission
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class InitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInitBinding
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var backgroundLocationPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var postNotificationsPermissionLauncher: ActivityResultLauncher<String>




    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInitBinding.inflate(layoutInflater)
        setContentView(binding.root)
        locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {isGranted ->
            if (isGranted) {
                // PERMISSION GRANTED
                checkPermissions()
            } else {
                // PERMISSION NOT GRANTED
            }
        }
        backgroundLocationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {isGranted ->
            if (isGranted) {
                // PERMISSION GRANTED
                checkPermissions()

            } else {
                // PERMISSION NOT GRANTED
            }
        }
        postNotificationsPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {isGranted ->
            if (isGranted) {
                // PERMISSION GRANTED
                checkPermissions()

            } else {
                // PERMISSION NOT GRANTED
            }
        }
        lifecycleScope.launch {
            delay(2000)

            checkPermissions()
        }


    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermissions() {
        if(hasLocationPermission(this)&& hasBackgroundLocationPermission(this)&& hasPostNotificationsPermission(this) ){
            toNextScreen()
        }
        else if(!hasLocationPermission(this)){
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)

        }else if(!hasBackgroundLocationPermission(this)){
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)


        }else if(!hasPostNotificationsPermission(this)){
            locationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

    }
    private fun toNextScreen(){
       startActivity(Intent(this,
           ServiceGeofencingActivity::class.java))
        finish()

    }

}