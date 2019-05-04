package kartollika.vkeducation.audioplayer.views.main_screen

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kartollika.vkeducation.audioplayer.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), MainActivityContract.MainActivityView {

    private lateinit var presenter: MainActivityPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        presenter = MainActivityPresenter(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        open_folder_button.setOnClickListener { Toast.makeText(this, "Test", Toast.LENGTH_LONG).show() }
    }

    override fun openFolderSelectView() {
    }
}