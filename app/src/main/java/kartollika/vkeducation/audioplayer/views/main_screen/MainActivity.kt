package kartollika.vkeducation.audioplayer.views.main_screen

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kartollika.vkeducation.audioplayer.R
import kartollika.vkeducation.audioplayer.data.models.AudioTrack
import kartollika.vkeducation.audioplayer.player.PlayerService
import kartollika.vkeducation.audioplayer.views.player_view.FloatingBottomPlayer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), MainActivityContract.MainActivityView {

    private lateinit var presenter: MainActivityPresenter
    private lateinit var floationBottomPlayer: FloatingBottomPlayer

    private var playerService: PlayerService? = null
    private var isPlayerBounded = false
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            playerService = (binder as PlayerService.AudioPlayerBinder).getService()
            isPlayerBounded = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isPlayerBounded = false
        }
    }

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

//        loadAudios()
    }

    override fun openFolderSelectView() {
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
        if (!isPlayerBounded) {
            val playerServiceIntent = getPlayerServiceIntent()
            playerServiceIntent.putExtra("tracks", loadAudios())
            startService(playerServiceIntent)
            bindService(playerServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun getPlayerServiceIntent() = Intent(this, PlayerService::class.java)

    private fun loadAudios(): Array<AudioTrack> {
        val tracks = mutableListOf<AudioTrack>()
        val contentResolver = contentResolver
        val uriQuery = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        contentResolver.query(uriQuery, null, selection, null, null)
            .use { cursor ->
                cursor?.let {
                    while (cursor.moveToNext()) {
                        val data =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                        val artist =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                        val title =
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                        val length =
                            cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))

                        tracks.add(
                            AudioTrack(
                                artist = artist,
                                title = title,
                                howLong = length,
                                uri = Uri.parse(data)
                            )
                        )
                    }
                }
            }
        return tracks.toTypedArray()
    }
}