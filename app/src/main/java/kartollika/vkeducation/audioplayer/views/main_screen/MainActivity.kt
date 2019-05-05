package kartollika.vkeducation.audioplayer.views.main_screen

import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kartollika.vkeducation.audioplayer.R
import kartollika.vkeducation.audioplayer.views.player_view.FloatingBottomPlayer
import kotlinx.android.synthetic.main.activity_main.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import kartollika.vkeducation.audioplayer.player.PlayerBinder
import kartollika.vkeducation.audioplayer.player.PlayerService

class MainActivity : AppCompatActivity(), MainActivityContract.MainActivityView {

    private lateinit var presenter: MainActivityPresenter
    private lateinit var floationBottomPlayer: FloatingBottomPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        presenter = MainActivityPresenter(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        open_folder_button.setOnClickListener {
            Toast.makeText(this, "Test", Toast.LENGTH_LONG).show()
        }
        floationBottomPlayer = floating_player
        floationBottomPlayer.initPlayerFragment(supportFragmentManager)

        floationBottomPlayer.addCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {

            }

            override fun onStateChanged(p0: View, p1: Int) {
            }
        })


    }

    override fun openFolderSelectView() {
    }

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