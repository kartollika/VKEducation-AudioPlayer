package kartollika.vkeducation.audioplayer.presentation.player.full

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.Fragment
import android.support.v4.media.session.MediaControllerCompat
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

class PlayerFragment : Fragment(), PlayerContract.PlayerView {

    companion object {
        fun newInstance() = PlayerFragment()
    }

    private lateinit var tracksAdapter: AudioTracksAdapter
    private lateinit var playerService: PlayerService
    private var isPlayerBounded = false

    private lateinit var presenter: PlayerPresenter

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            playerService = (binder as PlayerService.AudioPlayerBinder).getService()
            val mediaController = MediaControllerCompat(context, binder.getMediaSessionToken())
            presenter.initializeMediaController(mediaController)
            presenter.setExoPlayer(playerService.getExoPlayer()!!)
            presenter.setPlayerService(playerService)
            isPlayerBounded = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isPlayerBounded = false
            presenter.unregisterMediaController()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = PlayerPresenter(this)
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

        songNameTextView.isSelected = true
        artistNameTextView.isSelected = true
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService()
        presenter.unregisterMediaController()
    }

    private fun initTracksRecyclerView() {
        tracksAdapter = AudioTracksAdapter()
        tracksRecyclerView.setupAdapter(tracksAdapter)
        tracksRecyclerView.attachSnapHelperWithListener(LinearSnapHelper().apply {
            attachToRecyclerView(tracksRecyclerView)
        },
            SnapOnScrollListener.Behavior.NOTIFY_ON_SCROLL_IDLE,
            object : SnapOnScrollListener.OnSnapPositionChangeListener {
                override fun onSnapPositionChange(position: Int) {
                    presenter.onSkipToQueueItem(position)
                }
            })
    }

    private fun initListeners() {
        previousTrackActionView.setOnClickListener { presenter.onPreviousAction() }
        nextTrackActionView.setOnClickListener { presenter.onNextAction() }
        pausePlayActionView.setOnClickListener { presenter.onPlayAction() }
        shuffleActionView.setOnClickListener { presenter.onShuffleAction() }
        repeatActionView.setOnClickListener { presenter.onRepeatAction() }
        moreActionsActionView.setOnClickListener { presenter.onMoreOptionsAction() }
        addActionView.setOnClickListener { presenter.onAddAction() }
    }

    override fun changeControlsState(enabled: Boolean) {
        previousTrackActionView.isEnabled = enabled
        nextTrackActionView.isEnabled = enabled
        pausePlayActionView.isEnabled = enabled
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

    /* ========================
        View methods
     */

    override fun scrollCarouselToPosition(position: Int) {
        tracksRecyclerView.layoutManager?.smoothScrollToPosition(
            tracksRecyclerView, RecyclerView.State(), position
        )
    }

    override fun connectExoPlayerWithControllers(exoPlayer: ExoPlayer) {
        exo_controllers.player = exoPlayer
    }

    override fun showSongTitle(songTitle: String) {
        songNameTextView.text = songTitle
    }

    override fun showArtist(artistName: String) {
        artistNameTextView.text = artistName
    }

    override fun setPreviewAlbum(resource: Any) {
    }

    override fun switchToPlayAction() {
        pausePlayActionView.apply {
            setImageResource(R.drawable.ic_play_48)
            setOnClickListener { presenter.onPlayAction() }
        }
    }

    override fun switchToPauseAction() {
        pausePlayActionView.apply {
            setImageResource(R.drawable.ic_pause_48)
            setOnClickListener { presenter.onPauseAction() }
        }
    }

    override fun fillActiveTracks(tracks: List<AudioTrack>) {
        tracksAdapter.apply {
            audioTracks = tracks
            notifyDataSetChanged()
        }
    }

    override fun showDummyArtistAndSong() {
        showArtist(getDefaultNoArtistCaption())
        showSongTitle(getDefaultNoSongNameCaption())
    }

    private fun getPlayerServiceIntent() = Intent(activity, PlayerService::class.java)

    private fun getDefaultNoArtistCaption() = context?.getString(R.string.no_tracks_title) ?: ""

    private fun getDefaultNoSongNameCaption(): String =
        context?.getString(R.string.no_tracks_summary) ?: ""

}