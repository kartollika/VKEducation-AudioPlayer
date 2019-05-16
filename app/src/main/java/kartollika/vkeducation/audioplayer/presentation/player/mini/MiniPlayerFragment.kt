package kartollika.vkeducation.audioplayer.presentation.player.mini

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.Fragment
import android.support.v4.media.session.MediaControllerCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kartollika.vkeducation.audioplayer.R
import kartollika.vkeducation.audioplayer.common.utils.setImageResource
import kartollika.vkeducation.audioplayer.player.PlayerService
import kotlinx.android.synthetic.main.view_mini_player.*

class MiniPlayerFragment : Fragment(), MiniPlayerContract.MiniPlayerView {

    companion object {
        fun newInstance() = MiniPlayerFragment()
    }

    private var isPlayerBounded = false
    private lateinit var presenter: MiniPlayerPresenter

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val playerService = (binder as PlayerService.AudioPlayerBinder).getService()
            val mediaController = MediaControllerCompat(context, binder.getMediaSessionToken())
            presenter.setPlayerService(playerService)
            presenter.setExoPlayer(playerService.getExoPlayer()!!)
            presenter.setMediaController(mediaController)
            isPlayerBounded = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isPlayerBounded = false
        }
    }

    /* ========================
        Lifecycle methods
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = MiniPlayerPresenter(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.view_mini_player, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initActionsListeners()
        songNameTextView.isSelected = true
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        bindPlayerService()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
        unbindPlayerService()
        presenter.unregisterMediaController()
    }

    /* ========================
        Mvp methods
     */

    override fun showSongTitle(songTitle: String) {
        songNameTextView.text = songTitle
    }

    override fun setPreviewAlbum(resource: Any?) {
        if (resource == null) {
            albumPreviewImageView.setImageDrawable(null)
            return
        }
        albumPreviewImageView.setImageResource(resource)
    }

    override fun switchToPlayAction() {
        playPauseActionView.apply {
            setImageResource(R.drawable.ic_play_28)
            setOnClickListener { presenter.onPlayAction() }
        }
    }

    override fun switchToPauseAction() {
        playPauseActionView.apply {
            setImageResource(R.drawable.ic_pause_28)
            setOnClickListener { presenter.onPauseAction() }
        }
    }

    override fun hideMiniPlayer() {
    }

    override fun showMiniPlayer() {
    }

    override fun updateDurationLeft(durationLeft: String) {
        durationSongLengthView?.text = durationLeft
    }

    override fun changeControlsState(enabled: Boolean) {
        nextTrackActionView.isEnabled = enabled
        playPauseActionView.isEnabled = enabled
    }

    /* ========================
        Private methods
     */

    private fun initActionsListeners() {
        playPauseActionView.setOnClickListener { presenter.onPlayAction() }
        nextTrackActionView.setOnClickListener { presenter.onNextAction() }
    }

    private fun unbindPlayerService() {
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