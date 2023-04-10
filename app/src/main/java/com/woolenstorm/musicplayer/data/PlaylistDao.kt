package com.woolenstorm.musicplayer.data

import androidx.room.*
import com.woolenstorm.musicplayer.model.Playlist
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM playlists")
    fun getAll(): Flow<List<Playlist>>

    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    @Insert
    suspend fun insertPlaylist(playlist: Playlist)

    @Delete
    suspend fun delete(playlist: Playlist)
}