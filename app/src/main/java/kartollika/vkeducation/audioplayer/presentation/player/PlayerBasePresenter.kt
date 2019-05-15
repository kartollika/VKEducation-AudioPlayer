package kartollika.vkeducation.audioplayer.presentation.player

import android.support.v4.media.session.MediaControllerCompat
import com.google.android.exoplayer2.ExoPlayer
import kartollika.vkeducation.audioplayer.common.mvp.BasePresenter
import kartollika.vkeducation.audioplayer.common.mvp.MvpView
import kartollika.vkeducation.audioplayer.player.PlayerService

abstract class PlayerBasePresenter<T : MvpView>(view: T) : BasePresenter<T>(view),
    PlayerBaseContract.PlayerBasePresenter {

    internal lateinit var mediaController: MediaControllerCompat
    internal lateinit var exoPlayer: ExoPlayer
    internal lateinit var playerService: PlayerService

    override fun setPlayerService(playerService: PlayerService) {
        this.playerService = playerService
    }

    override fun setExoPlayer(exoPlayer: ExoPlayer) {
        this.exoPlayer = exoPlayer
    }

    override fun setMediaController(mediaController: MediaControllerCompat) {
        this.mediaController = mediaController
    }
}