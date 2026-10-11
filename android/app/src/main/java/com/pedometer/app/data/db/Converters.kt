package com.pedometer.app.data.db

import androidx.room.TypeConverter
import com.pedometer.app.data.model.LocationPoint
import com.pedometer.app.data.model.WorkoutType
import org.json.JSONArray
import org.json.JSONObject

class Converters {
    @TypeConverter
    fun fromHourlyList(list: List<Int>): String {
        return JSONArray(list).toString()
    }

    @TypeConverter
    fun toHourlyList(data: String?): List<Int> {
        if (data.isNullOrEmpty()) return List(24) { 0 }
        val array = JSONArray(data)
        val list = mutableListOf<Int>()
        for (i in 0 until array.length()) {
            list.add(array.getInt(i))
        }
        return list
    }

    @TypeConverter
    fun fromWorkoutType(type: WorkoutType): String = type.name

    @TypeConverter
    fun toWorkoutType(name: String): WorkoutType = try {
        WorkoutType.valueOf(name)
    } catch (e: Exception) {
        WorkoutType.WALK
    }

    @TypeConverter
    fun fromLocationList(list: List<LocationPoint>): String {
        val array = JSONArray()
        for (p in list) {
            val obj = JSONObject().apply {
                put("lat", p.latitude)
                put("lng", p.longitude)
                put("alt", p.altitude)
                put("time", p.timestamp)
                put("speed", p.speed)
            }
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toLocationList(data: String?): List<LocationPoint> {
        if (data.isNullOrEmpty()) return emptyList()
        val array = JSONArray(data)
        val list = mutableListOf<LocationPoint>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                LocationPoint(
                    latitude = obj.getDouble("lat"),
                    longitude = obj.getDouble("lng"),
                    altitude = obj.getDouble("alt"),
                    timestamp = obj.getLong("time"),
                    speed = obj.getDouble("speed").toFloat()
                )
            )
        }
        return list
    }
}
