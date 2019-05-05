package kartollika.vkeducation.audioplayer.player

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector

class PlayerService : Service() {

    private var binder: Binder = AudioPlayerBinder()
    private lateinit var exoPlayer: ExoPlayer
//    private val audioTracks: MutableList<> = mutableListOf()

    override fun onBind(intent: Intent?): IBinder? = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        intent?.getParcelableArrayExtra()
        initExoPlayer(this)
        return super.onStartCommand(intent, flags, startId)
    }

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

    inner class AudioPlayerBinder : Binder() {
        fun getService() = this@PlayerService
    }
}