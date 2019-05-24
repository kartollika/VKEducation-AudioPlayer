package kartollika.vkeducation.audioplayer.player

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import kartollika.vkeducation.audioplayer.common.utils.getRandomPreviewImage

class TracksLoader {

    interface OnQueryListener {
        fun onQuery(tracks: List<AudioTrack>)
    }

    private var cursorLoader: CursorLoader? = null
    private var listener: OnQueryListener? = null

    private val loaderListener: Loader.OnLoadCompleteListener<Cursor?> =
        Loader.OnLoadCompleteListener { _, cursor ->
            val tracks = mutableListOf<AudioTrack>()
            cursor?.let {
                while (cursor.moveToNext()) {
                    val data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
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
                            uri = Uri.parse(data),
                            albumArt = getRandomPreviewImage()
                        )
                    )
                }
            }
            listener?.onQuery(tracks)
        }

    @Suppress("UNUSED_PARAMETER")
    fun initializeLoader(
        context: Context,
        uriQuery: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?) {
        cursorLoader = CursorLoader(context, uriQuery, null, selection, selectionArgs, null)
    }

    fun startLoading() {
        if (cursorLoader != null) {
            cursorLoader!!.registerListener(1, loaderListener)
            cursorLoader!!.startLoading()
        }
    }

    fun stopLoading() {
        if (cursorLoader != null) {
            cursorLoader!!.unregisterListener(loaderListener)
            cursorLoader!!.cancelLoad()
            cursorLoader!!.stopLoading()
        }
    }

    fun setOnLoadListener(listener: OnQueryListener) {
        this.listener = listener
    }

}