package com.geofencing.demo.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Dao
interface GeofenceDao {

    @Query("SELECT * FROM geofence_table ORDER BY id ASC")
    fun getGeofences(): Flow<MutableList<GeofenceEntity>>

    @Query("SELECT * FROM geofence_table WHERE id=:id LIMIT 1")
    fun   getGeofenceById(id:Int): GeofenceEntity?

    @Query("SELECT id FROM geofence_table ORDER BY id DESC LIMIT 1 ")
    suspend  fun getLastGeoId():Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addGeofence(geofenceEntity: GeofenceEntity)

    @Delete
    suspend fun removeGeofence(geofenceEntity: GeofenceEntity)


    @Query("DELETE  FROM geofence_table  WHERE id=:id")
    suspend fun removeGeofenceByGeoId(id: Int)

}