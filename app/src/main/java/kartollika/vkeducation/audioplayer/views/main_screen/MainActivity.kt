package kartollika.vkeducation.audioplayer.views.main_screen

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import kartollika.vkeducation.audioplayer.player.PlayerBinder
import kartollika.vkeducation.audioplayer.player.PlayerService

class MainActivity : AppCompatActivity() {

    private var playerService: PlayerService? = null
    private var isPlayerBounded = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            isPlayerBounded = false
        }

        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            playerService = (binder as PlayerBinder).playerService
            isPlayerBounded = true
        }
    }

    override fun onStart() {
        super.onStart()
        bindPlayerService()
    }

    override fun onStop() {
        super.onStop()
        unbindPlayerService()
    }

    private fun unbindPlayerService() {
        if (isPlayerBounded) {
            unbindService(serviceConnection)
            isPlayerBounded = false
        }
    }

    private fun bindPlayerService() {
        val playerServiceIntent = getPlayerServiceIntent()
        bindService(playerServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun getPlayerServiceIntent() = Intent(this, PlayerService::class.java)
}