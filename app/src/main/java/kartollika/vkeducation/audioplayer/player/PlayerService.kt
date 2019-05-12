package kartollika.vkeducation.audioplayer.player

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.app.NotificationCompat.MediaStyle
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kartollika.vkeducation.audioplayer.R


class PlayerService : Service() {

    private var binder: Binder = AudioPlayerBinder()
    private var exoPlayer: SimpleExoPlayer? = null
    private var mediaSource: ConcatenatingMediaSource = ConcatenatingMediaSource()
    private var onTracksChangesListener: OnTracksChangesListener? = null
    private var mediaSessionManager: MediaSessionCompat? = null
    private lateinit var mediaSession: MediaSessionCompat
    private val playerRepository = PlayerRepository()
    private var lastPlayedUri: Uri? = null
    private lateinit var audioManager: AudioManager
    private lateinit var audioFocusRequest: AudioFocusRequest

    private val notificationId = 234
    private val notificationChannel = PlayerService::class.java.name
    private var metadataBuilder = MediaMetadataCompat.Builder()

    private val stateBuilder: PlaybackStateCompat.Builder = Builder().setActions(
        ACTION_PLAY or ACTION_STOP or ACTION_PAUSE or ACTION_PLAY_PAUSE or ACTION_SKIP_TO_NEXT or ACTION_SKIP_TO_PREVIOUS
    )

    private val becomingNoisyBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action.equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                mediaSessionCallbacks.onPause()
            }
        }
    }

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaSessionCallbacks.onPlay()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                mediaSessionCallbacks.onPause()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mediaSessionCallbacks.onPause()
            }

            AudioManager.AUDIOFOCUS_LOSS -> {
                mediaSessionCallbacks.onPause()
            }
        }
    }

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
                        playerRepository.getCurrentIndexAsLong(),
                        1f
                    ).build()
                )
                onPlay()
                updateForegroundNotification()
            }

            override fun onPlay() {
                super.onPlay()

                if (exoPlayer?.playWhenReady == false) {
                    exoPlayer?.playWhenReady = true

                    val currentTrack = playerRepository.getCurrentTrack()
                    updateRelevantMetadata(currentTrack)

                    val audioFocusResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        audioManager.requestAudioFocus(audioFocusRequest)
                    } else {
                        audioManager.requestAudioFocus(
                            audioFocusChangeListener,
                            AudioManager.STREAM_MUSIC,
                            AudioManager.AUDIOFOCUS_GAIN
                        )
                    }


                    if (audioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        return
                    }
                }

                registerReceiver(
                    becomingNoisyBroadcastReceiver,
                    IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
                )

                mediaSession.isActive = true
                mediaSession.setPlaybackState(
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_PLAYING,
                        playerRepository.getCurrentIndexAsLong(),
                        1f
                    ).build()
                )

                updateForegroundNotification()
            }

            override fun onStop() {
                super.onStop()

                if (exoPlayer?.playWhenReady == true) {
                    exoPlayer?.playWhenReady = false
                    unregisterReceiver(becomingNoisyBroadcastReceiver)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    audioManager.abandonAudioFocusRequest(audioFocusRequest)
                } else {
                    audioManager.abandonAudioFocus(audioFocusChangeListener)
                }

                mediaSession.isActive = false
                mediaSession.setPlaybackState(
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_STOPPED,
                        playerRepository.getCurrentIndexAsLong(),
                        1f
                    ).build()
                )

                updateForegroundNotification()
                stopSelf()
            }

            override fun onSkipToQueueItem(id: Long) {
                super.onSkipToQueueItem(id)
                var currentTrack = playerRepository.getCurrentTrack()

                if (id == exoPlayer?.currentWindowIndex?.toLong()) {
                    updateRelevantMetadata(currentTrack)
                    return
                }

                playerRepository.skipTo(id)
                currentTrack = playerRepository.getCurrentTrack()
                updateRelevantMetadata(currentTrack)

                exoPlayer?.seekTo(id.toInt(), 0)
                mediaSession.setPlaybackState(
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM, id, 1f
                    ).build()
                )

                mediaSession.setPlaybackState(
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_PLAYING, id, 1f
                    ).build()
                )
                onPlay()
                updateForegroundNotification()
            }

            override fun onSkipToNext() {
                super.onSkipToNext()
                exoPlayer?.next()

                val nextTrack = playerRepository.getNextTrack()
                updateRelevantMetadata(nextTrack)
                mediaSession.setPlaybackState(
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_SKIPPING_TO_NEXT,
                        playerRepository.getCurrentIndexAsLong(),
                        1f
                    ).build()
                )
                onPlay()
                updateForegroundNotification()
            }

            override fun onPause() {
                super.onPause()

                if (exoPlayer?.playWhenReady == true) {
                    exoPlayer?.playWhenReady = false
                    unregisterReceiver(becomingNoisyBroadcastReceiver)
                }

                mediaSession.setPlaybackState(
                    stateBuilder.setState(
                        PlaybackStateCompat.STATE_PAUSED,
                        playerRepository.getCurrentIndexAsLong(),
                        1f
                    ).build()
                )
                updateForegroundNotification()
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
    }

    interface OnTracksChangesListener {
        fun onTracksChanged(tracks: List<AudioTrack>)
    }

    override fun onBind(intent: Intent?): IBinder? = binder

    override fun onCreate() {
        super.onCreate()
        initMediaSession()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                notificationChannel, "VK Education Player", NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)

            val audioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAcceptsDelayedFocusGain(true)
//                .setWillPauseWhenDucked(true)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .setAudioAttributes(audioAttributes).build()
        }

        val mediaButtonIntent = Intent(
            Intent.ACTION_MEDIA_BUTTON, null, applicationContext, MediaButtonReceiver::class.java
        )
        mediaSession.setMediaButtonReceiver(
            PendingIntent.getBroadcast(
                applicationContext, 0, mediaButtonIntent, 0
            )
        )

        initExoPlayer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.release()
        exoPlayer?.release()
    }

    fun addOnTracksChangedListener(tracksChangesListener: OnTracksChangesListener) {
        this.onTracksChangesListener = tracksChangesListener
    }

    fun getActiveTracks(): List<AudioTrack> = playerRepository.audioTracks

    private fun initMediaSession() {
        if (mediaSessionManager != null) {
            return
        }
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mediaSessionManager = MediaSessionCompat(this, javaClass.name)
        mediaSession = MediaSessionCompat(applicationContext, javaClass.simpleName)
        mediaSession.let {
            it.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS or MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS)
            it.setCallback(mediaSessionCallbacks)
        }
    }

    fun reloadTracks(folderPath: String, tracks: List<AudioTrack>) {
        val folderUri = Uri.parse(folderPath)
//        if (lastPlayedUri != folderUri) {
        lastPlayedUri = folderUri
        playerRepository.audioTracks = tracks
        prepareToPlay()
//        } else {
//            val audioTracksPreviousTagUris =
//                playerRepository.audioTracks.map { audioTrack -> audioTrack.uri }.toMutableList()
//            val newTracksUris = tracks.map { audioTrack -> audioTrack.uri }
//            val toRemove = audioTracksPreviousTagUris.minus(newTracksUris)
//            for (toRemoveTrack in toRemove) {
//                mediaSource.removeMediaSource(toRemove.indexOf(toRemoveTrack))
//            }
//
//            for (i in 0 until audioTracksPreviousTagUris.size) {
//                if (audioTracksPreviousTagUris[i] != newTracksUris[i]) {
//                    mediaSource.addMediaSource(i, newTracksUris[i].makeMediaSource())
//                }
//            }
//            playerRepository.audioTracks = tracks
//        }
        onTracksChangesListener?.onTracksChanged(tracks)
    }

    private lateinit var dataSourceFactory: DefaultDataSourceFactory

    private fun prepareToPlay() {
        dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "test_name"))
        mediaSource.clear()
        for (track in playerRepository.audioTracks) {
            mediaSource.addMediaSource(track.uri.makeMediaSource())
        }
        exoPlayer?.prepare(mediaSource)
    }

    private fun Uri.makeMediaSource(): MediaSource {
        return ExtractorMediaSource.Factory(dataSourceFactory).setTag(this).createMediaSource(this)
    }

    private fun initExoPlayer() {
        val trackSelection = DefaultTrackSelector()
        exoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelection)
        exoPlayer!!.addListener(object : Player.EventListener {

            override fun onPlayerError(error: ExoPlaybackException?) {
                super.onPlayerError(error)
                mediaSessionCallbacks.onSkipToNext()
            }

            override fun onPositionDiscontinuity(reason: Int) {
                super.onPositionDiscontinuity(reason)
                if (reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION) {
                    mediaSessionCallbacks.onSkipToNext()
                }
            }
        })
    }

    fun startPlay() {
        if (playerRepository.audioTracks.isNotEmpty()) {
            mediaSessionCallbacks.onPlay()
        }
    }

    inner class AudioPlayerBinder : Binder() {
        fun getService() = this@PlayerService

        fun getMediaSessionToken(): MediaSessionCompat.Token {
            return mediaSession.sessionToken
        }
    }

    fun getExoPlayer() = exoPlayer

    private fun updateForegroundNotification() {
        val playbackState = mediaSession.controller.playbackState
        when (playbackState.state) {
            PlaybackStateCompat.STATE_PLAYING -> {
                startForeground(
                    12, getNotificationForState(playbackState)
                )
            }

            PlaybackStateCompat.STATE_PAUSED -> {
                NotificationManagerCompat.from(applicationContext)
                    .notify(12, getNotificationForState(playbackState))
                stopForeground(false)
            }

            else -> {
                stopForeground(true)
            }
        }
    }

    private fun getNotificationForState(state: PlaybackStateCompat): Notification {
        val builder =
            PlayerNotificationHelper.instanciateNotificationWithContent(this, mediaSession)

        with(builder) {
            addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_skip_previous_48,
                    "Next",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        applicationContext, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    )
                ).build()
            )

            if (state.state == PlaybackStateCompat.STATE_PLAYING) {
                addAction(
                    NotificationCompat.Action.Builder(
                        R.drawable.ic_pause_28,
                        "Pause",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            applicationContext, PlaybackStateCompat.ACTION_PAUSE
                        )
                    ).build()
                )
            } else {
                addAction(
                    NotificationCompat.Action.Builder(
                        R.drawable.ic_play_28,
                        "Play",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            applicationContext, PlaybackStateCompat.ACTION_PLAY
                        )
                    ).build()
                )
            }

            addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_skip_next_48,
                    "Next",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        applicationContext, PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    )
                ).build()
            )


            setStyle(
                MediaStyle().setShowActionsInCompactView(0, 1, 2).setShowCancelButton(
                    true
                ).setCancelButtonIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        applicationContext, PlaybackStateCompat.ACTION_STOP
                    )
                )
            )
            setSmallIcon(R.mipmap.ic_launcher)
            setOnlyAlertOnce(true)
            priority = NotificationCompat.PRIORITY_HIGH
        }
        return builder.build()
    }
}
