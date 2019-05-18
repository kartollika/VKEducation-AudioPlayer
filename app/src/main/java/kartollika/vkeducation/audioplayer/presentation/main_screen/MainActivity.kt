package kartollika.vkeducation.audioplayer.presentation.main_screen

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import kartollika.vkeducation.audioplayer.common.utils.PreferencesUtils
import kartollika.vkeducation.audioplayer.common.utils.onRenderFinished
import kartollika.vkeducation.audioplayer.player.PlayerService
import kartollika.vkeducation.audioplayer.presentation.folder_chooser.FolderChooserActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), MainActivityContract.MainActivityView {

    companion object {
        private const val PERMISSION_GRANT_VIA_SETTINGS_REQUEST_CODE = 100
        private const val PERMISSION_REQUEST_CODE = 101
    }

    private lateinit var presenter: MainActivityPresenter
    private var playerService: PlayerService? = null
    private var isPlayerBounded = false
    private var binder: Binder? = null
    private var isPlayerExpanded = false

    private var serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            this@MainActivity.binder = binder as Binder?
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

        if (savedInstanceState != null) {
            isPlayerBounded = savedInstanceState.getBoolean("ServiceState")
            isPlayerExpanded = savedInstanceState.getBoolean("PlayerExpanded")
        }
        setContentView(kartollika.vkeducation.audioplayer.R.layout.activity_main)

        openFolderActionView.setOnClickListener {
            presenter.onOpenFolderAction()
        }

        initializeFloatingBottomPlayer()
        bindPlayerService()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindPlayerService()
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putBoolean("ServiceState", isPlayerBounded)
        savedInstanceState.putBoolean("PlayerExpanded", floatingBottomPlayerView.isExpanded())
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FolderChooserActivity.FOLDER_CHOOSE_REQUEST_CODE -> {
                super.onActivityResult(requestCode, resultCode, data)

                if (resultCode == Activity.RESULT_OK) {
                    val folder = data?.getStringExtra("chosen_folder") ?: return
                    PreferencesUtils(this).saveLastPlayedDirectory(folder)
                    playerService?.reloadPlayTracksFromOutsize(folder)
                }
            }
            PERMISSION_GRANT_VIA_SETTINGS_REQUEST_CODE -> {
                checkStoragePermission()
            }
        }
    }

    override fun onBackPressed() {
        if (floatingBottomPlayerView.isExpanded()) {
            floatingBottomPlayerView.collapseSheet()
            return
        }
        super.onBackPressed()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.size == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                presenter.onOpenFolderStoragePermissionGranted()
                bindPlayerService()
            }

            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this, Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    createStoragePermissionDialog().show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun openFolderSelectView() {
        val intent = Intent(this, FolderChooserActivity::class.java)
        startActivityForResult(intent, FolderChooserActivity.FOLDER_CHOOSE_REQUEST_CODE)
    }

    private fun initializeFloatingBottomPlayer() {
        floatingBottomPlayerView.apply {
            initPlayerFragment(supportFragmentManager)
            initMiniPlayerFragment(supportFragmentManager)
            addCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(p0: View, p1: Float) {
                }

                override fun onStateChanged(p0: View, p1: Int) {
                }
            })

            onRenderFinished(floatingBottomPlayerView, Runnable {
                runOnUiThread {
                    if (isPlayerExpanded) {
                        expandSheet()
                    } else {
                        collapseSheet()
                    }
                }
            })
        }
    }

    private fun bindPlayerService() {
        bindService(getPlayerServiceIntent(), serviceConnection, Context.BIND_AUTO_CREATE)
        isPlayerBounded = true
    }

    private fun unbindPlayerService() {
        if (isPlayerBounded) {
            unbindService(serviceConnection)
            isPlayerBounded = false
        }
    }

    private fun getPlayerServiceIntent() = Intent(this, PlayerService::class.java)

    override fun checkStoragePermission() {
        if (isStoragePermissionGranted()) {
            presenter.onOpenFolderStoragePermissionGranted()
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                AlertDialog.Builder(this).setTitle("Разрешение")
                    .setMessage("Для выбора разделя для проигрывания аудио необходимо предоставить разрешение приложению")
                    .setPositiveButton("Ok") { _, _ ->
                        run {
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                                PERMISSION_REQUEST_CODE
                            )
                        }
                    }.create().show()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun isStoragePermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createStoragePermissionDialog(): AlertDialog {
        return AlertDialog.Builder(this).setTitle("Разрешение")
            .setMessage("Для выбора разделя для проигрывания аудио необходимо вручную предоставить разрешение приложению")
            .setPositiveButton("Настройки") { _, _ ->
                openApplicationSettings(PERMISSION_GRANT_VIA_SETTINGS_REQUEST_CODE)
            }.setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }.create()
    }

    private fun openApplicationSettings(requestCode: Int) {
        val appSettingsIntent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")
        )
        startActivityForResult(appSettingsIntent, requestCode)
    }
}