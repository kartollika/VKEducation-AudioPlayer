package kartollika.vkeducation.audioplayer.player

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.session.MediaSessionManager
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import android.provider.MediaStore
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util


class PlayerService : Service() {

    private var binder: Binder = AudioPlayerBinder()
    private var exoPlayer: ExoPlayer? = null
    private var mediaSource: ConcatenatingMediaSource = ConcatenatingMediaSource()
    private var onTracksChangesListener: OnTracksChangesListener? = null
    private var mediaSessionManager: MediaSessionManager? = null
    private lateinit var mediaSession: MediaSessionCompat
    private val playerRepository = PlayerRepository()
    private var lastPlayedUri: Uri? = null

    private var metadataBuilder = MediaMetadataCompat.Builder()

    val stateBuilder: PlaybackStateCompat.Builder = Builder().setActions(
        ACTION_PLAY or ACTION_STOP or ACTION_PAUSE or ACTION_PLAY_PAUSE or ACTION_SKIP_TO_NEXT or ACTION_SKIP_TO_PREVIOUS
    )

    private val mediaSessionCallbacks: MediaSessionCompat.Callback =
        object : MediaSessionCompat.Callback() {
            override fun onSkipToPrevious() {
                super.onSkipToPrevious()
                exoPlayer?.previous()

                val previousTrack = playerRepository.getPreviousTrack()
                updateRelevantMetadata(previousTrack)

                mediaSession.setPlaybackState(
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                        1f
                    ).build()
                )
            }

            override fun onPlay() {
                super.onPlay()
                exoPlayer?.playWhenReady = true

                val currentTrack = playerRepository.getCurrentTrack()
                updateRelevantMetadata(currentTrack)
                mediaSession.isActive = true
                mediaSession.setPlaybackState(
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_PLAYING,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                        1f
                    ).build()
                )
            }

            override fun onStop() {
                super.onStop()
                exoPlayer?.playWhenReady = false

                mediaSession.isActive = false
                mediaSession.setPlaybackState(
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_STOPPED,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                        1f
                    ).build()
                )
            }

            override fun onSkipToQueueItem(id: Long) {
                super.onSkipToQueueItem(id)
                exoPlayer?.seekTo(id.toInt(), 0)

                playerRepository.skipTo(id)
                val currentTrack = playerRepository.getCurrentTrack()
                updateRelevantMetadata(currentTrack)
                mediaSession.setPlaybackState(
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM, id, 1f
                    ).build()
                )
            }

            override fun onSkipToNext() {
                super.onSkipToNext()
                exoPlayer?.next()

                val previousTrack = playerRepository.getPreviousTrack()
                updateRelevantMetadata(previousTrack)
                mediaSession.setPlaybackState(
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_SKIPPING_TO_NEXT,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                        1f
                    ).build()
                )
            }

            override fun onPause() {
                super.onPause()
                exoPlayer?.playWhenReady = false

                mediaSession.setPlaybackState(
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_PAUSED,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                        1f
                    ).build()
                )
            }
        }

    private fun updateRelevantMetadata(track: AudioTrack) {
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.artist)
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.title)
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, track.howLong.toLong())
        metadataBuilder.putString(
            MediaMetadataCompat.METADATA_KEY_ART_URI, track.albumArt.toString()
        )
        mediaSession.setMetadata(metadataBuilder.build())
        resetMetadataBuilder()
    }

    private fun resetMetadataBuilder() {
        metadataBuilder = MediaMetadataCompat.Builder()
    }

    interface OnTracksChangesListener {
        fun onTracksChanged(tracks: List<AudioTrack>)
    }

    interface OnPlayerInitListener {
        fun onPlayerInit(player: ExoPlayer)
    }

    override fun onBind(intent: Intent?): IBinder? = binder

    override fun onCreate() {
        super.onCreate()
        initMediaSession()
        initExoPlayer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.release()
    }

    /* fun resumeMusic() {
         exoPlayer?.playWhenReady = true
     }
 */
    /*fun pauseMusic() {
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

    fun isNowPlaying(): Boolean {
        return exoPlayer?.playbackState == Player.STATE_READY && exoPlayer?.playWhenReady!!
    }*/

//    fun invalidateTracks() {
//        reloadTracks()
//        reloadExoPlayer()
//    }

    fun addOnTracksChangedListener(tracksChangesListener: OnTracksChangesListener) {
        this.onTracksChangesListener = tracksChangesListener
    }

    fun getActiveTracks(): List<AudioTrack> = playerRepository.audioTracks

    private fun initMediaSession() {
        if (mediaSessionManager != null) return
        mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        mediaSession = MediaSessionCompat(applicationContext, javaClass.simpleName)
        mediaSession.let {
            it.setCallback(mediaSessionCallbacks)
            it.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS or MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS)
        }
    }

    fun reloadTracks(tracks: List<AudioTrack>) {
        playerRepository.audioTracks = tracks
        onTracksChangesListener?.onTracksChanged(tracks)
        prepareToPlay()
    }

    fun setSourceUri(uri: Uri) {
        lastPlayedUri = uri
    }

    private fun prepareToPlay() {
        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "test_name"))
        mediaSource.clear()
        for (track in playerRepository.audioTracks) {
            mediaSource.addMediaSource(
                ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(track.uri)
            )
        }
        /*val dataSpec = DataSpec(uri)
        val fileDataSource = FileDataSource()
        try {
            fileDataSource.open(dataSpec)
        } catch (e: FileDataSource.FileDataSourceException) {
            e.printStackTrace()
        }

        val factory: DataSource.Factory = DataSource.Factory { fileDataSource }
        val audioSource = ExtractorMediaSource(
            fileDataSource.uri, factory, DefaultExtractorsFactory(), null, null
        )*/
        exoPlayer?.prepare(mediaSource)
    }

    /*private fun loadTracks(): List<AudioTrack> {
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
    }*/

    private fun initExoPlayer() {
        val trackSelection = DefaultTrackSelector()
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelection)
//        reloadTracks()
        /*for (track in audioTracks) {
            mediaSource.addMediaSource(
                ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(track.uri)
            )
        }

        (exoPlayer as SimpleExoPlayer).prepare(mediaSource)
        resumeMusic()*/
    }

    fun startPlay() {
        mediaSessionCallbacks.onPlay()
    }

/*
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
*/

    inner class AudioPlayerBinder : Binder() {
        fun getService() = this@PlayerService

        fun getMediaSessionToken(): MediaSessionCompat.Token {
            return mediaSession.sessionToken
        }
    }
}