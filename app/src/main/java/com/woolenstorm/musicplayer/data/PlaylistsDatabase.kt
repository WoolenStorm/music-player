package com.woolenstorm.musicplayer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.woolenstorm.musicplayer.model.Playlist


@Database(entities = [Playlist::class], version = 1)
@TypeConverters(Converters::class)
abstract class PlaylistsDatabase : RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao

    companion object {

        @Volatile
        private var Instance: PlaylistsDatabase? = null

        fun getDatabase(context: Context): PlaylistsDatabase {
            return Instance ?: synchronized(this) {
                Room
                    .databaseBuilder(context, PlaylistsDatabase::class.java, "items_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }


    }
}