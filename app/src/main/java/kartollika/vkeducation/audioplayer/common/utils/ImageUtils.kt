package kartollika.vkeducation.audioplayer.common.utils

import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import kartollika.vkeducation.audioplayer.R
import java.util.*

fun ImageView.setImageResource(imageResource: Any) {
    when (imageResource) {
        is Uri -> setImageURI(imageResource)
        is Bitmap -> setImageBitmap(imageResource)
        is Int -> setImageResource(imageResource)
        else -> setImageDrawable(null)
    }
}

fun getRandomPreviewImage(): Int {
    return when (Random().nextInt(4)) {
        0 -> R.drawable.preview1
        1 -> R.drawable.preview3
        2 -> R.drawable.preview4
        3 -> R.drawable.preview5
        else -> R.drawable.ic_baseline_music_off_24px
    }
}