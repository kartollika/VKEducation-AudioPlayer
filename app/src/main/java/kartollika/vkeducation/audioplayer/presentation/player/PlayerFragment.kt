package kartollika.vkeducation.audioplayer.presentation.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.Fragment
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.widget.LinearSnapHelper
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kartollika.vkeducation.audioplayer.R
import kartollika.vkeducation.audioplayer.common.mocks.getAudioTracksMocks
import kartollika.vkeducation.audioplayer.common.views.SnapOnScrollListener
import kartollika.vkeducation.audioplayer.common.views.attachSnapHelperWithListener
import kartollika.vkeducation.audioplayer.player.AudioTrack
import kartollika.vkeducation.audioplayer.player.PlayerService
import kartollika.vkeducation.audioplayer.presentation.player.tracks_list.AudioTracksAdapter
import kotlinx.android.synthetic.main.fragment_audioplayer.*


class PlayerFragment : Fragment() {

    private lateinit var tracksAdapter: AudioTracksAdapter
    private lateinit var playerService: PlayerService
    private var isPlayerBounded = false
    private lateinit var mediaController: MediaControllerCompat

    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            when (state?.state) {
                PlaybackStateCompat.STATE_SKIPPING_TO_NEXT, PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS -> {
                    tracksRecyclerView.layoutManager?.smoothScrollToPosition(
                        tracksRecyclerView, RecyclerView.State(), state.position.toInt()
                    )
                }
                PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM -> {
                }
                PlaybackStateCompat.STATE_PLAYING -> {
                    pauseActionView.visibility = View.VISIBLE
                    playActionView.visibility = View.GONE
                }

                PlaybackStateCompat.STATE_PAUSED -> {
                    pauseActionView.visibility = View.GONE
                    playActionView.visibility = View.VISIBLE
                }
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            playerService = (binder as PlayerService.AudioPlayerBinder).getService()
            mediaController = MediaControllerCompat(context, binder.getMediaSessionToken())
            mediaController.registerCallback(mediaControllerCallback)
            isPlayerBounded = true
            initializeInitialState()
            changeControlsState(playerService.getActiveTracks().isNotEmpty())
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isPlayerBounded = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isPlayerBounded) {
            bindPlayerService()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_audioplayer, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTracksRecyclerView()

        initListeners()
    }

    private fun initTracksRecyclerView() {
        tracksAdapter = AudioTracksAdapter(getAudioTracksMocks())
        tracksRecyclerView.setupAdapter(tracksAdapter)
        tracksRecyclerView.attachSnapHelperWithListener(LinearSnapHelper().apply {
            attachToRecyclerView(tracksRecyclerView)
        },
            SnapOnScrollListener.Behavior.NOTIFY_ON_SCROLL_IDLE,
            object : SnapOnScrollListener.OnSnapPositionChangeListener {
                override fun onSnapPositionChange(position: Int) {
                    mediaController.transportControls.skipToQueueItem(position.toLong())
                }
            })
    }

    private fun initListeners() {
        previousTrackActionView.setOnClickListener { mediaController.transportControls.skipToPrevious() }
        nextTrackActionView.setOnClickListener { mediaController.transportControls.skipToNext() }
        pauseActionView.setOnClickListener { mediaController.transportControls.pause() }
        playActionView.setOnClickListener { mediaController.transportControls.play() }
    }

    fun initializeInitialState() {
        fillTracks()
    }

    private fun fillTracks() {
        val currentTracks = playerService.getActiveTracks()
        playerService.addOnTracksChangedListener(object : PlayerService.OnTracksChangesListener {
            override fun onTracksChanged(tracks: List<AudioTrack>) {
                tracksAdapter.apply {
                    audioTracks = tracks
                    notifyDataSetChanged()


                    changeControlsState(tracks.isEmpty())
                }
            }
        })

        tracksAdapter.apply {
            audioTracks = currentTracks
            notifyDataSetChanged()
        }
    }

    fun changeControlsState(isPlaylistEmpty: Boolean) {
        previousTrackActionView.isEnabled = isPlaylistEmpty
        nextTrackActionView.isEnabled = isPlaylistEmpty
        pauseActionView.isEnabled = isPlaylistEmpty
        playActionView.isEnabled = isPlaylistEmpty
    }

    private fun bindPlayerService() {
        val playerServiceIntent = getPlayerServiceIntent()
        context?.bindService(playerServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun getPlayerServiceIntent() = Intent(context, PlayerService::class.java)

}