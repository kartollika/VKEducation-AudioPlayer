package kartollika.vkeducation.audioplayer.presentation.player.mini

import kartollika.vkeducation.audioplayer.common.mvp.MvpView

interface MiniPlayerContract {
    interface MiniPlayerView : MvpView {
        fun showSongTitle(songTitle: String)
        fun setPreviewAlbum(resource: Any?)
        fun switchToPlayAction()
        fun switchToPauseAction()
        fun hideMiniPlayer()
        fun showMiniPlayer()
        fun updateDurationLeft(durationLeft: String)
        fun changeControlsState(enabled: Boolean)
    }

    interface MiniPlayerPresenter {
        fun onPlayAction()
        fun onPauseAction()
        fun onNextAction()
        fun unregisterMediaController()
    }
}