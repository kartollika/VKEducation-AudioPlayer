package kartollika.vkeducation.audioplayer.views.main_screen

import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kartollika.vkeducation.audioplayer.R
import kartollika.vkeducation.audioplayer.views.player_view.FloatingBottomPlayer
import kotlinx.android.synthetic.main.activity_main.*

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
                window.statusBarColor =
                    ContextCompat.getColor(this@MainActivity, android.R.color.black)
            }
        })


    }

    override fun openFolderSelectView() {
    }
}