package com.woolenstorm.musicplayer.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.util.Log
import com.woolenstorm.musicplayer.model.Song
import java.io.File

private const val TAG = "MusicPlayerApi"

interface MusicPlayerApi {
   fun getSongs(): MutableList<Song>
}

class DefaultMusicPlayerApi(private val context: Context) : MusicPlayerApi {

    override fun getSongs(): MutableList<Song> {
        Log.d(TAG, "getting songs....")
        val list = mutableListOf<Song>()
        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                Media.EXTERNAL_CONTENT_URI
            }

        val projection = arrayOf(
            Media._ID,
            Media.DURATION,
            Media.TITLE,
            Media.ARTIST,
            Media.DATA,
            Media.ALBUM,
            Media.ALBUM_ID
        )

        val selection = Media.IS_MUSIC + "!= 0"
        val sortOrder = "${Media.TITLE} ASC"
        val cursor = context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val idColumn = cursor.getColumnIndexOrThrow(Media._ID)
                    val durationColumn = cursor.getColumnIndexOrThrow(Media.DURATION)
                    val titleColumn = cursor.getColumnIndexOrThrow(Media.TITLE)
                    val artistColumn = cursor.getColumnIndexOrThrow(Media.ARTIST)
                    val pathColumn = cursor.getColumnIndexOrThrow(Media.DATA)
                    val albumColumn = cursor.getColumnIndexOrThrow(Media.ALBUM)
                    val albumIdColumn = cursor.getColumnIndexOrThrow(Media.ALBUM_ID)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val duration = cursor.getFloat(durationColumn)
                        val title = cursor.getString(titleColumn)
                        val path = cursor.getString(pathColumn)
                        val artist = cursor.getString(artistColumn)
                        val album = cursor.getString(albumColumn)
                        val albumId = cursor.getLong(albumIdColumn)
                        val contentUri: Uri = ContentUris.withAppendedId(Media.EXTERNAL_CONTENT_URI, id)
                        val albumArtworkUri = ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"), albumId
                        )

                        val file = File(path)
                        if (file.exists())
                            list.add(
                                Song(
                                    uri = contentUri,
                                    id = id,
                                    duration = duration,
                                    title = title,
                                    artist = artist,
                                    path = path,
                                    album = album,
                                    albumId = albumId,
                                    albumArtworkUri = albumArtworkUri.toString()
                                )
                            )
                    }
                } while (cursor.moveToNext())
                cursor.close()
            }
        }
        if (list.size >= 21) {
            Log.d(TAG, "songs found: ${list.size}")
            Log.d(TAG, "songs found: ${list[6].title}")
            Log.d(TAG, "songs found: ${list[6].artist}")
            Log.d(TAG, "songs found: ${list[6].album}")
            Log.d(TAG, "songs found: ${list[6].albumId}")
            Log.d(TAG, "songs found: ${list[6].path}")
            Log.d(TAG, "songs found: ${list.size}")
            Log.d(TAG, "songs found: ${list[7].title}")
            Log.d(TAG, "songs found: ${list[7].artist}")
            Log.d(TAG, "songs found: ${list[7].album}")
            Log.d(TAG, "songs found: ${list[7].albumId}")
            Log.d(TAG, "songs found: ${list[7].path}")
            (0..20).forEach {
                Log.d(TAG, "albumArtworkUri: ${list[it].albumArtworkUri}")
            }
        }
        return list
    }
}