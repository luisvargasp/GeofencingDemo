package com.geofencing.demo.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.geofencing.demo.data.GeofenceEntity
import com.geofencing.demo.databinding.GeofenceItemBinding
import com.geofencing.demo.ui.google_geofencing.GeofencesFragmentDirections
import com.geofencing.demo.ui.google_geofencing.GoogleGeofencingViewModel
import com.geofencing.demo.util.MyDiffUtil
import kotlinx.coroutines.launch

class GeofencesAdapter(private val viewModel: GoogleGeofencingViewModel) :
    RecyclerView.Adapter<GeofencesAdapter.MyViewHolder>() {

    private var geofenceEntity = mutableListOf<GeofenceEntity>()

    class MyViewHolder(val binding: GeofenceItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(geofenceEntity: GeofenceEntity) {
            binding.geofencesEntity = geofenceEntity
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): MyViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = GeofenceItemBinding.inflate(layoutInflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentGeofence = geofenceEntity[position]
        holder.bind(currentGeofence)

        holder.binding.deleteImageView.setOnClickListener {
            removeItem(holder, position)
        }

        holder.binding.clContent.setOnClickListener {
            val action =
                GeofencesFragmentDirections.backToMaps(currentGeofence)
            holder.itemView.findNavController().navigate(action)
        }
    }

    private fun removeItem(holder: MyViewHolder, position: Int) {
        viewModel.viewModelScope.launch {
            viewModel.removeGeofenceFromSystem(geofenceEntity[position].id)
        }
    }





    override fun getItemCount(): Int {
        return geofenceEntity.size
    }

    fun setData(newGeofenceEntity: MutableList<GeofenceEntity>) {
        val geofenceDiffUtil = MyDiffUtil(geofenceEntity, newGeofenceEntity)
        val diffUtilResult = DiffUtil.calculateDiff(geofenceDiffUtil)
        geofenceEntity = newGeofenceEntity
        diffUtilResult.dispatchUpdatesTo(this)
    }
}