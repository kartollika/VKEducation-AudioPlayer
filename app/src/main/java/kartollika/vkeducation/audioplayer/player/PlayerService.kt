package kartollika.vkeducation.audioplayer.player

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kartollika.vkeducation.audioplayer.data.models.AudioTrack

class PlayerService : Service() {

    private var binder: Binder = AudioPlayerBinder()
    private lateinit var exoPlayer: ExoPlayer
    private val audioTracks: MutableList<AudioTrack> = mutableListOf()

    override fun onBind(intent: Intent?): IBinder? = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val iterator = intent?.getParcelableArrayExtra("tracks")!!.toList().iterator()
        while (iterator.hasNext()) {
            audioTracks.add(iterator.next() as AudioTrack)
        }

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
        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "test_name"))

        val mediaSource = ConcatenatingMediaSource()
        for (track in audioTracks) {
            mediaSource.addMediaSource(
                ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(track.uri)
            )
        }
        exoPlayer.prepare(mediaSource)
        playMusic()

        Handler().postDelayed({ nextTrack() }, 2000L)
    }

    inner class AudioPlayerBinder : Binder() {
        fun getService() = this@PlayerService
    }
}