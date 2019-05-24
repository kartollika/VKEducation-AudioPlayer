package kartollika.vkeducation.audioplayer.presentation.player.full

import com.google.android.exoplayer2.ExoPlayer
import kartollika.vkeducation.audioplayer.common.mvp.MvpView
import kartollika.vkeducation.audioplayer.player.AudioTrack

interface PlayerContract {
    interface PlayerView : MvpView {
        fun scrollCarouselToPosition(position: Int)
        fun showSongTitle(songTitle: String)
        fun connectExoPlayerWithControllers(exoPlayer: ExoPlayer)
        fun showArtistName(artistName: String)
        fun setPreviewAlbum(resource: Any)
        fun switchToPlayAction()
        fun switchToPauseAction()
        fun fillActiveTracks(tracks: List<AudioTrack>)
        fun changeControlsState(enabled: Boolean)
        fun showDummyArtistAndSong()
    }

    interface PlayerPresenter {
        fun onShuffleAction()
        fun onAddAction()
        fun onRepeatAction()
        fun onMoreOptionsAction()
        fun onPauseAction()
        fun onPlayAction()
        fun onPreviousAction()
        fun onNextAction()
        fun onSkipToQueueItem(position: Int)
        fun unregisterMediaController()
        fun onDestroy()
    }
}