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
import com.google.android.exoplayer2.ExoPlayer
import kartollika.vkeducation.audioplayer.R
import kartollika.vkeducation.audioplayer.common.mocks.getAudioTracksMocks
import kartollika.vkeducation.audioplayer.common.views.SnapOnScrollListener
import kartollika.vkeducation.audioplayer.common.views.attachSnapHelperWithListener
import kartollika.vkeducation.audioplayer.player.AudioTrack
import kartollika.vkeducation.audioplayer.player.PlayerService
import kartollika.vkeducation.audioplayer.presentation.player.tracks_list.AudioTracksAdapter
import kotlinx.android.synthetic.main.fragment_audioplayer.*

class PlayerFragment : Fragment() {

    companion object {
        fun newInstance() = PlayerFragment()
    }

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
                }

                PlaybackStateCompat.STATE_PAUSED -> {
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
            initExoplayerStaff(playerService.getExoPlayer())
            changeControlsState(playerService.getActiveTracks().isNotEmpty())
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isPlayerBounded = false
        }
    }

    private fun initExoplayerStaff(exoPlayer: ExoPlayer?) {
        exo_controllers.player = exoPlayer
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        bindPlayerService()
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

    override fun onDestroy() {
        super.onDestroy()
        unbindService()
    }

    private fun initTracksRecyclerView() {
        tracksAdapter = AudioTracksAdapter(getAudioTracksMocks()).apply {
            onSetAudioTracksListener = object : AudioTracksAdapter.OnSetTracksListener {
                override fun onSet(isEmpty: Boolean) {
                    changeControlsState(!isEmpty)
                }
            }
        }
        tracksRecyclerView.setupAdapter(tracksAdapter)
        tracksRecyclerView.attachSnapHelperWithListener(LinearSnapHelper().apply {
            attachToRecyclerView(tracksRecyclerView)
        },
            SnapOnScrollListener.Behavior.NOTIFY_ON_SCROLL_IDLE,
            object : SnapOnScrollListener.OnSnapPositionChangeListener {
                override fun onSnapPositionChange(position: Int) {
                    if (playerService.getActiveTracks().isNotEmpty()) {
                        mediaController.transportControls.skipToQueueItem(position.toLong())
                    }
                }
            })
    }

    private fun initListeners() {
        previousTrackActionView.setOnClickListener { mediaController.transportControls.skipToPrevious() }
        nextTrackActionView.setOnClickListener { mediaController.transportControls.skipToNext() }
        pausePlayActionView.setOnClickListener {
            if (mediaController.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                mediaController.transportControls.pause()
                pausePlayActionView.setImageResource(R.drawable.ic_play_48)
            } else if (mediaController.playbackState.state == PlaybackStateCompat.STATE_PAUSED) {
                mediaController.transportControls.play()
                pausePlayActionView.setImageResource(R.drawable.ic_pause_48)
            }
        }
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
                    changeControlsState(!tracks.isEmpty())
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
        pausePlayActionView.isEnabled = isPlaylistEmpty
    }

    private fun unbindService() {
        activity?.unbindService(serviceConnection)
        isPlayerBounded = false
    }

    private fun bindPlayerService() {
        val playerServiceIntent = getPlayerServiceIntent()
        activity?.bindService(playerServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        isPlayerBounded = true
    }

    private fun getPlayerServiceIntent() = Intent(activity, PlayerService::class.java)

}