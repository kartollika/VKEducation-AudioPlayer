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
import android.support.v7.widget.PagerSnapHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kartollika.vkeducation.audioplayer.R
import kartollika.vkeducation.audioplayer.common.mocks.getAudioTracksMocks
import kartollika.vkeducation.audioplayer.common.views.AudioTracksCarouselRecyclerView
import kartollika.vkeducation.audioplayer.common.views.audio_seekbar.AudioSeekbar
import kartollika.vkeducation.audioplayer.player.AudioTrack
import kartollika.vkeducation.audioplayer.player.PlayerService
import kartollika.vkeducation.audioplayer.presentation.player.tracks_list.AudioTracksAdapter
import kotlinx.android.synthetic.main.fragment_audioplayer.view.*


class PlayerFragment : Fragment() {

    private lateinit var audioSeekbar: AudioSeekbar
    private lateinit var nextTrackActionView: View
    private lateinit var previousTrackActionView: View
    private lateinit var optionsActionView: View
    private lateinit var shuffleTracksActionView: View
    private lateinit var tracksRecyclerView: AudioTracksCarouselRecyclerView
    private lateinit var tracksAdapter: AudioTracksAdapter
    private lateinit var playerService: PlayerService
    private var isPlayerBounded = false
    private lateinit var mediaController: MediaControllerCompat

    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            playerService = (binder as PlayerService.AudioPlayerBinder).getService()
            mediaController = MediaControllerCompat(context, binder.getMediaSessionToken())
            mediaController.registerCallback(object : MediaControllerCompat.Callback() {})
            isPlayerBounded = true
            initializeInitialState()
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
        val view = inflater.inflate(R.layout.fragment_audioplayer, null)

        audioSeekbar = view.audio_seekbar_view
        previousTrackActionView = view.previous_track_view
        nextTrackActionView = view.next_track_view
        tracksRecyclerView = view.tracks_recyclerview

        tracksAdapter = AudioTracksAdapter(getAudioTracksMocks())
        tracksRecyclerView.setupAdapter(tracksAdapter)
        PagerSnapHelper().apply {
            attachToRecyclerView(tracksRecyclerView)
        }
        return view
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
                }
            }
        })

        tracksAdapter.apply {
            audioTracks = currentTracks
            notifyDataSetChanged()
        }
    }

    private fun bindPlayerService() {
        val playerServiceIntent = getPlayerServiceIntent()
        context?.bindService(playerServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun getPlayerServiceIntent() = Intent(context, PlayerService::class.java)

}