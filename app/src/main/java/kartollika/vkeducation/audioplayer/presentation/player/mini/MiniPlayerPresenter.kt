package kartollika.vkeducation.audioplayer.presentation.player.mini

import android.annotation.SuppressLint
import android.os.Handler
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.Player
import kartollika.vkeducation.audioplayer.R
import kartollika.vkeducation.audioplayer.common.utils.parseIntToLength
import kartollika.vkeducation.audioplayer.player.AudioTrack
import kartollika.vkeducation.audioplayer.player.PlayerService
import kartollika.vkeducation.audioplayer.presentation.player.PlayerBasePresenter

class MiniPlayerPresenter(view: MiniPlayerContract.MiniPlayerView) :
    PlayerBasePresenter<MiniPlayerContract.MiniPlayerView>(view),
    MiniPlayerContract.MiniPlayerPresenter {

    private val handler = Handler()
    private val updateSongLeftRunnable = Runnable { updateSongLeft() }
    private lateinit var onTracksChangesListener: PlayerService.OnTracksChangesListener

    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            updateSongLeft()
            when (state?.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    view.switchToPauseAction()
                }
                PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.STATE_STOPPED -> {
                    view.switchToPlayAction()
                }
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            if (metadata == null) {
                view.showSongTitle("")
                view.setPreviewAlbum(null)
            } else {
                view.showSongTitle(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE))
                view.setPreviewAlbum(
                    metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)
                        ?: R.drawable.images
                )
            }
        }
    }

    override fun setMediaController(mediaController: MediaControllerCompat) {
        super.setMediaController(mediaController)
        mediaController.registerCallback(mediaControllerCallback)
        setInitialPlayerState()
    }

    override fun setPlayerService(playerService: PlayerService) {
        super.setPlayerService(playerService)
        onTracksChangesListener = object : PlayerService.OnTracksChangesListener {
            override fun onTracksChanged(tracks: List<AudioTrack>) {
                view.changeControlsState(tracks.isNotEmpty())
                if (tracks.isEmpty()) {
                    view.hideMiniPlayer()
                    mediaControllerCallback.onMetadataChanged(null)
                }
            }
        }
        playerService.addOnTracksChangedListener(onTracksChangesListener)
        view.changeControlsState(playerService.getActiveTracks().isNotEmpty())
    }

    override fun onPlayAction() {
        mediaController?.transportControls?.play()
    }

    override fun onPauseAction() {
        mediaController?.transportControls?.pause()
    }

    override fun onNextAction() {
        mediaController?.transportControls?.skipToNext()
    }

    override fun unregisterMediaController() {
        mediaController?.unregisterCallback(mediaControllerCallback)
        mediaController = null
    }

    @SuppressLint("SetTextI18n")
    private fun updateSongLeft() {
        handler.removeCallbacks(updateSongLeftRunnable)
        val playbackState = exoPlayer?.playbackState

        if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {

            if (playbackState == Player.STATE_READY) {
                val newSongLeftTime =
                    (exoPlayer?.duration ?: 0L) - (exoPlayer?.currentPosition ?: 0L)
                view.updateDurationLeft(
                    "-${newSongLeftTime.parseIntToLength()}"
                )
            }

            var delayMs = 1000 - (exoPlayer?.currentPosition ?: 0 % 1000)
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

    override fun onDestroy() {
        playerService?.removeOnTracksChangedListener(onTracksChangesListener)
    }

    private fun setInitialPlayerState() {
        mediaControllerCallback.onPlaybackStateChanged(mediaController?.playbackState)
        mediaControllerCallback.onMetadataChanged(mediaController?.metadata)
    }
}
