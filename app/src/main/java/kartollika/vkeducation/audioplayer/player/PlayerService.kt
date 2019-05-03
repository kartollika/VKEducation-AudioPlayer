package kartollika.vkeducation.audioplayer.player

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class PlayerService : Service() {

    private var binder: Binder = PlayerBinder(this)

    override fun onBind(intent: Intent?): IBinder? = binder
}