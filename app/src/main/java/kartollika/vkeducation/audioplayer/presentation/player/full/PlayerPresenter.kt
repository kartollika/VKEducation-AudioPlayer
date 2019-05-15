package kartollika.vkeducation.audioplayer.presentation.player.full

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.ExoPlayer
import kartollika.vkeducation.audioplayer.player.AudioTrack
import kartollika.vkeducation.audioplayer.player.PlayerService
import kartollika.vkeducation.audioplayer.presentation.player.PlayerBasePresenter

class PlayerPresenter(view: PlayerContract.PlayerView) :
    PlayerBasePresenter<PlayerContract.PlayerView>(view), PlayerContract.PlayerPresenter {

    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            when (state?.state) {
                PlaybackStateCompat.STATE_SKIPPING_TO_NEXT, PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS -> {
                    view.scrollCarouselToPosition(state.position.toInt())
                }

                PlaybackStateCompat.STATE_STOPPED -> {

                }

                PlaybackStateCompat.STATE_PLAYING -> {
                    view.switchToPauseAction()
                }

                PlaybackStateCompat.STATE_PAUSED -> {
                    view.switchToPlayAction()
                }
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            view.showSongTitle(metadata?.getString(MediaMetadataCompat.METADATA_KEY_TITLE) ?: "")
            view.showArtistName(metadata?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST) ?: "")
        }
    }

    override fun setMediaController(mediaController: MediaControllerCompat) {
        super.setMediaController(mediaController)
        mediaController.registerCallback(mediaControllerCallback)
    }

    override fun setPlayerService(playerService: PlayerService) {
        super.setPlayerService(playerService)
        playerService.addOnTracksChangedListener(object : PlayerService.OnTracksChangesListener {
            override fun onTracksChanged(tracks: List<AudioTrack>) {
                view.fillActiveTracks(tracks)
                view.changeControlsState(tracks.isNotEmpty())
                if (tracks.isEmpty()) {
                    view.showDummyArtistAndSong()
                }
            }
        })
        view.changeControlsState(playerService.getActiveTracks().isNotEmpty())
    }

    override fun setExoPlayer(exoPlayer: ExoPlayer) {
        super.setExoPlayer(exoPlayer)
        view.connectExoPlayerWithControllers(exoPlayer)
        setInitialPlayerState()
    }

    override fun onShuffleAction() {
    }

    override fun onAddAction() {
    }

    override fun onRepeatAction() {
    }

    override fun onMoreOptionsAction() {
    }

    override fun onPauseAction() {
        mediaController.transportControls.pause()
    }

    override fun onPlayAction() {
        mediaController.transportControls.play()
    }

    override fun onPreviousAction() {
        mediaController.transportControls.skipToPrevious()
    }

    override fun onNextAction() {
        mediaController.transportControls.skipToNext()
    }

    override fun onSkipToQueueItem(position: Int) {
        mediaController.transportControls?.skipToQueueItem(position.toLong())
    }

    override fun unregisterMediaController() {
        mediaController.unregisterCallback(mediaControllerCallback)
    }

    private fun setInitialPlayerState() {
        view.scrollCarouselToPosition(exoPlayer.currentWindowIndex)
        mediaControllerCallback.onPlaybackStateChanged(mediaController.playbackState)
        mediaControllerCallback.onMetadataChanged(mediaController.metadata)
    }
}