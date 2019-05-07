package kartollika.vkeducation.audioplayer.views.main_screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.os.IBinder
import android.provider.MediaStore
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.app.AppCompatActivity
import android.view.View
import kartollika.vkeducation.audioplayer.R
import kartollika.vkeducation.audioplayer.common.utils.PreferencesUtils
import kartollika.vkeducation.audioplayer.data.models.AudioTrack
import kartollika.vkeducation.audioplayer.player.PlayerService
import kartollika.vkeducation.audioplayer.views.player_view.FloatingBottomPlayer
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), MainActivityContract.MainActivityView,
    LoaderManager.LoaderCallbacks<Cursor> {

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
        setContentView(kartollika.vkeducation.audioplayer.R.layout.activity_main)

        open_folder_button.setOnClickListener {
            presenter.onOpenFolderAction()
        }

        floationBottomPlayer = floating_player
        floationBottomPlayer.initPlayerFragment(supportFragmentManager)
        floationBottomPlayer.addCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {

            }

            override fun onStateChanged(p0: View, p1: Int) {
            }
        })

        if (!isPlayerBounded) {
            bindPlayerService()
        }
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putBoolean("ServiceState", isPlayerBounded)
        super.onSaveInstanceState(savedInstanceState)
    }

    public override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        isPlayerBounded = savedInstanceState.getBoolean("ServiceState")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isPlayerBounded) {
            unbindService(serviceConnection)
            playerService?.stopSelf()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 9999) {
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    return
                }

                val filePath = data!!.data.path
                val fileName = data!!.data.lastPathSegment
                val lastPos = filePath.length - fileName.length
                val folder = filePath.substring(0, lastPos)
                val folderName: String =
                    data.data.pathSegments[data.data.pathSegments.lastIndex - 1] ?: ""
                PreferencesUtils(this).saveLastPlayedDirectory(folder)

                LoaderManager.getInstance(this)
                    .initLoader<Cursor>(taskId, Bundle.EMPTY, this@MainActivity)
            }
//            Log.d("files_test", filePath)
//            Log.d("files_test", folder)
//            Log.d("files_test", fileName)
//            loadAudiosFrom(folderName)
//            loadAudiosFrom(data?.data)
        }
    }

    override fun openFolderSelectView() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        startActivityForResult(intent, 9999)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val lastDirectory = PreferencesUtils(this).getLastPlayedDirectory()
        val uriQuery = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection =
            MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " + MediaStore.Audio.Media.DATA + " like ?"
        val selectionArgs = arrayOf("%$lastDirectory%")
        return CursorLoader(this, uriQuery, null, selection, selectionArgs, null)
    }

    override fun onLoadFinished(p0: Loader<Cursor>, p1: Cursor?) {
        playerService?.invalidateTracks()
        playerService?.playMusic()
        //        val tracks = parseAudioTracks(cursor)
//        playerService.invalidatePlayer()
    }

    override fun onLoaderReset(p0: Loader<Cursor>) {
    }

    private fun unbindPlayerService() {
        if (isPlayerBounded) {
            unbindService(serviceConnection)
            isPlayerBounded = false
        }
    }

    private fun bindPlayerService() {
        val playerServiceIntent = getPlayerServiceIntent()
        startService(playerServiceIntent)
        bindService(playerServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun getPlayerServiceIntent() = Intent(this, PlayerService::class.java)

    private fun parseAudioTracks(cursor: Cursor?): List<AudioTrack> {
        val tracks = mutableListOf<AudioTrack>()
        cursor?.let {
            while (cursor.moveToNext()) {
                val data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                val artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                val title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                val length = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))

                tracks.add(
                    AudioTrack(
                        artist = artist, title = title, howLong = length, uri = Uri.parse(data)
                    )
                )
            }
        }
        return tracks
    }

    override fun checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
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
                                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100
                            )
                        }
                    }.create().show()
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 100 && grantResults.size == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                presenter.onOpenFolderStoragePermissionGranted()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 101) {
            checkStoragePermission()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun createStoragePermissionDialog(): AlertDialog {
        return AlertDialog.Builder(this).setTitle("Разрешение")
            .setMessage("Для выбора разделя для проигрывания аудио необходимо вручную предоставить разрешение приложению")
            .setPositiveButton("Настройки") { _, _ -> openApplicationSettings(101) }
            .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }.create()
    }

    private fun openApplicationSettings(requestCode: Int) {
        val appSettingsIntent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")
        )
        startActivityForResult(appSettingsIntent, requestCode)
    }

}