package kartollika.vkeducation.audioplayer.presentation.player.mini

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v4.app.Fragment
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import kartollika.vkeducation.audioplayer.R
import kartollika.vkeducation.audioplayer.common.utils.parseIntToLength
import kartollika.vkeducation.audioplayer.common.utils.setImageResource
import kartollika.vkeducation.audioplayer.player.PlayerService
import kotlinx.android.synthetic.main.view_mini_player.*

class MiniPlayerFragment : Fragment() {

    companion object {
        fun newInstance() = MiniPlayerFragment()
    }

    private var isPlayerBounded = false
    private lateinit var mediaController: MediaControllerCompat
    private var exoPlayer: ExoPlayer? = null
    private val handler: Handler = Handler()

    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            when (state?.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    playPauseActionView.setImageResource(R.drawable.ic_pause_28)
                }
                PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.STATE_STOPPED -> {
                    playPauseActionView.setImageResource(R.drawable.ic_play_28)
                }
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            metadata?.let {
                songNameTextView.text = it.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
                albumPreviewImageView.setImageResource(
                    it.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)
                        ?: R.drawable.images
                )
            }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            playerService = (binder as PlayerService.AudioPlayerBinder).getService()
            initPlayerViews(playerService?.getExoPlayer())
            mediaController = MediaControllerCompat(context, binder.getMediaSessionToken())
            mediaController.registerCallback(mediaControllerCallback)
            isPlayerBounded = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isPlayerBounded = false
        }
    }

    private fun initPlayerViews(exoPlayer: ExoPlayer?) {
        this.exoPlayer = exoPlayer
        exoPlayer?.addListener(object : Player.EventListener {

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                super.onPlayerStateChanged(playWhenReady, playbackState)
                updateSongLeft()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun updateSongLeft() {
        handler.removeCallbacks(updateSongLeftRunnable)
        val playbackState = if (exoPlayer == null) {
            Player.STATE_IDLE
        } else {
            exoPlayer?.playbackState
        }

        durationSongLengthView?.text =
            "-${(exoPlayer!!.duration - exoPlayer!!.currentPosition).parseIntToLength()}"

        if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
            var delayMs = 1000 - (exoPlayer!!.currentPosition % 1000)
            if (delayMs < 200) {
                delayMs += 1000
            } else {
                delayMs = 1000
            }
            handler.postDelayed(
                updateSongLeftRunnable, delayMs
            )
        }
    }

    private val updateSongLeftRunnable = Runnable { updateSongLeft() }

    private var playerService: PlayerService? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.view_mini_player, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playPauseActionView.setOnClickListener {
            if (mediaController.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                mediaController.transportControls.pause()
            } else {
                mediaController.transportControls.play()
            }
        }
        nextTrackActionView.setOnClickListener { mediaController.transportControls.skipToNext() }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        bindPlayerService()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService()
        mediaController.unregisterCallback(mediaControllerCallback)
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