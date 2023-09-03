package com.woolenstorm.musicplayer.ui.screens.songDetailsScreen

import android.net.Uri
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.woolenstorm.musicplayer.R
import com.woolenstorm.musicplayer.ui.theme.MusicPlayerTheme
import java.io.FileNotFoundException

@Composable
fun AlbumArtwork(
    artworkUriString: String,
    modifier: Modifier = Modifier
) {
    val artworkUri = Uri.parse(artworkUriString)
    val context = LocalContext.current.applicationContext

    Box(modifier = modifier.aspectRatio(1f)) {
        val source = try {
            MediaStore.Images.Media.getBitmap(
                context.contentResolver,
                artworkUri
            )
        } catch (e: FileNotFoundException) {
            null
        }
        if (source != null) {
            Image(
                bitmap = source.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                painter = painterResource(id = R.drawable.album_artwork_placeholder),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlbumArtworkPreview() {
    MusicPlayerTheme {
        AlbumArtwork("")
    }
}
