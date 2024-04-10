package com.geofencing.demo.ui.google_geofencing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.geofencing.demo.R
import com.geofencing.demo.databinding.ActivityGoogleGeofencingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GoogleGeofencingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGoogleGeofencingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoogleGeofencingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }
}