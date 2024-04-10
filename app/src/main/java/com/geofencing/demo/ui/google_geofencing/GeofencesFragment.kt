package com.geofencing.demo.ui.google_geofencing

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.geofencing.demo.adapter.GeofencesAdapter
import com.geofencing.demo.databinding.FragmentGeofencesBinding


class GeofencesFragment : Fragment() {
    private var _binding: FragmentGeofencesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GoogleGeofencingViewModel by activityViewModels()
    private val geofencesAdapter by lazy { GeofencesAdapter(viewModel) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentGeofencesBinding.inflate(inflater, container, false)

        setupToolbar()
        setupRecyclerView()
        observeDatabase()

        return binding.root
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        binding.rvGeofences.layoutManager = LinearLayoutManager(requireContext())
        binding.rvGeofences.adapter = geofencesAdapter
    }

    private fun observeDatabase() {
        viewModel.geofences.observe(viewLifecycleOwner) {
            geofencesAdapter.setData(it)
            binding.rvGeofences.scheduleLayoutAnimation()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}