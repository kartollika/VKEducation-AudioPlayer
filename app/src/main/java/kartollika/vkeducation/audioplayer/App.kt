package kartollika.vkeducation.audioplayer

import android.app.Application
import android.content.Intent
import kartollika.vkeducation.audioplayer.player.PlayerService

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val playerServiceIntent = Intent(this, PlayerService::class.java)
        startService(playerServiceIntent)

    }
}