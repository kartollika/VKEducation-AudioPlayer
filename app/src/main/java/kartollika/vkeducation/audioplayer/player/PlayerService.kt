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
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
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
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kartollika.vkeducation.audioplayer.R
import kartollika.vkeducation.audioplayer.presentation.main_screen.MainActivity


class PlayerService : Service() {

    private var binder: Binder = AudioPlayerBinder()
    private var exoPlayer: SimpleExoPlayer? = null
    private var mediaSource: ConcatenatingMediaSource = ConcatenatingMediaSource()
    private var mediaSessionManager: MediaSessionCompat? = null
    private lateinit var mediaSession: MediaSessionCompat
    private val playerRepository = PlayerRepository()
    private var lastPlayedDirectory: String = ""
    private lateinit var audioManager: AudioManager
    private lateinit var audioFocusRequest: AudioFocusRequest
    private val notificationId = 234
    private val notificationChannelId = PlayerService::class.java.name
    private var metadataBuilder = MediaMetadataCompat.Builder()
    private val tracksChangesListeners: MutableList<OnTracksChangesListener> = mutableListOf()
    private val tracksLoader = TracksLoader()

    inner class AudioPlayerBinder : Binder() {
        fun getService() = this@PlayerService

        fun getMediaSessionToken(): MediaSessionCompat.Token {
            return mediaSession.sessionToken
        }
    }

    private val stateBuilder: Builder = Builder().setActions(
        ACTION_PLAY or ACTION_STOP or ACTION_PAUSE or ACTION_PLAY_PAUSE or ACTION_SKIP_TO_NEXT or ACTION_SKIP_TO_PREVIOUS or ACTION_PLAY_FROM_URI
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

            override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
                super.onPlayFromUri(uri, extras)
                startLoadingAudiosData(uri)
            }

            @Suppress("DEPRECATION")
            override fun onPlay() {
                super.onPlay()

                if (exoPlayer?.playWhenReady == false) {
                    exoPlayer?.playWhenReady = true

                    exoPlayer?.currentWindowIndex?.let { currentIndex ->
                        val currentTag = mediaSource.getMediaSource(currentIndex).tag!!
                        playerRepository.getTrackByTag(currentTag)?.let { audio ->
                            updateRelevantMetadata(audio)
                        }

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
                    registerNoisyReceiver()
                }

                mediaSession.isActive = true

                mediaSession.setPlaybackState(
                    stateBuilder.setState(
                        STATE_PLAYING, playerRepository.getCurrentIndexAsLong(), 1f
                    ).build()
                )

                updateForegroundNotification()
            }

            override fun onPause() {
                super.onPause()

                if (exoPlayer?.playWhenReady == true) {
                    exoPlayer?.playWhenReady = false
                    unregisterNoisyReceiver()
                }

                mediaSession.setPlaybackState(
                    stateBuilder.setState(
                        STATE_PAUSED, playerRepository.getCurrentIndexAsLong(), 1f
                    ).build()
                )
                updateForegroundNotification()
            }

            @Suppress("DEPRECATION")
            override fun onStop() {
                super.onStop()

                if (exoPlayer?.playWhenReady == true) {
                    exoPlayer?.playWhenReady = false
                    unregisterNoisyReceiver()
                }

                mediaSession.isActive = false

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    audioManager.abandonAudioFocusRequest(audioFocusRequest)
                } else {
                    audioManager.abandonAudioFocus(audioFocusChangeListener)
                }

                mediaSession.setPlaybackState(
                    stateBuilder.setState(
                        STATE_STOPPED, playerRepository.getCurrentIndexAsLong(), 1f
                    ).build()
                )

                updateForegroundNotification()
                stopSelf()
            }

            override fun onSkipToPrevious() {
                super.onSkipToPrevious()
                exoPlayer?.previous()

                exoPlayer?.currentWindowIndex?.let { previousIndex ->
                    playerRepository.skipTo(previousIndex)

                    val previousTag = mediaSource.getMediaSource(previousIndex).tag!!
                    playerRepository.getTrackByTag(previousTag)?.let { audio ->
                        updateRelevantMetadata(audio)
                    }

                    mediaSession.setPlaybackState(
                        stateBuilder.setState(
                            STATE_SKIPPING_TO_PREVIOUS, playerRepository.getCurrentIndexAsLong(), 1f
                        ).build()
                    )
                    onPlay()
                }
            }

            override fun onSkipToQueueItem(id: Long) {
                super.onSkipToQueueItem(id)

                if (id == exoPlayer?.currentWindowIndex?.toLong()) {
                    val skippedTag =
                        mediaSource.getMediaSource(exoPlayer?.currentWindowIndex!!).tag!!
                    playerRepository.getTrackByTag(skippedTag)?.let { audio ->
                        updateRelevantMetadata(audio)
                    }
                    return
                }

                exoPlayer?.seekTo(id.toInt(), 0)
                exoPlayer?.currentWindowIndex?.let { skippedIndex ->
                    playerRepository.skipTo(skippedIndex)

                    val skippedTag = mediaSource.getMediaSource(skippedIndex).tag!!
                    playerRepository.getTrackByTag(skippedTag)?.let { audio ->
                        updateRelevantMetadata(audio)
                    }

                    mediaSession.setPlaybackState(
                        stateBuilder.setState(
                            STATE_SKIPPING_TO_QUEUE_ITEM, id, 1f
                        ).build()
                    )

                    mediaSession.setPlaybackState(
                        stateBuilder.setState(
                            STATE_PLAYING, id, 1f
                        ).build()
                    )
                    onPlay()
                    updateForegroundNotification()
                }
            }

            override fun onSkipToNext() {
                super.onSkipToNext()
                exoPlayer?.next()

                exoPlayer?.currentWindowIndex?.let { nextIndex ->
                    playerRepository.skipTo(nextIndex)

                    val nextTag = mediaSource.getMediaSource(nextIndex).tag!!
                    playerRepository.getTrackByTag(nextTag)?.let { audio ->
                        updateRelevantMetadata(audio)
                    }

                    mediaSession.setPlaybackState(
                        stateBuilder.setState(
                            STATE_SKIPPING_TO_NEXT, playerRepository.getCurrentIndexAsLong(), 1f
                        ).build()
                    )
                    onPlay()
                }
            }

            private fun startLoadingAudiosData(uri: Uri?) {
                val path = uri.toString()
                val uriQuery = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                val selection =
                    MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " + MediaStore.Audio.Media.DATA + " like ? AND " + MediaStore.Audio.Media.DATA + " NOT LIKE ? "
                val selectionArgs = arrayOf("%$path%", "%$path/%/%")

                tracksLoader.apply {
                    initializeLoader(
                        applicationContext, uriQuery, null, selection, selectionArgs, null
                    )
                    setOnLoadListener(object : TracksLoader.OnQueryListener {
                        override fun onQuery(tracks: List<AudioTrack>) {
                            reloadTracks(path, tracks)
                            if (tracks.isNotEmpty()) {
                                onPlay()
                                return
                            }

                            if (tracks.isEmpty()) {
                                onStop()
                                return
                            }
                        }
                    })
                    startLoading()
                }
            }
        }

    private fun updateRelevantMetadata(track: AudioTrack) {
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.artist)
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.title)
        metadataBuilder.putLong(
            MediaMetadataCompat.METADATA_KEY_DURATION, track.howLong.toLong()
        )

        metadataBuilder.putString(
            MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, track.albumArt.toString()
        )
        mediaSession.setMetadata(metadataBuilder.build())
    }

    interface OnTracksChangesListener {
        fun onTracksChanged(tracks: List<AudioTrack>)
    }

    override fun onBind(intent: Intent?): IBinder? = binder

    private lateinit var notificationChannel: NotificationChannel

    override fun onCreate() {
        super.onCreate()
        initMediaSession()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(
                notificationChannelId, "VK Education Player", NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)

            val audioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .setAudioAttributes(audioAttributes).build()

            val notification =
                NotificationCompat.Builder(this, notificationChannelId).setContentTitle("")
                    .setContentText("").build()

            startForeground(notificationId, notification)
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
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.release()
        exoPlayer?.release()
        tracksLoader.stopLoading()
    }

    fun addOnTracksChangedListener(tracksChangesListener: OnTracksChangesListener) {
        tracksChangesListeners.add(tracksChangesListener)
        tracksChangesListener.onTracksChanged(getActiveTracks())
    }

    fun removeOnTracksChangedListener(listener: OnTracksChangesListener) {
        tracksChangesListeners.remove(listener)
    }

    private var validActiveTracks: List<AudioTrack> = mutableListOf()

    fun getActiveTracks(): List<AudioTrack> = validActiveTracks

    private fun invalidateValidTracks() {
        val tracks = mutableListOf<AudioTrack>()
        for (i in 0 until mediaSource.size) {
            playerRepository.getTrackByTag(mediaSource.getMediaSource(i).tag.toString())?.let {
                tracks.add(
                    it
                )
            }
        }
        validActiveTracks = tracks
    }

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

    fun reloadPlayTracksFromOutsize(folder: String) {
        mediaSessionCallbacks.onPlayFromUri(Uri.parse(folder), null)
    }

    enum class ReloadType {
        Cold, Hot
    }

    private fun registerNoisyReceiver() {
        registerReceiver(
            becomingNoisyBroadcastReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        )
    }

    private fun unregisterNoisyReceiver() {
        unregisterReceiver(becomingNoisyBroadcastReceiver)
    }

    private fun reloadTracks(folderPath: String, tracks: List<AudioTrack>): ReloadType {
        val reloadType = if (folderPath != lastPlayedDirectory) {
            if (exoPlayer?.playWhenReady == true) {
                mediaSessionCallbacks.onPause()
            }

            lastPlayedDirectory = folderPath
            mediaSource = doColdTracksReload(tracks)
            prepareToPlay(mediaSource)
            ReloadType.Cold
        } else {
            doHotTracksReload(tracks)
            ReloadType.Hot
        }
        playerRepository.audioTracks =
            tracks.associateBy { audioTrack: AudioTrack -> audioTrack.uri.toString() }
        invalidateValidTracks()
        notifyAllOnTracksChangedListeners()
        return reloadType
    }

    private fun notifyAllOnTracksChangedListeners() {
        val activeTracks = getActiveTracks()
        tracksChangesListeners.forEach { it.onTracksChanged(activeTracks) }
    }

    private fun doColdTracksReload(tracks: List<AudioTrack>): ConcatenatingMediaSource {
        val mediaSource = ConcatenatingMediaSource()
        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "test_name"))
        for (track in tracks) {
            mediaSource.addMediaSource(track.uri.makeMediaSource(dataSourceFactory))
        }
        return mediaSource
    }

    private fun doHotTracksReload(tracks: List<AudioTrack>): ConcatenatingMediaSource {
        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "test_name"))
        val mediaSourceTags = mutableMapOf<Int, String>()
        for (i in 0 until mediaSource.size) {
            mediaSourceTags[i] = mediaSource.getMediaSource(i).tag.toString()
        }

        val newSourceTags = mutableMapOf<Int, String>()
        for (i in 0 until tracks.size) {
            newSourceTags[i] = tracks[i].uri.toString()
        }

        var k = 0
        for (i in 0 until mediaSourceTags.size) {
            if (!newSourceTags.values.contains(mediaSourceTags[i])) {
                mediaSource.removeMediaSource(k)
            } else {
                k++
            }
        }

        for (i in 0 until newSourceTags.size) {
            if (!mediaSourceTags.values.contains(newSourceTags[i])) {
                mediaSource.addMediaSource(
                    Uri.parse(newSourceTags[i].toString()).makeMediaSource(
                        dataSourceFactory
                    )
                )
            }
        }
        return mediaSource
    }

    private fun prepareToPlay(mediaSource: MediaSource) {
        exoPlayer?.prepare(mediaSource)
    }

    private fun Uri.makeMediaSource(dataSourceFactory: DataSource.Factory): MediaSource {
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

    fun getExoPlayer() = exoPlayer

    private fun updateForegroundNotification() {
        val playbackState = mediaSession.controller.playbackState
        when (playbackState.state) {
            STATE_PLAYING -> {
                startForeground(
                    notificationId, getNotificationForState(playbackState)
                )
            }

            STATE_PAUSED -> {
                NotificationManagerCompat.from(applicationContext)
                    .notify(notificationId, getNotificationForState(playbackState))
            }

            else -> {
                stopForeground(true)
            }
        }
    }

    private fun getNotificationForState(state: PlaybackStateCompat): Notification {
        val builder =
            PlayerNotificationHelper.instantiateNotificationWithContent(this, mediaSession)

        with(builder) {
            addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_skip_previous_48,
                    "Next",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        applicationContext, ACTION_SKIP_TO_PREVIOUS
                    )
                ).build()
            )

            if (state.state == STATE_PLAYING) {
                addAction(
                    NotificationCompat.Action.Builder(
                        R.drawable.ic_pause_28,
                        "Pause",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            applicationContext, ACTION_PAUSE
                        )
                    ).build()
                )
            } else {
                addAction(
                    NotificationCompat.Action.Builder(
                        R.drawable.ic_play_28,
                        "Play",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            applicationContext, ACTION_PLAY
                        )
                    ).build()
                )
            }

            addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_skip_next_48,
                    "Next",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        applicationContext, ACTION_SKIP_TO_NEXT
                    )
                ).build()
            )

            addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_close_black_24dp,
                    "Close",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        applicationContext, ACTION_STOP
                    )
                ).build()
            )

            setStyle(
                MediaStyle().setShowActionsInCompactView(0, 1, 2, 3).setShowCancelButton(
                    true
                ).setCancelButtonIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        applicationContext, ACTION_STOP
                    )
                )
            )
            setOnlyAlertOnce(true)
            setSmallIcon(R.mipmap.ic_launcher).setShowWhen(false)
            setContentIntent(
                PendingIntent.getActivity(
                    applicationContext,
                    0,
                    Intent(applicationContext, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setChannelId(notificationChannelId)
            }
            priority = NotificationCompat.PRIORITY_HIGH
        }
        return builder.build()
    }
}
