package com.geofencing.demo.data
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.geofencing.demo.util.Constants.DATABASE_TABLE_NAME
import kotlinx.parcelize.Parcelize

@Entity(tableName = DATABASE_TABLE_NAME)
@Parcelize
class GeofenceEntity(
    @PrimaryKey(autoGenerate = false)
    var id: Int,
    val name: String="Geofence $id",
    val latitude: Double,
    val longitude: Double,
    val radius: Float,
): Parcelable {
    override fun toString(): String {
        return name
    }

}
