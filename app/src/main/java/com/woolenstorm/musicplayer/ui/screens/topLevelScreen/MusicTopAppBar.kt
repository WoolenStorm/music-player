package com.woolenstorm.musicplayer.ui.screens.topLevelScreen

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.woolenstorm.musicplayer.R
import com.woolenstorm.musicplayer.ui.AppViewModel
import kotlinx.coroutines.delay
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.woolenstorm.musicplayer.MusicPlayerApplication
import com.woolenstorm.musicplayer.data.SongsRepository
import com.woolenstorm.musicplayer.ui.theme.MusicPlayerTheme

private const val TAG = "MusicTopAppBar"

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MusicTopAppBar(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit = {},
    navigationIcon: @Composable (() -> Unit)? = {}
) {
    var input by remember { mutableStateOf("") }

    val showKeyboard = remember { mutableStateOf(true) }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    TopAppBar(
        modifier = modifier.clickable {
            viewModel.isSearching.value = !viewModel.isSearching.value
            if (!viewModel.isSearching.value) {
                viewModel.filterSongs("")
                keyboard?.hide()
                input = ""
            }
        },
        title =
        if (viewModel.isSearching.value) {
            {
                OutlinedTextField(
                    value = input,
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.song_search_prompt),
                            color = MaterialTheme.colors.secondary,
                        )
                    },
                    modifier = Modifier.focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.h6,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            Log.d(TAG, "onDone()")
                            viewModel.isSearching.value = false
                            viewModel.filterSongs("")
                            keyboard?.hide()
                            input = ""
                        }
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    onValueChange = {
                        input = it
                        viewModel.filterSongs(it)
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colors.primaryVariant,
                        cursorColor = MaterialTheme.colors.primary,
                        textColor = MaterialTheme.colors.primary
                    )
                )
                LaunchedEffect(focusRequester) {
                    if (showKeyboard.value) {
                        focusRequester.requestFocus()
                        delay(100) // Make sure you have delay here
                        keyboard?.show()
                    }
                }
            }
        } else title,
        navigationIcon = navigationIcon,
        backgroundColor = MaterialTheme.colors.primaryVariant,
        contentColor = MaterialTheme.colors.primary,
        actions = {
            Icon(
                painter = painterResource(id = R.drawable.search),
                contentDescription = null,
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(12.dp)
            )
        }
    )
}
