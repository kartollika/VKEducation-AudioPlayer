package kartollika.vkeducation.audioplayer.data.models

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AudioTrack(
    val title: String = "",
    val artist: String = "",
    val howLong: Int = 0,
    val isExplicit: Boolean = false,
    val uri: Uri = Uri.parse("")
) : Parcelable