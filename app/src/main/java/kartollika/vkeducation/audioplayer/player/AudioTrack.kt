package kartollika.vkeducation.audioplayer.player

import android.net.Uri
import kartollika.vkeducation.audioplayer.R

data class AudioTrack(
    val title: String = "",
    val artist: String = "",
    val howLong: Int = 0,
    val isExplicit: Boolean = false,
    val uri: Uri = Uri.parse(""),
    val albumArt: Any = R.mipmap.ic_launcher)