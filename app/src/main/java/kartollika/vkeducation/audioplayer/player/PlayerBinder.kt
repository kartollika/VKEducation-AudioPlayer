package kartollika.vkeducation.audioplayer.player

import android.content.Context
import android.os.Binder
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector

class PlayerBinder(val playerService: PlayerService) : Binder() {

    private lateinit var exoPlayer: ExoPlayer

    fun playMusic() {
        exoPlayer.playWhenReady = true
    }

    fun pauseMusic() {
        exoPlayer.playWhenReady = false
    }

    fun nextTrack() {
        exoPlayer.next()
    }

    fun previousTrack() {
        exoPlayer.previous()
    }

    private fun initExoPlayer(context: Context) {
        val trackSelection = DefaultTrackSelector()
        exoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelection)

        // TODO() PREPARE PLAYER
    }
}