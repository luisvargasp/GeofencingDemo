package com.geofencing.demo.ui.google_geofencing

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.geofencing.demo.R
import com.geofencing.demo.databinding.CreateGeofenceLayoutBinding
import com.geofencing.demo.databinding.FragmentMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private lateinit var map: GoogleMap
    private lateinit var circle: Circle
    private val args by navArgs<MapsFragmentArgs>()


    private val viewModel: GoogleGeofencingViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(layoutInflater, container, false)
        binding.lifecycleOwner=viewLifecycleOwner


        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        binding.geofencesFab.setOnClickListener {
            findNavController().navigate(R.id.toGeofences)
        }


    }
    private fun backFromGeofencesFragment() {
        if (args.geofenceEntity != null) {
            val selectedGeofence = LatLng(
                args.geofenceEntity!!.latitude,
                args.geofenceEntity!!.longitude
            )
            zoomToGeofence(selectedGeofence, args.geofenceEntity!!.radius)
        }
    }
    private fun zoomToGeofence(center: LatLng, radius: Float) {
        map.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                viewModel.getBounds(center, radius), 10
            ), 1000, null
        )
    }

    private fun drawCircle(location: LatLng, radius: Float) {
        circle = map.addCircle(
            CircleOptions().center(location).radius(radius.toDouble())
                .strokeColor(ContextCompat.getColor(requireContext(), R.color.blue_700))
                .fillColor(ContextCompat.getColor(requireContext(), R.color.blue_transparent))
        )
    }

    private fun drawMarker(location: LatLng, name: String) {
        map.addMarker(
            MarkerOptions().position(location).title(name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        )
    }






    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.isMyLocationEnabled = true
        map.setOnMapLongClickListener(this)
        map.uiSettings.apply {
            isMyLocationButtonEnabled = true
            isZoomControlsEnabled = true

            isMapToolbarEnabled = false
        }
        viewModel.geofences.observe(viewLifecycleOwner) { geofences->
            map.clear()
            geofences.forEach {
                drawCircle(LatLng(it.latitude, it.longitude), it.radius)
                drawMarker(LatLng(it.latitude, it.longitude), it.name)
            }
            //viewModel.registerLocalGeofences(geofences)

        }
        backFromGeofencesFragment()

    }

    override fun onMapLongClick(loc: LatLng) {

        val inflater = requireActivity().layoutInflater
        val binding = CreateGeofenceLayoutBinding.inflate(inflater)
        val dialog = AlertDialog.Builder(requireContext()).create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setView(binding.root)
         binding.btnAccept.setOnClickListener {
             lifecycleScope.launch {

                 viewModel.addGeofenceToSystem(loc.latitude,loc.longitude,binding.etRadius.text.toString().toFloat())
             }
             dialog.dismiss()
         }
        dialog.show()



    }



}