package kartollika.vkeducation.audioplayer.data.models

import android.net.Uri

data class AudioTrack(
    val title: String = "",
    val artist: String = "",
    val howLong: Int = 0,
    val isExplicit: Boolean = false,
    val uri: Uri = Uri.parse(""),
    val albumArt: String = "")