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
import android.provider.MediaStore
import android.provider.Settings
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import kartollika.vkeducation.audioplayer.player.AudioTrack
import kartollika.vkeducation.audioplayer.player.PlayerService
import kartollika.vkeducation.audioplayer.presentation.folder_chooser.FolderChooserActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), MainActivityContract.MainActivityView {

    private lateinit var presenter: MainActivityPresenter
    private var lastPlayedPath: String = ""
    private var playerService: PlayerService? = null
    private var isPlayerBounded = false
    private var binder: Binder? = null

    private var serviceConnection: ServiceConnection? = object : ServiceConnection {
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
            lastPlayedPath = savedInstanceState.getString("LastPlayedPath") ?: ""
        }
        setContentView(kartollika.vkeducation.audioplayer.R.layout.activity_main)

        openFolderActionView.setOnClickListener {
            presenter.onOpenFolderAction()
        }

        floatingBottomPlayerView.initPlayerFragment(supportFragmentManager)
        floatingBottomPlayerView.initMiniPlayerFragment(supportFragmentManager)
        floatingBottomPlayerView.addCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {

            }

            override fun onStateChanged(p0: View, p1: Int) {
            }
        })
        bindPlayerService()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbind()
//        if (isPlayerBounded) {
//            unbindService(serviceConnection)
//            isPlayerBounded = false
//        }
//        serviceConnection = null
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putBoolean("ServiceState", isPlayerBounded)
        savedInstanceState.putString("LastPlayedPath", lastPlayedPath)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            9999 -> {
                super.onActivityResult(requestCode, resultCode, data)

                if (resultCode == Activity.RESULT_OK) {
                    val folder = data?.getStringExtra("chosen_folder") ?: return
                    lastPlayedPath = folder
                    queryMusic(lastPlayedPath, object : OnQueryCompleteListener {
                        override fun onQueryComplete(tracks: List<AudioTrack>) {
                            if (playerService?.getActiveTracks() != tracks) {
                                playerService?.reloadTracks(folder, tracks)
                                playerService?.startPlay()
                            }
                        }
                    })
//                    LoaderManager.getInstance(this)
//                        .restartLoader<Cursor>(taskId, Bundle.EMPTY, this@MainActivity)
                }
            }
            101 -> {
                checkStoragePermission()
            }
        }
    }

    override fun openFolderSelectView() {
        val intent = Intent(this, FolderChooserActivity::class.java)
        startActivityForResult(intent, 9999)
    }

    private interface OnQueryCompleteListener {
        fun onQueryComplete(tracks: List<AudioTrack>)
    }

    private fun queryMusic(fromFolder: String, listener: OnQueryCompleteListener) {
        val uriQuery = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection =
            MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " + MediaStore.Audio.Media.DATA + " like ? AND " + MediaStore.Audio.Media.DATA + " NOT LIKE ? "
        val selectionArgs = arrayOf("%$fromFolder%", "%$fromFolder/%/%")
        contentResolver.query(uriQuery, null, selection, selectionArgs, null)?.use { cursor ->
            val tracks = mutableListOf<AudioTrack>()
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

            listener.onQueryComplete(tracks)

//            if (tracks != playerService?.getActiveTracks() && tracks.size != 0) {
//                playerService?.reloadTracks(lastPlayedPath, tracks)
//                playerService?.startPlay()
//            }
        }
    }

    /* override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
         val uriQuery = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
         val selection =
             MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " + MediaStore.Audio.Media.DATA + " like ? AND " + MediaStore.Audio.Media.DATA + " NOT LIKE ? "
         val selectionArgs = arrayOf("%$lastPlayedPath%", "%$lastPlayedPath/%/%")
         return CursorLoader(this, uriQuery, null, selection, selectionArgs, null)
     }

     override fun onLoadFinished(p0: Loader<Cursor>, cursor: Cursor?) {
         val tracks = mutableListOf<AudioTrack>()
         if (cursor == null) {
             return
         }

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

         if (tracks != playerService?.getActiveTracks() && tracks.size != 0) {
             playerService?.reloadTracks(lastPlayedPath, tracks)
             playerService?.startPlay()
         }
     }

     override fun onLoaderReset(p0: Loader<Cursor>) {
     }*/

//    private fun unbindPlayerService() {
//        if (isPlayerBounded) {
//            unbindService(serviceConnection)
//            isPlayerBounded = false
//        }
//}

    private fun bindPlayerService() {
        bindService(getPlayerServiceIntent(), serviceConnection, Context.BIND_AUTO_CREATE)
        isPlayerBounded = true
    }

    private fun unbind() {
        if (isPlayerBounded) {
            // Detach our existing connection.
            unbindService(serviceConnection);
            isPlayerBounded = false;
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

//    override fun getBinder(listener: OnBinderResultListener): Binder? {
//    }

    private fun isStoragePermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
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