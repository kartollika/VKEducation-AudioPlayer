package kartollika.vkeducation.audioplayer.presentation.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearSnapHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import kartollika.vkeducation.audioplayer.R
import kartollika.vkeducation.audioplayer.common.mocks.getAudioTracksMocks
import kartollika.vkeducation.audioplayer.common.views.AudioTracksCarouselRecyclerView
import kartollika.vkeducation.audioplayer.common.views.SnapOnScrollListener
import kartollika.vkeducation.audioplayer.common.views.attachSnapHelperWithListener
import kartollika.vkeducation.audioplayer.common.views.audio_seekbar.AudioSeekbar
import kartollika.vkeducation.audioplayer.data.models.AudioTrack
import kartollika.vkeducation.audioplayer.player.PlayerService
import kartollika.vkeducation.audioplayer.presentation.player.tracks_list.AudioTracksAdapter
import kotlinx.android.synthetic.main.fragment_audioplayer.*
import kotlinx.android.synthetic.main.fragment_audioplayer.view.*

class PlayerFragment : Fragment() {

    private lateinit var audioSeekbar: AudioSeekbar
    private lateinit var nextTrackActionView: View
    private lateinit var previousTrackActionView: View
    private lateinit var optionsActionView: View
    private lateinit var shuffleTracksActionView: View
    private lateinit var pauseResumeActionView: View
    private lateinit var tracksRecyclerView: AudioTracksCarouselRecyclerView
    private lateinit var tracksAdapter: AudioTracksAdapter
    private lateinit var playerService: PlayerService
    private lateinit var playerView: PlayerView
    private var isPlayerBounded = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            playerService = (binder as PlayerService.AudioPlayerBinder).getService()
            isPlayerBounded = true
            initializeInitialState()

            playerService.addOnPlayerInitListener(object : PlayerService.OnPlayerInitListener {
                override fun onPlayerInit(player: ExoPlayer) {
                    exo_controllers.player = player
                }
            })
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

        //            audioSeekbar = audio_seekbar_view
//            previousTrackActionView = previous_track_view
//            nextTrackActionView = next_track_view
        tracksRecyclerView = view.tracks_recyclerview
//            pauseResumeActionView = exo_play
//            playerView = player_view

        initTracksRecyclerView()
        initListeners()
        return view
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
                }
            })

    }

    private fun initListeners() {
//        previousTrackActionView.setOnClickListener { playerService.previousTrack() }
//        nextTrackActionView.setOnClickListener { playerService.nextTrack() }
//        pauseResumeActionView.setOnClickListener {
//            if (playerService.isNowPlaying()) {
//                playerService.pauseMusic()
//            } else {
//                playerService.resumeMusic()
//            }
//        }
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