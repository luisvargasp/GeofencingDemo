package com.geofencing.demo.data

import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ViewModelScoped
class GeofenceRepository @Inject constructor(private val geofenceDao: GeofenceDao) {
    val geofences: Flow<MutableList<GeofenceEntity>> = geofenceDao.getGeofences()

    suspend fun addGeofence(geofenceEntity: GeofenceEntity) {
        geofenceDao.addGeofence(geofenceEntity)
    }

    suspend fun removeGeofence(geofenceEntity: GeofenceEntity) {
        geofenceDao.removeGeofence(geofenceEntity)
    }
    suspend fun getNextGeoIdToCreate():Int {
        return if(geofenceDao.getLastGeoId()!=null) (geofenceDao.getLastGeoId()!!)+1 else 1
    }

    suspend fun removeGeofenceByGeoId(geoId: Int) {
        geofenceDao.removeGeofenceByGeoId(geoId)
    }

}