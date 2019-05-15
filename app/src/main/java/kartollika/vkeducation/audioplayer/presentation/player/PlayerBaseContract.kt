package kartollika.vkeducation.audioplayer.presentation.player

import android.support.v4.media.session.MediaControllerCompat
import com.google.android.exoplayer2.ExoPlayer
import kartollika.vkeducation.audioplayer.player.PlayerService

interface PlayerBaseContract {
    interface PlayerBasePresenter {
        fun setExoPlayer(exoPlayer: ExoPlayer)
        fun setPlayerService(playerService: PlayerService)
        fun setMediaController(mediaController: MediaControllerCompat)
    }
}
