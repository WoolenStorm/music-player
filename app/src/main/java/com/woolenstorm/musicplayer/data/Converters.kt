package com.woolenstorm.musicplayer.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.woolenstorm.musicplayer.model.Song

class Converters {

    @TypeConverter
    fun listToJson(ids: List<Long?>) = Gson().toJson(ids)

    @TypeConverter
    fun jsonToList(value: String) = Gson().fromJson(value, Array<Long?>::class.java).toList()
}