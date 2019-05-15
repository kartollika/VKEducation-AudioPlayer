package kartollika.vkeducation.audioplayer

import android.app.Application
import android.content.Intent
import android.os.Build
import kartollika.vkeducation.audioplayer.player.PlayerService

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        startPlayerService()
    }

    private fun startPlayerService() {
        val playerServiceIntent = Intent(this, PlayerService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(playerServiceIntent)
        } else {
            startService(playerServiceIntent)
        }
    }
}