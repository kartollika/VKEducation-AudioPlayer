package kartollika.vkeducation.audioplayer.player

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.provider.MediaStore
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kartollika.vkeducation.audioplayer.common.utils.PreferencesUtils
import kartollika.vkeducation.audioplayer.data.models.AudioTrack


class PlayerService : Service() {

    private var binder: Binder = AudioPlayerBinder()
    private var exoPlayer: ExoPlayer? = null
    private val audioTracks: MutableList<AudioTrack> = mutableListOf()
    private var lastPlayedDirectory: String = ""
    private var lastPlayedPosition = -1
    private val preferencesUtils = PreferencesUtils(this)
    private var mediaSource: ConcatenatingMediaSource = ConcatenatingMediaSource()
    private var onTracksChangesListener: OnTracksChangesListener? = null
    private var onPlayerInitListener: OnPlayerInitListener? = null

    private var isCurrentlyPlaying = false

    interface OnTracksChangesListener {
        fun onTracksChanged(tracks: List<AudioTrack>)
    }

    interface OnPlayerInitListener {
        fun onPlayerInit(player: ExoPlayer)
    }

    override fun onBind(intent: Intent?): IBinder? = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        reloadTracks()
        initExoPlayer()
        return START_STICKY
    }

    fun resumeMusic() {
        exoPlayer?.playWhenReady = true
    }

    fun pauseMusic() {
        exoPlayer?.playWhenReady = false
    }

    fun nextTrack() {
        exoPlayer?.next()
    }

    fun previousTrack() {
        exoPlayer?.previous()
    }

    fun isNowPlaying(): Boolean {
        return exoPlayer?.playbackState == Player.STATE_READY && exoPlayer?.playWhenReady!!
    }

    fun invalidateTracks() {
        reloadTracks()
        reloadExoPlayer()
    }

    fun addOnTracksChangedListener(tracksChangesListener: OnTracksChangesListener) {
        this.onTracksChangesListener = tracksChangesListener
    }

    fun getActiveTracks(): List<AudioTrack> = audioTracks.toList()

    private fun reloadTracks() {
        audioTracks.clear()
        lastPlayedDirectory = preferencesUtils.getLastPlayedDirectory()
        lastPlayedPosition = preferencesUtils.getLastPlayedPosition()
        loadTracks()
        onTracksChangesListener?.onTracksChanged(audioTracks)
    }

    private fun loadTracks() {
        val uriQuery = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection =
            MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " + MediaStore.Audio.Media.DATA + " like ?"
        val selectionArgs = arrayOf("%$lastPlayedDirectory%")

        contentResolver.query(
            uriQuery, null, selection, selectionArgs, null
        ).use { cursor ->
            cursor?.let {
                while (cursor.moveToNext()) {
                    val data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val artist =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val title =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val length =
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))

                    audioTracks.add(
                        AudioTrack(
                            artist = artist, title = title, howLong = length, uri = Uri.parse(data)
                        )
                    )
                }
            }
        }
    }

    private fun initExoPlayer() {
        val trackSelection = DefaultTrackSelector()
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelection)
        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "test_name"))

        (exoPlayer as SimpleExoPlayer).addListener(object : Player.EventListener {
            override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
                super.onTimelineChanged(timeline, manifest, reason)
            }

            override fun onPositionDiscontinuity(reason: Int) {
                super.onPositionDiscontinuity(reason)
            }
        })

        reloadTracks()
        for (track in audioTracks) {
            mediaSource.addMediaSource(
                ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(track.uri)
            )
        }

        (exoPlayer as SimpleExoPlayer).prepare(mediaSource)
        resumeMusic()
    }

    private fun reloadExoPlayer() {
        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "test_name"))
        mediaSource.clear()
        for (track in audioTracks) {
            mediaSource.addMediaSource(
                ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(track.uri)
            )
        }
        exoPlayer?.prepare(mediaSource)
    }

    inner class AudioPlayerBinder : Binder() {
        fun getService() = this@PlayerService
    }
}