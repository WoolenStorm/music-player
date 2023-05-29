# MusicPlayer
An app, which allows you to listen to local audio files without ads.

Initially created as a pet project for a "hey can you build an app for me" friend.

## Features

* Sorts all of your local audio files alphabetically
* Allows fast forwarding or rewinding of the currently playing song
* Play/Pause/Play Next controls are also available via Bluetooth Earphones 
* Create playlist / listen to a playlist
* Absolutely pseudo-random shuffle :)
* Audio files deletion (be careful, it works more often than it doesn't)
* Supports Dark Theme
* Adaptive Layout based on device dimensions/orientation
* en/ru localization

## UI Structure

                MainActivity.kt
                      |
                MusicPlayerApp.kt
                   /     \
    AddPlaylistDialog     TopLevelScreen
                             /        \
                   NavBar/NavRail     AppContent (also SongDetailsScreen if horizontal)
                                      /   |   \
                              TopAppBar   |   CurrentPlayingSong (at the bottom of the screen)
                                     HomeScreen (DeleteSongDialog)
                                     PlaylistsScreen (DeleteItemDialog)
                                     PlaylistDetailsScreen
                                     EditPlaylistScreen

## Logical Structure (singleton-inspired)

                       SongsRepository 
                (contains PlaylistDatabase db, 
                          MusicPlayer player, 
                          MutableList<Song> songs)
                      /                       \
                    /                           \
              AppViewModel                    PlaybackService + ControlsReceiver
    (gets a SongsRepository object)         (gets a SongsRepository object)
    (used to control logic through UI)      (used to control logic through Notification or Bluetooth,
                                                                                  both via Broadcasts)
                                                          
## Reflective thoughts and lessons learned

Well, the app was intended for a specific use case (namely listening to **LOCAL AUDIO FILES ONLY**). 

This assumption kind of prohibited me of implementing clever architecture desicions regarding possible scalability in the future.

If I had to rewrite the code for this app now, I would definitely pay more attention to **PLANNING** and **CLEAR MODULARIZATION** via interfaces.

I would also first do my research on which media technologies are out there rather then sticking to the initially chosen one (I used basic MediaPlayer class instead of much more feature-rich ExoPlayer).

On the other hand, it might have been simply fear of "analysis-paralysis": I was afraid of not getting anywhere with my "ideas" and wanted to get to the "minimal viable product" as soon as possible 
(at the time I had about three free weeks between my winter term and summer term).
