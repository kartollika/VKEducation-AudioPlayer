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
    private var mediaController: MediaControllerCompat? = null

    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            when (state?.state) {
                PlaybackStateCompat.STATE_SKIPPING_TO_NEXT, PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS -> {
                    tracksRecyclerView.layoutManager?.smoothScrollToPosition(
                        tracksRecyclerView, RecyclerView.State(), state.position.toInt()
                    )
                }

                PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.STATE_STOPPED -> {
                    pausePlayActionView.setImageResource(R.drawable.ic_pause_48)
                }

                PlaybackStateCompat.STATE_PAUSED -> {
                    pausePlayActionView.setImageResource(R.drawable.ic_play_48)
                }
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            songNameTextView.text = metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
                ?: getDefaultNoSongNameCaption()
            artistNameTextView.text = metadata?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
                ?: getDefaultNoArtistCaption()
        }
    }


    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            playerService = (binder as PlayerService.AudioPlayerBinder).getService()
            mediaController = MediaControllerCompat(context, binder.getMediaSessionToken())
            mediaController?.registerCallback(mediaControllerCallback)
            isPlayerBounded = true
            initExoplayerStaff(playerService.getExoPlayer())
            initializeInitialState(mediaController!!)
            changeControlsState(playerService.getActiveTracks().isNotEmpty())
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isPlayerBounded = false
            mediaController?.unregisterCallback(mediaControllerCallback)
            mediaController = null
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
        mediaController?.unregisterCallback(mediaControllerCallback)
        mediaController = null
    }

    private fun initTracksRecyclerView() {
        tracksAdapter = AudioTracksAdapter().apply {
            onSetAudioTracksListener = object : AudioTracksAdapter.OnSetTracksListener {
                override fun onSet(isEmpty: Boolean) {
                    if (isEmpty) {
                        artistNameTextView.text = getDefaultNoArtistCaption()
                        songNameTextView.text = getDefaultNoSongNameCaption()
                    }
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
                        mediaController?.transportControls?.skipToQueueItem(position.toLong())
                    }
                }
            })
    }

    private fun initListeners() {
        previousTrackActionView.setOnClickListener { mediaController?.transportControls?.skipToPrevious() }
        nextTrackActionView.setOnClickListener { mediaController?.transportControls?.skipToNext() }
        pausePlayActionView.setOnClickListener {
            if (mediaController?.playbackState?.state == PlaybackStateCompat.STATE_PLAYING) {
                mediaController?.transportControls?.pause()
            } else {
                mediaController?.transportControls?.play()
            }
        }
    }

    fun initializeInitialState(mediaSessionCompat: MediaControllerCompat) {
        fillTracks()
        setInitialAudioItem(mediaSessionCompat)
    }

    private fun setInitialAudioItem(mediaSessionCompat: MediaControllerCompat) {
        tracksRecyclerView.layoutManager?.smoothScrollToPosition(
            tracksRecyclerView,
            RecyclerView.State(),
            mediaSessionCompat.playbackState?.position?.toInt() ?: 0
        )
        mediaControllerCallback.onPlaybackStateChanged(mediaSessionCompat.playbackState)
        mediaControllerCallback.onMetadataChanged(mediaSessionCompat.metadata)
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

    private fun getDefaultNoArtistCaption() = context?.getString(R.string.no_tracks_title) ?: ""

    private fun getDefaultNoSongNameCaption(): String =
        context?.getString(R.string.no_tracks_summary) ?: ""

}